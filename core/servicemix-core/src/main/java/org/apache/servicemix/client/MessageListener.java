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
package org.apache.servicemix.client;

import javax.jbi.messaging.MessageExchange;

/**
 * A listener which is called when a message is available to be processed.
 * 
 * @version $Revision: $
 */
public interface MessageListener {

    /**
     * Processes the message. By the completion of this method call, without exceptions
     * being thrown the message is assumed to be processed unless the status is updated
     * on the exchange.
     * 
     * @param exchange
     * @param message
     * @throws Exception if the message could not be processed correctly
     */
    void onMessage(MessageExchange exchange, Message message) throws Exception;
}
