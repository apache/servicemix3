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
package org.apache.servicemix.common.xbean;

import org.apache.servicemix.common.ServiceUnit;
import org.xbean.kernel.Kernel;
import org.xbean.kernel.ServiceName;
import org.xbean.kernel.ServiceNotFoundException;
import org.xbean.server.spring.configuration.SpringConfigurationServiceFactory;

import javax.jbi.JBIException;

public class XBeanServiceUnit extends ServiceUnit {

    private Kernel kernel;
    private ServiceName configuration;

    /**
     * @return Returns the kernel.
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * @param kernel The kernel to set.
     */
    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    public ServiceName getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ServiceName configuration) {
        this.configuration = configuration;
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.common.ServiceUnit#shutDown()
     */
    public void shutDown() throws JBIException {
        if (kernel != null) {
            kernel.destroy();
        }
        super.shutDown();
    }
    
    public ClassLoader getConfigurationClassLoader() throws ServiceNotFoundException {
        if (kernel != null) {
            Object o = kernel.getServiceFactory(configuration);
            SpringConfigurationServiceFactory scsf = (SpringConfigurationServiceFactory) o;
            return scsf.getApplicationContext().getClassLoader();
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }
    
}
