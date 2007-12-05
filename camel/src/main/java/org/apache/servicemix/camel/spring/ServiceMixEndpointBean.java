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
package org.apache.servicemix.camel.spring;

import org.apache.servicemix.nmr.api.NMR;
import org.apache.servicemix.nmr.api.internal.Flow;
import org.apache.servicemix.nmr.api.service.ServiceHelper;
import org.apache.servicemix.nmr.core.StraightThroughFlow;

public class ServiceMixEndpointBean {
	private NMR nmr;
	private Flow flow;
	
	public ServiceMixEndpointBean() {
		if (getNmr() != null && getNmr().getFlowRegistry() != null) {
			if (getFlow() != null) {
				// set Flow we get from configuration file
				getNmr().getFlowRegistry().register(getFlow(), 
						ServiceHelper.createMap(Flow.ID, getFlow().getClass().getName()));
			} else {
				// set defaule Flow
				getNmr().getFlowRegistry().register(
						new StraightThroughFlow(), ServiceHelper.createMap(Flow.ID, StraightThroughFlow.class.getName()));
			}
		}
	}
	public void setNmr(NMR nmr) {
		this.nmr = nmr;
	}

	public NMR getNmr() {
		return nmr;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	public Flow getFlow() {
		return flow;
	}
	
	
}
