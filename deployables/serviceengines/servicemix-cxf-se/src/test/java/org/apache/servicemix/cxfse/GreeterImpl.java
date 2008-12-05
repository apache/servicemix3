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
package org.apache.servicemix.cxfse;

import java.util.concurrent.Future;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.apache.hello_world_soap_http.BadRecordLitFault;
import org.apache.hello_world_soap_http.Greeter;
import org.apache.hello_world_soap_http.NoSuchCodeLitFault;
import org.apache.hello_world_soap_http.types.GreetMeLaterResponse;
import org.apache.hello_world_soap_http.types.GreetMeResponse;
import org.apache.hello_world_soap_http.types.GreetMeSometimeResponse;
import org.apache.hello_world_soap_http.types.SayHiResponse;
import org.apache.hello_world_soap_http.types.TestDocLitFaultResponse;
import org.apache.hello_world_soap_http.types.TestNillableResponse;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.client.ServiceMixClientFacade;
import org.apache.servicemix.jbi.jaxp.StringSource;

@WebService(serviceName = "SOAPService", 
        portName = "SoapPort", 
        endpointInterface = "org.apache.hello_world_soap_http.Greeter", 
        targetNamespace = "http://apache.org/hello_world_soap_http")
public class GreeterImpl implements Greeter {

    private ComponentContext context;
    public String greetMe(String me) {
        try {
            
            // here use client api to test the injected context to invoke another endpoint
            ServiceMixClient client = new ServiceMixClientFacade(this.context);
            InOut exchange = client.createInOutExchange();
            NormalizedMessage message = exchange.getInMessage();
            
            message.setContent(new StringSource(
                    "<message xmlns='http://java.sun.com/xml/ns/jbi/wsdl-11-wrapper'>"
                    + "  <part>"
                    + "    <add xmlns='http://apache.org/cxf/calculator/types'>"
                    + "      <arg0>1</arg0>"
                    + "      <arg1>2</arg1>"
                    + "    </add>"
                    + "  </part>"
                    + "</message>"));
      
            exchange.setService(new QName("http://apache.org/cxf/calculator", "CalculatorService"));
            exchange.setInterfaceName(new QName("http://apache.org/cxf/calculator", "CalculatorPortType"));
            exchange.setOperation(new QName("http://apache.org/cxf/calculator", "add"));
            client.sendSync(exchange);
            
        } catch (JBIException e) {
            //
        }
        return "Hello " + me  + " " + context.getComponentName();
    }
    
    public ComponentContext getContext() {
        return context;
    }

    public void setContext(ComponentContext context) {
        this.context = context;
    }

    public Response<GreetMeResponse> greetMeAsync(String arg0) {
        return null;
    }

    public Future<?> greetMeAsync(String arg0, AsyncHandler<GreetMeResponse> arg1) {
        return null;
    }

    public String greetMeLater(long arg0) {
        return null;
    }

    public Response<GreetMeLaterResponse> greetMeLaterAsync(long arg0) {
        return null;
    }

    public Future<?> greetMeLaterAsync(long arg0, AsyncHandler<GreetMeLaterResponse> arg1) {
        return null;
    }

    public void greetMeOneWay(String arg0) {        
    }

    public String greetMeSometime(String arg0) {
        return null;
    }

    public Response<GreetMeSometimeResponse> greetMeSometimeAsync(String arg0) {
        return null;
    }

    public Future<?> greetMeSometimeAsync(String arg0, AsyncHandler<GreetMeSometimeResponse> arg1) {
        return null;
    }

    public String sayHi() {
        return null;
    }

    public Response<SayHiResponse> sayHiAsync() {
        return null;
    }

    public Future<?> sayHiAsync(AsyncHandler<SayHiResponse> arg0) {
        return null;
    }

    public void testDocLitFault(String arg0) throws BadRecordLitFault, NoSuchCodeLitFault {
    }

    public Response<TestDocLitFaultResponse> testDocLitFaultAsync(String arg0) {
        return null;
    }

    public Future<?> testDocLitFaultAsync(String arg0, AsyncHandler<TestDocLitFaultResponse> arg1) {
        return null;
    }

    public String testNillable(String arg0, int arg1) {
        return null;
    }

    public Response<TestNillableResponse> testNillableAsync(String arg0, int arg1) {
        return null;
    }

    public Future<?> testNillableAsync(String arg0, int arg1, AsyncHandler<TestNillableResponse> arg2) {
        return null;
    }
}
