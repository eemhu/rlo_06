package com.teragrep.rlo_06.refactored;

import com.teragrep.buf_01.buffer.lease.MemorySegmentLeaseStub;
import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.pool.OpeningPool;
import com.teragrep.buf_01.buffer.supply.ArenaMemorySegmentLeaseSupplier;
import com.teragrep.poj_01.pool.UnboundPool;
import com.teragrep.rlo_06.refactored.generic.DigitClaim;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

public final class DigitClaimTest {

    @Test
    void testSuccessWithZero() {
        final Claim<Integer> claim = new DigitClaim();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("04", pool).toList();

            final Result<Integer> result = claim.advance(leases);
            Assertions.assertEquals(0, result.value());
            Assertions.assertEquals("digit", result.name());

            // Success should advance the lease
            Assertions.assertEquals(1L, leases.getFirst().currentPosition());
        }
    }

    @Test
    void testSuccessWithOther() {
        final Claim<Integer> claim = new DigitClaim();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("54", pool).toList();

            final Result<Integer> result = claim.advance(leases);
            Assertions.assertEquals(5, result.value());
            Assertions.assertEquals("digit", result.name());

            // Success should advance the lease
            Assertions.assertEquals(1L, leases.getFirst().currentPosition());
        }
    }

    @Test
    void testSuccessNonZeroOnlyWithOther() {
        final Claim<Integer> claim = new DigitClaim(true);

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("54", pool).toList();

            final Result<Integer> result = claim.advance(leases);
            Assertions.assertEquals(5, result.value());
            Assertions.assertEquals("digit", result.name());

            // Success should advance the lease
            Assertions.assertEquals(1L, leases.getFirst().currentPosition());
        }
    }

    @Test
    void testFailureNonZeroOnly() {
        final Claim<Integer> claim = new DigitClaim(true);

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("04", pool).toList();

            Assertions.assertThrows(ClaimFailedException.class, () -> claim.advance(leases));

            // Failure should not advance the lease
            Assertions.assertEquals(0L, leases.getFirst().currentPosition());
        }
    }

    @Test
    void testFailure() {
        final Claim<Integer> claim = new DigitClaim();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("x4", pool).toList();

            Assertions.assertThrows(ClaimFailedException.class, () -> claim.advance(leases));

            // Failure should not advance the lease
            Assertions.assertEquals(0L, leases.getFirst().currentPosition());
        }
    }
}
