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
package org.apache.servicemix.jbi.util;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.jbi.messaging.NormalizedMessage;
import javax.security.auth.Subject;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;

public class MessageCopierTest extends TestCase {

    private NormalizedMessage message;
    
    private Subject subject;
    
    public void setUp() throws Exception {
        subject = new Subject();
        message = new MessageUtil.NormalizedMessageImpl();
        message.setContent(new StringSource("<doc>s1<doc>"));
        message.addAttachment("a", new DataHandler(createDataSource("s2")));
        message.setProperty("p", "s3");
        message.setSecuritySubject(subject);
    }

    public void tearDown() throws Exception {
    }

    public void testCopyContent() throws Exception {
        MessageCopier copier = new MessageCopier(false, true, false, false);
        NormalizedMessage copy = copier.copy(message);
        String content = new SourceTransformer().toString(copy.getContent());
        assertEquals("wrong content", "<doc>s1<doc>", content);
        assertEquals("wrong attachment", null, copy.getAttachment("a"));
        assertEquals("wrong property", null, copy.getProperty("p"));
        assertEquals("wrong subject", null, copy.getSecuritySubject());
    }
    
    public void testCopyAttachment() throws Exception {
        MessageCopier copier = new MessageCopier(false, false, false, true); 
        NormalizedMessage copy = copier.copy(message);
        String attachment = IOUtils.toString(copy.getAttachment("a").getInputStream());
        assertEquals("wrong content", null, copy.getContent());
        assertEquals("wrong attachment", "s2", attachment);
        assertEquals("wrong property", null, copy.getProperty("p"));
        assertEquals("wrong subject", null, copy.getSecuritySubject());
    }
    
    public void testCopyProperties() throws Exception {
        MessageCopier copier = new MessageCopier(false, false, true, false); 
        NormalizedMessage copy = copier.copy(message);
        assertEquals("wrong content", null, copy.getContent());
        assertEquals("wrong attachment", null, copy.getAttachment("a"));
        assertEquals("wrong property", "s3", copy.getProperty("p"));
        assertEquals("wrong subject", null, copy.getSecuritySubject());
    }
    
    public void testCopySubject() throws Exception {
        MessageCopier copier = new MessageCopier(true, false, false, false); 
        NormalizedMessage copy = copier.copy(message);
        assertEquals("wrong content", null, copy.getContent());
        assertEquals("wrong attachment", null, copy.getAttachment("a"));
        assertEquals("wrong property", null, copy.getProperty("p"));
        assertEquals("wrong subject", subject, copy.getSecuritySubject());
    }
    
    private static DataSource createDataSource(String text) {
        return new ByteArrayDataSource(text.getBytes(), "text/plain");
    }
    
}
