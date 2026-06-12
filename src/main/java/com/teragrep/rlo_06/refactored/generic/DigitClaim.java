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
import com.teragrep.rlo_06.refactored.*;

import java.lang.foreign.MemorySegment;
import java.util.List;

public final class DigitClaim implements Claim<TrackedLease<MemorySegment>> {

    private final boolean nonZeroDigitsOnly;

    public DigitClaim() {
        this(false);
    }

    public DigitClaim(final boolean nonZeroDigitsOnly) {
        this.nonZeroDigitsOnly = nonZeroDigitsOnly;
    }

    @Override
    public Result<TrackedLease<MemorySegment>> advance(final List<TrackedLease<MemorySegment>> src) {
        for (final TrackedLease<MemorySegment> lease : src) {
            if (lease.hasNext()) {
                lease.mark();
                final byte b = lease.next();

                if (Character.isDigit(b) && b != '0' && nonZeroDigitsOnly) {
                    // Claim successful
                    return new ResultImpl<>(ResultName.DIGIT, lease.sliceWithLength(lease.currentPosition() - 1, 1L));
                }
                else if (Character.isDigit(b) && !nonZeroDigitsOnly) {
                    // Claim successful
                    return new ResultImpl<>(ResultName.DIGIT, lease.sliceWithLength(lease.currentPosition() - 1, 1));
                }
                else {
                    lease.reset();
                    throw new ClaimFailedException(
                            getClass(),
                            "expected " + (nonZeroDigitsOnly ? "non-zero" : "") + " digit but found "
                                    + Character.toString(b)
                    );
                }
            }
        }
        throw new ClaimFailedException(getClass(), "expected digit but found no next byte");
    }
}
