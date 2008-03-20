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
package org.apache.servicemix.tck;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;

/**
 * @version $Revision$
 */
public class ExamplePojoTest extends TestCase {

    private static final int NUM_MESSAGES = 10;

    protected JBIContainer container = new JBIContainer();
    protected SenderComponent sender;
    protected ReceiverComponent receiver;

    public void testInOnly() throws Exception {
        sender.sendMessages(NUM_MESSAGES);
        receiver.getMessageList().assertMessagesReceived(NUM_MESSAGES);
    }

    protected void setUp() throws Exception {
        container.setEmbedded(true);
        container.init();
        container.start();
        receiver = new ReceiverComponent();
        sender = new SenderComponent();
        sender.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));

        container.activateComponent(new ActivationSpec("sender", sender));
        container.activateComponent(new ActivationSpec("receiver", receiver));
    }

    protected void tearDown() throws Exception {
        container.shutDown();
    }
}
