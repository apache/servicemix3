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
package org.servicemix.jbi.messaging;

import org.servicemix.jbi.jaxp.StringSource;
import org.w3c.dom.Node;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

/**
 * Default implementation of {@link Marshaler} which will pass through String
 * objects as XML content, DOM objects or Stream objects, otherwise the payload
 * is stored in a message property.
 * 
 * @version $Revision$
 */
public class DefaultMarshaler implements Marshaler {

    private Marshaler parent;

    public DefaultMarshaler() {
    }

    public DefaultMarshaler(Marshaler parent) {
        this.parent = parent;
    }

    public Marshaler getParent() {
        return parent;
    }

    public void marshal(MessageExchange exchange, NormalizedMessage message, Object body) throws MessagingException {
        if (body instanceof Source) {
            message.setContent((Source) body);
        }
        else {
            message.setProperty(BODY, body);
            Source content = asContent(message, body);
            message.setContent(content);
        }
    }

    public Object unmarshal(MessageExchange exchange, NormalizedMessage message) throws MessagingException {
        Object answer = message.getProperty(BODY);
        if (answer == null) {
            if (parent != null) {
                answer = parent.unmarshal(exchange, message);
            }
            if (answer == null) {
                answer = defaultUnmarshal(exchange, message);
            }
        }
        return answer;
    }

    protected Object defaultUnmarshal(MessageExchange exchange, NormalizedMessage message) {
        Source content = message.getContent();
        if (content instanceof DOMSource) {
            DOMSource source = (DOMSource) content;
            return source.getNode();
        }
        return content;
    }

    protected Source asContent(NormalizedMessage message, Object body) {
        if (body instanceof Source) {
            return (Source) body;
        }
        else if (body instanceof String) {
            // lets assume String is the XML to send
            return new StringSource((String) body);
        }
        else if (body instanceof Node) {
            return new DOMSource((Node) body);
        }
        return null;
    }
}
