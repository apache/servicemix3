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
package org.apache.servicemix.itests;

import java.io.StringWriter;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.servicemix.samples.wsdl_first.Person;
import org.apache.servicemix.samples.wsdl_first.PersonServiceService;
import org.apache.servicemix.samples.wsdl_first.types.GetPerson;
import org.apache.servicemix.samples.wsdl_first.types.GetPersonResponse;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

public class PersonTest extends SpringTestSupport {

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/itests/person.xml");
    }
    
    public void test() throws Exception {
        PostMethod method = new PostMethod("http://localhost:8192/PersonService/");
        String req = "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                     "              xmlns:tns=\"http://servicemix.apache.org/samples/wsdl-first/types\">" +
                     "  <env:Body>" +
                     "    <tns:GetPerson>" +
                     "       <tns:personId>world</tns:personId>" +
                     "    </tns:GetPerson>" +
                     "  </env:Body>" +
                     "</env:Envelope>";
        method.setRequestEntity(new StringRequestEntity(req));
        new HttpClient().executeMethod(method);
        System.err.println(method.getResponseBodyAsString());;
    }

    public void testFault() throws Exception {
        PostMethod method = new PostMethod("http://localhost:8192/PersonService/");
        String req = "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                     "              xmlns:tns=\"http://servicemix.apache.org/samples/wsdl-first/types\">" +
                     "  <env:Body>" +
                     "    <tns:GetPerson>" +
                     "       <tns:personId></tns:personId>" +
                     "    </tns:GetPerson>" +
                     "  </env:Body>" +
                     "</env:Envelope>";
        method.setRequestEntity(new StringRequestEntity(req));
        new HttpClient().executeMethod(method);
        System.err.println(method.getResponseBodyAsString());;
    }

}
