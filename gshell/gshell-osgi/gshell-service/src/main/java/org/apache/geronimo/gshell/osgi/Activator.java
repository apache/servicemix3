package org.apache.geronimo.gshell.osgi;

import org.apache.geronimo.gshell.command.IO;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.osgi.framework.*;

import java.util.concurrent.CountDownLatch;

public class Activator implements BundleActivator, Runnable {

    private BundleContext context;
    private GShell shell;
    private Thread thread;
    private ClassLoader classLoader;

    public void start(BundleContext bundleContext) throws Exception {
        context = bundleContext;
        classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(
                            context.getBundle(), Activator.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
        ClassWorld cw = new ClassWorld("gshell", classLoader);
        IO io = new IO(System.in, System.out, System.err);
        shell = new GShell(cw, io);
        thread = new Thread(this);
        thread.start();
        new OsgiCommandDiscoverer(bundleContext, shell.getCommandRegistry());
    }

    public void stop(BundleContext bundleContext) throws Exception {
        thread.interrupt();
        thread.join();
    }

    public void run() {
        try {
            // Wait a bit to make sure everything is initialized
            Thread.sleep(100);
            Thread.currentThread().setContextClassLoader(classLoader);
            shell.run();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}