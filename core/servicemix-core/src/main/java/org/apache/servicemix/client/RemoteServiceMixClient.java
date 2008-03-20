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
package org.apache.servicemix.client;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.JBIException;

import org.apache.servicemix.id.IdGenerator;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.nmr.flow.jms.JMSFlow;

/**
 * Provides remote access to ServiceMix JBI Containers running on the JMS NMR Flow
 * The RemoteServiceMixClient creates an enbedded JBIContainer and set the 
 * flow to use JMSFlow @see org.apache.servicemix.jbi.nmr.flow.jms.JMSFlow
 * 
 * @version $Revision$
 */
public class RemoteServiceMixClient extends DefaultServiceMixClient {

    private JBIContainer container;
    private ActivationSpec activationSpec;
    private String uri;
    private JMSFlow jmsFlow;
    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicBoolean started = new AtomicBoolean(false);

    /**
     * Create a RemoteServiceMixClient - setting the default
     * transport for the JMSFlow to be peer:// 
     * 
     */
    public RemoteServiceMixClient() {
        this("peer://org.apache.servicemix?persistent=false");
    }

    /**
     * Create a RemoteServiceMixClient
     * @param uri 
     * 
     */
    public RemoteServiceMixClient(String uri) {
        this(uri, new ActivationSpec());
    }

    /**
     * Create a RemoteServiceMixClient
     * @param uri 
     * @param activationSpec 
     */
    public RemoteServiceMixClient(String uri, ActivationSpec activationSpec) {
        container = new JBIContainer();
        container.setEmbedded(true);
        container.setUseMBeanServer(false);
        container.setName(new IdGenerator().generateSanitizedId());
        this.uri = uri;
        this.activationSpec = activationSpec;

    }

    /**
     * init initializes the embedded JBIContainer
     * 
     * @throws JBIException
     */
    public void init() throws JBIException {
        if (initialized.compareAndSet(false, true)) {
            jmsFlow = new JMSFlow();
            jmsFlow.setJmsURL(uri);
            container.setFlow(jmsFlow);
            container.setEmbedded(true);
            container.setUseMBeanServer(false);
            container.setCreateMBeanServer(false);
            container.setMonitorDeploymentDirectory(false);
            container.setMonitorInstallationDirectory(false);
            container.init();
            activationSpec.setComponent(this);
            container.activateComponent(activationSpec);
        }
    }

    /**
     * Start the item.
     * 
     * @exception javax.jbi.JBIException
     *                if the item fails to start.
     */
    public void start() throws JBIException {
        start(Long.MAX_VALUE);
    }
    
    public void start(long timeout) throws JBIException {
        init();
        if (started.compareAndSet(false, true)) {
            container.start();
            if (timeout > 0) {
                // Wait for cluster to be connected
                // This is very ugly but we have no way yet to be notified
                // of cluster events.
                long start = System.currentTimeMillis();
                while (jmsFlow.numberInNetwork() == 0
                           && System.currentTimeMillis() - start < timeout) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new JBIException(e);
                    }
                }
                if (jmsFlow.numberInNetwork() == 0) {
                    throw new JBIException("Timeout while connecting to remote JBI container");
                }
            }
            super.start();
        }
    }

    /**
     * Stop the item. This suspends current messaging activities.
     * 
     * @exception javax.jbi.JBIException
     *                if the item fails to stop.
     */
    public void stop() throws JBIException {
        super.stop();
    }

    /**
     * Shut down the item. The releases resources, preparatory to uninstallation.
     * 
     * @exception javax.jbi.JBIException
     *                if the item fails to shut down.
     */
    public void shutDown() throws JBIException {
        super.shutDown();
        container.shutDown();
    }
    
    public String getContainerName() {
        return container.getName();
    }
    
    public void setContainerName(String name) {
        container.setName(name);
    }
    
    public void close() throws JBIException {
        shutDown();
    }

}
