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
package org.apache.servicemix.jbi.framework;

import java.util.MissingResourceException;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.management.MBeanNames;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.ComponentEnvironment;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.container.SubscriptionSpec;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This context provides access to data needed by all JBI components running in the JBI environment.
 * 
 * @version $Revision$
 */
public class ComponentContextImpl implements ComponentContext, MBeanNames {
    
    private static final transient Logger LOGGER = LoggerFactory.getLogger(ComponentContextImpl.class);
    
    private ComponentNameSpace componentName;
    private ComponentEnvironment environment;
    private JBIContainer container;
    private Component component;
    private DeliveryChannel deliveryChannel;
    private ActivationSpec activationSpec;
    private boolean activated;

    /**
     * Constructor
     * 
     * @param container
     * @param componentName
     */
    public ComponentContextImpl(JBIContainer container, ComponentNameSpace componentName) {
        this.componentName = componentName;
        this.container = container;
    }

    /**
     * Activate the ComponentContext
     * 
     * @param comp
     * @param env
     * @param spec
     */
    public void activate(Component comp, 
                         ComponentEnvironment env,
                         ActivationSpec spec) {
        this.component = comp;
        this.environment = env;
        this.activationSpec = spec;
        activated = true;
        //activate and subscriptions
        container.getRegistry().registerSubscriptions(this, spec);
    }

    /**
     * get the id of the ComponentConnector
     * 
     * @return the id
     */
    public ComponentNameSpace getComponentNameSpace() {
        return componentName;
    }

    /**
     * @return the unique component name
     */
    public String getComponentName() {
        return componentName.getName();
    }

    /**
     * @return this component instance
     */
    public Component getComponent() {
        return component;
    }

    /**
     * @param serviceName
     * @param endpointName
     * @return EndPointReference
     * @throws JBIException
     */
    public ServiceEndpoint activateEndpoint(QName serviceName, String endpointName) throws JBIException {
        checkActivated();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Component: " + componentName.getName() + " activated endpoint: " + serviceName + " : " + endpointName);
        }
        return container.getRegistry().activateEndpoint(this, serviceName, endpointName);
    }

    /**
     * @param serviceName
     * @return endpoints registered against the service
     * @throws JBIException
     */
    public ServiceEndpoint[] availableEndpoints(QName serviceName) throws JBIException {
        checkActivated();
        return container.getRegistry().getEndpointsForService(serviceName);
    }

    /**
     * Deregister the endpoint with the NMR
     * 
     * @param endpoint
     * @throws JBIException
     */
    public void deactivateEndpoint(ServiceEndpoint endpoint) throws JBIException {
        checkActivated();
        container.getRegistry().deactivateEndpoint(this, (InternalEndpoint) endpoint);
    }
    
    /**
     * Register All subscriptions
     * @param context 
     * @param as 
     */
    public void registerSubscriptions(ComponentContextImpl context, ActivationSpec as) {
        checkActivated();
        container.getRegistry().registerSubscriptions(context, as);
    }
    
    /**
     * Deregister All subscriptions
     * @param context
     * @param as
     */
    public void deregisterSubscriptions(ComponentContextImpl context, ActivationSpec as) {
        checkActivated();
        container.getRegistry().deregisterSubscriptions(context, as);
    }
    
    /**
     * @param context 
     * @param subscription
     * @param endpoint
     */
    public void registerSubscription(ComponentContextImpl context, SubscriptionSpec subscription, ServiceEndpoint endpoint) {
        checkActivated();
        container.getRegistry().registerSubscription(context, subscription, endpoint);
    }

    /**
     * @param context 
     * @param subscription
     * @return the ServiceEndpoint
     */
    public InternalEndpoint deregisterSubscription(ComponentContextImpl context, SubscriptionSpec subscription) {
        checkActivated();
        return container.getRegistry().deregisterSubscription(context, subscription);
    }
    

    /**
     * @return the Delivery Channel
     */
    public DeliveryChannel getDeliveryChannel() {
        return deliveryChannel;
    }

    /**
     * Retrieve the default JMX Domain Name for MBeans registered in this instance of the JBI implementation.
     * 
     * @return the JMX domain name for this instance of the JBI implementation.
     */
    public String getJmxDomainName() {
        return container.getManagementContext().getJmxDomainName();
    }

    /**
     * Formulate and return the MBean ObjectName of a custom control MBean for a JBI component.
     * 
     * @param customName the name of the custom control.
     * @return the JMX ObjectName of the MBean, or <code>null</code> if <code>customName</code> is invalid.
     */
    public ObjectName createCustomComponentMBeanName(String customName) {
        return container.getManagementContext().createCustomComponentMBeanName(customName, componentName.getName());
    }

    /**
     * @return the MBeanNames service
     */
    public MBeanNames getMBeanNames() {
        return this;
    }

    /**
     * @return theMBean server assocated with the JBI
     */
    public MBeanServer getMBeanServer() {
        return container.getMBeanServer();
    }

    /**
     * @return the naming context
     */
    public InitialContext getNamingContext() {
        return container.getNamingContext();
    }

    /**
     * Get the TransactionManager for this implementation. The instance returned is an implementation of the standard
     * JTA interface. If none is available, this method returns <code>null</code>.
     * <p>
     * The object returned by this method is untyped, to allow this interface to be compiled in environments that do not
     * support JTA. If not null, the object returned must be of type <code>javax.transaction.TransactionManager</code>.
     * <p>
     * This downcast is necessary because JBI is used in environments that do not support JTA (i.e., J2SE). Explicit use
     * of JTA types would cause compilation failures in such environments.
     * 
     * @return A TransactionManager instance, or <code>null</code> if none is available in the execution environment.
     */
    public Object getTransactionManager() {
        return container.getTransactionManager();
    }

    /**
     * @return the root directory path
     */
    public String getWorkspaceRoot() {
        if (environment.getWorkspaceRoot() != null) {
            return environment.getWorkspaceRoot().getAbsolutePath();
        }
        return null;
    }

    /**
     * @return Returns the container.
     */
    public JBIContainer getContainer() {
        return container;
    }

    /**
     * @return Returns the ComponentEnvironment
     */
    public ComponentEnvironment getEnvironment() {
        return environment;
    }
    
    /**
     * Set the ComponentEnvironment
     * @param ce
     */
    public void setEnvironment(ComponentEnvironment ce) {
        this.environment = ce;
    }

    /**
     * @param container The container to set.
     */
    public void setContainer(JBIContainer container) {
        this.container = container;
    }

    /**
     * @param deliveryChannel The deliveryChannel to set.
     */
    public void setDeliveryChannel(DeliveryChannel deliveryChannel) {
        this.deliveryChannel = deliveryChannel;
    }

    /**
     * Registers the given external endpoint with the NMR. This indicates to the NMR that the given endpoint is used as
     * a proxy for external service consumers to access an internal service of the same service name (but a different
     * endpoint name).
     * 
     * @param externalEndpoint the external endpoint to be registered, must be non-null.
     * @exception JBIException if an external endpoint with the same name is already registered, by this or another
     * component.
     */
    public void registerExternalEndpoint(ServiceEndpoint externalEndpoint) throws JBIException {
        checkActivated();
        if (externalEndpoint == null) {
            throw new IllegalArgumentException("externalEndpoint should be non null");
        }
        container.getRegistry().registerExternalEndpoint(getComponentNameSpace(), externalEndpoint);
    }

    /**
     * Deregisters the given external endpoint with the NMR. This indicates to the NMR that the given external endpoint
     * can no longer be used as a proxy for external service consumers to access an internal service of the same service
     * name.
     * 
     * @param externalEndpoint the external endpoint to be deregistered; must be non-null.
     * @exception JBIException if the given external endpoint was not previously registered.
     */
    public void deregisterExternalEndpoint(ServiceEndpoint externalEndpoint) throws JBIException {
        checkActivated();
        container.getRegistry().deregisterExternalEndpoint(getComponentNameSpace(), externalEndpoint);
    }

    /**
     * Resolve the given endpoint reference into a service endpoint. This is called by the component when it has an EPR
     * that it wants to resolve into a service endpoint.
     * <p>
     * Note that the service endpoint returned refers to a dynamic endpoint; the endpoint will exist only as long as
     * this component retains a strong reference to the object returned by this method. The endpoint may not be included
     * in the list of "activated" endpoints.
     * 
     * @param epr endpoint reference as an XML fragment; must be non-null.
     * @return the service endpoint corresponding to the given endpoint reference; <code>null</code> if the reference
     * cannot be resolved.
     */
    public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
        checkActivated();
        return container.getRegistry().resolveEndpointReference(epr);
    }

    /**
     * Get the service endpoint for the named activated endpoint, if any.
     * 
     * @param service qualified-name of the endpoint's service; must be non-null.
     * @param name name of the endpoint; must be non-null.
     * @return the named endpoint, or <code>null</code> if the named endpoint is not activated.
     */
    public ServiceEndpoint getEndpoint(QName service, String name) {
        checkActivated();
        return container.getRegistry().getEndpoint(service, name);
    }

    /**
     * Retrieve the service description metadata for the specified endpoint.
     * <p>
     * Note that the result can use either the WSDL 1.1 or WSDL 2.0 description language.
     * 
     * @param endpoint endpoint reference; must be non-null.
     * @return metadata describing endpoint, or <code>null</code> if metadata is unavailable.
     * @exception JBIException invalid endpoint reference.
     */
    public Document getEndpointDescriptor(ServiceEndpoint endpoint) throws JBIException {
        checkActivated();
        return container.getRegistry().getEndpointDescriptor(endpoint);
    }

    /**
     * Queries the NMR for active endpoints that implement the given interface. This will return the endpoints for all
     * services and endpoints that implement the named interface (portType in WSDL 1.1). This method does NOT include
     * external endpoints (those registered using {@link #registerExternalEndpoint(ServiceEndpoint)}.
     * 
     * @param interfaceName qualified name of interface/portType that is implemented by the endpoint; if
     * <code>null</code> then all activated endpoints in the JBI environment must be returned.
     * @return an array of available endpoints for the specified interface name; must be non-null; may be empty.
     */
    public ServiceEndpoint[] getEndpoints(QName interfaceName) {
        checkActivated();
        return container.getRegistry().getEndpointsForInterface(interfaceName);
    }

    /**
     * Queries the NMR for active endpoints belonging to the given service. This method does NOT include external
     * endpoints (those registered using {@link #registerExternalEndpoint(ServiceEndpoint)}.
     * 
     * @param serviceName qualified name of the service that the endpoints are part of; must be non-null.
     * @return an array of available endpoints for the specified service name; must be non-null; may be empty.
     */
    public ServiceEndpoint[] getEndpointsForService(QName serviceName) {
        checkActivated();
        return container.getRegistry().getEndpointsForService(serviceName);
    }

    /**
     * Queries the NMR for external endpoints that implement the given interface name. This methods returns only
     * registered external endpoints (see {@link #registerExternalEndpoint(ServiceEndpoint)}.
     * 
     * @param interfaceName qualified name of interface implemented by the endpoints; must be non-null.
     * @return an array of available external endpoints for the specified interface name; must be non-null; may be
     * empty.
     */
    public ServiceEndpoint[] getExternalEndpoints(QName interfaceName) {
        checkActivated();
        return container.getRegistry().getExternalEndpoints(interfaceName);
    }

    /**
     * Queries the NMR for external endpoints that are part of the given service.
     * 
     * @param serviceName qualified name of service that contains the endpoints; must be non-null.
     * @return an array of available external endpoints for the specified service name; must be non-null; may be empty.
     */
    public ServiceEndpoint[] getExternalEndpointsForService(QName serviceName) {
        checkActivated();
        return container.getRegistry().getExternalEndpointsForService(serviceName);
    }

    /**
     * Get the installation root directory path for this component.
     * <p>
     * This method MUST return the file path formatted for the underlying platform.
     * 
     * @return the installation root directory path, in platform-specific form; must be non-null and non-empty.
     */
    public String getInstallRoot() {
        if (environment.getInstallRoot() != null) {
            return environment.getInstallRoot().getAbsolutePath();
        }
        return null;
    }

    /**
     * Get a LOGGER instance from JBI. Loggers supplied by JBI are guaranteed to have unique names such that they avoid
     * name collisions with loggers from other components created using this method. The suffix parameter allows for the
     * creation of subloggers as needed. The JBI specification says nothing about the exact names to be used, only that
     * they must be unique across components and the JBI implementation itself.
     * 
     * @param suffix for creating subloggers; use an empty string for the base component LOGGER; must be non-null.
     * @param resourceBundleName name of <code>ResourceBundle</code> to be used for localizing messages for the
     * LOGGER. May be <code>null</code> if none of the messages require localization. The resource, if non-null, must
     * be loadable using the component's class loader as the initiating loader.
     * @return a standard LOGGER, named uniquely for this component (plus the given suffix, if applicable); must be
     * non-null.
     * @exception MissingResourceException if the ResourceBundleName is non-null and no corresponding resource can be
     * found.
     * @exception JBIException if the resourceBundleName has changed from a previous invocation by this component of
     * this method with the same suffix.
     */
    public java.util.logging.Logger getLogger(String suffix, String resourceBundleName) throws MissingResourceException, JBIException {
        String name = suffix != null ? suffix : "";
        name = componentName.getName() + name;
        return container.getLogger(name, resourceBundleName);
    }

    /**
     * @return the ActivationSpec
     */
    public ActivationSpec getActivationSpec() {
        return activationSpec;
    }

    private void checkActivated() {
        if (!activated) {
            throw new IllegalStateException("ComponentContext not activated");
        }
    }
}