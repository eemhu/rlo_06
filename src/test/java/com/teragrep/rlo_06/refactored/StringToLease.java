package com.teragrep.rlo_06.refactored;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.lease.collection.TrackedLeaseCollection;
import com.teragrep.buf_01.buffer.lease.collection.TrackedMemorySegmentLeaseCollection;
import com.teragrep.buf_01.buffer.pool.OpeningPool;
import com.teragrep.buf_01.buffer.pool.get.LeaseMultiGet;
import com.teragrep.buf_01.buffer.pool.get.TrackedLeaseMultiGet;

import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class StringToLease {

    private final String origin;
    private final OpeningPool pool;

    public StringToLease(final String origin, final OpeningPool pool) {
        this.origin = origin;
        this.pool = pool;
    }

    public List<TrackedLease<MemorySegment>> toList() {
        final byte[] bytes = origin.getBytes(StandardCharsets.UTF_8);
        final List<TrackedLease<MemorySegment>> trackedLeases = new TrackedLeaseMultiGet(new LeaseMultiGet(pool))
                .getAsList(bytes.length);

        int i = 0;
        for (final TrackedLease<MemorySegment> trackedLease : trackedLeases) {
            while (trackedLease.hasNext() && i < bytes.length) {
                trackedLease.write(bytes[i]);
                i++;
            }
            trackedLease.flip();
        }

        return trackedLeases;
    }
}
