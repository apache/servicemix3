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
package org.apache.servicemix.http.endpoints;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.http.jetty.SmxHttpExchange;
import org.apache.servicemix.soap.api.InterceptorChain;
import org.apache.servicemix.soap.api.InterceptorProvider.Phase;
import org.apache.servicemix.soap.api.Message;
import org.apache.servicemix.soap.api.Policy;
import org.apache.servicemix.soap.api.model.Binding;
import org.apache.servicemix.soap.bindings.soap.SoapConstants;
import org.apache.servicemix.soap.interceptors.jbi.JbiConstants;
import org.apache.servicemix.soap.interceptors.xml.StaxInInterceptor;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.HttpMethods;

/**
 * 
 * @author gnodet
 * @since 3.2
 */
public class HttpSoapProviderMarshaler implements HttpProviderMarshaler {

    private static final Set<String> DEFAULT_HEADER_BLACKLIST =
            new HashSet<String>(
                Arrays.asList(HttpHeaders.AUTHORIZATION,
                              HttpHeaders.EXPECT,
                              HttpHeaders.FORWARDED,
                              HttpHeaders.FROM,
                              HttpHeaders.HOST,
                              HttpHeaders.CONTENT_ENCODING,
                              HttpHeaders.CONTENT_TYPE));

    private Binding<?> binding;
    private boolean useJbiWrapper = true;
    private Policy[] policies;
    private String baseUrl;
    private String soapAction;
    private Set<String> headerBlackList = DEFAULT_HEADER_BLACKLIST;

    public Binding<?> getBinding() {
        return binding;
    }

    public void setBinding(Binding<?> binding) {
        this.binding = binding;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isUseJbiWrapper() {
        return useJbiWrapper;
    }

    public void setUseJbiWrapper(boolean useJbiWrapper) {
        this.useJbiWrapper = useJbiWrapper;
    }

    public Policy[] getPolicies() {
        return policies;
    }

    public void setPolicies(Policy[] policies) {
        this.policies = policies;
    }
    
    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }
    
    public String getSoapAction() {
        return soapAction;
    }
    
    public Set<String> getHeaderBlackList() {
        return headerBlackList;
    }

    /**
     * Specifies a list of headers to not include in the HTTP request.
     * 
     * @param headerBlackList list of headers to not include in the HTTP request
     */
    public void setHeaderBlackList(Set<String> headerBlackList) {
        this.headerBlackList = headerBlackList;
    }

    public void createRequest(final MessageExchange exchange, 
                              final NormalizedMessage inMsg, 
                              final SmxHttpExchange httpExchange) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Message msg = binding.createMessage();
        msg.put(JbiConstants.USE_JBI_WRAPPER, useJbiWrapper);
        msg.setContent(MessageExchange.class, exchange);
        msg.setContent(NormalizedMessage.class, inMsg);
        msg.setContent(OutputStream.class, baos);
        exchange.setProperty(Message.class.getName(), msg);

        InterceptorChain phaseOut = getChain(Phase.ClientOut);
        phaseOut.doIntercept(msg);
        httpExchange.setMethod(HttpMethods.POST);
        httpExchange.setURL(baseUrl);
        httpExchange.setRequestContent(new ByteArrayBuffer(baos.toByteArray()));
        
        for (String header : msg.getTransportHeaders().keySet()) {
            if (!isBlackListed(header)) {
                httpExchange.setRequestHeader(header, msg.getTransportHeaders().get(header));
            }
        }
        if (soapAction != null) {
            httpExchange.setRequestHeader(SoapConstants.SOAP_ACTION_HEADER, soapAction);
        }
        /*
        httpExchange.setRequestEntity(new Entity() {
            public void write(OutputStream os, Writer w) throws IOException {
                // TODO: handle http headers: Content-Type, ... 
            }
        });
        */        
        // TODO: use streaming when appropriate (?)
    }

    public void handleResponse(MessageExchange exchange, SmxHttpExchange httpExchange) throws Exception {
        Message req = (Message) exchange.getProperty(Message.class.getName());
        exchange.setProperty(Message.class.getName(), null);
        Message msg = binding.createMessage(req);
        msg.put(JbiConstants.USE_JBI_WRAPPER, useJbiWrapper);
        msg.setContent(MessageExchange.class, exchange);
        msg.setContent(InputStream.class, new ByteArrayInputStream(httpExchange.getResponseData()));
        msg.put(StaxInInterceptor.ENCODING, httpExchange.getResponseEncoding());
        InterceptorChain phaseOut = getChain(Phase.ClientIn);
        phaseOut.doIntercept(msg);
        // TODO: Retrieve headers ? 
    }

    protected InterceptorChain getChain(Phase phase) {
        InterceptorChain chain = binding.getInterceptorChain(phase);
        if (policies != null) {
            for (int i = 0; i < policies.length; i++) {
                chain.add(policies[i].getInterceptors(phase));
            }
        }
        return chain;
    }

    /**
     * checks if a property is on black list
     *
     * @param name the property
     * @return true if on black list
     */
    protected boolean isBlackListed(String name) {
        return this.headerBlackList != null && this.headerBlackList.contains(name);
    }

}
