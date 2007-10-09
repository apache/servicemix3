package org.apache.geronimo.gshell.osgi;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.registry.NotRegisteredException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 8, 2007
 * Time: 8:57:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class OsgiCommandListener extends ServiceTracker {

    private static final transient Logger LOG = LoggerFactory.getLogger(OsgiCommandListener.class);

    private final CommandRegistry registry;

    public OsgiCommandListener(BundleContext context, CommandRegistry registry) {
        super(context, Command.class.getName(), null);
        LOG.debug("Initializing OsgiCommandListener");
        this.registry = registry;
    }

    public Object addingService(ServiceReference serviceReference) {
        Command cmd = (Command) super.addingService(serviceReference);
        try {
            registry.register(cmd);
        } catch (Exception e) {
            LOG.debug("Error registering command", e);
        }
        return cmd;
    }

    public void modifiedService(ServiceReference serviceReference, Object o) {
    }

    public void removedService(ServiceReference serviceReference, Object o) {
        Command cmd = (Command) o;
        try {
            registry.unregister(cmd);
        } catch (Exception e) {
            LOG.debug("Error unregistering command", e);
        }
    }

}
