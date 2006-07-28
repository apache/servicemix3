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

import java.util.List;

/**
 * A activity which completes when quorum of the child activities are completed
 * successfully (or there are too many child activities failed to achieve
 * quorum).
 * 
 * @version $Revision: $
 */
public class JoinQuorum extends JoinSupport {

    public JoinQuorum() {
        super();

    }

    public JoinQuorum(Activity... activities) {
        super(activities);
    }

    public JoinQuorum(List<Activity> activities) {
        super(activities);
    }

    protected void onChildStateChange(int childCount, int stoppedCount, int failedCount) {
        int quorum = calculateQuorum(childCount);
        int successes = stoppedCount - failedCount;
        if (successes >= quorum) {
            stop();
        }
        else {
            if (failedCount >= quorum) {
                fail("Too many child activities failed: " + failedCount);
            }
        }
    }

    protected int calculateQuorum(int childCount) {
        return (childCount / 2) + 1;
    }
}
