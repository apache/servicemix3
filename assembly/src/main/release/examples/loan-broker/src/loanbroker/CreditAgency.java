/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package loanbroker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.servicemix.components.util.TransformComponentSupport;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

public class CreditAgency extends TransformComponentSupport {

    private static final Log log = LogFactory.getLog(CreditAgency.class); 

    protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        log.info("Receiving credit agency request");
        int score = (int) (Math.random() * 600 + 300);
        int hlength = (int) (Math.random() * 19 + 1);
        out.setProperty(Constants.PROPERTY_SCORE, new Integer(score));
        out.setProperty(Constants.PROPERTY_HISTORYLENGTH, new Integer(hlength));
        return true;
    }

}
