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
package org.apache.servicemix.common.xbean;

import javax.jbi.management.DeploymentException;

import org.apache.servicemix.common.BaseComponent;
import org.apache.servicemix.common.Endpoint;

public class BaseXBeanDeployer extends AbstractXBeanDeployer {

    private final Class[] endpointClasses;
    
    public BaseXBeanDeployer(BaseComponent component) {
        this(component, new Class[0]);
    }
    
    public BaseXBeanDeployer(BaseComponent component, Class endpointClass) {
        this(component, new Class[] { endpointClass });
    }
    
    public BaseXBeanDeployer(BaseComponent component, Class[] endpointClasses) {
        super(component);
        if (endpointClasses == null) {
            throw new NullPointerException("endpointClasses must be non null");
        }
        this.endpointClasses = endpointClasses;
    }
    
    protected boolean validate(Endpoint endpoint) throws DeploymentException {
        for (int i = 0; i < endpointClasses.length; i++) {
            if (endpointClasses[i].isInstance(endpoint)) {
                endpoint.validate();
                return true;
            }
        }
        return false;
    }
    
}
