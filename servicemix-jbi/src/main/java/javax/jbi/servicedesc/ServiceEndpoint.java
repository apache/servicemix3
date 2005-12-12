package javax.jbi.servicedesc;

import javax.xml.namespace.QName;

public interface ServiceEndpoint
{
    org.w3c.dom.DocumentFragment getAsReference(QName operationName);

    String getEndpointName();

    javax.xml.namespace.QName[] getInterfaces();

    javax.xml.namespace.QName getServiceName();
}
