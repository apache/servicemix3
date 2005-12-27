/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.nmr.flow.jms.JMSFlow;

import javax.jbi.JBIException;

/**
 * Provides remote access to ServiceMix JBI Containers running on the JMS NMR Flow
 * The RemoteServiceMixClient creates an enbedded JBIContainer and set the 
 * flow to use JMSFlow @see org.apache.servicemix.jbi.nmr.flow.jms.JMSFlow
 * 
 * @version $Revision$
 */
public class RemoteServiceMixClient extends DefaultServiceMixClient{

    private JBIContainer container;
    private ActivationSpec activationSpec;
    private String uri;
    private JMSFlow jmsFlow;
    private AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Create a RemoteServiceMixClient - setting the default
     * transport for the JMSFlow to be peer:// 
     * 
     */
    public RemoteServiceMixClient(){
        this("peer://org.apache.servicemix");
    }

    /**
     * Create a RemoteServiceMixClient
     * @param uri 
     * 
     */
    public RemoteServiceMixClient(String uri){
        this(uri,new ActivationSpec());
    }

    /**
     * Create a RemoteServiceMixClient
     * @param uri 
     * @param activationSpec 
     */
    public RemoteServiceMixClient(String uri,ActivationSpec activationSpec){
        container = new JBIContainer();
        this.uri = uri;
        this.activationSpec = activationSpec;

    }

    /**
     * init initializes the embedded JBIContainer
     * 
     * @throws JBIException
     */
    public void init() throws JBIException{
        if(initialized.compareAndSet(false, true)){
            jmsFlow = new JMSFlow();
            jmsFlow.setJmsURL(uri);
            container.setFlow(jmsFlow);
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
    public void start() throws javax.jbi.JBIException{
        init();
        container.start();
        super.start();
    }

    /**
     * Stop the item. This suspends current messaging activities.
     * 
     * @exception javax.jbi.JBIException
     *                if the item fails to stop.
     */
    public void stop() throws javax.jbi.JBIException{
        super.stop();
        container.stop();
    }

    /**
     * Shut down the item. The releases resources, preparatory to uninstallation.
     * 
     * @exception javax.jbi.JBIException
     *                if the item fails to shut down.
     */
    public void shutDown() throws javax.jbi.JBIException{
        super.shutDown();
        container.shutDown();
    }

}
