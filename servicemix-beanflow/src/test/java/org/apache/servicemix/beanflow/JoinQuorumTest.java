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

/**
 * 
 * @version $Revision: $
 */
public class JoinQuorumTest extends FlowTestSupport {

    protected Flow child1 = new TimeoutFlow();
    protected Flow child2 = new TimeoutFlow();
    protected Flow child3 = new TimeoutFlow();

    public void testJoinAllWhenEachChildFlowCompletes() throws Exception {
        JoinQuorum flow = new JoinQuorum(child1, child2, child3);
        startFlow(flow, timeout);

        child1.stop();
        assertFlowStarted(flow);

        child2.stop();
        assertFlowStopped(flow);
        
        // lets check things are still fine when the quorum is complete
        child3.stop();
        assertFlowStopped(flow);
    }

    public void testJoinQuorumTerminatesWhenTooManyClientsFail() throws Exception {
        JoinQuorum flow = new JoinQuorum(child1, child2, child3);
        startFlow(flow, timeout);

        child3.fail("Test case error simulation");
        assertFlowStarted(flow);
        
        child2.fail("Test case error simulation");
        assertFlowFailed(flow);
    }

    public void testJoinQuorumFailsIfChildrenDoNotCompleteInTime() throws Exception {
        JoinQuorum flow = new JoinQuorum(child1, child2, child3);
        startFlow(flow, timeout);

        child1.stop();
        assertFlowStarted(flow);

        // lets force a timeout failure
        Thread.sleep(timeout * 2);

        assertFlowFailed(flow);

        // lets check that completing the final child flow keeps the join failed
        child2.stop();
        assertFlowFailed(flow);

        child3.stop();
        assertFlowFailed(flow);
    }
}
