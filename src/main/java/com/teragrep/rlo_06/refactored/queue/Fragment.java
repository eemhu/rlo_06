package com.teragrep.rlo_06.refactored.queue;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.stb_01.Stubable;

import java.lang.foreign.MemorySegment;

public interface Fragment extends Stubable {
    public abstract FragmentState state();
    public abstract Fragment apply(final TrackedLease<MemorySegment> trackedLease);
    public abstract TrackedLease<MemorySegment>[] leases();
    public abstract TrackedLease<MemorySegment>[] result();
}
