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
package com.teragrep.rlo_06.refactored.generic;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.lease.TrackedMemorySegmentLease;
import com.teragrep.rlo_06.refactored.*;
import com.teragrep.rlo_06.refactored.queue.Fragment;
import com.teragrep.rlo_06.refactored.queue.FragmentState;

import java.lang.foreign.MemorySegment;

public final class CharFragment implements Fragment {

    private final char targetChar;
    private final TrackedLease<MemorySegment>[] applicableLeases;
    private final FragmentState state;

    public CharFragment(final char targetChar) {
        this(targetChar, new TrackedMemorySegmentLease[0]);
    }

    public CharFragment(final char targetChar, final TrackedLease<MemorySegment>[] applicableLeases) {
        this(targetChar, applicableLeases, FragmentState.IN_PROGRESS);
    }

    public CharFragment(final char targetChar, final TrackedLease<MemorySegment>[] applicableLeases, final FragmentState state) {
        this.targetChar = targetChar;
        this.applicableLeases = applicableLeases;
        this.state = state;
    }

    @Override
    public FragmentState state() {
        return state;
    }

    @Override
    public Fragment apply(final TrackedLease<MemorySegment> trackedLease) {
        FragmentState newState;
        final TrackedLease<MemorySegment>[] newApplicableLeases = new TrackedMemorySegmentLease[1];

        if (trackedLease.hasNext()) {
            trackedLease.mark();
            final byte b = trackedLease.next();
            System.out.println("char claim: " + (char)b);

            if (b == targetChar) {
                // Claim successful
                newApplicableLeases[0] = trackedLease.sliceWithLength(trackedLease.currentPosition() - 1, 1);
                newState = FragmentState.SUCCESSFUL;
            }
            else {
                trackedLease.reset();
                newState = FragmentState.FAILED;
            }
        } else {
            newState = FragmentState.FAILED;
        }

        return new CharFragment(targetChar, newApplicableLeases, newState);
    }

    @Override
    public TrackedLease<MemorySegment>[] leases() {
        return applicableLeases;
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
