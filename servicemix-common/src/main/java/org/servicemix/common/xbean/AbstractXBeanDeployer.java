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
package org.servicemix.common.xbean;

import org.servicemix.common.AbstractDeployer;
import org.servicemix.common.BaseComponent;
import org.servicemix.common.Endpoint;
import org.servicemix.common.EndpointSupport;
import org.servicemix.common.ServiceUnit;
import org.xbean.kernel.Kernel;
import org.xbean.kernel.KernelFactory;
import org.xbean.kernel.ServiceName;
import org.xbean.server.repository.FileSystemRepository;
import org.xbean.server.spring.configuration.ClassLoaderXmlPreprocessor;
import org.xbean.server.spring.loader.SpringLoader;

import javax.jbi.management.DeploymentException;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AbstractXBeanDeployer extends AbstractDeployer {

    public AbstractXBeanDeployer(BaseComponent component) {
        super(component);
    }
    
    protected String getXBeanFile() {
        return "xbean";
    }

    /* (non-Javadoc)
     * @see org.servicemix.common.Deployer#canDeploy(java.lang.String, java.lang.String)
     */
    public boolean canDeploy(String serviceUnitName, String serviceUnitRootPath) {
        File xbean = new File(serviceUnitRootPath, getXBeanFile() + ".xml");
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for " + xbean + ": " + xbean.exists());
        }
        return xbean.exists();
    }

    /* (non-Javadoc)
     * @see org.servicemix.common.Deployer#deploy(java.lang.String, java.lang.String)
     */
    public ServiceUnit deploy(String serviceUnitName, String serviceUnitRootPath) throws DeploymentException {
        Kernel kernel = KernelFactory.newInstance().createKernel(component.getComponentName() + "/" + serviceUnitName);
        try {
            // Create service unit
            XBeanServiceUnit su = new XBeanServiceUnit();
            su.setKernel(kernel);
            su.setComponent(component);
            su.setName(serviceUnitName);
            su.setRootPath(serviceUnitRootPath);
            // Load configuration
            FileSystemRepository repository = new FileSystemRepository(new File(serviceUnitRootPath));
            ClassLoaderXmlPreprocessor classLoaderXmlPreprocessor = new ClassLoaderXmlPreprocessor(repository);
            List xmlPreprocessors = Collections.singletonList(classLoaderXmlPreprocessor);
            SpringLoader springLoader = new SpringLoader();
            springLoader.setKernel(kernel);
            springLoader.setBaseDir(new File(serviceUnitRootPath));
            springLoader.setXmlPreprocessors(xmlPreprocessors);
            ServiceName configurationName = springLoader.load(getXBeanFile());
            kernel.startService(configurationName);
            // Retrieve endpoints
            List services = getServices(kernel);
            if (services == null || services.size() == 0) {
                throw failure("deploy", "No endpoints found", null);
            }
            for (Iterator iter = services.iterator(); iter.hasNext();) {
                Endpoint endpoint = (Endpoint) iter.next();
                endpoint.setServiceUnit(su);
                if (validate(endpoint)) {
                    if (su.getEndpoint(EndpointSupport.getKey(endpoint)) != null) {
                        throw failure("deploy", "More than one endpoint found for: " + EndpointSupport.getKey(endpoint), null);
                    }
                    su.addEndpoint(endpoint);
                } else {
                    logger.warn("Endpoint " + endpoint + "has not been validated");
                }
            }
            if (su.getEndpoints().size() == 0) {
                throw failure("deploy", "No endpoint found", null);
            }
            return su;
        } catch (Exception e) {
            kernel.destroy();
            if (e instanceof DeploymentException) {
                throw ((DeploymentException) e);
            } else {
                throw failure("deploy", "Could not deploy xbean service unit", e);
            }
        }
    }
    
    protected List getServices(Kernel kernel) {
        return kernel.getServices(Endpoint.class);
    }
    
    protected boolean validate(Endpoint endpoint) throws DeploymentException {
        return true;
    }

}
