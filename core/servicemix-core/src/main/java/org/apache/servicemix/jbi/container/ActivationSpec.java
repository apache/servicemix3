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
package org.apache.servicemix.jbi.container;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.api.EndpointResolver;
import org.apache.servicemix.jbi.messaging.PojoMarshaler;
import org.apache.servicemix.jbi.resolver.EndpointChooser;
import org.apache.servicemix.jbi.resolver.InterfaceNameEndpointResolver;
import org.apache.servicemix.jbi.resolver.ServiceAndEndpointNameResolver;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.jbi.resolver.URIResolver;

/**
 * Represents the registration of a component with the {@link JBIContainer}
 * 
 * @org.apache.xbean.XBean element="activationSpec" description="The Component
 *                         configuration consisting of its container related
 *                         properties such as its routing information"
 * 
 * @version $Revision$
 */
public class ActivationSpec implements Serializable {

    static final long serialVersionUID = 8458586342841647313L;

    private String id;
    private String componentName;
    private Object component;
    private QName service;
    private QName interfaceName;
    private QName operation;
    private String endpoint;
    private transient EndpointResolver destinationResolver;
    private transient EndpointChooser interfaceChooser;
    private transient EndpointChooser serviceChooser;
    private QName destinationService;
    private QName destinationInterface;
    private QName destinationOperation;
    private String destinationEndpoint;
    private transient PojoMarshaler marshaler;
    private SubscriptionSpec[] subscriptions = {};
    private boolean failIfNoDestinationEndpoint = true;
    private Boolean persistent;
    private String destinationUri;

    public ActivationSpec() {
    }

    public ActivationSpec(Object component) {
        this.component = component;
    }

    public ActivationSpec(String id, Object component) {
        this.id = id;
        this.component = component;
    }

    /**
     * The unique component ID
     * 
     * @return the unique ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique component ID
     * 
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     * @org.apache.xbean.Property
     * @return
     */
    public Object getComponent() {
        return component;
    }

    public void setComponent(Object component) {
        this.component = component;
    }

    /**
     * Returns the service of the component to register
     */
    public QName getService() {
        return service;
    }

    public void setService(QName service) {
        this.service = service;
    }

    /**
     * Returns the endpoint name of this component
     */
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public QName getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(QName interfaceName) {
        this.interfaceName = interfaceName;
    }

    public QName getOperation() {
        return operation;
    }

    public void setOperation(QName operation) {
        this.operation = operation;
    }

    /**
     * Returns the destination resolver used to decide which destination the
     * container should route this component to.
     * 
     * @return the destination resolver, lazily creating one if possible
     */
    public EndpointResolver getDestinationResolver() {
        if (destinationResolver == null) {
            destinationResolver = createEndpointResolver();
        }
        return destinationResolver;
    }

    /**
     * Sets the destination resolver used by the container to route requests
     * send on the default endpoint.
     * 
     * @param destinationResolver
     */
    public void setDestinationResolver(EndpointResolver destinationResolver) {
        this.destinationResolver = destinationResolver;
    }

    public EndpointChooser getInterfaceChooser() {
        return interfaceChooser;
    }

    public void setInterfaceChooser(EndpointChooser interfaceChooser) {
        this.interfaceChooser = interfaceChooser;
    }

    public EndpointChooser getServiceChooser() {
        return serviceChooser;
    }

    public void setServiceChooser(EndpointChooser serviceChooser) {
        this.serviceChooser = serviceChooser;
    }

    /**
     * The destination service name
     */
    public QName getDestinationService() {
        return destinationService;
    }

    public void setDestinationService(QName destinationService) {
        this.destinationService = destinationService;
    }

    /**
     * The destination interface
     */
    public QName getDestinationInterface() {
        return destinationInterface;
    }

    public void setDestinationInterface(QName destinationInterface) {
        this.destinationInterface = destinationInterface;
    }

    /**
     * The destination operation name
     */
    public QName getDestinationOperation() {
        return destinationOperation;
    }

    public void setDestinationOperation(QName destinationOperation) {
        this.destinationOperation = destinationOperation;
    }

    /**
     * The destination endpoint
     */
    public String getDestinationEndpoint() {
        return destinationEndpoint;
    }

    public void setDestinationEndpoint(String destinationEndpoint) {
        this.destinationEndpoint = destinationEndpoint;
    }

    public PojoMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(PojoMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public SubscriptionSpec[] getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(SubscriptionSpec[] subscriptions) {
        this.subscriptions = subscriptions;
    }

    public boolean isFailIfNoDestinationEndpoint() {
        return failIfNoDestinationEndpoint;
    }

    /**
     * Sets whether or not there should be a failure if there is no matching
     * endpoint for the service dispatch. It may be in a pure publish/subscribe
     * model you want all available subscribes to receive the message but do not
     * mind if there is not a single destination endpoint that can be found.
     * 
     * @param failIfNoDestinationEndpoint
     */
    public void setFailIfNoDestinationEndpoint(boolean failIfNoDestinationEndpoint) {
        this.failIfNoDestinationEndpoint = failIfNoDestinationEndpoint;
    }

    /**
     * Lazily create a resolver from the available information
     */
    protected EndpointResolver createEndpointResolver() {
        // lets construct a resolver if any of the output
        if (destinationService != null) {
            if (destinationEndpoint != null) {
                return new ServiceAndEndpointNameResolver(destinationService, destinationEndpoint);
            } else {
                return new ServiceNameEndpointResolver(destinationService);
            }
        } else if (destinationInterface != null) {
            return new InterfaceNameEndpointResolver(destinationInterface);
        } else if (destinationUri != null) {
            return new URIResolver(destinationUri);
        }
        return null;
    }

    public Boolean getPersistent() {
        return persistent;
    }

    /**
     * Set if message exchanges issued by the component should be persistent or
     * not. This value will override the default one given on the
     * {@link org.apache.servicemix.jbi.container.JBIContainer#persistent}
     * attribute.
     * 
     * @param persistent
     *            the new value to set
     */
    public void setPersistent(Boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * @return the destinationUri
     */
    public String getDestinationUri() {
        return destinationUri;
    }

    /**
     * @param destinationUri
     *            the destinationUri to set
     */
    public void setDestinationUri(String destinationUri) {
        this.destinationUri = destinationUri;
    }

}
