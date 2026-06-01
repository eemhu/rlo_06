package com.teragrep.rlo_06.refactored.syslogMsg.header;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.rlo_06.refactored.*;
import com.teragrep.rlo_06.refactored.generic.DigitClaim;

import java.lang.foreign.MemorySegment;
import java.util.List;

/**
 * VERSION = NONZERO-DIGIT 0*2DIGIT
 */
public final class VersionClaim implements Claim<Integer> {
    private static final Result<Integer> resultIntStub = new ResultStub<>();

    @Override
    public Result<Integer> advance(final List<TrackedLease<MemorySegment>> src) {
        final Claim<Integer> nonZeroClaim = new DigitClaim(true);
        final Claim<Integer> digitClaim = new DigitClaim(false);
        final StringBuilder strBuilder = new StringBuilder();

        final Result<Integer> firstDigitResult;
        try {
             firstDigitResult = nonZeroClaim.advance(src);
        } catch (final ClaimFailedException e) {
            throw new ClaimFailedException(getClass(), "first non-zero digit is required for VERSION", e);
        }

        Result<Integer> secondDigitResult;
        try {
            secondDigitResult = digitClaim.advance(src);
        } catch (final ClaimFailedException e) {
            // not required
            secondDigitResult = resultIntStub;
        }

        Result<Integer> thirdDigitResult;
        try {
            thirdDigitResult = digitClaim.advance(src);
        } catch (final ClaimFailedException e) {
            // not required
            thirdDigitResult = resultIntStub;
        }

        strBuilder.append(firstDigitResult.value());

        if (!secondDigitResult.isStub()) {
            strBuilder.append(secondDigitResult.value());
        }

        if (!thirdDigitResult.isStub()) {
            strBuilder.append(thirdDigitResult.value());
        }

        return new ResultImpl<>(ResultName.VERSION, Integer.parseInt(strBuilder.toString()));
    }
}
