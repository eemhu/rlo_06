package com.teragrep.rlo_06.refactored;

import com.teragrep.buf_01.buffer.lease.MemorySegmentLeaseStub;
import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.pool.OpeningPool;
import com.teragrep.buf_01.buffer.supply.ArenaMemorySegmentLeaseSupplier;
import com.teragrep.poj_01.pool.UnboundPool;
import com.teragrep.rlo_06.refactored.syslogMsg.header.PriClaim;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

public final class PriClaimTest {
    @Test
    void testSuccess() {
        final Claim<Integer> claim = new PriClaim();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 2), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("<120>", pool).toList();

            final Result<Integer> result = claim.advance(leases);
            Assertions.assertEquals(120, result.value());
            Assertions.assertEquals("PRIVAL", result.name());

            // Success should advance the leases
            // Each lease has two bytes, so we should have 3 leases.
            Assertions.assertEquals(3, leases.size());
            Assertions.assertEquals(2L, leases.get(0).currentPosition());
            Assertions.assertEquals(2L, leases.get(1).currentPosition());
            Assertions.assertEquals(1L, leases.get(2).currentPosition());

        }
    }

    @Test
    void testFailure() {
        final Claim<Integer> claim = new PriClaim();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("<abc", pool).toList();

            Assertions.assertThrows(ClaimFailedException.class, () -> claim.advance(leases));
            // Failure should not advance the leases
            {
                int i;
                for (i = 0; i < 4; i++) {
                    Assertions.assertEquals(0L, leases.get(i).currentPosition());
                }
                Assertions.assertEquals(4, i);
                Assertions.assertEquals(4, leases.size());
            }
        }
    }
}
