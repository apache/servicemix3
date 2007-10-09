/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.gshell.osgi;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.descriptor.CommandDescriptor;
import org.apache.geronimo.gshell.descriptor.CommandSetDescriptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Sep 20, 2007
 * Time: 10:37:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class OsgiCommandDiscoverer {

    private static final transient Logger LOG = LoggerFactory.getLogger(OsgiCommandDiscoverer.class);

    private final BundleContext bundleContext;
    private final Map<String, OsgiCommand> commands;

    private class BundleListener implements SynchronousBundleListener {
        public void bundleChanged(BundleEvent event) {
            try {
                Bundle bundle = event.getBundle();
                if (bundle == bundleContext.getBundle()) {
                    return;
                }
                if (event.getType() == BundleEvent.STARTED) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Bundle resolved: " + bundle.getSymbolicName());
                    }
                    mayBeAddCommandsFor(bundle);
                } else if (event.getType() == BundleEvent.STOPPED) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Bundle unresolved: " + bundle.getSymbolicName());
                    }
                    mayBeRemoveCommandsFor(bundle);
                }
            } catch (Throwable e) {
                LOG.error("Exception handing bundle changed event", e);
            }
        }
    }

    private class OsgiCommand implements Command {
        Bundle bundle;
        String id;
        String implementation;
        String description;
        Class  type;
        ServiceRegistration reg;

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public Object execute(CommandContext commandContext, Object... objects) throws Exception {
            init();
            Command cmd = (Command) type.newInstance();
            return cmd.execute(commandContext, objects);
        }

        protected void init() throws Exception {
            if (type == null) {
                ClassLoader loader = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle);
                type = loader.loadClass(implementation);
            }
        }
    }

    public OsgiCommandDiscoverer(BundleContext context) {
        LOG.debug("Initializing OsgiCommandDiscoverer");
        this.commands = new ConcurrentHashMap<String, OsgiCommand>();
        this.bundleContext = context;
        bundleContext.addBundleListener(new BundleListener());
        Bundle[] previousBundles = bundleContext.getBundles();
        for (int i = 0; i < previousBundles.length; i++) {
            int state = previousBundles[i].getState();
            if (state == Bundle.ACTIVE) {
                mayBeAddCommandsFor(previousBundles[i]);
            }
        }
    }

    protected synchronized void mayBeAddCommandsFor(Bundle bundle) {
        URL url = bundle.getEntry("/META-INF/gshell/commands.xml");
        if (url != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found entry: " + url + " in bundle " + bundle.getSymbolicName());
            }
            try {
                CommandSetDescriptor set = CommandSetDescriptor.fromXML(new InputStreamReader(url.openStream()));
                for (CommandDescriptor desc : set.getCommands()) {
                    OsgiCommand cmd = new OsgiCommand();
                    cmd.bundle = bundle;
                    cmd.id = desc.getId();
                    cmd.implementation = desc.getImplementation();
                    cmd.description = desc.getDescription();
                    commands.put(cmd.id, cmd);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Adding command: " + cmd.id + " (" + cmd.implementation + ")");
                    }
                    cmd.reg = bundleContext.registerService(Command.class.getName(), cmd, new Properties());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected synchronized void mayBeRemoveCommandsFor(Bundle bundle) {
        for (OsgiCommand entry : commands.values()) {
            if (entry.bundle == bundle) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Removing entry: " + entry.id + " in bundle " + bundle.getSymbolicName());
                }
                OsgiCommand cmd = commands.remove(entry.id);
                cmd.reg.unregister();
            }
        }
    }

}
