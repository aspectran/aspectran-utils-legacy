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
package com.aspectran.utils;

import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import static org.junit.Assert.*;

public class ToStringBuilderTest {

    @Test
    public void testMap() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        assertEquals("{key1=value1, key2=value2}", ToStringBuilder.toString(map));
        assertEquals("Map {key1=value1, key2=value2}", ToStringBuilder.toString("Map", map));
    }

    @Test
    public void testCollection() {
        List<String> list = new ArrayList<String>();
        list.add("value1");
        list.add("value2");

        assertEquals("[value1, value2]", ToStringBuilder.toString(list));
        assertEquals("List [value1, value2]", ToStringBuilder.toString("List", list));
    }

    @Test
    public void testParameters() {
        Parameters parameters = new VariableParameters();
        parameters.putValue("key1", "value1");
        parameters.putValue("key2", "value2");

        assertEquals("{key1=value1, key2=value2}", ToStringBuilder.toString(parameters));
        assertEquals("Parameters {key1=value1, key2=value2}", ToStringBuilder.toString("Parameters", parameters));
    }

    @Test
    public void testToStringBuilder() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        String result = ToStringBuilder.toString(map);

        assertEquals("{key1=value1, key2=value2}", result);
    }

    @Test
    public void testAppend() {
        ToStringBuilder tsb = new ToStringBuilder("Named");
        tsb.append("key1", "value1");
        tsb.append("key2", "value2");

        assertEquals("Named {key1=value1, key2=value2}", tsb.toString());
    }

    @Test
    public void testAppend2() {
        ToStringBuilder tsb = new ToStringBuilder("Named");
        tsb.append("key1", "value1");
        tsb.append("key2", "value2");
        assertEquals("Named {key1=value1, key2=value2}", tsb.toString());
        tsb.append("key3", "value3");
        assertEquals("Named {key1=value1, key2=value2, key3=value3}", tsb.toString());
    }

    @Test
    public void testCircularReference() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("key1", "value1");
        map.put("key2", map);

        try {
            ToStringBuilder.toString(map);
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Serialization Failure"));
        }
    }

    @Test
    public void testDateFormat() throws ParseException {
        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

        SimpleDateFormat formatter = new SimpleDateFormat(stringifyContext.getDateTimeFormat(), Locale.ENGLISH);
        Date date1 = formatter.parse("2024-12-01 10:39:24");

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("key1", date1);

        String result = ToStringBuilder.toString("Variable", map, stringifyContext);
        assertEquals("Variable {key1=2024-12-01 10:39:24}", result);
    }

    @Test
    public void testVariable() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        Parameters parameters = new VariableParameters();
        parameters.putValue("key1", "value1");
        parameters.putValue("key2", "value2");
        map.put("parameters", parameters);

        String result = ToStringBuilder.toString("Variable", map);
        assertEquals("Variable {key1=value1, key2=value2, parameters={key1=value1, key2=value2}}", result);
    }

    @Test
    public void testAppendSize() {
        Vector<String> list = new Vector<String>();
        list.add("value1");
        list.add("value2");
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        Iterator<String> iterator = list.iterator();
        Enumeration<String> enumeration = list.elements();

        ToStringBuilder tsb = new ToStringBuilder("Size");
        tsb.appendSize("size1", list);
        tsb.appendSize("size2", map);
        tsb.appendSize("size2", iterator);
        tsb.appendSize("size2", enumeration);

        String actual = tsb.toString();
        String expected = "Size {size1=2, size2=2, size2=2, size2=2}";

        assertEquals(expected, actual);
    }

}