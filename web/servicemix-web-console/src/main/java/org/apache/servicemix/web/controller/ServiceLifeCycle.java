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
package org.apache.servicemix.web.controller;

import javax.jbi.management.LifeCycleMBean;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class ServiceLifeCycle implements Controller {

    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String SHUTDOWN = "shutdown";
    
    private LifeCycleMBean serviceMBean;
    private String name;
    private String view;
    private String action;
    
    public ServiceLifeCycle(LifeCycleMBean serviceMBean, String action) {
        if (serviceMBean == null) {
            throw new IllegalArgumentException("serviceMBean is null");
        }
        if (action == null) {
            throw new IllegalArgumentException("action is null");
        } else if (!START.equals(action) && 
                   !STOP.equals(action) && 
                   !SHUTDOWN.equals(action)) {
            throw new IllegalArgumentException("action must be start, stop, shutdown");
        }
        
        this.serviceMBean = serviceMBean;
        this.action = action;
    }
    
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (START.equals(action)) {
            serviceMBean.start();
        } else if (STOP.equals(action)) {
            serviceMBean.stop();
        } else if (SHUTDOWN.equals(action)) {
            serviceMBean.shutDown();
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
