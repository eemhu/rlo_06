package com.teragrep.rlo_06.refactored;

import com.teragrep.buf_01.buffer.lease.TrackedLease;

import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.Objects;

public class ResettedLeases {
    private final List<TrackedLease<MemorySegment>> origin;

    public ResettedLeases(final List<TrackedLease<MemorySegment>> origin) {
        this.origin = origin;
    }

    /**
     * Resets leases between given indices.
     * @param indexFrom start index, inclusive
     * @param indexTo end index, inclusive
     */
    public void resetBetween(int indexFrom, int indexTo) {
        for (int i = indexFrom; i <= indexTo; i++) {
            origin.get(i).reset();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResettedLeases that = (ResettedLeases) o;
        return Objects.equals(origin, that.origin);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(origin);
    }
}
