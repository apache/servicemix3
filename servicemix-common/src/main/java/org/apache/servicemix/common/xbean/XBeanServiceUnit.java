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
package org.apache.servicemix.common.xbean;

import org.apache.servicemix.common.ServiceUnit;
import org.apache.xbean.kernel.Kernel;
import org.apache.xbean.kernel.ServiceName;
import org.apache.xbean.kernel.ServiceNotFoundException;
import org.apache.xbean.server.spring.configuration.SpringConfigurationServiceFactory;

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
        super.shutDown();
        if (kernel != null) {
            kernel.destroy();
        }
    }
    
    public ClassLoader getConfigurationClassLoader() throws ServiceNotFoundException {
        ClassLoader cl = null;
        if (kernel != null) {
            Object o = kernel.getServiceFactory(configuration);
            SpringConfigurationServiceFactory scsf = (SpringConfigurationServiceFactory) o;
            cl = scsf.getApplicationContext().getClassLoader();
        } 
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
        }
        if (cl == null) {
            cl = getClass().getClassLoader();
        }
        return cl;
    }
    
}
