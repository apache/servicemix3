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

import java.util.List;

import org.apache.tuscany.common.discovery.util.ServiceProviderRegistry;
import org.apache.tuscany.common.logging.LogProvider;
import org.apache.servicemix.common.BaseComponent;
import org.apache.servicemix.common.BaseLifeCycle;

public class ScaLifeCycle extends BaseLifeCycle {

	public ScaLifeCycle(BaseComponent component) {
		super(component);
		List l = ServiceProviderRegistry.getInstance().getServiceProviders(LogProvider.class);
		l.clear();
		l.add(new CommonsLoggingLogProvider());
	}

}
