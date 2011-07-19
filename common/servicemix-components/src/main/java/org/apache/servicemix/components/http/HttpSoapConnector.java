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
import javax.jbi.component.ComponentContext;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.thread.BoundedThreadPool;

public class HttpSoapConnector extends HttpSoapInOutBinding {

    private Connector listener = new SocketConnector();
	
	/**
	 * The maximum number of threads for the Jetty SocketListener. It's set 
	 * to 256 by default to match the default value in Jetty. 
	 */
	private int maxThreads = 256;

	private Server server;

	private String host;

	private int port;

    /**
     * Constructor
     *
     * @param host
     * @param port
     */
    public HttpSoapConnector(String host, int port, boolean defaultInOut) {
        this.host = host;
        this.port = port;
        this.defaultInOut = defaultInOut;
    }

	public HttpSoapConnector() {
	}

	/**
	 * Constructor
	 *
	 * @param listener
	 */
	public HttpSoapConnector(Connector listener) {
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
			listener = new SocketConnector();
		}
		listener.setHost(host);
		listener.setPort(port);
		server = new Server();
        BoundedThreadPool btp = new BoundedThreadPool();
        btp.setMaxThreads(getMaxThreads());
        server.setThreadPool(btp);
	}

	/**
	 * start the Component
	 *
	 * @throws JBIException
	 */
	public void start() throws JBIException {
        server.setConnectors(new Connector[] { listener });
        ContextHandler context = new ContextHandler();
        context.setContextPath("/");
        ServletHolder holder = new ServletHolder();
        holder.setName("jbiServlet");
        holder.setClassName(BindingServlet.class.getName());
        ServletHandler handler = new ServletHandler();
        handler.setServlets(new ServletHolder[] { holder });
        ServletMapping mapping = new ServletMapping();
        mapping.setServletName("jbiServlet");
        mapping.setPathSpec("/*");
        handler.setServletMappings(new ServletMapping[] { mapping });
        context.setHandler(handler);
        server.setHandler(context);
        context.setAttribute("binding", this);
        try {
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
            if (server != null) {
                server.stop();
            }
        } catch (Exception e) {
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

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

}
