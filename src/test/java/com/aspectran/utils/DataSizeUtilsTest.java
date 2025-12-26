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

public class DataSizeUtilsTest {

    @Test
    public void toHumanFriendlyByteSize() {
        assertEquals("0 B", DataSizeUtils.toHumanFriendlyByteSize(0L));
        assertEquals("500 B", DataSizeUtils.toHumanFriendlyByteSize(500L));
        assertEquals("1023 B", DataSizeUtils.toHumanFriendlyByteSize(1023L));
        assertEquals("1 KB", DataSizeUtils.toHumanFriendlyByteSize(1024L));
        assertEquals("1.5 KB", DataSizeUtils.toHumanFriendlyByteSize(1536L));
        assertEquals("1 MB", DataSizeUtils.toHumanFriendlyByteSize(1048576L));
        assertEquals("1.1 GB", DataSizeUtils.toHumanFriendlyByteSize(1234567890L));
        assertEquals("-1 KB", DataSizeUtils.toHumanFriendlyByteSize(-1024L));
        assertEquals("-1.5 KB", DataSizeUtils.toHumanFriendlyByteSize(-1536L));
    }

    @Test
    public void toMachineFriendlyByteSize() {
        assertEquals(1024L, DataSizeUtils.toMachineFriendlyByteSize("1k"));
        assertEquals(1024L, DataSizeUtils.toMachineFriendlyByteSize("1KB"));
        assertEquals(10485760L, DataSizeUtils.toMachineFriendlyByteSize("10m"));
        assertEquals(10485760L, DataSizeUtils.toMachineFriendlyByteSize("10MB"));
        assertEquals(1288490189L, DataSizeUtils.toMachineFriendlyByteSize("1.2 GB"));
        assertEquals(2576980378L, DataSizeUtils.toMachineFriendlyByteSize("2.4GB"));
        assertEquals(3932160L, DataSizeUtils.toMachineFriendlyByteSize("3.75MB"));
        assertEquals(1311L, DataSizeUtils.toMachineFriendlyByteSize("1.28KB"));
        assertEquals(1024L, DataSizeUtils.toMachineFriendlyByteSize("1024"));
        assertEquals(1024L, DataSizeUtils.toMachineFriendlyByteSize("1024B"));
        assertEquals(1073741824L, DataSizeUtils.toMachineFriendlyByteSize("  1 G  "));
        assertEquals(-2048L, DataSizeUtils.toMachineFriendlyByteSize("-2kb"));
    }

    @Test(expected = NumberFormatException.class)
    public void toMachineFriendlyByteSize_withInvalidInput1() {
        DataSizeUtils.toMachineFriendlyByteSize("1.2.3 GB");
    }

    @Test(expected = NumberFormatException.class)
    public void toMachineFriendlyByteSize_withInvalidInput2() {
        DataSizeUtils.toMachineFriendlyByteSize("1 ZB");
    }

    @Test(expected = NumberFormatException.class)
    public void toMachineFriendlyByteSize_withInvalidInput3() {
        DataSizeUtils.toMachineFriendlyByteSize("KB");
    }

    @Test(expected = NumberFormatException.class)
    public void toMachineFriendlyByteSize_withInvalidInput4() {
        DataSizeUtils.toMachineFriendlyByteSize("");
    }

    @Test(expected = NumberFormatException.class)
    public void toMachineFriendlyByteSize_withInvalidInput5() {
        DataSizeUtils.toMachineFriendlyByteSize("  ");
    }

}
