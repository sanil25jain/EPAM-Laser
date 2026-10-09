# Lock-Free Ring Buffer

This project is a simple Java example of a lock-free producer/consumer buffer.
It is made to help understand how high-speed queues work without using locks.

## What this project is doing

This project shows how two threads can share data safely:

- one thread writes data
- one thread reads data
- the data flows in a fixed circular buffer
- no blocking locks are used

It is a small learning version inspired by the LMAX Disruptor design.

## Why this is useful

Normal queues often use locks. Locks are easy to understand, but they can slow programs down when many threads are working at the same time.

This project avoids that by using:

- a ring buffer
- sequence numbers
- memory ordering with `VarHandle`
- a circular array for reuse of memory

This helps keep the system fast and simple for a producer/consumer pattern.

## How the ring buffer works in simple words

Imagine a round table with fixed seats.

- The producer sits on one side and writes numbers
- The consumer sits on the other side and reads numbers
- When the table is full, the producer waits
- When the table is empty, the consumer waits
- After the last seat, it starts again from the beginning

That is the ring buffer idea.

## What is a sequence number?

A sequence number is just a counter.

- `writeSequence` tells us how many items were produced
- `readSequence` tells us how many items were consumed

The real slot in the array is calculated with a simple formula:

```java
slot = sequence & (capacity - 1)
```

Because the size is a power of two, this works like a fast wrap-around.

## What this project demonstrates

1. A lock-free single producer + single consumer queue
2. Circular storage of data
3. Safe memory ordering between threads
4. A comparison between a normal counter and a cache-friendly padded counter
5. A test that checks no data is lost or reordered

## Build and run tests

Install a JDK (Java 11 or newer), then check that both Java and the compiler are available:

```text
java -version
javac -version
```

Open a terminal in the project folder (the folder containing this README).

### macOS and Linux

Build the project:

```bash
bash scripts/build.sh
```

Run the test program after building:

```bash
bash scripts/run.sh
```

### Windows Command Prompt

Build the project:

```bat
scripts\build.bat
```

Run the test program after building:

```bat
scripts\run.bat
```

Both build scripts place compiled files in the `out` folder. To build and run the test in one command, use `bash scripts/build.sh && bash scripts/run.sh` on macOS/Linux, or `scripts\build.bat && scripts\run.bat` in Windows Command Prompt.

### What the test checks

`RingBufferTest` sends 100,000 numbered messages from one producer thread to one consumer thread. It checks that the messages arrive in the original order, with no missing or repeated values. A successful run prints `[PASS]`.

## Important note

This is not the official LMAX Disruptor library.
It is a small educational version inspired by it.

## Requirements

- Java 11 or newer
- No external dependencies

Check Java version:

```bash
java -version
```

## Project structure

```text
src/main/java/com/team/ringbuffer/
    LockFreeRingBuffer.java
    PaddedSequence.java
    RingBufferTest.java
    Sequence.java
```


