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

import java.util.ArrayList;
import java.util.List;

/**
 * A useful base class for a activity which joins on the success of a collection
 * of child activities.
 * 
 * @version $Revision: $
 */
public abstract class JoinSupport extends TimeoutActivity {

    private List<Activity> children = new ArrayList<Activity>();

    public JoinSupport() {
    }

    public JoinSupport(List<Activity> activities) {
        for (Activity activity : activities) {
            fork(activity);
        }
    }

    public JoinSupport(Activity... activities) {
        for (Activity activity : activities) {
            fork(activity);
        }
    }

    public void fork(Activity child) {
        synchronized (children) {
            child.getState().addRunnable(this);
            children.add(child);
            child.start();
        }
    }

    public void cancelFork(Activity child) {
        synchronized (children) {
            child.getState().removeRunnable(this);
            children.remove(child);
            child.stop();
        }
    }

    @Override
    protected void onValidStateChange() {
        int childCount = 0;
        int stoppedCount = 0;
        int failedCount = 0;
        synchronized (children) {
            childCount = children.size();
            for (Activity child : children) {
                if (child.isStopped()) {
                    stoppedCount++;
                    if (child.isFailed()) {
                        failedCount++;
                    }
                }
            }
        }
        onChildStateChange(childCount, stoppedCount, failedCount);
    }

    @Override
    protected void doStart() {
        super.doStart();

        // lets make sure that the child activities are started properly
        synchronized (children) {
            for (Activity child : children) {
                child.start();
            }
        }
    }

    /**
     * Decide whether or not we are done based on the number of children, the
     * number of child activities stopped and the number of failed activities
     */
    protected abstract void onChildStateChange(int childCount, int stoppedCount, int failedCount);

}
