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

import java.util.List;

/**
 * Represents a activity which joins on the completion of a collection of child
 * activities.
 * 
 * @version $Revision: $
 */
public class JoinAll extends JoinSupport {

    private boolean failFast;

    public JoinAll() {
        super();
    }

    public JoinAll(Activity... activities) {
        super(activities);
    }

    public JoinAll(List<Activity> activities) {
        super(activities);
    }

    public boolean isFailFast() {
        return failFast;
    }

    /**
     * If fail fast mode is enabled then this activity fails as soon as a child
     * activity fails. The default is to wait for all the child activities to
     * complete irrespective of whether they stop succesfully or fail before
     * completing this activity
     */
    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    /**
     * Decide whether or not we are done based on the number of children, the
     * number of child activities stopped and the number of failed activities
     */
    protected void onChildStateChange(int childCount, int stoppedCount, int failedCount) {
        //System.out.println("This: " + this + " child: " + childCount + " stopped: " + stoppedCount + " failed: " + failedCount);
        if (failFast && failedCount > 0) {
            fail("" + failedCount + " child workactivities have failed");
        }
        if (childCount <= stoppedCount) {
            if (failedCount > 0) {
                fail("" + failedCount + " child workactivities have failed");
            }
            else {
                stop();
            }
        }
    }

}
