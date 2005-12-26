/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Copyright 2005 Exist Software Engineering. All rights reserved.
 */
package org.apache.servicemix.components.mule;

import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;

import javax.jbi.messaging.NormalizedMessage;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;

/**
 * ServiceMixMessageAdapter *
 *
 * @version $Revision$
 */
public class JBIMessageAdapter extends AbstractMessageAdapter {
    private NormalizedMessage message;

    /**
     * Construct a ServiceMixAdapter
     *
     * @param object
     * @throws MessagingException
     */
    public JBIMessageAdapter(Object object) throws MessagingException {
        setMessage(object);
    }

    /**
     * @return the payload as a String
     * @throws Exception
     */
    public String getPayloadAsString() throws Exception {
        return message != null ? message.toString() : null;
    }

    /**
     * @return the payload as bytes
     * @throws Exception
     */
    public byte[] getPayloadAsBytes() throws Exception {
        byte[] result = null;
        if (message != null && message instanceof Serializable) {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
            objOut.writeObject(message);
            objOut.flush();
            bytesOut.flush();
            result = bytesOut.toByteArray();
        }
        return result;
    }

    /**
     * @return the payload
     */
    public Object getPayload() {
        return message;
    }

    /**
     * Set the message
     *
     * @param object expects an instance of javax.jbi.NormalizedMessage
     * @throws MessagingException if null of an unexpected type
     */
    public void setMessage(Object object) throws MessagingException {
        if (object != null && object instanceof NormalizedMessage) {
            this.message = (NormalizedMessage) object;
        }
        else {
            throw new MessageTypeNotSupportedException(object, getClass());
        }
        for (Iterator i = message.getPropertyNames().iterator(); i.hasNext();) {
            String name = i.next().toString();
            properties.put(name, message.getProperty(name));
        }
    }
}