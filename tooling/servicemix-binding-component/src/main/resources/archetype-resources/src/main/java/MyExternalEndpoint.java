package ${packageName};

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class MyExternalEndpoint implements ServiceEndpoint {

    private MyEndpoint endpoint;
    
    public MyExternalEndpoint(MyEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public DocumentFragment getAsReference(QName operationName) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder().newDocument();
            DocumentFragment df = doc.createDocumentFragment();
            Element e = doc.createElementNS(MyResolvedEndpoint.EPR_URI, MyResolvedEndpoint.EPR_NAME);
            Text t = doc.createTextNode(endpoint.getService() + "#" + endpoint.getEndpoint());
            e.appendChild(t);
            df.appendChild(e);
            return df;
        } catch (Exception e) {
            throw new RuntimeException("Could not create reference", e);
        }
    }

    public String getEndpointName() {
        return endpoint.getEndpoint();
    }

    public QName[] getInterfaces() {
        if (endpoint.getInterfaceName() != null) {
            return new QName[] { endpoint.getInterfaceName() }; 
        }
        return null;
    }

    public QName getServiceName() {
        return endpoint.getService();
    }

}
