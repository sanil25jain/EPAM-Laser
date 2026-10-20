# How the Ring Buffer Works

This project passes data from one producer thread to one consumer thread using a fixed-size array. The array is treated like a loop: after the last slot, the next item goes back to the first slot.

The producer adds items with `publish()`. The consumer removes them with `poll()`. If there is no room or no item available, those methods return right away. The caller can try again later; the ring buffer does not put either thread to sleep.

## The Buffer at a Glance

```text
                         Ring buffer (capacity = 8)
                    ┌─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐
                    │  0  │  1  │  2  │  3  │  4  │  5  │  6  │  7  │
                    └─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┘
                       ▲                                   ▲
                  readSequence                       writeSequence
                       │                                   │
                    Consumer                            Producer

  Empty: writeSequence == readSequence
  Full:  writeSequence - readSequence == capacity
  Slot:  sequence & (capacity - 1)
```

The read sequence marks the next item the consumer should take. The write sequence marks the next free position for the producer. Their difference tells us how many items are waiting in the buffer.

## Why Use Sequence Numbers?

Instead of moving an index back to zero every time it reaches the end of the array, the buffer keeps increasing read and write counters. To find the matching array slot, it uses:

```text
slot = sequence & (capacity - 1)
```

The capacity must be a power of two, such as 8, 16, or 1024. With that rule, this bit operation wraps the sequence around to a valid array slot. For example, in an eight-slot buffer, sequence 10 uses slot 2.

The counters also make it easy to tell whether the buffer is empty or full: compare the read and write sequences instead of keeping a separate item count.

## How Items Become Visible Between Threads

The producer and consumer share the array, so each must see the other thread's work in the right order. The code uses Java `VarHandle` operations called **release** and **acquire** to coordinate this without a lock.

### When the producer publishes an item

1. It writes the item into the array slot.
2. It updates the write sequence with a release operation.

That release update makes the slot write visible before the new sequence is observed. The consumer therefore won't see an updated write sequence and then read an older value from that slot.

### When the consumer reads an item

1. It reads the write sequence with an acquire operation.
2. If an item is available, it reads and clears the array slot.
3. It updates the read sequence with a release operation.

The acquire read ensures the consumer sees the producer's item after seeing the updated write sequence. The release update to the read sequence tells the producer that the slot can be reused.

In short, the sequence updates are the hand-off signals: the producer says, "this item is ready," and the consumer says, "this slot is free again."

## What “Lock-Free” Means Here

The ring buffer does not use locks or wait for another thread inside `publish()` or `poll()`. Each call makes one attempt and reports whether it succeeded:

- `publish(item)` returns `false` when the buffer is full.
- `poll()` returns `null` when the buffer is empty.

The test program retries until all messages have been sent and received. This retry loop uses CPU while it waits for the other thread to make progress, so the code is non-blocking but can still spin under load. This implementation is designed for **one producer and one consumer**; it is not a general multi-producer or multi-consumer queue.

## How This Relates to the LMAX Disruptor

The Disruptor uses a ring buffer and sequence numbers too, but it is a larger system designed for more kinds of workloads. It adds features such as:

- **Sequence barriers:** coordinate consumers and let them depend on other consumers.
- **Batching:** handle several items with fewer coordination steps.
- **Pre-allocated events:** reuse event objects to reduce memory allocation.
- **Cache-line padding:** reduce interference when different threads update nearby counters.

This project is a small learning example, not the Disruptor library. `PaddedSequence.java` demonstrates padding a counter, but the ring buffer currently uses its own `readSequence` and `writeSequence` fields rather than `PaddedSequence`.

## Performance

Throughput depends on the processor, Java runtime, operating system, and what else is running. See [Benchmark Notes](benchmarks.md) for measurements and the environment used for them. Treat those results as examples, not a guarantee for another machine.

## References

- Thompson, Farley, Barker, Gee, and Stewart. *Disruptor: High performance alternative to bounded queues for exchanging data between concurrent threads.* 2011.
- Martin Thompson's *Mechanical Sympathy* blog.
- JEP 193: Variable Handles.
- JSR 133: Java Memory Model and Thread Specification.