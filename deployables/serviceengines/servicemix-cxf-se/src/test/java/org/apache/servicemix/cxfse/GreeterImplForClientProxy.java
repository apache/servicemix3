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

import javax.jbi.component.ComponentContext;
import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.apache.cxf.calculator.CalculatorPortType;
import org.apache.hello_world_soap_http.BadRecordLitFault;
import org.apache.hello_world_soap_http.Greeter;
import org.apache.hello_world_soap_http.NoSuchCodeLitFault;
import org.apache.hello_world_soap_http.types.GreetMeLaterResponse;
import org.apache.hello_world_soap_http.types.GreetMeResponse;
import org.apache.hello_world_soap_http.types.GreetMeSometimeResponse;
import org.apache.hello_world_soap_http.types.SayHiResponse;
import org.apache.hello_world_soap_http.types.TestDocLitFaultResponse;
import org.apache.hello_world_soap_http.types.TestNillableResponse;

@WebService(serviceName = "SOAPService", 
        portName = "SoapPort", 
        endpointInterface = "org.apache.hello_world_soap_http.Greeter", 
        targetNamespace = "http://apache.org/hello_world_soap_http")
public class GreeterImplForClientProxy implements Greeter {

    private ComponentContext context;
    private CalculatorPortType calculator;
    public String greetMe(String me) {
        int ret = 0;
        try {
            
            ret = getCalculator().add(1, 2);
                        
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Hello " + me  + " " + ret;
    }
    
    public ComponentContext getContext() {
        return context;
    }

    public void setContext(ComponentContext context) {
        this.context = context;
    }

    public void setCalculator(CalculatorPortType calculator) {
        this.calculator = calculator;
    }

    public CalculatorPortType getCalculator() {
        return calculator;
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
