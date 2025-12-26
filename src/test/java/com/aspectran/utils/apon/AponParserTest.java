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

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.aspectran.utils.apon.AponFormat.SYSTEM_NEW_LINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        Parameters params = AponParser.parse(apon);

        assertTrue(params.isCompactStyle());
        assertEquals("John Doe", params.getString("name"));
        assertEquals(30, (int)params.getInt("age"));
        assertTrue(params.getBoolean("isActive"));
        assertEquals(123.45, params.getDouble("balance"), 0.0);
        assertEquals(1234567890123L, params.getLong("id").longValue());
        assertNull(params.getString("nullValue"));
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
        Parameters params = AponParser.parse(input);
        assertEquals("Hello World", params.getString("string"));
        assertEquals(123, (int)params.getInt("integer"));
        assertEquals(2147483648L, params.getLong("long").longValue());
        assertEquals(78.9f, params.getFloat("float"), 0.0f);
        assertEquals(0.1000000000000000055511151231257827021181583404541015625d, params.getDouble("double"), 0.0);
        assertTrue(params.getBoolean("boolean"));
        assertNull(params.getString("nullValue"));
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
        Parameters params = AponParser.parse(apon);
        List<String> items = params.getStringList("items");

        assertNotNull(items);
        assertEquals(3, items.size());
        assertEquals("apple", items.get(0));
        assertEquals("banana", items.get(1));
        assertEquals("cherry", items.get(2));
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
        Parameters params = AponParser.parse(apon);

        List<List<String>> matrix = (List<List<String>>)params.getValueList("matrix");

        assertNotNull(matrix);
        assertEquals(2, matrix.size());

        List<String> row1 = matrix.get(0);
        assertEquals(Arrays.asList("a", "b"), row1);

        List<String> row2 = matrix.get(1);
        assertEquals(Arrays.asList("c", "d", "e"), row2);
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
        Parameters config = AponParser.parse(apon).getParameters("config");

        assertEquals("App1", config.getString("name"));
        assertEquals(true, config.getParameters("settings").getBoolean("enabled"));
        assertEquals(2, config.getParametersList("users").size());
        assertEquals("Alice", config.getParametersList("users").get(0).getString("name"));
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
        Parameters params = AponParser.parse(apon);

        assertEquals("value1", params.getString("key1"));
        assertEquals("value2", params.getString("key2"));
        assertEquals(2, params.size());
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
        AponParser.parse(apon);
    }

    /**
     * Tests error handling for unclosed object blocks.
     */
    @Test(expected = AponParseException.class)
    public void testErrorHandlingUnclosedObject() throws AponParseException {
        String apon = "config: {\n" +
                "  key: value\n" +
                "# Missing closing brace\n";
        AponParser.parse(apon);
    }

    /**
     * Tests error handling for invalid line format.
     */
    @Test(expected = AponParseException.class)
    public void testErrorHandlingInvalidLineFormat() throws AponParseException {
        String apon = "key value # Missing colon\n";
        AponParser.parse(apon);
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

        Parameters params = AponParser.parse(apon);
        assertEquals(expected, params.getString("message"));
    }

    /**
     * Tests parsing of empty structures.
     */
    @Test
    public void testParseEmptyStructures() throws AponParseException {
        String apon = "emptyObject: {}\n" +
                "emptyArray: []\n";
        Parameters params = AponParser.parse(apon);

        Parameters emptyObject = params.getParameters("emptyObject");
        assertNotNull(emptyObject);
        assertTrue(emptyObject.isEmpty());

        List<?> emptyArray = params.getValueList("emptyArray");
        assertNotNull(emptyArray);
        assertTrue(emptyArray.isEmpty());
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
        Parameters params = AponParser.parse(apon);
        assertEquals("123", params.getString("val1"));
        assertEquals(456, (int)params.getInt("val2"));
        assertTrue(params.getBoolean("val3"));
        assertEquals(1.23, params.getDouble("val4"), 0.0);
        assertEquals(987L, params.getLong("val5").longValue());

        String hintedApon = new AponWriter().write(params).toString();
        assertEquals(apon.replace("\r\n", "\n"), hintedApon.replace("\r\n", "\n"));
    }

    /**
     * Tests that an invalid value for a hinted type throws an exception.
     */
    @Test
    public void testInvalidValueForHintedType() {
        String apon = "val(int): not-a-number";
        try {
            AponParser.parse(apon);
            fail("Should have thrown AponParseException");
        } catch (AponParseException e) {
            assertTrue(e.getMessage().contains("Invalid value 'not-a-number' for type 'int'"));
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
        Parameters params = AponParser.parse(apon);
        assertEquals("", params.getString("explicitEmpty"));
        assertNull(params.getString("implicitEmpty"));
        assertNull(params.getString("explicitNull"));
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
        Parameters params = AponParser.parse(apon);
        assertEquals(expected, params.getString("text"));
    }

    /**
     * Tests parsing of various string escape sequences.
     */
    @Test
    public void testStringUnescaping() throws AponParseException {
        String apon = "escaped: \"line1\\nline2\\t\\\\ \\\"quote\\\" \\u0041\""; // \u0041 is 'A'
        String expected = "line1" + '\n' + "line2" + '\t' + "\\ \"quote\" A";
        Parameters params = AponParser.parse(apon);
        assertEquals(expected, params.getString("escaped"));
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
            AponParser.parse(apon);
            fail("Should have thrown MalformedAponException");
        } catch (MalformedAponException e) {
            String message = e.getMessage();
            assertTrue(message.contains("[lineNumber: 4, columnNumber: 1]"));
            assertTrue(message.contains("bad line format"));
            assertTrue(message.contains("Invalid line format"));
        } catch (AponParseException e) {
            fail("Should have thrown MalformedAponException, not AponParseException");
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
        Parameters params = AponParser.parse(apon, VariableParameters.class);

        assertFalse(params.isCompactStyle());
        assertEquals("John Doe", params.getString("name"));
        assertEquals(30, (int)params.getInt("age"));
    }

    /**
     * Tests error handling for an unclosed braced root object.
     */
    @Test
    public void testErrorHandlingUnclosedBracedRoot() {
        String apon = "{\n" +
                "  name: John Doe\n"; // Missing closing brace
        try {
            AponParser.parse(apon);
            fail("Should have thrown AponParseException");
        } catch (AponParseException e) {
            assertTrue(e.getMessage().contains("Unclosed object block"));
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
            AponParser.parse(apon);
            fail("Should have thrown AponParseException");
        } catch (AponParseException e) {
            assertTrue(e.getMessage().contains("Unexpected content after closing brace"));
        }
    }

}
