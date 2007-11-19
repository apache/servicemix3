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
package org.apache.servicemix.openejb;

import java.util.Properties;

import javax.transaction.TransactionManager;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.dynamic.PassthroughFactory;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.ri.sp.PseudoSecurityService;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.server.webservices.WsRegistry;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

/**
 * Factory for OpenEJB to intitialize everything.
 *
 */
public class OpenEjbFactory {

    private static Messages messages = new Messages("org.apache.openejb.util.resources");

    private Properties properties;
    private TransactionManager transactionManager;
    private WsRegistry wsRegistry;
    private SecurityService securityService;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setWsRegistry(WsRegistry wsRegistry) {
        this.wsRegistry = wsRegistry;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void init() throws Exception {
        Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, "org.apache.openejb.util.resources");

        if (properties == null) {
            properties = new Properties();
        }

        try {
            SystemInstance.init(properties);
        } catch (Exception e) {
            throw new OpenEJBException(e);
        }
        SystemInstance system = SystemInstance.get();

        ApplicationServer appServer = new ServerFederation();
        system.setComponent(ApplicationServer.class, appServer);

        Assembler assembler = new Assembler();
        SystemInstance.get().setComponent(org.apache.openejb.spi.Assembler.class, assembler);

        ContainerSystem containerSystem = assembler.getContainerSystem();
        if (containerSystem == null) {
            String msg = messages.message("startup.assemblerReturnedNullContainer");
            logger.fatal(msg);
            throw new OpenEJBException(msg);
        }
        system.setComponent(ContainerSystem.class, containerSystem);

        if (securityService == null) {
            securityService = new PseudoSecurityService();
        }
        if (securityService != null) {
            SecurityServiceInfo securityServiceInfo = new SecurityServiceInfo();
            PassthroughFactory.add(securityServiceInfo, securityService);
            securityServiceInfo.id = "Default Security Service";
            securityServiceInfo.service = "SecurityService";
            assembler.createSecurityService(securityServiceInfo);
            system.setComponent(SecurityService.class, securityService);
        }

        if (transactionManager != null) {
            TransactionServiceInfo transactionServiceInfo = new TransactionServiceInfo();
            PassthroughFactory.add(transactionServiceInfo, transactionManager);
            transactionServiceInfo.id = "Default Transaction Manager";
            transactionServiceInfo.service = "TransactionManager";
            assembler.createTransactionManager(transactionServiceInfo);
        }

        if (wsRegistry != null) {
            SystemInstance.get().setComponent(WsRegistry.class, wsRegistry);
        }

        ServiceManager.getManager().init();
        ServiceManager.getManager().start(false);
    }

    public void destroy() throws Exception {
        ServiceManager.getManager().stop();
        OpenEJB.destroy();
    }
}
