package org.apache.geronimo.gshell.osgi.commands;

import org.osgi.framework.Bundle;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 3, 2007
 * Time: 12:10:15 PM
 * To change this template use File | Settings | File Templates.
 */
@CommandComponent(id="start", description="Start bundle")
public class StartBundle extends BundleCommand {

    protected void doExecute(Bundle bundle) throws Exception {
        bundle.start();
    }

}
