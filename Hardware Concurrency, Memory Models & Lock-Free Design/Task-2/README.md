# Task 2 — MESI/MOESI, Cache Lines, False Sharing & Cache-Line Bouncing

## Overview

This task explores how cache coherence and false sharing can affect the performance of multithreaded Java programs. Two benchmark versions were tested:

- **False-sharing benchmark:** Two threads update adjacent `volatile long` variables that may occupy the same cache line.
- **Padded benchmark:** Two threads update counters in separate objects, with padding fields intended to reduce the chance of false sharing.

Both versions performed **200 million total operations**: two threads, each performing 100,000,000 increments.

## Measurements

### False Sharing

| Trial | Execution Time |
|---:|---:|
| 1 | 3183.1351 ms |
| 2 | 3788.3040 ms |
| 3 | 3001.7870 ms |
| 4 | 3516.4233 ms |
| 5 | 2803.0595 ms |
| **Average** | **3258.5418 ms** |

### Padded

| Trial | Execution Time |
|---:|---:|
| 1 | 624.0979 ms |
| 2 | 675.9600 ms |
| 3 | 715.4838 ms |
| 4 | 677.4589 ms |
| 5 | 656.9753 ms |
| **Average** | **669.9952 ms** |

## Throughput Comparison

Throughput is calculated as total operations divided by elapsed time. The values below use **200,000,000 operations** for each benchmark.

| Metric | False Sharing | Padded |
|---|---:|---:|
| Average execution time | 3258.54 ms | 670.00 ms |
| Throughput | 61.38 M ops/s | 298.51 M ops/s |

## Experiment Explanation

Two Java benchmark programs were tested to observe the effect of false sharing on performance. In the first version, two threads repeatedly updated two adjacent variables that could reside in the same cache line, causing cache-line bouncing and additional cache-coherence traffic.

In the second version, padding fields were added to the counter objects to increase the separation between the frequently updated values and reduce the chance of false sharing.

Both versions performed the same **200 million total operations** under the same conditions. The false-sharing version had an average execution time of **3258.54 ms** and throughput of **61.38 million operations per second**. The padded version took **670.00 ms** and achieved **298.51 million operations per second**.

In these measurements, the padded version was approximately **4.86 times faster** and reduced average execution time by approximately **79.44%**. The results suggest that reducing cache-line contention improved performance on this system.

> **Note:** Padding fields are intended to reduce false sharing, but ordinary Java fields do not guarantee exact cache-line placement across all JVM implementations. These results are from this specific system and run setup.

## Environment

| Item | Details |
|---|---|
| Programming language | Java |
| JDK | Java 21.0.12 (Microsoft OpenJDK / HotSpot, based on the Java executable path) |
| Operating system | Windows 11 Home Single Language, Version 25H2 (OS Build 26200.9457) |
| CPU | 11th Gen Intel Core i5-1135G7 @ 2.40 GHz |
| CPU cores / logical processors | 4 / 8 |
| RAM | 8 GB |
| Benchmark threads | 2 |
| Iterations per thread | 100,000,000 |
| Total operations | 200,000,000 |
| Measured trials | 5 per version |
| Warm-up | Informal preliminary runs were performed to check that the programs worked; no separate, fixed warm-up phase was recorded. |

## Conclusion

The padded benchmark showed lower execution time and higher throughput than the false-sharing benchmark. This experiment illustrates that the layout of frequently modified variables can affect multithreaded performance because cache coherence operates at cache-line granularity.
