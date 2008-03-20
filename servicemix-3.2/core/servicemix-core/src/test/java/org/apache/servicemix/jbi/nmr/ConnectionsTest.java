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
package org.apache.servicemix.jbi.nmr;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.Sender;
import org.apache.servicemix.tck.SenderComponent;

public class ConnectionsTest extends TestCase {

    private JBIContainer container;
    
    protected void setUp() throws Exception {
        container = new JBIContainer();
        container.setEmbedded(true);
        container.init();
        container.start();
    }
    
    protected void tearDown() throws Exception {
        container.shutDown();
    }
    
    public void testEndpointConnection() throws Exception {
        Receiver receiver = new ReceiverComponent();
        ActivationSpec asReceiver = new ActivationSpec();
        asReceiver.setComponent(receiver);
        asReceiver.setService(new QName("service"));
        asReceiver.setEndpoint("endpoint");
        
        Sender sender = new SenderComponent();
        ActivationSpec asSender = new ActivationSpec();
        asSender.setComponent(sender);
        asSender.setDestinationService(new QName("service"));
        asSender.setDestinationEndpoint("linkedEndpoint");
        
        container.activateComponent(asReceiver);
        container.activateComponent(asSender);
        container.getRegistry().registerEndpointConnection(new QName("service"), "linkedEndpoint", new QName("service"), "endpoint", null);
        
        sender.sendMessages(1);
        receiver.getMessageList().assertMessagesReceived(1);
    }
    
    public void testInterfaceConnection() throws Exception {
        Receiver receiver = new ReceiverComponent();
        ActivationSpec asReceiver = new ActivationSpec();
        asReceiver.setComponent(receiver);
        asReceiver.setService(new QName("service"));
        asReceiver.setEndpoint("endpoint");
        
        Sender sender = new SenderComponent();
        ActivationSpec asSender = new ActivationSpec();
        asSender.setComponent(sender);
        asSender.setDestinationInterface(new QName("interface"));
        
        container.activateComponent(asReceiver);
        container.activateComponent(asSender);
        container.getRegistry().registerInterfaceConnection(new QName("interface"), new QName("service"), "endpoint");
        
        sender.sendMessages(1);
        receiver.getMessageList().assertMessagesReceived(1);
    }
    
}
