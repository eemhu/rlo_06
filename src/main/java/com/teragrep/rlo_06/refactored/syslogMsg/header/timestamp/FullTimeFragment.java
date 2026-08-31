package com.teragrep.rlo_06.refactored.syslogMsg.header.timestamp;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.lease.TrackedMemorySegmentLease;
import com.teragrep.rlo_06.refactored.queue.Fragment;
import com.teragrep.rlo_06.refactored.queue.FragmentState;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

public final class FullTimeFragment implements Fragment {
    private final FragmentState state;
    private final TrackedLease<MemorySegment>[] leases;
    private final TrackedLease<MemorySegment>[] finalResult;

    public FullTimeFragment() {
        this(FragmentState.IN_PROGRESS, new TrackedMemorySegmentLease[0], new TrackedMemorySegmentLease[0]);
    }

    private FullTimeFragment(final FragmentState state, final TrackedLease<MemorySegment>[] leases, final TrackedLease<MemorySegment>[] finalResult) {
        this.state = state;
        this.leases = leases;
        this.finalResult = finalResult;

    }

    @Override
    public FragmentState state() {
        return state;
    }


    private enum Position {
        IN_HOURS, IN_MINUTES, IN_SECONDS, IN_SECFRAC, AFTER_SECFRAC, OFFSET_HOURS, OFFSET_MINUTES, DONE
    }

    //00:00:00.000000[Z|[+-]00:00]
    @Override
    public Fragment apply(final TrackedLease<MemorySegment> trackedLease) {
        final TrackedLease<MemorySegment>[] newLeases = new TrackedMemorySegmentLease[leases.length + 1];
        System.arraycopy(leases, 0, newLeases, 0, leases.length);
        newLeases[newLeases.length - 1] = trackedLease;


        Position position = Position.IN_HOURS;
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
                    System.out.println("Digit " + (char)b);
                    digits++;
                }

                if (position.equals(Position.OFFSET_MINUTES) && digits==2) {
                    System.out.println("shift to done after offset minutes");
                    position = Position.DONE;
                    digits = 0;
                }

                if (b == ':' && position.equals(Position.IN_HOURS) && digits==2) {
                    // split
                    System.out.println(": shift to minutes");
                    position = Position.IN_MINUTES;
                    digits = 0;
                } else if (b == ':' && position.equals(Position.IN_MINUTES) && digits==2) {
                    System.out.println(": shift to seconds");
                    position = Position.IN_SECONDS;
                    digits = 0;
                } else if (b == '.' && position.equals(Position.IN_SECONDS) && digits==2) {
                    System.out.println(". shift to sec frac");
                    position = Position.IN_SECFRAC;
                    digits = 0;
                } else if (b=='Z' && position.equals(Position.IN_SECFRAC) && digits == 6) {
                    System.out.println("Z shift to done");
                    position = Position.DONE;
                    digits = 0;
                } else if ((b=='+'||b=='-') && position.equals(Position.IN_SECFRAC) && digits == 6) {
                    System.out.println("+- shift to offset hours");
                    position = Position.OFFSET_HOURS;
                    digits = 0;
                } else if (b == ':' && position.equals(Position.OFFSET_HOURS) && digits==2) {
                    System.out.println(": shift to offset minutes");
                    position = Position.OFFSET_MINUTES;
                    digits = 0;
                } else if (!Character.isDigit(b)) {
                    System.out.println("failure state from char: " + (char)b);
                    // failure state
                    newState = FragmentState.FAILED;
                }
            }
            result.add(current);
            positions[i] = newLeases[i].currentPosition() + current.currentPosition();
        }

        if (position.equals(Position.DONE)) {
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

        return new FullTimeFragment(newState, newLeases, finalResult);
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
