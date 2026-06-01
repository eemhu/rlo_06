package com.teragrep.rlo_06.refactored.generic;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.rlo_06.refactored.*;

import java.lang.foreign.MemorySegment;
import java.util.List;

public final class CharClaim implements Claim<Character> {

    private final char targetChar;
    public CharClaim(final char targetChar) {
        this.targetChar = targetChar;
    }

    public Result<Character> advance(final List<TrackedLease<MemorySegment>> src) {
        for (final TrackedLease<MemorySegment> lease : src) {
            if (lease.hasNext()) {
                lease.mark();
                final byte b = lease.next();

                if (b == targetChar) {
                    // Claim successful
                    return new ResultImpl<>(ResultName.CHAR, targetChar);
                } else {
                    lease.reset();
                    throw new ClaimFailedException(getClass(), "expected " + targetChar + " but found " + Character.toString(b));
                }
            }
        }
        throw new ClaimFailedException(getClass(), "expected " + targetChar + " but found no next byte");
    }
}
