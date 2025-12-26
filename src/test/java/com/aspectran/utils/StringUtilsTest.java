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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StringUtilsTest {

    @Test
    public void isEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("hello"));
    }

    @Test
    public void hasLength() {
        assertFalse(StringUtils.hasLength(null));
        assertFalse(StringUtils.hasLength(""));
        assertTrue(StringUtils.hasLength(" "));
        assertTrue(StringUtils.hasLength("hello"));
    }

    @Test
    public void hasText() {
        assertFalse(StringUtils.hasText(null));
        assertFalse(StringUtils.hasText(""));
        assertFalse(StringUtils.hasText(" "));
        assertTrue(StringUtils.hasText(" hello "));
    }

    @Test
    public void trimWhitespace() {
        assertEquals("hello", StringUtils.trimWhitespace("  hello  "));
        assertEquals("hello world", StringUtils.trimWhitespace("  hello world  "));
        assertEquals("", StringUtils.trimWhitespace("   "));
        assertNull(StringUtils.trimWhitespace(null));
    }

    @Test
    public void trimAllWhitespace() {
        assertEquals("helloworld", StringUtils.trimAllWhitespace("  hello world  "));
        assertEquals("helloworld", StringUtils.trimAllWhitespace("hello world"));
        assertEquals("", StringUtils.trimAllWhitespace("   "));
    }

    @Test
    public void trimLeadingCharacter() {
        assertEquals("  hello  ", StringUtils.trimLeadingCharacter("  hello  ", ','));
        assertEquals("hello--", StringUtils.trimLeadingCharacter("--hello--", '-'));
        assertEquals("hello__", StringUtils.trimLeadingCharacter("__hello__", '_'));
    }

    @Test
    public void trimTrailingCharacter() {
        assertEquals("  hello  ", StringUtils.trimTrailingCharacter("  hello  ", ','));
        assertEquals("--hello", StringUtils.trimTrailingCharacter("--hello--", '-'));
        assertEquals("__hello", StringUtils.trimTrailingCharacter("__hello__", '_'));
    }

    @Test
    public void split() {
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a,b,c", ","));
        assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.split("a,,b,c", ","));
        assertArrayEquals(new String[]{"a", "b", "c", ""}, StringUtils.split("a,b,c,", ","));
        assertArrayEquals(new String[]{"", "a", "b", "c"}, StringUtils.split(",a,b,c", ","));
    }

    @Test
    public void splitWithMultiCharDelimiter() {
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a||b||c", "||"));
        assertArrayEquals(new String[]{"a", "", "c"}, StringUtils.split("a||||c", "||"));
    }

    @Test
    public void splitWithComma() {
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.splitWithComma(" a, b, c "));
    }

    @Test
    public void tokenize() {
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.tokenize("a,b;c", ",;"));
        // StringTokenizer skips empty tokens
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.tokenize("a,,b,c", ","));
    }

    @Test
    public void join() {
        assertEquals("a,b,c", StringUtils.join(new String[]{"a", "b", "c"}, ","));
        assertEquals("a", StringUtils.join(new String[]{"a"}, ","));
        assertEquals("", StringUtils.join(new String[]{}, ","));
    }

    @Test
    public void joinWithCommas() {
        assertEquals("a, b, c", StringUtils.joinWithCommas(new String[]{"a", "b", "c"}));
        assertEquals("a, b, c", StringUtils.joinWithCommas(Arrays.asList("a", "b", "c")));
    }

    @Test
    public void replace() {
        assertEquals("he--o", StringUtils.replace("hello", "l", "-"));
        assertEquals("axbyc", StringUtils.replace("abc", new String[]{"a", "b"}, new String[]{"ax", "by"}));
    }

    @Test
    public void replaceWithOverlappingResults() {
        assertEquals("axbyc", StringUtils.replace("abc", new String[]{"a", "b"}, new String[]{"ax", "by"}));
        // After 'a' -> 'b', the string becomes "bbc". The next replacement for 'b' should apply to both.
        assertEquals("xxc", StringUtils.replace("abc", new String[]{"a", "b"}, new String[]{"b", "x"}));
    }

    @Test
    public void replaceLast() {
        assertEquals("helloo", StringUtils.replaceLast("hello-o", "-", ""));
    }

    @Test
    public void search() {
        assertEquals(2, StringUtils.search("hello", "l"));
        assertEquals(1, StringUtils.search("hello", "o"));
        assertEquals(2, StringUtils.search("aspectran", "a"));
    }

    @Test
    public void searchWithNonOverlappingPatterns() {
        assertEquals(2, StringUtils.search("aaaaa", "aa")); // non-overlapping
        assertEquals(1, StringUtils.search("ababab", "aba"));
    }

    @Test
    public void nullAndEmpty() {
        assertEquals("", StringUtils.nullToEmpty(null));
        assertEquals("hi", StringUtils.nullToEmpty("hi"));
        assertNull(StringUtils.emptyToNull(""));
        assertNull(StringUtils.emptyToNull(null));
        assertEquals("hi", StringUtils.emptyToNull("hi"));
    }

    @Test
    public void toStringArray_withEmptyOrNullCollections() {
        assertEquals(0, StringUtils.toStringArray((Collection<String>)null).length);
        assertEquals(0, StringUtils.toStringArray(Collections.<String>emptyList()).length);
    }

    @Test
    public void toStringArray_withValidCollection() {
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.toStringArray(Arrays.asList("a", "b")));
    }

}
