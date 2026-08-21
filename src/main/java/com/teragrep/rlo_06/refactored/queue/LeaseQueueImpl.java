package com.teragrep.rlo_06.refactored.queue;

import com.teragrep.buf_01.buffer.lease.TrackedLease;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class LeaseQueueImpl implements LeaseQueue {
    private final Queue<TrackedLease<MemorySegment>> leases;
    private final List<Fragment> registrations;

    public LeaseQueueImpl() {
        this(new ConcurrentLinkedDeque<>(), new ArrayList<>());
    }

    public LeaseQueueImpl(final Queue<TrackedLease<MemorySegment>> leases) {
        this(leases, new ArrayList<>());
    }

    public LeaseQueueImpl(final Queue<TrackedLease<MemorySegment>> leases, final List<Fragment> registrations) {
        this.leases = leases;
        this.registrations = registrations;
    }

    @Override
    public void push(final TrackedLease<MemorySegment> trackedLease) {
        leases.add(trackedLease);
        registrations.forEach(registration -> {
           registration.apply(trackedLease);
        });
    }

    @Override
    public void register(final Fragment fragment) {
        registrations.add(fragment);
    }
}
