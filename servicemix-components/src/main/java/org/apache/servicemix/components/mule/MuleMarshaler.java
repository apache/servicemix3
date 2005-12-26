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
package org.apache.servicemix.components.mule;

import org.apache.servicemix.components.util.MarshalerSupport;
import org.apache.servicemix.jbi.messaging.PojoMarshaler;
import org.mule.impl.MuleMessage;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Revision$
 */
public class MuleMarshaler extends MarshalerSupport {

    /**
     * Populates the normalized message from the Mule event.
     */
    public void populateNormalizedMessage(NormalizedMessage message, UMOEvent event) {
        Map properties = event.getProperties();
        for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = (String) entry.getKey();
            message.setProperty(name, entry.getValue());
        }

        message.setProperty("org.apache.servicemix.mule.event", event);

        Object body = event.getMessage().getPayload();
        message.setProperty(PojoMarshaler.BODY, body);
    }

    /**
     * Creates a new Mule message from the given JBI message and body.
     */
    public UMOMessage createMuleMessage(MessageExchange exchange, NormalizedMessage message, Object body) {
        Map properties = new HashMap();
        for (Iterator iter = message.getPropertyNames().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            properties.put(name, message.getProperty(name));
        }
        return new MuleMessage(body, properties);
    }
}
