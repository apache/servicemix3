package org.apache.servicemix.osgi.jmx.registry.impl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.servicemix.osgi.jmx.registry.RmiRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private RmiRegistryImpl registry;

    /**
     * Implements BundleActivator.start(). P
     * @param context the framework context for the bundle.
    **/
    public void start(BundleContext context) throws Exception {
        int port = Registry.REGISTRY_PORT;
        String portS = context.getProperty("org.apache.servicemix.osgi.jmx.registry.port");
        if (portS != null) {
            port = Integer.parseInt(portS);
        }
        System.setProperty("java.rmi.server.RMIClassLoaderSpi", RmiClassLoaderSpiImpl.class.getName());
        Registry reg = LocateRegistry.createRegistry(port);
        registry = new RmiRegistryImpl(reg, port);
        context.registerService(new String[] { RmiRegistry.class.getName(), Registry.class.getName() }, 
                                registry, null);
    }

    /**
     * Implements BundleActivator.stop(). 
     * @param context the framework context for the bundle.
    **/
    public void stop(BundleContext context) throws Exception {
        UnicastRemoteObject.unexportObject(registry.getRegistry(), true);
        registry = null;
    }

}
