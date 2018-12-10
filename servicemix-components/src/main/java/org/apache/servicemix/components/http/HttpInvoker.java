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
package org.apache.servicemix.components.http;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.TransformComponentSupport;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * Performs HTTP client invocations on a remote HTTP site.
 *
 * @version $Revision$
 */
public class HttpInvoker extends TransformComponentSupport implements MessageExchangeListener {

    protected HttpClientMarshaler marshaler = new HttpClientMarshaler();
    protected MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
    protected HttpClient httpClient = new HttpClient(connectionManager);
    protected HostConfiguration hostConfiguration = new HostConfiguration();
    protected String url;
    protected boolean defaultInOut = true;

    public void stop() throws JBIException {
        super.stop();
        connectionManager.shutdown();
    }

    protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        PostMethod method = new PostMethod(url);
        try {
            marshaler.fromNMS(method, exchange, in);
            if (url != null) {
                hostConfiguration.setHost(new URI(url, false));
            }
            int response = httpClient.executeMethod(hostConfiguration, method);

            if (response != HttpStatus.SC_OK) {
                throw new InvalidStatusResponseException(response);
            }

            // now lets grab the output and set it on the out message
            if (defaultInOut) {
                marshaler.toNMS(out, method);
            }
            return defaultInOut;
        }
        catch (Exception e) {
            throw new MessagingException("Error executing http request", e);
        }
        finally {
            method.releaseConnection();
        }
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isDefaultInOut() {
        return defaultInOut;
    }

    public void setDefaultInOut(boolean defaultInOut) {
        this.defaultInOut = defaultInOut;
    }

    public HttpClientMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(HttpClientMarshaler marshaler) {
        this.marshaler = marshaler;
    }
}
