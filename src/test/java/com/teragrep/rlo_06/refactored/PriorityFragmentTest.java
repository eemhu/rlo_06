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
package com.teragrep.rlo_06.refactored;

import com.teragrep.buf_01.buffer.lease.MemorySegmentLeaseStub;
import com.teragrep.buf_01.buffer.lease.TrackedLease;
import com.teragrep.buf_01.buffer.pool.OpeningPool;
import com.teragrep.buf_01.buffer.supply.ArenaMemorySegmentLeaseSupplier;
import com.teragrep.poj_01.pool.UnboundPool;
import com.teragrep.rlo_06.refactored.queue.Fragment;
import com.teragrep.rlo_06.refactored.queue.FragmentState;
import com.teragrep.rlo_06.refactored.syslogMsg.header.PriorityFragment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

public final class PriorityFragmentTest {

    @Test
    void testSuccess() {
        Fragment priorityFragment = new PriorityFragment();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 2), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("<120>", pool).toList();

            for (final TrackedLease<MemorySegment> lease : leases) {
                priorityFragment = priorityFragment.apply(lease);
            }

            Assertions.assertEquals(FragmentState.SUCCESSFUL, priorityFragment.state());

            Assertions.assertEquals(3, priorityFragment.leases().length);
            Assertions.assertEquals('<', priorityFragment.leases()[0].next());
            Assertions.assertEquals('1', priorityFragment.leases()[0].next());
            Assertions.assertEquals('2', priorityFragment.leases()[1].next());
            Assertions.assertEquals('0', priorityFragment.leases()[1].next());
            Assertions.assertEquals('>', priorityFragment.leases()[2].next());

            // Success should advance the leases
            // Each lease has two bytes, so we should have 3 leases.
            Assertions.assertEquals(3, leases.size());
            Assertions.assertEquals(2L, leases.get(0).currentPosition());
            Assertions.assertEquals(2L, leases.get(1).currentPosition());
            Assertions.assertEquals(1L, leases.get(2).currentPosition());

        }
    }

    @Test
    void testFailure() {
        Fragment claim = new PriorityFragment();

        try (
                final OpeningPool pool = new OpeningPool(
                        new UnboundPool<>(new ArenaMemorySegmentLeaseSupplier(Arena.ofShared(), 1), new MemorySegmentLeaseStub())
                )
        ) {
            final List<TrackedLease<MemorySegment>> leases = new StringToLease("<abc", pool).toList();

            for (final TrackedLease<MemorySegment> lease : leases) {
                claim = claim.apply(lease);
            }

            Assertions.assertEquals(FragmentState.FAILED, claim.state());
            // Failure should not advance the leases
            {
                int i;
                for (i = 0; i < 4; i++) {
                    Assertions.assertEquals(0L, leases.get(i).currentPosition());
                }
                Assertions.assertEquals(4, i);
                Assertions.assertEquals(4, leases.size());
            }
        }
    }
}
