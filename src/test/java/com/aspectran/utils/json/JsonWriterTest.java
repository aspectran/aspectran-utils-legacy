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
package com.aspectran.utils.json;

import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;
import com.aspectran.utils.apon.test.Customer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Test cases for JsonWriter.
 */
public class JsonWriterTest {

    @Test
    public void testWriteComplexMap() throws IOException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("intro", "Start Testing Now!");
        map.put("null0", null);
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("nullArray", new String[] {null, null});

        List<Customer> customerList = new ArrayList<Customer>();
        for (int i = 1; i <= 2; i++) {
            Customer customer = new Customer();
            customer.putValue(Customer.id, "guest-" + i);
            customer.putValue(Customer.name, "Guest" + i);
            customer.putValue(Customer.age, 20 + i);
            customer.putValue(Customer.approved, true);
            customer.putValue(Customer.episode, null);
            customerList.add(customer);
        }
        map.put("customers", customerList);

        Map<String, Object> emptyMap = new LinkedHashMap<String, Object>();
        map.put("emptyMap", emptyMap);

        map.put("null", null);
        map.put("null2", null);

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

        String result = new JsonWriter(new StringWriter())
                .apply(stringifyContext)
                .nullWritable(false)
                .value(map)
                .toString();

        String expected = "{\n" +
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
                "}";

        Assert.assertEquals(expected.trim(), result.trim());
    }

    @Test
    public void testWriteAponParameters() throws IOException {
        Parameters parameters = new VariableParameters();
        parameters.putValue("item1", 1);
        parameters.putValue("item2", 2);
        Parameters parameters2 = new VariableParameters();
        parameters2.putValue("item11", 11);
        parameters2.putValue("item22", 22);
        parameters.putValue("item3", parameters2);
        parameters.putValue("null", null);

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

        String result = new JsonWriter(new StringWriter())
                .apply(stringifyContext)
                .nullWritable(false)
                .value(parameters)
                .toString();

        String expected = "{\n" +
                "  \"item1\": 1,\n" +
                "  \"item2\": 2,\n" +
                "  \"item3\": {\n" +
                "    \"item11\": 11,\n" +
                "    \"item22\": 22\n" +
                "  }\n" +
                "}";

        Assert.assertEquals(expected.trim(), result.trim());
    }

    @Test
    public void testWriteDateTimeTypes() throws IOException, ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2019-11-19");
        Date dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-11-19 11:15:30");

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("date", date);
        map.put("dateTime", dateTime);

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

        String result = new JsonWriter(new StringWriter())
                .apply(stringifyContext)
                .value(map)
                .toString();

        String expected = "{\n" +
                "  \"date\": \"2019-11-19 00:00:00\",\n" +
                "  \"dateTime\": \"2019-11-19 11:15:30\"\n" +
                "}";

        Assert.assertEquals(expected.trim().replace("\r\n", "\n"), result.trim());
    }

    @Test
    public void testWriteRawJson() throws IOException {
        JsonWriter writer2 = new JsonWriter(new StringWriter());
        writer2.beginObject();
        writer2.writeName("key1");
        writer2.writeValue("value");
        writer2.writeName("key2");
        writer2.writeJson("\"1234\"");
        writer2.endObject();

        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.writeName("key1");
        writer.writeValue("value");
        writer.writeName("key2");
        writer.writeJson("\"1234\"");
        writer.writeName("json");
        writer.writeJson(writer2.toString());
        writer.writeName("array");
        writer.writeJson("[1, 2, 3]");
        writer.endObject();

        String expected = "{\n" +
                "  \"key1\": \"value\",\n" +
                "  \"key2\": \"1234\",\n" +
                "  \"json\": {\n" +
                "    \"key1\": \"value\",\n" +
                "    \"key2\": \"1234\"\n" +
                "  },\n" +
                "  \"array\": [1, 2, 3]\n" +
                "}";

        Assert.assertEquals(expected.replace("\r\n", "\n"), writer.toString().trim());
    }

    @Test
    public void testCircularReferenceInMap() {
        Map<String, Object> map1 = new HashMap<String, Object>();
        Map<String, Object> map2 = new HashMap<String, Object>();

        map1.put("map0", "map0");
        map1.put("map1-2", map2);
        map2.put("map2-1", map1);

        try {
            JsonWriter writer = new JsonWriter(new StringWriter());
            writer.writeValue(map1);
            Assert.fail("Should have thrown IOException due to circular reference");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().startsWith("JSON Serialization Failure: Circular reference detected for member 'map2-1'"));
        }
    }

    @Test
    public void testCircularReferenceInList() {
        Map<String, Object> map1 = new HashMap<String, Object>();
        List<Object> list1 = new ArrayList<Object>();

        map1.put("list1", list1);
        list1.add(map1);

        try {
            JsonWriter writer = new JsonWriter(new StringWriter());
            writer.writeValue(map1);
            Assert.fail("Should have thrown IOException due to circular reference");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().startsWith("JSON Serialization Failure: Circular reference detected for a member"));
        }
    }

    @Test
    public void testCompactOutput() throws IOException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("name", "John Doe");
        map.put("age", 30);
        StringWriter stringWriter = new StringWriter();
        String result = new JsonWriter(stringWriter).prettyPrint(false).value(map).toString();
        Assert.assertEquals("{\"name\":\"John Doe\",\"age\":30}", result);
    }

    @Test
    public void testWriteSimpleValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        String result = new JsonWriter(stringWriter).value("hello").toString();
        Assert.assertEquals("\"hello\"", result);

        stringWriter = new StringWriter();
        result = new JsonWriter(stringWriter).value(123).toString();
        Assert.assertEquals("123", result);

        stringWriter = new StringWriter();
        result = new JsonWriter(stringWriter).value(true).toString();
        Assert.assertEquals("true", result);

        stringWriter = new StringWriter();
        result = new JsonWriter(stringWriter).nullWritable(true).value(null).toString();
        Assert.assertEquals("null", result);
    }

    @Test
    public void testCustomIndent() throws IOException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("key", "value");
        StringWriter stringWriter = new StringWriter();
        String result = new JsonWriter(stringWriter).indentString("    ").value(map).toString();
        String expected = "{\n    \"key\": \"value\"\n}";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testWriteEmptyJsonString() throws IOException {
        JsonString emptyJsonString = new JsonString("");
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("empty");
        writer.value(emptyJsonString);
        writer.endObject();
        Assert.assertEquals("{\n  \"empty\": null\n}", writer.toString().trim());
    }

    @Test
    public void testCustomSerializerForBigDecimal() throws IOException {
        class Money {
            private final java.math.BigDecimal amount;
            public Money(String amount) {
                this.amount = new java.math.BigDecimal(amount);
            }
            public java.math.BigDecimal getAmount() {
                return amount;
            }
        }

        Money money = new Money("123.456");

        JsonSerializer<java.math.BigDecimal> bigDecimalSerializer = new JsonSerializer<java.math.BigDecimal>() {
            @Override
            public void serialize(java.math.BigDecimal value, JsonWriter writer) throws IOException {
                writer.value(value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
            }
        };

        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.registerSerializer(java.math.BigDecimal.class, bigDecimalSerializer);
        writer.beginObject();
        writer.name("amount");
        writer.value(money.getAmount());
        writer.endObject();

        Assert.assertEquals("{\n  \"amount\": \"123.46\"\n}", writer.toString().trim());
    }

    @Test
    public void testCustomSerializerForUserObject() throws IOException {
        class User {
            private final int id;
            private final String name;
            public User(int id, String name) {
                this.id = id;
                this.name = name;
            }
            public int getId() {
                return id;
            }
            public String getName() {
                return name;
            }
        }

        User user = new User(1, "John Doe");

        JsonSerializer<User> userSerializer = new JsonSerializer<User>() {
            @Override
            public void serialize(User value, JsonWriter writer) throws IOException {
                writer.beginObject();
                writer.name("id").value(value.getId());
                writer.name("name").value(value.getName());
                writer.endObject();
            }
        };

        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.registerSerializer(User.class, userSerializer);
        writer.value(user);

        Assert.assertEquals("{\n  \"id\": 1,\n  \"name\": \"John Doe\"\n}", writer.toString().trim());
    }

}
