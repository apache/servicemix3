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

import java.util.TimerTask;

/**
 * A really simple flow which if the flow is not started within a specific
 * timeout period then the flow fails.
 * 
 * @version $Revision: $
 */
public class TimeoutFlow extends FlowSupport {

    private State<Boolean> timedOut = new DefaultState<Boolean>(Boolean.FALSE);
    private TimerTask timeoutTask;

    /**
     * Called when the timeout event occurs
     */
    public void onTimedOut() {
        // ignore any timeout events after the workflow is stopped
        if (!isStopped()) {
            timedOut.set(Boolean.TRUE);
        }
    }

    public void run() {
        if (!isStopped()) {
            if (timedOut.get().booleanValue()) {
                fail();
            }
        }
    }

    /**
     * Returns true if the flow timed out
     */
    public boolean isTimedOut() {
        return timedOut.get();
    }

    /**
     * Returns a timer task for the timeout event
     */
    public TimerTask getTimeoutTask() {
        if (timeoutTask == null) {
            timeoutTask = new TimerTask() {
                public void run() {
                    onTimedOut();
                }
            };
        }
        return timeoutTask;
    }
}
