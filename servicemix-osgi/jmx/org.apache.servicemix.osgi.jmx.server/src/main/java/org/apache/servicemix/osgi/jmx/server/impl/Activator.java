package org.apache.servicemix.osgi.jmx.server.impl;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import org.apache.servicemix.osgi.jmx.server.JmxServer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private JmxServerImpl server;

    /**
     * Implements BundleActivator.start(). P
     * @param context the framework context for the bundle.
    **/
    public void start(BundleContext context) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        server = new JmxServerImpl(mbs);
        context.registerService(new String[] { JmxServer.class.getName(), 
                                               MBeanServer.class.getName() }, 
                                server, null);
    }

    /**
     * Implements BundleActivator.stop(). 
     * @param context the framework context for the bundle.
    **/
    public void stop(BundleContext context) throws Exception {
        server = null;
    }

}
