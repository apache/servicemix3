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

import java.util.Map;

import org.apache.tuscany.common.logging.Log;
import org.apache.tuscany.common.logging.LogFactory;
import org.apache.tuscany.core.addressing.EndpointReference;
import org.apache.tuscany.core.invocation.InvocationConfiguration;
import org.apache.tuscany.core.invocation.ProxyConfiguration;
import org.apache.tuscany.core.message.Message;
import org.apache.tuscany.core.message.handler.MessageHandler;
import org.apache.tuscany.core.runtime.TuscanyModuleComponentContext;
import org.apache.tuscany.model.assembly.Binding;
import org.apache.tuscany.model.assembly.ConfiguredService;
import org.apache.tuscany.model.assembly.ExternalService;
import org.apache.tuscany.model.assembly.Interface;
import org.apache.tuscany.model.assembly.Part;
import org.apache.tuscany.model.types.InterfaceType;
import org.apache.tuscany.model.types.OperationType;
import org.osoa.sca.CurrentModuleContext;
import org.osoa.sca.ServiceRuntimeException;
import org.osoa.sca.model.JbiBinding;

public class ExternalJbiServiceConfigurationHandler implements MessageHandler {
    public final static Log log = LogFactory.getLog(ExternalJbiServiceConfigurationHandler.class);

    /**
     * Constructor.
     */
    public ExternalJbiServiceConfigurationHandler() {
        super();
    }

    /**
     * @see org.apache.tuscany.core.message.handler.MessageHandler#processMessage(org.apache.tuscany.core.message.Message)
     */
    public boolean processMessage(Message message) {
        if (log.isEntryEnabled())
            log.entry("processMessage");
        try {

            // Get the endpoint reference of the target service and the service model element
            EndpointReference endpointReference = message.getEndpointReference();
            Object portEndpoint = endpointReference.getConfiguredPort();

            // Return immediately if the target is not an external service
            if (!(portEndpoint instanceof ConfiguredService))
                return false;
            ConfiguredService serviceEndpoint = (ConfiguredService) portEndpoint;
            Part part = serviceEndpoint.getPart();
            if (!(part instanceof ExternalService))
                return false;
            ExternalService externalService = (ExternalService) part;

            // Return immediately if this is not an external web service
            Binding binding = externalService.getBindings().get(0);
            if (!(binding instanceof JbiBinding))
                return false;
            JbiBinding jbiBinding = (JbiBinding) binding;

            TuscanyModuleComponentContext context = (TuscanyModuleComponentContext) CurrentModuleContext.getContext();

            // Get the proxy configuration
            ProxyConfiguration proxyConfiguration=(ProxyConfiguration)message.getBody();
            Map<OperationType, InvocationConfiguration> invocationConfigurations=proxyConfiguration.getInvocationConfigurations();
            
            // Get the business interface
            Interface targetInterface = serviceEndpoint.getService().getInterfaceContract();
            InterfaceType targetInterfaceType = targetInterface.getInterfaceType();

            // Create the invocation configurations
            for (InvocationConfiguration invocationConfiguration : invocationConfigurations.values()) {
                OperationType targetOperationType=targetInterfaceType.getOperationType(invocationConfiguration.getOperationType().getName());

                // Handle a business method invocation, get a message handler from the port
                ExternalJbiServiceHandler handler = new ExternalJbiServiceHandler(context, targetOperationType, externalService, jbiBinding, endpointReference);
                invocationConfiguration.addRequestHandler(handler);
            }

            return false;

        } catch (Exception e) {
            log.fatal(e);

            throw new ServiceRuntimeException(e);

        } finally {
            if (log.isEntryEnabled())
                log.exit("processMessage");
        }

    }

}
