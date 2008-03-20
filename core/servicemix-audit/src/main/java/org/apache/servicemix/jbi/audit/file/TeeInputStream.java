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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@link FilterInputStream} implementation that sends a copy of all data being processed to the specified OutputStream.
 * 
 * @author Gert Vanthienen (gertv)
 * @since 3.2
 */
public class TeeInputStream extends FilterInputStream {

    private final OutputStream os;

    /**
     * Create a new TeeInputStream
     * 
     * @param is the InputStream to read from
     * @param os the OuputStream to copy data to
     */
    public TeeInputStream(InputStream is, OutputStream os) {
        super(is);
        this.os = os;
    }

    /**
     * Reads a single byte from the underlying InputStream.  In addition, it also write the same byte to the OutputStream.
     */
    @Override
    public int read() throws IOException {
        int read = super.read();
        if (read != -1) {
            os.write(read);
        }
        return read;
    }

    /**
     * Read a block of bytes from the underlying InputStream.  In addition, write the same data to the OutputStream.
     */
    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        int read = super.read(bytes, offset, length);
        if (read != -1) {
            os.write(bytes, offset, read);
        }
        return read;
    }

    /**
     * Close the underlying InputStream.  Also, flush and close the underlying OutputStream.
     */
    @Override
    public void close() throws IOException {
        super.close();
        os.flush();
        os.close();
    }
}