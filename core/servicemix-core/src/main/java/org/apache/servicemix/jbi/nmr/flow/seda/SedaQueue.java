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
package org.apache.servicemix.jbi.nmr.flow.seda;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessagingException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;

import org.apache.servicemix.executors.Executor;
import org.apache.servicemix.jbi.framework.ComponentNameSpace;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.BaseLifeCycle;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple Straight through flow
 * 
 * @version $Revision$
 */
public class SedaQueue extends BaseLifeCycle {
    
    private static final transient Logger LOGGER = LoggerFactory.getLogger(SedaQueue.class);
    
    protected SedaFlow flow;
    protected ComponentNameSpace name;
    protected AtomicBoolean started = new AtomicBoolean(false);
    protected AtomicBoolean running = new AtomicBoolean(false);
    protected ObjectName objectName;
    protected String subType;
    protected Thread thread;
    protected Executor executor;

    /**
     * SedaQueue name
     * 
     * @param name
     */
    public SedaQueue(ComponentNameSpace name) {
        this.name = name;
    }

    /**
     * Get the name
     * 
     * @return name
     */
    public String getName() {
        return name.getName();
    }

    public String getType() {
        return "SedaQueue";
    }

    /**
     * @return Return the name
     */
    public ComponentNameSpace getComponentNameSpace() {
        return this.name;
    }

    /**
     * Get the description
     * 
     * @return description
     */
    public String getDescription() {
        return "bounded worker Queue for the NMR";
    }

    /**
     * Initialize the Region
     * 
     * @param seda
     */
    public void init(SedaFlow seda) {
        this.flow = seda;
    }

    /**
     * @return the capacity of the Queue
     */
    public int getCapacity() {
        if (executor == null) {
            return -1;
        }
        return this.executor.capacity();
    }

    /**
     * @return size of the Queue
     */
    public int getSize() {
        if (executor == null) {
            return -1;
        }
        return this.executor.size();
    }

    /**
     * Enqueue a Packet for processing
     * 
     * @param me
     * @throws InterruptedException
     * @throws MessagingException 
     */
    public void enqueue(final MessageExchangeImpl me) throws InterruptedException, MessagingException {
        executor.execute(new Runnable() {
            public void run() {
                try {
                    LOGGER.debug("{} dequeued exchange: {}", this, me);
                    flow.doRouting(me);
                } catch (Throwable e) {
                    LOGGER.error(this + " got error processing " + me, e);
                }
            }
        });
    }

    /**
     * start processing
     * 
     * @throws JBIException
     */
    public void start() throws JBIException {
        this.executor = flow.getExecutorFactory().createExecutor("flow.seda." + getName());
        super.start();
    }

    /**
     * stop processing
     * 
     * @throws JBIException
     */
    public void stop() throws JBIException {
        super.stop();
        this.executor.shutdown();
    }

    /**
     * shutDown the Queue
     * 
     * @throws JBIException
     */
    public void shutDown() throws JBIException {
        stop();
        super.shutDown();
    }

    /**
     * @return pretty print
     */
    public String toString() {
        return "SedaQueue{" + name + "}";
    }

    /**
     * Get an array of MBeanAttributeInfo
     * 
     * @return array of AttributeInfos
     * @throws JMException
     */
    public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute(getObjectToManage(), "capacity", "The capacity of the SedaQueue");
        helper.addAttribute(getObjectToManage(), "size", "The size (depth) of the SedaQueue");
        return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
    }

    /**
     * @return Returns the objectName.
     */
    public ObjectName getObjectName() {
        return objectName;
    }

    /**
     * @param objectName The objectName to set.
     */
    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

}
