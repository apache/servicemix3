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
package org.apache.servicemix.components.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.jbi.JBIException;
import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class SimpleFlatFileMarshalerTest extends TestCase {

    public void testFixedLengthMarshalling() throws FileNotFoundException, IOException, JBIException {
        SimpleFlatFileMarshaler marshaler = new SimpleFlatFileMarshaler();
        marshaler.setLineFormat(SimpleFlatFileMarshaler.LINEFORMAT_FIXLENGTH);
        marshaler.setColumnLengths(new String[] {"2", "3", "5" });

        String path = "./src/test/resources/org/apache/servicemix/components/util/fixedlength.txt";
        String result = convertLinesToString(marshaler, new FileInputStream(new File(path)), path);
        assertNotNull(result);
        assertTrue(result.length() > 0);

        assertTrue(result.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<File"));
        assertTrue(result.endsWith("</File>"));

        assertTrue(StringUtils.countMatches(result, "<Line") == 3);
    }

    public void testFixedLengthWithColNamesMarshalling() throws FileNotFoundException, IOException, JBIException {
        SimpleFlatFileMarshaler marshaler = new SimpleFlatFileMarshaler();
        marshaler.setLineFormat(SimpleFlatFileMarshaler.LINEFORMAT_FIXLENGTH);
        marshaler.setColumnLengths(new String[] {"2", "3", "5" });
        marshaler.setColumnNames(new String[] {"First", "Second", "Third" });

        String path = "./src/test/resources/org/apache/servicemix/components/util/fixedlength.txt";
        String result = convertLinesToString(marshaler, new FileInputStream(new File(path)), path);
        assertNotNull(result);
        assertTrue(result.length() > 0);

        assertTrue(result.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<File"));
        assertTrue(result.endsWith("</File>"));

        assertTrue(StringUtils.countMatches(result, "<Line") == 3);
        assertTrue(StringUtils.countMatches(result, "<First") == 3);
        assertTrue(StringUtils.countMatches(result, "<Second") == 3);
        assertTrue(StringUtils.countMatches(result, "<Third") == 3);
    }

    public void testFixedLengthWithConversionMarshalling() throws FileNotFoundException, IOException, JBIException {
        SimpleFlatFileMarshaler marshaler = new SimpleFlatFileMarshaler();
        marshaler.setLineFormat(SimpleFlatFileMarshaler.LINEFORMAT_FIXLENGTH);
        marshaler.setColumnLengths(new String[] {"2", "3", "5", "8" });
        marshaler.setColumnNames(new String[] {"Number", "Text1", "Text2", "Date" });

        List columnConverters = new ArrayList();
        columnConverters.add(new NumberConverter());
        columnConverters.add(null);
        columnConverters.add(null);
        columnConverters.add(new DateConverter(new SimpleDateFormat("yyyyMMdd"), new SimpleDateFormat("yyyy-MM-dd")));
        marshaler.setColumnConverters(columnConverters);

        String path = "./src/test/resources/org/apache/servicemix/components/util/fixedlength_morecomplex.txt";
        String result = convertLinesToString(marshaler, new FileInputStream(new File(path)), path);
        assertNotNull(result);
        assertTrue(result.length() > 0);

        assertTrue(result.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<File"));
        assertTrue(result.endsWith("</File>"));

        assertTrue(StringUtils.countMatches(result, "<Line") == 3);
        assertTrue(StringUtils.countMatches(result, "<Number") == 3);
        assertTrue(StringUtils.countMatches(result, "<Text1") == 3);
        assertTrue(StringUtils.countMatches(result, "<Text2") == 3);
        assertTrue(StringUtils.countMatches(result, "<Date") == 2);
    }

    public void testCSVMarshalling() throws FileNotFoundException, IOException, JBIException {
        SimpleFlatFileMarshaler marshaler = new SimpleFlatFileMarshaler();
        marshaler.setLineFormat(SimpleFlatFileMarshaler.LINEFORMAT_CSV);

        String path = "./src/test/resources/org/apache/servicemix/components/util/csv_simplesample.csv";
        String result = convertLinesToString(marshaler, new FileInputStream(new File(path)), path);
        assertNotNull(result);
        assertTrue(result.length() > 0);

        assertTrue(result.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<File"));
        assertTrue(result.endsWith("</File>"));

        assertTrue(StringUtils.countMatches(result, "<Line") == 3);
    }
    
    class FileFewTimes extends InputStream {
        byte [] buffer;
        int pos;
        int timesLeft;
        public FileFewTimes(File file, int times) throws FileNotFoundException, IOException {
            this.timesLeft = times;
            buffer = new byte [(int)file.length()];
            FileInputStream fs = new FileInputStream(file);
            if (buffer.length != fs.read(buffer)) {
                throw new IOException("Unexpected end of file " + file.getCanonicalPath());
            }
        }

        @Override
        public int read() throws IOException {
            if (pos < buffer.length) {
                return buffer[pos++];
            }
            if (timesLeft == 0) {
                return -1;
            }
            timesLeft--;
            pos = 0;
            return buffer[pos++];
        }
    }
    
    public void testHugeStream() throws FileNotFoundException, IOException {
        SimpleFlatFileMarshaler marshaler = new SimpleFlatFileMarshaler();
        marshaler.setLineFormat(SimpleFlatFileMarshaler.LINEFORMAT_CSV);

        String path = "./src/test/resources/org/apache/servicemix/components/util/csv_simplesample.csv";
        InputStream in = new FileFewTimes(new File(path), 500000);
        InputStream out = marshaler.convertLines(null, in, path);
        int r = 0;
        while (r != -1) {
            r = out.read();
        }
    }

    private String convertLinesToString(SimpleFlatFileMarshaler marshaler, FileInputStream fileInputStream,
            String path) throws IOException, JBIException {
        InputStream out = marshaler.convertLines(null, fileInputStream, path);
        StringBuilder sb = new StringBuilder();
        for (Object string : IOUtils.readLines(out)) {
            sb.append(string);
        }
        return sb.toString();
    }

}
