package com.teragrep.rlo_06.refactored.queue;

import com.teragrep.buf_01.buffer.lease.TrackedLease;

import java.lang.foreign.MemorySegment;

public interface LeaseQueue {
    public abstract void push(final TrackedLease<MemorySegment> trackedLease);
    public abstract void register(final Fragment fragment);
}
