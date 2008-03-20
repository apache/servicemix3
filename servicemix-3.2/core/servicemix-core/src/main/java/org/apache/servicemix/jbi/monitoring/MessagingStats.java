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
package org.apache.servicemix.jbi.monitoring;

import org.apache.servicemix.jbi.monitoring.stats.CountStatisticImpl;
import org.apache.servicemix.jbi.monitoring.stats.StatsImpl;
import org.apache.servicemix.jbi.monitoring.stats.TimeStatisticImpl;
import org.apache.servicemix.jbi.util.IndentPrinter;


/**
 * Basic J2EE stats for the messaging in the NMR
 * 
 * @version $Revision$
 */
public class MessagingStats extends StatsImpl {

    protected CountStatisticImpl inboundExchanges;
    protected CountStatisticImpl outboundExchanges;
    protected TimeStatisticImpl inboundExchangeRate;
    protected TimeStatisticImpl outboundExchangeRate;
    private String name;

    /**
     * Default Constructor
     * @param name
     */
    public MessagingStats(String name) {
        this.name = name;
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
     * Default Constructor
     * @param name
     */
    public MessagingStats(String name, MessagingStats parent) {
        this.name = name;
        inboundExchanges = new CountStatisticImpl(parent.inboundExchanges, "inboundExchanges", "Number of Inbound MessageExchanges");
        outboundExchanges = new CountStatisticImpl(parent.outboundExchanges, "outboundExchanges", "Number of Outbound MessageExchanges");
        inboundExchangeRate = new TimeStatisticImpl(parent.inboundExchangeRate, "inboundExchangeRate", "time taken to process an Exchange");
        outboundExchangeRate = new TimeStatisticImpl(parent.outboundExchangeRate, "outboundExchangeRate", "time taken to send an Exchange");
        addStatistic("inboundExchanges", inboundExchanges);
        addStatistic("outboundExchanges", outboundExchanges);
        addStatistic("inboundExchangeRate", inboundExchangeRate);
        addStatistic("outboundExchangeRate", outboundExchangeRate);
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
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
        buffer.append("Statistics: ");
        buffer.append(name);
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
        out.println("Statistics: ");
        out.print(name);
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