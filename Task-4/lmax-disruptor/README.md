# LMAX Disruptor Library Demo

This project is a practical comparison between a hand-built ring buffer and the real LMAX Disruptor library from Maven Central.

The goal is simple: test whether the production library behaves the same way as the custom implementation, while also checking that it handles more advanced cases like multiple producers and staged pipelines.

## What this project does

It runs several real-world queueing scenarios using the library version 3.4.4:

1. One producer sending to one consumer, one slot at a time
2. One producer sending to one consumer in batches of 64
3. A pipeline where one stage rewrites data and the next verifies it
4. Two producers feeding one consumer, which is a pattern the hand-written version cannot support as easily

Every scenario includes a warm-up phase and then verifies:

- no lost messages
- no duplicate messages
- correct ordering
- correct per-producer ordering in the multi-producer case

## Requirements

Before running the project, make sure you have:

- Java 11 or newer
- Maven 3.6 or newer
- Internet access for the first build so Maven can download the Disruptor dependency

## Quick start

From the project root, run:

```bash
mvn test
```

To run the actual demo benchmark:

```bash
mvn exec:java -Dexec.mainClass=com.team.disruptorlib.DisruptorLibraryTest
```

You can also run the provided shell scripts:

```bash
./scripts/build.sh
./scripts/run.sh
./scripts/run.sh 100000
./scripts/run.sh 5000000
```

On Windows:

```bat
scripts\build.bat
scripts\run.bat
```

## Build, run, and test commands

### Build the project

```bash
mvn clean compile
```

### Run tests

```bash
mvn test
```

### Run the benchmark/demo

```bash
mvn exec:java -Dexec.mainClass=com.team.disruptorlib.DisruptorLibraryTest
```

Or with a custom message count:

```bash
mvn exec:java -Dexec.mainClass=com.team.disruptorlib.DisruptorLibraryTest -Dexec.args="100000"
```

## Why the project matters

This is a good example of how a lock-free queue can be used in high-throughput systems. It shows how the LMAX Disruptor library helps with:

- low-latency communication between threads
- batching for better throughput
- pipeline processing across multiple stages
- safe concurrent publishing from multiple producers

## Notes

- The project uses BusySpinWaitStrategy, which is very fast but uses CPU aggressively.
- For reliable benchmark results, keep enough free CPU cores available.
- The benchmark output is intentionally designed to be easy to compare against the hand-written version.

Additional project notes:

- Architecture details: [docs/architecture.md](docs/architecture.md)
- Benchmark results: [docs/benchmarks.md](docs/benchmarks.md)
