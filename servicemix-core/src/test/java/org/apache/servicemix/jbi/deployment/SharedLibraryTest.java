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

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.schemas.deployment.Identification;
import org.apache.servicemix.schemas.deployment.Descriptor.SharedLibrary;

/**
 * @version $Revision$
 */
public class SharedLibraryTest extends DeploymentTest {

    protected SourceTransformer transformer = new SourceTransformer();

    public void testParse() throws Exception {

        // lets force the JBI container to be constructed first
        assertNotNull("JBI descriptor not found", root);

        SharedLibrary sl = root.getSharedLibrary();
        Identification identification = sl.getIdentification();
        assertEquals("getName", "TestSharedLibrary", identification.getName());
        assertEquals("getDescription", "This is a test shared library.", identification.getDescription());
        
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

    protected URL getDescriptorURL() throws Exception {
        return getClass().getResource("SharedLibrary.xml");
    }

}
