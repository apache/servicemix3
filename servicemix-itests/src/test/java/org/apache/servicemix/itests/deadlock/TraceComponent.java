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
package org.apache.servicemix.itests.deadlock;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.tck.ReceiverComponent;
import org.w3c.dom.Node;

/**
 * A simple tracing component which can be placed inside a pipeline
 * to trace the message exchange though the component.
 *
 * @version $Revision: 426415 $
 */
public class TraceComponent extends ReceiverComponent {

    private Log log = LogFactory.getLog(TraceComponent.class);
    private int msToSleep=0;
    
    private SourceTransformer sourceTransformer = new SourceTransformer();

    public int getMsToSleep() {
		return msToSleep;
	}

	public void setMsToSleep(int msToSleep) {
		this.msToSleep = msToSleep;
	}

	public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public SourceTransformer getSourceTransformer() {
        return sourceTransformer;
    }

    public void setSourceTransformer(SourceTransformer sourceTransformer) {
        this.sourceTransformer = sourceTransformer;
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        // lets dump the incoming message
        NormalizedMessage message = exchange.getMessage("in");
        if (message == null) {
            log.warn("Received null message from exchange: " + exchange);
        }
        else {
            try {
                Node node = sourceTransformer.toDOMNode(message.getContent());
                String str = sourceTransformer.toString(new DOMSource(node));
                log.info("Body is: " + str);
            }
            catch (Exception e) {
                log.error("Failed to turn message body into text: " + e, e);
            }
        }
        
        sleepComponent(msToSleep);
        
        super.onMessageExchange(exchange);
    }

	private void sleepComponent(int ms) {
		try{
			if (ms > 0){
	        	Thread.sleep(ms);
	        }
		}
		catch (InterruptedException ie){
			log.warn("Thread was interrupted.");
		}
	}
    
    
    
}
