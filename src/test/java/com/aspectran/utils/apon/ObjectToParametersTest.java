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
import com.aspectran.utils.apon.test.Customer;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * <p>Created: 2019-07-07</p>
 */
public class ObjectToParametersTest {

    @Test
    public void testConvert1() throws IOException {
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add(null);
        list.add("3");

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("array", list.toArray(new String[0]));
        map.put("list", list);
        map.put("enum", Collections.enumeration(list));

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        stringifyContext.setNullWritable(true);

        Parameters parameters = new ObjectToParameters()
            .apply(stringifyContext)
            .read(map);

        String expected = "array: [\n" +
            "  1\n" +
            "  2\n" +
            "  null\n" +
            "  3\n" +
            "]\n" +
            "list: [\n" +
            "  1\n" +
            "  2\n" +
            "  null\n" +
            "  3\n" +
            "]\n" +
            "enum: [\n" +
            "  1\n" +
            "  2\n" +
            "  null\n" +
            "  3\n" +
            "]\n";

        expected = expected.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String converted = new AponWriter()
            .nullWritable(true)
            .write(parameters)
            .toString();
        assertEquals(expected, converted);
    }

    @Test
    public void testConvert2() throws IOException {
        List<Customer> customerList = new ArrayList<Customer>();
        for (int i = 1; i <= 2; i++) {
            Customer customer = new Customer();
            customer.putValue(Customer.id, "guest-" + i);
            customerList.add(customer);
        }
        customerList.add(null);

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("customers", customerList);

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        stringifyContext.setNullWritable(false);

        Parameters parameters = new ObjectToParameters()
            .apply(stringifyContext)
            .read(map);

        String expected = "customers: [\n" +
            "  {\n" +
            "    id: guest-1\n" +
            "  }\n" +
            "  {\n" +
            "    id: guest-2\n" +
            "  }\n" +
            "]\n";

        expected = expected.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String converted = new AponWriter()
            .nullWritable(false)
            .write(parameters)
            .toString();
        assertEquals(expected, converted);
        System.out.println(parameters);
    }

    @Test
    public void testConvert3() throws IOException, ParseException {
        String expected = "intro: Start Testing Now!\n" +
            "one: 1\n" +
            "two: 2\n" +
            "three: 3\n" +
            "null: null\n" +
            "nullArray: [\n" +
            "  null\n" +
            "  null\n" +
            "  null\n" +
            "]\n" +
            "date: 1998-12-31 11:12:13\n" +
            "char: A\n" +
            "customers: [\n" +
            "  {\n" +
            "    id: guest-1\n" +
            "    name: Guest1\n" +
            "    age: 21\n" +
            "    episode: (\n" +
            "      |His individual skills are outstanding.\n" +
            "      |I don't know as how he is handsome\n" +
            "    )\n" +
            "    approved: true\n" +
            "  }\n" +
            "  {\n" +
            "    id: guest-2\n" +
            "    name: Guest2\n" +
            "    age: 22\n" +
            "    episode: (\n" +
            "      |His individual skills are outstanding.\n" +
            "      |I don't know as how he is handsome\n" +
            "    )\n" +
            "    approved: true\n" +
            "  }\n" +
            "]\n";
        expected = expected.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String converted = convert();
        assertEquals(expected, converted);
    }

    static String convert() throws IOException, ParseException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("intro", "Start Testing Now!");
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("null", null);
        map.put("nullArray", new String[] {null, null, null});
        map.put("date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse("31/12/1998 11:12:13"));
        map.put("char", 'A');

        List<Customer> customerList = new ArrayList<Customer>();
        for (int i = 1; i <= 2; i++) {
            Customer customer = new Customer();
            customer.putValue(Customer.id, "guest-" + i);
            customer.putValue(Customer.name, "Guest" + i);
            customer.putValue(Customer.age, 20 + i);
            customer.putValue(Customer.episode, "His individual skills are outstanding.\nI don't know as how he is handsome");
            customer.putValue(Customer.approved, true);
            customerList.add(customer);
        }

        map.put("customers", customerList);

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        stringifyContext.setNullWritable(true);

        Parameters parameters = new ObjectToParameters()
                .apply(stringifyContext)
                .read(map);

        return new AponWriter()
                .nullWritable(true)
                .write(parameters)
                .toString();
    }

}
