# Lock-Free Ring Buffer vs Disruptor

This project is a small educational lock-free ring buffer inspired by the design ideas behind LMAX Disruptor. It is useful for learning the mechanics of sequence numbers, memory ordering, and bounded circular storage. However, it is not the same thing as the full Disruptor library.

## What this project does

The current implementation demonstrates:

- a producer thread writing values
- a consumer thread reading values
- a fixed-size circular array
- lock-free coordination using sequence counters and `VarHandle`
- strict FIFO ordering with no loss or duplication

This is a compact single-producer / single-consumer pattern.

## What Disruptor is

Disruptor is a production-oriented high-throughput framework created by LMAX. It is designed for low-latency event processing and is much more than a simple queue.

It commonly includes:

- a ring buffer for storing events
- sequence barriers
- wait strategies
- event handlers
- support for batching and multi-stage processing
- coordination for multiple consumers and advanced throughput tuning

## Why the custom ring buffer is still useful

The custom implementation is valuable when you want to understand the core ideas without the extra framework complexity.

It helps explain:

- circular indexing with power-of-two masks
- producer/consumer sequence tracking
- the role of memory fences and ordering
- how a bounded buffer prevents data races without locks

## Why Disruptor may be preferred in production

Disruptor is usually a better choice when you need:

- very high message throughput
- low latency under heavy concurrency
- multiple event consumers or stages
- a framework that already handles coordination and waiting behavior
- more robust production-ready infrastructure than a small learning example

## Important difference

A lock-free ring buffer and a Disruptor are not interchangeable as direct replacements.

A custom ring buffer is a low-level queue implementation. Disruptor is a higher-level event-processing system built around a ring buffer. Replacing one with the other usually requires redesigning the producer/consumer model, event handlers, and sequencing logic.

## Recommended usage

- Keep this implementation for learning, experimentation, and understanding lock-free coordination.
- Use Disruptor when building a real high-throughput event pipeline or messaging system with heavier operational requirements.

## Summary

This repository demonstrates the fundamental ideas behind Disruptor-style architecture in a simple, readable form. It is intentionally minimal, while Disruptor adds production-oriented abstractions and coordination that are not present here.
