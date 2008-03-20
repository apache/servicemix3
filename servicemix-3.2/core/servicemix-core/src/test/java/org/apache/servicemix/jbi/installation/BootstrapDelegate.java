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

public class BootstrapDelegate implements Bootstrap {

    private Bootstrap delegate;

    public BootstrapDelegate(Bootstrap delegate) {
        this.delegate = delegate;
    }

    public Bootstrap getDelegate() {
        return delegate;
    }

    public void setDelegate(Bootstrap delegate) {
        this.delegate = delegate;
    }

    public void cleanUp() throws JBIException {
        delegate.cleanUp();
    }

    public ObjectName getExtensionMBeanName() {
        return delegate.getExtensionMBeanName();
    }

    public void init(InstallationContext installContext) throws JBIException {
        delegate.init(installContext);
    }

    public void onInstall() throws JBIException {
        delegate.onInstall();
    }

    public void onUninstall() throws JBIException {
        delegate.onUninstall();
    }

}
