package com.teragrep.rlo_06.refactored.syslogMsg.header.timestamp;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.lease.TrackedMemorySegmentLease;
import com.teragrep.rlo_06.refactored.generic.CharFragment;
import com.teragrep.rlo_06.refactored.queue.Fragment;
import com.teragrep.rlo_06.refactored.queue.FragmentState;

import java.lang.foreign.MemorySegment;

/**
 * TIMESTAMP = NILVALUE / FULL-DATE "T" FULL-TIME
 *       FULL-DATE       = DATE-FULLYEAR "-" DATE-MONTH "-" DATE-MDAY
 *       DATE-FULLYEAR   = 4DIGIT
 *       DATE-MONTH      = 2DIGIT  ; 01-12
 *       DATE-MDAY       = 2DIGIT  ; 01-28, 01-29, 01-30, 01-31 based on
 *                                 ; month/year
 *       FULL-TIME       = PARTIAL-TIME TIME-OFFSET
 *       PARTIAL-TIME    = TIME-HOUR ":" TIME-MINUTE ":" TIME-SECOND
 *                         [TIME-SECFRAC]
 *       TIME-HOUR       = 2DIGIT  ; 00-23
 *       TIME-MINUTE     = 2DIGIT  ; 00-59
 *       TIME-SECOND     = 2DIGIT  ; 00-59
 *       TIME-SECFRAC    = "." 1*6DIGIT
 *       TIME-OFFSET     = "Z" / TIME-NUMOFFSET
 *       TIME-NUMOFFSET  = ("+" / "-") TIME-HOUR ":" TIME-MINUTE
 */
public final class TimestampFragment implements Fragment {
    private final TrackedLease<MemorySegment>[] applicableLeases;
    private final TrackedLease<MemorySegment>[] result;
    private final FragmentState state;


    public TimestampFragment() {
        this(new TrackedMemorySegmentLease[0], FragmentState.IN_PROGRESS);
    }

    public TimestampFragment(final TrackedLease<MemorySegment>[] applicableLeases, final FragmentState state) {
        this(applicableLeases, new TrackedMemorySegmentLease[0], state);
    }

    public TimestampFragment(final TrackedLease<MemorySegment>[] applicableLeases, final TrackedLease<MemorySegment>[] result, final FragmentState state) {
        this.applicableLeases = applicableLeases;
        this.result = result;
        this.state = state;
    }

    @Override
    public FragmentState state() {
        return state;
    }

    @Override
    public Fragment apply(final TrackedLease<MemorySegment> trackedLease) {
        final TrackedLease<MemorySegment>[] newLeases = new TrackedMemorySegmentLease[applicableLeases.length + 1];
        System.arraycopy(applicableLeases, 0, newLeases, 0, applicableLeases.length);
        newLeases[newLeases.length - 1] = trackedLease;

        FragmentState newState = FragmentState.IN_PROGRESS;

        Fragment dateFragment = new FullDateFragment(); // yyyy-mm-dd
        Fragment tFragment = new CharFragment('T'); // T
        Fragment timeFragment = new FullTimeFragment(); // hh:mm:ss.SSSSSSZ | hh:mm:ss.SSSSSS+hh:mm
        Fragment charFragment = new CharFragment('-'); // NILVALUE

        final long[] positions = new long[newLeases.length];
        TrackedLease<MemorySegment>[] result = new TrackedMemorySegmentLease[0];

        //TODO: This probably won't work properly with memorySegment length > 1
        // due to an oversight. Need to check if current lease has more remaining
        // and feed that to the next fragment as a starting point.
        for (int i = 0; i < newLeases.length; i++) {
            final TrackedLease<MemorySegment> current = newLeases[i].sliceAt(newLeases[i].currentPosition());
            // NILVALUE, has to be the first char

            if (!charFragment.state().equals(FragmentState.FAILED)) {
                charFragment = charFragment.apply(current);
                if (charFragment.state().equals(FragmentState.SUCCESSFUL)) {
                    newState = FragmentState.SUCCESSFUL;
                    result = charFragment.result();
                    break;
                }
            }

            if (dateFragment.state().equals(FragmentState.IN_PROGRESS)) {
                dateFragment = dateFragment.apply(current);
            }
            else if (tFragment.state().equals(FragmentState.IN_PROGRESS)) {
                tFragment = tFragment.apply(current);
            }
            else if (timeFragment.state().equals(FragmentState.IN_PROGRESS)) {
                timeFragment = timeFragment.apply(current);
            }


            // Failed / Success states.
            if (dateFragment.state().equals(FragmentState.FAILED) ||
                    tFragment.state().equals(FragmentState.FAILED) ||
                    timeFragment.state().equals(FragmentState.FAILED)) {
                newState = FragmentState.FAILED;
            }

            positions[i] = newLeases[i].currentPosition() + current.currentPosition();

            if (dateFragment.state().equals(FragmentState.SUCCESSFUL) &&
            timeFragment.state().equals(FragmentState.SUCCESSFUL) &&
            tFragment.state().equals(FragmentState.SUCCESSFUL)) {
                newState = FragmentState.SUCCESSFUL;
                int size = dateFragment.result().length + tFragment.result().length + timeFragment.result().length;

                result = new TrackedMemorySegmentLease[size];
                System.arraycopy(dateFragment.result(), 0, result, 0, dateFragment.result().length);
                System.arraycopy(tFragment.result(), 0, result, dateFragment.result().length, tFragment.result().length);
                System.arraycopy(timeFragment.result(), 0, result, dateFragment.result().length + tFragment.result().length, timeFragment.result().length);
            }
        }


        //FIXME: On success origin lease position should change to where this fragment's part ends.
        if (newState == FragmentState.SUCCESSFUL) {
            // Move the origin leases to the correct positions on success
            //TODO: Is this an issue if the next fragment fails and restarts from beginning??
            int endIndex = newLeases.length - 1;
            for (int i = 0; i <= endIndex; i++) {
                newLeases[i].position(positions[i]);
            }

            /*for (final TrackedLease<MemorySegment> lease : result) {
                lease.limit(lease.currentPosition());
                lease.position(0L);
            }*/
        }

        System.out.println("result: " + result.length);
        return new TimestampFragment(newLeases, result, newState);
    }

    @Override
    public TrackedLease<MemorySegment>[] leases() {
        return applicableLeases;
    }

    @Override
    public TrackedLease<MemorySegment>[] result() {
        return result;
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
