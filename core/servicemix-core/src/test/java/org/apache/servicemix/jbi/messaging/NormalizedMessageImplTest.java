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
package org.apache.servicemix.jbi.messaging;

import javax.activation.DataHandler;
import javax.jbi.messaging.NormalizedMessage;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.util.ByteArrayDataSource;

/**
 * this test case is for normalized message impl class
 * 
 * @author lhe
 */
public class NormalizedMessageImplTest extends TestCase {
    /**
     * getAttachment test method
     * 
     * @throws Exception
     */
    public void testGetAttachment() throws Exception {
        // testcase for SM-1186
        DataHandler dh = new DataHandler(new ByteArrayDataSource("test".getBytes(), "test"));

        // first test with existing attachments
        MessageExchangeImpl inOnly = new InOnlyImpl("inonly");
        NormalizedMessage msg = inOnly.createMessage();
        msg.addAttachment("att_1", dh);
        inOnly.setMessage(msg, "in");

        assertNotNull(inOnly.getMessage("in").getAttachment("att_1"));
        assertNull(inOnly.getMessage("in").getAttachment("att_2"));
        
        // now test without attachments
        inOnly.getMessage("in").removeAttachment("att_1");
        
        assertNull(inOnly.getMessage("in").getAttachment("att_1"));
        assertNull(inOnly.getMessage("in").getAttachment("att_2"));
    }
}
