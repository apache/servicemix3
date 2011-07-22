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
package org.apache.servicemix.components.util;

import java.util.Iterator;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;

import org.w3c.dom.DocumentFragment;

import org.apache.servicemix.jbi.NotInitialisedYetException;
import org.apache.servicemix.jbi.container.ActivationSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A Spring implementation of the {@link Bootstrap}
 * 
 * @version $Revision$
 */
public class SpringBootstrap implements Bootstrap, ApplicationContextAware {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(SpringBootstrap.class);

    private InstallationContext installContext;

    private ObjectName extensionMBeanName;

    private ApplicationContext applicationContext;

    public void init(InstallationContext ctx) throws JBIException {
        this.installContext = ctx;
    }

    public void cleanUp() throws JBIException {
    }

    public ObjectName getExtensionMBeanName() {
        return extensionMBeanName;
    }

    public void onInstall() throws JBIException {
        if (installContext == null) {
            throw new NotInitialisedYetException();
        }
        DocumentFragment fragment = installContext.getInstallationDescriptorExtension();
        if (fragment != null) {
            LOGGER.debug("Installation Descriptor Extension Found");
        } else {
            LOGGER.debug("Installation Descriptor Extension Not Found !");
        }
        // lets load this from Spring...
        Map map = applicationContext.getBeansOfType(ActivationSpec.class, false, false);
        for (Iterator iter = map.values().iterator(); iter.hasNext();) {
            ActivationSpec spec = (ActivationSpec) iter.next();
            LOGGER.debug("Registering {}", spec.getComponentName());
        }
    }

    public void onUninstall() throws JBIException {
    }

    public InstallationContext getInstallContext() {
        return installContext;
    }

    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.applicationContext = appCtx;
    }

}
