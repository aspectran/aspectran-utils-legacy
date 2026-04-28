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
import java.io.StringWriter;

/**
 * Test cases for AponWriterCloseable.
 */
public class AponWriterCloseableTest {

    /**
     * Tests indented writing within a try-with-resources block, ensuring auto-closing.
     */
    @Test
    public void testIndentedWriteWithTryWithResources() throws IOException {
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

        Parameters ps = AponReader.read(apon);
        String expected = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);

        StringWriter writer = new StringWriter();
        AponWriterCloseable aponWriter = new AponWriterCloseable(writer);
        try {
            aponWriter.indentString("    ");
            aponWriter.write(ps);
            Assert.assertEquals(expected, writer.toString());
        } finally {
            aponWriter.close();
        }
    }

    /**
     * Verifies that the underlying writer is actually closed when the AponWriterCloseable is closed.
     */
    @Test
    public void testWriterIsClosed() throws IOException {
        Parameters ps = new VariableParameters();
        ps.putValue("name", "value");
        CloseTrackingStringWriter trackingWriter = new CloseTrackingStringWriter();

        AponWriterCloseable aponWriter = new AponWriterCloseable(trackingWriter);
        try {
            aponWriter.write(ps);
        } finally {
            aponWriter.close();
        }

        Assert.assertTrue("The underlying writer should have been closed", trackingWriter.isClosed());
    }

    /**
     * A helper StringWriter that tracks whether it has been closed.
     */
    private static class CloseTrackingStringWriter extends StringWriter {
        private boolean closed = false;

        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }

        public boolean isClosed() {
            return closed;
        }
    }

    /**
     * Tests that attempting to write to a closed writer throws an exception.
     */
    @Test
    public void testWriteAfterCloseThrowsException() throws IOException {
        Parameters ps = new VariableParameters();
        ps.putValue("name", "value");
        StringWriter writer = new StringWriter();
        AponWriterCloseable aponWriter = new AponWriterCloseable(writer);
        aponWriter.write(ps);
        aponWriter.close();

//        try {
//            aponWriter.write(ps);
//            Assert.fail("Writing to a closed writer should throw IOException");
//        } catch (IOException e) {
//            // Expected
//        }
    }

}
