/** 
 * <a href="http://servicemix.org">ServiceMix: The open source ESB</a> 
 * 
 * Copyright 2005 RAJD Consultancy Ltd
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


package org.servicemix.jbi.management.task;

import org.servicemix.jbi.container.JBIContainer;

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
