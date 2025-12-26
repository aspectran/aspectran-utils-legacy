/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DurationUtilsTest {

    @Test
    public void toHumanReadableNanos() {
        assertEquals("0ns", DurationUtils.toHumanReadableNanos(0L));
        assertEquals("999ns", DurationUtils.toHumanReadableNanos(999L));
        assertEquals("1µs", DurationUtils.toHumanReadableNanos(1000L));
        assertEquals("1.001µs", DurationUtils.toHumanReadableNanos(1001L));
        assertEquals("999.999µs", DurationUtils.toHumanReadableNanos(999999L));
        assertEquals("1ms", DurationUtils.toHumanReadableNanos(1000000L));
        assertEquals("999.999ms", DurationUtils.toHumanReadableNanos(999999999L));
        assertEquals("1s", DurationUtils.toHumanReadableNanos(1000000000L));
        assertEquals("1.500s", DurationUtils.toHumanReadableNanos(1500000000L));
        assertEquals("59.999s", DurationUtils.toHumanReadableNanos(59999999999L));
        assertEquals("1m", DurationUtils.toHumanReadableNanos(60000000000L));
        assertEquals("1m 1s", DurationUtils.toHumanReadableNanos(61500000000L));
        assertEquals("59m 59s", DurationUtils.toHumanReadableNanos(3599000000000L));
        assertEquals("1h", DurationUtils.toHumanReadableNanos(3600000000000L));
        assertEquals("1h 1m 1s", DurationUtils.toHumanReadableNanos(3661000000000L));
        assertEquals("0ns", DurationUtils.toHumanReadableNanos(-1L));
    }

    @Test
    public void toHumanReadableMillis() {
        assertEquals("0ms", DurationUtils.toHumanReadableMillis(0L));
        assertEquals("1ms", DurationUtils.toHumanReadableMillis(1L));
        assertEquals("1.500s", DurationUtils.toHumanReadableMillis(1500L));
        assertEquals("1m", DurationUtils.toHumanReadableMillis(60000L));
        assertEquals("1h", DurationUtils.toHumanReadableMillis(3600000L));
        assertEquals("0ms", DurationUtils.toHumanReadableMillis(-1L));
    }

}
