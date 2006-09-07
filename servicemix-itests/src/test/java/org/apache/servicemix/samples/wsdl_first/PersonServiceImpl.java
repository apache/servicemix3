
package org.apache.servicemix.samples.wsdl_first;

import javax.jws.WebService;
import org.apache.servicemix.samples.wsdl_first.types.GetPersonResponse;

@WebService(serviceName = "PersonService", targetNamespace = "http://servicemix.apache.org/samples/wsdl-first", endpointInterface = "org.apache.servicemix.samples.wsdl_first.Person")
public class PersonServiceImpl
    implements Person
{


    public GetPersonResponse getPerson(org.apache.servicemix.samples.wsdl_first.types.GetPerson GetPerson)
        throws UnknownPersonFault
    {
        throw new UnsupportedOperationException();
    }

}
