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
package org.apache.servicemix.components.util;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple tracing component which can be placed inside a pipeline
 * to trace the message exchange though the component.
 *
 * @version $Revision$
 */
public class TraceComponent extends ComponentSupport implements MessageExchangeListener {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(TraceComponent.class);

    private SourceTransformer sourceTransformer = new SourceTransformer();

    public Logger getLog() {
        return LOGGER;
    }

    public SourceTransformer getSourceTransformer() {
        return sourceTransformer;
    }

    public void setSourceTransformer(SourceTransformer sourceTransformer) {
        this.sourceTransformer = sourceTransformer;
    }

    /** 
     * Intercepts the {@link MessageExchange} to output the message and its 
     * properties for debugging purposes. 
     * 
     * @param exchange A JBI {@link MessageExchange} between two endpoints
     */
    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        // lets dump the incoming message  
        NormalizedMessage message = exchange.getMessage("in");
        if (message == null) {
            LOGGER.warn("Received null message from exchange: {}", exchange);
        } else {
            LOGGER.info("Exchange: {} received IN message: {}", exchange, message);
            try {
                LOGGER.info("Body is: {}", sourceTransformer.toString(message.getContent()));
            } catch (TransformerException e) {
                LOGGER.error("Failed to turn message body into text: {}", e.getMessage(), e);
            }
            outputProperties(message);
        }
        done(exchange);
    }
    
    /**
     * Outputs the properties on the {@link NormalizedMessage}. Properties of 
     * type {@link Source} are transformed to a {@link String} before 
     * being output.
     *
     * @param message The {@link NormalizedMessage} to be processed
     */
    @SuppressWarnings("unchecked")
    protected void outputProperties(NormalizedMessage message) {
        // Loop over all the properties on the normalized message 
        for (Object o : message.getPropertyNames()) {
            // NormalizedMessage API does not use generics. This interface is
            // written in Java older than 5.0. On the basis of other methods and
            // the default implementation of this interface we can assume that
            // only String keys are allowed.
            String key = (String) o;
            try {
                Object contents = message.getProperty(key);
                // Is this the only value type that we would like to treat
                // differently? The default behavior is to invoke toString() on
                // the object.
                if (contents instanceof Source) {
                    contents = getSourceTransformer().toString((Source) contents);
                }

                LOGGER.info("Value for property '{}' is: {}", key, contents);
            } catch (TransformerException e) {
                LOGGER.error("Failed to turn property '{}' value into text", key, e);
            }
        }
    }

}
