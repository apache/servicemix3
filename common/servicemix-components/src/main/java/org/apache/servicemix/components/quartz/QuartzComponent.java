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
package org.apache.servicemix.components.quartz;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.ComponentSupport;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jbi.JBIException;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * A <a href="http://www.opensymphony.com/quartz/">Quartz</a> component for triggering components when timer events fire.
 *
 * @version $Revision$
 */
public class QuartzComponent extends ComponentSupport implements MessageExchangeListener {

    private static final transient Logger logger = LoggerFactory.getLogger(QuartzComponent.class);

    public static final String COMPONENT_KEY = "org.apache.servicemix.component";

    private SchedulerFactory factory;
    private Scheduler scheduler;
    private Map triggers;
    private QuartzMarshaler marshaler = new DefaultQuartzMarshaler();

    public void start() throws JBIException {
        try {
            scheduler.start();
            super.start();
        } catch (SchedulerException e) {
            throw new JBIException(e);
        }
    }

    public void stop() throws JBIException {
        try {
            super.stop();
            scheduler.standby();
        } catch (SchedulerException e) {
            throw new JBIException(e);
        }
    }

    public void shutDown() throws JBIException {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            throw new JBIException(e);
        } finally {
            super.shutDown();
        }
    }

    public void addTrigger(Trigger trigger, JobDetail detail) throws JBIException {
        try {
            // lets default the trigger name to the job name
            if (trigger.getName() == null) {
                trigger.setName(detail.getName());
            }
            // lets default the trigger group to the job group
            if (trigger.getGroup() == null) {
                trigger.setGroup(detail.getGroup());
            }
            // default start time to now if not specified
            if (trigger.getStartTime() == null) {
                trigger.setStartTime(new Date());
            }
            detail.getJobDataMap().put(COMPONENT_KEY, this);
            Class jobClass = detail.getJobClass();
            if (jobClass == null) {
                detail.setJobClass(ServiceMixJob.class);
            }
            scheduler.scheduleJob(detail, trigger);
        }
        catch (SchedulerException e) {
            throw new JBIException("Failed to add trigger: " + trigger + " with detail: " + detail + ". Reason: " + e, e);
        }
    }


    /**
     * This method is invoked when a Quartz job is fired.
     *
     * @param context the Quartz Job context
     */
    public void onJobExecute(JobExecutionContext context) throws JobExecutionException {
        logger.debug("Firing Quartz Job with context: {}", context);
        try {
            InOnly exchange = getExchangeFactory().createInOnlyExchange();
            NormalizedMessage message = exchange.createMessage();
            getMarshaler().populateNormalizedMessage(message, context);
            exchange.setInMessage(message);
            send(exchange);
        }
        catch (MessagingException e) {
            throw new JobExecutionException(e);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    public SchedulerFactory getFactory() {
        return factory;
    }

    public void setFactory(SchedulerFactory factory) {
        this.factory = factory;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Map getTriggers() {
        return triggers;
    }

    public void setTriggers(Map triggers) {
        this.triggers = triggers;
    }

    public QuartzMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(QuartzMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void init() throws JBIException {
        super.init();
        try {
            if (scheduler == null) {
                if (factory == null) {
                    factory = new StdSchedulerFactory();
                }
                scheduler = factory.getScheduler();
            }
        }
        catch (SchedulerException e) {
            throw new JBIException(e);
        }

        if (triggers != null) {
            for (Iterator iter = triggers.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (key == null) {
                    throw new IllegalArgumentException("Key of the map cannot be null");
                }
                if (value == null) {
                    throw new IllegalArgumentException("Key of the map cannot be null");
                }
                if (!(key instanceof Trigger)) {
                    throw new IllegalArgumentException("Key of the map must be a Trigger but was: " + key.getClass().getName());
                }
                if (!(value instanceof JobDetail)) {
                    throw new IllegalArgumentException("Key of the map must be a JobDetail but was: " + value.getClass().getName());
                }
                addTrigger((Trigger) key, (JobDetail) value);
            }
        }
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        // As we send in-only MEPS, we will only
        // receive DONE or ERROR status
    }

}
