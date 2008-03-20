/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.jbi.audit.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 * Unit tests for {@link TeeInputStream}
 * 
 * @author Gert Vanthienen (gertv)
 * @since 3.2
 */
public class TeeInputStreamTest extends TestCase {

    private static final String TEXT = "Apache ServiceMix is an Open Source ESB (Enterprise Service Bus) "
            + "that combines the functionality of a Service Oriented Architecture (SOA) and an Event Driven Architecture (EDA) "
            + "to create an agile, enterprise ESB";

    /**
     * Test for reading byte-by-byte
     */
    public void testReadByByte() throws Exception {
        InputStream is = new ByteArrayInputStream(TEXT.getBytes());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TeeInputStream tis = new TeeInputStream(is, bos);
        while (tis.read() >= 0) {
            // nothing to do
        }
        is.close();
        assertResult(bos.toByteArray());
    }

    /**
     * Test for reading blocks of bytes
     */
    public void testReadBlock() throws Exception {
        InputStream is = new ByteArrayInputStream(TEXT.getBytes());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TeeInputStream tis = new TeeInputStream(is, bos);
        byte[] data = new byte[4096];
        assertEquals(TEXT.length(), tis.read(data, 0, data.length));
        tis.close();
        assertResult(bos.toByteArray());
    }

    private void assertResult(byte[] bytes) {
        assertEquals(TEXT.length(), bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            assertEquals("Characters on position " + (i + 1) + " should match", TEXT.getBytes()[i], bytes[i]);
        }
    }
}
