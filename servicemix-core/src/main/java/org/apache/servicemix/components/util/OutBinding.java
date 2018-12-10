/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.MessageExchangeListener;

import javax.jbi.JBIException;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * A base class for bindings which process inbound JBI messages
 *
 * @version $Revision$
 */
public abstract class OutBinding extends ComponentSupport implements Runnable, MessageExchangeListener {
    private static final Log log = LogFactory.getLog(OutBinding.class);
    private AtomicBoolean stop = new AtomicBoolean(false);
    private Thread runnable;

    public OutBinding() {
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
            try {
                NormalizedMessage message = getInMessage(exchange);
                process(exchange, message);
            }
            catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Exchange failed", e);
                }
                fail(exchange, e);
            }
        }
    }

    /**
     * Runnable implementation
     */
    public void run() {
        try {
            DeliveryChannel deliveryChannel = getDeliveryChannel();
            while (!stop.get()) {
                MessageExchange exchange = deliveryChannel.accept();
                if (exchange != null) {
                    try {
                        onMessageExchange(exchange);
                    } catch (MessagingException e) {
                        log.error("MessageExchange processing failed", e);
                    }
                }
            }
        }
        catch (MessagingException e) {
            log.error("run failed", e);
        }
    }

    /**
     * shutdown
     *
     * @throws JBIException
     */
    public void shutDown() throws JBIException {
    }

    /**
     * stop
     *
     * @throws JBIException
     */
    public void stop() throws JBIException {
        stop.compareAndSet(true, false);
        if (runnable != null) {
            runnable.interrupt();
            try {
                runnable.join();
            } catch (InterruptedException e) {
                log.warn("Unable to stop component polling thread", e);
            }
            runnable = null;
        }
    }

    /**
     * start
     */
    public void start() throws JBIException {
        if (stop.compareAndSet(false, true)) {
            runnable = new Thread(this);
            runnable.setDaemon(true);
            runnable.start();
        }
    }

    /**
     * Process incoming exchange.
     * The exchange is in the ACTIVE state.
     * The method should end by a call to done() or answer().
     * When an exception is thrown, the fail() method will be called.
     *
     * @param messageExchange the exchange to process
     * @param message the input message of the exchange
     * @throws Exception if an error occurs
     */
    protected abstract void process(MessageExchange messageExchange, NormalizedMessage message) throws Exception;
}
