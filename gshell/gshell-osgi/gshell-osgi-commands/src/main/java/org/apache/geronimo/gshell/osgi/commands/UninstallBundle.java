package org.apache.geronimo.gshell.osgi.commands;

import org.osgi.framework.Bundle;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 3, 2007
 * Time: 12:37:30 PM
 * To change this template use File | Settings | File Templates.
 */
@CommandComponent(id="uninstall", description="Uninstall bundle")
public class UninstallBundle extends BundleCommand {

    protected void doExecute(Bundle bundle) throws Exception {
        bundle.uninstall();
    }

}
