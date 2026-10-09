package com.team.disruptorlib;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

/**
 * Same verification as the hand-written mini Disruptor, but running on the
 * real LMAX Disruptor library (com.lmax:disruptor 3.4.4).
 *
 * <p>
 * Scenarios (each preceded by an untimed warm-up):
 * <ol>
 * <li>1 producer -> 1 consumer, claim 1 slot per publish.</li>
 * <li>1 producer -> 1 consumer, claim 64 slots per publish (batching).</li>
 * <li>Pipeline: producer -> stage 1 (rewrites event) -> stage 2
 * (verifies).</li>
 * <li>2 producers (ProducerType.MULTI) -> 1 consumer. The hand-written
 * version cannot do this; the library can.</li>
 * </ol>
 *
 * <p>
 * Usage: {@code DisruptorLibraryTest [totalMessages]} (default 100,000).
 */
public class DisruptorLibraryTest {

    private static final int BUFFER_SIZE = 1024;
    private static final int DEFAULT_MESSAGES = 100_000;
    private static final int CLAIM_BATCH = 64;
    private static final int PRODUCERS = 2;
    private static final int COUNTER_BITS = 40; // multi-producer event = (producerId << 40) | counter

    // ---------------------------------------------------------------- handlers

    /** Checks event i carries exactly i * multiplier, in order. */
    private static final class CheckingHandler implements EventHandler<ValueEvent> {
        private final long multiplier;
        private long expected;
        private long errors;
        private long firstBad = -1;
        private long batches;
        volatile long processed; // written once per batch (cheap), read by the main thread

        CheckingHandler(long multiplier) {
            this.multiplier = multiplier;
        }

        @Override
        public void onEvent(ValueEvent e, long seq, boolean endOfBatch) {
            if (seq != expected || e.value != expected * multiplier) {
                if (errors++ == 0)
                    firstBad = seq;
            }
            expected++;
            if (endOfBatch) {
                batches++;
                processed = expected;
            }
        }
    }

    /** Pipeline stage 1: doubles the value in place. */
    private static final class DoublingHandler implements EventHandler<ValueEvent> {
        @Override
        public void onEvent(ValueEvent e, long seq, boolean endOfBatch) {
            e.value = e.value * 2;
        }
    }

    /**
     * Multi-producer checker: each producer's own events must arrive 0,1,2,... in
     * order.
     */
    private static final class MultiCheckingHandler implements EventHandler<ValueEvent> {
        private final long[] nextPerProducer = new long[PRODUCERS];
        private long count;
        private long errors;
        private long firstBad = -1;
        private long batches;
        volatile long processed;

        @Override
        public void onEvent(ValueEvent e, long seq, boolean endOfBatch) {
            int producer = (int) (e.value >>> COUNTER_BITS);
            long counter = e.value & ((1L << COUNTER_BITS) - 1);
            if (producer < 0 || producer >= PRODUCERS || counter != nextPerProducer[producer]++) {
                if (errors++ == 0)
                    firstBad = seq;
            }
            count++;
            if (endOfBatch) {
                batches++;
                processed = count;
            }
        }
    }

    // ----------------------------------------------------------------- helpers

    private static final class Result {
        String name;
        long elapsedNs;
        long errors;
        long firstBad;
        long consumed;
        long expected;
        long batches;
    }

    private static ThreadFactory daemonThreads() {
        AtomicInteger n = new AtomicInteger();
        return r -> {
            Thread t = new Thread(r, "disruptor-" + n.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
    }

    // --------------------------------------------------- single-producer runs

    private static Result run(String name, int total, int claimBatch, boolean pipeline)
            throws InterruptedException {

        Disruptor<ValueEvent> disruptor = new Disruptor<>(
                ValueEvent.FACTORY, BUFFER_SIZE, daemonThreads(),
                ProducerType.SINGLE, new BusySpinWaitStrategy());

        CheckingHandler checker;
        if (pipeline) {
            checker = new CheckingHandler(2);
            disruptor.handleEventsWith(new DoublingHandler()).then(checker);
        } else {
            checker = new CheckingHandler(1);
            disruptor.handleEventsWith(checker);
        }

        RingBuffer<ValueEvent> ring = disruptor.start();

        long start = System.nanoTime();
        long published = 0;
        while (published < total) { // this (main) thread is the single producer
            int n = (int) Math.min(claimBatch, total - published);
            long hi = ring.next(n); // claim n slots
            long lo = hi - n + 1;
            for (long s = lo; s <= hi; s++) {
                ring.get(s).value = s; // fill pre-allocated event in place
            }
            ring.publish(lo, hi); // make the whole range visible
            published += n;
        }
        while (checker.processed < total) {
            Thread.onSpinWait();
        }
        long elapsedNs = System.nanoTime() - start;

        disruptor.shutdown();

        Result r = new Result();
        r.name = name;
        r.elapsedNs = elapsedNs;
        r.errors = checker.errors;
        r.firstBad = checker.firstBad;
        r.consumed = checker.processed;
        r.expected = total;
        r.batches = checker.batches;
        return r;
    }

    // ---------------------------------------------------- multi-producer run

    private static Result runMulti(int total) throws InterruptedException {
        final int perProducer = total / PRODUCERS;
        final int expectedTotal = perProducer * PRODUCERS;

        Disruptor<ValueEvent> disruptor = new Disruptor<>(
                ValueEvent.FACTORY, BUFFER_SIZE, daemonThreads(),
                ProducerType.MULTI, new BusySpinWaitStrategy());

        MultiCheckingHandler checker = new MultiCheckingHandler();
        disruptor.handleEventsWith(checker);
        RingBuffer<ValueEvent> ring = disruptor.start();

        Thread[] producers = new Thread[PRODUCERS];
        for (int p = 0; p < PRODUCERS; p++) {
            final long producerId = p;
            producers[p] = new Thread(() -> {
                for (long i = 0; i < perProducer; i++) {
                    long seq = ring.next(); // thread-safe claim (CAS)
                    ring.get(seq).value = (producerId << COUNTER_BITS) | i;
                    ring.publish(seq);
                }
            }, "producer-" + p);
        }

        long start = System.nanoTime();
        for (Thread t : producers)
            t.start();
        for (Thread t : producers)
            t.join();
        while (checker.processed < expectedTotal) {
            Thread.onSpinWait();
        }
        long elapsedNs = System.nanoTime() - start;

        disruptor.shutdown();

        Result r = new Result();
        r.name = PRODUCERS + "P (MULTI) -> 1C";
        r.elapsedNs = elapsedNs;
        r.errors = checker.errors;
        r.firstBad = checker.firstBad;
        r.consumed = checker.processed;
        r.expected = expectedTotal;
        r.batches = checker.batches;
        return r;
    }

    // -------------------------------------------------------------------- main

    private static void printBanner(String scenario, String producers, String consumers) {
        System.out.println("Scenario         : " + scenario);
        System.out.println("Producer started : " + producers);
        System.out.println("Consumer started : " + consumers);
        System.out.println();
        System.out.println("... running ...");
        System.out.println();
    }

    /** Prints the results block; returns true if the scenario passed. */
    private static boolean printResults(Result r, String orderingText) {
        long lost = Math.max(0, r.expected - r.consumed);
        long duplicated = Math.max(0, r.consumed - r.expected);
        boolean ordered = r.errors == 0;
        boolean ok = ordered && lost == 0 && duplicated == 0;

        double elapsedMs = r.elapsedNs / 1_000_000.0;
        double throughputM = r.expected / (r.elapsedNs / 1e9) / 1e6;

        System.out.println("--- Results ---");
        System.out.println("Messages sent     : " + r.expected);
        System.out.println("Messages received : " + r.consumed);
        System.out.println("Order preserved   : " + ordered);
        System.out.println("Data lost         : " + lost);
        System.out.println("Data duplicated   : " + duplicated);
        System.out.printf("Elapsed           : %.2f ms%n", elapsedMs);
        System.out.printf("Throughput        : %.2f M msg/sec%n", throughputM);
        System.out.println();

        if (ok) {
            System.out.println("[PASS] No loss, no duplication, " + orderingText + ".");
        } else {
            System.out.println("[FAIL] Check failed"
                    + (r.errors > 0 ? " - first mismatch at sequence " + r.firstBad : "") + ".");
        }
        System.out.println();
        return ok;
    }

    public static void main(String[] args) throws InterruptedException {
        int total = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_MESSAGES;
        int warmup = Math.min(total, 200_000);

        System.out.println("=== LMAX Disruptor (library 3.4.4) Test ===");
        System.out.println("Capacity         : " + BUFFER_SIZE);
        System.out.println("Total messages   : " + total);
        System.out.println();

        String[] names = {
                "1P -> 1C (claim 1)",
                "1P -> 1C (claim " + CLAIM_BATCH + ")",
                "1P -> stage1 -> stage2 (pipeline)"
        };
        int[] claims = { 1, CLAIM_BATCH, CLAIM_BATCH };
        boolean[] pipelines = { false, false, true };

        boolean allPassed = true;

        for (int i = 0; i < names.length; i++) {
            run(names[i], warmup, claims[i], pipelines[i]); // silent warm-up
            printBanner(names[i], "producer", pipelines[i] ? "stage-1, stage-2" : "consumer");
            Result r = run(names[i], total, claims[i], pipelines[i]);
            allPassed &= printResults(r, "strict FIFO ordering");
        }

        // Multi-producer scenario (library only)
        runMulti(warmup); // silent warm-up
        printBanner(PRODUCERS + "P (MULTI) -> 1C", "producer-0, producer-1", "consumer");
        Result multi = runMulti(total);
        allPassed &= printResults(multi, "per-producer FIFO ordering");

        if (allPassed) {
            System.out.println("[PASS] All scenarios passed.");
        } else {
            System.out.println("[FAIL] At least one scenario failed.");
            System.exit(1);
        }
    }
}
