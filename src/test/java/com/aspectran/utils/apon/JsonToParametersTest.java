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

/**
 * Test cases for converting JSON to APON Parameters.
 *
 * <p>Created: 2019-06-29</p>
 */
public class JsonToParametersTest {

    /**
     * Tests converting a JSON array to an ArrayParameters object.
     */
    @Test
    public void testConvertJsonArrayToArrayParameters() throws IOException {
        String json = "[\n" +
                "  { \"param1\": 111, \"param2\": 222 },\n" +
                "  { \"param3\": 333, \"param4\": 444 },\n" +
                "  null\n" +
                "]\n";
        String apon = "[\n" +
                "  {\n" +
                "    param1: 111\n" +
                "    param2: 222\n" +
                "  }\n" +
                "  {\n" +
                "    param3: 333\n" +
                "    param4: 444\n" +
                "  }\n" +
                "  null\n" +
                "]\n";

        ArrayParameters params = JsonToParameters.from(json, ArrayParameters.class);
        Assert.assertEquals(3, params.getParametersList().size());
        Assert.assertEquals(111, (int)params.getParametersList().get(0).getInt("param1"));
        Assert.assertEquals(222, (int)params.getParametersList().get(0).getInt("param2"));
        Assert.assertEquals(333, (int)params.getParametersList().get(1).getInt("param3"));
        Assert.assertEquals(444, (int)params.getParametersList().get(1).getInt("param4"));
        Assert.assertEquals(apon.replace("\r\n", "\n"), params.toString().replace("\r\n", "\n"));
    }

    /**
     * Tests converting a complex, nested JSON object.
     */
    @Test
    public void testConvertComplexJsonObject() throws IOException {
        String json = "{\n" +
                "    \"glossary\": {\n" +
                "        \"title\": \"example glossary\",\n" +
                "        \"GlossDiv\": {\n" +
                "            \"title\": \"S\",\n" +
                "            \"GlossList\": {\n" +
                "                \"GlossEntry\": {\n" +
                "                    \"ID\": \"SGML\",\n" +
                "                    \"GlossSeeAlso\": [\"GML\", \"XML\"]\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        Parameters params = JsonToParameters.from(json);
        Parameters glossary = params.getParameters("glossary");
        Assert.assertNotNull(glossary);
        Assert.assertEquals("example glossary", glossary.getString("title"));

        Parameters glossEntry = glossary.getParameters("GlossDiv").getParameters("GlossList").getParameters("GlossEntry");
        Assert.assertNotNull(glossEntry);
        Assert.assertEquals("SGML", glossEntry.getString("ID"));
        Assert.assertEquals(Arrays.asList("GML", "XML"), glossEntry.getStringList("GlossSeeAlso"));
    }

    /**
     * Tests converting JSON to a specific, typed Parameters subclass.
     */
    @Test
    public void testConvertJsonToTypedParameters() throws IOException {
        String json = "{\"message\": \"line1\\nline2\"}";

        MessagePayload messagePayload = JsonToParameters.from(json, MessagePayload.class);
        Assert.assertEquals("line1\nline2", messagePayload.getContent());

        // Verify that the converted object can be read back by AponReader
        String apon = messagePayload.toString();
        MessagePayload rereadPayload = new AponReader(apon).read(new MessagePayload());
        Assert.assertEquals(messagePayload.getContent(), rereadPayload.getContent());
    }

    public static class MessagePayload extends DefaultParameters {
        private static final ParameterKey message = new ParameterKey("message", ValueType.STRING);
        private static final ParameterKey[] parameterKeys = { message };

        public MessagePayload() {
            super(parameterKeys);
        }

        public String getContent() {
            return getString(message);
        }
    }

    /**
     * Tests converting a JSON object that contains an array of objects.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testConvertJsonObjectWithArray() throws IOException {
        String json = "{\n" +
                "  \"arrayObject1\": [\n" +
                "    { \"key1\": \"value1\" }\n" +
                "  ],\n" +
                "  \"arrayObject2\": [\n" +
                "    { \"key2-1\": \"value2-1\" },\n" +
                "    { \"key2-2\": \"value2-2\" }\n" +
                "  ],\n" +
                "  \"arrayString1\": [\n" +
                "    \"str1\"\n" +
                "  ],\n" +
                "  \"arrayString2\": [\n" +
                "    \"str1\", \"str2\"\n" +
                "  ],\n" +
                "  \"arrayStringWithNull\": [\n" +
                "    \"str1\", null\n" +
                "  ],\n" +
                "  \"arrayNullWithString\": [\n" +
                "    null, \"str2\"\n" +
                "  ],\n" +
                "  \"arrayInt1\": [\n" +
                "    1\n" +
                "  ],\n" +
                "  \"arrayInt2\": [\n" +
                "    1, 2\n" +
                "  ]\n" +
                "}\n";

        Parameters parameters = JsonToParameters.from(json);
        List<Parameters> arrayObject1 = parameters.getParametersList("arrayObject1");
        Assert.assertEquals(1, arrayObject1.size());
        Assert.assertEquals("value1", arrayObject1.get(0).getString("key1"));

        List<Parameters> arrayObject2 = parameters.getParametersList("arrayObject2");
        Assert.assertNotNull(arrayObject2);
        Assert.assertEquals(2, arrayObject2.size());
        Assert.assertEquals("value2-1", arrayObject2.get(0).getString("key2-1"));
        Assert.assertEquals("value2-2", arrayObject2.get(1).getString("key2-2"));

        List<String> arrayString1 = parameters.getStringList("arrayString1");
        Assert.assertEquals(1, arrayString1.size());
        Assert.assertEquals("str1", arrayString1.get(0));

        List<String> arrayString2 = parameters.getStringList("arrayString2");
        Assert.assertEquals(2, arrayString2.size());
        Assert.assertEquals("str1", arrayString2.get(0));
        Assert.assertEquals("str2", arrayString2.get(1));

        List<String> arrayStringWithNull = parameters.getStringList("arrayStringWithNull");
        Assert.assertEquals(2, arrayStringWithNull.size());
        Assert.assertEquals("str1", arrayStringWithNull.get(0));
        Assert.assertNull(arrayStringWithNull.get(1));

        List<String> arrayNullWithString = parameters.getStringList("arrayNullWithString");
        Assert.assertEquals(2, arrayNullWithString.size());
        Assert.assertNull(arrayNullWithString.get(0));
        Assert.assertEquals("str2", arrayNullWithString.get(1));

        List<Integer> arrayInt1 = parameters.getIntList("arrayInt1");
        Assert.assertEquals(1, arrayInt1.size());
        Assert.assertEquals(Integer.valueOf(1), arrayInt1.get(0));

        List<Integer> arrayInt2 = parameters.getIntList("arrayInt2");
        Assert.assertEquals(2, arrayInt2.size());
        Assert.assertEquals(Integer.valueOf(1), arrayInt2.get(0));
        Assert.assertEquals(Integer.valueOf(2), arrayInt2.get(1));
    }

    /**
     * Tests conversion of all JSON primitive types.
     */
    @Test
    public void testJsonPrimitiveTypes() throws IOException {
        String json = "{\n" +
                "  \"string\": \"hello\",\n" +
                "  \"integer\": 123,\n" +
                "  \"long\": 1234567890123,\n" +
                "  \"float\": 45.67,\n" +
                "  \"double\": 98.76,\n" +
                "  \"boolean\": true,\n" +
                "  \"nullValue\": null\n" +
                "}\n";
        Parameters params = JsonToParameters.from(json);
        Assert.assertEquals("hello", params.getString("string"));
        Assert.assertEquals(123, (int)params.getInt("integer"));
        Assert.assertEquals(1234567890123L, params.getLong("long").longValue());
        Assert.assertEquals(45.67, params.getDouble("float"), 0.0001); // Assert as double, with a delta for float comparison
        Assert.assertEquals(98.76, params.getDouble("double"), 0.0001);
        Assert.assertTrue(params.getBoolean("boolean"));
        Assert.assertNull(params.getString("nullValue"));
    }

    /**
     * Tests conversion of empty JSON structures.
     */
    @Test
    public void testEmptyJsonStructures() throws IOException {
        // Empty object
        Parameters p1 = JsonToParameters.from("{}");
        Assert.assertTrue(p1.isEmpty());

        // Empty array
        Parameters p2 = JsonToParameters.from("{\"emptyArray\":[]}");
        Assert.assertTrue(p2.getValueList("emptyArray").isEmpty());
    }

    /**
     * Tests that invalid JSON input throws an exception.
     */
    @Test
    public void testLenientParsing() throws IOException {
        // Lenient mode should parse non-standard JSON
        Parameters p1 = JsonToParameters.from("{ key: 'value' }", true);
        Assert.assertEquals("value", p1.getString("key"));

        Parameters p2 = JsonToParameters.from("{ \"key\": \"value\", }", true);
        Assert.assertEquals("value", p2.getString("key"));

        // Strict mode (default) should fail
        try {
            JsonToParameters.from("{ key: 'value' }");
            Assert.fail("Should have thrown IOException");
        } catch (IOException e) {
            // Expected
        }
        try {
            JsonToParameters.from("{ \"key\": \"value\", }");
            Assert.fail("Should have thrown IOException");
        } catch (IOException e) {
            // Expected
        }
        try {
            JsonToParameters.from("not json");
            Assert.fail("Should have thrown IOException");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test
    public void testFloatConversionWithTypedParameters() throws IOException {
        String json = "{\"floatValue\": 45.67}";

        TypedPayload payload = JsonToParameters.from(json, TypedPayload.class);
        Assert.assertEquals(45.67f, payload.getFloatValue(), 0.001f);
    }

    public static class TypedPayload extends DefaultParameters {
        private static final ParameterKey floatValue = new ParameterKey("floatValue", ValueType.FLOAT);
        private static final ParameterKey[] parameterKeys = { floatValue };

        public TypedPayload() {
            super(parameterKeys);
        }

        public float getFloatValue() {
            return getFloat(floatValue);
        }
    }

    @Test
    public void testStringWithQuote() throws IOException {
        String json = "{\"name\":\"she's\"}";
        Parameters parameters = JsonToParameters.from(json);
        String expected = "name: \"she's\"";
        String actual = parameters.toString().trim();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testMixedJsonObject() throws IOException {
        String json = "{\n" +
                "  \"intro\": \"Start Testing Now!\",\n" +
                "  \"one\": 1,\n" +
                "  \"two\": 2,\n" +
                "  \"three\": 3,\n" +
                "  \"nullArray\": [\n" +
                "    null,\n" +
                "    null\n" +
                "  ],\n" +
                "  \"customers\": [\n" +
                "    {\n" +
                "      \"id\": \"guest-1\",\n" +
                "      \"name\": \"Guest1\",\n" +
                "      \"age\": 21,\n" +
                "      \"approved\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"guest-2\",\n" +
                "      \"name\": \"Guest2\",\n" +
                "      \"age\": 22,\n" +
                "      \"approved\": true\n" +
                "    }\n" +
                "  ],\n" +
                "  \"emptyMap\": {\n" +
                "  }\n" +
                "}\n";

        String expected = "intro: Start Testing Now!\n" +
                "one: 1\n" +
                "two: 2\n" +
                "three: 3\n" +
                "nullArray: [\n" +
                "  null\n" +
                "  null\n" +
                "]\n" +
                "customers: [\n" +
                "  {\n" +
                "    id: guest-1\n" +
                "    name: Guest1\n" +
                "    age: 21\n" +
                "    approved: true\n" +
                "  }\n" +
                "  {\n" +
                "    id: guest-2\n" +
                "    name: Guest2\n" +
                "    age: 22\n" +
                "    approved: true\n" +
                "  }\n" +
                "]\n" +
                "emptyMap: {}\n";
        expected = expected.replace("\n", AponFormat.SYSTEM_NEW_LINE);

        Parameters parameters = JsonToParameters.from(json);

        String actual = new AponWriter()
                .nullWritable(true)
                .write(parameters)
                .toString();

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests converting a JSON double array (array of arrays) to Parameters.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testConvertJsonDoubleArray() throws IOException {
        String json = "{\n" +
                "  \"matrix\": [\n" +
                "    [\"a\", \"b\"],\n" +
                "    [\"c\", \"d\", \"e\"]\n" +
                "  ],\n" +
                "  \"numbers\": [\n" +
                "    [1, 2],\n" +
                "    [3, 4, 5]\n" +
                "  ]\n" +
                "}\n";

        String apon = "matrix: [\n" +
                "  [\n" +
                "    a\n" +
                "    b\n" +
                "  ]\n" +
                "  [\n" +
                "    c\n" +
                "    d\n" +
                "    e\n" +
                "  ]\n" +
                "]\n" +
                "numbers: [\n" +
                "  [\n" +
                "    1\n" +
                "    2\n" +
                "  ]\n" +
                "  [\n" +
                "    3\n" +
                "    4\n" +
                "    5\n" +
                "  ]\n" +
                "]\n";
        apon = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);

        Parameters parameters = JsonToParameters.from(json);
        Assert.assertNotNull(parameters);

        // Test matrix (array of string arrays)
        List<List<String>> matrix = (List<List<String>>)parameters.getValueList("matrix");
        Assert.assertNotNull(matrix);
        Assert.assertEquals(2, matrix.size());

        List<String> row1 = matrix.get(0);
        Assert.assertNotNull(row1);
        Assert.assertEquals(Arrays.asList("a", "b"), row1);

        List<String> row2 = matrix.get(1);
        Assert.assertNotNull(row2);
        Assert.assertEquals(Arrays.asList("c", "d", "e"), row2);

        // Test numbers (array of integer arrays)
        List<List<Integer>> numbers = (List<List<Integer>>)parameters.getValueList("numbers");
        Assert.assertNotNull(numbers);
        Assert.assertEquals(2, numbers.size());

        List<Integer> numRow1 = numbers.get(0);
        Assert.assertNotNull(numRow1);
        Assert.assertEquals(Arrays.asList(1, 2), numRow1);

        List<Integer> numRow2 = numbers.get(1);
        Assert.assertNotNull(numRow2);
        Assert.assertEquals(Arrays.asList(3, 4, 5), numRow2);

        String actualApon = new AponWriter()
                .nullWritable(true)
                .write(parameters)
                .toString();

        Assert.assertEquals(apon, actualApon);
    }

    /**
     * Defines a Parameters class with a fixed structure for location data.
     * This class is used to represent the intended structure of objects
     * within the complex 3D array test.
     */
    public static class LocationParameters extends DefaultParameters {

        private static final ParameterKey id = new ParameterKey("id", ValueType.LONG);
        private static final ParameterKey name = new ParameterKey("name", ValueType.STRING);
        private static final ParameterKey type = new ParameterKey("type", ValueType.STRING);

        private static final ParameterKey[] parameterKeys = {id, name, type};

        public LocationParameters() {
            super(parameterKeys);
        }

    }

    /**
     * Tests converting a JSON object with a complex 3D array.
     * The array contains nested objects (with a fixed structure), strings, and nulls.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testComplexThreeDimensionalArrayWithFixedParameters() throws IOException {
        String json = "{\n" +
                "  \"dataCube\": [\n" +
                "    [\n" +
                "      [\n" +
                "        { \"id\": 101, \"name\": \"Seoul\", \"type\": \"City\" },\n" +
                "        \"DataPoint1\",\n" +
                "        null\n" +
                "      ],\n" +
                "      [\n" +
                "        { \"id\": 202, \"name\": \"Busan\", \"type\": \"City\" }\n" +
                "      ]\n" +
                "    ],\n" +
                "    [\n" +
                "      [\n" +
                "        \"DataPoint2\",\n" +
                "        { \"id\": 303, \"name\": \"Jeju\", \"type\": \"Island\" }\n" +
                "      ]\n" +
                "    ]\n" +
                "  ]\n" +
                "}\n";

        // The parser will create VariableParameters instances for the JSON objects.
        // The test will verify that their content matches the LocationParameters structure.
        Parameters parameters = JsonToParameters.from(json);
        Assert.assertNotNull(parameters);

        // Retrieve the 3D list
        List<List<List<Object>>> dataCube = (List<List<List<Object>>>) parameters.getValueList("dataCube");
        Assert.assertNotNull(dataCube);

        // Assert dimensions
        Assert.assertEquals(2, dataCube.size()); // 1st dimension
        Assert.assertEquals(2, dataCube.get(0).size()); // 2nd dimension in first element
        Assert.assertEquals(1, dataCube.get(1).size()); // 2nd dimension in second element
        Assert.assertEquals(3, dataCube.get(0).get(0).size()); // 3rd dimension (innermost)

        // Assert contents of the first innermost array: [ {location}, "DataPoint1", null ]
        List<Object> innerArray1 = dataCube.get(0).get(0);
        Assert.assertTrue(innerArray1.get(0) instanceof Parameters);
        Parameters location1 = (Parameters) innerArray1.get(0);
        Assert.assertEquals(101, (int)location1.getInt("id"));
        Assert.assertEquals("Seoul", location1.getString("name"));
        Assert.assertEquals("City", location1.getString("type"));

        Assert.assertEquals("DataPoint1", innerArray1.get(1));
        Assert.assertNull(innerArray1.get(2));

        // Assert contents of the second innermost array: [ {location} ]
        List<Object> innerArray2 = dataCube.get(0).get(1);
        Assert.assertEquals(1, innerArray2.size());
        Assert.assertTrue(innerArray2.get(0) instanceof Parameters);
        Parameters location2 = (Parameters) innerArray2.get(0);
        Assert.assertEquals(202, (int)location2.getInt("id"));
        Assert.assertEquals("Busan", location2.getString("name"));
        Assert.assertEquals("City", location2.getString("type"));

        // Assert contents of the third innermost array: [ "DataPoint2", {location} ]
        List<Object> innerArray3 = dataCube.get(1).get(0);
        Assert.assertEquals(2, innerArray3.size());
        Assert.assertEquals("DataPoint2", innerArray3.get(0));
        Assert.assertTrue(innerArray3.get(1) instanceof Parameters);
        Parameters location3 = (Parameters) innerArray3.get(1);
        Assert.assertEquals(303, (int)location3.getInt("id"));
        Assert.assertEquals("Jeju", location3.getString("name"));
        Assert.assertEquals("Island", location3.getString("type"));
    }

    public static class LocationListParameters extends DefaultParameters {

        private static final ParameterKey locations = new ParameterKey("locations", LocationParameters.class, true);

        private static final ParameterKey[] parameterKeys = {locations};

        public LocationListParameters() {
            super(parameterKeys);
        }

        public List<LocationParameters> getLocations() {
            return getParametersList(locations);
        }

    }

    /**
     * Tests that the parser uses the provided schema (typed Parameters)
     * to correctly parse data types, such as forcing a number to be a Long.
     */
    @Test
    public void testTypedParametersInArray() throws IOException {
        String json = "{\n" +
                "  \"locations\": [\n" +
                "    { \"id\": 101, \"name\": \"Seoul\", \"type\": \"City\" },\n" +
                "    { \"id\": 9999999999, \"name\": \"Big City\", \"type\": \"Mega\" }\n" +
                "  ]\n" +
                "}\n";

        // Parse the JSON using LocationListParameters as the schema.
        // The parser will now know that 'id' should be a Long, as defined in LocationParameters.
        LocationListParameters params = JsonToParameters.from(json, LocationListParameters.class);
        Assert.assertNotNull(params);

        List<LocationParameters> locations = params.getLocations();
        Assert.assertNotNull(locations);
        Assert.assertEquals(2, locations.size());

        // Verify the first location
        LocationParameters location1 = locations.get(0);
        Assert.assertNotNull(location1);
        // Now getLong() works without exception, because the parser was guided by the schema.
        Assert.assertEquals(101L, location1.getLong("id").longValue());
        Assert.assertEquals("Seoul", location1.getString("name"));
        Assert.assertEquals("City", location1.getString("type"));

        // Verify the second location with a number that would fit in Long but not Integer
        LocationParameters location2 = locations.get(1);
        Assert.assertNotNull(location2);
        Assert.assertEquals(9999999999L, location2.getLong("id").longValue());
        Assert.assertEquals("Big City", location2.getString("name"));
        Assert.assertEquals("Mega", location2.getString("type"));
    }

}
