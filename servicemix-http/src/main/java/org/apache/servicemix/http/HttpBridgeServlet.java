/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.http;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpBridgeServlet extends HttpServlet {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = -7995806514300732777L;
    
    private HttpProcessor processor;

    public HttpProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(HttpProcessor processor) {
        this.processor = processor;
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (processor == null) {
            processor = (HttpProcessor) getServletContext().getAttribute("processor");
            if (processor == null) {
                throw new ServletException("No binding property available on the servlet context");
            }
        }
    }

    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            getProcessor().process(request, response);
        } catch (IOException e) {
            throw e;
        } catch (ServletException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException("Failed to process request: " + e, e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            getProcessor().process(request, response);
        } catch (IOException e) {
            throw e;
        } catch (ServletException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException("Failed to process request: " + e, e);
        }
    }

}
