package com.teragrep.rlo_06.refactored.grammar;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.rlo_06.refactored.queue.Fragment;

import java.lang.foreign.MemorySegment;
import java.util.List;

public final class GrammarNodeStub implements GrammarNode {
    @Override
    public GrammarNode and(final GrammarNode other) {
        throw new UnsupportedOperationException("Stub object");
    }

    @Override
    public GrammarNode apply(final TrackedLease<MemorySegment> input) {
        throw new UnsupportedOperationException("Stub object");
    }

    @Override
    public List<Fragment> fragments() {
        throw new UnsupportedOperationException("Stub object");
    }

    @Override
    public List<GrammarNode> options() {
        throw new UnsupportedOperationException("Stub object");
    }

    @Override
    public boolean isStub() {
        return true;
    }
}
