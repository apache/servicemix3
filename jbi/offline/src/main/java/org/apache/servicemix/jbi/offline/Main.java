/**
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
package org.apache.servicemix.jbi.offline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.servicemix.jbi.deployer.descriptor.Descriptor;
import org.apache.servicemix.jbi.deployer.descriptor.DescriptorFactory;

public class Main {

    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    public void run(String... args) throws Exception {
        File f = new File(args[0]);
        if (!f.exists()) {
            throw new IllegalStateException("File " + f.toString() + " does not exists.");
        }
        JarFile jar = new JarFile(args[0]);
        Manifest m = jar.getManifest();
        JarEntry entry = jar.getJarEntry("META-INF/jbi.xml");
        InputStream is = jar.getInputStream(entry);
        Descriptor desc = DescriptorFactory.buildDescriptor(is);

        if (desc.getComponent() != null) {
            String name = desc.getComponent().getIdentification().getName();
            String version = m.getMainAttributes().getValue("Implementation-Version");
            m.getMainAttributes().put(new Attributes.Name("Bundle-SymbolicName"), name);
            m.getMainAttributes().put(new Attributes.Name("Bundle-Version"), version);
        }

        JarInputStream jis = new JarInputStream(new FileInputStream(args[0]));
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(args[1]), m);

        byte[] buffer = new byte[8192];
        entry = jis.getNextJarEntry();
        while (entry != null) {
            jos.putNextEntry(entry);
            copyInputStream(jis, jos);
            jos.closeEntry();
            entry = jis.getNextJarEntry();
        }
        jos.close();
        jis.close();
    }

    protected static void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
    }
}
