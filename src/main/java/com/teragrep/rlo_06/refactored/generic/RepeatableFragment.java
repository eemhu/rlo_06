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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class RepeatableFragment implements Fragment {

    private final Fragment origin;
    private final int minimum;
    private final int maximum;

    private final FragmentState state;
    private final TrackedLease<MemorySegment>[] applicableLeases;
    private final long[] originalPositions;

    public RepeatableFragment(final Fragment origin, final int minimum) {
        this(origin, minimum, -1);
    }

    public RepeatableFragment(final Fragment origin, final int minimum, final int maximum) {
        this(origin,minimum,maximum,FragmentState.IN_PROGRESS, new TrackedMemorySegmentLease[0]);
    }

    public RepeatableFragment(final Fragment origin, final int minimum, final int maximum, final FragmentState state, final TrackedLease<MemorySegment>[] applicableLeases) {
        this(origin, minimum,maximum,state,applicableLeases, new long[0]);
    }

    public RepeatableFragment(final Fragment origin, final int minimum, final int maximum, final FragmentState state, final TrackedLease<MemorySegment>[] applicableLeases, final long[] originalPositions) {
        this.origin = origin;
        this.minimum = minimum;
        this.maximum = maximum;
        this.state = state;
        this.applicableLeases = applicableLeases;
        this.originalPositions = originalPositions;
    }

    @Override
    public FragmentState state() {
        return state;
    }

    @Override
    public Fragment apply(final TrackedLease<MemorySegment> trackedLease) {
        int successes = 0;
        int beginIndex = -1;
        FragmentState newState = FragmentState.IN_PROGRESS;
        final List<TrackedLease<MemorySegment>> slices = new ArrayList<>();

        TrackedLease<MemorySegment>[] newLeases = new TrackedMemorySegmentLease[applicableLeases.length + 1];
        final long[] newPositions = new long[applicableLeases.length + 1];
        System.arraycopy(applicableLeases, 0, newLeases, 0, applicableLeases.length);
        System.arraycopy(originalPositions, 0, newPositions, 0, originalPositions.length);
        newLeases[applicableLeases.length] = trackedLease;
        newPositions[originalPositions.length] = trackedLease.currentPosition();

        for (int i = 0; i < newLeases.length; i++) {
            final TrackedLease<MemorySegment> current = newLeases[i];
            while (current.hasNext()) {
                if (beginIndex == -1) {
                    beginIndex = i;
                }
                final Fragment res = origin.apply(current);

                if (res.state() == FragmentState.SUCCESSFUL) {
                    System.out.println("Success res state");
                    slices.addAll(Arrays.asList(res.leases()));
                    successes++;
                }

                if (res.state() == FragmentState.IN_PROGRESS) {
                    newState = FragmentState.IN_PROGRESS;
                }

                if (res.state() == FragmentState.FAILED) {
                    if (successes >= minimum && maximum == -1) {
                        newState = FragmentState.SUCCESSFUL;
                        break;
                    }
                    else if (successes >= minimum && successes <= maximum) {
                        newState = FragmentState.SUCCESSFUL;
                        break;
                    } else {
                       newState = FragmentState.FAILED;
                        break;
                    }
                }
            }


        }


        if (newState == FragmentState.IN_PROGRESS || newState == FragmentState.FAILED) {
            for (int i = 0; i < newPositions.length; i++) {
                final long newPosition = newPositions[i];
                newLeases[i].position(newPosition);
            }
        }
        else {
            // TODO: this should just use the array
            newLeases = slices.toArray(new TrackedMemorySegmentLease[0]);
        }
        final var rv = new RepeatableFragment(origin, minimum, maximum, newState, newLeases, newPositions);

        System.out.println(rv.state());
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
    public boolean isStub() {
        return false;
    }
}
