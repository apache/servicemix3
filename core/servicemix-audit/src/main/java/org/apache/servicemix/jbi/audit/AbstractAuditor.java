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
package org.apache.servicemix.jbi.audit;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.event.ExchangeEvent;
import org.apache.servicemix.jbi.event.ExchangeListener;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.apache.servicemix.jbi.management.ParameterHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for ServiceMix auditors implementations.
 *
 * @since 2.1
 * @version $Revision$
 */
public abstract class AbstractAuditor extends BaseSystemService implements AuditorMBean, ExchangeListener {

    protected static final transient Logger LOGGER = LoggerFactory.getLogger(AbstractAuditor.class);
    
    private boolean asContainerListener = true;

    public JBIContainer getContainer() {
        return container;
    }

    public void setContainer(JBIContainer container) {
        this.container = container;
    }
    
    protected Class getServiceMBean() {
        return AuditorMBean.class;
    }

    /* (non-Javadoc)
     * @see javax.jbi.management.LifeCycleMBean#start()
     */
    public void start() throws javax.jbi.JBIException {
        super.start();
        doStart();
        if (isAsContainerListener()) {
            this.container.addListener(this);
        }
    }

    /* (non-Javadoc)
     * @see javax.jbi.management.LifeCycleMBean#stop()
     */
    public void stop() throws javax.jbi.JBIException {
        this.container.removeListener(this);
        doStop();
        super.stop();
    }
    
    protected void doStart() throws JBIException {

    }

    protected void doStop() throws JBIException {

    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.management.MBeanInfoProvider#getAttributeInfos()
     */
    public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
        // TODO: this should not be an attribute, as it can require access to database
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute(getObjectToManage(), "exchangeCount", "number of exchanges");
        return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.management.MBeanInfoProvider#getOperationInfos()
     */
    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        // TODO: add other operations infos
        OperationInfoHelper helper = new OperationInfoHelper();
        ParameterHelper ph = helper.addOperation(getObjectToManage(), "getExchangesByRange", 2, "retrieve a bunch messages");
        ph.setDescription(0, "fromIndex", "lower index of message (start from 0)");
        ph.setDescription(1, "toIndex", "upper index of message (exclusive, > fromIndex)");
        ph = helper.addOperation(getObjectToManage(), "getExchangeById", 1, "retrieve an exchange given its id");
        ph.setDescription(0, "id", "id of the exchange to retrieve");
        return OperationInfoHelper.join(super.getOperationInfos(), helper.getOperationInfos());
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchangeCount()
     */
    public abstract int getExchangeCount() throws AuditorException;
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchangeId(int)
     */
    public String getExchangeIdByIndex(int index) throws AuditorException {
        if (index < 0) {
            throw new IllegalArgumentException("index should be greater or equal to zero");
        }
        return getExchangeIdsByRange(index, index + 1)[0];
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchangeIds()
     */
    public String[] getAllExchangeIds() throws AuditorException {
        return getExchangeIdsByRange(0, getExchangeCount());
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchangeIds(int, int)
     */
    public abstract String[] getExchangeIdsByRange(int fromIndex, int toIndex)  throws AuditorException;
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchange(int)
     */
    public MessageExchange getExchangeByIndex(int index) throws AuditorException {
        if (index < 0) {
            throw new IllegalArgumentException("index should be greater or equal to zero");
        }
        return getExchangesByRange(index, index + 1)[0];
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchange(java.lang.String)
     */
    public MessageExchange getExchangeById(String id) throws AuditorException {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("id should be non null and non empty");
        }
        return getExchangesByIds(new String[] {id })[0];
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchanges()
     */
    public MessageExchange[] getAllExchanges() throws AuditorException {
        return getExchangesByRange(0, getExchangeCount());
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchanges(int, int)
     */
    public MessageExchange[] getExchangesByRange(int fromIndex, int toIndex) throws AuditorException {
        return getExchangesByIds(getExchangeIdsByRange(fromIndex, toIndex));
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#getExchanges(java.lang.String[])
     */
    public abstract MessageExchange[] getExchangesByIds(String[] ids) throws AuditorException;

    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#deleteExchanges()
     */
    public int deleteAllExchanges() throws AuditorException {
        return deleteExchangesByRange(0, getExchangeCount());
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#deleteExchange(int)
     */
    public boolean deleteExchangeByIndex(int index) throws AuditorException {
        if (index < 0) {
            throw new IllegalArgumentException("index should be greater or equal to zero");
        }
        return deleteExchangesByRange(index, index + 1) == 1;
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#deleteExchange(java.lang.String)
     */
    public boolean deleteExchangeById(String id) throws AuditorException {
        return deleteExchangesByIds(new String[] {id }) == 1;
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#deleteExchanges(int, int)
     */
    public int deleteExchangesByRange(int fromIndex, int toIndex) throws AuditorException {
        return deleteExchangesByIds(getExchangeIdsByRange(fromIndex, toIndex));
    }
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#deleteExchanges(java.lang.String[])
     */
    public abstract int deleteExchangesByIds(String[] ids) throws AuditorException;
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.jbi.audit.AuditorMBean#resendExchange(javax.jbi.messaging.MessageExchange)
     */
    public void resendExchange(MessageExchange exchange) throws JBIException {
        container.resendExchange(exchange);
    }

    /**
     * Test if Auditor should be included as a container listener
     * 
     * @return Returns the addToContainer.
     */
    public boolean isAsContainerListener() {
        return asContainerListener;
    }

    /**
     * Set if Auditor should be included as a container listener.
     * 
     * @param addToContainer
     *            The addToContainer to set.
     */
    public void setAsContainerListener(boolean addToContainer) {
        this.asContainerListener = addToContainer;
    }

    public void exchangeAccepted(ExchangeEvent event) {

    }

}
