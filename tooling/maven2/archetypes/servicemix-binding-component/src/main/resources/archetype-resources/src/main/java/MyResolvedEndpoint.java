package ${packageName};

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.util.DOMUtil;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MyResolvedEndpoint implements ServiceEndpoint {

    public final static String EPR_URI = "urn:servicemix:my";
    public final static QName EPR_SERVICE = new QName(EPR_URI, "MyComponent");
    public final static String EPR_NAME = "epr";
    
    private DocumentFragment reference;
    private String epName;
    
    public MyResolvedEndpoint(DocumentFragment epr, String epName) {
        this.reference = epr;
        this.epName = epName;
    }

    public DocumentFragment getAsReference(QName operationName) {
        return reference;
    }

    public String getEndpointName() {
        return epName;
    }

    public QName[] getInterfaces() {
        return null;
    }

    public QName getServiceName() {
        return EPR_SERVICE;
    }
    
    public static ServiceEndpoint resolveEndpoint(DocumentFragment epr) {
        if (epr.getChildNodes().getLength() == 1) {
            Node child = epr.getFirstChild();
            if (child instanceof Element) {
                Element elem = (Element) child;
                String nsUri = elem.getNamespaceURI();
                String name = elem.getLocalName();
                // Check simple endpoints
                if (EPR_URI.equals(nsUri) && EPR_NAME.equals(name)) {
                    return new MyResolvedEndpoint(epr, DOMUtil.getElementText(elem));
                // Check WSA endpoints
                } else {
                    NodeList nl = elem.getElementsByTagNameNS("http://www.w3.org/2005/08/addressing", "Address");
                    if (nl.getLength() == 1) {
                        Element address = (Element) nl.item(0);
                        String uri = DOMUtil.getElementText(address);
                        if (uri != null) {
                            uri = uri.trim();
                            if (uri.startsWith("my://")) {
                                return new MyResolvedEndpoint(epr, uri);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
}
