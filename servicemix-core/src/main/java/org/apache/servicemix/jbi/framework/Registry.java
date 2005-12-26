/** 
 * <a href="http://servicemix.org">ServiceMix: The open source ESB</a> 
 * 
 * Copyright 2005 RAJD Consultancy Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.apache.servicemix.jbi.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.management.DeploymentException;
import javax.jbi.management.LifeCycleMBean;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.EnvironmentContext;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.container.SubscriptionSpec;
import org.apache.servicemix.jbi.deployment.ServiceAssembly;
import org.apache.servicemix.jbi.management.BaseLifeCycle;
import org.apache.servicemix.jbi.messaging.DeliveryChannelImpl;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry - state infomation including running state, SA's deployed etc.
 * 
 * @version $Revision$
 */
public class Registry extends BaseLifeCycle {
    
    private JBIContainer container;
    private ComponentRegistry componentRegistry;
    private EndpointRegistry endpointRegistry;
    private SubscriptionRegistry subscriptionRegistry;
    private ServiceAssemblyRegistry serviceAssemblyRegistry;
    private List componentPacketListeners;

    /**
     * Constructor
     */
    public Registry() {
        this.componentRegistry = new ComponentRegistry(this);
        this.endpointRegistry = new EndpointRegistry(componentRegistry);
        this.subscriptionRegistry = new SubscriptionRegistry();
        this.serviceAssemblyRegistry = new ServiceAssemblyRegistry(this);
        this.componentPacketListeners = new CopyOnWriteArrayList();
    }
    
    /**
     * Get the description
     * @return description
     */
    public String getDescription(){
        return "Registry of Components/SU's and Endpoints";
    }

    /**
     * Initialize the Registry
     * 
     * @param container
     * @throws JBIException
     */
    public void init(JBIContainer container) throws JBIException {
        this.container = container;
        this.componentRegistry.setContainerName(container.getName());
        this.subscriptionRegistry.init(this);
        container.getManagementContext().registerSystemService(this, LifeCycleMBean.class);
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
    public synchronized ServiceEndpoint activateEndpoint(ComponentContextImpl context, QName serviceName,
            String endpointName) throws JBIException {
        InternalEndpoint result = endpointRegistry.activateEndpoint(context, serviceName, endpointName);
        if (result != null) {
            ComponentConnector cc = componentRegistry.getComponentConnector(result.getComponentNameSpace());
            if (cc != null) {
                fireComponentPacketEvent(cc, ComponentPacketEvent.STATE_CHANGE);
            }
        }
        return result;
    }

    /**
     * @param interfaceName qualified name
     * @return an array of available endpoints for the specified interface name;
     */
    public ServiceEndpoint[] getEndpoints(QName interfaceName) {
        return endpointRegistry.getEndpoints(interfaceName);
    }

    /**
     * @param provider
     * @param serviceEndpoint
     */
    public void deactivateEndpoint(ComponentContext provider, InternalEndpoint serviceEndpoint) {
        endpointRegistry.deactivateEndpoint(provider, serviceEndpoint);
        if (serviceEndpoint != null) {
            ComponentConnector cc = componentRegistry.getComponentConnector(serviceEndpoint.getComponentNameSpace());
            if (cc != null) {
                fireComponentPacketEvent(cc, ComponentPacketEvent.STATE_CHANGE);
            }
        }
    }

    /**
     * @param endpoint endpoint reference; must be non-null.
     * @return metadata describing endpoint, or <code>null</code> if metadata is unavailable.
     * @throws JBIException invalid endpoint reference.
     */
    public Document getEndpointDescriptor(ServiceEndpoint endpoint) throws JBIException {
        return endpointRegistry.getEndpointDescriptor(endpoint);
    }

    /**
     * @param epr
     * @return endpoint
     */
    public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
        return endpointRegistry.resolveEndpointReference(epr);
    }

    /**
     * @param provider
     * @param externalEndpoint the external endpoint to be registered, must be non-null.
     */
    public void registerExternalEndpoint(ComponentContextImpl provider, ServiceEndpoint externalEndpoint) {
        if (externalEndpoint != null) {
            endpointRegistry.registerExternalEndpoint(provider, externalEndpoint);
            ComponentConnector cc = componentRegistry.getComponentConnector(provider.getComponentNameSpace());
            if (cc != null) {
                fireComponentPacketEvent(cc, ComponentPacketEvent.STATE_CHANGE);
            }
        }
    }

    /**
     * @param provider
     * @param externalEndpoint the external endpoint to be deregistered; must be non-null.
     */
    public void deregisterExternalEndpoint(ComponentContextImpl provider, ServiceEndpoint externalEndpoint) {
        endpointRegistry.deregisterExternalEndpoint(provider, externalEndpoint);
        if (externalEndpoint != null) {
            ComponentConnector cc = componentRegistry.getComponentConnector(provider.getComponentNameSpace());
            if (cc != null) {
                fireComponentPacketEvent(cc, ComponentPacketEvent.STATE_CHANGE);
            }
        }
    }

    /**
     * @param service
     * @param name
     * @return endpoint
     */
    public ServiceEndpoint getEndpoint(QName service, String name) {
        return endpointRegistry.getEndpoint(service, name);
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
        return endpointRegistry.getExternalEndpoints(interfaceName);
    }

    /**
     * @param serviceName
     * @return endpoints
     */
    public ServiceEndpoint[] getExternalEndpointsForService(QName serviceName) {
        return endpointRegistry.getExternalEndpointsForService(serviceName);
    }

    /**
     * Utility method to get a ComponentConnector from an InterfaceName
     * 
     * @param interfaceName
     * @return the ComponentConnector
     */
    public ComponentConnector getComponentConnector(QName interfaceName) {
        return endpointRegistry.getComponentConnector(interfaceName);
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
    public LocalComponentConnector registerComponent(ComponentNameSpace name, String description,Component component,
            DeliveryChannelImpl dc, boolean binding, boolean service) throws JBIException {
        LocalComponentConnector result = componentRegistry.registerComponent(name,description, component, dc, binding, service);
        if (result != null) {
            fireComponentPacketEvent(result, ComponentPacketEvent.ACTIVATED);
        }
        return result;
    }

    /**
     * @param component
     * @return ComponentConnector
     */
    public ComponentConnector deregisterComponent(Component component) {
        ComponentConnector result = componentRegistry.deregisterComponent(component);
        if (result != null) {
            fireComponentPacketEvent(result, ComponentPacketEvent.DEACTIVATED);
        }
        return result;
    }

    /**
     * @return all local ComponentConnectors
     */
    public Collection getLocalComponentConnectors() {
        return componentRegistry.getLocalComponentConnectors();
    }

    /**
     * Add a listener
     * 
     * @param l
     */
    public void addComponentPacketListener(ComponentPacketEventListener l) {
        this.componentPacketListeners.add(l);
    }

    /**
     * remove a listener
     * 
     * @param l
     */
    public void removeComponentPacketListener(ComponentPacketEventListener l) {
        this.componentPacketListeners.remove(l);
    }

    /**
     * Get a registered ComponentConnector from it's id
     * 
     * @param id
     * @return the ComponentConnector or null
     */
    public ComponentConnector getComponentConnector(ComponentNameSpace id) {
        return componentRegistry.getComponentConnector(id);
    }
    
    
    /**
     * For distributed containers, get a ComponentConnector by round-robin
     * @param id
     * @return the ComponentConnector or null
     */
    public ComponentConnector getLoadBalancedComponentConnector(ComponentNameSpace id){
        return componentRegistry.getLoadBalancedComponentConnector(id);
    }

    /**
     * Add a ComponentConnector to ComponentRegistry Should be called for adding remote ComponentConnectors from other
     * Containers
     * 
     * @param connector
     */
    public void addRemoteComponentConnector(ComponentConnector connector) {
        componentRegistry.addComponentConnector(connector);
    }

    /**
     * Remove a ComponentConnector
     * 
     * @param connector
     */
    public void removeRemoteComponentConnector(ComponentConnector connector) {
        componentRegistry.removeComponentConnector(connector.getComponentNameSpace());
    }

    /**
     * Update a ComponentConnector
     * 
     * @param connector
     */
    public void updateRemoteComponentConnector(ComponentConnector connector) {
        componentRegistry.updateConnector(connector);
    }

    /**
     * Get a locally create ComponentConnector
     * 
     * @param id - id of the ComponentConnector
     * @return ComponentConnector or null if not found
     */
    public LocalComponentConnector getLocalComponentConnector(ComponentNameSpace id) {
        return componentRegistry.getLocalComponentConnector(id);
    }
    
    /**
     * Find existence of a Component locally registered to this Container
     * @param componentName
     * @return true if the Component exists
     */
    public boolean isLocalComponentRegistered(String componentName){
        return componentRegistry.isLocalComponentRegistered(componentName);
    }

    /**
     * Get the ComponentConnector associated with the componet
     * 
     * @param component
     * @return the associated ComponentConnector
     */
    public LocalComponentConnector getComponentConnector(Component component) {
        return componentRegistry.getComponentConnector(component);
    }

    /**
     * Get a Component
     * @param cns
     * @return the Component
     */
    public Component getComponent(ComponentNameSpace cns) {
        return componentRegistry.getComponent(cns);
    }
    
    /**
     * Get a Component
     * @param name
     * @return the Componment
     */
    public Component getComponent(String name) {
        ComponentNameSpace cns = new ComponentNameSpace(container.getName(), name, name);
        return getComponent(cns);
    }
    
    /**
     * Get the ObjectName for the MBean managing the Component
     * @param name
     * @return ObjectName or null
     */
    public ObjectName getComponentObjectName(String name){
        ObjectName  result = null;
        ComponentNameSpace cns = new ComponentNameSpace(container.getName(), name, name);
        LocalComponentConnector lcc = getLocalComponentConnector(cns);
        if (lcc != null){
            result = lcc.getMbeanName();
        }
        return result;
        
    }
    
    /**
     * Check if a given JBI Installable Component is a Binding Component.
     * @param name - the unique name of the component
     * @return true if the component is a binding
     */
    public boolean isBinding(String name){
        boolean result = false;
        ComponentNameSpace cns = new ComponentNameSpace(container.getName(), name, name);
        LocalComponentConnector lcc = getLocalComponentConnector(cns);
        if (lcc != null){
            result = lcc.isBinding();
        }
        return result;
    }
    
    /**
     * Check if a given JBI Component is a service engine.
     * @param name - the unique name of the component
     * @return true if the component is a service engine
     */
    public boolean isEngine(String name){
        boolean result = false;
        ComponentNameSpace cns = new ComponentNameSpace(container.getName(), name, name);
        LocalComponentConnector lcc = getLocalComponentConnector(cns);
        if (lcc != null){
            result = lcc.isService();
        }
        return result;
    }
    
    /**
     * Get a list of all engines currently installed.
     * @return array of JMX object names of all installed SEs.
     */
    public ObjectName[] getEngineComponents(){
        ObjectName[] result = null;
        List tmpList = new ArrayList();
        for (Iterator i = getLocalComponentConnectors().iterator(); i.hasNext();){
            LocalComponentConnector lcc = (LocalComponentConnector) i.next();
            if (lcc.isService() && lcc.getMbeanName() != null){
                tmpList.add(lcc.getMbeanName());
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
    public ObjectName[] getBindingComponents(){
        ObjectName[] result = null;
        List tmpList = new ArrayList();
        for (Iterator i = getLocalComponentConnectors().iterator(); i.hasNext();){
            LocalComponentConnector lcc = (LocalComponentConnector) i.next();
            if (lcc.isBinding() && lcc.getMbeanName() != null){
                tmpList.add(lcc.getMbeanName());
            }
        }
        result = new ObjectName[tmpList.size()];
        tmpList.toArray(result);
        return result;
    }


    /**
     * Get set of Components
     * 
     * @return a Set of Component objects
     */
    public Set getComponents() {
        return componentRegistry.getComponents();
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
        if (sei != null) {
            ComponentConnector cc = componentRegistry.getComponentConnector(sei.getComponentNameSpace());
            if (cc != null) {
                fireComponentPacketEvent(cc, ComponentPacketEvent.STATE_CHANGE);
            }
        }
    }

    /**
     * @param context 
     * @param subscription
     * @return the ServiceEndpoint
     */
    public InternalEndpoint deregisterSubscription(ComponentContextImpl context,SubscriptionSpec subscription) {
        subscription.setName(context.getComponentNameSpace());
        InternalEndpoint result = subscriptionRegistry.deregisterSubscription(subscription);
        if (result != null) {
            ComponentConnector cc = componentRegistry.getComponentConnector(result.getComponentNameSpace());
            if (cc != null) {
                fireComponentPacketEvent(cc, ComponentPacketEvent.STATE_CHANGE);
            }
        }
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
     * Start a ServiceAssembly
     * @param serviceAssemblyName
     * @return status
     * @throws DeploymentException 
     */
    public String startServiceAssembly(String serviceAssemblyName) throws DeploymentException{
        return serviceAssemblyRegistry.start(serviceAssemblyName);
    }
    
    /**
     * Stop a ServiceAssembly
     * @param serviceAssemblyName
     * @return status
     * @throws DeploymentException 
     */
    public String stopServiceAssembly(String serviceAssemblyName) throws DeploymentException{
        return serviceAssemblyRegistry.stop(serviceAssemblyName);
    }
    
    /**
     * Shutdown a ServiceAssembly
     * @param serviceAssemblyName
     * @return status
     * @throws DeploymentException 
     */
    public String shutDownServiceAssembly(String serviceAssemblyName) throws DeploymentException{
        return serviceAssemblyRegistry.shutDown(serviceAssemblyName);
    }
    
    /**
     * Get the lifecycle state of a service assembly
     * @param serviceAssemblyName
     * @return status
     */
    public String getServiceAssemblyState(String serviceAssemblyName){
        return serviceAssemblyRegistry.getState(serviceAssemblyName);
    }
    
    
    /**
     * Register a service assembly
     * @param sa
     * @return true if not already registered
     * @throws DeploymentException 
     */
    public boolean registerServiceAssembly(ServiceAssembly sa) throws DeploymentException{
        return serviceAssemblyRegistry.register(sa);
    }
    
    /**
     * Un-register a service assembly
     * @param sa
     * @return true if successfully unregistered
     */
    public boolean unregisterServiceAssembly(ServiceAssembly sa) {
        return serviceAssemblyRegistry.unregister(sa);
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
    public ServiceAssembly getServiceAssembly(String name){
        return serviceAssemblyRegistry.get(name);
    }
    
    /**
     * Returns a list of Service Units that are currently deployed to the given component.
     * 
     * @param componentName name of the component.
     * @return List of deployed ASA Ids.
     */
    public String[] getSADeployedServiceUnitList(String componentName)  {
        return serviceAssemblyRegistry.getDeployedServiceUnitList(componentName);
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
    
    

    protected void fireComponentPacketEvent(ComponentConnector cc, int status) {
        if (!componentPacketListeners.isEmpty()) {
            ComponentPacketEvent event = new ComponentPacketEvent(cc.getComponentPacket(), status);
            for (Iterator i = componentPacketListeners.iterator();i.hasNext();) {
                ComponentPacketEventListener l = (ComponentPacketEventListener) i.next();
                l.onEvent(event);
            }
        }
    }
}
