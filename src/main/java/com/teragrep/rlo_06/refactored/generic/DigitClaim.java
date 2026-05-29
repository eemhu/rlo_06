package com.teragrep.rlo_06.refactored.generic;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.rlo_06.refactored.Claim;
import com.teragrep.rlo_06.refactored.ClaimFailedException;
import com.teragrep.rlo_06.refactored.Result;
import com.teragrep.rlo_06.refactored.ResultImpl;

import java.lang.foreign.MemorySegment;
import java.util.List;

public final class DigitClaim implements Claim<Integer> {
    private final boolean nonZeroDigitsOnly;

    public DigitClaim() {
        this(false);
    }

    public DigitClaim(final boolean nonZeroDigitsOnly) {
        this.nonZeroDigitsOnly = nonZeroDigitsOnly;
    }

    @Override
    public Result<Integer> advance(final List<TrackedLease<MemorySegment>> src) {
        for (final TrackedLease<MemorySegment> lease : src) {
            if (lease.hasNext()) {
                lease.mark();
                final byte b = lease.next();

                if (Character.isDigit(b) && b != '0' && nonZeroDigitsOnly) {
                    // Claim successful
                    return new ResultImpl<>("digit", Character.digit(b, 10));
                } else if (Character.isDigit(b) && !nonZeroDigitsOnly) {
                    // Claim successful
                    return new ResultImpl<>("digit", Character.digit(b, 10));
                } else {
                    lease.reset();
                    throw new ClaimFailedException(getClass(), "expected digit but found " + Character.toString(b));
                }
            }
        }
        throw new ClaimFailedException(getClass(), "expected digit but found no next byte");
    }
}
