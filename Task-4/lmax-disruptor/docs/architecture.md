# Architecture Overview

This project is the third step in a learning journey. The earlier versions built the core ideas by hand, and this one swaps in the real LMAX Disruptor library to validate the same ideas under production-style code.

In plain English, the project is testing a queue that can move data between threads with minimal overhead, while making sure messages are not lost, duplicated, or reordered in ways that break the application.

## How the library maps to the custom version

| Custom hand-written version | Real library equivalent |
|----------------------------|-------------------------|
| Ring buffer and slot management | Disruptor with a configured ring buffer |
| Event factory to create per-slot objects | EventFactory for pre-allocation |
| Manual handler logic | EventHandler implementations |
| Manual processor threads | DSL-managed workers created by the library |
| Sequence barrier wiring | handleEventsWith(...).then(...) DSL chaining |
| Producer gating and sequencing | Built-in sequence tracking and coordination |
| Single-producer design | ProducerType.SINGLE or ProducerType.MULTI |
| Busy-spin strategy | BusySpinWaitStrategy and other configurable strategies |

## Typical usage flow

```java
Disruptor<ValueEvent> disruptor = new Disruptor<>(
        ValueEvent.FACTORY,
        1024,
        threadFactory,
        ProducerType.SINGLE,
        new BusySpinWaitStrategy());

Disruptor.handleEventsWith(stage1).then(stage2);
RingBuffer<ValueEvent> ring = disruptor.start();

long seq = ring.next();
ring.get(seq).value = 42;
ring.publish(seq);

disruptor.shutdown();
```

This pattern is what makes the library so useful: you describe the processing flow, and the framework handles the coordination.

## What the library adds over the hand-written version

- Multi-producer support with atomic slot claiming
- Better handling for concurrent publishing without corrupting data
- Configurable wait strategies to trade performance for CPU usage
- Built-in support for pipelines, fan-out, and dependency chains
- A mature, battle-tested implementation that is far easier to reason about in production code

## Why the multi-producer scenario matters

The most interesting case is the two-producer scenario. Each producer claims its own sequence, but the order in which they publish may differ from the order in which they claimed slots.

The library keeps track of the actual published events and makes sure the consumer only advances through finished work. The test checks that:

- each producer keeps its own message order
- nothing is lost
- nothing is duplicated
- the final stream remains valid

That is the key reason this benchmark is useful: it verifies not just speed, but correctness under real concurrency.
