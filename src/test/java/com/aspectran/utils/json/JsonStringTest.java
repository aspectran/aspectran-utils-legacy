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

import com.aspectran.utils.apon.JsonToParameters;
import com.aspectran.utils.apon.Parameters;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * Test cases for {@link JsonString}.
 *
 * <p>Created: 2026. 01. 19.</p>
 */
public class JsonStringTest {

    @Test
    public void testJsonString() {
        String rawJson = "{\"key\":\"value\"}";
        JsonString jsonString = new JsonString(rawJson);
        Assert.assertEquals(rawJson, jsonString.toString());
    }

    @Test
    public void testJsonStringWithNull() {
        JsonString jsonString = new JsonString(null);
        Assert.assertNull(jsonString.toString());
    }

    @Test
    public void testWithJsonWriter() throws IOException {
        String rawJson = "{\"key\":\"value\"}";
        JsonString jsonString = new JsonString(rawJson);

        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        writer.setPrettyPrint(false);
        writer.beginObject()
                .name("data").value(jsonString)
                .name("desc").value(rawJson)
                .endObject();

        String expected = "{\"data\":{\"key\":\"value\"},\"desc\":\"{\\\"key\\\":\\\"value\\\"}\"}";
        Assert.assertEquals(expected, out.toString());
    }

    @Test
    public void testWithJsonWriterNullContent() throws IOException {
        JsonString jsonString = new JsonString(null);

        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        writer.setPrettyPrint(false);
        writer.beginObject()
                .name("data").value(jsonString)
                .endObject();

        String expected = "{\"data\":null}";
        Assert.assertEquals(expected, out.toString());
    }

    @Test
    public void testSingleElementArrayConversion() throws IOException {
        String json = "{\n" +
                "    \"stringList\": [\"item1\"],\n" +
                "    \"objList\": [{\"id\": 1}]\n" +
                "}\n";

        JsonWriter jw = new JsonWriter(new StringWriter());
        jw.beginObject()
                .name("param1").value("value1")
                .name("param2").value(new JsonString(json))
        .endObject();
        String writtenJson = jw.toString();

        Parameters params = JsonToParameters.from(writtenJson);
        Parameters param2 = params.getParameters("param2");

        // Check string list
        Assert.assertTrue(param2.hasParameter("stringList"));
        // This is the critical check: verify it's a List, not a single String
        Object stringVal = param2.getValue("stringList");
        Assert.assertTrue(stringVal instanceof List);
        List<?> stringList = (List<?>)stringVal;
        Assert.assertEquals(1, stringList.size());
        Assert.assertEquals("item1", stringList.get(0));

        // Check object list
        Assert.assertTrue(param2.hasParameter("objList"));
        // This is the critical check: verify it's a List, not a single Parameters object
        Object objVal = param2.getValue("objList");
        Assert.assertTrue(objVal instanceof List);
        List<?> objList = (List<?>)objVal;
        Assert.assertEquals(1, objList.size());
        Assert.assertTrue(objList.get(0) instanceof Parameters);
        Assert.assertEquals(1, ((Parameters)objList.get(0)).getInt("id").intValue());
    }

}
