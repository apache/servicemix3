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

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.dom.DOMSource;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.schemas.deployment.ClassPath;
import org.apache.servicemix.schemas.deployment.Component;
import org.apache.servicemix.schemas.deployment.Descriptor;
import org.apache.servicemix.schemas.deployment.DescriptorFactory;
import org.apache.servicemix.schemas.deployment.Identification;
import org.apache.servicemix.schemas.deployment.Component.SharedLibrary;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * @version $Revision$
 */
public class DeploymentTest extends TestCase {

    protected SourceTransformer transformer = new SourceTransformer();

    protected Descriptor root;
    
    protected ClassPath newClassPath(String[] jars) {
    	ClassPath cp = new ClassPath();
    	for (int i = 0; i < jars.length; i++) {
        	cp.getPathElement().add(jars[i]);
		}
    	return cp;
    }
    
    protected SharedLibrary newSharedLibrary(String name) {
    	SharedLibrary sl = new SharedLibrary();
    	sl.setContent(name);
    	return sl;
    }
    
    public void testParse() throws Exception {
        assertNotNull("JBI Container not found in spring!", root);

        // component stuff
        Component component = root.getComponent();
        assertNotNull("component is null", component);
        assertEquals("getBootstrapClassName", "com.foo.Engine1Bootstrap", component.getBootstrapClassName());
        assertEquals("getComponentClassName", "com.foo.Engine1", component.getComponentClassName());
        assertEquals("getComponentClassPath", newClassPath(new String[] {"Engine1.jar"}), component.getComponentClassPath());
        assertEquals("getBootstrapClassPath", newClassPath(new String[] {"Engine2.jar"}), component.getBootstrapClassPath());

        assertEquals("getDescription", "foo", component.getIdentification().getDescription());

        assertEquals("getSharedLibraries",  Collections.singletonList(newSharedLibrary("slib1")), component.getSharedLibraryList());

        Identification identification = component.getIdentification();
        assertNotNull("identification is null", identification);
        assertEquals("getName", "example-engine-1", identification.getName());
        assertEquals("getDescription", "An example service engine", identification.getDescription());

        List<Element> descriptorExtension = component.getAnyOrAny();
        assertNotNull("descriptorExtension is null", descriptorExtension);

        DocumentFragment fragment = DescriptorFactory.getDescriptorExtension(component);
        assertNotNull("fragment is null", fragment);

        System.out.println("Created document fragment: " + fragment);
        System.out.println("XML: " + transformer.toString(new DOMSource(fragment)));
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

    protected void setUp() throws Exception {
        root = DescriptorFactory.buildDescriptor(getDescriptorURL());
    }

    protected URL getDescriptorURL() throws Exception {
        return getClass().getResource("example.xml");
    }

}
