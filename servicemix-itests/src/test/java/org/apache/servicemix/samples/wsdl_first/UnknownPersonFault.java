
package org.apache.servicemix.samples.wsdl_first;

import javax.xml.ws.WebFault;

@WebFault(name = "UnknownPersonFault", targetNamespace = "http://servicemix.apache.org/samples/wsdl-first/types")
public class UnknownPersonFault
    extends Exception
{

    private org.apache.servicemix.samples.wsdl_first.types.UnknownPersonFault faultInfo;

    public UnknownPersonFault(String message, org.apache.servicemix.samples.wsdl_first.types.UnknownPersonFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public UnknownPersonFault(String message, org.apache.servicemix.samples.wsdl_first.types.UnknownPersonFault faultInfo, Throwable t) {
        super(message, t);
        this.faultInfo = faultInfo;
    }

    public org.apache.servicemix.samples.wsdl_first.types.UnknownPersonFault getFaultInfo() {
        return faultInfo;
    }

}
