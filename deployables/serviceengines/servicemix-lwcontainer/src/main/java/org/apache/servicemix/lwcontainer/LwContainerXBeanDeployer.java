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
package org.apache.servicemix.lwcontainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jbi.management.DeploymentException;

import org.apache.servicemix.common.BaseComponent;
import org.apache.servicemix.common.Endpoint;
import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.common.xbean.AbstractXBeanDeployer;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.SpringServiceUnitContainer;
import org.springframework.context.support.AbstractXmlApplicationContext;

public class LwContainerXBeanDeployer extends AbstractXBeanDeployer {

    public LwContainerXBeanDeployer(BaseComponent component) {
        super(component);
    }

    protected String getXBeanFile() {
        return "servicemix";
    }

    @Override
    protected Collection<Endpoint> getServices(AbstractXmlApplicationContext applicationContext) throws Exception {
        try {
            List<Endpoint> services = new ArrayList<Endpoint>();
            Map<String, SpringServiceUnitContainer> containers = applicationContext.getBeansOfType(SpringServiceUnitContainer.class);
            for (SpringServiceUnitContainer suContainer : containers.values()) { 
                ActivationSpec[] specs = suContainer.getActivationSpecs();
                if (specs != null) {
                    for (int i = 0; i < specs.length; i++) {
                        services.add(new LwContainerEndpoint(specs[i]));
                    }
                }
                if (suContainer.getComponents() != null || suContainer.getEndpoints() != null
                        || suContainer.getListeners() != null || suContainer.getServices() != null) {
                    services.add(new LwContainerExtra(suContainer.getComponents(), suContainer.getEndpoints(),
                                                  suContainer.getListeners(), suContainer.getServices()));
                }
            }
            return services;
        } catch (Exception e) {
            throw new RuntimeException("Can not find 'jbi' bean", e);
        }
    }

    protected void validate(ServiceUnit su) throws DeploymentException {
    }

}
