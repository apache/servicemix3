/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.common;

import org.apache.commons.logging.Log;
import org.apache.servicemix.executors.Executor;

import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

/**
 * Represents an extended JBI Component implementation which exposes some extra features
 *
 * @version $Revision$
 */
public interface ServiceMixComponent extends Component {

    /**
     * @return Returns the logger.
     */
    public Log getLogger();

    /**
     * @return Returns the registry.
     */
    public Registry getRegistry();

    /**
     * @return Returns the executor for this component
     */
    public Executor getExecutor();

    /**
     * @return Returns the components context
     */
    public ComponentContext getComponentContext();

    /**
     * @return Returns the name of the component
     */
    public String getComponentName();
    
    /**
     * Sends a consumer exchange from the given endpoint. 
     * 
     * @param exchange the exchange to send
     * @param endpoint the endpoint sending the exchange
     * @throws MessagingException
     */
    public void sendConsumerExchange(MessageExchange exchange, Endpoint endpoint) throws MessagingException;
}
