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

public class Bank extends TransformComponentSupport {

    private static final Log log = LogFactory.getLog(Bank.class); 

    protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        log.info("Receiving bank request");
        double rate = Math.random() * 10;
        out.setProperty(Constants.PROPERTY_RATE, new Double(rate));
        // Sleep some time
        try {
            Thread.sleep((int) (Math.random() * 10) * 10);
        } catch (InterruptedException e) {
            // Discard
        }
        return true;
    }

}
