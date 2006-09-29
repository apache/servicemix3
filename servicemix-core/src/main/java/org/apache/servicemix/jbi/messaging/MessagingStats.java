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
package org.apache.servicemix.jbi.messaging;

import org.apache.servicemix.jbi.management.stats.CountStatisticImpl;
import org.apache.servicemix.jbi.management.stats.StatsImpl;
import org.apache.servicemix.jbi.management.stats.TimeStatisticImpl;
import org.apache.servicemix.jbi.util.IndentPrinter;


/**
 * Basic J2EE stats for the messaging in the NMR
 * 
 * @version $Revision$
 */
public class MessagingStats extends StatsImpl {
    private String componentName;
    protected CountStatisticImpl inboundExchanges;
    protected CountStatisticImpl outboundExchanges;
    protected TimeStatisticImpl inboundExchangeRate;
    protected TimeStatisticImpl outboundExchangeRate;

    /**
     * Default Constructor
     * @param componentName
     */
    public MessagingStats(String componentName) {
        this.componentName = componentName;
        inboundExchanges = new CountStatisticImpl("inboundExchanges", "Number of Inbound MessageExchanges");
        outboundExchanges = new CountStatisticImpl("outboundExchanges", "Number of Outbound MessageExchanges");
        inboundExchangeRate = new TimeStatisticImpl("inboundExchangeRate", "time taken to process an Exchange");
        outboundExchangeRate = new TimeStatisticImpl("outboundExchangeRate", "time taken to send an Exchange");
        addStatistic("inboundExchanges", inboundExchanges);
        addStatistic("outboundExchanges", outboundExchanges);
        addStatistic("inboundExchangeRate", inboundExchangeRate);
        addStatistic("outboundExchangeRate", outboundExchangeRate);
    }
    
    /**
     * @return Returns the componentName.
     */
    public String getComponentName() {
        return componentName;
    }
    /**
     * @return Returns the inboundExchangeRate.
     */
    public TimeStatisticImpl getInboundExchangeRate() {
        return inboundExchangeRate;
    }
    /**
     * @return Returns the inboundExchanges.
     */
    public CountStatisticImpl getInboundExchanges() {
        return inboundExchanges;
    }
    /**
     * @return Returns the outboundExchangeRate.
     */
    public TimeStatisticImpl getOutboundExchangeRate() {
        return outboundExchangeRate;
    }
    /**
     * @return Returns the outboundExchanges.
     */
    public CountStatisticImpl getOutboundExchanges() {
        return outboundExchanges;
    }

    /**
     * reset the Stats
     */
    public synchronized void reset() {
        super.reset();
        inboundExchanges.reset();
        outboundExchanges.reset();
        inboundExchangeRate.reset();
        outboundExchangeRate.reset();
    }

    /**
     * @return pretty print
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Component: ");
        buffer.append(componentName);
        buffer.append(" { ");
        buffer.append(inboundExchanges);
        buffer.append(" ");
        buffer.append(inboundExchangeRate);
        buffer.append(" ");
        buffer.append(outboundExchanges);
        buffer.append(" ");
        buffer.append(outboundExchangeRate);
        buffer.append(" }");
        return buffer.toString();
    }

    /**
     * Dump out to an IndentPrinter
     * 
     * @param out
     */
    public void dump(IndentPrinter out) {
        out.printIndent();
        out.println("Component: ");
        out.print(componentName);
        out.println(" {");
        out.incrementIndent();
        out.println(inboundExchanges);
        out.printIndent();
        out.println(inboundExchangeRate);
        out.printIndent();
        out.println(outboundExchanges);
        out.printIndent();
        out.decrementIndent();
        out.printIndent();
        out.println("}");
    }
    
}