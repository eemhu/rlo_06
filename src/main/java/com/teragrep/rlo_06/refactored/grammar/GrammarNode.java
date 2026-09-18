package com.teragrep.rlo_06.refactored.grammar;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.rlo_06.refactored.queue.Fragment;
import com.teragrep.stb_01.Stubable;

import java.lang.foreign.MemorySegment;
import java.util.List;

public interface GrammarNode extends Stubable {
    public abstract GrammarNode and(final GrammarNode other);
    public abstract GrammarNode apply(final TrackedLease<MemorySegment> input);
    public abstract List<Fragment> fragments();
    public abstract List<GrammarNode> options();
}
