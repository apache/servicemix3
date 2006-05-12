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
 * Represents a flow which joins on the completion of a collection of child
 * flows.
 * 
 * @version $Revision: $
 */
public class JoinAll extends JoinSupport {

    private boolean failFast;

    public JoinAll() {
        super();
    }

    public JoinAll(Flow... flows) {
        super(flows);
    }

    public JoinAll(List<Flow> flows) {
        super(flows);
    }

    public boolean isFailFast() {
        return failFast;
    }

    /**
     * If fail fast mode is enabled then this flow fails as soon as a child flow
     * fails. The default is to wait for all the child flows to complete
     * irrespective of whether they stop succesfully or fail before completing
     * this flow
     */
    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    /**
     * Decide whether or not we are done based on the number of children, the
     * number of child flows stopped and the number of failed flows
     */
    protected void onChildStateChange(int childCount, int stoppedCount, int failedCount) {
        if (failFast && failedCount > 0) {
            fail("" + failedCount + " child workflows have failed");
        }
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
