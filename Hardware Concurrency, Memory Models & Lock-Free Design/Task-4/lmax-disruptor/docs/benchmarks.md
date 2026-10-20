# Benchmark Notes

Current benchmark from a verified local run using 100,000 messages.

## How to measure

    ./scripts/build.sh
    ./scripts/run.sh 100000

## Environment

- Java: OpenJDK 26.0.1 (64-bit)
- Maven: 3.9.12
- OS: macOS 27.0.1 (Darwin, arm64)
- CPU / cores: Apple M4, 10 cores
- Disruptor version: 3.4.4

## Results

### Verified run with 100,000 messages

| Scenario                          | Elapsed (ms) | Throughput (M msg/sec) |
|-----------------------------------|--------------|------------------------|
| 1P -> 1C, claim 1                 | 4.22         | 23.67                  |
| 1P -> 1C, claim 64                | 1.58         | 63.14                  |
| 1P -> stage1 -> stage2 (pipeline) | 10.17        | 9.83                   |
| 2P (MULTI) -> 1C                  | 11.26        | 8.88                   |

### Additional run with 1,000,000 messages

| Scenario                          | Elapsed (ms) | Throughput (M msg/sec) |
|-----------------------------------|--------------|------------------------|
| 2P (MULTI) -> 1C                  | 62.30        | 16.05                  |

## Sample console output

```text
=== LMAX Disruptor (library 3.4.4) Test ===
Capacity         : 1024
Total messages   : 100000

Scenario         : 1P -> 1C (claim 1)
Producer started : producer
Consumer started : consumer

... running ...

--- Results ---
Messages sent     : 100000
Messages received : 100000
Order preserved   : true
Data lost         : 0
Data duplicated   : 0
Elapsed           : 4.22 ms
Throughput        : 23.67 M msg/sec

[PASS] No loss, no duplication, strict FIFO ordering.
```

## Comparing with the hand-written version

Run `lmax-disruptor-demo` on the same machine with the same message count
and compare scenarios 1–3. Expect scenario 4 to be slower than 1: two
producers contend on the claim CAS.

## Notes

- Use at least 3 free cores (4 for scenario 4); busy-spinning threads sharing a core give very low, noisy numbers.
- Always keep the warm-up; the JIT needs it.
- These numbers come from a single verified run of the current project on this machine.
