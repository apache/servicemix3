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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.beanflow.Activity.Transitions;

import java.util.Timer;

import junit.framework.TestCase;

/**
 * 
 * @version $Revision: $
 */
public abstract class FlowTestSupport extends TestCase {

    private static final Log log = LogFactory.getLog(FlowTestSupport.class);

    protected Timer timer = new Timer();
    protected long timeout = 500L;

    protected void assertFlowStarted(Activity flow) throws Exception {
        assertNotFailed(flow);
        assertEquals("Transition", Transitions.Started, flow.getState().get());
    }

    protected void assertFlowStopped(Activity flow) throws Exception {
        assertNotFailed(flow);
        assertEquals("Transition", Transitions.Stopped, flow.getState().get());

        assertTrue("Flow should be stopped but is: " + flow.getState().get(), flow.isStopped());
        assertTrue("Flow should not have failed", !flow.isFailed());
    }

    protected void assertNotFailed(Activity flow) throws Exception {
        Throwable failedException = flow.getFailedException();
        if (failedException != null) {
            if (failedException instanceof Exception) {
                throw (Exception) failedException;
            }
            else {
                throw new RuntimeException(failedException);
            }
        }
    }

    protected void assertFlowFailed(Activity flow) {
        assertEquals("Transition", Transitions.Failed, flow.getState().get());

        assertTrue("Flow should be stopped but is: " + flow.getState().get(), flow.isStopped());
        assertTrue("Flow should have failed", flow.isFailed());

        log.info("The flow failed due to: " + flow.getFailedReason());
    }

    protected void startFlow(Activity flow, long timeout) throws Exception {
        assertTrue("flow should not be stopped", !flow.isStopped());
        assertTrue("flow should not have failed", !flow.isFailed());
        assertEquals("Transition", Transitions.Initialised, flow.getState().get());

        flow.startWithTimeout(timer, timeout);
        assertFlowStarted(flow);
    }

}
