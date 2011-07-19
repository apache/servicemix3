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
package org.apache.servicemix.components.splitter;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.SenderComponent;

public class SplitterComponentTest extends TestCase {

	private JBIContainer jbiContainer;

	protected SenderComponent sender;

	protected ReceiverComponent receiver;

	protected SplitterComponent fec;

	protected void setUp() throws Exception {
		jbiContainer = new JBIContainer();
		jbiContainer.setUseMBeanServer(false);
		jbiContainer.setCreateMBeanServer(false);
		jbiContainer.setFlowName("seda");
		jbiContainer.init();
		jbiContainer.start();
		setUpReceiver();
		setUpSender();
		setUpComponent();
	}

	protected QName getComponentService() {
		return new QName("http://www.neogrid.com.br", "component");
	}

	private void setUpSender() throws Exception {
		sender = new SenderComponent();
		ActivationSpec as = new ActivationSpec(SenderComponent.ENDPOINT, sender);
		as.setService(SenderComponent.SERVICE);
		as.setDestinationService(getComponentService());
		jbiContainer.activateComponent(as);
	}

	private void setUpReceiver() throws Exception {
		receiver = new ReceiverComponent();
		ActivationSpec as = new ActivationSpec(ReceiverComponent.ENDPOINT,
				receiver);
		as.setService(ReceiverComponent.SERVICE);
		jbiContainer.activateComponent(as);
	}

	private void setUpComponent() throws Exception {
		fec = new SplitterComponent();
		fec.setNodePath("");
		ActivationSpec as = new ActivationSpec("component", fec);
		as.setService(getComponentService());
		as.setDestinationService(ReceiverComponent.SERVICE);
		jbiContainer.activateComponent(as);
	}

	public void testForEach() throws Exception {
		fec.setNodePath("ROOT/DOC[@att = 2]");
		sender
				.setMessage("<ROOT><DOC att=\"1\"/><DOC att=\"2\"/><DOC att=\"2\"/></ROOT>");
		sender.sendMessages(1);
		Thread.sleep(1000);
		assertEquals(2, receiver.getMessageList().getMessageCount());
	}

	protected void tearDown() throws Exception {
		if (jbiContainer != null)
			jbiContainer.shutDown();
	}

}
