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
package org.apache.servicemix.beanflow;

import org.apache.servicemix.beanflow.Flow.Transitions;

import java.util.Timer;

/**
 * A simple proxy to an underlying activity making it easy to compose activities
 * together
 * 
 * @version $Revision: $
 */
public class ProxyActivity implements Flow {

    private Flow proxy;

    public ProxyActivity(Flow proxy) {
        this.proxy = proxy;
    }

    public void fail(String reason) {
        getProxy().fail(reason);
    }

    public String getFailedReason() {
        return getProxy().getFailedReason();
    }
    
    public Throwable getFailedException() {
        return getProxy().getFailedException();
    }

    public State<Transitions> getState() {
        return getProxy().getState();
    }

    public boolean isFailed() {
        return getProxy().isFailed();
    }

    public boolean isStopped() {
        return getProxy().isStopped();
    }

    public void start() {
        getProxy().start();
    }

    public void startWithTimeout(Timer timer, long timeout) {
        getProxy().startWithTimeout(timer, timeout);
    }

    public void stop() {
        getProxy().stop();
    }

    protected Flow getProxy() {
        return proxy;
    }

    protected void setProxy(Flow proxy) {
        this.proxy = proxy;
    }

}
