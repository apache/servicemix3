package org.apache.servicemix.itests;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

public class EIPAndXsltTest extends SpringTestSupport {

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/itests/eipxslt.xml");
    }
    
    public void test() throws Exception {
        ServiceMixClient client = new DefaultServiceMixClient(jbi);
        InOut me = client.createInOutExchange();
        me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
        me.setService(new QName("http://servicemix.org/test/", "routingSlip"));
        client.sendSync(me, 5000);
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        client.done(me);
    }

}
