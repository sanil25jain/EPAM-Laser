# Race Condition, Instruction Reordering & Memory Model

## Overview

This repository demonstrates how multithreaded programs interact with shared memory in Java. It explores race conditions, lost updates, synchronization, the Java Memory Model (JMM), instruction reordering, and visibility guarantees using `volatile`.

## Task Specifications & Environment

| Property | Details |
|---|---|
| **Task Name** | Race Condition, Instruction Reordering & Memory Model |
| **Assigned Members** | Sanil Jain, Radhika Shukla & Paras Dubey|
| **CPU** | Apple M1 |
| **Operating System** | macOS 26.6.2 (Build 25G83) |
| **JDK** | OpenJDK 21.0.12.1 (Homebrew) |
| **JVM** | OpenJDK 64-Bit Server VM |
| **Language** | Java |
| **Tools** | VS Code, Terminal |

## Topics Covered

### Phase 1: Race Condition & Instruction Reordering

- **Process vs. Thread Basics:** Differences between process memory isolation and threads sharing memory while maintaining individual stacks.
- **Concurrency & Parallelism:** Understanding interleaved execution and simultaneous execution across multiple CPU cores.
- **Shared Mutable State & Lost Updates:** Exploring how read-modify-write operations such as `counter++` can produce incorrect results without proper synchronization.
- **Synchronization:** Using locks, critical sections, and the `synchronized` keyword to enforce mutual exclusion and thread safety.

### Phase 2: Java Memory Model

- **Java Memory Model (JMM):** Understanding the rules governing interactions between threads and shared variables.
- **Instruction Reordering & Visibility:** Exploring how compilers and processors can reorder operations while preserving required program behavior, and how other threads observe shared-state changes.
- **Volatile & Happens-Before:** Understanding visibility guarantees and the happens-before relationship established by volatile reads and writes.

## Experimental Results

### Phase 1: Counter Increments

**Configuration:** 4 threads × 1,000,000 iterations per thread.

- **Expected Counter Value:** `4,000,000`
- **Without Synchronization:** Lost updates cause an incorrect final counter value.
- **With Synchronization:** The final counter value matches the expected result.

| Run | Without `synchronized` | With `synchronized` |
|---|---:|---:|
| 1 | 1,944,382 | 4,000,000 |
| 2 | 1,323,114 | 4,000,000 |
| 3 | 1,034,609 | 4,000,000 |
| 4 | 2,249,834 | 4,000,000 |
| 5 | 2,770,255 | 4,000,000 |

**Observation:** The unsynchronized implementation produces inconsistent results because multiple threads can update the shared counter concurrently. Synchronization protects the critical section and ensures that all increments are accounted for.

### Phase 2: Volatile Visibility & Publication

**Writer Thread:**
```java
data = 42;
ready = true;
```

**Reader Thread:**
```java
while (!ready) {
    // Wait until ready becomes true
}
System.out.println("Data = " + data);
```

The experiment compares two implementations: one without `volatile` and another using `volatile` for the shared readiness flag.

| Run | Without `volatile` | With `volatile` |
|---|---|---|
| 1–5 | `Data = 42` | `Data = 42` |

**Observation:** Both implementations printed `Data = 42` during the recorded test runs. However, these results do not prove that the non-volatile implementation is correct.

When `ready` is declared `volatile`, the writer's write to `ready` happens-before a subsequent reader's read of `ready` that observes the write. Consequently, the preceding write to `data` becomes visible to the reader, assuming the variables are accessed in the stated order and the reader observes the published flag.

Without `volatile` or another suitable synchronization mechanism, the Java Memory Model does not guarantee the same visibility behavior.

## Key Learnings

- Race conditions can cause lost updates when multiple threads modify shared mutable state.
- `counter++` is not an atomic operation.
- The `synchronized` keyword provides mutual exclusion and memory visibility guarantees.
- The Java Memory Model defines the rules for visibility, ordering, and synchronization between threads.
- `volatile` provides visibility and ordering guarantees but does not make compound operations such as `counter++` atomic.
- The happens-before relationship provides a formal basis for reasoning about inter-thread visibility.
- A successful experimental run does not necessarily prove that a concurrent program is correct.

## Technologies Used

- **Programming Language:** Java
- **JDK:** OpenJDK 21
- **Development Environment:** Visual Studio Code
- **Execution Tools:** Terminal

## References

1. **Oracle** — [Java Language Specification, Chapter 17: Threads and Locks](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)
2. **Vlad Zuev** — [Java Multithreading, Lesson 59: Java Memory Model](https://youtu.be/EDGjm6zmass?si=ybqdLgaeu7w4Q_th)
3. **EverythingAboutJava** — [Happens-Before Relationship in Java](https://youtu.be/W-1Te6Bl-p8?si=w5c8swoiHAtUmK4c)

## Contributors

- **Sanil Jain**
- **Radhika Shukla**
- **Paras Dubey**
---
