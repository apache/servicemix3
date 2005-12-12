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
