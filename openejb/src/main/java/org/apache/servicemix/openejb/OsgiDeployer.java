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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.internal.context.support.BundleDelegatingClassLoader;

/**
 * 
 */
public class OsgiDeployer implements BundleListener, BundleContextAware, InitializingBean, DisposableBean {

    private static final Log LOGGER = LogFactory.getLog(OsgiDeployer.class);

    private BundleContext context;
    private Deployer deployer = new Deployer();

    public void setBundleContext(BundleContext context) {
        this.context = context;
    }

    public void afterPropertiesSet() throws Exception {
        System.out.println("Registering EJB deployer bundle listener");
        this.context.addBundleListener(this);
    }

    public void destroy() throws Exception {
        this.context.removeBundleListener(this);
    }

    public void bundleChanged(BundleEvent event) {
        try {
            if (event.getType() == BundleEvent.INSTALLED) {
                System.out.println("Checking bundle: " + event.getBundle().getSymbolicName());

                ClassLoader classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(
                                                event.getBundle(), BundleContext.class.getClassLoader());
                deployer.deploy(classLoader, event.getBundle().getLocation());
            }
        } catch (Exception e) {
            LOGGER.error("Error handling bundle event", e);
        }
    }

}