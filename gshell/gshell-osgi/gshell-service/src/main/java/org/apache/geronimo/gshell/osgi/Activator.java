package org.apache.geronimo.gshell.osgi;

import java.net.URI;
import java.util.HashMap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.remote.server.auth.BogusLoginModule;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator, Runnable {

    private BundleContext context;
    private GShell shell;
    private Thread thread;
    private ClassLoader classLoader;
    private OsgiCommandListener listener;
    private OsgiCommandDiscoverer discoverer;

    public void start(BundleContext bundleContext) throws Exception {

        Configuration.setConfiguration(new Configuration() {
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return new AppConfigurationEntry[] {
                    new AppConfigurationEntry(BogusLoginModule.class.getName(),
                                              AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT,
                                              new HashMap())        
                };
            }
            public void refresh() {
            }
        });

        context = bundleContext;
        classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(
                            context.getBundle(), Activator.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
        ClassWorld cw = new ClassWorld("gshell", classLoader);
        IO io = new IO(System.in, System.out, System.err);
        shell = new GShell(cw, io);
        thread = new Thread(this);
        thread.start();
        listener = new OsgiCommandListener(bundleContext, shell.getCommandRegistry());
        listener.open();;
        discoverer = new OsgiCommandDiscoverer(bundleContext);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        listener.close();;
        thread.interrupt();
        thread.join();
    }

    public void run() {
        try {
            // Wait a bit to make sure everything is initialized
            Thread.sleep(500);
            Thread.currentThread().setContextClassLoader(classLoader);
            shell.getRshServer().bind(new URI("tcp://0.0.0.0:8000"));
            shell.run();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}