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
public class JoinAllTest extends FlowTestSupport {

    protected Flow child1 = new TimeoutFlow();
    protected Flow child2 = new TimeoutFlow();
    protected Flow child3 = new TimeoutFlow();

    public void testJoinAllWhenEachChildFlowCompletes() throws Exception {
        // START SNIPPET: example
        // lets create a join on a number of child flows completing
        JoinAll flow = new JoinAll(child1, child2, child3);
        flow.startWithTimeout(timer, timeout);

        // now lets test the flow
        child1.stop();
        assertFlowStarted(flow);

        child2.stop();
        assertFlowStarted(flow);

        child3.stop();
        assertFlowStopped(flow);
        // END SNIPPET: example
    }

    public void testJoinAllTerminatesWhenAClientFails() throws Exception {
        JoinAll flow = new JoinAll(child1, child2, child3);
        startFlow(flow, timeout);

        child3.fail("Test case error simulation");
        assertFlowStarted(flow);

        child2.stop();
        assertFlowStarted(flow);

        child1.stop();
        assertFlowFailed(flow);
    }
    
    public void testJoinAllTerminatesAsSoonAsOneChildFails() throws Exception {
        JoinAll flow = new JoinAll(child1, child2, child3);
        flow.setFailFast(true);
        startFlow(flow, timeout);
        
        child1.fail("Test case error simulation");
        assertFlowFailed(flow);
    }

    public void testJoinAllFailsIfChildrenDoNotCompleteInTime() throws Exception {
        JoinAll flow = new JoinAll(child1, child2, child3);
        startFlow(flow, timeout);

        child1.stop();
        assertFlowStarted(flow);

        child2.stop();
        assertFlowStarted(flow);

        // lets force a timeout failure
        Thread.sleep(timeout * 2);
        
        assertFlowFailed(flow);
        
        // lets check that completing the final child flow keeps the join failed
        child3.stop();
        assertFlowFailed(flow);
    }
}
