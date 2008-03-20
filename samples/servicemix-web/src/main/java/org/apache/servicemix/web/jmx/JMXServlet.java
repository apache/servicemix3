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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

/**
 * Creates an XML response for one or more MBeans using an optional node in the
 * JMX tree or query parameters.
 * 
 * @version $Revision: 356269 $
 */
public class JMXServlet extends JMXServletSupport {

    private static final Log log = LogFactory.getLog(JMXServlet.class);
    private static final long serialVersionUID = -5953322364144161756L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            MBeanServer beanServer = getMBeanServer();
            ObjectName name = getObjectName(request);
            QueryExp query = getQueryExp(request);

            JMXWriter writer = new JMXWriter(response.getWriter(), getManagementContext());

            String style = request.getParameter("style");
            String view = request.getParameter("view");
            if (view == null) {
                view = "";
            }

            if (style != null && style.equals("html")) {
                Set names = beanServer.queryNames(name, query);

                if (log.isDebugEnabled()) {
                    log.debug("ObjectName: " + name);
                    log.debug("Query: " + query);
                    log.debug("Matches ObjectNames: " + names);
                }

                if (view.equals("properties")) {
                    writer.outputHtmlProperties(names);
                }
                else if (view.equals("attributes")) {
                    writer.outputHtmlAttributes(names);
                }
                else if (view.equals("flat")) {
                    writer.outputHtmlNames(names);
                }
                else {
                    writer.outputHtmlNamesByDomain(names);
                }
            }
            else {
                writer.outputHeader();
                if (view.equals("bean")) {
                    Set mbeans = beanServer.queryMBeans(name, query);
                    writer.outputMBeans(mbeans);
                }
                else if (view.equals("detail")) {
                    Set names = beanServer.queryNames(name, query);
                    writer.outputDetail(names);
                }
                else {
                    Set names = beanServer.queryNames(name, query);
                    writer.outputNames(names);
                }
                writer.outputFooter();
            }
        }
        catch (JMException e) {
            throw new ServletException(e);
        }
    }
}
