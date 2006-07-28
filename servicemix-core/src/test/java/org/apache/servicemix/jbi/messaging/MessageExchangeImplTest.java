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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.jaxp.BytesSource;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.InOnlyImpl;
import org.apache.servicemix.jbi.util.StreamDataSource;

import javax.activation.DataHandler;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

public class MessageExchangeImplTest extends TestCase {
    
	private static final Log log = LogFactory.getLog(MessageExchangeImplTest.class);
	
    protected void testSerializeDeserialize(Source src) throws Exception {
        MessageExchange me = new InOnlyImpl("exchangeId");
        me.setOperation(new QName("uri", "op"));
        me.setProperty("myProp", "myValue");
        NormalizedMessage msg = me.createMessage();
        msg.setProperty("myMsgProp", "myMsgValue");
        msg.setContent(src);
        msg.addAttachment("myAttachment", new DataHandler(new StreamDataSource(new ByteArrayInputStream("hello".getBytes()))));
        me.setMessage(msg, "in");
        assertNotNull(((NormalizedMessageImpl) msg).getBody());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(me);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object out = ois.readObject();
        
        assertNotNull(out);
        assertTrue(out instanceof MessageExchange);
        MessageExchange meOut = (MessageExchange) out;
        assertEquals(new QName("uri", "op"), meOut.getOperation());
        assertEquals("myValue", meOut.getProperty("myProp"));
        NormalizedMessage msgOut = meOut.getMessage("in");
        assertNotNull(msgOut);
        assertEquals("myMsgValue", msgOut.getProperty("myMsgProp"));
        Source outSrc = msgOut.getContent();
        assertNotNull(outSrc);
        String outStr = new SourceTransformer().toString(outSrc);
        assertNotNull(outStr);
        assertNotNull(((NormalizedMessageImpl) msgOut).getBody());
        log.info(outStr);
        assertNotNull(msgOut.getAttachment("myAttachment"));
    }

    
    public void testSerializeDeserializeWithStringSource() throws Exception {
        Source src = new StringSource("<hello>world</hello>");
        testSerializeDeserialize(src);
    }

    public void testSerializeDeserializeWithBytesSource() throws Exception {
        Source src = new BytesSource("<hello>world</hello>".getBytes());
        testSerializeDeserialize(src);
    }

    public void testSerializeDeserializeWithStreamSource() throws Exception {
        Source src = new StreamSource(new ByteArrayInputStream("<hello>world</hello>".getBytes()));
        testSerializeDeserialize(src);
    }

    public void testSerializeDeserializeWithDomSource() throws Exception {
        Source src = new SourceTransformer().toDOMSource(new StringSource("<hello>world</hello>"));
        testSerializeDeserialize(src);
    }

    public void testSerializeDeserializeWithSaxSource() throws Exception {
        Source src = new SourceTransformer().toSAXSource(new StringSource("<hello>world</hello>"));
        testSerializeDeserialize(src);
    }

}
