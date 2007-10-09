package org.apache.geronimo.gshell.osgi;

import java.util.Collection;

import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.registry.DefaultCommandRegistry;
import org.apache.geronimo.gshell.registry.DuplicateRegistrationException;
import org.apache.geronimo.gshell.registry.NotRegisteredException;
import org.apache.geronimo.gshell.command.Command;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 8, 2007
 * Time: 9:39:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class OsgiCommandRegistry implements CommandRegistry {

    private static CommandRegistry registry = new DefaultCommandRegistry();

    public void register(Command command) throws DuplicateRegistrationException {
        registry.register(command);
    }

    public void unregister(Command command) throws NotRegisteredException {
        registry.unregister(command);
    }

    public Command lookup(String s) throws NotRegisteredException {
        return registry.lookup(s);
    }

    public Collection<Command> commands() {
        return registry.commands();
    }
}
