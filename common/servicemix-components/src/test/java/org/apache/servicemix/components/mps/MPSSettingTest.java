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
package org.apache.servicemix.components.mps;

import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.TestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * @version $Revision$
 */
public class MPSSettingTest extends TestSupport {

	/**
	 * load the property set from the spring injects string (fixed set)
	 * @throws Exception
	 */
    public void testStaticStringPropertySet() throws Exception {
        QName service = new QName("http://servicemix.org/cheese/", "mpsFixed");

        InOut exchange = client.createInOutExchange();
        exchange.setService(service);

        NormalizedMessage message = exchange.getInMessage();
        message.setContent(new StringSource(createMessageXmlText(777888)));
        message.setProperty("prop.xpath.or.keep.existing","someValue");
        message.setProperty("other.set.property","thatOtherValue");
        client.sendSync(exchange);

        NormalizedMessage outMessage = exchange.getOutMessage();

        assertTrue(outMessage.getProperty("property.1").equals("foobarAndCheese"));
        assertTrue(outMessage.getProperty("property.2").equals("777888"));
        assertTrue(outMessage.getProperty("prop.xpath.or.keep.existing").equals("someValue"));
        // this property is configured, but there is no "value to resolve it to"
        assertTrue(outMessage.getProperty("new.prop.name") == null);
        assertTrue(outMessage.getProperty("property.3").equals("thatOtherValue"));
    }

    /**
     * Load the property set using an xpath applied to the in message
     * @throws Exception
     */
    public void testtestXpathLoadingPropertySet() throws Exception {
        QName service = new QName("http://servicemix.org/cheese/", "mpsXpath");

        InOut exchange = client.createInOutExchange();
        exchange.setService(service);

        NormalizedMessage message = exchange.getInMessage();
        message.setContent(new StringSource(createMessageXmlText(400)));
        client.sendSync(exchange);

        NormalizedMessage outMessage = exchange.getOutMessage();

        assertTrue(outMessage.getProperty("my-superdooper.property").equals("wishAusMadeItTotheFinals"));
    }
    
	/**
	 * load the property set from a JBI property value
	 * @throws Exception
	 */
    public void testJBIPropertyLoadingPropertySet() throws Exception {
        QName service = new QName("http://servicemix.org/cheese/", "mpsJBIPropsPS");

        InOut exchange = client.createInOutExchange();
        exchange.setService(service);

        NormalizedMessage message = exchange.getInMessage();
        message.setProperty(MessagePropertySetterXML.MPS_PROP_NAME_PROPERTYSET,"hello");
        message.setContent(new StringSource(createMessageXmlText(54)));
        client.sendSync(exchange);

        NormalizedMessage outMessage = exchange.getOutMessage();

        assertTrue(outMessage.getProperty("my-superdooper.property").equals("wishAusMadeItTotheFinals"));
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/mps/servicemix-mps-test.xml");
    }

}


