# Task 3 — CAS & Lock-Free Design

## 1. Overview

This task demonstrates Compare-And-Set (CAS) and lock-free programming in Java. Multiple threads attempt to update one shared maximum value. The implementations use `AtomicInteger.compareAndSet()` instead of `synchronized` or traditional locks.

The task includes two versions:

- **Ayush's implementation:** fixes the total workload at 1,000,000 operations and records CAS attempts, successes, failures, and execution time.
- **Ananya's implementation:** provides a simple four-thread example and a reusable benchmark that tests 1, 2, 4, 8, and 16 threads.

The benchmarks are reported separately because they use different workload definitions.

## 2. Objectives

- Understand concurrent execution using multiple threads.
- Implement a shared maximum-value updater.
- Use CAS for atomic updates.
- Avoid `synchronized` and traditional mutex locks.
- Explain CAS failure and retry behavior.
- Verify the final maximum.
- Measure execution time and record results.

## 3. Technologies

- Java 21
- `AtomicInteger` and `AtomicLong`
- Java threads
- `compareAndSet()`
- Maven (Ayush's project)
- CSV benchmark output

## 4. How CAS Works

CAS updates a value only if the value currently stored is equal to the expected value.

1. Read the current maximum.
2. If the new value is not greater than the maximum, stop.
3. Otherwise, attempt `compareAndSet(current, value)`.
4. If the stored value has not changed, the update succeeds.
5. If another thread changed the value first, CAS fails. The thread reads the latest value and retries if an update is still needed.

Example:

```
Initial maximum = 50

Thread 1 reads 50
Thread 2 reads 50

Thread 1: CAS(50, 80) -> SUCCESS
Thread 2: CAS(50, 70) -> FAILURE

Thread 2 reads 80
70 <= 80, so no update is needed
```

The implementations do not use `synchronized`, `Lock`, or `ReentrantLock`.

## 5. Implementations

### 5.1 Ayush — CAS maximum with statistics

Maven source files:

```
src/main/java/com/teamlaser/cas/
├── CASCounter.java
└── CASBenchmark.java
```

`CASCounter` stores the shared maximum in an `AtomicInteger`. It also uses `AtomicLong` values to record CAS attempts, successes, and failures.

`CASBenchmark` tests 1, 2, 4, 8, and 16 threads. The total workload stays fixed at 1,000,000 operations and is divided among the threads.

| Threads | Operations per thread | Total operations |
|--------:|----------------------:|-----------------:|
| 1  | 1,000,000 | 1,000,000 |
| 2  | 500,000   | 1,000,000 |
| 4  | 250,000   | 1,000,000 |
| 8  | 125,000   | 1,000,000 |
| 16 | 62,500    | 1,000,000 |

Expected final maximum for every test: **1,000,000**.

### 5.2 Ananya — LockFreeMax.java

This is a simple four-thread example. Each worker processes 1,000 values and attempts to update the shared maximum. After all threads finish, the program prints the final maximum and execution time and writes the result to `results.csv`.

### 5.3 Ananya — LockFreeMax1.java

This version uses a reusable `runTest(threadCount, iterations)` method. It runs tests with 1, 2, 4, 8, and 16 threads. Each thread performs the specified number of iterations, and the program records the final maximum and execution time.

**Important:** In Ananya's benchmark, `iterations` is **per thread**. With 1,000 iterations per thread, total work increases as thread count increases. It is a different experiment from Ayush's fixed-total-work benchmark, so the timings should not be compared directly.

## 6. Benchmark Results

### 6.1 Ayush's fixed-workload results

These are the supplied results from the completed run.

| Threads | Operations / thread | Total operations | Expected max | Actual max | CAS attempts | CAS successes | CAS failures | Time (ms) |
|--------:|--------------------:|-----------------:|-------------:|-----------:|-------------:|--------------:|-------------:|----------:|
| 1  | 1,000,000 | 1,000,000 | 1,000,000 | 1,000,000 | 1,000,000 | 1,000,000 | 0   | 11.141916 |
| 2  | 500,000   | 1,000,000 | 1,000,000 | 1,000,000 | 503,071   | 503,070   | 1   | 14.272666 |
| 4  | 250,000   | 1,000,000 | 1,000,000 | 1,000,000 | 255,640   | 255,218   | 422 | 2.956750  |
| 8  | 125,000   | 1,000,000 | 1,000,000 | 1,000,000 | 141,346   | 141,307   | 39  | 1.754458  |
| 16 | 62,500    | 1,000,000 | 1,000,000 | 1,000,000 | 82,339    | 81,801    | 538 | 1.507291  |

All five configurations produced the expected maximum of 1,000,000.

### 6.2 Ananya's results

Ananya's measurements are stored separately in `results.csv`, with columns for thread count, iterations per thread, final maximum, and execution time. Use the supplied CSV as the source of truth for those measurements.

Because the workload is 1,000 iterations per thread, total iterations increase as more threads are used. Keep these results separate from Ayush's fixed-workload table.

## 7. Interpreting the Results

- **Correctness:** Ayush's actual maximum equals the expected maximum for all tested thread counts.
- **CAS failure:** A CAS failure means the shared value changed between the read and the attempted update. The implementation retries when necessary.
- **Timing:** Execution time does not have to decrease every time thread count increases. Thread scheduling, startup overhead, CPU resources, and contention can affect results.
- **Fair comparison:** Ayush's benchmark holds total work constant; Ananya's benchmark holds work per thread constant. They answer different performance questions.

## 8. Experimental Setup

| Parameter | Value |
|---|---|
| Language | Java |
| JDK | Java 21 |
| Build tool for Ayush's project | Maven |
| Atomic operation | `AtomicInteger.compareAndSet()` |
| Ayush workload | 1,000,000 total operations |
| Ayush thread counts | 1, 2, 4, 8, 16 |
| Ananya workload | 1,000 iterations per thread |
| Traditional locks | Not used |
| Timing method | `System.nanoTime()` |

## 9. Run Ayush's Maven Benchmark

Run these commands from the `LockFreeCAS` project directory:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass=com.teamlaser.cas.CASBenchmark
cat results/benchmark-results.csv
```

Output file:

```
results/benchmark-results.csv
```

The `results/` directory must exist before running the benchmark.

## 10. Run Ananya's Standalone Examples

Run these commands from the directory containing the source files:

```bash
javac LockFreeMax.java
java LockFreeMax
```

For the multi-thread benchmark:

```bash
javac LockFreeMax1.java
java LockFreeMax1
```

These programs write `results.csv` in the current working directory. Run them from the directory where you want that file created.

## 11. Deliverables

- CAS-based lock-free maximum updater.
- Multiple-thread test cases.
- CAS success, failure, and retry explanation.
- Correctness check against the expected maximum.
- Actual benchmark results and execution-time measurements.
- Ayush's fixed-workload benchmark with CAS statistics.
- Ananya's `LockFreeMax.java` and `LockFreeMax1.java` examples.
- Separate interpretation of both benchmark designs.

## 12. Conclusion

This task demonstrates how Java's CAS operation can safely update a shared maximum without traditional locks. Ayush's benchmark adds detailed CAS statistics and a fixed total workload, while Ananya's examples provide a simpler implementation and a reusable multi-thread test. Together, they demonstrate atomic updates, CAS failure and retry behavior, correctness checking, and basic performance measurement.
