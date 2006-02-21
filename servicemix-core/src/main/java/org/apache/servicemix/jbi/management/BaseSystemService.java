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

import org.apache.servicemix.jbi.container.JBIContainer;

public abstract class BaseSystemService extends BaseLifeCycle {

    protected JBIContainer container;
    
    /**
     * Get the name of the item
     * @return the name
     */
    public String getName() {
        String name = getClass().getName();
        int index = name.lastIndexOf(".");
        if (index >= 0 && (index+1) < name.length()) {
            name = name.substring(index+1);
        }
        return name;
    }

    /**
     * Get the type of the item
     * @return the type
     */
   public String getType() {
        return "SystemService";
   }
   
   public void init(JBIContainer container) throws JBIException {
       this.container = container;
       container.getManagementContext().registerSystemService(this, getServiceMBean());

   }
   
   public void shutDown() throws JBIException {
       stop();
       super.shutDown();
       if (container != null && container.getManagementContext() != null) {
           container.getManagementContext().unregisterMBean(this);
       }
   }
   
   protected abstract Class getServiceMBean();
   
}
