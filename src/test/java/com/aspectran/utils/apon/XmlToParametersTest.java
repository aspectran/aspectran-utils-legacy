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

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test cases for converting XML to APON Parameters.
 *
 * <p>Created: 2019-07-08</p>
 */
public class XmlToParametersTest {

    /**
     * Tests the conversion of a complex XML structure with nested elements,
     * attributes, and repeated sibling elements.
     */
    @Test
    public void testComplexXmlToParametersConversion() throws IOException {
        String xml = "<container id=\"12\">\n" +
                "  <item1>\n" +
                "    <container id=\"34\">\n" +
                "      <item id=\"56\">a\n" +
                "a\n" +
                "a</item>\n" +
                "      <item id=\"78\">bbb</item>\n" +
                "    </container>\n" +
                "    <container>\n" +
                "      <item>aaa</item>\n" +
                "      <item>bbb</item>\n" +
                "      <item>ccc</item>\n" +
                "    </container>\n" +
                "  </item1>\n" +
                "  <item2>\n" +
                "    xyz\n" +
                "  </item2>\n" +
                "</container>";

        Parameters params = XmlToParameters.from(xml);
        Parameters container = params.getParameters("container");
        assertNotNull(container);
        assertEquals("12", container.getString("id"));

        Parameters item1 = container.getParameters("item1");
        assertNotNull(item1);

        // Test array of containers within item1
        java.util.List<Parameters> containers = item1.getParametersList("container");
        assertEquals(2, containers.size());

        // First container in the array
        Parameters container1 = containers.get(0);
        assertEquals("34", container1.getString("id"));
        java.util.List<Parameters> items1 = container1.getParametersList("item");
        assertEquals(2, items1.size());
        assertEquals("56", items1.get(0).getString("id"));
        assertEquals("a\na\na", items1.get(0).getString("item"));
        assertEquals("78", items1.get(1).getString("id"));
        assertEquals("bbb", items1.get(1).getString("item"));

        // Second container in the array
        Parameters container2 = containers.get(1);
        java.util.List<String> items2 = container2.getStringList("item");
        assertEquals(3, items2.size());
        assertEquals("aaa", items2.get(0));
    }

    /**
     * Tests a simple XML to Parameters conversion.
     */
    @Test
    public void testSimpleXmlConversion() throws IOException {
        String xml = "<root><key>value</key><number>123</number></root>";
        Parameters params = XmlToParameters.from(xml);
        Parameters root = params.getParameters("root");
        assertEquals("value", root.getString("key"));
        assertEquals("123", root.getString("number"));
    }

    /**
     * Tests that sibling elements with the same name are converted to an array.
     */
    @Test
    public void testXmlWithSiblingElements() throws IOException {
        String xml = "<root><item>a</item><item>b</item><item>c</item></root>";
        Parameters params = XmlToParameters.from(xml);
        java.util.List<String> items = params.getParameters("root").getStringList("item");
        assertEquals(3, items.size());
        assertEquals(java.util.Arrays.asList("a", "b", "c"), items);
    }

    /**
     * Tests that CDATA sections are correctly parsed as text content.
     */
    @Test
    public void testXmlWithCDataSection() throws IOException {
        String xml = "<root><![CDATA[This is <some> text & characters.]]></root>";
        Parameters params = XmlToParameters.from(xml);
        assertEquals("This is <some> text & characters.", params.getString("root"));
    }

    /**
     * Tests that invalid XML input throws an exception.
     */
    @Test(expected = IOException.class)
    public void testInvalidXmlInput() throws IOException {
        String malformedXml = "<root><item>a</item><item>b</item</root"; // Missing closing tag
        XmlToParameters.from(malformedXml);
    }

}
