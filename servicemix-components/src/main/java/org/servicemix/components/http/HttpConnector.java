/** 
 * 
 * Copyright 2005 Protique Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/

package org.servicemix.components.http;

import org.mortbay.http.HttpContext;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import java.net.UnknownHostException;

/**
 * An embedded Servlet engine to implement a HTTP connector
 *
 * @version $Revision$
 */
public class HttpConnector extends HttpInOutBinding {
    private SocketListener listener = new SocketListener();
    private Server server;
    private String host;
    private int port;

    /**
     * Constructor
     *
     * @param host
     * @param port
     */
    public HttpConnector(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public HttpConnector() {
    }

    /**
     * Constructor
     *
     * @param listener
     */
    public HttpConnector(SocketListener listener) {
        this.listener = listener;
    }

    /**
     * Called when the Component is initialized
     *
     * @param cc
     * @throws JBIException
     */
    public void init(ComponentContext cc) throws JBIException {
        super.init(cc);
        //should set all ports etc here - from the naming context I guess ?
        if (listener == null) {
            listener = new SocketListener();
        }
        try {
            listener.setHost(host);
        }
        catch (UnknownHostException e) {
            throw new JBIException("init failed", e);
        }
        listener.setPort(port);
        server = new Server();
    }

    /**
     * start the Component
     *
     * @throws JBIException
     */
    public void start() throws JBIException {
        server.addListener(listener);
        HttpContext context = server.addContext("/");
        ServletHandler handler = new ServletHandler();
        handler.addServlet("jbiServlet", "/*", BindingServlet.class.getName());
        context.addHandler(handler);
        try {
            context.setAttribute("binding", this);
            server.start();
        }
        catch (Exception e) {
            throw new JBIException("Start failed: " + e, e);
        }
    }

    /**
     * stop
     */
    public void stop() throws JBIException {
        try {
            server.stop();
        }
        catch (InterruptedException e) {
            throw new JBIException("Stop failed: " + e, e);
        }
    }

    /**
     * shutdown
     */
    public void shutDown() throws JBIException {
        server = null;
    }


    // Properties
    //-------------------------------------------------------------------------
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
