package org.apache.geronimo.gshell.osgi.commands;

import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.osgi.framework.Bundle;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 3, 2007
 * Time: 1:59:04 PM
 * To change this template use File | Settings | File Templates.
 */
@CommandComponent(id="shutdown", description="Shutdown")
public class Shutdown extends OsgiCommandSupport {

    protected Object doExecute() throws Exception {
        Bundle bundle = getBundleContext().getBundle(0);
        bundle.stop();
        return SUCCESS;
    }

}
