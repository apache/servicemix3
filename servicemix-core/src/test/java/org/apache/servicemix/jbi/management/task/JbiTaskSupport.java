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
package org.apache.servicemix.jbi.management.task;

import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.management.task.JbiTask;

import junit.framework.TestCase;


/**
 *
 * JbiTaskTest
 * @version $Revision$
 */
public abstract class JbiTaskSupport extends TestCase {
    protected JBIContainer container = new JBIContainer();
    JbiTask remoteConnection;
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        container.setCreateMBeanServer(true);
        container.init();
        container.start();
        
        // Need to sleep as the jmx remote connector 
        // is started in another thread
        Thread.sleep(10000);
        
        remoteConnection = new JbiTask(){};
        remoteConnection.init();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        remoteConnection.close();
        container.shutDown();
    }

    
}
