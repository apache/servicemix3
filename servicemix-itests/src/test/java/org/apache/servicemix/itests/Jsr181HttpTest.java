package org.apache.servicemix.itests;

import java.io.StringWriter;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;

import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

public class Jsr181HttpTest extends SpringTestSupport {

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/itests/jsr181http.xml");
    }
    
    public void test() throws Exception {
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        Definition def = wsdlFactory.newWSDLReader().readWSDL("http://localhost:8194/Service/?wsdl");
        StringWriter writer = new StringWriter();
        wsdlFactory.newWSDLWriter().writeWSDL(def, writer);
        System.err.println(writer.toString());
    }

}
