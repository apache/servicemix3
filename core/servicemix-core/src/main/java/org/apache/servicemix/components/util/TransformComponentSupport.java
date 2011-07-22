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

import java.io.IOException;
import java.net.URI;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.jbi.util.MessageUtil;
import org.apache.servicemix.store.Store;
import org.apache.servicemix.store.StoreFactory;
import org.apache.servicemix.store.memory.MemoryStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A useful base class for a transform component.
 *
 * @version $Revision$
 */
public abstract class TransformComponentSupport extends ComponentSupport implements MessageExchangeListener {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(TransformComponentSupport.class);
    
    private String correlation;
    
    private boolean copyProperties = true;
    private boolean copyAttachments = true;
    private StoreFactory storeFactory;
    private Store store;

    protected TransformComponentSupport() {
    }

    protected TransformComponentSupport(QName service, String endpoint) {
        super(service, endpoint);
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        // Handle consumer exchanges && non-active RobustInOnly provider exchanges
        if (exchange.getRole() == MessageExchange.Role.CONSUMER
                || exchange.getProperty(correlation) != null) {
            processOngoingExchange(exchange);
        } else if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
            processFirstExchange(exchange);
        }
    }

    protected void processFirstExchange(MessageExchange exchange) {
        try {
            MessageExchange outExchange = null;
            NormalizedMessage in = getInMessage(exchange);
            NormalizedMessage out;
            if (isInAndOut(exchange)) {
                out = exchange.createMessage();
            } else {
                URI pattern = exchange.getPattern();
                outExchange = getExchangeFactory().createExchange(pattern);
                outExchange.setProperty(JbiConstants.SENDER_ENDPOINT, getService() + ":" + getEndpoint());
                // Set correlations
                outExchange.setProperty(correlation, exchange.getExchangeId());
                exchange.setProperty(correlation, outExchange.getExchangeId());
                String processCorrelationId = (String)exchange.getProperty(JbiConstants.CORRELATION_ID);
                if (processCorrelationId != null) {
                    outExchange.setProperty(JbiConstants.CORRELATION_ID, processCorrelationId);
                }
                out = outExchange.createMessage();
            }
            boolean txSync = exchange.isTransacted() && Boolean.TRUE.equals(exchange.getProperty(JbiConstants.SEND_SYNC));
            copyPropertiesAndAttachments(exchange, in, out);
            if (transform(exchange, in, out)) {
                if (isInAndOut(exchange)) {
                    exchange.setMessage(out, "out");
                    if (txSync) {
                        sendSync(exchange);
                    } else {
                        send(exchange);
                    }
                } else {
                    outExchange.setMessage(out, "in");
                    if (txSync) {
                        sendSync(outExchange);
                        if (outExchange.getStatus() == ExchangeStatus.DONE) {
                            done(exchange);
                        } else if (outExchange.getStatus() == ExchangeStatus.ERROR) {
                            fail(exchange, outExchange.getError());
                        } else if (outExchange.getFault() != null) {
                            Fault fault = MessageUtil.copyFault(outExchange);
                            done(outExchange);
                            MessageUtil.transferToFault(fault, exchange);
                            sendSync(exchange);
                        } else {
                            done(outExchange);
                            throw new IllegalStateException("Exchange status is " + ExchangeStatus.ACTIVE
                                    + " but has no Out nor Fault message");
                        }
                    } else {
                        store.store(exchange.getExchangeId(), exchange);
                        try {
                            send(outExchange);
                        } catch (Exception e) {
                            store.load(exchange.getExchangeId());
                            throw e;
                        }
                    }
                }
            } else {
                exchange.setStatus(ExchangeStatus.DONE);
                send(exchange);
            }
        } catch (Exception e) {
            try {
                fail(exchange, e);
            } catch (Exception e2) {
                LOGGER.warn("Unable to handle error: {}", e2.getMessage(), e2);
                LOGGER.debug("Original error: {}", e.getMessage(), e);
            }
        }
    }

    protected void processOngoingExchange(MessageExchange exchange) {
        MessageExchange original = null;
        String id = null;
        try {
            id = (String) exchange.getProperty(correlation);
            original = (MessageExchange) store.load(id);
        } catch (Exception e) {
            // We can't do, so just return
            return;
        }
        try {
            if (exchange.getStatus() == ExchangeStatus.DONE) {
                done(original);
            // Reproduce ERROR status to the other side
            } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
                fail(original, exchange.getError());
            // Reproduce faults to the other side and listeners
            } else if (exchange.getFault() != null) {
                store.store(exchange.getExchangeId(), exchange);
                try {
                    MessageUtil.transferTo(exchange, original, "fault");
                    send(original);
                } catch (Exception e) {
                    store.load(exchange.getExchangeId());
                    throw e;
                }
            // Reproduce answers to the other side
            } else if (exchange.getMessage("out") != null) {
                store.store(exchange.getExchangeId(), exchange);
                try {
                    MessageUtil.transferTo(exchange, original, "out");
                    send(original);
                } catch (Exception e) {
                    store.load(exchange.getExchangeId());
                    throw e;
                }
            } else {
                throw new IllegalStateException("Exchange status is " + ExchangeStatus.ACTIVE
                        + " but has no Out nor Fault message");
            }
        } catch (Exception e) {
            LOGGER.debug("Original error: {}", e.getMessage(), e);
        }
    }
    

    // Implementation methods
    //-------------------------------------------------------------------------

    
    /**
     * Transforms the given out message
     */
    protected abstract boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception;

    @Override
    protected void init() throws JBIException {
        super.init();
        if (store == null) {
            if (storeFactory == null) {
                storeFactory = new MemoryStoreFactory();
            }
            try {
                store = storeFactory.open(getService().toString() + getEndpoint());
            } catch (IOException e) {
                throw new JBIException("Unable to open storeFactory" + e.getMessage(), e);
            }
        }
        correlation = "TransformComponentSupport.Correlation." + getService() + "." + getEndpoint();
        
    }

    public boolean isCopyProperties() {
        return copyProperties;
    }


    public void setCopyProperties(boolean copyProperties) {
        this.copyProperties = copyProperties;
        if (getMessageTransformer() instanceof CopyTransformer) {
            ((CopyTransformer) getMessageTransformer()).setCopyProperties(copyProperties);
        }
    }


    public boolean isCopyAttachments() {
        return copyAttachments;
    }


    public void setCopyAttachments(boolean copyAttachments) {
        this.copyAttachments = copyAttachments;
        if (getMessageTransformer() instanceof CopyTransformer) {
            ((CopyTransformer) getMessageTransformer()).setCopyAttachments(copyAttachments);
        }
    }


    /**
     * If enabled the properties and attachments are copied to the destination message
     */
    protected void copyPropertiesAndAttachments(MessageExchange exchange, NormalizedMessage in, 
                                                NormalizedMessage out) throws MessagingException {
        if (isCopyProperties()) {
            CopyTransformer.copyProperties(in, out);
        }
        if (isCopyAttachments()) {
            CopyTransformer.copyAttachments(in, out);
        }
    }
    
    public StoreFactory getStoreFactory() {
        return storeFactory;
    }

    public void setStoreFactory(StoreFactory storeFactory) {
        this.storeFactory = storeFactory;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }
    
}
