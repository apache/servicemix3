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

import org.apache.servicemix.components.util.ComponentSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.jbi.JBIException;
import javax.jbi.messaging.NormalizedMessage;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @version $Revision$
 */
public abstract class HttpBindingSupport extends ComponentSupport implements HttpBinding {
    protected static final int BAD_REQUEST_STATUS_CODE = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    private HttpMarshaler marshaler;

    public HttpMarshaler getMarshaler() {
        if (marshaler == null) {
            marshaler = createMarshaler();
        }
        return marshaler;
    }

    public void setMarshaler(HttpMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public abstract void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
            JBIException;

    /**
     * A factory method used to create the default {@link HttpMarshaler} to be used to turn the request into a
     * {@link NormalizedMessage} and to turn a normalized message into a response.
     *
     * @return the newly created marshaler
     */
    protected HttpMarshaler createMarshaler() {
      return new HttpMarshaler();
    }

    protected void outputException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(BAD_REQUEST_STATUS_CODE);
        PrintWriter writer = response.getWriter();
        writer.println("Request failed with error: " + e);
        e.printStackTrace(writer);
    }
}
