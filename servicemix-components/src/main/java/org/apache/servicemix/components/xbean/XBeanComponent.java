/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.apache.servicemix.components.xbean;

import javax.jbi.messaging.MessageExchange;

import org.apache.servicemix.components.AbstractDeployableComponent;
import org.apache.servicemix.components.ServiceUnit;

public class XBeanComponent extends AbstractDeployableComponent {

    protected ServiceUnit doDeploy(String serviceUnitName, String serviceUnitRootPath) throws Exception {
        return new XBeanServiceUnit(this, serviceUnitName, serviceUnitRootPath);
    }

    protected void process(MessageExchange me) {
    }


}
