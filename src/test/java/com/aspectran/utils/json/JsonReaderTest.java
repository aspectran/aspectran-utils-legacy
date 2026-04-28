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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Test cases for JsonReader.
 *
 * <p>Created: 2020/05/30</p>
 */
public class JsonReaderTest {

    @Test
    public void testReadSimpleObject() throws IOException {
        String json = "{\"name\":\"John Doe\",\"age\":30,\"isStudent\":false}";
        JsonReader reader = new JsonReader(json);
        Assert.assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
        reader.beginObject();
        Assert.assertEquals("name", reader.nextName());
        Assert.assertEquals("John Doe", reader.nextString());
        Assert.assertEquals("age", reader.nextName());
        Assert.assertEquals(30, reader.nextInt());
        Assert.assertEquals("isStudent", reader.nextName());
        Assert.assertFalse(reader.nextBoolean());
        reader.endObject();
        Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testReadSimpleArray() throws IOException {
        String json = "[1, \"hello\", true, null]";
        JsonReader reader = new JsonReader(json);
        Assert.assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
        reader.beginArray();
        Assert.assertEquals(1, reader.nextInt());
        Assert.assertEquals("hello", reader.nextString());
        Assert.assertTrue(reader.nextBoolean());
        reader.nextNull();
        reader.endArray();
        Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testReadNested() throws IOException {
        String json = "{\"data\":[{\"id\":1}]}";
        JsonReader reader = new JsonReader(json);
        reader.beginObject();
        Assert.assertEquals("data", reader.nextName());
        reader.beginArray();
        reader.beginObject();
        Assert.assertEquals("id", reader.nextName());
        Assert.assertEquals(1, reader.nextInt());
        reader.endObject();
        reader.endArray();
        reader.endObject();
        Assert.assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(expected = IOException.class)
    public void testUnclosedObject() throws IOException {
        String json = "{\"name\":\"John Doe\"";
        JsonReader reader = new JsonReader(json);
        reader.beginObject();
        reader.nextName();
        reader.nextString();
        reader.endObject();
    }

    @Test(expected = MalformedJsonException.class)
    public void testMalformedJson() throws IOException {
        String json = "{key: 'value'}"; // Unquoted key
        JsonReader reader = new JsonReader(json);
        reader.beginObject();
        reader.nextName(); // This should fail
    }

}
