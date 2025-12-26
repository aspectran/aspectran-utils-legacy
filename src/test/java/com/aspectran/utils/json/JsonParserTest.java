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

import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for JsonParser.
 *
 * <p>Created: 2025-10-15</p>
 */
@SuppressWarnings("unchecked")
public class JsonParserTest {

    @Test
    public void testParseSimpleObject() throws IOException {
        String json = "{\"name\":\"John Doe\",\"age\":30,\"isStudent\":false}";
        Object result = JsonParser.parse(json);
        assertTrue(result instanceof Map);
        Map<String, Object> map = (Map<String, Object>)result;
        assertEquals("John Doe", map.get("name"));
        assertEquals(30, map.get("age"));
        assertEquals(false, map.get("isStudent"));
    }

    @Test
    public void testParseSimpleArray() throws IOException {
        String json = "[1, \"hello\", true, null]";
        Object result = JsonParser.parse(json);
        assertTrue(result instanceof List);
        List<Object> list = (List<Object>)result;
        assertEquals(1, list.get(0));
        assertEquals("hello", list.get(1));
        assertEquals(true, list.get(2));
        assertNull(list.get(3));
    }

    @Test
    public void testParseNestedObject() throws IOException {
        String json = "{\"person\":{\"name\":\"Jane Doe\",\"age\":25},\"city\":\"New York\"}";
        Object result = JsonParser.parse(json);
        assertTrue(result instanceof Map);
        Map<String, Object> map = (Map<String, Object>)result;
        assertEquals("New York", map.get("city"));
        assertTrue(map.get("person") instanceof Map);
        Map<String, Object> personMap = (Map<String, Object>)map.get("person");
        assertEquals("Jane Doe", personMap.get("name"));
        assertEquals(25, personMap.get("age"));
    }

    @Test
    public void testParseNestedArray() throws IOException {
        String json = "{\"data\":[{\"id\":1},{\"id\":2}]}";
        Object result = JsonParser.parse(json);
        assertTrue(result instanceof Map);
        Map<String, Object> map = (Map<String, Object>)result;
        assertTrue(map.get("data") instanceof List);
        List<Object> list = (List<Object>)map.get("data");
        assertEquals(2, list.size());
        assertTrue(list.get(0) instanceof Map);
        Map<String, Object> item1 = (Map<String, Object>)list.get(0);
        assertEquals(1, item1.get("id"));
    }

    @Test
    public void testParseEmptyObject() throws IOException {
        String json = "{}";
        Object result = JsonParser.parse(json);
        assertTrue(result instanceof Map);
        assertTrue(((Map<?, ?>)result).isEmpty());
    }

    @Test
    public void testParseEmptyArray() throws IOException {
        String json = "[]";
        Object result = JsonParser.parse(json);
        assertTrue(result instanceof List);
        assertTrue(((List<?>)result).isEmpty());
    }

    @Test
    public void testParseNullInput() throws IOException {
        assertNull(JsonParser.parse(null));
    }

    @Test
    public void testParseNumberTypes() throws IOException {
        String json = "{\"int\":123,\"long\":1234567890123,\"double\":123.45}";
        Object result = JsonParser.parse(json);
        assertTrue(result instanceof Map);
        Map<String, Object> map = (Map<String, Object>)result;
        assertEquals(123, map.get("int"));
        assertEquals(1234567890123L, map.get("long"));
        assertEquals(123.45, map.get("double"));
    }

    @Test
    public void testWhitespaceHandling() throws IOException {
        String json = "  { \n \"key\" \t : \r \"value\" \n }  ";
        Object result = JsonParser.parse(json);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>)result;
        assertEquals("value", map.get("key"));
        assertEquals(1, map.size());
    }

    @Test
    public void testComplexStructure() throws IOException {
        String json = "{\n" +
                "  \"id\": \"001\",\n" +
                "  \"type\": \"donut\",\n" +
                "  \"name\": \"Cake\",\n" +
                "  \"ppu\": 0.55,\n" +
                "  \"batters\": {\n" +
                "    \"batter\": [\n" +
                "      { \"id\": \"1001\", \"type\": \"Regular\" },\n" +
                "      { \"id\": \"1002\", \"type\": \"Chocolate\" },\n" +
                "      { \"id\": \"1003\", \"type\": \"Blueberry\" },\n" +
                "      { \"id\": \"1004\", \"type\": \"Devil's Food\" }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"topping\": [\n" +
                "    { \"id\": \"5001\", \"type\": \"None\" },\n" +
                "    { \"id\": \"5002\", \"type\": \"Glazed\" },\n" +
                "    { \"id\": \"5005\", \"type\": \"Sugar\" },\n" +
                "    { \"id\": \"5007\", \"type\": \"Powdered Sugar\" },\n" +
                "    { \"id\" : \"5006\", \"type\": \"Chocolate with Sprinkles\" },\n" +
                "    { \"id\": \"5003\", \"type\": \"Chocolate\" },\n" +
                "    { \"id\": \"5004\", \"type\": \"Maple\" }\n" +
                "  ]\n" +
                "}";
        Object result = JsonParser.parse(json);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("001", map.get("id"));
        assertEquals("donut", map.get("type"));
        assertTrue(map.get("batters") instanceof Map);
        Map<?, ?> batters = (Map<?, ?>) map.get("batters");
        assertTrue(batters.get("batter") instanceof List);
        List<?> batterList = (List<?>) batters.get("batter");
        assertEquals(4, batterList.size());
        assertTrue(map.get("topping") instanceof List);
        List<?> toppingList = (List<?>) map.get("topping");
        assertEquals(7, toppingList.size());
    }

    @Test(expected = MalformedJsonException.class)
    public void testMalformedJson() throws IOException {
        String json = "{\"name\":\"John Doe\",,}";
        JsonParser.parse(json);
    }

    @Test(expected = EOFException.class)
    public void testIncompleteJson() throws IOException {
        String json = "{\"name\":\"John Doe\"";
        JsonParser.parse(json);
    }

}
