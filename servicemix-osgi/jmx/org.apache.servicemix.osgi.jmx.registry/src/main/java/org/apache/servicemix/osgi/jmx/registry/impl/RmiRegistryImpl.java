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
package org.apache.servicemix.osgi.jmx.registry.impl;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.apache.servicemix.osgi.jmx.registry.RmiRegistry;

/**
 * 
 * @author gnodet
 * @org.apache.xbean.XBean element="rmiRegistry"
 */
public class RmiRegistryImpl implements RmiRegistry {

    private Registry registry;
    private int port;
    
    public RmiRegistryImpl(Registry registry, int port) {
        this.registry = registry;
        this.port = port;
    }
    
    /**
     * @param name
     * @param obj
     * @throws RemoteException
     * @throws AlreadyBoundException
     * @throws AccessException
     * @see java.rmi.registry.Registry#bind(java.lang.String, java.rmi.Remote)
     */
    public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException, AccessException {
        registry.bind(name, obj);
    }

    /**
     * @return
     * @throws RemoteException
     * @throws AccessException
     * @see java.rmi.registry.Registry#list()
     */
    public String[] list() throws RemoteException, AccessException {
        return registry.list();
    }

    /**
     * @param name
     * @return
     * @throws RemoteException
     * @throws NotBoundException
     * @throws AccessException
     * @see java.rmi.registry.Registry#lookup(java.lang.String)
     */
    public Remote lookup(String name) throws RemoteException, NotBoundException, AccessException {
        return registry.lookup(name);
    }

    /**
     * @param name
     * @param obj
     * @throws RemoteException
     * @throws AccessException
     * @see java.rmi.registry.Registry#rebind(java.lang.String, java.rmi.Remote)
     */
    public void rebind(String name, Remote obj) throws RemoteException, AccessException {
        registry.rebind(name, obj);
    }

    /**
     * @param name
     * @throws RemoteException
     * @throws NotBoundException
     * @throws AccessException
     * @see java.rmi.registry.Registry#unbind(java.lang.String)
     */
    public void unbind(String name) throws RemoteException, NotBoundException, AccessException {
        registry.unbind(name);
    }

    /**
     * @return the registry
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

}
