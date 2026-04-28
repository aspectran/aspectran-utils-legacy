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

import java.util.Arrays;
import java.util.List;

/**
 * Test cases for AponReader.
 *
 * <p>Created: 2020/05/30</p>
 */
public class AponReaderTest {

    /**
     * Tests reading a value containing an escaped unicode character.
     */
    @Test
    public void testReadWithEscapedUnicodeCharacter() throws AponParseException {
        String input = "name: \"she\\u2019s \"";
        AponReader reader = new AponReader(input);
        Parameters parameters = reader.read();
        Assert.assertEquals("she’s ", parameters.getString("name"));
    }

    /**
     * Tests reading a value containing a raw (unescaped) unicode character.
     */
    @Test
    public void testReadWithUnescapedUnicodeCharacter() throws AponParseException {
        String input = "name: she\u2019s";
        AponReader reader = new AponReader(input);
        Parameters parameters = reader.read();
        Assert.assertEquals("she\u2019s", parameters.getString("name"));
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
     * Tests parsing of a multi-line text block.
     */
    @Test
    public void testParseTextBlock() throws AponParseException {
        String input = "message: (\n" +
                "  |Line 1\n" +
                "  |Line 2\n" +
                "  |  Indented Line 3\n" +
                ")\n";
        String expected = "Line 1\n" +
                "Line 2\n" +
                "  Indented Line 3";
        Parameters params = AponReader.read(input);
        Assert.assertEquals(expected.replace("\r\n", "\n"), params.getString("message").replace("\r\n", "\n"));
    }

    /**
     * Tests parsing of a nested structure with blocks and arrays.
     */
    @Test
    public void testParseNestedStructure() throws AponParseException {
        String input = "config: {\n" +
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
        Parameters config = AponReader.read(input).getParameters("config");
        Assert.assertEquals("App1", config.getString("name"));
        Assert.assertEquals(true, config.getParameters("settings").getBoolean("enabled"));
        Assert.assertEquals(2, config.getParametersList("users").size());
        Assert.assertEquals("Alice", config.getParametersList("users").get(0).getString("name"));
    }

    /**
     * Tests that comments are properly ignored by the parser.
     */
    @Test
    public void testParseWithComments() throws AponParseException {
        String input = "# This is a full-line comment\n" +
                "key: value\n" +
                "# Another comment\n" +
                "anotherKey: anotherValue\n";
        Parameters params = AponReader.read(input);
        Assert.assertEquals("value", params.getString("key"));
        Assert.assertEquals("anotherValue", params.getString("anotherKey"));
        Assert.assertEquals(2, params.size());
    }

    /**
     * Tests that invalid syntax correctly throws AponParseException.
     */
    @Test(expected = AponParseException.class)
    public void testInvalidSyntaxThrowsException() throws AponParseException {
        AponReader.read("key value");    // missing colon
    }

    /**
     * Tests parsing of empty and whitespace-only input.
     */
    @Test
    public void testEmptyAndWhitespaceInput() throws AponParseException {
        Parameters p1 = AponReader.read("");
        Assert.assertTrue(p1.isEmpty());

        Parameters p2 = AponReader.read("   \n\t\r\n   ");
        Assert.assertTrue(p2.isEmpty());
    }

    @Test
    public void testParseSingleLineEmptyStructures() throws AponParseException {
        String input = "emptyBlock: {}\n" +
                "arrayWithEmpty: [\n" +
                "    {}\n" +
                "    {}\n" +
                "]\n";
        Parameters params = AponReader.read(input);

        Parameters emptyBlock = params.getParameters("emptyBlock");
        Assert.assertNotNull(emptyBlock);
        Assert.assertTrue(emptyBlock.isEmpty());

        List<Parameters> list = params.getParametersList("arrayWithEmpty");
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.get(0).isEmpty());
        Assert.assertTrue(list.get(1).isEmpty());
    }

    @Test
    public void testParseMultiDimensionalStringArray() throws AponParseException {
        String input = "matrix: [\n" +
                "  [\n" +
                "    a\n" +
                "    b\n" +
                "  ]\n" +
                "  [\n" +
                "     c\n" +
                "     d\n" +
                "     e\n" +
                "  ]\n" +
                "]\n";

        Parameters params = AponReader.read(input);
        Assert.assertNotNull(params);

        // AponReader does not parse into List<List<String>>.
        // Instead, it parses into a List of Parameters objects.
        @SuppressWarnings("unchecked")
        List<List<String>> matrix = (List<List<String>>)params.getValueList("matrix");
        Assert.assertNotNull(matrix);
        Assert.assertEquals(2, matrix.size());

        // Check the first inner array, which is parsed as a Parameters object
        List<String> row1 = matrix.get(0);
        Assert.assertNotNull(row1);
        Assert.assertEquals(Arrays.asList("a", "b"), row1);

        // Check the second inner array
        List<String> row2 = matrix.get(1);
        Assert.assertNotNull(row2);
        Assert.assertEquals(Arrays.asList("c", "d", "e"), row2);
    }

    @Test
    public void testParseBracedRoot() throws AponParseException {
        String apon = "{\n" +
                "  name: John Doe\n" +
                "  age: 30\n" +
                "}\n";
        Parameters params = AponReader.read(apon);

        Assert.assertFalse(params.isBraceless());
        Assert.assertEquals("John Doe", params.getString("name"));
        Assert.assertEquals(30, (int)params.getInt("age"));
    }

    @Test
    public void testParseNonBracedRootAndBraceless() throws AponParseException {
        String apon = "name: John Doe\n" +
                "age: 30\n";
        Parameters params = AponReader.read(apon);

        Assert.assertTrue(params.isBraceless());
        Assert.assertEquals("John Doe", params.getString("name"));
        Assert.assertEquals(30, (int)params.getInt("age"));
    }

    @Test
    public void testErrorHandlingUnclosedBracedRoot() {
        String apon = "{\n" +
                "  name: John Doe\n"; // Missing closing brace
        try {
            AponReader.read(apon);
            Assert.fail("Should have thrown AponParseException");
        } catch (AponParseException e) {
            Assert.assertTrue(e.getMessage().contains("no closing curly bracket"));
        }
    }

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

}
