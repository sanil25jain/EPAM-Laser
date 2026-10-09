package com.teamlaser.cas;

import java.io.FileWriter;

public class CASBenchmark {
    // Same total work for every thread configuration
    static final int TOTAL_OPERATIONS = 1_000_000;

    public static void main(String[] args) throws Exception {
        // Thread counts we want to test
        int[] threadCounts = {1, 2, 4, 8, 16};
       // Create CSV file and write the header
        FileWriter file = new FileWriter(
                "results/benchmark-results.csv"
        );

        file.write(
                "threads,total_operations,expected_max,actual_max," +
                "attempts,successes,failures,time_ms\n"
        );
      // Run the test for each thread count
        for (int threads : threadCounts) {

            CASCounter counter = new CASCounter();

            Thread[] workers = new Thread[threads];
        // Divide the same total work among the threads
            int operationsPerThread =
                    TOTAL_OPERATIONS / threads;

            long start = System.nanoTime();
        // Create and start the worker threads
            for (int i = 0; i < threads; i++) {

                int startValue =
                        i * operationsPerThread + 1;

                int endValue =
                        startValue + operationsPerThread;

                workers[i] = new Thread(() -> {

                    for (int value = startValue;
                         value < endValue;
                         value++) {

                        counter.updateMax(value);
                    }
                });

                workers[i].start();
            }
            // Wait for all threads to finish
            for (Thread worker : workers) {
                worker.join();
            }

            long end = System.nanoTime();

            double time =
                    (end - start) / 1_000_000.0;
            
            
            int expected = TOTAL_OPERATIONS;
            int actual = counter.get();

            System.out.println();
            System.out.println("===== CAS MAXIMUM TEST =====");
            System.out.println("Threads       : " + threads);
            System.out.println("Total Work    : " + TOTAL_OPERATIONS);
            System.out.println("Expected Max  : " + expected);
            System.out.println("Actual Max    : " + actual);
            System.out.println("CAS Attempts  : " + counter.getAttempts());
            System.out.println("CAS Successes : " + counter.getSuccesses());
            System.out.println("CAS Failures  : " + counter.getFailures());
            System.out.println("Time (ms)     : " + time);
            
            // Check whether the final result is correct
            if (expected == actual) {
                System.out.println("Status        : PASS");
            } else {
                System.out.println("Status        : FAIL");
            }
            // Save the result to CSV
            file.write(
                    threads + "," +
                    TOTAL_OPERATIONS + "," +
                    expected + "," +
                    actual + "," +
                    counter.getAttempts() + "," +
                    counter.getSuccesses() + "," +
                    counter.getFailures() + "," +
                    time + "\n"
            );
        }

        file.close();

        System.out.println();
        System.out.println("Results saved to results/benchmark-results.csv");
    }
}

