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

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import junit.framework.TestCase;

import org.apache.servicemix.http.jetty.SmxHttpExchange;
import org.springframework.core.io.FileSystemResource;


public class HttpSoapProviderEndpointTest extends TestCase {

    private HttpSoapProviderEndpoint soapProviderEp;

    protected void setUp() throws Exception {
        super.setUp();
        soapProviderEp = new HttpSoapProviderEndpoint();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        soapProviderEp = null;
    }

    // Test for validate() when WSDL is null.
    public void testValidateWsdlNull() throws Exception {
        try {
            soapProviderEp.validate();
            fail("validate() should fail when WSDL is null");
        } catch (DeploymentException de) {
            // test passes
        }
    }

    public void testValidateInvalidMarshaler() throws Exception {
        FileSystemResource wsdl = new FileSystemResource("provider/http.wsdl");
        soapProviderEp.setWsdl(wsdl);
        soapProviderEp.setMarshaler(new TestHttpProviderMarshaler());

        try {
            soapProviderEp.validate();
            fail("validate() should fail when an invalid marshaler is used");
        } catch (DeploymentException de) {
            // test passes
        }
    }
 
    public void testValidateNonexistentWsdl() throws Exception {
        FileSystemResource wsdl = new FileSystemResource("provider/noexist.wsdl");
        soapProviderEp.setWsdl(wsdl);
 
        try {
            soapProviderEp.validate();
            fail("validate() should fail for non-existent WSDL");
        } catch (DeploymentException de) {
            // test succeeds
        }
    }

    // Dummy implementation of a marshaler for the invalid marshaler test.
    public static class TestHttpProviderMarshaler implements HttpProviderMarshaler {
        public void createRequest(MessageExchange exchange, NormalizedMessage inMsg, SmxHttpExchange httpExchange) {
        }

        public void handleResponse(MessageExchange exchange, SmxHttpExchange httpExchange) {
        }

        public void handleException(MessageExchange exchange, SmxHttpExchange httpExchange, Throwable ex) {
        }
    }
}
