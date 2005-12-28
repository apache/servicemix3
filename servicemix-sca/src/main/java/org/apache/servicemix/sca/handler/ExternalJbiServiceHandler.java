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
package org.apache.servicemix.sca.handler;

import org.apache.tuscany.core.addressing.EndpointReference;
import org.apache.tuscany.core.message.Message;
import org.apache.tuscany.core.message.handler.MessageHandler;
import org.apache.tuscany.core.runtime.TuscanyModuleComponentContext;
import org.apache.tuscany.model.assembly.ExternalService;
import org.apache.tuscany.model.types.OperationType;
import org.osoa.sca.model.JbiBinding;

public class ExternalJbiServiceHandler implements MessageHandler {

	public ExternalJbiServiceHandler(TuscanyModuleComponentContext context, 
									 OperationType type, 
									 ExternalService externalService, 
									 JbiBinding jbiBinding, 
									 EndpointReference endpointReference) {
		// TODO Auto-generated constructor stub
	}

	public boolean processMessage(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

}
