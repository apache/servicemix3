package org.eclipse.jst.server.servicemix;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;

import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.ServerType;

public class JMXPingThread {

	// delay before pinging starts
	private static final int PING_DELAY = 2000;

	// delay between pings
	private static final int PING_INTERVAL = 250;

	private ServiceMixServerBehaviour serviceMixServer;

	private int maxPings;

	private IServer server;

	private boolean stop;

	public JMXPingThread(IServer server, ServiceMixServerBehaviour genericServer) {
		this.server = server;
		this.maxPings = guessMaxPings(genericServer);
		this.serviceMixServer = genericServer;
		Thread t = new Thread() {
			public void run() {
				ping();
			}
		};
		t.setDaemon(true);
		t.start();
	}

	private int guessMaxPings(ServiceMixServerBehaviour server) {
		int maxpings = 60;
		int startTimeout = ((ServerType) server.getServer().getServerType())
				.getStartTimeout();
		if (startTimeout > 0)
			maxpings = startTimeout / PING_INTERVAL;
		return maxpings;
	}

	/**
	 * Ping the server until it is started. Then set the server state to
	 * STATE_STARTED.
	 */
	protected void ping() {
		int count = 0;
		try {
			Thread.sleep(PING_DELAY);
		} catch (Exception e) {
			// ignore
		}
		while (!stop) {
			try {
				if (count == maxPings) {
					try {
						server.stop(false);
					} catch (Exception e) {

					}
					stop = true;
					break;
				}
				count++;

				ObjectName containerObjectName = new ObjectName(
						"org.apache.servicemix:type=Container,name=defaultJBI");
				Object value = serviceMixServer.getJMXConnector()
						.getMBeanServerConnection().getAttribute(
								containerObjectName, "currentState");
				System.out.println("Got status "+value);
				if ("Running".equals(value.toString()) && !stop) {
					Thread.sleep(200);
					serviceMixServer.setStarted();
					stop = true;
				}
			} catch (InstanceNotFoundException e) {
				if (!stop) {
					try {
						Thread.sleep(PING_INTERVAL);
					} catch (InterruptedException e2) {
						// ignore
					}
				}				
			} catch (IOException e) {
				if (!stop) {
					try {
						Thread.sleep(PING_INTERVAL);
					} catch (InterruptedException e2) {
						// ignore
					}
				}
			} catch (Exception e) {				
				if (!stop) {
					try {
						Thread.sleep(PING_INTERVAL);
					} catch (InterruptedException e2) {
						// ignore
					}
				}
			}
		}
	}

	/**
	 * Tell the pinging to stop.
	 */
	public void stop() {
		stop = true;
	}

}
