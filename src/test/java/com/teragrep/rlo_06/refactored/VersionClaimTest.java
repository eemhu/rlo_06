package com.teragrep.rlo_06.refactored;

import com.teragrep.buf_01.buffer.lease.MemorySegmentLeaseStub;
import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.pool.OpeningPool;
import com.teragrep.buf_01.buffer.supply.ArenaMemorySegmentLeaseSupplier;
import com.teragrep.poj_01.pool.UnboundPool;
import com.teragrep.rlo_06.refactored.syslogMsg.header.VersionClaim;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

public final class VersionClaimTest {

    @Test
    void testSuccessThreeDigits() {
        final String input = "123";
        final Claim<Integer> claim = new VersionClaim();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease(input, pool).toList();

            final Result<Integer> result = claim.advance(leases);
            Assertions.assertEquals(123, result.value());
            Assertions.assertEquals("VERSION", result.name());

            // Success should advance the lease
            Assertions.assertEquals(3, leases.size());
            Assertions.assertEquals(1L, leases.get(0).currentPosition());
            Assertions.assertEquals(1L, leases.get(1).currentPosition());
            Assertions.assertEquals(1L, leases.get(2).currentPosition());

        }
    }

    @Test
    void testSuccessTwoDigits() {
        final String input = "12";
        final Claim<Integer> claim = new VersionClaim();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease(input, pool).toList();

            final Result<Integer> result = claim.advance(leases);
            Assertions.assertEquals(12, result.value());
            Assertions.assertEquals("VERSION", result.name());

            // Success should advance the lease
            Assertions.assertEquals(2, leases.size());
            Assertions.assertEquals(1L, leases.get(0).currentPosition());
            Assertions.assertEquals(1L, leases.get(1).currentPosition());
        }
    }

    @Test
    void testSuccessOneDigit() {
        final String input = "1";
        final Claim<Integer> claim = new VersionClaim();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease(input, pool).toList();

            final Result<Integer> result = claim.advance(leases);
            Assertions.assertEquals(1, result.value());
            Assertions.assertEquals("VERSION", result.name());

            // Success should advance the lease
            Assertions.assertEquals(1, leases.size());
            Assertions.assertEquals(1L, leases.get(0).currentPosition());
        }
    }

    @Test
    void testFailureFirstDigitIsZero() {
        final String input = "0";
        final Claim<Integer> claim = new VersionClaim();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease(input, pool).toList();

            Assertions.assertThrows(ClaimFailedException.class, () -> claim.advance(leases));

            // Failure should not advance the lease
            Assertions.assertEquals(1, leases.size());
            Assertions.assertEquals(0L, leases.get(0).currentPosition());
        }
    }

    @Test
    void testFailureFirstCharIsNotDigit() {
        final String input = "x";
        final Claim<Integer> claim = new VersionClaim();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease(input, pool).toList();

            Assertions.assertThrows(ClaimFailedException.class, () -> claim.advance(leases));

            // Failure should not advance the lease
            Assertions.assertEquals(1, leases.size());
            Assertions.assertEquals(0L, leases.get(0).currentPosition());
        }
    }
}
