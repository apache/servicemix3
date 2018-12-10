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
package javax.jbi.component;
 
import java.util.MissingResourceException;
import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;   

public interface ComponentContext
{
    ServiceEndpoint activateEndpoint(QName serviceName, String endpointName)
        throws JBIException;
    
    void deactivateEndpoint(ServiceEndpoint endpoint)
        throws JBIException;

    void registerExternalEndpoint(ServiceEndpoint externalEndpoint)
        throws JBIException;

    void deregisterExternalEndpoint(ServiceEndpoint externalEndpoint)
        throws JBIException;

    ServiceEndpoint resolveEndpointReference(DocumentFragment epr);

    String getComponentName();

    DeliveryChannel getDeliveryChannel()
        throws MessagingException;

    ServiceEndpoint getEndpoint(QName service, String name);

    Document getEndpointDescriptor(ServiceEndpoint endpoint)
        throws JBIException;

    ServiceEndpoint[] getEndpoints(QName interfaceName);

    ServiceEndpoint[] getEndpointsForService(QName serviceName);

    ServiceEndpoint[] getExternalEndpoints(QName interfaceName);

    ServiceEndpoint[] getExternalEndpointsForService(QName serviceName);

    String getInstallRoot();

    Logger getLogger(String suffix, String resourceBundleName)
        throws MissingResourceException, JBIException;

    javax.jbi.management.MBeanNames getMBeanNames();

    javax.management.MBeanServer getMBeanServer();

    javax.naming.InitialContext getNamingContext();

    Object getTransactionManager();

    String getWorkspaceRoot();

}
