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
package org.apache.servicemix.web;

import javax.management.MalformedObjectNameException;

import org.apache.servicemix.web.filter.Factory;
import org.apache.servicemix.web.model.Endpoint;
import org.apache.servicemix.web.model.Registry;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jmx.support.ObjectNameManager;

public class EndpointFactoryBean implements FactoryBean {

    private Registry registry;
    
    public Object getObject() throws Exception {
        return new Factory() {
            private String objectName;
            private boolean showWsdl;
            public Object getBean() {
                try {
                    Endpoint ep = registry.getEndpoint(ObjectNameManager.getInstance(objectName));
                    ep.setShowWsdl(showWsdl);
                    return ep;
                } catch (MalformedObjectNameException e) {
                    return null;
                }
            }
            @SuppressWarnings("unused")
            public void setObjectName(String objectName) {
                this.objectName = objectName;
            }
            @SuppressWarnings("unused")
            public void setShowWsdl(boolean showWsdl) {
                this.showWsdl = showWsdl;
            }
        };
    }

    public Class getObjectType() {
        return Factory.class;
    }

    public boolean isSingleton() {
        return false;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

}
