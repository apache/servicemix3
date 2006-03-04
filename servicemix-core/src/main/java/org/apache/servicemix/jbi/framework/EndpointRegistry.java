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
package org.apache.servicemix.jbi.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.servicedesc.AbstractServiceEndpoint;
import org.apache.servicemix.jbi.servicedesc.DynamicEndpoint;
import org.apache.servicemix.jbi.servicedesc.ExternalEndpoint;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.JMException;
import javax.management.ObjectName;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Registry for Components
 * 
 * @version $Revision$
 */
public class EndpointRegistry {
    
    private static final Log logger = LogFactory.getLog(EndpointRegistry.class);
    
    private Registry registry;
    
    private Map endpoints;

    /**
     * Constructor
     * 
     * @param cr
     */
    public EndpointRegistry(Registry registry) {
        this.registry = registry;
        this.endpoints = new ConcurrentHashMap();
    }

    /**
     * Get all endpoints for a given service
     * 
     * @param serviceName
     * @return array of endpoints
     */
    public ServiceEndpoint[] getEndpointsForService(QName serviceName) {
        Collection collection = getEndpointsByName(serviceName, getInternalEndpoints());
        return asEndpointArray(collection);
    }

    /**
     * This will return the endpoints for all services and endpoints that implement the named interface (portType in
     * WSDL 1.1). This method does NOT include external endpoints.
     * 
     * @param interfaceName qualified name of interface/portType that is implemented by the endpoint; if
     * <code>null</code> then all activated endpoints in the JBI environment must be returned.
     * @return an array of available endpoints for the specified interface name; must be non-null; may be empty.
     */
    public ServiceEndpoint[] getEndpoints(QName interfaceName) {
        Set result = getEndpointsByInterface(interfaceName, getInternalEndpoints());
        return asEndpointArray(result);
    }

    /**
     * Activate an endpoint
     * 
     * @param provider
     * @param serviceName
     * @param endpointName
     * @return the endpoint
     */
    public InternalEndpoint activateEndpoint(ComponentContextImpl provider, QName serviceName, String endpointName) {
        InternalEndpoint answer = new InternalEndpoint(provider.getComponentNameSpace(), endpointName, serviceName);
        if (provider.getActivationSpec().getInterfaceName() != null) {
            answer.addInterface(provider.getActivationSpec().getInterfaceName());
        }
        retrieveInterfacesFromDescription(provider, answer);
        activateEndpoint(provider, answer);
        return answer;
    }

    protected void retrieveInterfacesFromDescription(ComponentContextImpl provider, InternalEndpoint answer) {
        try {
            Document document = provider.getComponent().getServiceDescription(answer);
            if (document == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Endpoint " + answer + " has no service description");
                }
                return;
            }
            Definition definition = WSDLFactory.newInstance().newWSDLReader().readWSDL(null, document);
            Service service = definition.getService(answer.getServiceName());
            if (service == null) {
                logger.info("Endpoint " + answer + " has a service description, but no matching service found in " + definition.getServices().keySet());
                return;
            }
            Port port = service.getPort(answer.getEndpointName());
            if (port == null) {
                logger.info("Endpoint " + answer + " has a service description, but no matching endpoint found in " + service.getPorts().keySet());
                return;
            }
            QName interfaceName = port.getBinding().getPortType().getQName();
            if (logger.isDebugEnabled()) {
                logger.debug("Endpoint " + answer + " implements interface " + interfaceName);
            }
            answer.addInterface(interfaceName);
        } catch (Exception e) {
            logger.warn("Error retrieving interfaces from service description: " + e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error retrieving interfaces from service description", e);
            }
        }
    }

    /**
     * Activate an Endpoint
     * 
     * @param provider
     * @param serviceEndpoint
     */
    public synchronized void activateEndpoint(ComponentContext provider, InternalEndpoint serviceEndpoint) {
        ComponentConnector cc = registry.getLocalComponentConnector(serviceEndpoint.getComponentNameSpace());
        if (cc != null) {
            cc.addActiveEndpoint(serviceEndpoint);
        }
        try {
            Endpoint endpoint = new Endpoint(serviceEndpoint, this);
            ObjectName objectName = registry.getContainer().getManagementContext().createObjectName(endpoint);
            registry.getContainer().getManagementContext().registerMBean(objectName, endpoint, EndpointMBean.class);
            endpoints.put(serviceEndpoint, endpoint);
        } catch (JMException e) {
            logger.error("Could not register MBean for endpoint", e);
        }
    }

    /**
     * Called by component context when endpoints are being deactivated.
     * 
     * @param provider
     * @param serviceEndpoint
     */
    public void deactivateEndpoint(ComponentContext provider, InternalEndpoint serviceEndpoint) {
        ComponentConnector cc = registry.getLocalComponentConnector(serviceEndpoint.getComponentNameSpace());
        if (cc != null) {
            cc.removeActiveEndpoint(serviceEndpoint);
        }
        Endpoint ep = (Endpoint) endpoints.remove(serviceEndpoint);
        if (ep != null) {
            try {
                registry.getContainer().getManagementContext().unregisterMBean(ep);
            } catch (JBIException e) {
                logger.error("Could not unregister MBean for endpoint", e);
            }
        }
    }

    /**
     * Get the named ServiceEndpoint, if activated
     * 
     * @param service
     * @param name
     * @return the activated ServiceEndpoint or null
     */
    public ServiceEndpoint getEndpoint(QName service, String name) {
        ServiceEndpoint result = null;
        for (Iterator i = getInternalEndpoints().iterator();i.hasNext();) {
            ServiceEndpoint endpoint = (ServiceEndpoint) i.next();
            if (endpoint.getServiceName().equals(service) && endpoint.getEndpointName().equals(name)) {
                result = endpoint;
                break;
            }
        }
        return result;
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
        if (endpoint instanceof AbstractServiceEndpoint == false) {
            throw new JBIException("Descriptors can not be queried for external endpoints");
        }
        AbstractServiceEndpoint se = (AbstractServiceEndpoint) endpoint;
        Component component = registry.getComponent(se.getComponentNameSpace());
        return component.getServiceDescription(endpoint);
    }

    /**
     * Registers the given external endpoint with the NMR. This indicates to the NMR that the given endpoint is used as
     * a proxy for external service consumers to access an internal service of the same service name (but a different
     * endpoint name).
     * 
     * @param provider
     * @param externalEndpoint the external endpoint to be registered, must be non-null.
     */
    public void registerExternalEndpoint(ComponentContextImpl provider, ServiceEndpoint externalEndpoint) {
        ComponentConnector cc = registry.getLocalComponentConnector(provider.getComponentNameSpace());
        if (cc != null) {
            cc.addExternalActiveEndpoint(new ExternalEndpoint(cc.getComponentNameSpace(), externalEndpoint));
        }
    }

    /**
     * Deregisters the given external endpoint with the NMR. This indicates to the NMR that the given external endpoint
     * can no longer be used as a proxy for external service consumers to access an internal service of the same service
     * name.
     * 
     * @param provider
     * @param externalEndpoint the external endpoint to be deregistered; must be non-null.
     */
    public void deregisterExternalEndpoint(ComponentContextImpl provider, ServiceEndpoint externalEndpoint) {
        ComponentConnector cc = registry.getLocalComponentConnector(provider.getComponentNameSpace());
        if (cc != null) {
            cc.removeExternalActiveEndpoint(externalEndpoint);
        }
    }

    /**
     * This methods returns only registered external endpoints
     * 
     * @param interfaceName qualified name of interface implemented by the endpoints; must be non-null.
     * @return an array of available external endpoints for the specified interface name; must be non-null; may be
     * empty.
     */
    public ServiceEndpoint[] getExternalEndpoints(QName interfaceName) {
        Set endpoints = getEndpointsByInterface(interfaceName, getExternalEndpoints());
        return asEndpointArray(endpoints);
    }

    /**
     * Get external endpoints for the service
     * 
     * @param serviceName qualified name of service that contains the endpoints; must be non-null.
     * @return an array of available external endpoints for the specified service name; must be non-null; may be empty.
     */
    public ServiceEndpoint[] getExternalEndpointsForService(QName serviceName) {
        Set endpoints = getEndpointsByName(serviceName, getExternalEndpoints());
        return asEndpointArray(endpoints);
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
        Collection connectors = registry.getLocalComponentConnectors();
        for (Iterator iter = connectors.iterator(); iter.hasNext();) {
            LocalComponentConnector connector = (LocalComponentConnector) iter.next();
            ServiceEndpoint se = connector.getComponent().resolveEndpointReference(epr);
            if (se != null) {
                return new DynamicEndpoint(connector.getComponentNameSpace(), se, epr);  
            }
        }
        return null;
    }

    /**
     * Helper method to convert the given collection into an array of endpoints
     * 
     * @param collection
     * @return array of endpoints
     */
    protected ServiceEndpoint[] asEndpointArray(Collection collection) {
        if (collection == null) {
            return new ServiceEndpoint[0];
        }
        int size = collection.size();
        ServiceEndpoint[] answer = new ServiceEndpoint[size];
        Iterator it = collection.iterator();
        for (int i = 0; i < size; i++) {
            answer[i] = (ServiceEndpoint) it.next();
        }
        return answer;
    }

    /**
     * return a collection of endpoints
     * 
     * @param serviceName
     * @param endpoints
     * @return collection of endpoints
     */
    protected Set getEndpointsByName(QName serviceName, Set endpoints) {
        Set answer = new HashSet();
        for (Iterator i = endpoints.iterator(); i.hasNext();) {
            ServiceEndpoint endpoint = (ServiceEndpoint) i.next();
            if (endpoint.getServiceName().equals(serviceName)) {
                answer.add(endpoint);
            }
        }
        return answer;
    }
    
    /**
     * Filters the given endpoints and returns those implementing the
     * given interface name.  If interfaceName is null, then no filter
     * is applied.
     * 
     */
    protected Set getEndpointsByInterface(QName interfaceName, Set endpoints) {
        if (interfaceName == null) {
            return endpoints;
        }
        Set answer = new HashSet();
        for (Iterator i = endpoints.iterator(); i.hasNext();) {
            ServiceEndpoint endpoint = (ServiceEndpoint) i.next();
            QName[] interfaces = endpoint.getInterfaces();
            if (interfaces != null) {
                for (int k = 0;k < interfaces.length;k++) {
                    QName qn = interfaces[k];
                    if (qn != null && qn.equals(interfaceName)) {
                        answer.add(endpoint);
                        break;
                    }
                }
            }
        }
        return answer;
    }

    /**
     * Utility method to get a ComponentConnector from a serviceName
     * 
     * @param serviceName
     * @return the ComponentConnector
     */
    public ComponentConnector getComponentConnectorByServiceName(QName serviceName) {
        ComponentConnector result = null;
        Set set = getEndpointsByName(serviceName, getInternalEndpoints());
        if (!set.isEmpty()) {
            InternalEndpoint endpoint = (InternalEndpoint) set.iterator().next();
            result = registry.getComponentConnector(endpoint.getComponentNameSpace());
        }
        return result;
    }

    protected ComponentConnector getComponentConnectorByEndpointName(String endpointName) {
        ComponentConnector result = null;
        if (endpointName != null) {
            for (Iterator i = getInternalEndpoints().iterator();i.hasNext();) {
                InternalEndpoint endpoint = (InternalEndpoint) i.next();
                if (endpoint.getEndpointName().equals(endpointName)) {
                    result = registry.getComponentConnector(endpoint.getComponentNameSpace());
                    break;
                }
            }
        }
        return result;
    }
    
    protected InternalEndpoint getEndpointByName(String endpointName) {
        InternalEndpoint result = null;
        if (endpointName != null) {
            for (Iterator i = getInternalEndpoints().iterator();i.hasNext();) {
                InternalEndpoint endpoint = (InternalEndpoint) i.next();
                if (endpoint.getEndpointName().equals(endpointName)) {
                    result = endpoint;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * @return all default endpoints
     */
    protected Set getInternalEndpoints() {
        Set answer = new HashSet();
        for (Iterator iter = this.registry.getComponentConnectors().iterator();iter.hasNext();) {
            ComponentConnector cc = (ComponentConnector) iter.next();
            answer.addAll(cc.getActiveEndpoints());
        }
        return answer;
    }

    /**
     * @return all external endpoints
     */
    protected Set getExternalEndpoints() {
        Set answer = new HashSet();
        for (Iterator iter = this.registry.getComponentConnectors().iterator(); iter.hasNext();) {
            ComponentConnector cc = (ComponentConnector) iter.next();
            answer.addAll(cc.getExternalActiveEndpoints());
        }
        return answer;
    }

}
