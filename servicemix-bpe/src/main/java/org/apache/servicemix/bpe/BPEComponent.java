/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.bpe;

import org.apache.servicemix.common.BaseComponent;
import org.apache.servicemix.common.BaseLifeCycle;
import org.apache.servicemix.common.BaseServiceUnitManager;
import org.apache.servicemix.common.Deployer;

import org.apache.ode.bpe.bped.EventDirector;

public class BPEComponent extends BaseComponent {

    public static String PART_PAYLOAD = "payload";
    
    private static BPEComponent INSTANCE;
    
    public static BPEComponent getInstance() {
        return INSTANCE;
    }
    
    public BPEComponent() {
        INSTANCE = this;
    }
    
	protected BaseLifeCycle createLifeCycle() {
		return new BPELifeCycle(this);
	}

	protected BaseServiceUnitManager createServiceUnitManager() {
		return new BaseServiceUnitManager(this, 
										  new Deployer[] { new BPEDeployer(this) });
	}
    
    public EventDirector getEventDirector() {
        return ((BPELifeCycle) getLifeCycle()).getEventDirector();
    }

}
