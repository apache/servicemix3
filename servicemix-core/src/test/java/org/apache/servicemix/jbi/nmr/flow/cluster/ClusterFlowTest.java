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
package org.apache.servicemix.jbi.nmr.flow.cluster;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.nmr.flow.cluster.ClusterFlow;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.SenderComponent;

import java.io.File;

import junit.framework.TestCase;

/**
 *
 * ClusterFlowTest
 */
public class ClusterFlowTest extends TestCase {
    JBIContainer senderContainer = new JBIContainer();
    JBIContainer receiverContainer = new JBIContainer();
    private SenderComponent sender;
    private ReceiverComponent receiver;
    private static final int NUM_MESSAGES = 10;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
       
        senderContainer.setName("senderContainer");
        senderContainer.setFlowName("cluster");
        senderContainer.init();
        senderContainer.start();
        ClusterFlow senderFlow = (ClusterFlow) senderContainer.getFlow();
        
        
        receiverContainer.setName("receiverContainer");
        receiverContainer.setFlowName("cluster");
        receiverContainer.init();
        receiverContainer.start();
        ClusterFlow receiverFlow = (ClusterFlow)receiverContainer.getFlow();

        receiver = new ReceiverComponent();
        sender = new SenderComponent();
        sender.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));

        senderContainer.activateComponent(new ActivationSpec("sender", sender));
        receiverContainer.activateComponent(new ActivationSpec("receiver", receiver));

        assertTrue("Sender cluster complete",senderFlow.waitForClusterToComplete(1, 10000));
        assertTrue("Receiver cluster complete",receiverFlow.waitForClusterToComplete(1,10000));
    }
    
    protected void tearDown() throws Exception{
        super.tearDown();
        FileUtil.deleteFile(new File("ActiveMQ"));
    }
    
    public void testInOnly() throws Exception {
      sender.sendMessages(NUM_MESSAGES);
      Thread.sleep(3000);
      receiver.getMessageList().assertMessagesReceived(NUM_MESSAGES);
    }
}
