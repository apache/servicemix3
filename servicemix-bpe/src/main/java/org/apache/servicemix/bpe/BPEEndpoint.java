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
package org.apache.servicemix.bpe;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.RobustInOnly;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.transform.dom.DOMSource;

import org.apache.servicemix.common.Endpoint;
import org.apache.servicemix.common.ExchangeProcessor;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.w3c.dom.Document;

import org.apache.ode.bped.EventDirector;
import org.apache.ode.client.IFormattableValue;
import org.apache.ode.event.BPELStaticKey;
import org.apache.ode.event.IResponseMessage;
import org.apache.ode.event.SimpleRequestMessageEvent;
import org.apache.ode.interaction.IInteraction;
import org.apache.ode.interaction.InvocationFactory;
import org.apache.ode.interaction.XMLInteractionObject;
import org.apache.ode.scope.service.BPRuntimeException;

public class BPEEndpoint extends Endpoint implements ExchangeProcessor {

    protected ServiceEndpoint activated;
    protected DeliveryChannel channel;
    protected SourceTransformer transformer = new SourceTransformer();
	
	public Role getRole() {
		return Role.PROVIDER;
	}

	public void activate() throws Exception {
        logger = this.serviceUnit.getComponent().getLogger();
        ComponentContext ctx = this.serviceUnit.getComponent().getComponentContext();
        activated = ctx.activateEndpoint(service, endpoint);
        channel = ctx.getDeliveryChannel();
	}

	public void deactivate() throws Exception {
        ServiceEndpoint ep = activated;
        activated = null;
        ComponentContext ctx = this.serviceUnit.getComponent().getComponentContext();
        ctx.deactivateEndpoint(ep);
	}

	public ExchangeProcessor getProcessor() {
		return this;
	}

	public void process(MessageExchange exchange) throws Exception {
        if (exchange.getStatus() == ExchangeStatus.DONE) {
            return;
        } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
            exchange.setStatus(ExchangeStatus.DONE);
            channel.send(exchange);
            return;
        }
		BPELStaticKey bsk = new BPELStaticKey();
		bsk.setTargetNamespace(getInterfaceName().getNamespaceURI());
		bsk.setPortType(getInterfaceName().getLocalPart());
		if (exchange.getOperation() != null) {
			bsk.setOperation(exchange.getOperation().getLocalPart());
		}
		SimpleRequestMessageEvent msg = new SimpleRequestMessageEvent();
		msg.setStaticKey(bsk);
		XMLInteractionObject interaction = new XMLInteractionObject();
		interaction.setDocument((Document) transformer.toDOMNode(exchange.getMessage("in")));
		msg.setPart(BPEComponent.PART_PAYLOAD, interaction);
        
        EventDirector ed = ((BPEComponent) getServiceUnit().getComponent()).getEventDirector();
        try {
            IResponseMessage response = ed.sendEvent(msg, true);
            IInteraction payload = response.getPart(BPEComponent.PART_PAYLOAD);
            if (response.getFault() != null) {
                Exception e = response.getFault().getFaultException();
                if (e != null) {
                    throw e;
                }
                // TODO: handle simple fault
                throw new BPRuntimeException(response.getFault().getFaultString(), "");
            } else if (exchange instanceof InOnly || exchange instanceof RobustInOnly) {
                if (payload != null) {
                    throw new UnsupportedOperationException("Did not expect return value for in-only or robust-in-only");
                }
                exchange.setStatus(ExchangeStatus.DONE);
                channel.send(exchange);
            } else if (exchange instanceof InOptionalOut) {
                if (payload == null) {
                    exchange.setStatus(ExchangeStatus.DONE);
                    channel.send(exchange);
                } else {
                    IFormattableValue value = (IFormattableValue) payload.invoke(InvocationFactory.newInstance().createGetObjectInvocation());
                    Document doc = (Document) value.getValueAs(Document.class);
                    NormalizedMessage out = exchange.createMessage();
                    out.setContent(new DOMSource(doc));
                    exchange.setMessage(out, "out");
                    channel.send(exchange);
                }
            } else if (exchange instanceof InOut) {
                if (payload == null) {
                    throw new UnsupportedOperationException("Expected return data for in-out"); 
                }
                IFormattableValue value = (IFormattableValue) payload.invoke(InvocationFactory.newInstance().createGetObjectInvocation());
                Document doc = (Document) value.getValueAs(Document.class);
                NormalizedMessage out = exchange.createMessage();
                out.setContent(new DOMSource(doc));
                exchange.setMessage(out, "out");
                channel.send(exchange);
            } else {
                throw new UnsupportedOperationException("Unhandled mep: " + exchange.getPattern());
            }
        } catch (BPRuntimeException e) {
            IInteraction payload = (IInteraction) e.getPartMessage(BPEComponent.PART_PAYLOAD);
            if (payload != null) {
                Fault fault = exchange.createFault();
                IFormattableValue value = (IFormattableValue) payload.invoke(InvocationFactory.newInstance().createGetObjectInvocation());
                Document doc = (Document) value.getValueAs(Document.class);
                fault.setContent(new DOMSource(doc));
                exchange.setFault(fault);
            } else {
                exchange.setError(e);
            }
            exchange.setStatus(ExchangeStatus.ERROR);
            channel.send(exchange);
        }
	}

	public void start() throws Exception {
	}

	public void stop() throws Exception {
	}


}
