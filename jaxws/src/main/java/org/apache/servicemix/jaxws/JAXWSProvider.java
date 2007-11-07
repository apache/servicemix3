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
package org.apache.servicemix.jaxws;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.cxf.interceptor.Interceptor;
//import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.servicemix.nmr.api.Channel;
import org.apache.servicemix.nmr.api.Endpoint;
import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.NMR;
import org.w3c.dom.DocumentFragment;

/**
 * 
 * @author ffang
 * @org.apache.xbean.XBean element="provider"
 */

public class JAXWSProvider implements Endpoint, ServiceEndpoint {

	
    private Object pojo;

    //private EndpointImpl endpoint;

    private List<Interceptor> in = new CopyOnWriteArrayList<Interceptor>();

    private List<Interceptor> out = new CopyOnWriteArrayList<Interceptor>();

    private List<Interceptor> outFault = new CopyOnWriteArrayList<Interceptor>();

    private List<Interceptor> inFault = new CopyOnWriteArrayList<Interceptor>();
    
    private Map properties;
    
    private boolean mtomEnabled;
	private Channel channel;
    private Queue<Exchange> queue;
    private QName serviceName;
    private String endpointName;
    private NMR nmr;

    public JAXWSProvider() {
    }
    
    /**
     * @return the pojo
     */
    public Object getPojo() {
        return pojo;
    }

    /**
     * @param pojo
     *            the pojo to set
     */
    public void setPojo(Object pojo) {
        this.pojo = pojo;
    }

    public List<Interceptor> getOutFaultInterceptors() {
        return outFault;
    }

    public List<Interceptor> getInFaultInterceptors() {
        return inFault;
    }

    public List<Interceptor> getInInterceptors() {
        return in;
    }

    public List<Interceptor> getOutInterceptors() {
        return out;
    }

    public void setInInterceptors(List<Interceptor> interceptors) {
        in = interceptors;
    }

    public void setInFaultInterceptors(List<Interceptor> interceptors) {
        inFault = interceptors;
    }

    public void setOutInterceptors(List<Interceptor> interceptors) {
        out = interceptors;
    }

    public void setOutFaultInterceptors(List<Interceptor> interceptors) {
        outFault = interceptors;
    }

    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public void setMtomEnabled(boolean mtomEnabled) {
        this.mtomEnabled = mtomEnabled;
    }

    public boolean isMtomEnabled() {
        return mtomEnabled;
    }

    public void process(Exchange exchange) {
        if (exchange.getProperty(ServiceEndpoint.class) == null) {
            exchange.setProperty(ServiceEndpoint.class, this);
        }
        queue.offer(exchange);
    }

    public DocumentFragment getAsReference(QName operationName) {
        return null;  
    }

    public QName[] getInterfaces() {
        return new QName[0];
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Queue<Exchange> getQueue() {
        return queue;
    }

    public void setQueue(Queue<Exchange> queue) {
        this.queue = queue;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

	public void setNmr(NMR nmr) {
		this.nmr = nmr;
	}

	public NMR getNmr() {
		return nmr;
	}

}
