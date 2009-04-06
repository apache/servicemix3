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
package org.apache.servicemix.xbean;

import java.io.File;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Unit tests for the <code>ClassLoaderXmlPreprocessor</code>, mainly focus
 * on the classloader URL resources management.
 * @author jbonofre
 */
public class ClassLoaderXmlPreprocessorTest extends TestCase {

    private static final transient Log LOG = LogFactory.getLog(ClassLoaderXmlPreprocessorTest.class);
    
    public void testUrlResources() throws Exception {
        LOG.debug("Define a sample System properties");
        System.setProperty("servicemix.home", "/tmp");
        LOG.debug("Create a ClassLoaderXmlPreprocessor with . as root");
        ClassLoaderXmlPreprocessor classloader = new ClassLoaderXmlPreprocessor(new File("."));
        LOG.debug("Get the /tmp resource URL");
        List<URL> dirResourceList = classloader.getResources("src/test/resources");
        assertEquals("The directory resource list has not a correct size", 1, dirResourceList.size());
        LOG.info("Directory relative resource: " + dirResourceList.get(0).toString());
        List<URL> dirPropertyResourceList = classloader.getResources("${servicemix.home}");
        assertEquals("The directory system property resource list has not a correct size", 1, dirPropertyResourceList.size());
        LOG.info("Directory system property resource : " + dirPropertyResourceList.get(0).toString());
        List<URL> fileResourceList = classloader.getResources("file:./src/test/resources");
        assertEquals("The file resource list has not a correct size", 1, fileResourceList.size());
        LOG.info("File resource : "  + fileResourceList.get(0).toString());
        // Comment these tests, the test.ear must be present in src/test/resources directory
        // this test.ear should contains a test file
        /*
        List<URL> jarResourceList = classloader.getResources("jar:file:./src/test/resources/test.ear!/test");
        assertEquals("The jar resource list has not a correct size", 1, jarResourceList.size());
        LOG.info("Inside jar file resource : " + jarResourceList.get(0).toString());
        List<URL> jarRegexpResourceList = classloader.getResources("jar:file:./src/test/resources/test.ear!/te(.*)");
        assertEquals("The jar resource regexp list has not a correct size", 1, jarRegexpResourceList.size());
        LOG.info("Inside jar regexp file resource : " + jarRegexpResourceList.get(0).toString());
        assertEquals(new URL("jar:file:./src/test/resources/test.ear!/test"), (URL)jarRegexpResourceList.get(0));
        */
    }
    
}
