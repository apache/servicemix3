/**
 * <a href="http://servicemix.org">ServiceMix: The open source ESB</a> 
 * 
 * Copyright 2005 RAJD Consultancy Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.servicemix.jbi.util;

import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArrayList;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Write to multiple OutputStreams
 * 
 * @version $Revision$
 */
public class MultiplexOutputStream extends OutputStream {
    List streams = new CopyOnWriteArrayList();
    
    
    /**
     * Add an Output Stream
     * @param os
     */
    public void add(OutputStream os){
        streams.add(os);
    }
    
    /**
     * Remove an OutputStream
     * @param os
     */
    public void remove(OutputStream os){
        streams.remove(os);
    }

    /**
     * write a byte
     * @param b
     * @throws IOException
     */
    public synchronized void write(int b) throws IOException {
        for (Iterator i = streams.iterator();i.hasNext();) {
            OutputStream s = (OutputStream) i.next();
            s.write(b);
        }
    }

    /**
     * write an array
     * @param b
     * @param off
     * @param len
     * @throws IOException
     */
    public synchronized void write(byte b[], int off, int len) throws IOException {
        for (Iterator i = streams.iterator();i.hasNext();) {
            OutputStream s = (OutputStream) i.next();
            s.write(b, off, len);
        }
    }

    /**
     * flush
     * @throws IOException
     */
    public void flush() throws IOException {
        for (Iterator i = streams.iterator();i.hasNext();) {
            OutputStream s = (OutputStream) i.next();
            s.flush();
        }
    }

    /**
     * close
     * @throws IOException
     */
    public void close() throws IOException {
        for (Iterator i = streams.iterator();i.hasNext();) {
            OutputStream s = (OutputStream) i.next();
            s.close();
        }
        streams.clear();
    }
}
