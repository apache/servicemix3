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
package org.apache.servicemix.jbi.deployment;

import java.util.Arrays;

import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.DocumentFragment;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Revision$
 */
public class ComponentTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentTest.class);

    protected SourceTransformer transformer = new SourceTransformer();

    public void testParse() throws Exception {

        // lets force the JBI container to be constructed first
        Descriptor root = DescriptorFactory.buildDescriptor(getClass().getResource("component.xml"));
        assertNotNull("Unable to parse descriptor", root);

        // component stuff
        Component component = root.getComponent();
        assertNotNull("component is null", component);
        assertEquals("getBootstrapClassName", "com.foo.Engine1Bootstrap", component.getBootstrapClassName());
        assertEquals("getComponentClassName", "com.foo.Engine1", component.getComponentClassName());
        assertEquals("getComponentClassPath", new ClassPath(new String[] {"Engine1.jar"}), component.getComponentClassPath());
        assertEquals("getBootstrapClassPath", new ClassPath(new String[] {"Engine2.jar"}), component.getBootstrapClassPath());

        assertEquals("getDescription", "foo", component.getDescription());

        assertArrayEquals("getSharedLibraries", new SharedLibraryList[] {new SharedLibraryList("slib1")}, component.getSharedLibraries());

        Identification identification = component.getIdentification();
        assertNotNull("identification is null", identification);
        assertEquals("getName", "example-engine-1", identification.getName());
        assertEquals("getDescription", "An example service engine", identification.getDescription());

        InstallationDescriptorExtension descriptorExtension = component.getDescriptorExtension();
        assertNotNull("descriptorExtension is null", descriptorExtension);

        DocumentFragment fragment = descriptorExtension.getDescriptorExtension();
        assertNotNull("fragment is null", fragment);

        LOGGER.info("Created document fragment: {}", fragment);
        LOGGER.info("XML: {}", transformer.toString(new DOMSource(fragment)));
    }

    protected void assertArrayEquals(String text, Object[] expected, Object[] actual) {
        assertTrue(text + ". Expected <" + toString(expected) + "> Actual <"  + toString(actual) + ">", Arrays.equals(expected, actual));
    }

    protected String toString(Object[] objects) {
        if (objects == null) {
            return "null Object[]";
        }
        StringBuffer buffer = new StringBuffer("[");
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(object);
        }
        buffer.append("]");
        return buffer.toString();
    }

}
