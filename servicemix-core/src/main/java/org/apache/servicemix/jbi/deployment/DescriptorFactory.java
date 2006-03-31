/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.jbi.deployment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.config.spring.XBeanProcessor;
import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.xbean.spring.context.ResourceXmlApplicationContext;
import org.springframework.core.io.UrlResource;

/**
 * @version $Revision: 359151 $
 */
public class DescriptorFactory {

    public static final String DESCRIPTOR_FILE = "META-INF/jbi.xml";

    private static Log log = LogFactory.getLog(DescriptorFactory.class);
    
    /**
     * Build a jbi descriptor from a file archive
     * 
     * @param descriptorFile path to the jbi descriptor, or to the root directory
     * @return the Descriptor object
     */
    public static Descriptor buildDescriptor(File descriptorFile) {
        if (descriptorFile.isDirectory()) {
            descriptorFile = new File(descriptorFile, DESCRIPTOR_FILE);
        }
        if (descriptorFile.isFile()) {
            try {
                return buildDescriptor(descriptorFile.toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException("There is a bug here...", e);
            }
        }
        return null;
    }
    
    /**
     * Build a jbi descriptor from the specified URL
     * 
     * @param url url to the jbi descriptor
     * @return the Descriptor object
     */
    public static Descriptor buildDescriptor(URL url) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(DescriptorFactory.class.getClassLoader());
            ResourceXmlApplicationContext context = new ResourceXmlApplicationContext(
                    new UrlResource(url), 
                    Arrays.asList(new Object[] { new XBeanProcessor() }));
            Descriptor descriptor = (Descriptor) context.getBean("jbi");
            return descriptor;
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }
    
    /**
     * Check validity of the JBI descriptor
     * @param descriptor the descriptor to check
     * @throws Exception if the descriptor is not valid
     */
    public static void checkDescriptor(Descriptor descriptor) throws Exception {
        // TODO
    }

    /**
     * Retrieves the jbi descriptor as a string
     * 
     * @param descriptorFile path to the jbi descriptor, or to the root directory
     * @return the contents of the jbi descriptor
     */
    public static String getDescriptorAsText(File descriptorFile) {
        if (descriptorFile.isDirectory()) {
            descriptorFile = new File(descriptorFile, DESCRIPTOR_FILE);
        }
        if (descriptorFile.isFile()) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                InputStream is = new FileInputStream(descriptorFile);
                FileUtil.copyInputStream(is, os);
                return os.toString();
            } catch (Exception e) {
                log.debug("Error reading jbi descritor: " + descriptorFile, e);
            }
        }
        return null;
    }

}
