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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.servicemix.soap.marshalers.SoapMarshaler;
import org.apache.servicemix.soap.marshalers.SoapMessage;
import org.apache.servicemix.soap.util.IoUtil;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

public class Jsr181HttpTest extends SpringTestSupport {

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/itests/jsr181http.xml");
    }
    
    public void testWSDL() throws Exception {
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        Definition def = wsdlFactory.newWSDLReader().readWSDL("http://localhost:8194/Service/?wsdl");
        StringWriter writer = new StringWriter();
        wsdlFactory.newWSDLWriter().writeWSDL(def, writer);
        System.err.println(writer.toString());
    }
    
    public void testRequest() throws Exception {
        PostMethod method = new PostMethod("http://localhost:8194/Service/");
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new HttpMethodRetryHandler() {
            public boolean retryMethod(HttpMethod method, IOException exception, int executionCount) {
                return false;
            }
        });
        method.setRequestEntity(new StringRequestEntity(
                        "<env:Envelope xmlns:env='http://www.w3.org/2003/05/soap-envelope'>" + 
                        "  <env:Body>" + 
                        "    <echo xmlns='http://servicemix.org/test/'>" + 
                        "      <req>" + 
                        "        <msg xmlns='http://beans.itests.servicemix.apache.org'>" +
                        "          world" +
                        "        </msg>" +
                        "      </req>" +
                        "    </echo>" +
                        "  </env:body" +
                        "</env:Envelope>"));
        int state = new HttpClient().executeMethod(method);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copyInputStream(method.getResponseBodyAsStream(), baos);
        System.err.println(baos.toString());
        assertEquals(HttpServletResponse.SC_OK, state);
    }

    public void testMtom() throws Exception {
        PostMethod method = new PostMethod("http://localhost:8194/Service/");
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new HttpMethodRetryHandler() {
            public boolean retryMethod(HttpMethod method, IOException exception, int executionCount) {
                return false;
            }
        });
        method.setRequestEntity(new StringRequestEntity(
                        "<env:Envelope xmlns:env='http://www.w3.org/2003/05/soap-envelope'>" + 
                        "  <env:Body>" + 
                        "    <mtom xmlns='http://servicemix.org/test/'>" + 
                        "      <id>10</id>" +
                        "    </mtom>" +
                        "  </env:body" +
                        "</env:Envelope>"));
        int state = new HttpClient().executeMethod(method);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copyInputStream(method.getResponseBodyAsStream(), baos);
        System.err.println(baos.toString());
        assertEquals(HttpServletResponse.SC_OK, state);
        SoapMessage msg = new SoapMarshaler().createReader().read(new ByteArrayInputStream(baos.toByteArray()), method.getResponseHeader("Content-Type").getValue());
        DataHandler att = (DataHandler) msg.getAttachments().values().iterator().next();
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        IoUtil.copyStream(att.getInputStream(), baos2);
        assertEquals("<xsl:stylesheet />", baos2.toString());
        assertEquals(1, msg.getAttachments().size());
    }

}
