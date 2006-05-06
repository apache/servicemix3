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

import org.apache.servicemix.components.util.MarshalerSupport;
import org.apache.servicemix.jbi.jaxp.StringSource;

import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * A class which marshalls a HTTP request to a NMS message
 * 
 * @version $Revision$
 */
public class HttpMarshaler extends MarshalerSupport {

    public static final String AUTH_TYPE = "AUTH_TYPE";
    public static final String CONTENT_LENGTH = "CONTENT_LENGTH";
    public static final String CONTENT_TYPE = "CONTENT_TYPE";
    public static final String DOCUMENT_ROOT = "DOCUMENT_ROOT";
    public static final String PATH_INFO = "PATH_INFO";
    public static final String PATH_TRANSLATED = "PATH_TRANSLATED";
    public static final String QUERY_STRING = "QUERY_STRING";
    public static final String REMOTE_ADDRESS = "REMOTE_ADDR";
    public static final String REMOTE_HOST = "REMOTE_HOST";
    public static final String REMOTE_USER = "REMOTE_USER";
    public static final String REQUEST_METHOD = "REQUEST_METHOD";
    public static final String REQUEST_URI = "REQUEST_URI";
    public static final String SCRIPT_NAME = "SCRIPT_NAME";
    public static final String SERVER_NAME = "SERVER_NAME";
    public static final String SERVER_PORT = "SERVER_PORT";
    public static final String SERVER_PROTOCOL = "SERVER_PROTOCOL";
    
    protected static final Source EMPTY_CONTENT = new StringSource("<payload/>");

    private String contentType = "text/xml";

    public void toNMS(MessageExchange exchange, NormalizedMessage inMessage, HttpServletRequest request) throws IOException, MessagingException {
        addNmsProperties(exchange, request);
        String method = request.getMethod();
        if (method != null && method.equalsIgnoreCase("POST")) {
            inMessage.setContent(new StreamSource(request.getInputStream()));
        }
        else {
            Enumeration enumeration = request.getParameterNames();
            while (enumeration.hasMoreElements()) {
                String name = (String) enumeration.nextElement();
                String value = request.getParameter(name);
                inMessage.setProperty(name, value);
            }
            inMessage.setContent(EMPTY_CONTENT);
        }
    }

    public void toResponse(InOut exchange, NormalizedMessage message, HttpServletResponse response) throws IOException, TransformerException {
        if (message != null) {
            addHttpHeaders(response, message);
        }

        response.setContentType(contentType);
        getTransformer().toResult(message.getContent(), new StreamResult(response.getOutputStream()));
    }

    // Properties
    // -------------------------------------------------------------------------
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected void addNmsProperties(MessageExchange exchange, HttpServletRequest request) {
        Enumeration enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            String value = request.getHeader(name);
            exchange.setProperty(name, value);
        }
        exchange.setProperty(AUTH_TYPE, request.getAuthType());
        exchange.setProperty(CONTENT_LENGTH, String.valueOf(request.getContentLength()));
        exchange.setProperty(CONTENT_TYPE, request.getContentType());
        exchange.setProperty(DOCUMENT_ROOT, request.getRealPath("/"));
        exchange.setProperty(PATH_INFO, request.getPathInfo());
        exchange.setProperty(PATH_TRANSLATED, request.getPathTranslated());
        exchange.setProperty(QUERY_STRING, request.getQueryString());
        exchange.setProperty(REMOTE_ADDRESS, request.getRemoteAddr());
        exchange.setProperty(REMOTE_HOST, request.getRemoteHost());
        exchange.setProperty(REMOTE_USER, request.getRemoteUser());
        exchange.setProperty(REQUEST_METHOD, request.getMethod());
        exchange.setProperty(REQUEST_URI, request.getRequestURL());
        exchange.setProperty(SCRIPT_NAME, request.getServletPath());
        exchange.setProperty(SERVER_NAME, request.getServerName());
        exchange.setProperty(SERVER_PORT, String.valueOf(request.getServerPort()));
        exchange.setProperty(SERVER_PROTOCOL, request.getProtocol());
    }
    
    protected void addHttpHeaders(HttpServletResponse response, NormalizedMessage normalizedMessage) {
        for (Iterator iter = normalizedMessage.getPropertyNames().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            Object value = normalizedMessage.getProperty(name);
            if (shouldIncludeHeader(normalizedMessage, name, value)) {
                response.setHeader(name, value.toString());
            }
        }
    }

    /**
     * Decides whether or not the given header should be included in the JMS
     * message. By default this includes all suitable typed values
     */
    protected boolean shouldIncludeHeader(NormalizedMessage normalizedMessage, String name, Object value) {
        return value instanceof String && 
                !"Content-Length".equalsIgnoreCase(name) &&
                !"Content-Type".equalsIgnoreCase(name);
    }

}
