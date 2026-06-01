package com.teragrep.rlo_06.refactored.generic;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.rlo_06.refactored.*;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

public class RepeatableClaim<T> implements Claim<List<T>> {

    private final Claim<T> origin;
    private final int minimum;
    private final int maximum;

    public RepeatableClaim(Claim<T> origin, final int minimum) {
        this(origin, minimum, Integer.MAX_VALUE);
    }

    public RepeatableClaim(final Claim<T> origin, final int minimum, final int maximum) {
        this.origin = origin;
        this.minimum = minimum;
        this.maximum = minimum;
    }

    @Override
    public Result<List<T>> advance(final List<TrackedLease<MemorySegment>> src) {
        final List<T> results = new ArrayList<>();
        int successes = 0;
        while (successes < minimum || successes < maximum) {
            try {
                final Result<T> res = origin.advance(src);
                results.add(res.value());
                successes++;
            } catch (final ClaimFailedException e) {
                System.out.println("RepeatableClaim advance failure due to origin claim failure. " + e.getMessage());
                throw new ClaimFailedException(getClass(), "minimum repeated " + minimum + " required, instead was " + successes);
            }
        }

        return new ResultImpl<>(ResultName.REPEATABLE, results);
    }
}
