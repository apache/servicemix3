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
package org.apache.servicemix.osgi.jmx.rmi.impl;

import java.net.InetAddress;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXConnectorServerMBean;
import javax.management.remote.JMXServiceURL;

import org.apache.servicemix.osgi.jmx.rmi.RmiConnector;
import org.apache.servicemix.osgi.rmi.registry.RmiRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator, ServiceListener {

    private RmiConnectorImpl connector;

    private ServiceReference mbsRef;

    private ServiceReference rmiRef;

    private MBeanServer mbs;

    private RmiRegistry rmi;
    
    private BundleContext context;
    
    private ServiceRegistration registration;

    /**
     * Implements BundleActivator.start(). P
     * 
     * @param context
     *            the framework context for the bundle.
     */
    public void start(BundleContext context) throws Exception {
        this.context = context;
        mbsRef = context.getServiceReference(MBeanServer.class.getName());
        rmiRef = context.getServiceReference(RmiRegistry.class.getName());
        context.addServiceListener(this, "(|(objectClass=" + RmiRegistry.class.getName() + ")" + "(objectClass="
                + MBeanServer.class.getName() + "))");
        if (mbsRef != null && rmiRef != null) {
            startRmiConnector();
        }
    }

    /**
     * Implements BundleActivator.stop().
     * 
     * @param context
     *            the framework context for the bundle.
     */
    public void stop(BundleContext context) throws Exception {
        stopRmiConnector();
    }

    /**
     * Implements ServiceListener.serviceChanged().
     * 
     * @param event
     *            the service event.
     */
    public void serviceChanged(ServiceEvent event) {
        ServiceReference servicereference = event.getServiceReference();
        String[] ast = (String[]) (servicereference.getProperty("objectClass"));
        String as = ast[0];
        switch (event.getType()) {
        case ServiceEvent.REGISTERED:
            if (as.equals(RmiRegistry.class.getName())) {
                rmiRef = servicereference;
            } else if (as.equals(MBeanServer.class.getName())) {
                mbsRef = servicereference;
            }
            if (rmiRef != null && mbsRef != null) {
                try {
                    this.startRmiConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
        case ServiceEvent.UNREGISTERING:
            try {
                this.stopRmiConnector();
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        }
    }

    protected void startRmiConnector() throws Exception {
        mbs = (MBeanServer) context.getService(mbsRef);
        rmi = (RmiRegistry) context.getService(rmiRef);
        if (mbs == null || rmi == null) {
            return;
        }
        int port = rmi.getPort();
        String url = "service:jmx:rmi:///jndi/rmi://"+ InetAddress.getLocalHost().getHostAddress() + ":" + port + "/jmxrmi";
        JMXServiceURL address = new JMXServiceURL(url);
        JMXConnectorServer con = JMXConnectorServerFactory.newJMXConnectorServer(address, null, this.mbs);
        connector = new RmiConnectorImpl(con);
        connector.start();
        registration = context.registerService(
                                new String[] { RmiConnector.class.getName(), JMXConnectorServerMBean.class.getName() }, 
                                connector, null);
    }
    
    protected void stopRmiConnector() throws Exception {
        System.err.println("Stop connector");
        registration.unregister();
        if (connector != null) {
            connector.stop();
            connector = null;
        }
        if (mbs != null) {
            context.ungetService(mbsRef);
            mbs = null;
        }
        if (rmi != null) {
            context.ungetService(rmiRef);
            rmi = null;
        }
    }
}
