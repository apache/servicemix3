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
package org.apache.servicemix.sca;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.servicedesc.ServiceEndpoint;

import org.apache.tuscany.core.runtime.webapp.TuscanyWebAppRuntime;
import org.apache.tuscany.model.assembly.EntryPoint;
import org.apache.servicemix.common.Endpoint;
import org.apache.servicemix.common.ExchangeProcessor;

public class ScaEndpoint extends Endpoint implements ExchangeProcessor {

    protected ServiceEndpoint activated;
    protected EntryPoint entryPoint;
	
	public ScaEndpoint(EntryPoint entryPoint) {
		this.entryPoint = entryPoint;
	}

	public Role getRole() {
		return Role.PROVIDER;
	}

	public void activate() throws Exception {
        logger = this.serviceUnit.getComponent().getLogger();
        ComponentContext ctx = this.serviceUnit.getComponent().getComponentContext();
        activated = ctx.activateEndpoint(service, endpoint);
        getProcessor().start();
	}

	public void deactivate() throws Exception {
        ServiceEndpoint ep = activated;
        activated = null;
        getProcessor().stop();
        ComponentContext ctx = this.serviceUnit.getComponent().getComponentContext();
        ctx.deactivateEndpoint(ep);
	}

	public ExchangeProcessor getProcessor() {
		return this;
	}

	public void process(MessageExchange exchange) throws Exception {
		TuscanyWebAppRuntime sca = ((ScaServiceUnit) getServiceUnit()).getTuscanyRuntime();
		Object mth = sca.getModuleComponentContext().locateService(entryPoint.getName());
		
	}

	public void start() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
