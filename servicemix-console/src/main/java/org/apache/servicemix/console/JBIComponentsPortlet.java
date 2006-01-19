package org.apache.servicemix.console;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jbi.management.LifeCycleMBean;
import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;

import org.apache.servicemix.jbi.management.ManagementContextMBean;

public class JBIComponentsPortlet extends ServiceMixPortlet {

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
	    
	    protected void fillViewRequest(RenderRequest request) throws Exception {
	        LifeCycleMBean container = getJBIContainer();
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
	    }
	    
	    protected String getAttribute(ObjectName name, String attribute) {
	        try {
	            return (String) getServerConnection().getAttribute(name, attribute);
	        } catch (Exception e) {
	            log.error("Could not retrieve attribute '" + attribute + "' for mbean '" + name + "'");
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
