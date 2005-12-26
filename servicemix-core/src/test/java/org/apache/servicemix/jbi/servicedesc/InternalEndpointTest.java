package org.apache.servicemix.jbi.servicedesc;

import org.apache.servicemix.jbi.framework.ComponentNameSpace;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;

import javax.xml.namespace.QName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

public class InternalEndpointTest extends TestCase {

    public void testSerializeDeserialize() throws Exception {
        ComponentNameSpace cns = new ComponentNameSpace("myContainer", "myName", "myId");
        InternalEndpoint e = new InternalEndpoint(cns, "myEndpoint", new QName("myService"));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(e);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object out = ois.readObject();
        
        assertNotNull(out);
        assertTrue(out instanceof InternalEndpoint);
        InternalEndpoint outE = (InternalEndpoint) out; 
        assertNotNull(outE.getComponentNameSpace());
        assertNotNull(outE.getServiceName());
        assertNotNull(outE.getEndpointName());
    }

}
