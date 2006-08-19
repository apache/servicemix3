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
package org.apache.servicemix.web.jmx;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * A useful base class for any JMS related servlet; there are various ways to
 * map JMS operations to web requests so we put most of the common behaviour in
 * a reusable base class.
 *
 * @version $Revision: 356269 $
 */
public abstract class JMXServletSupport extends HttpServlet {

    protected static final String MANAGEMENT_CONTEXT_PROPERTY = "org.activemq.jmx.ManagementContext";

    private ManagementContext managementContext;

    public void init() throws ServletException {
        if (managementContext == null) {
            managementContext = (ManagementContext) getServletContext().getAttribute(MANAGEMENT_CONTEXT_PROPERTY);
            if (managementContext == null) {
                managementContext = new ManagementContext();
            }
        }
    }

    public MBeanServer getMBeanServer() {
        return managementContext.getMBeanServer();
    }

    public ManagementContext getManagementContext() {
        return managementContext;
    }

    public void setManagementContext(ManagementContext managementContext) {
        this.managementContext = managementContext;
    }

    protected QueryExp getQueryExp(HttpServletRequest request) throws ServletException {
        QueryExp answer = null;
        String value = request.getParameter("query");
        if (value != null) {
            try {
                answer = new ObjectName(value);
            }
            catch (MalformedObjectNameException e) {
                throw new ServletException(e);
            }
        }
        return answer;
    }

    protected ObjectName getObjectName(HttpServletRequest request) throws ServletException {
        String value = request.getParameter("name");
        ObjectName answer = null;
        if (value != null) {
            try {
                answer = new ObjectName(value);
            }
            catch (MalformedObjectNameException e) {
                throw new ServletException("Failed to parse object name: " + value + ". Reason: " + e, e);
            }
        }
        return answer;
    }

    /**
     * Converts the value of the named parameter into a boolean
     */
    protected boolean asBoolean(HttpServletRequest request, String name) {
        String param = request.getParameter(name);
        return param != null && param.equalsIgnoreCase("true");
    }
}
