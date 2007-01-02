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
package org.eclipse.jst.server.servicemix;

import java.io.IOException;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jst.server.generic.core.internal.GenericServerBehaviour;
import org.eclipse.wst.server.core.IServer;

/**
 * Eclipse JST Server Behaviour for Apache ServiceMix
 * 
 * Basically just extends the Generic to allow for the pinging of the server
 * during start-up to use the JMX access to determine whether the server has
 * started
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceMixServerBehaviour extends GenericServerBehaviour {
	
	private static final String ATTR_STOP = "stop-server";

	private JMXPingThread jmxPingThread;

	public void stop(boolean force) {
		if (jmxPingThread != null)
			jmxPingThread.stop();
		super.stop(force);
	}

	protected void setupLaunch(ILaunch launch, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		if ("true".equals(launch.getLaunchConfiguration().getAttribute(
				ATTR_STOP, "false")))
			return;

		setServerState(IServer.STATE_STARTING);
		setMode(launchMode);
		jmxPingThread = new JMXPingThread(getServer(), this);
	}

	protected JMXConnector getJMXConnector() throws CoreException, IOException {
		String jndiPath = "jmxrmi";
		JMXServiceURL url;
		url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/"
				+ jndiPath);
		JMXConnector connector = JMXConnectorFactory.connect(url);
		return connector;
	}

	public void setStarted() {
		setServerState(IServer.STATE_STARTED);
	}
}
