package com.teragrep.rlo_06.refactored.queue;

import com.teragrep.buf_01.buffer.lease.TrackedLease;

import java.lang.foreign.MemorySegment;

public final class FragmentStub implements Fragment{
    @Override
    public FragmentState state() {
        throw new UnsupportedOperationException("state() is not provided by the stub object.");
    }

    @Override
    public Fragment apply(final TrackedLease<MemorySegment> trackedLease) {
        throw new UnsupportedOperationException("apply() is not provided by the stub object.");
    }

    @Override
    public TrackedLease<MemorySegment>[] leases() {
        throw new UnsupportedOperationException("leases() is not provided by the stub object.");
    }

    @Override
    public TrackedLease<MemorySegment>[] result() {
        throw new UnsupportedOperationException("result() is not provided by the stub object.");
    }

    @Override
    public String toString() {
        return "FragmentStub{}";
    }

    @Override
    public boolean isStub() {
        return true;
    }
}
