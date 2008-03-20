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

import org.apache.servicemix.beanflow.util.ActivityTestSupport;

/**
 * 
 * @version $Revision: $
 */
public class JoinQuorumTest extends ActivityTestSupport {

    protected Activity child1 = new TimeoutActivity();
    protected Activity child2 = new TimeoutActivity();
    protected Activity child3 = new TimeoutActivity();

    public void testJoinAllWhenEachChildFlowCompletes() throws Exception {
        JoinQuorum flow = new JoinQuorum(child1, child2, child3);
        startActivity(flow, timeout);

        child1.stop();
        assertStarted(flow);

        child2.stop();
        assertStopped(flow);

        // lets check things are still fine when the quorum is complete
        child3.stop();
        assertStopped(flow);
    }

    public void testJoinQuorumTerminatesWhenTooManyClientsFail() throws Exception {
        JoinQuorum flow = new JoinQuorum(child1, child2, child3);
        startActivity(flow, timeout);

        child3.fail("Test case error simulation");
        assertStarted(flow);

        child2.fail("Test case error simulation");
        assertFailed(flow);
    }

    public void testJoinQuorumFailsIfChildrenDoNotCompleteInTime() throws Exception {
        JoinQuorum flow = new JoinQuorum(child1, child2, child3);
        startActivity(flow, timeout);

        child1.stop();
        assertStarted(flow);

        // lets force a timeout failure
        Thread.sleep(timeout * 2);

        assertFailed(flow);

        // lets check that completing the final child flow keeps the join failed
        child2.stop();
        assertFailed(flow);

        child3.stop();
        assertFailed(flow);
    }
}
