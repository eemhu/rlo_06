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
import com.teragrep.rlo_06.refactored.queue.Fragment;
import com.teragrep.rlo_06.refactored.queue.FragmentState;
import com.teragrep.rlo_06.refactored.queue.FragmentStub;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class PriorityFragment implements Fragment {
    private final TrackedLease<MemorySegment>[] applicableLeases;
    private final FragmentState state;

    public PriorityFragment() {
        this(new TrackedMemorySegmentLease[0]);
    }

    public PriorityFragment(final TrackedLease<MemorySegment>[] applicableLeases) {
        this(applicableLeases, FragmentState.IN_PROGRESS);
    }

    public PriorityFragment(final TrackedLease<MemorySegment>[] applicableLeases, final FragmentState state) {
        this.applicableLeases = applicableLeases;
        this.state = state;
    }

    @Override
    public FragmentState state() {
        return state;
    }

    @Override
    public Fragment apply(final TrackedLease<MemorySegment> leaseArg) {
        Fragment rv = new FragmentStub();

        final TrackedLease<MemorySegment>[] newLeases = new TrackedMemorySegmentLease[applicableLeases.length + 1];
        System.arraycopy(applicableLeases, 0, newLeases, 0, applicableLeases.length);
        newLeases[applicableLeases.length] = leaseArg;

        boolean insideTags = false;

        int beginIndex=-1;

        FragmentState currentState = FragmentState.IN_PROGRESS;

        for (int i = 0; i < newLeases.length && !currentState.equals(FragmentState.FAILED); i++) {
            final TrackedLease<MemorySegment> lease = newLeases[i];
            lease.mark();
            while (lease.hasNext() && !currentState.equals(FragmentState.FAILED)) {
                if (beginIndex == -1) {
                    beginIndex = i;
                }
                final byte b = lease.next();

                if (!insideTags && b == '<') {
                    // Claim start
                    insideTags = true;
                    currentState = FragmentState.IN_PROGRESS;
                    rv = new PriorityFragment(newLeases, currentState);
                } else if (!insideTags) {
                    // Failure!
                    new ResettedLeases(newLeases).resetBetween(beginIndex, i);
                    currentState = FragmentState.FAILED;
                    rv = new PriorityFragment(new TrackedMemorySegmentLease[0], currentState);
                } else if (b == '>') {
                    // tag done, claim ok?
                    newLeases[applicableLeases.length] = lease.sliceWithLength(lease.currentMark(), lease.currentPosition() - lease.currentMark());
                    newLeases[applicableLeases.length].mark(); //without this lease reset will fail
                    currentState = FragmentState.SUCCESSFUL;
                    rv = new PriorityFragment(newLeases, currentState);
                } else if (Character.isDigit(b)) {
                    // inside tags and number
                    // this is fine.
                    currentState = FragmentState.IN_PROGRESS;
                    rv = new PriorityFragment(newLeases, currentState);
                } else {
                    // Failure!
                    new ResettedLeases(newLeases).resetBetween(beginIndex, i);
                    currentState = FragmentState.FAILED;
                    rv = new PriorityFragment(new TrackedMemorySegmentLease[0], currentState);
                }
            }
        }

        if (!rv.isStub() && (rv.state() == FragmentState.IN_PROGRESS || rv.state() == FragmentState.SUCCESSFUL)) {
            new ResettedLeases(newLeases).resetBetween(beginIndex, newLeases.length - 1);
        }

        return rv;
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
    public String toString() {
        return "PriorityFragment{" +
                "applicableLeases=" + Arrays.toString(Arrays.stream(applicableLeases).map(lease -> {
            return new String(lease.leasedObject().toArray(ValueLayout.JAVA_BYTE), StandardCharsets.UTF_8);
        }).toArray()) +
                ", state=" + state +
                '}';
    }


    @Override
    public boolean isStub() {
        return false;
    }
}
