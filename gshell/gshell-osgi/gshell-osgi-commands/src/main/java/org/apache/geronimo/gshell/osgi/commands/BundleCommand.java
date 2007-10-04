package org.apache.geronimo.gshell.osgi.commands;

import org.apache.geronimo.gshell.clp.Argument;
import org.osgi.framework.Bundle;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 3, 2007
 * Time: 12:10:32 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BundleCommand extends OsgiCommandSupport {

    @Argument(required = true)
    long id;

    protected Object doExecute() throws Exception {
        Bundle bundle = getBundleContext().getBundle(id);
        if (bundle == null) {
            io.out.println("Bundle " + id + " not found");
            return FAILURE;
        }
        doExecute(bundle);
        return SUCCESS;
    }

    protected abstract void doExecute(Bundle bundle) throws Exception;
}
