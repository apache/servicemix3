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
package org.apache.servicemix.beanflow;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A really simple flow which if the flow is not started within a specific
 * timeout period then the flow fails.
 * 
 * @version $Revision: $
 */
public class TimeoutActivity extends AbstractActivity {

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
                fail("Timed out");
            }
            else {
                onValidStateChange();
            }
        }
    }

    /**
     * Returns true if the flow timed out
     */
    public boolean isTimedOut() {
        return timedOut.get();
    }

    public void startWithTimeout(Timer timer, long timeout) {
        scheduleTimeout(timer, timeout);
        start();
    }

    /**
     * Schedules the flow to timeout at the given value
     */
    public void scheduleTimeout(Timer timer, long timeout) {
        if (timeout > 0) {
            timer.schedule(getTimeoutTask(), timeout);
        }
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

    /**
     * A hook so that derived classes can ignore whether the flow is started or
     * timed out and instead focus on the other state
     */
    protected void onValidStateChange() {
    }
}
