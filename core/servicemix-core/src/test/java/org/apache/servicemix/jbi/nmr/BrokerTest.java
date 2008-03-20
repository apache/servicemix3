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
package org.apache.servicemix.jbi.nmr;

import javax.jbi.JBIException;
import javax.jbi.messaging.InOnly;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import junit.framework.TestCase;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.ReceiverComponent;

public class BrokerTest extends TestCase {
    
    public void testExternalRouting() throws Exception {
        JBIContainer container = new JBIContainer();
        container.setEmbedded(true);
        container.init();
        container.start();
        ReceiverComponent receiver = new ReceiverComponent() {
            protected void init() throws JBIException {
                ServiceEndpoint ep = new TestExternalEndpoint(getService(), getEndpoint());
                getContext().registerExternalEndpoint(ep);
                setService(null);
                setEndpoint(null);
            }

            public ServiceEndpoint resolveEndpointReference(DocumentFragment fragment) {
                try {
                    SourceTransformer st = new SourceTransformer();
                    String xml = st.toString(fragment);
                    return (ServiceEndpoint) new XStream(new DomDriver()).fromXML(xml);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        container.activateComponent(new ActivationSpec("receiver", receiver));
        ServiceMixClient client = new DefaultServiceMixClient(container);
        ServiceEndpoint[] endpoints = client.getContext().getExternalEndpoints(null);
        assertNotNull(endpoints);
        assertEquals(1, endpoints.length);
        assertNull(client.getContext().getEndpointDescriptor(endpoints[0]));
        ServiceEndpoint se = client.getContext().resolveEndpointReference(endpoints[0].getAsReference(null));
        assertNull(client.getContext().getEndpointDescriptor(se));
        InOnly me = client.createInOnlyExchange();
        me.setEndpoint(se);
        client.send(me);
        receiver.getMessageList().assertMessagesReceived(1);
    }
    
    public static class TestExternalEndpoint implements ServiceEndpoint {
        private QName service;
        private String endpoint;
        public TestExternalEndpoint(QName service, String endpoint) {
            this.service = service;
            this.endpoint = endpoint;
        }
        public DocumentFragment getAsReference(QName operationName) {
            try {
                SourceTransformer st = new SourceTransformer();
                String xml = new XStream(new DomDriver()).toXML(this);
                Document doc = (Document) st.toDOMNode(new StringSource(xml));
                DocumentFragment df = doc.createDocumentFragment();
                df.appendChild(doc.getDocumentElement());
                return df;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        public String getEndpointName() {
            return endpoint;
        }
        public QName[] getInterfaces() {
            return null;
        }
        public QName getServiceName() {
            return service;
        }
    }
    
}
