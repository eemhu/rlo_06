package com.teragrep.rlo_06.refactored;

import com.teragrep.buf_01.buffer.lease.MemorySegmentLeaseStub;
import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.pool.OpeningPool;
import com.teragrep.buf_01.buffer.supply.ArenaMemorySegmentLeaseSupplier;
import com.teragrep.poj_01.pool.UnboundPool;
import com.teragrep.rlo_06.refactored.generic.CharClaim;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

public final class CharClaimTest {
    @Test
    void testSuccess() {
        final Claim<Character> claim = new CharClaim(' ');

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease(" abc", pool).toList();

            final Result<Character> result = claim.advance(leases);
            Assertions.assertEquals(' ', result.value());
            Assertions.assertEquals(ResultName.CHAR, result.name());

            // Success should advance the lease
            Assertions.assertEquals(1L, leases.getFirst().currentPosition());
        }
    }

    @Test
    void testFailure() {
        final Claim<Character> claim = new CharClaim(' ');

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("abc", pool).toList();

            Assertions.assertThrows(ClaimFailedException.class, () -> claim.advance(leases));
            // Failure should not advance the lease
            Assertions.assertEquals(0L, leases.getFirst().currentPosition());
        }
    }
}
