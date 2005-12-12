package org.servicemix.components.activesoap;

import org.codehaus.activesoap.transport.Invocation;
import org.codehaus.activesoap.util.XMLStreamFactory;

import javax.xml.stream.XMLStreamReader;

/**
 * An <a href="http://activesoap.codehaus.org/">ActiveSOAP</a> {@link Invocation} which uses JBI.
 *
 * @version $Revision$
 */
public class ASInvocation extends Invocation {
    private ASTransport asTransport;

    public ASInvocation(ASTransport transport, XMLStreamFactory streamFactory) {
        super(transport, streamFactory);
        this.asTransport = transport;
    }

    public void invokeOneWay() throws Exception {
        String xml = getRequestText();
        asTransport.invokeOneWay(this, xml);
    }

    public XMLStreamReader invokeRequest() throws Exception {
        String xml = getRequestText();
        return asTransport.invokeRequest(this, xml);
    }
}
