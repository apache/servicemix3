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
package org.apache.servicemix.common;

import javax.jbi.messaging.MessageExchange;
import javax.transaction.Status;

import org.apache.servicemix.MessageExchangeListener;

/**
 * Base class for life cycle management of components.
 * This lifecycle uses Push delivery by implementing MessageExchangeListerner interface
 * 
 * @author Guillaume Nodet
 * @version $Revision$
 * @since 3.0
 */
public class BaseLifeCycle extends AsyncBaseLifeCycle implements MessageExchangeListener {

    public BaseLifeCycle(BaseComponent component) {
        super(component);
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.common.AsyncBaseLifeCycle#onMessageExchange(javax.jbi.messaging.MessageExchange)
     */
    public void onMessageExchange(MessageExchange exchange) {
        try {
            processExchange(exchange);
        } catch (Exception e) {
            logger.error("Error processing exchange " + exchange, e);
            try {
                // If we are transacted and this is a runtime exception
                // try to mark transaction as rollback
                if (transactionManager != null && 
                    transactionManager.getStatus() == Status.STATUS_ACTIVE && 
                    exceptionShouldRollbackTx(e)) {
                    transactionManager.setRollbackOnly();
                }
                exchange.setError(e);
                channel.send(exchange);
            } catch (Exception inner) {
                logger.error("Error setting exchange status to ERROR", inner);
            }
        }
    }
    
}
