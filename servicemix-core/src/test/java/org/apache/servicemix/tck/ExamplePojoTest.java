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
package org.apache.servicemix.tck;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.SenderComponent;

import junit.framework.TestCase;

/**
 * @version $Revision$
 */
public class ExamplePojoTest extends TestCase {
    protected JBIContainer container = new JBIContainer();
    private SenderComponent sender;
    private ReceiverComponent receiver;
    protected int NUM_MESSAGES = 10;

    public void testInOnly() throws Exception {
        sender.sendMessages(NUM_MESSAGES);
        receiver.getMessageList().assertMessagesReceived(NUM_MESSAGES);
    }

    protected void setUp() throws Exception {
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
