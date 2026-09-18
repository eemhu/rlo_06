package com.teragrep.rlo_06.refactored;

import com.teragrep.buf_01.buffer.lease.MemorySegmentLeaseStub;
import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.pool.OpeningPool;
import com.teragrep.buf_01.buffer.supply.ArenaMemorySegmentLeaseSupplier;
import com.teragrep.poj_01.pool.UnboundPool;
import com.teragrep.rlo_06.refactored.generic.CharFragment;
import com.teragrep.rlo_06.refactored.grammar.*;
import com.teragrep.rlo_06.refactored.queue.FragmentState;
import com.teragrep.rlo_06.refactored.syslogMsg.header.PriorityFragment;
import com.teragrep.rlo_06.refactored.syslogMsg.header.timestamp.TimestampFragment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

public final class GrammarNodeTest {

    @Test
    void testGrammarNode() {
        GrammarNode rfcNode = new GrammarNodeImpl(
                List.of(), List.of(new TimestampFragment())
        );

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final String input = "2020-01-01T01:23:34.123456Z";
            final List<TrackedLease<MemorySegment>> leases = new StringToLease(input, pool).toList();

            for (TrackedLease<MemorySegment> lease : leases) {
                rfcNode = rfcNode.apply(lease);
            }
            Assertions.assertEquals(1, rfcNode.fragments().size());
            Assertions.assertEquals(FragmentState.SUCCESSFUL, rfcNode.fragments().getFirst().state());
            Assertions.assertEquals(input.length(), rfcNode.fragments().getFirst().result().length);
        }
    }

    @Test
    void testMultipleFragments() {
        GrammarNode main = new GrammarNodeImpl(
                List.of(), List.of(new TimestampFragment(), new PriorityFragment())
        );
        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("2020-01-01T01:23:34.123456Z<123>", pool).toList();

            for (TrackedLease<MemorySegment> lease : leases) {
                main = main.apply(lease);
            }

            Assertions.assertEquals(2, main.fragments().size());
            Assertions.assertEquals(FragmentState.SUCCESSFUL, main.fragments().getFirst().state());
            Assertions.assertEquals(FragmentState.SUCCESSFUL, main.fragments().getLast().state());
        }
    }

    @Test
    void testMultipleOptions() {
        GrammarNode main = new GrammarNodeImpl(
                List.of(new GrammarNodeImpl(
                        List.of(), List.of(new CharFragment('-'))
                )), List.of(new TimestampFragment())
        );

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("2020-01-01T01:23:34.123456Z", pool).toList();

            for (TrackedLease<MemorySegment> lease : leases) {
                main = main.apply(lease);
            }

            Assertions.assertEquals(1, main.fragments().size());
            Assertions.assertEquals(FragmentState.SUCCESSFUL, main.fragments().getFirst().state());
            //Assertions.assertEquals(FragmentState.SUCCESSFUL, main.fragments().getLast().state());
            Assertions.assertEquals(1, main.options().size());
            Assertions.assertEquals(1, main.options().getFirst().fragments().size());
            System.out.println(main.options().getFirst().fragments().getFirst().toString());
            Assertions.assertEquals(FragmentState.FAILED, main.options().getFirst().fragments().getFirst().state());

        }
    }
}
