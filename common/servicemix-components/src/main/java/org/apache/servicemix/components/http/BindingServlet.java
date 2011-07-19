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
package org.apache.servicemix.components.http;

import javax.jbi.JBIException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * A Servlet which dispatches requests into the JBI container and returns the result.
 *
 * @version $Revision$
 */
public class BindingServlet extends HttpServlet {

    private HttpBinding binding;

    public HttpBinding getBinding() {
        return binding;
    }

    public void setBinding(HttpBinding binding) {
        this.binding = binding;
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (binding == null) {
            binding = (HttpBinding) getServletContext().getAttribute("binding");
            if (binding == null) {
                binding = createHttpBinding(config);
            }
                if (binding == null) {
                throw new ServletException("No binding property available on the servlet context");
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            getBinding().process(request, response);
        }
        catch (JBIException e) {
            throw new ServletException("Failed to process JBI request: " + e, e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            getBinding().process(request, response);
        }
        catch (JBIException e) {
            throw new ServletException("Failed to process JBI request: " + e, e);
        }
    }

    protected HttpBinding createHttpBinding(ServletConfig config) throws ServletException {
        // lets default to in/out
        return new HttpInOutBinding();
    }

}
