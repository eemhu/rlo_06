package com.teragrep.rlo_06.refactored.syslogMsg.header;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.rlo_06.refactored.*;

import java.lang.foreign.MemorySegment;
import java.util.List;

public final class PriClaim implements Claim<Integer> {
    @Override
    public Result<Integer> advance(final List<TrackedLease<MemorySegment>> src) {
        boolean insideTags = false;
        StringBuilder priVal = new StringBuilder();
        int fromIndex = -1;

        for (int i = 0; i < src.size(); i++) {
            final TrackedLease<MemorySegment> lease = src.get(i);
            while (lease.hasNext()) {
                if (fromIndex == -1) {
                    fromIndex = i;
                }

                lease.mark();
                final byte b = lease.next();

                if (!insideTags && b == '<') {
                    // Claim start
                    insideTags = true;
                } else if (!insideTags){
                    new ResettedLeases(src).resetBetween(fromIndex, i);
                    throw new ClaimFailedException(getClass(), "expected '<' but found " + Character.toString(b));
                } else if (b == '>' && !priVal.isEmpty()) {
                    // tag done, claim ok?
                    return new ResultImpl<>(ResultName.PRI, Integer.parseInt(priVal.toString()));
                } else if (Character.isDigit(b)) {
                    // inside tags and number
                    priVal.append((char)b);
                } else {
                    // fault
                    new ResettedLeases(src).resetBetween(fromIndex, i);
                    throw new ClaimFailedException(getClass(), "expected PRI");
            }
        }
    }
        throw new ClaimFailedException(getClass(), "expected PRI but found no next byte");
    }
}
