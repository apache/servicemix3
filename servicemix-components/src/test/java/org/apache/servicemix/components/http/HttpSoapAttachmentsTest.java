/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.apache.servicemix.components.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.components.http.HttpSoapConnector;
import org.apache.servicemix.components.util.EchoComponent;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.codehaus.xfire.attachments.JavaMailAttachments;
import org.codehaus.xfire.attachments.SimpleAttachment;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class HttpSoapAttachmentsTest extends TestCase {

    private static final int PORT = 7012;

    protected JBIContainer container;
    
    protected void setUp() throws Exception {
        container = new JBIContainer();
        container.setMonitorInstallationDirectory(false);
        container.setUseMBeanServer(false);
        container.setCreateMBeanServer(false);
        container.init();
        container.start();
    }
    
    protected void tearDown() throws Exception {
        if (container != null) {
            container.shutDown();
        }
    }
    
    public void testWithAttachments() throws Exception {
        ActivationSpec as = new ActivationSpec();
        as.setId("echo");
        as.setComponent(new EchoComponent());
        as.setService(new QName("echo"));
        container.activateComponent(as);
        as = new ActivationSpec();
        as.setId("xfireBinding");
        as.setComponent(new HttpSoapConnector(null, PORT, true));
        as.setDestinationService(new QName("echo"));
        container.activateComponent(as);

        JavaMailAttachments sendAtts = new JavaMailAttachments();
        sendAtts.setSoapMessage(new SimpleAttachment("soap-request.xml",
                createDataHandler("soap-request.xml")));
        sendAtts.addPart(new SimpleAttachment("ServiceMix.jpg",
                createDataHandler("ServiceMix.jpg")));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sendAtts.write(bos);
        InputStream is = new ByteArrayInputStream(bos.toByteArray());
        PostMethodWebRequest req = new PostMethodWebRequest(
                "http://localhost:" + PORT, is, sendAtts.getContentType());
        WebResponse response = new WebConversation().getResponse(req);
        System.out.println(response.getText());
    }

    private DataHandler createDataHandler(String name) throws MessagingException {
        File f = new File(getClass().getResource(name).getPath());
        FileDataSource fs = new FileDataSource(f);
        return new DataHandler(fs);
    }
}
