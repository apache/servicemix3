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
package org.apache.servicemix.sca;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.apache.servicemix.common.ServiceUnit;
import org.apache.tuscany.common.resource.loader.ResourceLoader;
import org.apache.tuscany.common.resource.loader.ResourceLoaderFactory;
import org.apache.tuscany.core.runtime.EventContext;
import org.apache.tuscany.core.runtime.TuscanyModuleComponentContext;
import org.apache.tuscany.core.runtime.config.ConfigurationLoader;
import org.apache.tuscany.core.runtime.config.impl.EMFConfigurationLoader;
import org.apache.tuscany.core.runtime.impl.EventContextImpl;
import org.apache.tuscany.core.runtime.impl.TuscanyModuleComponentContextImpl;
import org.apache.tuscany.core.runtime.scopes.DefaultScopeStrategy;
import org.apache.tuscany.core.runtime.webapp.TuscanyWebAppRuntime;
import org.apache.tuscany.model.assembly.AssemblyFactory;
import org.apache.tuscany.model.assembly.AssemblyModelContext;
import org.apache.tuscany.model.assembly.EntryPoint;
import org.apache.tuscany.model.assembly.Module;
import org.apache.tuscany.model.assembly.ModuleComponent;
import org.apache.tuscany.model.assembly.impl.AssemblyFactoryImpl;
import org.apache.tuscany.model.assembly.impl.AssemblyModelContextImpl;
import org.apache.tuscany.model.types.wsdl.WSDLTypeHelper;
import org.osoa.sca.model.Binding;
import org.osoa.sca.model.JbiBinding;
import org.w3c.dom.Document;

public class ScaServiceUnit extends ServiceUnit {

	protected static final ThreadLocal<ScaServiceUnit> SERVICE_UNIT = new ThreadLocal<ScaServiceUnit>();
	
	public static ScaServiceUnit getCurrentScaServiceUnit() {
		return SERVICE_UNIT.get();
	}
	
	protected TuscanyWebAppRuntime tuscanyRuntime;
	protected ClassLoader classLoader;
	
	public void init() throws Exception {
		createScaRuntime();
		createEndpoints();
	}
	
	protected void createScaRuntime() throws Exception {
		File root = new File(getRootPath());
		File[] files = root.listFiles(new JarFileFilter());
		URL[] urls = new URL[files.length + 1];
		for (int i = 0; i < files.length; i++) {
			urls[i] = files[i].toURL();
		}
		urls[urls.length - 1] = root.toURL();
		classLoader = new URLClassLoader(urls, getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
		
        ResourceLoader resourceLoader = ResourceLoaderFactory.getResourceLoader(classLoader);
        AssemblyModelContext modelContext = new AssemblyModelContextImpl(resourceLoader);
        ConfigurationLoader moduleComponentLoader = new EMFConfigurationLoader(modelContext);
        ModuleComponent moduleComponent = moduleComponentLoader.loadModuleComponent(getName(), getName());
        EventContext eventContext = new EventContextImpl();
        DefaultScopeStrategy scopeStrategy = new DefaultScopeStrategy();
        TuscanyModuleComponentContext moduleComponentContext = new TuscanyModuleComponentContextImpl(moduleComponent, eventContext, scopeStrategy, modelContext);
        tuscanyRuntime = new TuscanyWebAppRuntime(moduleComponentContext);
	}
	
	protected void createEndpoints() throws Exception {
        AssemblyFactory assemblyFactory = new AssemblyFactoryImpl();
        TuscanyModuleComponentContext moduleComponentContext = tuscanyRuntime.getModuleComponentContext(); 
        Module module = moduleComponentContext.getModuleComponent().getModuleImplementation();
        for (Iterator i = module.getEntryPoints().iterator(); i.hasNext();) {
            EntryPoint entryPoint = (EntryPoint) i.next();
            Binding binding = (Binding) entryPoint.getBindings().get(0);
            if (binding instanceof JbiBinding) {
                JbiBinding jbiBinding = (JbiBinding) binding;
                Definition definition = null;
                Document description = null;
                QName serviceName = null;
                QName interfaceName = null;
                String endpointName = null;
                QName qname = assemblyFactory.createQName(jbiBinding.getPort());
                if (qname != null) {
                	try {
	                    WSDLTypeHelper typeHelper = moduleComponentContext.getAssemblyModelContext().getWSDLTypeHelper();
	                    definition = typeHelper.getWSDLDefinition(qname.getNamespaceURI());
	                    for (Iterator itSvc = definition.getServices().values().iterator(); itSvc.hasNext();) {
	                    	Service svc = (Service) itSvc.next();
	                    	if (svc.getQName().getNamespaceURI().equals(qname.getNamespaceURI())) {
	                    		for (Iterator itPort = svc.getPorts().values().iterator(); itPort.hasNext();) {
	                    			Port port = (Port) itPort.next();
	                    			if (port.getName().equals(qname.getLocalPart())) {
	                    				serviceName = svc.getQName();
	                    				endpointName = port.getName();
	                    				interfaceName = port.getBinding().getPortType().getQName();
	                    			}
	                    		}
	                    	}
	                    }
	                    javax.wsdl.Binding b = definition.getBinding(qname);
	                    description = WSDLFactory.newInstance().newWSDLWriter().getDocument(definition);
                	} catch (Exception e) {
                		// TODO warn
                	}
                	if (serviceName == null) {
                		serviceName = new QName(qname.getNamespaceURI(), entryPoint.getName());
                	}
                	if (endpointName == null) {
                		endpointName = qname.getLocalPart();
                	}
                }
                ScaEndpoint endpoint = new ScaEndpoint(entryPoint);
                endpoint.setServiceUnit(this);
                endpoint.setService(serviceName);
                endpoint.setEndpoint(endpointName);
                endpoint.setInterfaceName(interfaceName);
                endpoint.setDefinition(definition);
                endpoint.setDescription(description);
                addEndpoint(endpoint);
            }
        }
	}
	
	private static class JarFileFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
	}

	public TuscanyWebAppRuntime getTuscanyRuntime() {
		return tuscanyRuntime;
	}

	@Override
	public void start() throws Exception {
		tuscanyRuntime.start();
		try {
			SERVICE_UNIT.set(this);
			tuscanyRuntime.getModuleComponentContext().start();
			tuscanyRuntime.getModuleComponentContext().fireEvent(EventContext.MODULE_START, null);
		} finally {
			tuscanyRuntime.stop();
			SERVICE_UNIT.set(null);
		}
		super.start();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		tuscanyRuntime.start();
		try {
			tuscanyRuntime.getModuleComponentContext().fireEvent(EventContext.MODULE_STOP, null);
			tuscanyRuntime.getModuleComponentContext().stop();
		} finally {
			tuscanyRuntime.stop();
		}
	}

}
