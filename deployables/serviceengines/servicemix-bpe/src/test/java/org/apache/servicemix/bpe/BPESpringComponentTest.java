/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.bpe;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

public class BPESpringComponentTest extends SpringTestSupport {
    private static transient Log log = LogFactory.getLog(BPESpringComponentTest.class);

    public void test() throws Exception {
        ServiceMixClient client = new DefaultServiceMixClient(jbi);
        MessageExchange me = client.createInOutExchange();
        me.setService(new QName("urn:logicblaze:soa:loanbroker", "LoanBrokerService"));
        me.setOperation(new QName("getLoanQuote"));
        me.getMessage("in").setContent(new StringSource(
                                "<getLoanQuoteRequest xmlns=\"urn:logicblaze:soa:loanbroker\"><ssn>1234341</ssn>"
                                + "<amount>100000.0</amount><duration>12</duration></getLoanQuoteRequest>"));
        long t0 = System.currentTimeMillis();
        client.sendSync(me);
        long t1 = System.currentTimeMillis();
        if (me.getError() != null) {
            throw me.getError();
        }
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        String out = new SourceTransformer().contentToString(me.getMessage("out"));
        log.info(out);
        log.info("Time: " + (t1 - t0));
        client.done(me);
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/bpe/spring.xml");
    }

}
