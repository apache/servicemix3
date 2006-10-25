package org.apache.servicemix.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class ComponentLifeCycle implements Controller {

    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String SHUTDOWN = "shutdown";
    public static final String UNINSTALL = "uninstall";
    
    private AdminCommandsServiceMBean adminCommandsService;
    private String name;
    private String view;
    private String action;
    
    public ComponentLifeCycle(AdminCommandsServiceMBean adminCommandsService, String action) {
        if (adminCommandsService == null) {
            throw new IllegalArgumentException("adminCommandsServiceMBean is null");
        }
        if (action == null) {
            throw new IllegalArgumentException("action is null");
        } else if (!START.equals(action) && 
                   !STOP.equals(action) && 
                   !SHUTDOWN.equals(action) &&
                   !UNINSTALL.equals(action)) {
            throw new IllegalArgumentException("action must be start, stop or shutdown");
        }
        
        this.adminCommandsService = adminCommandsService;
        this.action = action;
    }
    
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("start".equals(action)) {
            adminCommandsService.startComponent(name);
        } else if ("stop".equals(action)) {
            adminCommandsService.stopComponent(name);
        } else if ("shutdown".equals(action)) {
            adminCommandsService.shutdownComponent(name);
        } else if ("uninstall".equals(action)) {
            adminCommandsService.uninstallComponent(name);
        }
        return new ModelAndView(getView());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

}
