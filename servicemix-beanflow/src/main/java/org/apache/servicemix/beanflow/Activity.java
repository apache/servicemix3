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

import java.util.Timer;

/**
 * Represents an activity (step) in a workflow written typically using regular
 * Java code. An activity typically monitors various {@link State} objects and
 * takes action when things change. Activities are designed to be thread safe
 * and are intended for use in highly concurrent or distributed applications so
 * that the state can be changed from any thread. <br>
 * The Processing of notifications of state changes should generally be quick;
 * if lots of work is required when some state changes it is advisable to use a
 * thread pool to do the work.
 * 
 * @version $Revision: $
 */
public interface Activity {

    /**
     * The core transition states of the activity
     * 
     * @version $Revision: 1.1 $
     */
    public enum Transitions {
        Initialised, Starting, Started, Stopping, Stopped, Failed
    };

    /**
     * Starts the activity. Once it is started it can take an arbitrary amount
     * of time to complete. The execution of an activity is usually asynchronous
     * in nature (though its not mandatory)
     */
    public void start();

    /**
     * For activities that support timeout based operation this helper method
     * starts the activity and registers the timeout
     */
    public void startWithTimeout(Timer timer, long timeout);

    /**
     * Stops the activity, setting the status to {@link Stopped}
     */
    public void stop();

    /**
     * Stops the activity with a failed state, giving the reason for the failure
     */
    public void fail(String reason);

    /**
     * Returns the current running state of this activity
     */
    public State<Transitions> getState();

    /**
     * Returns true if the activity has stopped running either successfully or
     * if it failed
     */
    public boolean isStopped();

    /**
     * Returns true if the activity has failed to complete succesfully
     */
    public boolean isFailed();

    /**
     * If this activity has failed then return a reason for the failure
     */
    public String getFailedReason();

    /**
     * Returns the exception which caused the failure
     */
    public Throwable getFailedException();

    /**
     * A helper method to add a task to fire when the activity is completed
     */
    public void onStop(Runnable runnable);

    /**
     * A helper method to add a task to fire when the activity fails
     */
    public void onFailure(Runnable runnable);

    /**
     * A helper method to block the calling thread until the activity completes. Behaves
     * similar to {@link Thread#join()}
     */
    public void join();
}