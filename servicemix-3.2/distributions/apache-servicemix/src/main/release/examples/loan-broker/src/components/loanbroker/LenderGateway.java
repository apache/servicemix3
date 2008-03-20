/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package loanbroker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.components.util.TransformComponentSupport;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

public class LenderGateway extends TransformComponentSupport {

    private static final Log log = LogFactory.getLog(LenderGateway.class); 

    protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        log.info("Receiving lender gateway request");
        double amount = ((Double) in.getProperty(Constants.PROPERTY_AMOUNT)).doubleValue();
        int score = ((Integer) in.getProperty(Constants.PROPERTY_SCORE)).intValue();
        int hlength = ((Integer) in.getProperty(Constants.PROPERTY_HISTORYLENGTH)).intValue();
        QName[] recipients;
        if (amount >= 75000.0 && score >= 600 && hlength >= 8) {
            recipients = new QName[] { new QName(Constants.LOANBROKER_NS, "bank1"), 
                                       new QName(Constants.LOANBROKER_NS, "bank2") };
        } else
        if (amount >= 10000.0 && amount < 75000.0 && score >= 400 && hlength >= 3) {
            recipients = new QName[] { new QName(Constants.LOANBROKER_NS, "bank3"), 
                                       new QName(Constants.LOANBROKER_NS, "bank4") };
        } else {
            recipients = new QName[] { new QName(Constants.LOANBROKER_NS, "bank5") };
        }
        out.setProperty(Constants.PROPERTY_RECIPIENTS, recipients);
        return true;
    }

}
