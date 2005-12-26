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
package org.apache.servicemix.components;

import java.util.HashMap;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.spi.work.Work;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.jencks.factory.WorkManagerFactoryBean;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

public abstract class AbstractComponent implements Component, ComponentLifeCycle {

    protected final transient Log logger = LogFactory.getLog(getClass());
    
    protected ComponentContext context;
    
    protected ObjectName mbeanName;
    
    protected Map serviceDescriptions = new HashMap();
    
    protected Thread meListener;
    
    protected GeronimoWorkManager workManager;
    
    public ComponentContext getContext() {
        return context;
    }
    
    /* (non-Javadoc)
     * @see javax.jbi.component.Component#getLifeCycle()
     */
    public ComponentLifeCycle getLifeCycle() {
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Component#getServiceUnitManager()
     */
    public ServiceUnitManager getServiceUnitManager() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Component#getServiceDescription(javax.jbi.servicedesc.ServiceEndpoint)
     */
    public Document getServiceDescription(ServiceEndpoint endpoint) {
        if (logger.isDebugEnabled()) {
            logger.debug("Querying service description for " + endpoint);
        }
        String key = getKey(endpoint.getServiceName(), endpoint.getEndpointName());
        Document doc = (Document) this.serviceDescriptions.get(key);
        if (logger.isDebugEnabled()) {
            if (doc != null) {
                logger.debug("Description found");
            } else {
                logger.debug("Description not found");
            }
        }
        return doc;
    }
    
    public void setServiceDescription(QName service, String endpoint, Document doc) {
        String key = getKey(service, endpoint);
        if (logger.isDebugEnabled()) {
            logger.debug("Setting service description for " + endpoint);
        }
        this.serviceDescriptions.put(key, doc);
    }
    
    private String getKey(QName service, String endpoint) {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append(service.getNamespaceURI());
        sb.append("}");
        sb.append(service.getLocalPart());
        sb.append(":");
        sb.append(endpoint);
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Component#isExchangeWithConsumerOkay(javax.jbi.servicedesc.ServiceEndpoint, javax.jbi.messaging.MessageExchange)
     */
    public boolean isExchangeWithConsumerOkay(ServiceEndpoint endpoint, MessageExchange exchange) {
        return true;
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Component#isExchangeWithProviderOkay(javax.jbi.servicedesc.ServiceEndpoint, javax.jbi.messaging.MessageExchange)
     */
    public boolean isExchangeWithProviderOkay(ServiceEndpoint endpoint, MessageExchange exchange) {
        return true;
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.Component#resolveEndpointReference(org.w3c.dom.DocumentFragment)
     */
    public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.ComponentLifeCycle#getExtensionMBeanName()
     */
    public final ObjectName getExtensionMBeanName() {
        return this.mbeanName;
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.ComponentLifeCycle#init(javax.jbi.component.ComponentContext)
     */
    public final void init(ComponentContext context) throws JBIException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Initializing component");
            }
            this.context = context;
            doInit();
            if (logger.isDebugEnabled()) {
                logger.debug("Component initialized");
            }
        } catch (JBIException e) {
            throw e;
        } catch (Exception e) {
            throw new JBIException("Error calling init", e);
        }
    }
    
    protected void doInit() throws Exception {
        Object mbean = getExtensionMBean();
        if (mbean != null) {
            this.mbeanName = createExtensionMBeanName();
            MBeanServer server = this.context.getMBeanServer();
            if (server == null) {
                throw new JBIException("null mBeanServer");
            }
            if (server.isRegistered(this.mbeanName)) {
                server.unregisterMBean(this.mbeanName);
            }
            server.registerMBean(mbean, this.mbeanName);
        }
        // Create work manager
        WorkManagerFactoryBean wmfb = new WorkManagerFactoryBean();
        this.workManager = wmfb.getWorkManager();
    }
    
    /**
     * 
     * @return the component extension MBean.
     * @throws Exception if an error occurs
     */
    protected Object getExtensionMBean() throws Exception {
        return null;
    }
    
    protected ObjectName createExtensionMBeanName() throws Exception {
        return this.context.getMBeanNames().createCustomComponentMBeanName("extension");
    }


    /* (non-Javadoc)
     * @see javax.jbi.component.ComponentLifeCycle#shutDown()
     */
    public final void shutDown() throws JBIException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Shutting down component");
            }
            doShutDown();
            this.context = null;
            if (logger.isDebugEnabled()) {
                logger.debug("Component shut down");
            }
        } catch (JBIException e) {
            throw e;
        } catch (Exception e) {
            throw new JBIException("Error calling shutdown", e);
        }
    }

    protected void doShutDown() throws Exception {
        if (this.mbeanName != null) {
            MBeanServer server = this.context.getMBeanServer();
            if (server == null) {
                throw new JBIException("null mBeanServer");
            }
            if (server.isRegistered(this.mbeanName)) {
                server.unregisterMBean(this.mbeanName);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.ComponentLifeCycle#start()
     */
    public final void start() throws JBIException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Starting component");
            }
            doStart();
            if (logger.isDebugEnabled()) {
                logger.debug("Component started");
            }
        } catch (JBIException e) {
            throw e;
        } catch (Exception e) {
            throw new JBIException("Error calling init", e);
        }
    }

    protected void doStart() throws Exception {
        this.meListener = new Thread(new Runnable() {
            public void run() {
                try {
                    DeliveryChannel channel = AbstractComponent.this.context.getDeliveryChannel();
                    while (true) {
                        final MessageExchange me = channel.accept();
                        if (me.isTransacted()) {
                            TransactionManager mgr = (TransactionManager) AbstractComponent.this.context.getTransactionManager();
                            Transaction tx = (Transaction) me.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME);
                            if (tx == mgr.getTransaction()) {
                                mgr.suspend();
                            }
                        }
                        AbstractComponent.this.workManager.scheduleWork(new Work() {
                            public void release() {
                            }
                            public void run() {
                                try {
                                    if (me.isTransacted()) {
                                        TransactionManager mgr = (TransactionManager) AbstractComponent.this.context.getTransactionManager();
                                        Transaction tx = (Transaction) me.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME);
                                        mgr.resume(tx);
                                    }
                                    AbstractComponent.this.process(me);
                                } catch (Exception e) {
                                    logger.error("Error processing message", e);
                                }
                            } 
                        });
                    }
                } catch (Throwable e) {
                    if (e instanceof MessagingException) {
                        if (e.getCause() instanceof InterruptedException) {
                            // component has been stopped
                            return;
                        }
                    }
                    logger.error("An exception occured in component polling thread", e);
                }
            } 
        });
        this.meListener.setDaemon(true);
        this.meListener.start();
    }
    
    protected void process(MessageExchange me) throws Exception {
        
    }

    /* (non-Javadoc)
     * @see javax.jbi.component.ComponentLifeCycle#stop()
     */
    public final void stop() throws JBIException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Stopping component");
            }
            doStop();
            if (logger.isDebugEnabled()) {
                logger.debug("Component stopped");
            }
        } catch (JBIException e) {
            throw e;
        } catch (Exception e) {
            throw new JBIException("Error calling stop", e);
        }
    }

    protected void doStop() throws Exception {
        if (this.meListener != null) {
            this.meListener.interrupt();
            this.meListener.join();
        }
        if (this.workManager != null) {
            this.workManager.doStop();
        }
    }

}
