package org.apache.servicemix.common;

import java.util.List;

import junit.framework.TestCase;

public class EPRTest extends TestCase {


    public void testEPR() {
        DummyComponent component = new DummyComponent();
        assertEquals("urn:servicemix:dummy", component.getEPRServiceName().getNamespaceURI());
        assertEquals("DummyComponent", component.getEPRServiceName().getLocalPart());
        assertEquals("urn:servicemix:dummy", component.getEPRElementName().getNamespaceURI());
        assertEquals("epr", component.getEPRElementName().getLocalPart());
        assertEquals(1, component.getEPRProtocols().length);
        assertEquals("dummy:", component.getEPRProtocols()[0]);
    }
    
    public static class DummyComponent extends DefaultComponent {

        @Override
        protected List getConfiguredEndpoints() {
            return null;
        }

        @Override
        protected Class[] getEndpointClasses() {
            return null;
        }

    }
}
