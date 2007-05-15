/*
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
package org.apache.servicemix.osgi.rmi.registry.impl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.servicemix.osgi.rmi.registry.RmiRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private RmiRegistryImpl registry;

    /**
     * Implements BundleActivator.start(). P
     * @param context the framework context for the bundle.
    **/
    public void start(BundleContext context) throws Exception {
        int port = Registry.REGISTRY_PORT;
        String portS = context.getProperty("org.apache.servicemix.osgi.rmi.registry.port");
        if (portS != null) {
            port = Integer.parseInt(portS);
        }
        //System.setProperty("java.rmi.server.RMIClassLoaderSpi", RmiClassLoaderSpiImpl.class.getName());
        Registry reg = LocateRegistry.createRegistry(port);
        registry = new RmiRegistryImpl(reg, port);
        context.registerService(new String[] { RmiRegistry.class.getName(), Registry.class.getName() }, 
                                registry, null);
    }

    /**
     * Implements BundleActivator.stop(). 
     * @param context the framework context for the bundle.
    **/
    public void stop(BundleContext context) throws Exception {
        UnicastRemoteObject.unexportObject(registry.getRegistry(), true);
        registry = null;
    }

}
