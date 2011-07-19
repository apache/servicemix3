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
package org.apache.servicemix.components.pojo;

import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.jaxp.StringSource;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * @version $Revision$
 */
// START SNIPPET: send
public class PojoSender {

    private ServiceMixClient client;

    public void sendMessages(int count) throws MessagingException {
        for (int i = 0; i < count; i++) {
            InOnly exchange = client.createInOnlyExchange();
            NormalizedMessage message = exchange.getInMessage();

            message.setProperty("id", new Integer(i));
            message.setContent(new StringSource("<example id='" + i + "'/>"));

            client.send(exchange);
        }
    }

    public ServiceMixClient getClient() {
        return client;
    }

    public void setClient(ServiceMixClient client) {
        this.client = client;
    }

}
// END SNIPPET: send
