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
package org.apache.servicemix.sca.handler;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.sca.ScaServiceUnit;
import org.apache.tuscany.core.addressing.EndpointReference;
import org.apache.tuscany.core.message.Message;
import org.apache.tuscany.core.message.handler.MessageHandler;
import org.apache.tuscany.core.runtime.TuscanyModuleComponentContext;
import org.apache.tuscany.model.assembly.ExternalService;
import org.apache.tuscany.model.types.OperationType;
import org.apache.tuscany.model.types.java.JavaOperationType;
import org.osoa.sca.model.JbiBinding;

public class ExternalJbiServiceHandler implements MessageHandler {

	private TuscanyModuleComponentContext context;
	private OperationType type;
	private ExternalService externalService;
	private JbiBinding jbiBinding;
	private EndpointReference endpointReference;
	private ScaServiceUnit serviceUnit;
	
	public ExternalJbiServiceHandler(TuscanyModuleComponentContext context, 
									 OperationType type, 
									 ExternalService externalService, 
									 JbiBinding jbiBinding, 
									 EndpointReference endpointReference) {
		this.context = context;
		this.type = type;
		this.externalService = externalService;
		this.jbiBinding = jbiBinding;
		this.endpointReference = endpointReference;
		this.serviceUnit = ScaServiceUnit.getCurrentScaServiceUnit();
	}

	public boolean processMessage(Message message) {
		try {
			QName interfaceName;
			String[] parts = jbiBinding.getPort().split("#");
			if (parts.length > 1) {
				interfaceName = new QName(parts[0], parts[1]);
			} else {
				interfaceName = new QName(parts[0]);
			}
			
			Object payload = message.getPayload();
			if (payload instanceof Object[]) {
				payload = ((Object[]) payload)[0];
			}
			
			Method method = ((JavaOperationType) type).getJavaMethod();
			Class inputClass = method.getParameterTypes()[0];
			Class outputClass = method.getReturnType();
			JAXBContext context = JAXBContext.newInstance(inputClass, outputClass);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			context.createMarshaller().marshal(payload, baos);
			
			DeliveryChannel channel = serviceUnit.getComponent().getComponentContext().getDeliveryChannel();
			// TODO: in-only case ?
			InOut inout = channel.createExchangeFactory(interfaceName).createInOutExchange();
			NormalizedMessage in = inout.createMessage();
			inout.setInMessage(in);
			in.setContent(new StringSource(baos.toString()));
			boolean sent = channel.sendSync(inout);
			// TODO: check for error ?
			NormalizedMessage out = inout.getOutMessage();
			Object response = context.createUnmarshaller().unmarshal(out.getContent());
			message.setPayload(response);
			message.getCallbackChannel().send(message);
			inout.setStatus(ExchangeStatus.DONE);
			channel.send(inout);
			return false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
