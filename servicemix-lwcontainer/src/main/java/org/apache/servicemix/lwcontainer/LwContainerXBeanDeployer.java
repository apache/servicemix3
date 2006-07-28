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
import java.util.List;

import org.apache.servicemix.common.BaseComponent;
import org.apache.servicemix.common.xbean.AbstractXBeanDeployer;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.SpringServiceUnitContainer;
import org.apache.xbean.kernel.Kernel;
import org.apache.xbean.kernel.StringServiceName;

public class LwContainerXBeanDeployer extends AbstractXBeanDeployer {

    public LwContainerXBeanDeployer(BaseComponent component) {
        super(component);
    }

    protected String getXBeanFile() {
        return "servicemix";
    }

    protected List getServices(Kernel kernel) {
        try {
            Object jbi = kernel.getService(new StringServiceName("jbi"));
            SpringServiceUnitContainer suContainer = (SpringServiceUnitContainer) jbi; 
            ActivationSpec[] specs = suContainer.getActivationSpecs();
            List services = new ArrayList();
            for (int i = 0; i < specs.length; i++) {
                services.add(new LwContainerEndpoint(specs[i]));
            }
            return services;
        } catch (Exception e) {
            throw new RuntimeException("Can not find 'jbi' bean", e);
        }
    }

}
