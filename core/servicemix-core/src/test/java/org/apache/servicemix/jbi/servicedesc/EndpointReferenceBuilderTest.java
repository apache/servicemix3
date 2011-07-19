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
package org.apache.servicemix.jbi.servicedesc;

import javax.xml.namespace.QName;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.util.DOMUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointReferenceBuilderTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointReferenceBuilderTest.class);

    /*
     * Test method for
     * 'org.apache.servicemix.jbi.servicedesc.EndpointReferenceBuilder.getReference(ServiceEndpoint)'
     */
    public void testGetReference() throws Exception {
        InternalEndpoint endpoint = new InternalEndpoint(null, "myEndpoint", new QName("http://foo.bar.com", "myService"));
        DocumentFragment df = EndpointReferenceBuilder.getReference(endpoint);
        assertNotNull(df);
        LOGGER.info(new SourceTransformer().toString(df));
        Element e = (Element) df.getFirstChild();
        assertEquals("http://java.sun.com/jbi/end-point-reference", e.getNamespaceURI());
        assertEquals("end-point-reference", e.getLocalName());
        assertEquals("myEndpoint", e.getAttributeNodeNS("http://java.sun.com/jbi/end-point-reference", "end-point-name").getValue());
        assertEquals(new QName("http://foo.bar.com", "myService"), DOMUtil.createQName(e, e.getAttributeNS(
                        "http://java.sun.com/jbi/end-point-reference", "service-name")));
    }

}
