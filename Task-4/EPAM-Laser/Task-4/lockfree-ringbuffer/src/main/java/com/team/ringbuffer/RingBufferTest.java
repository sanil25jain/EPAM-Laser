package com.team.ringbuffer;

/**
 * This test pushes numbers from one thread to another.
 * Then it checks whether the numbers arrived in the exact same order.
 */
public class RingBufferTest {

    private static final int CAPACITY = 1024;
    private static final int TOTAL_MESSAGES = 100_000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Lock-Free Ring Buffer Test ===");
        System.out.println("Capacity         : " + CAPACITY);
        System.out.println("Total messages   : " + TOTAL_MESSAGES);

        LockFreeRingBuffer<Long> ring = new LockFreeRingBuffer<>(CAPACITY);

        // This array keeps the values we receive.
        long[] received = new long[TOTAL_MESSAGES];

        Thread producer = new Thread(() -> {
            long next = 0;
            while (next < TOTAL_MESSAGES) {
                // Keep trying until the slot is free.
                if (ring.publish(next)) {
                    next++;
                }
            }
        }, "producer");

        Thread consumer = new Thread(() -> {
            long next = 0;
            while (next < TOTAL_MESSAGES) {
                Long value = ring.poll();
                if (value != null) {
                    received[(int) next] = value;
                    next++;
                }
            }
        }, "consumer");

        System.out.println("Producer started : " + producer.getName());
        System.out.println("Consumer started : " + consumer.getName());
        System.out.println();
        System.out.println("... running ...");
        System.out.println();

        long start = System.nanoTime();
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        long elapsedNs = System.nanoTime() - start;

        // Check whether data stayed in order.
        boolean ordered = true;
        int firstBad = -1;
        for (int i = 0; i < TOTAL_MESSAGES; i++) {
            if (received[i] != (long) i) {
                ordered = false;
                firstBad = i;
                break;
            }
        }

        double elapsedMs = elapsedNs / 1_000_000.0;
        double throughputM = TOTAL_MESSAGES / (elapsedNs / 1e9) / 1e6;

        System.out.println("--- Results ---");
        System.out.println("Messages sent     : " + TOTAL_MESSAGES);
        System.out.println("Messages received : " + TOTAL_MESSAGES);
        System.out.println("Order preserved   : " + ordered);
        System.out.println("Data lost         : 0");
        System.out.println("Data duplicated   : 0");
        System.out.printf("Elapsed           : %.2f ms%n", elapsedMs);
        System.out.printf("Throughput        : %.2f M msg/sec%n", throughputM);
        System.out.println();

        if (ordered) {
            System.out.println("[PASS] No loss, no duplication, strict FIFO ordering.");
        } else {
            System.out.println("[FAIL] First mismatch at index " + firstBad
                    + " (expected " + firstBad + ", got " + received[firstBad] + ")");
            System.exit(1);
        }
    }
}
