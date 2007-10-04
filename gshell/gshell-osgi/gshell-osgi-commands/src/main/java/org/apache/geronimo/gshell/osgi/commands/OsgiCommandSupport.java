package org.apache.geronimo.gshell.osgi.commands;

import org.apache.geronimo.gshell.command.CommandSupport;
import org.osgi.framework.BundleContext;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 3, 2007
 * Time: 9:44:39 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class OsgiCommandSupport extends CommandSupport {

    private static BundleContext bundleContext;

    public static void setBundleContext(BundleContext context) {
        bundleContext = context;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }
}
