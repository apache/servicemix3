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
package org.servicemix.components.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.servicemix.MessageExchangeListener;
import org.servicemix.jbi.jaxp.SourceTransformer;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.TransformerException;

/**
 * A simple tracing component which can be placed inside a pipeline
 * to trace the message exchange though the component.
 *
 * @version $Revision$
 */
public class TraceComponent extends ComponentSupport implements MessageExchangeListener {

    private Log log = LogFactory.getLog(TraceComponent.class);

    private SourceTransformer sourceTransformer = new SourceTransformer();

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public SourceTransformer getSourceTransformer() {
        return sourceTransformer;
    }

    public void setSourceTransformer(SourceTransformer sourceTransformer) {
        this.sourceTransformer = sourceTransformer;
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        // lets dump the incoming message
        NormalizedMessage message = exchange.getMessage("in");
        if (message == null) {
            log.warn("Received null message from exchange: " + exchange);
        }
        else {
            log.info("Exchange: " + exchange + " received IN message: " + message);
            try {
                log.info("Body is: " + sourceTransformer.toString(message.getContent()));
            }
            catch (TransformerException e) {
                log.error("Failed to turn message body into text: " + e, e);
            }
        }
        done(exchange);
    }
}
