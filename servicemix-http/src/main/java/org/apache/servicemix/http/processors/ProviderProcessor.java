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
package org.apache.servicemix.http.processors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.common.ExchangeProcessor;
import org.apache.servicemix.http.HttpEndpoint;
import org.apache.servicemix.http.HttpLifeCycle;
import org.apache.servicemix.soap.marshalers.JBIMarshaler;
import org.apache.servicemix.soap.marshalers.SoapMarshaler;
import org.apache.servicemix.soap.marshalers.SoapMessage;
import org.apache.servicemix.soap.marshalers.SoapReader;
import org.apache.servicemix.soap.marshalers.SoapWriter;

/**
 * 
 * @author Guillaume Nodet
 * @version $Revision: 370186 $
 * @since 3.0
 */
public class ProviderProcessor implements ExchangeProcessor {

    protected HttpEndpoint endpoint;
    protected HostConfiguration host;
    protected SoapMarshaler soapMarshaler;
    protected JBIMarshaler jbiMarshaler;
    protected DeliveryChannel channel;
    private String relUri;
    
    public ProviderProcessor(HttpEndpoint endpoint) {
        this.endpoint = endpoint;
        this.soapMarshaler = new SoapMarshaler(endpoint.isSoap());
        if (endpoint.isSoap() && "1.1".equals(endpoint.getSoapVersion())) {
            this.soapMarshaler.setSoapUri(SoapMarshaler.SOAP_11_URI);
        }
        this.jbiMarshaler = new JBIMarshaler();
        java.net.URI uri = java.net.URI.create(endpoint.getLocationURI());
        relUri = uri.getPath();
        if (!relUri.startsWith("/")) {
            relUri = "/" + relUri;
        }
        if (uri.getQuery() != null) {
            relUri += "?" + uri.getQuery();
        }
        if (uri.getFragment() != null) {
            relUri += "#" + uri.getFragment();
        }
    }

    public void process(MessageExchange exchange) throws Exception {
        if (exchange.getStatus() == ExchangeStatus.DONE) {
            return;
        } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
            return;
        }
        PostMethod method = new PostMethod(relUri);
        SoapMessage soapMessage = new SoapMessage();
        NormalizedMessage nm = exchange.getMessage("in");
        jbiMarshaler.fromNMS(soapMessage, nm);
        SoapWriter writer = soapMarshaler.createWriter(soapMessage);
        Map headers = (Map) nm.getProperty(JbiConstants.PROTOCOL_HEADERS);
        if (headers != null) {
            for (Iterator it = headers.keySet().iterator(); it.hasNext();) {
                String name = (String) it.next();
                String value = (String) headers.get(name);
                method.addRequestHeader(name, value);
            }
        }
        RequestEntity entity = writeMessage(writer);
        method.addRequestHeader("Content-Type", entity.getContentType());
        if (entity.getContentLength() < 0) {
            method.removeRequestHeader("Content-Length");
        } else {
            method.setRequestHeader("Content-Length", Long.toString(entity.getContentLength()));
        }
        if (endpoint.isSoap() && method.getRequestHeader("SOAPAction") == null) {
            method.setRequestHeader("SOAPAction", "\"\"");
        }
        method.setRequestEntity(entity);
        try {
            int response = getClient().executeMethod(host, method);
            if (response != HttpStatus.SC_OK) {
                if (exchange instanceof InOnly == false) {
                    SoapReader reader = soapMarshaler.createReader();
                    Header contentType = method.getResponseHeader("Content-Type");
                    soapMessage = reader.read(method.getResponseBodyAsStream(), 
                                              contentType != null ? contentType.getValue() : null);
                    Fault fault = exchange.createFault();
                    fault.setProperty(JbiConstants.PROTOCOL_HEADERS, getHeaders(method));
                    jbiMarshaler.toNMS(fault, soapMessage);
                    exchange.setFault(fault);
                    channel.send(exchange);
                    return;
                } else {
                    throw new Exception("Invalid status response: " + response);
                }
            }
            if (exchange instanceof InOut) {
                NormalizedMessage msg = exchange.createMessage();
                SoapReader reader = soapMarshaler.createReader();
                Header contentType = method.getResponseHeader("Content-Type");
                soapMessage = reader.read(method.getResponseBodyAsStream(), 
                                          contentType != null ? contentType.getValue() : null);
                msg.setProperty(JbiConstants.PROTOCOL_HEADERS, getHeaders(method));
                jbiMarshaler.toNMS(msg, soapMessage);
                ((InOut) exchange).setOutMessage(msg);
                channel.sendSync(exchange);
            } else if (exchange instanceof InOptionalOut) {
                if (method.getResponseContentLength() == 0) {
                    exchange.setStatus(ExchangeStatus.DONE);
                    channel.send(exchange);
                } else {
                    NormalizedMessage msg = exchange.createMessage();
                    SoapReader reader = soapMarshaler.createReader();
                    soapMessage = reader.read(method.getResponseBodyAsStream(), 
                                              method.getResponseHeader("Content-Type").getValue());
                    msg.setProperty(JbiConstants.PROTOCOL_HEADERS, getHeaders(method));
                    jbiMarshaler.toNMS(msg, soapMessage);
                    ((InOptionalOut) exchange).setOutMessage(msg);
                    channel.sendSync(exchange);
                }
            } else {
                exchange.setStatus(ExchangeStatus.DONE);
                channel.send(exchange);
            }
        } finally {
            method.releaseConnection();
        }
    }

    public void start() throws Exception {
        URI uri = new URI(endpoint.getLocationURI(), false);
        if (uri.getScheme().equals("https")) {
            ProtocolSocketFactory sf = new CommonsHttpSSLSocketFactory(endpoint.getSsl());
            Protocol protocol = new Protocol("https", sf, 443);
            HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), protocol);
            this.host = new HostConfiguration();
            this.host.setHost(host);
        } else {
            this.host = new HostConfiguration();
            this.host.setHost(uri.getHost(), uri.getPort());
        }
        channel = endpoint.getServiceUnit().getComponent().getComponentContext().getDeliveryChannel();
    }

    public void stop() throws Exception {
    }

    protected Map getHeaders(HttpServletRequest request) {
        Map headers = new HashMap();
        Enumeration enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            String value = request.getHeader(name);
            headers.put(name, value);
        }
        return headers;
    }

    protected Map getHeaders(HttpMethod method) {
        Map headers = new HashMap();
        Header[] h = method.getResponseHeaders();
        for (int i = 0; i < h.length; i++) {
            headers.put(h[i].getName(), h[i].getValue());
        }
        return headers;
    }
	
    protected RequestEntity writeMessage(SoapWriter writer) throws Exception {
        HttpLifeCycle lf = (HttpLifeCycle) endpoint.getServiceUnit().getComponent().getLifeCycle();
        if (lf.getConfiguration().isStreamingEnabled()) {
            return new StreamingRequestEntity(writer);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writer.write(baos);
            return new ByteArrayRequestEntity(baos.toByteArray(), writer.getContentType());
        }
    }

    protected HttpClient getClient() {
        HttpLifeCycle lf =  (HttpLifeCycle) endpoint.getServiceUnit().getComponent().getLifeCycle();
        return lf.getClient();
    }

    public static class StreamingRequestEntity implements RequestEntity {

        private SoapWriter writer;
        
        public StreamingRequestEntity(SoapWriter writer) {
            this.writer = writer;
        }
        
        public boolean isRepeatable() {
            return false;
        }

        public void writeRequest(OutputStream out) throws IOException {
            try {
                writer.write(out);
                out.flush();
            } catch (Exception e) {
                throw (IOException) new IOException("Could not write request").initCause(e);
            }
        }

        public long getContentLength() {
            // not known so we send negative value
            return -1;
        }

        public String getContentType() {
            return writer.getContentType();
        }
        
    }
}
