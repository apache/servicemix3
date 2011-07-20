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
package org.apache.servicemix.jbi.framework.support;

import org.apache.servicemix.jbi.deployment.Provides;
import org.apache.servicemix.jbi.deployment.Services;
import org.apache.servicemix.jbi.framework.Registry;
import org.apache.servicemix.jbi.framework.ServiceUnitLifeCycle;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieve interface implemented by the given endpoint using the SU jbi descriptors.
 */
public class SUDescriptorProcessor implements EndpointProcessor {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(SUDescriptorProcessor.class);
    
    private Registry registry;
    
    public void init(Registry reg) {
        this.registry = reg;
    }

    /**
     * Retrieve interface implemented by the given endpoint using the SU jbi descriptors.
     * 
     * @param serviceEndpoint the endpoint being checked
     */
    public void process(InternalEndpoint serviceEndpoint) {
        ServiceUnitLifeCycle[] sus = registry.getDeployedServiceUnits(serviceEndpoint.getComponentNameSpace().getName());
        for (int i = 0; i < sus.length; i++) {
            Services services = sus[i].getServices();
            if (services != null) {
                Provides[] provides = services.getProvides();
                if (provides != null) {
                    for (int j = 0; j < provides.length; j++) {
                        if (provides[j].getInterfaceName() != null
                                && serviceEndpoint.getServiceName().equals(provides[j].getServiceName())
                                && serviceEndpoint.getEndpointName().equals(provides[j].getEndpointName())) {
                            LOGGER.debug("Endpoint {} is provided by SU {}", serviceEndpoint, sus[i].getName());
                            LOGGER.debug("Endpoint {} implements interface {}", serviceEndpoint, provides[j].getInterfaceName());
                            serviceEndpoint.addInterface(provides[j].getInterfaceName());
                        }
                    }
                }
            }
        }
    }

}
