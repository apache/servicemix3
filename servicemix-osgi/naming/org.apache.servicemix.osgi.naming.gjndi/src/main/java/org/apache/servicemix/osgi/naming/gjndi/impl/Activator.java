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
package org.apache.servicemix.osgi.naming.gjndi.impl;

import javax.naming.Context;

import org.apache.servicemix.osgi.rmi.registry.RmiRegistry;
import org.apache.xbean.naming.context.WritableContext;
import org.apache.xbean.naming.global.GlobalContextManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator, ServiceListener {

    private BundleContext context;
    private ServiceReference rmiRef;
    private RmiRegistry rmi;

    /**
     * Implements BundleActivator.start(). P
     * 
     * @param context
     *            the framework context for the bundle.
     */
    public void start(BundleContext context) throws Exception {
        this.context = context;
        rmiRef = context.getServiceReference(RmiRegistry.class.getName());
        context.addServiceListener(this, "(|(objectClass=" + RmiRegistry.class.getName() + "))");
        if (rmiRef != null) {
            startRmiGJndi();
        }
    }

    /**
     * Implements BundleActivator.stop().
     * 
     * @param context
     *            the framework context for the bundle.
     */
    public void stop(BundleContext context) throws Exception {
        stopRmiGJndi();
    }

    /**
     * Implements ServiceListener.serviceChanged().
     * 
     * @param event
     *            the service event.
     */
    public void serviceChanged(ServiceEvent event) {
        ServiceReference servicereference = event.getServiceReference();
        switch (event.getType()) {
        case ServiceEvent.REGISTERED:
            rmiRef = servicereference;
            if (rmiRef != null) {
                try {
                    this.startRmiGJndi();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
        case ServiceEvent.UNREGISTERING:
            try {
                this.stopRmiGJndi();
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        }
    }

    protected void startRmiGJndi() throws Exception {
        rmi = (RmiRegistry) context.getService(rmiRef);
        if (rmi == null) {
            return;
        }
        int port = rmi.getPort();
        GlobalContextManager.setGlobalContext(new WritableContext(""));
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, GlobalContextManager.class.getName());
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.xbean.naming");
        System.setProperty(Context.PROVIDER_URL, "rmi://0.0.0.0:" + port);
    }

    protected void stopRmiGJndi() throws Exception {
        GlobalContextManager.setGlobalContext(null);
        if (rmi != null) {
            context.ungetService(rmiRef);
            rmi = null;
        }
    }

}
