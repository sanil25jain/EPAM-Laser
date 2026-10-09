package com.team.ringbuffer;

/**
 * A simple counter.
 * We keep it here to compare with PaddedSequence.
 */
public class Sequence {

    private volatile long value;

    public Sequence(long initialValue) {
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
