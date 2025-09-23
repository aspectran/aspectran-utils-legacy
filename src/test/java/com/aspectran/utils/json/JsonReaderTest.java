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

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.apon.AponFormat;
import com.aspectran.utils.apon.AponWriter;
import com.aspectran.utils.apon.Parameter;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.ValueType;
import com.aspectran.utils.apon.VariableParameters;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * <p>Created: 2020/05/30</p>
 */
public class JsonReaderTest {

    @Test
    public void test1() throws IOException {
        JsonReader reader = new JsonReader("{\"name\":\"she's\"}");
        Parameters parameters = new VariableParameters();
        convertToParameters(reader, parameters, null);
        String expected = "name: \"she's\"";
        String actual = parameters.toString().trim();
        assertEquals(expected, actual);
    }

    @Test
    public void test2() throws IOException {
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
            "}";

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
            "emptyMap: {\n" +
            "}\n";

        expected = expected.replace("\n", AponFormat.SYSTEM_NEW_LINE);

        JsonReader reader = new JsonReader(json);
        Parameters parameters = new VariableParameters();
        convertToParameters(reader, parameters, null);

        String actual = new AponWriter()
            .nullWritable(true)
            .write(parameters)
            .toString();

        assertEquals(expected, actual);
    }

    static void convertToParameters(@NonNull JsonReader reader, Parameters container, String name) throws IOException {
        switch (reader.peek()) {
            case BEGIN_OBJECT:
                reader.beginObject();
                if (name != null) {
                    container = container.newParameters(name);
                }
                while (reader.hasNext()) {
                    convertToParameters(reader, container, reader.nextName());
                }
                reader.endObject();
                return;
            case BEGIN_ARRAY:
                reader.beginArray();
                while (reader.hasNext()) {
                    convertToParameters(reader, container, name);
                }
                reader.endArray();
                return;
            case STRING:
                container.putValue(name, reader.nextString());
                return;
            case BOOLEAN:
                container.putValue(name, reader.nextBoolean());
                return;
            case NUMBER:
                try {
                    container.putValue(name, reader.nextInt());
                } catch (NumberFormatException e0) {
                    try {
                        container.putValue(name, reader.nextLong());
                    } catch (NumberFormatException e1) {
                        container.putValue(name, reader.nextDouble());
                    }
                }
                return;
            case NULL:
                reader.nextNull();
                Parameter parameter = container.getParameter(name);
                if (parameter == null || parameter.getValueType() != ValueType.PARAMETERS) {
                    container.putValue(name, null);
                }
                return;
            default:
                throw new IllegalStateException();
        }
    }

}