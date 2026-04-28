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
package com.aspectran.utils.apon;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for APON comments.
 */
public class AponCommentTest {

    @Test
    public void testInlineComments() throws AponParseException {
        String apon = "# This is a comment\n" +
                "key1: val1 # This is an inline comment\n" +
                "key2: val2 # Another one\n" +
                "# Full line comment\n" +
                "obj: {\n" +
                "    inner: value # Comment inside object\n" +
                "} # Comment after object\n" +
                "array: [\n" +
                "    item1 # Comment inside array\n" +
                "    item2\n" +
                "] # Comment after array\n";
        Parameters params = AponReader.read(apon);
        Assert.assertEquals("val1", params.getString("key1"));
        Assert.assertEquals("val2", params.getString("key2"));
        Assert.assertEquals("value", params.getParameters("obj").getString("inner"));
        Assert.assertEquals("item1", params.getStringList("array").get(0));
        Assert.assertEquals("item2", params.getStringList("array").get(1));
    }

    @Test
    public void testQuotedHash() throws AponParseException {
        String apon = "key1: \"val1 # not a comment\"\n" +
                "\"key2 # not a comment\": val2\n";
        Parameters params = AponReader.read(apon);
        Assert.assertEquals("val1 # not a comment", params.getString("key1"));
        Assert.assertEquals("val2", params.getString("key2 # not a comment"));
    }

    @Test
    public void testNestedInlineComments() throws AponParseException {
        // In APON, # is a line-end comment.
        // If multiple items are on the same line, everything after # is ignored.
        String apon = "key1: val1 # comment, key2: val2";
        Parameters params = AponReader.read(apon);
        Assert.assertEquals("val1", params.getString("key1"));
        // key2 should be null because it's part of the comment
        Assert.assertNull(params.getString("key2"));

        // If they are on different lines, it works as expected
        String apon2 = "key1: val1 # comment\n" +
                "key2: val2 # comment\n";
        Parameters params2 = AponReader.read(apon2);
        Assert.assertEquals("val1", params2.getString("key1"));
        Assert.assertEquals("val2", params2.getString("key2"));
    }

}
