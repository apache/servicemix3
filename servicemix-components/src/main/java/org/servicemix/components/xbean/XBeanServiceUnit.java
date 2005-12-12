/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
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
package org.servicemix.components.xbean;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.management.DeploymentException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.servicemix.components.AbstractComponent;
import org.servicemix.components.ServiceUnit;
import org.xbean.kernel.Kernel;
import org.xbean.kernel.KernelFactory;
import org.xbean.kernel.ServiceName;
import org.xbean.server.repository.FileSystemRepository;
import org.xbean.server.spring.configuration.ClassLoaderXmlPreprocessor;
import org.xbean.server.spring.loader.SpringLoader;

public class XBeanServiceUnit extends ServiceUnit {

    private Kernel kernel;
    protected ComponentContext context;
    protected AbstractComponent component;
    protected List services;
    protected Map endpoints = new HashMap();
    
    public XBeanServiceUnit(AbstractComponent component, String serviceUnitName, String serviceUnitRootPath) throws Exception {
        this.component = component;
        this.context = component.getContext();
        setName(serviceUnitName);
        setRootPath(serviceUnitRootPath);
        
        kernel = KernelFactory.newInstance().createKernel(serviceUnitName);
        boolean loaded = false;
        try {
            FileSystemRepository repository = new FileSystemRepository(new File(getRootPath()));
            ClassLoaderXmlPreprocessor classLoaderXmlPreprocessor = new ClassLoaderXmlPreprocessor(repository);
            List xmlPreprocessors = Collections.singletonList(classLoaderXmlPreprocessor);
            SpringLoader springLoader = new SpringLoader();
            springLoader.setKernel(kernel);
            springLoader.setBaseDir(new File(getRootPath()));
            springLoader.setXmlPreprocessors(xmlPreprocessors);
            ServiceName configurationName = springLoader.load("xbean");
            kernel.startService(configurationName);
            services = kernel.getServices(EndpointSpec.class);
            if (services == null || services.size() == 0) {
                throw new DeploymentException("no endpoints specified");
            }
            loaded = true;
        } finally {
            if (!loaded) {
                kernel.destroy();
            }
        }
        
    }

    public void start() throws JBIException {
        super.start();
        for (Iterator iter = services.iterator(); iter.hasNext();) {
            EndpointSpec es = (EndpointSpec) iter.next();
            ServiceEndpoint ep = this.context.activateEndpoint(es.getServiceName(), es.getEndpointName());
            endpoints.put(getKey(es), ep);
        }
    }

    public void stop() throws JBIException {
        for (Iterator iter = services.iterator(); iter.hasNext();) {
            EndpointSpec es = (EndpointSpec) iter.next();
            this.context.deactivateEndpoint(this.context.getEndpoint(es.getServiceName(), es.getEndpointName()));
            
        }
        super.stop();
    }

    public void shutDown() throws JBIException {
        kernel.destroy();
        super.shutDown();
    }
    
    public List getServices() {
        return services;
    }

    protected String getKey(QName service, String endpoint) {
        return service.toString() + "|" + endpoint;
    }
    
    protected String getKey(ServiceEndpoint ep) {
        return getKey(ep.getServiceName(), ep.getEndpointName());
    }
    
    protected String getKey(EndpointSpec es) {
        return getKey(es.getServiceName(), es.getEndpointName());
    }
    
}
