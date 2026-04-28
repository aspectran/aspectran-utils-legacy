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

import com.aspectran.utils.StringifyContext;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Test cases for converting Java Objects to APON Parameters.
 *
 * <p>Created: 2019-07-07</p>
 */
public class ObjectToParametersTest {

    /**
     * Tests the conversion of various collection types like Array, List, and Enumeration.
     */
    @Test
    public void testConvertVariousCollectionTypes() {
        List<String> list = Arrays.asList("1", "2", null, "3");
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("array", list.toArray(new String[0]));
        map.put("list", list);
        map.put("enum", Collections.enumeration(list));

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setNullWritable(true);

        Parameters parameters = new ObjectToParameters()
                .apply(stringifyContext)
                .read(map);

        Assert.assertEquals(Arrays.asList("1", "2", null, "3"), parameters.getStringList("array"));
        Assert.assertEquals(Arrays.asList("1", "2", null, "3"), parameters.getStringList("list"));
        Assert.assertEquals(Arrays.asList("1", "2", null, "3"), parameters.getStringList("enum"));
    }

    /**
     * Tests the conversion of a List of Parameters and how the 'nullWritable' option affects the output.
     */
    @Test
    public void testConvertListWithNullWritableOption() {
        // Create a list of Parameters objects, including a null
        List<Parameters> customerList = new ArrayList<Parameters>();
        Parameters p1 = new VariableParameters();
        p1.putValue("id", "guest-1");
        customerList.add(p1);
        customerList.add(null); // Add a null element

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("customers", customerList);

        // Case 1: nullWritable = false (nulls in list should be skipped)
        StringifyContext contextFalse = new StringifyContext();
        contextFalse.setNullWritable(false);
        Parameters paramsFalse = new ObjectToParameters().apply(contextFalse).read(map);
        List<Parameters> resultListFalse = paramsFalse.getParametersList("customers");
        Assert.assertEquals(1, resultListFalse.size());
        Assert.assertEquals("guest-1", resultListFalse.get(0).getString("id"));

        // Case 2: nullWritable = true (nulls in list should be preserved)
        StringifyContext contextTrue = new StringifyContext();
        contextTrue.setNullWritable(true);
        Parameters paramsTrue = new ObjectToParameters().apply(contextTrue).read(map);
        List<Parameters> resultListTrue = paramsTrue.getParametersList("customers");
        Assert.assertEquals(2, resultListTrue.size());
        Assert.assertEquals("guest-1", resultListTrue.get(0).getString("id"));
        Assert.assertNull(resultListTrue.get(1));
    }

    /**
     * Tests the conversion of a Map with a mix of various data types.
     */
    @Test
    public void testConvertMapWithMixedDataTypes() throws ParseException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("intro", "Start Testing Now!");
        map.put("one", 1);
        map.put("aNull", null);
        map.put("date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse("31/12/1998 11:12:13"));
        
        Calendar cal = Calendar.getInstance();
        cal.set(2016, 7, 16, 0, 0, 0); // 2016-08-16 (month is 0-indexed)
        cal.set(Calendar.MILLISECOND, 0);
        map.put("localDate", cal.getTime());

        Calendar cal2 = Calendar.getInstance();
        cal2.set(2016, 2, 4, 10, 15, 30); // 2016-03-04 (month is 0-indexed)
        cal2.set(Calendar.MILLISECOND, 0);
        map.put("localDateTime", cal2.getTime());
        
        map.put("char", 'A');

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        stringifyContext.setNullWritable(true);

        Parameters params = new ObjectToParameters()
                .apply(stringifyContext)
                .read(map);

        Assert.assertEquals("Start Testing Now!", params.getString("intro"));
        Assert.assertEquals(1, (int)params.getInt("one"));
        Assert.assertTrue(params.hasParameter("aNull"));
        Assert.assertNull(params.getString("aNull"));
        Assert.assertEquals("1998-12-31 11:12:13", params.getString("date"));
        Assert.assertEquals("2016-08-16 00:00:00", params.getString("localDate"));
        Assert.assertEquals("2016-03-04 10:15:30", params.getString("localDateTime"));
        Assert.assertEquals("A", params.getString("char"));
    }

    /**
     * Tests the conversion of a nested Map structure.
     */
    @Test
    public void testNestedMapConversion() {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        Map<String, Object> nested = new LinkedHashMap<String, Object>();
        nested.put("key", "value");
        nested.put("number", 123);
        root.put("nestedMap", nested);

        Parameters params = new ObjectToParameters().read(root);

        Parameters nestedParams = params.getParameters("nestedMap");
        Assert.assertNotNull(nestedParams);
        Assert.assertEquals("value", nestedParams.getString("key"));
        Assert.assertEquals(123, (int)nestedParams.getInt("number"));
    }

}
