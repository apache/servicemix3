package org.apache.servicemix.bpe;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

public class BPESpringComponentTest extends SpringTestSupport {

    public void test() throws Exception {
        ServiceMixClient client = new DefaultServiceMixClient(jbi);
        MessageExchange me = client.createInOutExchange();
        me.setService(new QName("urn:logicblaze:soa:loanbroker", "LoanBrokerService"));
        me.setOperation(new QName("getLoanQuote"));
        me.getMessage("in").setContent(new StringSource("<getLoanQuoteRequest xmlns=\"urn:logicblaze:soa:loanbroker\"><ssn>1234341</ssn><amount>100000.0</amount><duration>12</duration></getLoanQuoteRequest>"));
        long t0 = System.currentTimeMillis();
        client.sendSync(me);
        long t1 = System.currentTimeMillis();
        if (me.getError() != null) {
            throw me.getError();
        }
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        String out = new SourceTransformer().contentToString(me.getMessage("out"));
        System.err.println(out);
        System.err.println("Time: " + (t1 - t0));
        client.done(me);
    }
    
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/bpe/spring.xml");
    }
    
}
