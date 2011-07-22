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
package org.apache.servicemix.console;

import org.apache.servicemix.jbi.management.ManagementContextMBean;

import javax.jbi.management.LifeCycleMBean;
import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;

import java.util.ArrayList;
import java.util.List;


public class JBIContainerPortlet extends ServiceMixPortlet {

    public static class ServiceInfo {
        private String name;
        private String description;
        private String state;
        
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getState() {
            return state;
        }
        public void setState(String state) {
            this.state = state;
        }
    }
    
    protected void fillViewRequest(RenderRequest request) throws Exception {
        LifeCycleMBean container = getJBIContainer();
        ManagementContextMBean management = getManagementContext();
        request.setAttribute("state", container.getCurrentState());
        request.setAttribute("info", management.getSystemInfo());
        ObjectName[] services = management.getSystemServices();
        List infos = new ArrayList();
        for (int i = 0; i < services.length; i++) {
            ServiceInfo info = new ServiceInfo();
            info.name =  getAttribute(services[i], "name");
            info.description =  getAttribute(services[i], "description");
            info.state =  getAttribute(services[i], "currentState");
            infos.add(info);
        }
        request.setAttribute("services", infos);
    }
    
    protected String getAttribute(ObjectName name, String attribute) {
        try {
            return (String) getServerConnection().getAttribute(name, attribute);
        } catch (Exception e) {
            LOGGER.error("Could not retrieve attribute '" + attribute + "' for mbean '" + name + "'");
            return null;
        }
    }

    protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
        String action = actionRequest.getParameter("action");
        String name   = actionRequest.getParameter("name");
        System.err.println("doProcessAction: " + action + " for " + name);
        ManagementContextMBean management = getManagementContext();
        ObjectName service = management.getSystemService(getContainerName() + "." + name);
        getServerConnection().invoke(service, action, new Object[0], new String[0]);
    }
}