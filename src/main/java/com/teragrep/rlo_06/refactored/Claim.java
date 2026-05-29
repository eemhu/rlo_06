package com.teragrep.rlo_06.refactored;

import com.teragrep.buf_01.buffer.lease.TrackedLease;

import java.lang.foreign.MemorySegment;
import java.util.List;

public interface Claim<T> {
    public abstract Result<T> advance(List<TrackedLease<MemorySegment>> src);
}
