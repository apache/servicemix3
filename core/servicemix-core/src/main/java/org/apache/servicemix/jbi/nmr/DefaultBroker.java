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

import java.util.ArrayList;
import java.util.List;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.JMException;
import javax.management.MBeanOperationInfo;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.api.EndpointResolver;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.framework.ComponentContextImpl;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.jbi.framework.ComponentNameSpace;
import org.apache.servicemix.jbi.framework.Registry;
import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.servicemix.jbi.management.ManagementContext;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.nmr.flow.DefaultFlowChooser;
import org.apache.servicemix.jbi.nmr.flow.Flow;
import org.apache.servicemix.jbi.nmr.flow.FlowChooser;
import org.apache.servicemix.jbi.nmr.flow.FlowProvider;
import org.apache.servicemix.jbi.resolver.ConsumerComponentEndpointFilter;
import org.apache.servicemix.jbi.resolver.EndpointChooser;
import org.apache.servicemix.jbi.resolver.EndpointFilter;
import org.apache.servicemix.jbi.resolver.FirstChoicePolicy;
import org.apache.servicemix.jbi.resolver.ProducerComponentEndpointFilter;
import org.apache.servicemix.jbi.servicedesc.AbstractServiceEndpoint;
import org.apache.servicemix.jbi.servicedesc.ExternalEndpoint;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
import org.apache.servicemix.jbi.servicedesc.LinkedEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Broker handles Normelized Message Routing within ServiceMix
 * 
 * @version $Revision$
 */
public class DefaultBroker extends BaseSystemService implements Broker {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(DefaultBroker.class);

    private Registry registry;
    private String flowNames = "seda";
    private String subscriptionFlowName;
    private Flow[] flows;
    private EndpointChooser defaultServiceChooser = new FirstChoicePolicy();
    private EndpointChooser defaultInterfaceChooser = new FirstChoicePolicy();
    private SubscriptionManager subscriptionManager = new SubscriptionManager();
    private FlowChooser defaultFlowChooser = new DefaultFlowChooser();

    /**
     * Constructor
     */
    public DefaultBroker() {

    }

    /**
     * Get the description
     * 
     * @return description
     */
    public String getDescription() {
        return "Normalized Message Router";
    }

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }

    /**
     * Sets the subscription manager
     */
    public void setSubscriptionManager(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    /**
     * initialize the broker
     * 
     * @param container
     * @throws JBIException
     */
    public void init(JBIContainer container) throws JBIException {
        super.init(container);
        this.registry = container.getRegistry();
        // Create and initialize flows
        if (this.flows == null) {
            String[] names = flowNames.split(",");
            flows = new Flow[names.length];
            for (int i = 0; i < names.length; i++) {
                flows[i] = FlowProvider.getFlow(names[i]);
                flows[i].init(this);
            }
        } else {
            for (int i = 0; i < flows.length; i++) {
                flows[i].init(this);
            }
        }
        subscriptionManager.init(this, registry);
    }

    protected Class<BrokerMBean> getServiceMBean() {
        return BrokerMBean.class;
    }

    /**
     * Get the name of the Container
     * 
     * @return containerName
     */
    public String getContainerName() {
        return container.getName();
    }

    /**
     * Get the ManagementContext
     * 
     * @return the managementContext
     */
    public ManagementContext getManagementContext() {
        return container.getManagementContext();
    }

    /**
     * Get the Registry
     * 
     * @return the registry
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * start brokering
     * 
     * @throws JBIException
     */
    public void start() throws JBIException {
        for (int i = 0; i < flows.length; i++) {
            flows[i].start();
        }
        super.start();
    }

    /**
     * stop brokering
     * 
     * @throws JBIException
     */
    public void stop() throws JBIException {
        for (int i = 0; i < flows.length; i++) {
            flows[i].stop();
        }
        super.stop();
    }

    /**
     * shutdown all Components
     * 
     * @throws JBIException
     */
    public void shutDown() throws JBIException {
        stop();
        for (int i = 0; i < flows.length; i++) {
            flows[i].shutDown();
        }
        container.deactivateComponent(SubscriptionManager.COMPONENT_NAME);
        super.shutDown();
        container.getManagementContext().unregisterMBean(this);
    }

    /**
     * @return Returns the flow.
     */
    public String getFlowNames() {
        return flowNames;
    }

    /**
     * @param flowNames
     *            The flow to set.
     */
    public void setFlowNames(String flowNames) {
        this.flowNames = flowNames;
    }

    /**
     * @return the subscriptionFlowName
     */
    public String getSubscriptionFlowName() {
        return subscriptionFlowName;
    }

    /**
     * Set the subscription flow name
     * 
     * @param subscriptionFlowName
     */
    public void setSubscriptionFlowName(String subscriptionFlowName) {
        this.subscriptionFlowName = subscriptionFlowName;
    }

    /**
     * Set the flow
     * 
     * @param flows
     */
    public void setFlows(Flow[] flows) {
        this.flows = flows;
    }

    /**
     * @return the Flow
     */
    public Flow[] getFlows() {
        return this.flows;
    }

    /**
     * suspend the flow to prevent any message exchanges
     */
    public void suspend() {
        for (int i = 0; i < flows.length; i++) {
            flows[i].suspend();
        }
    }

    /**
     * resume message exchange processing
     */
    public void resume() {
        for (int i = 0; i < flows.length; i++) {
            flows[i].resume();
        }
    }

    /**
     * Route an ExchangePacket to a destination
     * 
     * @param me
     * @throws JBIException
     */
    public void sendExchangePacket(MessageExchange me) throws JBIException {
        MessageExchangeImpl exchange = (MessageExchangeImpl) me;
        if (exchange.getRole() == Role.PROVIDER && exchange.getDestinationId() == null) {
            resolveAddress(exchange);
        }

        boolean foundRoute = false;
        // If we found a destination, or this is a reply
        if (exchange.getEndpoint() != null || exchange.getRole() == Role.CONSUMER) {
            foundRoute = true;
            Flow flow = defaultFlowChooser.chooseFlow(flows, exchange);
            if (flow == null) {
                throw new MessagingException("Unable to choose a flow for exchange: " + exchange);
            }
            flow.send(exchange);
        }

        if (exchange.getRole() == Role.PROVIDER) {
            getSubscriptionManager().dispatchToSubscribers(exchange);
        }

        if (!foundRoute) {
            boolean throwException = true;
            ActivationSpec activationSpec = exchange.getActivationSpec();
            if (activationSpec != null) {
                throwException = activationSpec.isFailIfNoDestinationEndpoint();
            }
            if (throwException) {
                throw new MessagingException("Could not find route for exchange: " + exchange + " for service: " + exchange.getService()
                                + " and interface: " + exchange.getInterfaceName());
            } else if (exchange.getMirror().getSyncState() == MessageExchangeImpl.SYNC_STATE_SYNC_SENT) {
                exchange.handleAccept();
                ComponentContextImpl ctx = (ComponentContextImpl) getSubscriptionManager().getContext();
                exchange.setDestinationId(ctx.getComponentNameSpace());
                // TODO: this will fail if exchange is InOut
                getSubscriptionManager().done(exchange);
            }
        }
    }

    protected void resolveAddress(MessageExchangeImpl exchange) throws JBIException {
        ServiceEndpoint theEndpoint = exchange.getEndpoint();
        if (theEndpoint != null) {
            if (theEndpoint instanceof ExternalEndpoint) {
                throw new JBIException("External endpoints can not be used for routing: should be an internal or dynamic endpoint.");
            }
            if (!(theEndpoint instanceof AbstractServiceEndpoint)) {
                throw new JBIException(
                                "Component-specific endpoints can not be used for routing: should be an internal or dynamic endpoint.");
            }
        }
        // Resolve linked endpoints
        if (theEndpoint instanceof LinkedEndpoint) {
            QName svcName = ((LinkedEndpoint) theEndpoint).getToService();
            String epName = ((LinkedEndpoint) theEndpoint).getToEndpoint();
            ServiceEndpoint ep = registry.getInternalEndpoint(svcName, epName);
            if (ep == null) {
                throw new JBIException("Could not resolve linked endpoint: " + theEndpoint);
            }
            theEndpoint = ep;
        }

        // get the context which created the exchange
        ComponentContextImpl context = exchange.getSourceContext();
        if (theEndpoint == null) {
            QName serviceName = exchange.getService();
            QName interfaceName = exchange.getInterfaceName();

            // check in order, ServiceName then InterfaceName
            // check to see if there is a match on the serviceName
            if (serviceName != null) {
                ServiceEndpoint[] endpoints = registry.getEndpointsForService(serviceName);
                endpoints = getMatchingEndpoints(endpoints, exchange);
                theEndpoint = getServiceChooser(exchange).chooseEndpoint(endpoints, context, exchange);
                if (theEndpoint == null) {
                    LOGGER.warn("ServiceName ({}) specified for routing, but can't find it registered", serviceName);
                }
            }
            if (theEndpoint == null && interfaceName != null) {
                ServiceEndpoint[] endpoints = registry.getEndpointsForInterface(interfaceName);
                endpoints = getMatchingEndpoints(endpoints, exchange);
                theEndpoint = (InternalEndpoint) getInterfaceChooser(exchange).chooseEndpoint(endpoints, context, exchange);
                if (theEndpoint == null) {
                    LOGGER.warn("InterfaceName ({}) specified for routing, but can't find any matching components", interfaceName);
                }
            }
            if (theEndpoint == null) {
                // lets use the resolver on the activation spec if
                // applicable
                ActivationSpec activationSpec = exchange.getActivationSpec();
                if (activationSpec != null) {
                    EndpointResolver destinationResolver = activationSpec.getDestinationResolver();
                    if (destinationResolver != null) {
                        try {
                            EndpointFilter filter = createEndpointFilter(context, exchange);
                            theEndpoint = (InternalEndpoint) destinationResolver.resolveEndpoint(context, exchange, filter);
                        } catch (JBIException e) {
                            throw new MessagingException("Failed to resolve endpoint: " + e, e);
                        }
                    }
                }
            }
        }
        if (theEndpoint != null) {
            exchange.setEndpoint(theEndpoint);
        }
        LOGGER.trace("Routing exchange {} to {}", exchange, theEndpoint);
    }

    /**
     * Filter the given endpoints by asking to the provider and consumer if they
     * are both ok to process the exchange.
     * 
     * @param endpoints
     *            an array of internal endpoints to check
     * @param exchange
     *            the exchange that will be serviced
     * @return an array of endpoints on which both consumer and provider agrees
     */
    protected ServiceEndpoint[] getMatchingEndpoints(ServiceEndpoint[] endpoints, MessageExchangeImpl exchange) {
        List<ServiceEndpoint> filtered = new ArrayList<ServiceEndpoint>();
        ComponentMBeanImpl consumer = getRegistry().getComponent(exchange.getSourceId());

        for (int i = 0; i < endpoints.length; i++) {
            ComponentNameSpace id = ((InternalEndpoint) endpoints[i]).getComponentNameSpace();
            if (id != null) {
                ComponentMBeanImpl provider = getRegistry().getComponent(id);
                if (provider != null
                        && (!consumer.getComponent().isExchangeWithProviderOkay(endpoints[i], exchange)
                                || !provider.getComponent().isExchangeWithConsumerOkay(endpoints[i], exchange))) {
                    continue;
                }
            }
            filtered.add(endpoints[i]);
        }
        return filtered.toArray(new ServiceEndpoint[filtered.size()]);
    }

    /**
     * @return the default EndpointChooser
     */
    public EndpointChooser getDefaultInterfaceChooser() {
        return defaultInterfaceChooser;
    }

    /**
     * Set the default EndpointChooser
     * 
     * @param defaultInterfaceChooser
     */
    public void setDefaultInterfaceChooser(EndpointChooser defaultInterfaceChooser) {
        this.defaultInterfaceChooser = defaultInterfaceChooser;
    }

    /**
     * @return the default EndpointChooser
     */
    public EndpointChooser getDefaultServiceChooser() {
        return defaultServiceChooser;
    }

    /**
     * Set default EndpointChooser
     * 
     * @param defaultServiceChooser
     */
    public void setDefaultServiceChooser(EndpointChooser defaultServiceChooser) {
        this.defaultServiceChooser = defaultServiceChooser;
    }

    /**
     * @return the defaultFlowChooser
     */
    public FlowChooser getDefaultFlowChooser() {
        return defaultFlowChooser;
    }

    /**
     * @param defaultFlowChooser
     *            the defaultFlowChooser to set
     */
    public void setDefaultFlowChooser(FlowChooser defaultFlowChooser) {
        this.defaultFlowChooser = defaultFlowChooser;
    }

    /**
     * Returns the endpoint chooser for endpoints found by service which will
     * use the chooser on the exchange's activation spec if available otherwise
     * will use the default
     * 
     * @param exchange
     * @return the EndpointChooser
     */
    protected EndpointChooser getServiceChooser(MessageExchangeImpl exchange) {
        EndpointChooser chooser = null;
        ActivationSpec activationSpec = exchange.getActivationSpec();
        if (activationSpec != null) {
            chooser = activationSpec.getServiceChooser();
        }
        if (chooser == null) {
            chooser = defaultServiceChooser;
        }
        return chooser;
    }

    /**
     * Returns the endpoint chooser for endpoints found by service which will
     * use the chooser on the exchange's activation spec if available otherwise
     * will use the default
     * 
     * @param exchange
     * @return the EndpointChooser
     */
    protected EndpointChooser getInterfaceChooser(MessageExchangeImpl exchange) {
        EndpointChooser chooser = null;
        ActivationSpec activationSpec = exchange.getActivationSpec();
        if (activationSpec != null) {
            chooser = activationSpec.getInterfaceChooser();
        }
        if (chooser == null) {
            chooser = defaultInterfaceChooser;
        }
        return chooser;
    }

    /**
     * Factory method to create an endpoint filter for the given component
     * context and message exchange
     * 
     * @param context
     * @param exchange
     * @return the EndpointFilter
     */
    protected EndpointFilter createEndpointFilter(ComponentContextImpl context, MessageExchangeImpl exchange) {
        Component component = context.getComponent();
        if (exchange.getRole() == Role.PROVIDER) {
            return new ConsumerComponentEndpointFilter(component);
        } else {
            return new ProducerComponentEndpointFilter(component);
        }
    }

    /**
     * Get an array of MBeanOperationInfo
     * 
     * @return array of OperationInfos
     * @throws JMException
     */
    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        helper.addOperation(getObjectToManage(), "suspend", "suspend the NMR processing");
        helper.addOperation(getObjectToManage(), "resume", "resume the NMR processing");

        return OperationInfoHelper.join(super.getOperationInfos(), helper.getOperationInfos());
    }

    public JBIContainer getContainer() {
        return container;
    }

}