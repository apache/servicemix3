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

import org.apache.servicemix.beanflow.Activity.Transitions;

/**
 * 
 * @version $Revision: $
 */
public class TimeoutFlowTest extends FlowTestSupport {
    protected TimeoutActivity flow = new TimeoutActivity();
    
    public void testFlowStopsSuccessfully() throws Exception {
        flow.getState().set(Transitions.Stopped);
        assertFlowStopped(flow);
        
        // lets sleep so that the timer can go off now to check we don't fail after we've stopped
        Thread.sleep(timeout  * 4);
        assertFlowStopped(flow);
    }

    public void testFlowTimesOutAndFails() throws Exception {
        Thread.sleep(timeout  * 4);
        assertFlowFailed(flow);
    }

    protected void setUp() throws Exception {
        startFlow(flow, timeout);
    }
}
