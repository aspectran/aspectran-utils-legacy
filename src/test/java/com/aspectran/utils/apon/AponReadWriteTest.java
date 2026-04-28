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

import com.aspectran.utils.ResourceUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Test cases for reading from and writing to APON format.
 *
 * <p>Created: 2016. 9. 7.</p>
 */
public class AponReadWriteTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Tests a full read-write cycle: read from a file, write to a temporary file,
     * read back from the temporary file, and verify data integrity.
     */
    @Test
    public void testFileReadWriteCycle() throws IOException {
        // 1. Read from the original resource file
        File inputFile = ResourceUtils.getResourceAsFile("config/apon/apon-test.apon");
        Parameters originalParams = AponReader.read(inputFile);

        // 2. Write to a temporary file
        File outputFile = tempFolder.newFile("apon-test-output.apon");
        AponWriterCloseable aponWriter = new AponWriterCloseable(outputFile);
        try {
            aponWriter.write(originalParams);
        } finally {
            aponWriter.close();
        }

        // 3. Read back from the temporary file
        Parameters rereadParams = AponReader.read(outputFile);

        // 4. Verify that the data is the same
        Assert.assertEquals(originalParams.toString(), rereadParams.toString());
    }

    /**
     * Tests an in-memory read-write cycle using StringWriter and AponReader.
     */
    @Test
    public void testInMemoryReadWriteCycle() throws IOException {
        // 1. Create a Parameters object programmatically
        Parameters originalParams = new VariableParameters();
        originalParams.putValue("name", "test");
        originalParams.putValue("version(double)", 1.0);
        Parameters nestedParams = new VariableParameters();
        nestedParams.putValue("key", "value");
        originalParams.putValue("nested", nestedParams);

        // 2. Write to a StringWriter
        StringWriter stringWriter = new StringWriter();
        AponWriterCloseable aponWriter = new AponWriterCloseable(stringWriter);
        try {
            aponWriter.write(originalParams);
        } finally {
            aponWriter.close();
        }
        String aponString = stringWriter.toString();

        // 3. Read back from the string
        Parameters rereadParams = AponReader.read(aponString);

        // 4. Verify that the data is the same
        Assert.assertEquals(originalParams.toString(), rereadParams.toString());
    }

}
