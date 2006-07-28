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

public class Bootstrap2 implements Bootstrap {

    private static Bootstrap delegate;

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#cleanUp()
     */
    public void cleanUp() throws JBIException {
        delegate.cleanUp();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#getExtensionMBeanName()
     */
    public ObjectName getExtensionMBeanName() {
        return delegate.getExtensionMBeanName();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#init(javax.jbi.component.InstallationContext)
     */
    public void init(InstallationContext installContext) throws JBIException {
        delegate.init(installContext);
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#onInstall()
     */
    public void onInstall() throws JBIException {
        delegate.onInstall();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Bootstrap#onUninstall()
     */
    public void onUninstall() throws JBIException {
        delegate.onUninstall();
    }

    /**
     * @return Returns the delegate.
     */
    public static Bootstrap getDelegate() {
        return delegate;
    }

    /**
     * @param delegate The delegate to set.
     */
    public static void setDelegate(Bootstrap delegate) {
        Bootstrap2.delegate = delegate;
    }
    
}
