
package org.apache.servicemix.samples.wsdl_first;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import org.apache.servicemix.samples.wsdl_first.types.GetPersonResponse;

@WebService(name = "Person", targetNamespace = "http://servicemix.apache.org/samples/wsdl-first")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface Person {


    @WebMethod(operationName = "GetPerson", action = "")
    @WebResult(name = "GetPersonResponse", targetNamespace = "http://servicemix.apache.org/samples/wsdl-first/types")
    public GetPersonResponse getPerson(
        @WebParam(name = "GetPerson", targetNamespace = "http://servicemix.apache.org/samples/wsdl-first/types")
        org.apache.servicemix.samples.wsdl_first.types.GetPerson GetPerson)
        throws UnknownPersonFault
    ;

}
