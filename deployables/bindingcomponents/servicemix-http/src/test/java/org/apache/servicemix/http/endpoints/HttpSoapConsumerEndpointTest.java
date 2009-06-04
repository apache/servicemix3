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
package org.apache.servicemix.http.endpoints;

//import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.http.HttpComponent;

public class HttpSoapConsumerEndpointTest extends TestCase {

    private HttpSoapConsumerEndpoint httpSoapConsEp;        

    protected void setUp() throws Exception {
        super.setUp();
        httpSoapConsEp = new HttpSoapConsumerEndpoint();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        httpSoapConsEp = null;
    }

    // Test activate() when target endpoint WSDL is null.
    /*public void testActivateTargetEndpointWsdlNull() throws Exception {
        TestServiceUnit serviceUnit = new TestServiceUnit();        
        httpSoapConsEp.setServiceUnit(serviceUnit);
        
        try {
            httpSoapConsEp.activate();
            fail("activate() should fail if target endpoint WSDL is null");
        } catch (JBIException jbie) {
            // test passes
        }
    }*/

    // Test validate() when an invalid marshaler is set.
    public void testValidateInvalidMarshaler() throws Exception {
        httpSoapConsEp.setMarshaler(new TestHttpConsumerMarshaler());
        
        try {
            httpSoapConsEp.validate();
            fail("validate() should fail when marshaler is not an HttpSoapConsumerMarshaler");
        } catch (DeploymentException de) {
            // test succeeds
        }
    } 

    // TestServiceUnit needed to create a service unit associated with the HTTP component.
    public static class TestServiceUnit extends ServiceUnit {
        public TestServiceUnit() {
           super(new HttpComponent());           
        }
    }

    // TestHttpConsumerMarshaler needed for invalid marshaler test.
    public static class TestHttpConsumerMarshaler implements HttpConsumerMarshaler {
        public void sendOut(MessageExchange exchange, NormalizedMessage outMsg, HttpServletRequest request, HttpServletResponse response) {
        }

        public void sendFault(MessageExchange exchange, Fault fault, HttpServletRequest request, HttpServletResponse response) {
        }

        public void sendError(MessageExchange exchange, Exception error, HttpServletRequest request, HttpServletResponse response) {
        }

        public void sendAccepted(MessageExchange exchange, HttpServletRequest request, HttpServletResponse response) {
        }
        
        public MessageExchange createExchange(HttpServletRequest request, ComponentContext context) {
            return null;
        }        
    }
    
}
