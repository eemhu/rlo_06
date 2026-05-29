package com.teragrep.rlo_06.refactored;

import com.teragrep.buf_01.buffer.lease.MemorySegmentLeaseStub;
import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.pool.OpeningPool;
import com.teragrep.buf_01.buffer.supply.ArenaMemorySegmentLeaseSupplier;
import com.teragrep.poj_01.pool.UnboundPool;
import com.teragrep.rlo_06.refactored.generic.CharClaim;
import com.teragrep.rlo_06.refactored.generic.RepeatableClaim;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

public final class RepeatableClaimTest {

    @Test
    void testSuccess() {
        final Claim<Character> claim = new CharClaim('a');

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 2), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("aaa", pool).toList();

            final Result<List<Character>> repeatableResult = new RepeatableClaim<>(claim, 3).advance(leases);

            // Success should advance the lease
            Assertions.assertEquals(2, leases.size());
            Assertions.assertEquals(2L, leases.get(0).currentPosition());
            Assertions.assertEquals(1L, leases.get(1).currentPosition());

            Assertions.assertEquals(3L, repeatableResult.value().size());
            Assertions.assertEquals('a', repeatableResult.value().get(0).charValue());
            Assertions.assertEquals('a', repeatableResult.value().get(1).charValue());
            Assertions.assertEquals('a', repeatableResult.value().get(2).charValue());
        }
    }

    @Test
    void testFailTooFew() {
        final Claim<Character> claim = new CharClaim('a');

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 2), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("aaa", pool).toList();

            Assertions.assertThrows(ClaimFailedException.class, () -> new RepeatableClaim<>(claim, 4).advance(leases));

            // FIXME: Failure should not advance the lease
        }
    }
}
