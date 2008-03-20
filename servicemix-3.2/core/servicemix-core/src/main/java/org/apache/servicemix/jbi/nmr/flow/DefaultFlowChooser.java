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
package org.apache.servicemix.jbi.nmr.flow;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;

public class DefaultFlowChooser implements FlowChooser {

    public DefaultFlowChooser() {
    }

    public Flow chooseFlow(Flow[] flows, MessageExchange exchange) throws MessagingException {
        // Check if flow was specified
        String flow = (String) exchange.getProperty(JbiConstants.FLOW_PROPERTY_NAME);
        if (flow != null) {
            Flow foundFlow = null;
            for (int i = 0; i < flows.length; i++) {
                if (flows[i].getName().equalsIgnoreCase(flow)) {
                    foundFlow = flows[i];
                    break;
                }
            }
            if (foundFlow == null) {
                throw new MessagingException("Flow '" + flow + "' was specified but not found");
            }
            if (foundFlow.canHandle(exchange)) {
                return foundFlow;
            } else {
                throw new MessagingException("Flow '" + flow + "' was specified but not able to handle exchange");
            }
        }
        // Check against flow capabilities
        for (int i = 0; i < flows.length; i++) {
            if (flows[i].canHandle(exchange)) {
                ((MessageExchangeImpl) exchange).getPacket().setProperty(JbiConstants.FLOW_PROPERTY_NAME, flows[i].getName());
                return flows[i];
            }
        }
        return null;
    }

}
