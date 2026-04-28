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

import com.aspectran.utils.wildcard.IncludeExcludeParameters;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Test cases for verifying the noBrackets option in ParameterKey.
 */
public class AponNoBracketsTest {

    @Test
    public void testNoBracketsForStringArray() {
        IncludeExcludeParameters params = new IncludeExcludeParameters();
        params.addIncludePattern("/**");
        params.addExcludePattern("/assets/**");
        params.addExcludePattern("/favicon.ico");

        String result = params.toString();

        // Expected output with repeated keys instead of [ ]
        String expected = "+: /**\n" +
                "-: /assets/**\n" +
                "-: /favicon.ico\n";

        Assert.assertEquals(expected.replace("\r\n", "\n"), result.replace("\r\n", "\n"));
    }

    @Test
    public void testNoBracketsInNestedStructure() {
        // Simulating WebConfig structure
        Parameters webConfig = new VariableParameters();

        // Simulating AcceptableConfig (which inherits IncludeExcludeParameters)
        IncludeExcludeParameters acceptable = new IncludeExcludeParameters();
        acceptable.addIncludePattern("/**");
        acceptable.addExcludePattern("/assets/**");
        acceptable.addExcludePattern("/favicon.ico");

        webConfig.putValue("acceptable", acceptable);

        String result = webConfig.toString();

        String expected = "acceptable: {\n" +
                "  +: /**\n" +
                "  -: /assets/**\n" +
                "  -: /favicon.ico\n" +
                "}\n";

        Assert.assertEquals(expected.replace("\r\n", "\n"), result.replace("\r\n", "\n"));
    }

    @Test
    public void testRoundTripWithRepeatedKeys() throws IOException {
        String apon = "item: {\n" +
                "  id: 1\n" +
                "}\n" +
                "item: {\n" +
                "  id: 2\n" +
                "}\n";

        // Parsing without schema (using VariableParameters)
        Parameters root = AponReader.read(apon);

        // Writing back should preserve repeated keys instead of creating [ ]
        String result = root.toString();

        Assert.assertEquals(apon.replace("\r\n", "\n"), result.replace("\r\n", "\n"));
    }

}
