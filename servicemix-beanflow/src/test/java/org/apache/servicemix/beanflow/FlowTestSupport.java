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

import junit.framework.TestCase;

/**
 * 
 * @version $Revision: $
 */
public abstract class FlowTestSupport extends TestCase {

    protected Timer timer = new Timer();
    protected long timeout = 500L;

    protected void assertFlowStopped(Flow flow) {
        assertEquals("Transition", Transitions.Stopped, flow.getState().get());

        assertTrue("Flow should be stopped but is: " + flow.getState().get(), flow.isStopped());
        assertTrue("Flow should not have failed", !flow.isFailed());
    }

    protected void assertFlowFailed(Flow flow) {
        assertEquals("Transition", Transitions.Failed, flow.getState().get());

        assertTrue("Flow should be stopped but is: " + flow.getState().get(), flow.isStopped());
        assertTrue("Flow should have failed", flow.isFailed());

        System.out.println("The flow failed due to: " + flow.getFailedReason());
    }

    protected void startFlow(Flow flow, long timeout) {
        assertTrue("flow should not be stopped", !flow.isStopped());
        assertTrue("flow should not have failed", !flow.isFailed());
        assertEquals("Transition", Transitions.Initialised, flow.getState().get());

        flow.startWithTimeout(timer, timeout);
        assertFlowStarted(flow);
    }

    protected void assertFlowStarted(Flow flow) {
        assertEquals("Transition", Transitions.Started, flow.getState().get());
    }

}
