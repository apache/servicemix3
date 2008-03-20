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
package org.apache.servicemix.components.util;

import javax.jbi.JBIException;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.jbi.MissingPropertyException;
import org.apache.servicemix.jbi.NoServiceAvailableException;

/**
 * This component acts as an InOnly component which pipelines a request/response (InOut) to a service
 * then forwards the response onto an InOut component.
 *
 * @version $Revision$
 */
public class PipelineComponent extends ComponentSupport implements MessageExchangeListener {
    private ServiceEndpoint requestResponseEndpoint;
    private ServiceEndpoint outputEndpoint;
    private QName requestResponseServiceName;
    private QName outputEndpointServiceName;

    public PipelineComponent() {
    }

    public PipelineComponent(QName service, String endpoint) {
        super(service, endpoint);
    }

    public void start() throws JBIException {
        super.start();

        if (requestResponseEndpoint == null) {
            if (requestResponseServiceName == null) {
                throw new MissingPropertyException("requestResponseServiceName");
            }
            requestResponseEndpoint = chooseEndpoint(requestResponseServiceName);

        }
        if (outputEndpoint == null) {
            if (outputEndpointServiceName == null) {
                throw new MissingPropertyException("outputEndpointServiceName");
            }
            outputEndpoint = chooseEndpoint(outputEndpointServiceName);
        }
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        // Skip done exchanges
        if (exchange.getStatus() == ExchangeStatus.DONE) {
            return;
        // Handle error exchanges
        } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
            return;
        }

        // lets create an endpoint
        DeliveryChannel deliveryChannel = getDeliveryChannel();
        MessageExchangeFactory rpcFactory = deliveryChannel.createExchangeFactory(requestResponseEndpoint);
        InOut rpc = rpcFactory.createInOutExchange();
        rpc.setInMessage(exchange.getMessage("in"));
        boolean answer = deliveryChannel.sendSync(rpc);

        MessageExchangeFactory outputFactory = deliveryChannel.createExchangeFactory(outputEndpoint);
        InOnly inOnly = outputFactory.createInOnlyExchange();

        if (answer) {
            inOnly.setInMessage(rpc.getOutMessage());
            deliveryChannel.send(inOnly);
            done(exchange);
        } else if (!(exchange instanceof InOnly)) {
            inOnly.setError(rpc.getError());
            Fault fault = rpc.getFault();
            fail(exchange, fault);
        } else {
            // terminate the exchange
            done(exchange);
        }
        done(rpc);
    }

    // Properties
    //-------------------------------------------------------------------------
    public ServiceEndpoint getRequestResponseEndpoint() {
        return requestResponseEndpoint;
    }

    public void setRequestResponseEndpoint(ServiceEndpoint requestResponseEndpoint) {
        this.requestResponseEndpoint = requestResponseEndpoint;
    }

    public ServiceEndpoint getOutputEndpoint() {
        return outputEndpoint;
    }

    public void setOutputEndpoint(ServiceEndpoint outputEndpoint) {
        this.outputEndpoint = outputEndpoint;
    }

    public QName getRequestResponseServiceName() {
        return requestResponseServiceName;
    }

    public void setRequestResponseServiceName(QName requestResponseServiceName) {
        this.requestResponseServiceName = requestResponseServiceName;
    }

    public QName getOutputEndpointServiceName() {
        return outputEndpointServiceName;
    }

    public void setOutputEndpointServiceName(QName outputEndpointServiceName) {
        this.outputEndpointServiceName = outputEndpointServiceName;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Resolves the given service endpoint reference from a serviceName
     *
     * @param serviceName is the name of the service
     * @return the service endpoint
     * @throws JBIException if the service cannot be resolved
     */
    protected ServiceEndpoint chooseEndpoint(QName serviceName) throws JBIException {
        ServiceEndpoint[] endpoints = getContext().getEndpointsForService(serviceName);
        if (endpoints == null || endpoints.length == 0) {
            throw new NoServiceAvailableException(serviceName);
        }
        // TODO how should we choose?
        return endpoints[0];
    }

}
