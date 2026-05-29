package com.teragrep.rlo_06.refactored.syslogMsg;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.rlo_06.refactored.Claim;
import com.teragrep.rlo_06.refactored.Result;

import java.lang.foreign.MemorySegment;
import java.util.List;

public class RFC5424Claim implements Claim<List<Result<String>>> {
    @Override
    public Result<List<Result<String>>> advance(final List<TrackedLease<MemorySegment>> src) {

       return null;
    }
}
