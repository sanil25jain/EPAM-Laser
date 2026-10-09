package com.teamlaser.cas;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CASCounter {

    private final AtomicInteger max = new AtomicInteger(0);

    private final AtomicLong attempts = new AtomicLong(0);
    private final AtomicLong successes = new AtomicLong(0);
    private final AtomicLong failures = new AtomicLong(0);

    public void updateMax(int value) {

        while (true) {
             // Read the current maximum
            int current = max.get();

             // No update is needed if our value is smaller
            if (value <= current) {
                return;
            }

            attempts.incrementAndGet();
            // Update only if the value has not changed
            if (max.compareAndSet(current, value)) {
                successes.incrementAndGet();
                return;
            }
            // Another thread changed max, so try again
            failures.incrementAndGet();
        }
    }

    public int get() {
        return max.get();
    }

    public long getAttempts() {
        return attempts.get();
    }

    public long getSuccesses() {
        return successes.get();
    }

    public long getFailures() {
        return failures.get();
    }
}
