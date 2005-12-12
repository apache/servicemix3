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
package org.servicemix.components;

import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;

public class ServiceUnit implements LifeCycleMBean {

    private String name;
    private String rootPath;
    private String status = STOPPED;
    
    public void start() throws JBIException {
        this.status = RUNNING;
    }
    
    public void stop() throws JBIException {
        this.status = STOPPED;
    }
    
    public void shutDown() throws JBIException {
        this.status = SHUTDOWN;
    }
    
    public String getCurrentState() {
        return status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
    
}
