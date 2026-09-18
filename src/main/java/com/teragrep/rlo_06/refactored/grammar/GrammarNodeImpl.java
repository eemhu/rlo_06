package com.teragrep.rlo_06.refactored.grammar;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.rlo_06.refactored.queue.Fragment;
import com.teragrep.rlo_06.refactored.queue.FragmentState;
import com.teragrep.rlo_06.refactored.queue.FragmentStub;
import com.teragrep.rlo_06.refactored.syslogMsg.header.timestamp.TimestampFragment;

import java.lang.foreign.MemorySegment;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public final class GrammarNodeImpl implements GrammarNode {

    //OR options
    private final List<GrammarNode> grammarNodes;
    private final List<Fragment> fragments;
    private final GrammarNode next;

    public GrammarNodeImpl() {
        this(List.of(new GrammarNodeStub()));
    }

    public GrammarNodeImpl(final List<GrammarNode> grammarNodes) {
        this(grammarNodes, List.of(new FragmentStub()));
    }

    public GrammarNodeImpl(final List<GrammarNode> grammarNodes, final List<Fragment> fragments) {
        this(grammarNodes, fragments, new GrammarNodeStub());
    }

    public GrammarNodeImpl(final List<GrammarNode> grammarNodes, final List<Fragment> fragments, final GrammarNode next) {
        this.grammarNodes = grammarNodes;
        this.fragments = fragments;
        this.next = next;
    }

    @Override
    public GrammarNode and(final GrammarNode other) {
        return new GrammarNodeImpl(grammarNodes, fragments, other);
    }

    @Override
    public GrammarNode apply(final TrackedLease<MemorySegment> input) {
        // for each of the GrammarNodes
        // Could be
        // RFC5424->PRI->VER->TimestampNil->...
        // RFC5424->PRI->VER->TimestampUTC->...
        // But in this case RFC5424->PRI->VER parts should be shared, and the processing would only fork
        // after VER fragment.

        // Parallel grammars


        // final List<GrammarNode> newNodes = new ArrayList<>(grammarNodes.size());
        final List<CompletableFuture<GrammarNode>> futures = new ArrayList<>(grammarNodes.size());
        for (final GrammarNode grammarNode : grammarNodes) {
            CompletableFuture<GrammarNode> future = CompletableFuture.supplyAsync(() -> grammarNode.apply(input));
            futures.add(future);
            // GrammarNode node = grammarNode.apply(input);
            // newNodes.add(node);
        }

        List<GrammarNode> nodes = futures.stream().map(CompletableFuture::join).toList();

        final List<Fragment> newFragments = new ArrayList<>(fragments);

        for (int i = 0; i < newFragments.size(); i++) {
            Fragment fragment = newFragments.get(i);

            System.out.println(fragment.toString() + " " + fragment.state());
            if (fragment.state().equals(FragmentState.IN_PROGRESS)) {
                fragment = fragment.apply(input);
                System.out.println("after: " + fragment.state());
                newFragments.set(i, fragment);
                break;
            } else if (fragment.state().equals(FragmentState.FAILED)) {
                newFragments.set(i, fragment);
                break;
            }

        }


        //empty=>move to next node
        if (newFragments.isEmpty() && !next.isStub()) {
            return next.apply(input);
        }

        return new GrammarNodeImpl(nodes, newFragments, next);
    }

    @Override
    public boolean isStub() {
        return false;
    }

    public List<Fragment> fragments() {
        return Collections.unmodifiableList(fragments);
    }

    @Override
    public List<GrammarNode> options() {
        return Collections.unmodifiableList(grammarNodes);
    }
}
