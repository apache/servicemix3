package org.apache.servicemix.osgi.naming.gjndi.impl;

import org.apache.servicemix.osgi.jmx.registry.RmiRegistry;
import org.apache.xbean.naming.global.GlobalContextManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator, ServiceListener {

    static final String JAVA_NAMING_FACTORY_INITIAL = "java.naming.factory.initial";
    static final String JAVA_NAMING_FACTORY_URL_PKGS = "java.naming.factory.url.pkgs";
    static final String JAVA_NAMING_PROVIDER_URL = "java.naming.provider.url";


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
        String[] ast = (String[]) (servicereference.getProperty("objectClass"));
        String as = ast[0];
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
        System.setProperty(JAVA_NAMING_FACTORY_INITIAL, GlobalContextManager.class.getName());
        System.setProperty(JAVA_NAMING_FACTORY_URL_PKGS, "org.apache.xbean.naming");
        System.setProperty(JAVA_NAMING_PROVIDER_URL, "rmi://0.0.0.0:" + rmi.getPort());
    }

    protected void stopRmiGJndi() throws Exception {
        if (rmi != null) {
            context.ungetService(rmiRef);
            rmi = null;
        }
    }

}
