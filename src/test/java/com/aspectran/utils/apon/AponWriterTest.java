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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Test cases for AponWriter.
 *
 * <p>Created: 2020/05/29</p>
 */
public class AponWriterTest {

    @RunWith(Parameterized.class)
    public static class SpecialStringsTest {
        private final String inputValue;

        public SpecialStringsTest(String inputValue) {
            this.inputValue = inputValue;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"'"}, {"\""}, {" s "}, {"\u2019"}, {"\\u2019"}, {"a:b"}, {"{c}"}, {"[d]"}
            });
        }

        @Test
        public void testWriteAndReadBackSpecialStrings() throws IOException {
            Parameters parameters = new VariableParameters();
            parameters.putValue("param1", inputValue);

            StringWriter stringWriter = new StringWriter();
            new AponWriter(stringWriter).write(parameters);

            Parameters output = new AponReader(stringWriter.toString()).read();

            Assert.assertEquals(inputValue, output.getString("param1"));
        }
    }

    /**
     * Tests that a string with newlines is written as a text block and can be read back correctly.
     */
    @Test
    public void testWriteMultiLineStringAsTextBlock() throws IOException {
        String input = "1\n2\n3";
        input = input.replace("\n", AponFormat.SYSTEM_NEW_LINE);

        Parameters parameters = new VariableParameters();
        parameters.putValue("textParam", input);

        StringWriter stringWriter = new StringWriter();
        new AponWriter(stringWriter).write(parameters);
        String apon = stringWriter.toString();
        Assert.assertTrue(apon.contains("(\n".replace("\n", AponFormat.SYSTEM_NEW_LINE)));
        Assert.assertTrue(apon.contains("|1\n".replace("\n", AponFormat.SYSTEM_NEW_LINE)));

        Parameters output = new AponReader(apon).read();
        Assert.assertEquals(input, output.getString("textParam"));
    }

    /**
     * Tests writing various primitive data types.
     */
    @Test
    public void testWriteValueTypes() throws IOException {
        Parameters params = new VariableParameters();
        params.putValue("boolean", true);
        params.putValue("integer", 123);
        params.putValue("long", 456L);
        params.putValue("double", 78.9);
        params.putValue("nullValue", null);

        String apon = new AponWriter().enableValueTypeHints(true).write(params).toString();
        Parameters readParams = AponReader.read(apon);

        Assert.assertEquals(true, readParams.getBoolean("boolean"));
        Assert.assertEquals(123, (int)readParams.getInt("integer"));
        Assert.assertEquals(456L, readParams.getLong("long").longValue());
        Assert.assertEquals(78.9, readParams.getDouble("double"), 0.0);
        Assert.assertTrue(readParams.hasParameter("nullValue"));
        Assert.assertNull(readParams.getString("nullValue"));
    }

    /**
     * Tests writing nested structures like blocks and arrays.
     */
    @Test
    public void testWriteNestedStructures() throws IOException {
        Parameters params = new VariableParameters();
        Parameters nestedBlock = new VariableParameters();
        nestedBlock.putValue("key", "value");
        params.putValue("block", nestedBlock);
        List<String> list = Arrays.asList("a", "b", "c");
        params.putValue("array", list);

        String apon = new AponWriter().write(params).toString();
        Parameters readParams = AponReader.read(apon);

        Assert.assertEquals("value", readParams.getParameters("block").getString("key"));
        Assert.assertEquals(list, readParams.getStringList("array"));
    }

    /**
     * Tests the 'nullWritable' option.
     */
    @Test
    public void testNullWritableOption() throws IOException {
        Parameters params = new VariableParameters();
        params.putValue("key", "value");
        params.putValue("nullKey", null);

        // When nullWritable is false (default), null values are omitted
        AponWriter writer1 = new AponWriter().nullWritable(false);
        String apon1 = writer1.write(params).toString();
        Assert.assertFalse(apon1.contains("nullKey"));

        // When nullWritable is true, null values are included
        AponWriter writer2 = new AponWriter().nullWritable(true);
        String apon2 = writer2.write(params).toString();
        Assert.assertTrue(apon2.contains("nullKey: null"));
    }

    /**
     * Tests the 'nullWritable' option for toString().
     */
    @Test
    public void testToStringWithNullWritableOption() {
        Parameters params = new VariableParameters();
        params.putValue("key", "value");
        params.putValue("nullKey", null);

        // When nullWritable is false, null values are omitted
        String apon1 = params.toString(false);
        Assert.assertFalse(apon1.contains("nullKey"));

        // When nullWritable is true, null values are included
        String apon2 = params.toString(true);
        Assert.assertTrue(apon2.contains("nullKey"));
    }

    /**
     * Tests the indentation option for pretty formatting.
     */
    @Test
    public void testIndentationOption() throws IOException {
        Parameters params = new VariableParameters();
        Parameters nested = new VariableParameters();
        nested.putValue("key", "value");
        params.putValue("nested", nested);

        AponWriter writer = new AponWriter().indentString("  "); // 2 spaces
        String apon = writer.write(params).toString();

        String expected = "nested: {\n" +
                "  key: value\n" +
                "}\n";
        Assert.assertEquals(expected.replace("\r\n", "\n"), apon.replace("\r\n", "\n"));
    }

    @Test
    public void testCompactWritingOfEmptyStructures() throws IOException {
        Parameters params = new VariableParameters();
        params.putValue("emptyBlock", new VariableParameters());
        params.putValue("emptyArray", new ArrayList<String>());

        String expected = "emptyBlock: {}\n" +
                "emptyArray: []\n";
        Assert.assertEquals(expected.replace("\r\n", "\n"), params.toString().replace("\r\n", "\n"));

        // Test with prettyPrint = false
        AponWriter compactWriter = new AponWriter().prettyPrint(false);
        String compactApon = compactWriter.write(params).toString();
        String expectedCompact = "emptyBlock:{},emptyArray:[]";
        Assert.assertEquals(expectedCompact, compactApon);

        // Test with prettyPrint = true (default)
        params.setBraceless(false);
        AponWriter noCompactWriter = new AponWriter();
        String noCompactApon = noCompactWriter.write(params).toString();
        Assert.assertFalse(noCompactApon.contains("emptyBlock:{}"));
        Assert.assertFalse(noCompactApon.contains("emptyArray:[]"));
        Assert.assertTrue(noCompactApon.contains("emptyBlock: {}"));
        Assert.assertTrue(noCompactApon.contains("emptyArray: []"));
    }

    @Test
    public void testCompactObject() throws IOException {
        Parameters params = new VariableParameters();
        params.putValue("name", "John");
        params.putValue("age", 30);
        params.setRenderStyle(AponRenderStyle.COMPACT);

        AponWriter writer = new AponWriter();
        String apon = writer.write(params).toString();
        Assert.assertEquals("name:John,age:30", apon);
    }

    @Test
    public void testStringifyContext() throws IOException {
        Parameters params = new VariableParameters();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(2026, 2, 29, 16, 30, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        java.util.Date date = cal.getTime();
        params.putValue("dateTime", date);

        com.aspectran.utils.StringifyContext context = new com.aspectran.utils.StringifyContext();
        context.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        context.setPrettyPrint(false);

        AponWriter writer = new AponWriter().apply(context);
        String apon = writer.write(params).toString();

        Assert.assertEquals("dateTime:\"2026-03-29 16:30:00\"", apon);
    }

    /**
     * Tests that unassigned array parameters are not written as empty arrays.
     */
    @Test
    public void testUnassignedArrayParameters() throws IOException {
        LocalTestParameters params = new LocalTestParameters();
        // None of the parameters are assigned.

        // When nullWritable is false (default), unassigned parameters are omitted
        String apon1 = new AponWriter().nullWritable(false).write(params).toString();
        Assert.assertEquals("", apon1.trim());

        // When nullWritable is true, unassigned parameters are written as null
        String apon2 = new AponWriter().nullWritable(true).write(params).toString();
        Assert.assertTrue(apon2.contains("methods: null"));
        Assert.assertTrue(apon2.contains("headers: null"));
        Assert.assertTrue(apon2.contains("pointcut: null"));
        Assert.assertFalse(apon2.contains("[]"));
    }

    private static class LocalTestParameters extends DefaultParameters {
        static final ParameterKey methods = new ParameterKey("methods", ValueType.STRING, true);
        static final ParameterKey headers = new ParameterKey("headers", ValueType.STRING, true);
        static final ParameterKey pointcut = new ParameterKey("pointcut", VariableParameters.class, true);
        static final ParameterKey[] parameterKeys = new ParameterKey[] { methods, headers, pointcut };
        LocalTestParameters() { super(parameterKeys); }
    }

    /**
     * Tests that explicitly assigned empty arrays are still written as empty arrays [].
     */
    @Test
    public void testAssignedEmptyArrayParameters() throws IOException {
        TestParameters params = new TestParameters();
        params.putValue(TestParameters.methods, new ArrayList<String>());
        // The parameter is explicitly assigned an empty list.
        Assert.assertTrue(params.getParameter(TestParameters.methods).isAssigned());

        String apon = new AponWriter().write(params).toString();
        Assert.assertTrue(apon.contains("methods: []"));

        TestParameters params2 = AponReader.read(apon, TestParameters.class);
        String apon2 = new AponWriter().write(params2).toString();
        Assert.assertTrue(apon2.contains("methods: []"));
    }

    public static class TestParameters extends DefaultParameters {
        static final ParameterKey methods = new ParameterKey("methods", ValueType.STRING, true, true);
        static final ParameterKey[] parameterKeys = new ParameterKey[]{methods};

        public TestParameters() {
            super(parameterKeys);
        }
    }

}
