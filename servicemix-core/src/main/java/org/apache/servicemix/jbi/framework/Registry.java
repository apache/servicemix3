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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.management.DeploymentException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.JMException;
import javax.management.ObjectName;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.EnvironmentContext;
import org.apache.servicemix.jbi.container.SubscriptionSpec;
import org.apache.servicemix.jbi.deployment.ServiceAssembly;
import org.apache.servicemix.jbi.deployment.ServiceUnit;
import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.servicedesc.AbstractServiceEndpoint;
import org.apache.servicemix.jbi.servicedesc.DynamicEndpoint;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry - state infomation including running state, SA's deployed etc.
 * 
 * @version $Revision$
 */
public class Registry extends BaseSystemService implements RegistryMBean {
    
    private static final Log log = LogFactory.getLog(Registry.class);
    private ComponentRegistry componentRegistry;
    private EndpointRegistry endpointRegistry;
    private SubscriptionRegistry subscriptionRegistry;
    private ServiceAssemblyRegistry serviceAssemblyRegistry;
    private Map serviceUnits;
    private List pendingAssemblies;

    /**
     * Constructor
     */
    public Registry() {
        this.componentRegistry = new ComponentRegistry(this);
        this.endpointRegistry = new EndpointRegistry(this);
        this.subscriptionRegistry = new SubscriptionRegistry(this);
        this.serviceAssemblyRegistry = new ServiceAssemblyRegistry(this);
        this.serviceUnits = new ConcurrentHashMap();
        this.pendingAssemblies = new CopyOnWriteArrayList();
    }
    
    /**
     * Get the description
     * @return description
     */
    public String getDescription(){
        return "Registry of Components/SU's and Endpoints";
    }

    protected Class getServiceMBean() {
        return RegistryMBean.class;
    }

    /**
     * start brokering
     * 
     * @throws JBIException
     */
    public void start() throws JBIException {

        componentRegistry.start();
        serviceAssemblyRegistry.start();
        super.start();
    }

    /**
     * stop brokering
     * 
     * @throws JBIException
     */
    public void stop() throws JBIException {
        serviceAssemblyRegistry.stop();
        componentRegistry.stop();
        super.stop();
    }

    /**
     * shutdown all Components
     * 
     * @throws JBIException
     */
    public void shutDown() throws JBIException {
        serviceAssemblyRegistry.shutDown();
        componentRegistry.shutDown();
        super.shutDown();
        container.getManagementContext().unregisterMBean(this);
    }
    
    /**
     * @return the EnvironmentContext
     */
    protected EnvironmentContext getEnvironmentContext(){
        return container.getEnvironmentContext();
    }
    
    /**
     * @return true if the container is embedded
     */
    protected boolean isContainerEmbedded(){
        return container.isEmbedded();
    }

    protected InternalEndpoint matchEndpointByName(ServiceEndpoint[] endpoints, String endpointName) {
        InternalEndpoint result = null;
        if (endpoints != null && endpointName != null && endpointName.length() > 0) {
            for (int i = 0;i < endpoints.length;i++) {
                if (endpoints[i].getEndpointName().equals(endpointName)) {
                    result = (InternalEndpoint) endpoints[i];
                    break;
                }
            }
        }
        return result;
    }

    /**
     * @param context
     * @param serviceName
     * @param endpointName
     * @return EndPointReference
     * @throws JBIException
     */
    public ServiceEndpoint activateEndpoint(ComponentContextImpl context, 
                                            QName serviceName,
                                            String endpointName) throws JBIException {
        InternalEndpoint result = endpointRegistry.registerInternalEndpoint(context, serviceName, endpointName);
        return result;
    }

    public ServiceEndpoint[] getEndpointsForComponent(ComponentNameSpace cns) {
        return endpointRegistry.getEndpointsForComponent(cns);
    }
    
    /**
     * @param interfaceName qualified name
     * @return an array of available endpoints for the specified interface name;
     */
    public ServiceEndpoint[] getEndpointsForInterface(QName interfaceName) {
        return endpointRegistry.getEndpointsForInterface(interfaceName);
    }

    /**
     * @param provider
     * @param serviceEndpoint
     */
    public void deactivateEndpoint(ComponentContext provider, InternalEndpoint serviceEndpoint) {
        endpointRegistry.unregisterInternalEndpoint(provider, serviceEndpoint);
    }

    /**
     * Retrieve the service description metadata for the specified endpoint.
     * <p>
     * Note that the result can use either the WSDL 1.1 or WSDL 2.0 description language.
     * 
     * @param endpoint endpoint reference; must be non-null.
     * @return metadata describing endpoint, or <code>null</code> if metadata is unavailable.
     * @throws JBIException invalid endpoint reference.
     */
    public Document getEndpointDescriptor(ServiceEndpoint endpoint) throws JBIException {
        if (endpoint instanceof AbstractServiceEndpoint == false) {
            throw new JBIException("Descriptors can not be queried for external endpoints");
        }
        AbstractServiceEndpoint se = (AbstractServiceEndpoint) endpoint;
        // TODO: what if the endpoint is linked or dynamic
        ComponentMBeanImpl component = getComponent(se.getComponentNameSpace());
        return component.getComponent().getServiceDescription(endpoint);
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
        Collection connectors = getComponents();
        for (Iterator iter = connectors.iterator(); iter.hasNext();) {
            ComponentMBeanImpl connector = (ComponentMBeanImpl) iter.next();
            ServiceEndpoint se = connector.getComponent().resolveEndpointReference(epr);
            if (se != null) {
                return new DynamicEndpoint(connector.getComponentNameSpace(), se, epr);  
            }
        }
        return null;
    }

    /**
     * @param provider
     * @param externalEndpoint the external endpoint to be registered, must be non-null.
     * @throws JBIException 
     */
    public void registerExternalEndpoint(ComponentNameSpace cns, ServiceEndpoint externalEndpoint) throws JBIException {
        if (externalEndpoint != null) {
            endpointRegistry.registerExternalEndpoint(cns, externalEndpoint);
        }
    }

    /**
     * @param provider
     * @param externalEndpoint the external endpoint to be deregistered; must be non-null.
     */
    public void deregisterExternalEndpoint(ComponentNameSpace cns, ServiceEndpoint externalEndpoint) {
        endpointRegistry.unregisterExternalEndpoint(cns, externalEndpoint);
    }

    /**
     * @param service
     * @param name
     * @return endpoint
     */
    public ServiceEndpoint getEndpoint(QName service, String name) {
        return endpointRegistry.getEndpoint(service, name);
    }
    
    public ServiceEndpoint getInternalEndpoint(QName service, String name) {
        return endpointRegistry.getInternalEndpoint(service, name);
    }

    /**
     * @param serviceName
     * @return endpoints
     */
    public ServiceEndpoint[] getEndpointsForService(QName serviceName) {
        return endpointRegistry.getEndpointsForService(serviceName);
    }

    /**
     * @param interfaceName
     * @return endpoints
     */
    public ServiceEndpoint[] getExternalEndpoints(QName interfaceName) {
        return endpointRegistry.getExternalEndpointsForInterface(interfaceName);
    }

    /**
     * @param serviceName
     * @return endpoints
     */
    public ServiceEndpoint[] getExternalEndpointsForService(QName serviceName) {
        return endpointRegistry.getExternalEndpointsForService(serviceName);
    }

    /**
     * REgister a local Component
     * 
     * @param name
     * @param description
     * @param component
     * @param dc
     * @param binding
     * @param service
     * @return ComponentConnector
     * @throws JBIException
     */
    public ComponentMBeanImpl registerComponent(ComponentNameSpace name, 
                                                String description,
                                                Component component,
                                                boolean binding, 
                                                boolean service) throws JBIException {
        return componentRegistry.registerComponent(name,description, component, binding, service);
    }

    /**
     * @param component
     * @return ComponentConnector
     */
    public void deregisterComponent(ComponentMBeanImpl component) {
        componentRegistry.deregisterComponent(component);
    }

    /**
     * @return all local ComponentConnectors
     */
    public Collection getComponents() {
        return componentRegistry.getComponents();
    }

    /**
     * Get a Component
     * @param cns
     * @return the Component
     */
    public ComponentMBeanImpl getComponent(ComponentNameSpace cns) {
        return componentRegistry.getComponent(cns);
    }
    
    /**
     * Get a Component
     * @param name
     * @return the Componment
     */
    public ComponentMBeanImpl getComponent(String name) {
        ComponentNameSpace cns = new ComponentNameSpace(container.getName(), name);
        return getComponent(cns);
    }
    
    /**
     * Get a list of all engines currently installed.
     * @return array of JMX object names of all installed SEs.
     */
    public ObjectName[] getEngineComponents() {
        ObjectName[] result = null;
        List tmpList = new ArrayList();
        for (Iterator i = getComponents().iterator(); i.hasNext();){
            ComponentMBeanImpl lcc = (ComponentMBeanImpl) i.next();
            if (!lcc.isPojo() && lcc.isService() && lcc.getMBeanName() != null){
                tmpList.add(lcc.getMBeanName());
            }
        }
        result = new ObjectName[tmpList.size()];
        tmpList.toArray(result);
        return result;
        
    }
    
    /**
     * Get a list of all binding components currently installed.
     * @return array of JMX object names of all installed BCs.
     */
    public ObjectName[] getBindingComponents() {
        ObjectName[] result = null;
        List tmpList = new ArrayList();
        for (Iterator i = getComponents().iterator(); i.hasNext();){
            ComponentMBeanImpl lcc = (ComponentMBeanImpl) i.next();
            if (!lcc.isPojo() && lcc.isBinding() && lcc.getMBeanName() != null){
                tmpList.add(lcc.getMBeanName());
            }
        }
        result = new ObjectName[tmpList.size()];
        tmpList.toArray(result);
        return result;
    }

    /**
     * Get a list of all pojos currently installed.
     * @return array of JMX object names of all installed PJOJO Conponents.
     */
    public ObjectName[] getPojoComponents() {
        ObjectName[] result = null;
        List tmpList = new ArrayList();
        for (Iterator i = getComponents().iterator(); i.hasNext();){
            ComponentMBeanImpl lcc = (ComponentMBeanImpl) i.next();
            if (lcc.isPojo() && lcc.getMBeanName() != null){
                tmpList.add(lcc.getMBeanName());
            }
        }
        result = new ObjectName[tmpList.size()];
        tmpList.toArray(result);
        return result;
        
    }
    
    /**
     * Register All subscriptions
     * @param context 
     * @param as 
     */
    public void registerSubscriptions(ComponentContextImpl context,ActivationSpec as) {
        QName service = as.getService();
        String endpointName = as.getEndpoint();
        InternalEndpoint endpoint = new InternalEndpoint(context.getComponentNameSpace(), endpointName, service);
        SubscriptionSpec[] specs = as.getSubscriptions();
        if (specs != null) {
            for (int i =0; i<specs.length; i++) {
                registerSubscription(context, specs[i], endpoint);
            }
        }
    }
    
    /**
     * Deregister All subscriptions
     * @param context
     * @param as
     */
    public void deregisterSubscriptions(ComponentContextImpl context,ActivationSpec as) {
        SubscriptionSpec[] specs = as.getSubscriptions();
        if (specs != null) {
            for (int i =0; i<specs.length; i++) {
                deregisterSubscription(context,specs[i]);
            }
        }
    }
    
    /**
     * @param context 
     * @param subscription
     * @param endpoint
     */
    public void registerSubscription(ComponentContextImpl context,SubscriptionSpec subscription, ServiceEndpoint endpoint) {
        InternalEndpoint sei = (InternalEndpoint)endpoint;
        subscription.setName(context.getComponentNameSpace());
        subscriptionRegistry.registerSubscription(subscription,sei);
    }

    /**
     * @param context 
     * @param subscription
     * @return the ServiceEndpoint
     */
    public InternalEndpoint deregisterSubscription(ComponentContextImpl context,SubscriptionSpec subscription) {
        subscription.setName(context.getComponentNameSpace());
        InternalEndpoint result = subscriptionRegistry.deregisterSubscription(subscription);
        return result;
    }
    
    
    /**
     * @param exchange 
     * @return a List of matching endpoints - can return null if no matches
     */
    public List getMatchingSubscriptionEndpoints(MessageExchangeImpl exchange) {
        return subscriptionRegistry.getMatchingSubscriptionEndpoints(exchange);
    }
    
    /**
     * Register a service assembly
     * @param sa
     * @return true if not already registered
     * @throws DeploymentException 
     */
    public ServiceAssemblyLifeCycle registerServiceAssembly(ServiceAssembly sa) throws DeploymentException{
        return serviceAssemblyRegistry.register(sa);
    }
    
    /**
     * Register a service assembly
     * @param sa
     * @param sus list of deployed service units
     * @return true if not already registered
     * @throws DeploymentException 
     */
    public ServiceAssemblyLifeCycle registerServiceAssembly(ServiceAssembly sa, String[] deployedSUs) throws DeploymentException{
        return serviceAssemblyRegistry.register(sa, deployedSUs);
    }
    
    /**
     * Un-register a service assembly
     * @param saName 
     * @return true if successfully unregistered
     */
    public boolean unregisterServiceAssembly(String saName) {
        return serviceAssemblyRegistry.unregister(saName);
    }
    
    /**
     * Get a named ServiceAssembly
     * @param name
     * @return the ServiceAssembly or null if it doesn't exist
     */
    public ServiceAssemblyLifeCycle getServiceAssembly(String saName){
        return serviceAssemblyRegistry.getServiceAssembly(saName);
    }

    /**
     * Returns a list of Service Units that are currently deployed to the given component.
     * 
     * @param componentName name of the component.
     * @return List of deployed service units
     */
    public ServiceUnitLifeCycle[] getDeployedServiceUnits(String componentName)  {
        Collection sus = serviceUnits.values();
        List tmpList = new ArrayList();
        for (Iterator iter = sus.iterator(); iter.hasNext();) {
            ServiceUnitLifeCycle su = (ServiceUnitLifeCycle) iter.next();
            if (su.getComponentName().equals(componentName)) {
                tmpList.add(su);
            }
        }
        ServiceUnitLifeCycle[] result = new ServiceUnitLifeCycle[tmpList.size()];
        tmpList.toArray(result);
        return result;
    }

    /**
     * Returns a list of Service Assemblies deployed to the JBI enviroment.
     * 
     * @return list of Service Assembly Name's.
     */
    public String[] getDeployedServiceAssemblies()  {
        return serviceAssemblyRegistry.getDeployedServiceAssemblies();
    }
    
    /**
     * Returns a list of Service Assemblies that contain SUs for the given component.
     * 
     * @param componentName name of the component.
     * @return list of Service Assembly names.
     */
    public String[] getDeployedServiceAssembliesForComponent(String componentName)  {
        return serviceAssemblyRegistry.getDeployedServiceAssembliesForComponent(componentName);
    }

    /**
     * Returns a list of components(to which SUs are targeted for) in a Service Assembly.
     * 
     * @param saName name of the service assembly.
     * @return list of component names.
     */
    public String[] getComponentsForDeployedServiceAssembly(String saName) {
        return serviceAssemblyRegistry.getComponentsForDeployedServiceAssembly(saName);
    }

    /**
     * Returns a boolean value indicating whether the SU is currently deployed.
     * 
     * @param componentName - name of component.
     * @param suName - name of the Service Unit.
     * @return boolean value indicating whether the SU is currently deployed.
     */
    public boolean isSADeployedServiceUnit(String componentName, String suName)  {
        return serviceAssemblyRegistry.isDeployedServiceUnit(componentName, suName);
    }
    
    /**
     * Get a ServiceUnit by its key.
     * 
     * @param suKey the key of the service unit
     * @return the ServiceUnit or null of it doesn't exist
     */
    public ServiceUnitLifeCycle getServiceUnit(String suKey) {
        return (ServiceUnitLifeCycle) serviceUnits.get(suKey);
    }
    
    /**
     * Register a ServiceUnit.
     * 
     * @param su the service unit to register
     * @param serviceAssembly the service assembly the service unit belongs to 
     * @return the service unit key
     */
    public String registerServiceUnit(ServiceUnit su, String serviceAssembly) {
        ServiceUnitLifeCycle sulc = new ServiceUnitLifeCycle(su, serviceAssembly, this);
        this.serviceUnits.put(sulc.getKey(), sulc);
        try {
            ObjectName objectName = getContainer().getManagementContext().createObjectName(sulc);
            getContainer().getManagementContext().registerMBean(objectName, sulc, ServiceUnitMBean.class);
        } catch (JMException e) {
            log.error("Could not register MBean for service unit", e);
        }
        return sulc.getKey();
    }
    
    /**
     * Unregister a ServiceUnit by its key.
     * 
     * @param suKey the key of the service unit
     */
    public void unregisterServiceUnit(String suKey) {
        ServiceUnitLifeCycle sulc = (ServiceUnitLifeCycle) this.serviceUnits.remove(suKey);
        if (sulc != null) {
            try {
                getContainer().getManagementContext().unregisterMBean(sulc);
            } catch (JBIException e) {
                log.error("Could not unregister MBean for service unit", e);
            }
        }
    }

    public void registerEndpointConnection(QName fromSvc, String fromEp, QName toSvc, String toEp, String link) throws JBIException {
        endpointRegistry.registerEndpointConnection(fromSvc, fromEp, toSvc, toEp, link);
    }

    public void unregisterEndpointConnection(QName fromSvc, String fromEp) {
        endpointRegistry.unregisterEndpointConnection(fromSvc, fromEp);
    }
    
    public void registerInterfaceConnection(QName fromItf, QName toSvc, String toEp) throws JBIException {
        endpointRegistry.registerInterfaceConnection(fromItf, toSvc, toEp);
    }

    public void unregisterInterfaceConnection(QName fromItf) {
        endpointRegistry.unregisterInterfaceConnection(fromItf);
    }

    public void registerRemoteEndpoint(ServiceEndpoint endpoint) {
        endpointRegistry.registerRemoteEndpoint((InternalEndpoint) endpoint);
    }

    public void unregisterRemoteEndpoint(ServiceEndpoint endpoint) {
        endpointRegistry.unregisterRemoteEndpoint((InternalEndpoint) endpoint);
    }

    public void checkPendingAssemblies() {
        try {
            getContainer().getWorkManager().scheduleWork(new Work() {
                public void release() {
                }
                public void run() {
                    startPendingAssemblies();
                }
            });
        } catch (WorkException e) {
            log.error("Could not schedule work", e);
        }
    }

    public void addPendingAssembly(ServiceAssemblyLifeCycle sa) {
        if (!pendingAssemblies.contains(sa)) {
            pendingAssemblies.add(sa);
        }
    }
    
    protected synchronized void startPendingAssemblies() {
        for (Iterator iter = pendingAssemblies.iterator(); iter.hasNext();) {
            ServiceAssemblyLifeCycle sa = (ServiceAssemblyLifeCycle) iter.next();
            ServiceUnitLifeCycle[] sus = sa.getDeployedSUs();
            boolean ok = true;
            for (int i = 0; i < sus.length; i++) {
                ComponentMBeanImpl c = getComponent(sus[i].getComponentName());
                if (c == null || !c.isStarted()) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                try {
                    sa.restore();
                    pendingAssemblies.remove(sa);
                } catch (Exception e) {
                    log.error("Error trying to restore service assembly state", e);
                }
            }
        }
    }

}
