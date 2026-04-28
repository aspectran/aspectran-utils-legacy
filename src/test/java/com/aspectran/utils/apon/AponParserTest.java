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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.aspectran.utils.apon.AponFormat.SYSTEM_NEW_LINE;

/**
 * Test cases for AponParser.
 *
 * <p>Created: 2025-11-13</p>
 */
public class AponParserTest {

    /**
     * Tests parsing of a basic APON object with scalar values.
     */
    @Test
    public void testParseBasicObject() throws AponParseException {
        String apon = "name: John Doe\n" +
                "age: 30\n" +
                "isActive: true\n" +
                "balance: 123.45\n" +
                "id: 1234567890123\n" +
                "nullValue: null\n";
        Parameters params = AponReader.read(apon);

        Assert.assertTrue(params.isBraceless());
        Assert.assertEquals("John Doe", params.getString("name"));
        Assert.assertEquals(30, (int)params.getInt("age"));
        Assert.assertTrue(params.getBoolean("isActive"));
        Assert.assertEquals(123.45, params.getDouble("balance"), 0.0);
        Assert.assertEquals(1234567890123L, params.getLong("id").longValue());
        Assert.assertNull(params.getString("nullValue"));
    }

    /**
     * Tests parsing of various primitive data types and null.
     */
    @Test
    public void testParseValueTypes() throws AponParseException {
        String input = "string: Hello World\n" +
                "integer: 123\n" +
                "long: 2147483648\n" +
                "float(float): 78.9\n" +
                "double: 0.1000000000000000055511151231257827021181583404541015625\n" +
                "boolean: true\n" +
                "nullValue: null\n";
        Parameters params = AponReader.read(input);
        Assert.assertEquals("Hello World", params.getString("string"));
        Assert.assertEquals(123, (int)params.getInt("integer"));
        Assert.assertEquals(2147483648L, params.getLong("long").longValue());
        Assert.assertEquals(78.9f, params.getFloat("float"), 0.0f);
        Assert.assertEquals(0.1000000000000000055511151231257827021181583404541015625d, params.getDouble("double"), 0.0);
        Assert.assertTrue(params.getBoolean("boolean"));
        Assert.assertNull(params.getString("nullValue"));
    }

    /**
     * Tests parsing of a one-dimensional array of scalar values.
     */
    @Test
    public void testParseOneDimensionalArray() throws AponParseException {
        String apon = "items: [\n" +
                "  \"apple\"\n" +
                "  \"banana\"\n" +
                "  \"cherry\"\n" +
                "]\n";
        Parameters params = AponReader.read(apon);
        List<String> items = params.getStringList("items");

        Assert.assertNotNull(items);
        Assert.assertEquals(3, items.size());
        Assert.assertEquals("apple", items.get(0));
        Assert.assertEquals("banana", items.get(1));
        Assert.assertEquals("cherry", items.get(2));
    }

    /**
     * Tests parsing of a multi-dimensional array (List of Lists).
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testParseMultiDimensionalArray() throws AponParseException {
        String apon = "matrix: [\n" +
                "  [\n" +
                "    \"a\"\n" +
                "    \"b\"\n" +
                "  ]\n" +
                "  [\n" +
                "    \"c\"\n" +
                "    \"d\"\n" +
                "    \"e\"\n" +
                "  ]\n" +
                "]\n";
        Parameters params = AponReader.read(apon);

        List<List<String>> matrix = (List<List<String>>)params.getValueList("matrix");

        Assert.assertNotNull(matrix);
        Assert.assertEquals(2, matrix.size());

        List<String> row1 = matrix.get(0);
        Assert.assertEquals(Arrays.asList("a", "b"), row1);

        List<String> row2 = matrix.get(1);
        Assert.assertEquals(Arrays.asList("c", "d", "e"), row2);
    }

    /**
     * Tests parsing of nested objects and arrays.
     */
    @Test
    public void testParseNestedStructure() throws AponParseException {
        String apon = "config: {\n" +
                "  name: App1\n" +
                "  settings: {\n" +
                "    enabled: true\n" +
                "    retries: 3\n" +
                "  }\n" +
                "  users: [\n" +
                "    {\n" +
                "        name: Alice\n" +
                "        role: admin\n" +
                "    }\n" +
                "    {\n" +
                "        name: Bob\n" +
                "        role: user\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
        Parameters config = AponReader.read(apon).getParameters("config");

        Assert.assertEquals("App1", config.getString("name"));
        Assert.assertEquals(true, config.getParameters("settings").getBoolean("enabled"));
        Assert.assertEquals(2, config.getParametersList("users").size());
        Assert.assertEquals("Alice", config.getParametersList("users").get(0).getString("name"));
    }

    /**
     * Tests parsing with comments and empty lines.
     */
    @Test
    public void testParseWithCommentsAndEmptyLines() throws AponParseException {
        String apon = "# This is a comment\n" +
                "key1: value1\n" +
                "\n" +
                "# Another comment\n" +
                "key2: value2\n";
        Parameters params = AponReader.read(apon);

        Assert.assertEquals("value1", params.getString("key1"));
        Assert.assertEquals("value2", params.getString("key2"));
        Assert.assertEquals(2, params.size());
    }

    /**
     * Tests error handling for unclosed array brackets.
     */
    @Test(expected = AponParseException.class)
    public void testErrorHandlingUnclosedArray() throws AponParseException {
        String apon = "items: [\n" +
                "  \"item1\"\n" +
                "  \"item2\"\n" +
                "# Missing closing bracket\n";
        AponReader.read(apon);
    }

    /**
     * Tests error handling for unclosed object blocks.
     */
    @Test(expected = AponParseException.class)
    public void testErrorHandlingUnclosedObject() throws AponParseException {
        String apon = "config: {\n" +
                "  key: value\n" +
                "# Missing closing brace\n";
        AponReader.read(apon);
    }

    /**
     * Tests error handling for invalid line format.
     */
    @Test(expected = AponParseException.class)
    public void testErrorHandlingInvalidLineFormat() throws AponParseException {
        String apon = "key value # Missing colon\n";
        AponReader.read(apon);
    }

    /**
     * Tests parsing of a multi-line text block.
     */
    @Test
    public void testParseTextBlock() throws AponParseException {
        String apon = "message: (\n" +
                "  |Line 1\n" +
                "  |  Line 2\n" +
                "  |Line 3\n" +
                ")\n";
        String expected = "Line 1" + SYSTEM_NEW_LINE +
                "  Line 2" + SYSTEM_NEW_LINE +
                "Line 3";

        Parameters params = AponReader.read(apon);
        Assert.assertEquals(expected, params.getString("message"));
    }

    /**
     * Tests parsing of empty structures.
     */
    @Test
    public void testParseEmptyStructures() throws AponParseException {
        String apon = "emptyObject: {}\n" +
                "emptyArray: []\n";
        Parameters params = AponReader.read(apon);

        Parameters emptyObject = params.getParameters("emptyObject");
        Assert.assertNotNull(emptyObject);
        Assert.assertTrue(emptyObject.isEmpty());

        List<?> emptyArray = (List<?>)params.getValueList("emptyArray");
        Assert.assertNotNull(emptyArray);
        Assert.assertTrue(emptyArray.isEmpty());
    }

    /**
     * Tests parsing with value type hints.
     */
    @Test
    public void testValueTypeHinting() throws IOException {
        String apon = "val1(string): 123\n" +
                "val2(int): 456\n" +
                "val3(boolean): true\n" +
                "val4(double): 1.23\n" +
                "val5(long): 987\n";
        Parameters params = AponReader.read(apon);
        Assert.assertEquals("123", params.getString("val1"));
        Assert.assertEquals(456, (int)params.getInt("val2"));
        Assert.assertTrue(params.getBoolean("val3"));
        Assert.assertEquals(1.23, params.getDouble("val4"), 0.0);
        Assert.assertEquals(987L, params.getLong("val5").longValue());

        String hintedApon = new AponWriter().write(params).toString();
        Assert.assertEquals(apon.replace("\r\n", "\n"), hintedApon.replace("\r\n", "\n"));
    }

    /**
     * Tests that an invalid value for a hinted type throws an exception.
     */
    @Test
    public void testInvalidValueForHintedType() {
        String apon = "val(int): not-a-number";
        try {
            AponReader.read(apon);
            Assert.fail("Should have thrown AponParseException");
        } catch (AponParseException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid value 'not-a-number' for type 'int'"));
        }
    }

    /**
     * Tests the handling of implicit empty values (null) vs. explicit empty strings.
     */
    @Test
    public void testEmptyAndNullValueHandling() throws AponParseException {
        String apon = "explicitEmpty: \"\"\n" +
                "implicitEmpty:\n" +
                "explicitNull: null\n";
        Parameters params = AponReader.read(apon);
        Assert.assertEquals("", params.getString("explicitEmpty"));
        Assert.assertNull(params.getString("implicitEmpty"));
        Assert.assertNull(params.getString("explicitNull"));
    }

    /**
     * Tests that whitespace is preserved correctly in text blocks.
     */
    @Test
    public void testTextBlockWhitespacePreservation() throws AponParseException {
        String apon = "text: (" + SYSTEM_NEW_LINE +
                      "  |  leading and trailing spaces  " + SYSTEM_NEW_LINE +
                      "  |" + SYSTEM_NEW_LINE +
                      "  |    line with just spaces    " + SYSTEM_NEW_LINE +
                      "  |last line" + SYSTEM_NEW_LINE +
                      ")" + SYSTEM_NEW_LINE;
        String expected = "  leading and trailing spaces  " + SYSTEM_NEW_LINE +
                "" + SYSTEM_NEW_LINE +
                "    line with just spaces    " + SYSTEM_NEW_LINE +
                "last line";
        Parameters params = AponReader.read(apon);
        Assert.assertEquals(expected, params.getString("text"));
    }

    /**
     * Tests parsing of various string escape sequences.
     */
    @Test
    public void testStringUnescaping() throws AponParseException {
        String apon = "escaped: \"line1\\nline2\\t\\\\ \\\"quote\\\" \\u0041\""; // \u0041 is 'A'
        String expected = "line1" + '\n' + "line2" + '\t' + "\\ \"quote\" A";
        Parameters params = AponReader.read(apon);
        Assert.assertEquals(expected, params.getString("escaped"));
    }

    /**
     * Tests that MalformedAponException contains detailed error information.
     */
    @Test
    public void testMalformedAponExceptionDetails() {
        String apon = "# Line 1\n" +
                "# Line 2\n" +
                "good: value\n" +
                "bad line format\n";
        try {
            AponReader.read(apon);
            Assert.fail("Should have thrown MalformedAponException");
        } catch (MalformedAponException e) {
            String message = e.getMessage();
            Assert.assertTrue(message.contains("[lineNumber: 4, columnNumber: 1]"));
            Assert.assertTrue(message.contains("bad line format"));
            Assert.assertTrue(message.contains("Invalid line format"));
        } catch (AponParseException e) {
            Assert.fail("Should have thrown MalformedAponException");
        }
    }

    /**
     * Tests parsing of a root object enclosed in braces.
     */
    @Test
    public void testParseBracedRoot() throws AponParseException {
        String apon = "{\n" +
                "  name: John Doe\n" +
                "  age: 30\n" +
                "}\n";
        Parameters params = AponReader.read(apon, VariableParameters.class);

        Assert.assertFalse(params.isBraceless());
        Assert.assertEquals("John Doe", params.getString("name"));
        Assert.assertEquals(30, (int)params.getInt("age"));
    }

    /**
     * Tests error handling for an unclosed braced root object.
     */
    @Test
    public void testErrorHandlingUnclosedBracedRoot() {
        String apon = "{\n" +
                "  name: John Doe\n"; // Missing closing brace
        try {
            AponReader.read(apon);
            Assert.fail("Should have thrown AponParseException");
        } catch (AponParseException e) {
            Assert.assertTrue(e.getMessage().contains("no closing curly bracket") ||
                    e.getMessage().contains("Unclosed object block"));
        }
    }

    /**
     * Tests error handling for trailing content after a closed braced root.
     */
    @Test
    public void testErrorHandlingTrailingContentAfterBracedRoot() {
        String apon = "{\n" +
                "  name: John Doe\n" +
                "}\n" +
                "extra: content\n";
        try {
            AponReader.read(apon);
            Assert.fail("Should have thrown AponParseException");
        } catch (AponParseException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected content after closing brace"));
        }
    }

    /**
     * Tests parsing of various edge cases and potential infinite loop scenarios.
     */
    @Test(timeout = 5000)
    public void testEdgeCases() throws AponParseException {
        // Empty/whitespace/comments
        AponReader.read("");
        AponReader.read("   ");
        AponReader.read("\n\n\n");
        AponReader.read("# comment");
        AponReader.read(" , , , ");

        // Unclosed structures
        try { AponReader.read("key: \"unclosed"); Assert.fail(); } catch (AponParseException ignored) {}
        try { AponReader.read("key: { "); Assert.fail(); } catch (AponParseException ignored) {}
        try { AponReader.read("key: [ "); Assert.fail(); } catch (AponParseException ignored) {}
        try { AponReader.read("key: (\n|line1\n"); Assert.fail(); } catch (AponParseException ignored) {}

        // Trailing escape
        Parameters p1 = AponReader.read("key: value\\");
        Assert.assertEquals("value", p1.getString("key"));

        // Incomplete hint (treated as part of the name)
        Parameters p2 = AponReader.read("key(: value");
        Assert.assertEquals("value", p2.getString("key("));

        // Invalid top-level characters
        try { AponReader.read("key: value}"); Assert.fail(); } catch (AponParseException ignored) {}
        try { AponReader.read("key: value]"); Assert.fail(); } catch (AponParseException ignored) {}

        // Multiple items on one line
        Parameters p3 = AponReader.read("key1: val1, key2: val2, key3: val3");
        Assert.assertEquals("val1", p3.getString("key1"));
        Assert.assertEquals("val2", p3.getString("key2"));
        Assert.assertEquals("val3", p3.getString("key3"));

        // Complex unquoted values
        Parameters p4 = AponReader.read("key: value with {curly and [square open");
        Assert.assertEquals("value with {curly and [square open", p4.getString("key"));

        // Deeply nested but broken
        String apon = "{ { { { { { { { { { { { { { { { { { { { ";
        try { AponReader.read(apon); Assert.fail(); } catch (AponParseException ignored) {}

        // Long whitespace
        Parameters p5 = AponReader.read("key:                 value                ");
        Assert.assertEquals("value", p5.getString("key"));
    }

}
