package com.team.disruptorlib;

import com.lmax.disruptor.EventFactory;

/** The mutable event that lives in every ring slot. Created once by the Disruptor, reused forever. */
public final class ValueEvent {

    public long value;

    /** Called by the Disruptor once per slot at construction time (pre-allocation). */
    public static final EventFactory<ValueEvent> FACTORY = ValueEvent::new;
}
