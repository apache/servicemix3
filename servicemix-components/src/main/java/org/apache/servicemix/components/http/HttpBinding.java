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
package org.apache.servicemix.components.http;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.jbi.JBIException;
import java.io.IOException;

/**
 * @version $Revision$
 */
public interface HttpBinding {

    /**
     * Invokes the HTTP request from a servlet
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     * @throws JBIException
     */
    void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
            JBIException;
}
