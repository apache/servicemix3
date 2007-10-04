package org.apache.geronimo.gshell.osgi.commands;

import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 3, 2007
 * Time: 12:37:30 PM
 * To change this template use File | Settings | File Templates.
 */
@CommandComponent(id="resolve", description="Resolve bundle")
public class ResolveBundle extends BundleCommand {

    protected void doExecute(Bundle bundle) throws Exception {
        // Get package admin service.
        ServiceReference ref = getBundleContext().getServiceReference(PackageAdmin.class.getName());
        if (ref == null) {
            io.out.println("PackageAdmin service is unavailable.");
            return;
        }
        try {
            PackageAdmin pa = (PackageAdmin) getBundleContext().getService(ref);
            if (pa == null) {
                io.out.println("PackageAdmin service is unavailable.");
                return;
            }
            pa.resolveBundles(new Bundle[] { bundle });
        }
        finally {
            getBundleContext().ungetService(ref);
        }
    }

}