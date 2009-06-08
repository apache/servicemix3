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
package org.apache.servicemix.http.packaging;

import java.util.List;

//import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.xml.namespace.QName;

//import org.w3c.dom.Document;

import junit.framework.TestCase;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.Endpoint;
import org.apache.servicemix.common.ExchangeProcessor;
import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.http.HttpEndpoint;


public class HttpServiceUnitAnalyzerTest extends TestCase {

    private HttpServiceUnitAnalyzer httpSuAnalyzer;
    private ServiceUnit su;

    protected void setUp() throws Exception {
        super.setUp();
        httpSuAnalyzer = new HttpServiceUnitAnalyzer();
        su = new ServiceUnit();
        su.setComponent(new MyComponent());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        su = null;
        httpSuAnalyzer = null;
    }

    // Test isValidEndpoint() for valid HttpEndpoint on ServiceUnit.
    public void testIsValidEndpointTrue() throws Exception {
        HttpEndpoint httpEp = new HttpEndpoint();
        httpEp.setService(new QName("urn:test", "service"));
        httpEp.setEndpoint("http://localhost:8080/someService");
        su.addEndpoint(httpEp);

        // Create the key that was used by the addEndpoint method.
        String epKey = "{urn:test}service:http://localhost:8080/someService";
        assertTrue("", httpSuAnalyzer.isValidEndpoint(su.getEndpoint(epKey)));
    }

    // Test isValidEndpoint() for non-HttpEndpoint on Service Unit.
    public void testIsValidEndpointFalse() throws Exception {
        QName serviceName = new QName("http://localhost:8080/someService", "someService");
        TestEndpoint testEp = new TestEndpoint(su, serviceName, "test");
        System.out.println("key == " + testEp.getKey());
        su.addEndpoint(testEp);
        assertFalse("isValidEndpoint() should return false for non-HttpEndpoint", httpSuAnalyzer.isValidEndpoint(su.getEndpoint("test")));
    }

    // Test getConsumes() when endpoint has proper target setup.
    public void testGetConsumesUsingTargets() throws Exception {
        HttpEndpoint httpEp = new HttpEndpoint();
        httpEp.setRoleAsString("consumer");
        httpEp.setTargetService(new QName("urn:test", "service"));
        httpEp.setTargetEndpoint("http://localhost:8080/someService");
        httpEp.setTargetInterfaceName(new QName("urn:test", "portType"));

        List consumeList = httpSuAnalyzer.getConsumes(httpEp);

        assertNotNull("getConsumes() should not return null list", consumeList);
    }

    // Test getConsumes() when endpoint does not have target setup.
    public void testGetConsumesInvalidList() throws Exception {
        HttpEndpoint httpEp = new HttpEndpoint();
        httpEp.setRoleAsString("consumer");
        httpEp.setService(new QName("urn:test", "service"));
        httpEp.setEndpoint("http://localhost:8080/someService");
        httpEp.setInterfaceName(new QName("urn:test", "portType"));
        List consumeList = httpSuAnalyzer.getConsumes(httpEp);
        assertNotNull("getConsumes() should not return null list", consumeList);
    }

    // Dummy Endpoint implementation for testing.
    public static class TestEndpoint extends Endpoint {
        private String key;

        public TestEndpoint() {
            key = "test";
        }
        
        public TestEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
           super(serviceUnit, service, endpoint);
        }

        public String getKey() { 
            return key;
        }

        public void activate() throws Exception {
        }

        public void deactivate() throws Exception {
        }

        public ExchangeProcessor getProcessor() {
            return null;
        }

        public Role getRole() {
            return null;
        }

        public boolean isExchangeOkay(MessageExchange exchange) {
            return false;
        }
        
    }

    protected class MyComponent extends DefaultComponent {
        public MyComponent() {
            super();
        }

        public List getConfiguredEndpoints() {
            return null;
        }

        public Class[] getEndpointClasses() {
            return new Class[1];
        }
    }
}
