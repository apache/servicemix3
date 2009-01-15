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
package org.apache.servicemix.web.http;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.jbi.component.Component;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This servlet is meant to be used when with the servicemix-http component.
 * It is based on org.apache.servicemix.http.HttpManagedServlet, but
 * uses introspection to be able to use it with an installed component,
 * rather than an embedded component.
 *  
 * @author gnodet
 */
public class HttpManagedServlet extends HttpServlet {

    public static final String CONTAINER_PROPERTY = "container";
    public static final String CONTAINER_DEFAULT = "jbi";
    
    public static final String COMPONENT_PROPERTY = "component";
    public static final String COMPONENT_DEFAULT = "servicemix-http";
    
    public static final String MAPPING_PROPERTY = "mapping";
    
    private JBIContainer container;
    private Object processor;
    private Method processorMethod;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        // Retrieve spring application context
        ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        
        // Retrieve 
        String containerName = config.getInitParameter(CONTAINER_PROPERTY);
        if (containerName == null) {
            containerName = CONTAINER_DEFAULT;
        }
        container = (JBIContainer) applicationContext.getBean(containerName);
        if (container == null) {
            throw new IllegalStateException("Unable to find jbi container " + containerName);
        }
    }
    
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (processor == null) {
                String componentName = getServletConfig().getInitParameter(COMPONENT_PROPERTY);
                if (componentName == null) {
                    componentName = COMPONENT_DEFAULT;
                }
                ComponentMBeanImpl mbean = container.getComponent(componentName);
                if (mbean == null) {
                    throw new ServletException("Component " + componentName + " not installed");
                }
                Component component = mbean.getComponent();
                Method mth = component.getClass().getMethod("getMainProcessor", (Class[]) null);
                processor = mth.invoke(component, (Object[]) null);
                processorMethod = processor.getClass().getMethod("process", new Class[] { HttpServletRequest.class, HttpServletResponse.class });
            }
            processorMethod.invoke(processor, new Object[] { request, response });
        } catch (ServletException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw new ServletException("Failed to process request: " + e.getTargetException(), e.getTargetException());
        } catch (Exception e) {
            throw new ServletException("Failed to process request: " + e, e);
        }
    }
}
