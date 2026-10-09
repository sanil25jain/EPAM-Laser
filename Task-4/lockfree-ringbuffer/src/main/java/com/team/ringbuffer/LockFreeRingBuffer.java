package com.team.ringbuffer;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * A small ring buffer for one producer and one consumer.
 * It is a simple version of a lock-free queue.
 */
public class LockFreeRingBuffer<E> {

    private static final VarHandle READ_SEQ;
    private static final VarHandle WRITE_SEQ;

    static {
        try {
            READ_SEQ = MethodHandles.lookup().findVarHandle(LockFreeRingBuffer.class, "readSequence", long.class);
            WRITE_SEQ = MethodHandles.lookup().findVarHandle(LockFreeRingBuffer.class, "writeSequence", long.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Object[] buffer;
    private final int capacity;
    private final int mask;

    private volatile long readSequence;
    private volatile long writeSequence;

    public LockFreeRingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be > 0");
        }
        if ((capacity & (capacity - 1)) != 0) {
            throw new IllegalArgumentException("Capacity must be a power of two");
        }
        this.capacity = capacity;
        this.mask = capacity - 1;
        this.buffer = new Object[capacity];
    }

    public int capacity() {
        return capacity;
    }

    // Add one item if a free slot exists.
    public boolean publish(E item) {
        if (item == null) {
            throw new IllegalArgumentException("Null values are not supported");
        }

        long write = (long) WRITE_SEQ.getAcquire(this);
        long read = (long) READ_SEQ.getAcquire(this);

        if (write - read >= capacity) {
            return false;
        }

        int slot = (int) (write & mask);
        buffer[slot] = item;
        WRITE_SEQ.setRelease(this, write + 1);
        return true;
    }

    // Take one item if something is available.
    @SuppressWarnings("unchecked")
    public E poll() {
        long read = (long) READ_SEQ.getAcquire(this);
        long write = (long) WRITE_SEQ.getAcquire(this);

        if (read >= write) {
            return null;
        }

        int slot = (int) (read & mask);
        E item = (E) buffer[slot];
        buffer[slot] = null;
        READ_SEQ.setRelease(this, read + 1);
        return item;
    }
}
