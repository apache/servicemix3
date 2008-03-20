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
package org.apache.servicemix.jbi.installation;

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;

public class Bootstrap1 implements Bootstrap {

    private static Bootstrap delegate;
    private static InstallationContext installContext;

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#cleanUp()
     */
    public void cleanUp() throws JBIException {
        Bootstrap1.delegate.cleanUp();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#getExtensionMBeanName()
     */
    public ObjectName getExtensionMBeanName() {
        return Bootstrap1.delegate.getExtensionMBeanName();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#init(javax.jbi.component.InstallationContext)
     */
    public void init(InstallationContext ctx) throws JBIException {
        Bootstrap1.installContext = ctx;
        Bootstrap1.delegate.init(ctx);
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#onInstall()
     */
    public void onInstall() throws JBIException {
        Bootstrap1.delegate.onInstall();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#onUninstall()
     */
    public void onUninstall() throws JBIException {
        Bootstrap1.delegate.onUninstall();
    }

    /**
     * @return Returns the delegate.
     */
    public static Bootstrap getDelegate() {
        return Bootstrap1.delegate;
    }

    /**
     * @param delegate The delegate to set.
     */
    public static void setDelegate(Bootstrap delegate) {
        Bootstrap1.delegate = delegate;
    }

    /**
     * @return Returns the installContext.
     */
    public static InstallationContext getInstallContext() {
        return Bootstrap1.installContext;
    }
    
}
