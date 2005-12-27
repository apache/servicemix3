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
package org.apache.servicemix.jbi.management;

import javax.jbi.JBIException;
import javax.jbi.management.AdminServiceMBean;

/**
 * ManagementContext interface
 * 
 * @version $Revision$
 */
public interface ManagementContextMBean extends AdminServiceMBean {
    /**
     * Start a Component
     * 
     * @param componentName
     * @return the status
     * @throws JBIException
     */
    public String startComponent(String componentName) throws JBIException;

    /**
     * Stop a Component
     * 
     * @param componentName
     * @return the status
     * @throws JBIException
     */
    public String stopComponent(String componentName) throws JBIException;

    /**
     * Shutdown a Component
     * 
     * @param componentName
     * @return the status
     * @throws JBIException
     */
    public String shutDownComponent(String componentName) throws JBIException;
}