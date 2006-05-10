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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a flow which joins on the success of a number of child flows.
 * 
 * @version $Revision: $
 */
public class JoinAll extends TimeoutFlow {

    private List<Flow> children = new ArrayList<Flow>();

    public JoinAll() {
    }

    public JoinAll(List<Flow> flows) {
        for (Flow flow : flows) {
            addChildFlow(flow);
        }
    }

    public JoinAll(Flow... flows) {
        for (Flow flow : flows) {
            addChildFlow(flow);
        }
    }

    public void addChildFlow(Flow child) {
        synchronized (children) {
            child.getState().addRunnable(this);
            children.add(child);
        }
    }

    public void removeChildFlow(Flow child) {
        synchronized (children) {
            child.getState().removeRunnable(this);
            children.remove(child);
        }
    }

    @Override
    public void run() {
        if (isStopped()) {
            return;
        }
        int childCount = 0;
        int stoppedCount = 0;
        int failedCount = 0;
        synchronized (children) {
            childCount = children.size();
            for (Flow child : children) {
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
        
        // lets make sure that the child flows are started properly
        synchronized (children) {
            for (Flow child : children) {
                child.start();
            }
        }
    }

    /**
     * Decide whether or not we are done based on the number of children, the
     * number of child flows stopped and the number of failed flows
     */
    protected void onChildStateChange(int childCount, int stoppedCount, int failedCount) {
        if (childCount <= stoppedCount) {
            if (failedCount > 0) {
                fail("" + failedCount + " child workflows have failed");
            }
            else {
                stop();
            }
        }
    }

}
