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
package org.apache.servicemix.jbi.container;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.JBIException;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.framework.Registry;
import org.apache.servicemix.tck.ReceiverComponent;

/**
 * Test cases for {@link JBIContainer}
 */
public class JBIContainerTest extends TestCase {
    
    private static final long SHUTDOWN_DELAY = 10000;
    private static final long FORCE_SHUTDOWN_DELAY = SHUTDOWN_DELAY / 10;

    public void testForceShutdown() throws Exception {
        final AtomicBoolean forcedShutdown = new AtomicBoolean(false);
        final JBIContainer container = new JBIContainer() {
            @Override
            protected void forceShutdown(Exception e) {
                forcedShutdown.set(true);
                super.forceShutdown(e);
            }
        };
        container.registry = new Registry() {
            @Override
            public void shutDown() throws JBIException {
                try {
                    // let's simulate pending exchanges by slowing down the shutdown of the registry  
                    Thread.sleep(SHUTDOWN_DELAY);
                } catch (InterruptedException e) {
                    // ignore it
                }
                super.shutDown();
            }
        };
        container.setForceShutdown(FORCE_SHUTDOWN_DELAY);
        container.init();
        container.start();

        long start = System.currentTimeMillis();
        
        // now let's shutdown the container and await the termination
        container.shutDown();
        assertTrue("Container should have used forced shutdown", forcedShutdown.get());
        assertTrue("Should have taken less than the shutdown delay (" + SHUTDOWN_DELAY  + "ms)", 
                   System.currentTimeMillis() - start < SHUTDOWN_DELAY);
    }
    
    public void testForceShutdownWithPendingSyncExchanges() throws Exception {
        final JBIContainer container = new JBIContainer();
        container.setForceShutdown(FORCE_SHUTDOWN_DELAY);
        container.init();
        container.start();

        ActivationSpec spec = new ActivationSpec("receiver", new ReceiverComponent() {
            public void stop() throws JBIException {
                try {
                    // this component will now wait before shutting down
                    // (i.e. simulate component waiting for pending exchanges or a process to end)
                    Thread.sleep(SHUTDOWN_DELAY);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                super.stop();
            }
        });
        spec.setService(new QName("urn:test", "receiver"));
        container.activateComponent(spec);
        
        long start = System.currentTimeMillis();
        
        // now let's shutdown the container and await the termination
        container.shutDown();
        long delay = System.currentTimeMillis() - start;
        assertTrue("Should have taken less than " + delay  + "ms (estimated ca. " + FORCE_SHUTDOWN_DELAY + "ms)",
                   delay < SHUTDOWN_DELAY);
    }
    
}
