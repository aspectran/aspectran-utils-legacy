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

import com.aspectran.utils.apon.test.XSSPatternItem;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Test cases for ArrayParameters.
 *
 * <p>Created: 2019-06-28</p>
 */
public class ArrayParametersTest {

    /**
     * Tests that parsing an APON string representing an array of objects
     * via the constructor and via AponReader yields the same result.
     */
    @Test
    public void testParsingAponArrayFromText() throws AponParseException {
        String apon = "[\n" +
                "    {\n" +
                "      param1: 111\n" +
                "      param2: 222\n" +
                "    }\n" +
                "    {\n" +
                "      param3: 333\n" +
                "      param4: 444\n" +
                "    }\n" +
                "]\n";

        ArrayParameters fromConstructor = new ArrayParameters(apon);
        ArrayParameters fromReader = new AponReader(apon).read(new ArrayParameters());

        Assert.assertEquals(fromConstructor.toString(), fromReader.toString());
        Assert.assertEquals(2, fromConstructor.getParametersList().size());
        Assert.assertEquals(111, (int)fromConstructor.getParametersList().get(0).getInt("param1"));
        Assert.assertEquals(444, (int)fromConstructor.getParametersList().get(1).getInt("param4"));
    }

    /**
     * Tests the programmatic creation of an ArrayParameters object and verifies its string output.
     */
    @Test
    public void testProgrammaticCreation() {
        Parameters p1 = new VariableParameters();
        p1.putValue("param1", 111);
        p1.putValue("param2", 222);

        Parameters p2 = new VariableParameters();
        p2.putValue("param3", 333);
        p2.putValue("param4", 444);

        ArrayParameters arrayParameters = new ArrayParameters();
        arrayParameters.addValue(p1);
        arrayParameters.addValue(p2);

        String expected = "[\n" +
                "  {\n" +
                "    param1: 111\n" +
                "    param2: 222\n" +
                "  }\n" +
                "  {\n" +
                "    param3: 333\n" +
                "    param4: 444\n" +
                "  }\n" +
                "]\n";
        // Normalize line endings for comparison
        String actual = arrayParameters.toString().trim().replace("\r\n", "\n");
        String normalizedExpected = expected.trim().replace("\r\n", "\n");

        Assert.assertEquals(normalizedExpected, actual);
    }

    /**
     * Tests adding, accessing, and checking the size of elements in ArrayParameters.
     */
    @Test
    public void testAddingAndAccessingElements() {
        ArrayParameters arrayParameters = new ArrayParameters();
        Assert.assertFalse(arrayParameters.isEmpty());

        Parameters p1 = new VariableParameters();
        p1.putValue("id", 1);
        arrayParameters.addValue(p1);

        Assert.assertEquals(1, arrayParameters.getParametersList().size());
        Assert.assertEquals(1, (int)arrayParameters.getParametersList().get(0).getInt("id"));

        Parameters p2 = new VariableParameters();
        p2.putValue("id", 2);
        arrayParameters.addValue(p2);

        Assert.assertEquals(2, arrayParameters.getParametersList().size());
        Assert.assertEquals(2, (int)arrayParameters.getParametersList().get(1).getInt("id"));
    }

    /**
     * Tests the behavior of an empty ArrayParameters object.
     */
    @Test
    public void testEmptyArray() throws AponParseException {
        ArrayParameters fromEmptyString = new ArrayParameters("");
        Assert.assertNotNull(fromEmptyString.iterator());
        Assert.assertFalse(fromEmptyString.iterator().hasNext());

        ArrayParameters fromWhitespace = new ArrayParameters("  \n\t  ");
        Assert.assertNotNull(fromWhitespace.iterator());
        Assert.assertFalse(fromWhitespace.iterator().hasNext());

        ArrayParameters programmatically = new ArrayParameters();
        Assert.assertNotNull(programmatically.iterator());
        Assert.assertEquals("[]", programmatically.toString().trim());
    }

    @Test
    public void testGettingTypedArrays() {
        // Test with Strings
        ArrayParameters stringParams = new ArrayParameters();
        stringParams.addValue("apple");
        stringParams.addValue("banana");
        stringParams.addValue("cherry");

        String[] stringArray = {"apple", "banana", "cherry"};
        List<String> stringList = Arrays.asList("apple", "banana", "cherry");
        Assert.assertArrayEquals(stringArray, stringParams.getStringArray());
        Assert.assertEquals(stringList, stringParams.getStringList());

        // Test with Integers
        ArrayParameters intParams = new ArrayParameters();
        intParams.addValue(10);
        intParams.addValue(20);
        intParams.addValue(30);

        Integer[] intArray = {10, 20, 30};
        List<Integer> intList = Arrays.asList(10, 20, 30);
        Assert.assertArrayEquals(intArray, intParams.getIntArray());
        Assert.assertEquals(intList, intParams.getIntList());

        // Test with Longs
        ArrayParameters longParams = new ArrayParameters();
        longParams.addValue(100L);
        longParams.addValue(200L);
        longParams.addValue(300L);

        Long[] longArray = {100L, 200L, 300L};
        List<Long> longList = Arrays.asList(100L, 200L, 300L);
        Assert.assertArrayEquals(longArray, longParams.getLongArray());
        Assert.assertEquals(longList, longParams.getLongList());

        // Test with Doubles
        ArrayParameters doubleParams = new ArrayParameters();
        doubleParams.addValue(10.1);
        doubleParams.addValue(20.2);
        doubleParams.addValue(30.3);

        Double[] doubleArray = {10.1, 20.2, 30.3};
        List<Double> doubleList = Arrays.asList(10.1, 20.2, 30.3);
        Assert.assertArrayEquals(doubleArray, doubleParams.getDoubleArray());
        Assert.assertEquals(doubleList, doubleParams.getDoubleList());

        // Test with Booleans
        ArrayParameters boolParams = new ArrayParameters();
        boolParams.addValue(true);
        boolParams.addValue(false);
        boolParams.addValue(true);

        Boolean[] boolArray = {true, false, true};
        List<Boolean> boolList = Arrays.asList(true, false, true);
        Assert.assertArrayEquals(boolArray, boolParams.getBooleanArray());
        Assert.assertEquals(boolList, boolParams.getBooleanList());
    }

    @Test
    public void testMixedTypeArray() {
        ArrayParameters mixedParams = new ArrayParameters();
        mixedParams.addValue("text");
        mixedParams.addValue(123);
        mixedParams.addValue(true);
        Parameters p = new VariableParameters();
        p.putValue("p1", "v1");
        mixedParams.addValue(p);

        List<?> valueList = mixedParams.getValueList();
        Assert.assertEquals(4, valueList.size());
        Assert.assertEquals("text", valueList.get(0));
        Assert.assertEquals(123, valueList.get(1));
        Assert.assertEquals(true, valueList.get(2));
        Assert.assertEquals(p, valueList.get(3));

        // Test conversion to string array
        String[] stringArray = {"text", "123", "true", p.toString()};
        Assert.assertArrayEquals(stringArray, mixedParams.getStringArray());
    }

    @Test
    public void testNestedArrayParametersInVariableParameters() throws IOException {
        VariableParameters mainParams = new VariableParameters();

        // 1. Add an ArrayParameters of Strings
        ArrayParameters stringArrayParams = new ArrayParameters();
        stringArrayParams.addValue("value1");
        stringArrayParams.addValue("value2");
        mainParams.putValue("stringList", stringArrayParams);

        // 2. Add an ArrayParameters of Integers
        ArrayParameters intArrayParams = new ArrayParameters();
        intArrayParams.addValue(100);
        intArrayParams.addValue(200);
        mainParams.putValue("intList", intArrayParams);

        // 3. Add an ArrayParameters of nested Parameters
        ArrayParameters nestedParamsArray = new ArrayParameters();
        VariableParameters nested1 = new VariableParameters();
        nested1.putValue("id", 1);
        nested1.putValue("name", "Item A");
        nestedParamsArray.addValue(nested1);

        VariableParameters nested2 = new VariableParameters();
        nested2.putValue("id", 2);
        nested2.putValue("name", "Item B");
        nestedParamsArray.addValue(nested2);
        mainParams.putValue("objectList", nestedParamsArray);

        // Verify retrieval of stringList
        ArrayParameters retrievedStringArray = mainParams.getParameters("stringList");
        Assert.assertNotNull(retrievedStringArray);
        Assert.assertEquals(2, retrievedStringArray.getStringList().size());
        Assert.assertEquals("value1", retrievedStringArray.getStringList().get(0));
        Assert.assertEquals("value2", retrievedStringArray.getStringList().get(1));

        // Verify retrieval of intList
        ArrayParameters retrievedIntArray = mainParams.getParameters("intList");
        Assert.assertNotNull(retrievedIntArray);
        Assert.assertEquals(2, retrievedIntArray.getIntList().size());
        Assert.assertEquals(100, (int)retrievedIntArray.getIntList().get(0));
        Assert.assertEquals(200, (int)retrievedIntArray.getIntList().get(1));

        // Verify retrieval of objectList
        ArrayParameters retrievedObjectArray = mainParams.getParameters("objectList");
        Assert.assertNotNull(retrievedObjectArray);
        Assert.assertEquals(2, retrievedObjectArray.getParametersList().size());
        Assert.assertEquals(1, (int)retrievedObjectArray.getParametersList().get(0).getInt("id"));
        Assert.assertEquals("Item A", retrievedObjectArray.getParametersList().get(0).getString("name"));
        Assert.assertEquals(2, (int)retrievedObjectArray.getParametersList().get(1).getInt("id"));
        Assert.assertEquals("Item B", retrievedObjectArray.getParametersList().get(1).getString("name"));

        // 4. Verify APON string output
        String apon = new AponWriter().write(mainParams).toString();
        Assert.assertEquals(apon, mainParams.toString());

        String expectedApon = "stringList: [\n" +
                "  value1\n" +
                "  value2\n" +
                "]\n" +
                "intList: [\n" +
                "  100\n" +
                "  200\n" +
                "]\n" +
                "objectList: [\n" +
                "  {\n" +
                "    id: 1\n" +
                "    name: Item A\n" +
                "  }\n" +
                "  {\n" +
                "    id: 2\n" +
                "    name: Item B\n" +
                "  }\n" +
                "]\n";
        // Normalize line endings for comparison
        String actual = mainParams.toString().trim().replace("\r\n", "\n");
        String normalizedExpected = expectedApon.trim().replace("\r\n", "\n");
        Assert.assertEquals(normalizedExpected, actual);
    }

    @Test
    public void testXSSPatternItem() throws IOException {
        String patterns = "[\n" +
                "    {\n" +
                "        pattern: <script>(.*?)</script>\n" +
                "        caseInsensitive: true\n" +
                "        multiline: false\n" +
                "        dotall: false\n" +
                "    }\n" +
                "    {\n" +
                "        pattern: \"src[\\\\r\\n]*=[\\\\r\\\\n]*\\\\'(.*?)\\\\'\"\n" +
                "        caseInsensitive: true\n" +
                "        multiline: true\n" +
                "        dotall: true\n" +
                "    }\n" +
                "    {\n" +
                "        pattern: \"src[\\\\r\\\\n]*=[\\\\r\\\\n]*\\\"(.*?)\\\"\"\n" +
                "        caseInsensitive: true\n" +
                "        multiline: true\n" +
                "        dotall: true\n" +
                "    }\n" +
                "    {\n" +
                "        pattern: </script>\n" +
                "        caseInsensitive: true\n" +
                "        multiline: false\n" +
                "        dotall: false\n" +
                "    }\n" +
                "    {\n" +
                "        pattern: <script(.*?)>\n" +
                "        caseInsensitive: true\n" +
                "        multiline: true\n" +
                "        dotall: true\n" +
                "    }\n" +
                "    {\n" +
                "        pattern: eval\\((.*?)\\)\n" +
                "        caseInsensitive: true\n" +
                "        multiline: true\n" +
                "        dotall: true\n" +
                "    }\n" +
                "    {\n" +
                "        pattern: expression\\((.*?)\\)\n" +
                "        caseInsensitive: true\n" +
                "        multiline: true\n" +
                "        dotall: true\n" +
                "    }\n" +
                "    {\n" +
                "        pattern: \"javascript:\"\n" +
                "        caseInsensitive: true\n" +
                "        multiline: false\n" +
                "        dotall: false\n" +
                "    }\n" +
                "    {\n" +
                "        pattern: \"vbscript:\"\n" +
                "        caseInsensitive: true\n" +
                "        multiline: false\n" +
                "        dotall: false\n" +
                "    }\n" +
                "    {\n" +
                "        pattern: onload(.*?)=\n" +
                "        caseInsensitive: true\n" +
                "        multiline: true\n" +
                "        dotall: true\n" +
                "    }\n" +
                "]\n";

        ArrayParameters xssPatternParameters = new ArrayParameters(XSSPatternItem.class, patterns);
        @SuppressWarnings("unchecked")
        List<XSSPatternItem> xssPatternItemList = (List<XSSPatternItem>)xssPatternParameters.getValueList();
        Assert.assertNotNull(xssPatternItemList);
        Assert.assertEquals(10, xssPatternItemList.size());
        Assert.assertEquals("<script>(.*?)</script>", xssPatternItemList.get(0).getPattern());
        String expectedApon = new AponWriter().indentString("    ").write(xssPatternParameters).toString();
        Assert.assertEquals(patterns.replace("\r\n", "\n"), expectedApon.replace("\r\n", "\n"));
    }

}
