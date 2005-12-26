/**
 * <a href="http://servicemix.org">ServiceMix: The open source ESB</a>
 * 
 * Copyright 2005 RAJD Consultancy Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 */
package org.apache.servicemix.jbi.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XmlPersistenceSupport {

    private static XStream xstream = new XStream(new DomDriver());
    
    public static Object read(File file) throws IOException {
        Reader r = new FileReader(file);
        try {
            return xstream.fromXML(r);
        } finally {
            r.close();   
        }
    }
    
    public static void write(File file, Object obj) throws IOException {
        Writer w = new FileWriter(file);
        try {
            xstream.toXML(obj, w);
        } finally {
            w.close();
        }
    }
    
}
