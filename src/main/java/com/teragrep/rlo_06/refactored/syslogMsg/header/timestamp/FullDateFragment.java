package com.teragrep.rlo_06.refactored.syslogMsg.header.timestamp;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.lease.TrackedMemorySegmentLease;
import com.teragrep.rlo_06.refactored.queue.Fragment;
import com.teragrep.rlo_06.refactored.queue.FragmentState;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

public final class FullDateFragment implements Fragment {
    private final FragmentState state;
    private final TrackedLease<MemorySegment>[] leases;
    private final TrackedLease<MemorySegment>[] finalResult;

    public FullDateFragment(final FragmentState state, final TrackedLease<MemorySegment>[] leases, final TrackedLease<MemorySegment>[] finalResult) {
        this.state = state;
        this.leases = leases;
        this.finalResult = finalResult;
    }

    @Override
    public FragmentState state() {
        return state;
    }


    private enum Position {
        IN_YEAR, IN_MONTH, IN_MDAY
    }

    //0000-00-00T
    @Override
    public Fragment apply(final TrackedLease<MemorySegment> trackedLease) {
        final TrackedLease<MemorySegment>[] newLeases = new TrackedMemorySegmentLease[leases.length + 1];
        System.arraycopy(leases, 0, newLeases, 0, leases.length);
        newLeases[newLeases.length - 1] = trackedLease;


        Position position = Position.IN_YEAR;
        int digits = 0;
        FragmentState newState = FragmentState.IN_PROGRESS;

        int beginIndex = -1;
        long[] positions = new long[newLeases.length];

        List<TrackedLease<MemorySegment>> result = new ArrayList<>();

        for (int i = 0; i < newLeases.length; i++) {
            final TrackedLease<MemorySegment> current = newLeases[i].sliceAt(newLeases[i].currentPosition());

            while (current.hasNext()) {
                if (beginIndex == -1) {
                    beginIndex = i;
                }
                final byte b = current.next();
                if (Character.isDigit(b)) {
                    digits++;
                }
                else if (b == '-' && position.equals(Position.IN_YEAR) && digits==4) {
                    // split
                    position = Position.IN_MONTH;
                    digits = 0;
                } else if (b == '-' && position.equals(Position.IN_MONTH) && digits==2) {
                    position = Position.IN_MDAY;
                    digits = 0;
                } else {
                    // failure state
                    newState = FragmentState.FAILED;
                }

                // any other than digit or '-' should be failure state as well

            }
            result.add(current);
            positions[i] = newLeases[i].currentPosition() + current.currentPosition();
        }

        if (position.equals(Position.IN_MDAY) && digits == 2) {
            // success
            newState = FragmentState.SUCCESSFUL;
        }


        final TrackedLease<MemorySegment>[] finalResult;
        if (newState == FragmentState.SUCCESSFUL) {
            // Move the origin leases to the correct positions on success
            int endIndex = newLeases.length - 1;
            for (int i = beginIndex; i <= endIndex; i++) {
                newLeases[i].position(positions[i]);
            }

            for (final TrackedLease<MemorySegment> lease : result) {
                lease.limit(lease.currentPosition());
                lease.position(0L);
            }

            finalResult = result.toArray(new TrackedMemorySegmentLease[0]);
        }
        else {
            finalResult = new TrackedMemorySegmentLease[0];
        }

        return new FullDateFragment(newState, newLeases, finalResult);
    }

    @Override
    public TrackedLease<MemorySegment>[] leases() {
        return leases;
    }

    @Override
    public TrackedLease<MemorySegment>[] result() {
        return finalResult;
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
