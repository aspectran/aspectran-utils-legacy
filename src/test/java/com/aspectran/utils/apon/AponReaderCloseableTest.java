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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

/**
 * Test cases for AponReaderCloseable.
 */
public class AponReaderCloseableTest {

    /**
     * Tests that AponReaderCloseable works correctly within a try-with-resources block.
     */
    @Test
    public void testSuccessfulReadWithTryWithResources() throws AponParseException {
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
        AponReaderCloseable aponReader = new AponReaderCloseable(reader);
        try {
            aponReader.read();
        } finally {
            aponReader.close();
        }
    }

    /**
     * Verifies that the underlying reader is actually closed when the AponReaderCloseable is closed.
     */
    @Test
    public void testReaderIsClosed() throws AponParseException {
        String apon = "name: value";
        CloseTrackingStringReader trackingReader = new CloseTrackingStringReader(apon);

        AponReaderCloseable aponReader = new AponReaderCloseable(trackingReader);
        try {
            aponReader.read();
        } finally {
            aponReader.close();
        }

        Assert.assertTrue("The underlying reader should have been closed", trackingReader.isClosed());
    }

    /**
     * A helper StringReader that tracks whether it has been closed.
     */
    private static class CloseTrackingStringReader extends StringReader {
        private boolean closed = false;

        public CloseTrackingStringReader(String s) {
            super(s);
        }

        @Override
        public void close() {
            super.close();
            this.closed = true;
        }

        public boolean isClosed() {
            return closed;
        }
    }

    /**
     * Tests that attempting to read from a closed reader throws an exception.
     */
    @Test(expected = IOException.class)
    public void testReadAfterCloseThrowsException() throws AponParseException {
        String apon = "name: value";
        AponReaderCloseable aponReader = new AponReaderCloseable(new StringReader(apon));
        aponReader.read();
        aponReader.close();
        aponReader.read();
    }

}
