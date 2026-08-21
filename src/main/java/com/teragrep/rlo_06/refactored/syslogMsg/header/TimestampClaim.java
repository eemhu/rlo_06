package com.teragrep.rlo_06.refactored.syslogMsg.header;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.rlo_06.refactored.Claim;
import com.teragrep.rlo_06.refactored.Result;
import com.teragrep.rlo_06.refactored.ResultStub;

import java.lang.foreign.MemorySegment;
import java.util.List;

/**
 * TIMESTAMP = NILVALUE / FULL-DATE "T" FULL-TIME
 *       FULL-DATE       = DATE-FULLYEAR "-" DATE-MONTH "-" DATE-MDAY
 *       DATE-FULLYEAR   = 4DIGIT
 *       DATE-MONTH      = 2DIGIT  ; 01-12
 *       DATE-MDAY       = 2DIGIT  ; 01-28, 01-29, 01-30, 01-31 based on
 *                                 ; month/year
 *       FULL-TIME       = PARTIAL-TIME TIME-OFFSET
 *       PARTIAL-TIME    = TIME-HOUR ":" TIME-MINUTE ":" TIME-SECOND
 *                         [TIME-SECFRAC]
 *       TIME-HOUR       = 2DIGIT  ; 00-23
 *       TIME-MINUTE     = 2DIGIT  ; 00-59
 *       TIME-SECOND     = 2DIGIT  ; 00-59
 *       TIME-SECFRAC    = "." 1*6DIGIT
 *       TIME-OFFSET     = "Z" / TIME-NUMOFFSET
 *       TIME-NUMOFFSET  = ("+" / "-") TIME-HOUR ":" TIME-MINUTE
 */
public final class TimestampClaim implements Claim<List<TrackedLease<MemorySegment>>> {
    @Override
    public Result<List<TrackedLease<MemorySegment>>> advance(final List<TrackedLease<MemorySegment>> src) {
        

        return new ResultStub<>();
    }
}
