package com.team.ringbuffer;

/**
 * A counter with extra empty space around it.
 * This helps reduce cache sharing between threads.
 */
public class PaddedSequence {

    // Extra space before the real value.
    @SuppressWarnings("unused")
    private volatile long p1, p2, p3, p4, p5, p6, p7;

    // Real value used by the counter.
    private volatile long value;

    // Extra space after the real value.
    @SuppressWarnings("unused")
    private volatile long q1, q2, q3, q4, q5, q6, q7;

    public PaddedSequence(long initialValue) {
        this.value = initialValue;
    }

    public long get() {
        return value;
    }

    public void set(long v) {
        this.value = v;
    }

    public long incrementAndGet() {
        return ++value;
    }
}
