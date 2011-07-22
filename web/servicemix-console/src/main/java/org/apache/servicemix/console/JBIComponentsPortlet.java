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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.servicemix.jbi.management.ManagementContextMBean;

public class JBIComponentsPortlet extends ServiceMixPortlet {

    private static final String MODE_KEY = "mode";
    private static final String LIST_MODE = "list";
    private static final String COMP_MODE = "comp";
    
    protected PortletRequestDispatcher compView;
    
	   public static class ComponentInfo {
	        private String name;
	        private String type;
	        private String state;
	        
	        public String getType() {
	            return type;
	        }
	        public void setType(String type) {
	            this.type = type;
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

        public void init(PortletConfig portletConfig) throws PortletException {
            super.init(portletConfig);
            PortletContext pc = portletConfig.getPortletContext();
            compView = pc.getRequestDispatcher("/WEB-INF/view/" + getPortletName() + "/comp.jsp");
        }
       
        protected void renderView(RenderRequest renderRequest, RenderResponse renderResponse) throws Exception {
            String mode = renderRequest.getParameter(MODE_KEY);
            System.err.println("Mode: " + mode);
            if (COMP_MODE.equals(mode)) {
                renderCompRequest(renderRequest, renderResponse);
            } else {
                // Render list
                renderListRequest(renderRequest, renderResponse);
            }
        }

        protected void renderCompRequest(RenderRequest request, RenderResponse response) throws Exception {
            compView.include(request, response);
        }
        
	    protected void renderListRequest(RenderRequest request, RenderResponse response) throws Exception {
	        ManagementContextMBean management = getManagementContext();
	        SortedMap components = new TreeMap();
	        ObjectName[] bcs = management.getBindingComponents();
	        for (int i = 0; i < bcs.length; i++) {
	            ComponentInfo info = new ComponentInfo();
	            info.name =  getAttribute(bcs[i], "name");
	            info.type =  "Binding Component";
	            info.state =  getAttribute(bcs[i], "currentState");
	            components.put(info.name, info);
	        }
	        ObjectName[] ses = management.getEngineComponents();
	        for (int i = 0; i < ses.length; i++) {
	            ComponentInfo info = new ComponentInfo();
	            info.name =  getAttribute(ses[i], "name");
	            info.type =  "Service Engine";
	            info.state =  getAttribute(ses[i], "currentState");
	            if (components.containsKey(info.name)) {
	            	((ComponentInfo) components.get(info.name)).type = "Unknown";
	            } else {
		            components.put(info.name, info);
	            }
	        }
	        List infos = new ArrayList(components.values());
	        request.setAttribute("components", infos);
            normalView.include(request, response);
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
	        if ("stop".equals(action)) {
	        	management.stopComponent(name);
	        } else if ("start".equals(action)) {
	        	management.startComponent(name);
	        } else if ("shutdown".equals(action)) {
	        	management.shutDownComponent(name);
	        }
	    }
}
