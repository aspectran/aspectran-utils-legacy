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

import java.io.StringReader;

public class AponReaderCloseableTest {

    @Test
    public void testReaderClose() throws AponParseException {
        String apon = "aspectran: {\n" +
            "    settings: {\n" +
            "        transletNameSuffix: .job\n" +
            "    }\n" +
            "    bean: {\n" +
            "        id: *\n" +
            "        scan: test.**.*Schedule\n" +
            "        mask: test.**.*\n" +
            "        scope: singleton\n" +
            "    }\n" +
            "}\n";

        StringReader reader = new StringReader(apon);
        AponReaderCloseable aponReader = null;
        try {
            aponReader = new AponReaderCloseable(reader);
            aponReader.read();
        } finally {
            if (aponReader != null) {
                aponReader.close();
            }
        }
    }

}