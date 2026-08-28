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
package com.teragrep.rlo_06.refactored.syslogMsg;

import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.rlo_06.refactored.queue.Fragment;
import com.teragrep.rlo_06.refactored.queue.FragmentState;
import com.teragrep.rlo_06.refactored.syslogMsg.header.PriorityFragment;
import com.teragrep.rlo_06.refactored.syslogMsg.header.VersionClaim;

import java.lang.foreign.MemorySegment;

public final class RFC5424Message implements Fragment {
    private final Fragment[] schema;
    private final FragmentState state;
    public RFC5424Message() {
        this(
                new Fragment[] {
                        new PriorityFragment(),
                        //new VersionClaim()
                }
        );
    }

    public RFC5424Message(final Fragment[] schema) {
        this(schema, FragmentState.IN_PROGRESS);
    }

    public RFC5424Message(final Fragment[] schema, final FragmentState state) {
        this.schema = schema;
        this.state = state;
    }

    @Override
    public FragmentState state() {
        return state;
    }

    @Override
    public Fragment apply(final TrackedLease<MemorySegment> trackedLease) {
        final Fragment[] updatedSchema = new Fragment[schema.length];
        for (int i = 0; i < schema.length; i++) {
            final Fragment fragment = schema[i];
            if (fragment.state().equals(FragmentState.IN_PROGRESS)) {
                updatedSchema[i] = fragment.apply(trackedLease);
            }

            if (!trackedLease.hasNext()) {
                break;
            }
            // Check if trackedLease hasNext
            // Otherwise, return new copy of RFC5424Message
        }

        return new RFC5424Message(updatedSchema);
    }

    @Override
    public TrackedLease<MemorySegment>[] leases() {
        return new TrackedLease[0];
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
