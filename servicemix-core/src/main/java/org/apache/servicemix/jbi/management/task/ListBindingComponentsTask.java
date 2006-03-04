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
package org.apache.servicemix.jbi.management.task;

import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;
import org.apache.tools.ant.BuildException;

/**
 * ListBindingComponentsTask
 *
 * @version $Revision: 
 */
public class ListBindingComponentsTask extends JbiTask {
    
    private String sharedLibraryName;
    private String serviceAssemblyName;
    private String bindingComponentName;
    private String state;

    /**
     *
     * @return shared library name
     */
    public String getSharedLibraryName() {
        return sharedLibraryName;
    }

    /**
     *
     * @param sharedLibraryName
     */
    public void setSharedLibraryName(String sharedLibraryName) {
        this.sharedLibraryName = sharedLibraryName;
    }

    /**
     *
     * @return service assembly name
     */
    public String getServiceAssemblyName() {
        return serviceAssemblyName;
    }

    /**
     *
     * @param serviceAssemblyName
     */
    public void setServiceAssemblyName(String serviceAssemblyName) {
        this.serviceAssemblyName = serviceAssemblyName;
    }

    /**
     *
     * @return binding component name
     */
    public String getBindingComponentName() {
        return bindingComponentName;
    }

    /**
     *
     * @param bindingComponentName
     */
    public void setBindingComponentName(String bindingComponentName) {
        this.bindingComponentName = bindingComponentName;
    }

    /**
     *
     * @return component state
     */
    public String getState() {
        return state;
    }

    /**
     *
     * @param state Sets the component state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * execute the task
     * 
     * @throws BuildException
     */
    public void doExecute(AdminCommandsServiceMBean acs) throws Exception {
        String result = acs.listComponents(true, false, true, getState(), getSharedLibraryName(), getServiceAssemblyName());
        System.out.println(result);
    }
    
}
