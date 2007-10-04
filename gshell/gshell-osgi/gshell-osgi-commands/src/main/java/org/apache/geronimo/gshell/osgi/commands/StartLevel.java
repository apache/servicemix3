package org.apache.geronimo.gshell.osgi.commands;

import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.clp.Argument;
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
@CommandComponent(id="startlevel", description="Get or set the start level")
public class StartLevel extends BundleCommand {

    @Argument
    Integer level;

    protected void doExecute(Bundle bundle) throws Exception {
        // Get package admin service.
        ServiceReference ref = getBundleContext().getServiceReference(org.osgi.service.startlevel.StartLevel.class.getName());
        if (ref == null) {
            io.out.println("StartLevel service is unavailable.");
            return;
        }
        try {
            org.osgi.service.startlevel.StartLevel sl = (org.osgi.service.startlevel.StartLevel) getBundleContext().getService(ref);
            if (sl == null) {
                io.out.println("StartLevel service is unavailable.");
                return;
            }

            if (level == null) {
                io.out.println("Level " + sl.getStartLevel());
            }
            else {
                sl.setStartLevel(level);
            }
        }
        finally {
            getBundleContext().ungetService(ref);
        }
    }

}