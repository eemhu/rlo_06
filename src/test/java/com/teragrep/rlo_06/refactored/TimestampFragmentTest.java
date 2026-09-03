package com.teragrep.rlo_06.refactored;

import com.teragrep.buf_01.buffer.lease.MemorySegmentLeaseStub;
import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.pool.OpeningPool;
import com.teragrep.buf_01.buffer.supply.ArenaMemorySegmentLeaseSupplier;
import com.teragrep.poj_01.pool.UnboundPool;
import com.teragrep.rlo_06.refactored.queue.Fragment;
import com.teragrep.rlo_06.refactored.queue.FragmentState;
import com.teragrep.rlo_06.refactored.syslogMsg.header.timestamp.FullTimeFragment;
import com.teragrep.rlo_06.refactored.syslogMsg.header.timestamp.TimestampFragment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

public final class TimestampFragmentTest {
    @Test
    void testIdealCaseZOffset() {
        Fragment fragment = new TimestampFragment();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("2020-01-01T01:23:34.123456Z", pool).toList();

            for (TrackedLease<MemorySegment> lease : leases) {
                fragment = fragment.apply(lease);

                if (fragment.state() != FragmentState.IN_PROGRESS) {
                    break;
                }
            }

            Assertions.assertEquals(FragmentState.SUCCESSFUL, fragment.state());

            final StringBuilder strBuilder = new StringBuilder();
            System.out.println("fragments: " + fragment.result().length);
            for (int i = 0; i < fragment.result().length; i++) {
                final TrackedLease<MemorySegment> lease = fragment.result()[i];
                while (lease.hasNext()) {
                    strBuilder.append((char)lease.next());
                }
            }

            Assertions.assertEquals("2020-01-01T01:23:34.123456Z", strBuilder.toString());

            // Success should advance the lease
            {
                int i;
                for (i = 0; i < leases.size(); i++) {
                    Assertions.assertEquals(1L, leases.get(i).currentPosition(), "failed at lease #" + i);
                }
                Assertions.assertEquals(16, i);
            }
        }
    }

    @Test
    void testIdealCasePositiveOffset() {
        Fragment fragment = new TimestampFragment();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("2020-01-01T01:23:34.123456+02:00", pool).toList();

            for (TrackedLease<MemorySegment> lease : leases) {
                fragment = fragment.apply(lease);

                if (fragment.state() != FragmentState.IN_PROGRESS) {
                    break;
                }
            }

            Assertions.assertEquals(FragmentState.SUCCESSFUL, fragment.state());

            final StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < fragment.result().length; i++) {
                final TrackedLease<MemorySegment> lease = fragment.result()[i];
                while (lease.hasNext()) {
                    strBuilder.append((char)lease.next());
                }
            }

            Assertions.assertEquals("2020-01-01T01:23:34.123456+02:00", strBuilder.toString());

            // Success should advance the lease
            {
                int i;
                for (i = 0; i < leases.size(); i++) {
                    Assertions.assertEquals(1L, leases.get(i).currentPosition(), "failed at lease #" + i);
                }
                Assertions.assertEquals(21, i);
            }
        }
    }

    @Test
    void testIdealCaseNegativeOffset() {
        Fragment fragment = new TimestampFragment();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("2020-01-01T01:23:34.123456-02:00", pool).toList();

            for (TrackedLease<MemorySegment> lease : leases) {
                fragment = fragment.apply(lease);

                if (fragment.state() != FragmentState.IN_PROGRESS) {
                    break;
                }
            }

            Assertions.assertEquals(FragmentState.SUCCESSFUL, fragment.state());

            final StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < fragment.result().length; i++) {
                final TrackedLease<MemorySegment> lease = fragment.result()[i];
                while (lease.hasNext()) {
                    strBuilder.append((char)lease.next());
                }
            }

            Assertions.assertEquals("2020-01-01T01:23:34.123456-02:00", strBuilder.toString());

            // Success should advance the lease
            {
                int i;
                for (i = 0; i < leases.size(); i++) {
                    Assertions.assertEquals(1L, leases.get(i).currentPosition(), "failed at lease #" + i);
                }
                Assertions.assertEquals(21, i);
            }
        }
    }

    @Test
    void testFailureCaseUnexpectedCharacter() {
        Fragment fragment = new FullTimeFragment();
        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("2x20-01-01T01:23:34.123456Z", pool).toList();

            for (TrackedLease<MemorySegment> lease : leases) {
                fragment = fragment.apply(lease);

                if (fragment.state() != FragmentState.IN_PROGRESS) {
                    break;
                }
            }

            Assertions.assertEquals(FragmentState.FAILED, fragment.state());
            // Failure should NOT advance the lease
            {
                int i;
                for (i = 0; i < leases.size(); i++) {
                    Assertions.assertEquals(0L, leases.get(i).currentPosition(), "failed at lease #" + i);
                }
                Assertions.assertEquals(27, i);
            }
        }
    }

    @Test
    void testFailureCaseWrongOrderOfSeparator() {
        Fragment fragment = new FullTimeFragment();
        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("012:334:.123456Z", pool).toList();

            for (TrackedLease<MemorySegment> lease : leases) {
                fragment = fragment.apply(lease);

                if (fragment.state() != FragmentState.IN_PROGRESS) {
                    break;
                }
            }

            Assertions.assertEquals(FragmentState.FAILED, fragment.state());
            // Failure should NOT advance the lease
            {
                int i;
                for (i = 0; i < leases.size(); i++) {
                    Assertions.assertEquals(0L, leases.get(i).currentPosition(), "failed at lease #" + i);
                }
                Assertions.assertEquals(16, i);
            }
        }
    }

    @Test
    void testExcessiveInput() {
        Fragment fragment = new TimestampFragment();
        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("2020-01-01T01:23:34.123456ZZZZZ", pool).toList();

            for (TrackedLease<MemorySegment> lease : leases) {
                fragment = fragment.apply(lease);

                if (fragment.state() != FragmentState.IN_PROGRESS) {
                    break;
                }
            }

            final StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < fragment.result().length; i++) {
                final TrackedLease<MemorySegment> lease = fragment.result()[i];
                while (lease.hasNext()) {
                    strBuilder.append((char)lease.next());
                }
            }

            Assertions.assertEquals("2020-01-01T01:23:34.123456Z", strBuilder.toString());

            Assertions.assertEquals(FragmentState.SUCCESSFUL, fragment.state());
            // Successful, extra leases should remain at position=0
            {
                final long[] expectedPositions = {1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,0L,0L,0L,0L};
                final long[] actualPositions = new long[leases.size()];
                int i;
                for (i = 0; i < leases.size(); i++) {
                    actualPositions[i] = leases.get(i).currentPosition();
                }
                Assertions.assertEquals(20, i);
                Assertions.assertArrayEquals(expectedPositions, actualPositions);
            }
        }
    }

    @Test
    void testNilValue() {
        Fragment fragment = new TimestampFragment();
        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("-", pool).toList();

            for (TrackedLease<MemorySegment> lease : leases) {
                fragment = fragment.apply(lease);

                if (fragment.state() != FragmentState.IN_PROGRESS) {
                    break;
                }
            }

            final StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < fragment.result().length; i++) {
                final TrackedLease<MemorySegment> lease = fragment.result()[i];
                while (lease.hasNext()) {
                    strBuilder.append((char)lease.next());
                }
            }

            Assertions.assertEquals("-", strBuilder.toString());

            Assertions.assertEquals(FragmentState.SUCCESSFUL, fragment.state());
            // Successful, extra leases should remain at position=0
            {
                //final long[] expectedPositions = {1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,1L,0L,0L,0L,0L};
                //final long[] actualPositions = new long[leases.size()];
                int i;
                for (i = 0; i < leases.size(); i++) {
                  //  actualPositions[i] = leases.get(i).currentPosition();
                }
                Assertions.assertEquals(1, i);
               // Assertions.assertArrayEquals(expectedPositions, actualPositions);
            }
        }
    }
}
