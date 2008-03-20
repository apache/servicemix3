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
package org.apache.servicemix.jbi.servicedesc;

import java.io.Serializable;

import javax.jbi.servicedesc.ServiceEndpoint;

import org.apache.servicemix.jbi.framework.ComponentNameSpace;

public abstract class AbstractServiceEndpoint implements ServiceEndpoint, Serializable {

    private ComponentNameSpace componentName;
    private String key;
    private String uniqueKey;
    
    public AbstractServiceEndpoint(ComponentNameSpace componentName) {
        this.componentName = componentName;
    }
    
    protected AbstractServiceEndpoint() {
    }

    /**
     * get the id of the ComponentConnector
     * @return the id
     */
    public ComponentNameSpace getComponentNameSpace() {
        return componentName;
    }

    public void setComponentName(ComponentNameSpace componentName) {
        this.componentName = componentName;
    }

    public String getKey() {
        if (key == null) {
            key = EndpointSupport.getKey(getServiceName(), getEndpointName());
        }
        return key;
    }

    public String getUniqueKey() {
        if (uniqueKey == null) {
            uniqueKey = getClassifier() + ":" + getKey();
        }
        return uniqueKey;
    }

    protected abstract String getClassifier();
}
