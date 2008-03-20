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

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.RobustInOnly;

import org.apache.servicemix.jbi.resolver.URIResolver;

/**
 * 
 * @version $Revision$
 */
public class DefaultDestination implements Destination {

    private ServiceMixClient client;
    private String destinationUri;

    public DefaultDestination(ServiceMixClient client, String destinationUri) throws MessagingException {
        this.client = client;
        this.destinationUri = destinationUri;
    }

    public InOnly createInOnlyExchange() throws MessagingException {
        InOnly answer = client.createInOnlyExchange();
        configure(answer);
        return answer;
    }

    public InOptionalOut createInOptionalOutExchange() throws MessagingException {
        InOptionalOut answer = client.createInOptionalOutExchange();
        configure(answer);
        return answer;
    }

    public InOut createInOutExchange() throws MessagingException {
        InOut answer = client.createInOutExchange();
        configure(answer);
        return answer;
    }

    public RobustInOnly createRobustInOnlyExchange() throws MessagingException {
        RobustInOnly answer = client.createRobustInOnlyExchange();
        configure(answer);
        return answer;
    }

    public Message createInOnlyMessage() throws MessagingException {
        return (Message) createInOnlyExchange().getInMessage();
    }
    
    protected void configure(MessageExchange exchange) throws MessagingException {
        URIResolver.configureExchange(exchange, client.getContext(), destinationUri);
    }

}
