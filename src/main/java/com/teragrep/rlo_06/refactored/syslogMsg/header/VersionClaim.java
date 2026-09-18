/*
 * Teragrep RFC5424 frame library for Java (rlo_06)
 * Copyright (C) 2022-2024 Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */
package com.teragrep.rlo_06.refactored.syslogMsg.header;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.lease.TrackedMemorySegmentLease;
import com.teragrep.rlo_06.refactored.*;
import com.teragrep.rlo_06.refactored.generic.DigitFragment;
import com.teragrep.rlo_06.refactored.queue.Fragment;
import com.teragrep.rlo_06.refactored.queue.FragmentState;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

/**
 * VERSION = NONZERO-DIGIT 0*2DIGIT
 */
public final class VersionClaim implements Fragment {

    private static final Result<TrackedLease<MemorySegment>> resultLeaseStub = new ResultStub<>();

    private final TrackedLease<MemorySegment>[] applicableLeases;
    private final FragmentState state;

    public VersionClaim() {
        this(new TrackedMemorySegmentLease[0]);
    }

    public VersionClaim(final TrackedLease<MemorySegment>[] applicableLeases) {
        this(applicableLeases, FragmentState.IN_PROGRESS);
    }

    public VersionClaim(final TrackedLease<MemorySegment>[] applicableLeases, final FragmentState state) {
        this.applicableLeases = applicableLeases;
        this.state = state;
    }

    @Override
    public FragmentState state() {
        return state;
    }

    @Override
    public Fragment apply(final TrackedLease<MemorySegment> trackedLease) {
        Fragment nonZeroFragment = new DigitFragment(true);
        Fragment digitFragment = new DigitFragment(false);
        Fragment digit2Fragment = new DigitFragment(false);

        TrackedLease<MemorySegment>[] newLeases = new TrackedMemorySegmentLease[applicableLeases.length + 1];
        System.arraycopy(applicableLeases, 0, newLeases, 0, applicableLeases.length);

        for (int i = 0; i < newLeases.length; i++) {
            TrackedLease<MemorySegment> newLease = newLeases[i].sliceAt(newLeases[i].currentPosition());
                if (nonZeroFragment.state() == FragmentState.IN_PROGRESS) {
                    nonZeroFragment = nonZeroFragment.apply(trackedLease);
                    if (nonZeroFragment.state() == FragmentState.SUCCESSFUL) {

                    }
                }
                else if (digitFragment.state() == FragmentState.IN_PROGRESS) {
                    digitFragment = digitFragment.apply(trackedLease);
                }
                else {
                    digit2Fragment = digit2Fragment.apply(trackedLease);
                }

        }

        return null;
    }

    @Override
    public TrackedLease<MemorySegment>[] leases() {
        return applicableLeases;
    }

    @Override
    public TrackedLease<MemorySegment>[] result() {
        return new TrackedLease[0];
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
