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
package org.apache.servicemix.beanflow.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.beanflow.Activity;
import org.apache.servicemix.beanflow.Activity.Transitions;

import java.util.Timer;

import junit.framework.TestCase;

/**
 * 
 * @version $Revision: $
 */
public abstract class ActivityTestSupport extends TestCase {

    private static final Log log = LogFactory.getLog(ActivityTestSupport.class);

    protected Timer timer = new Timer();
    protected long timeout = 500L;

    protected void assertStarted(Activity activity) throws Exception {
        assertNotFailed(activity);
        assertEquals("Transition", Transitions.Started, activity.getState().get());
    }

    protected void assertStopped(Activity activity) throws Exception {
        assertNotFailed(activity);
        assertEquals("Transition", Transitions.Stopped, activity.getState().get());

        assertTrue("Flow should be stopped but is: " + activity.getState().get(), activity.isStopped());
        assertTrue("Flow should not have failed", !activity.isFailed());
    }

    protected void assertNotFailed(Activity activity) throws Exception {
        assertTrue("Should not have failed: " + activity.getFailedReason() + " exception: "
                + activity.getFailedException(), !activity.isFailed());
        
        Throwable failedException = activity.getFailedException();
        if (failedException != null) {
            if (failedException instanceof Exception) {
                throw (Exception) failedException;
            }
            else {
                throw new RuntimeException(failedException);
            }
        }
    }

    protected void assertFailed(Activity activity) {
        assertEquals("Transition", Transitions.Failed, activity.getState().get());

        assertTrue("Flow should be stopped but is: " + activity.getState().get(), activity.isStopped());
        assertTrue("Flow should have failed", activity.isFailed());

        log.info("The activity failed due to: " + activity.getFailedReason());
    }

    protected void startActivity(Activity activity, long timeout) throws Exception {
        assertTrue("activity should not be stopped", !activity.isStopped());
        assertTrue("activity should not have failed", !activity.isFailed());
        assertEquals("Transition", Transitions.Initialised, activity.getState().get());

        activity.startWithTimeout(timer, timeout);
        assertStarted(activity);
    }

}
