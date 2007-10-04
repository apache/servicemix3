package org.apache.geronimo.gshell.osgi.commands;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 3, 2007
 * Time: 10:47:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class Activator implements BundleActivator {

    public void start(BundleContext bundleContext) throws Exception {
        OsgiCommandSupport.setBundleContext(bundleContext);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        OsgiCommandSupport.setBundleContext(null);
    }


}
