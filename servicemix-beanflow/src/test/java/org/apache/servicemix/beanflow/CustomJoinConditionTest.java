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
public class CustomJoinConditionTest extends FlowTestSupport  {

    public void testCustomJoinCondition() throws Exception {
        // START SNIPPET: join
        // lets create some child workflows
        final Flow a = new TimeoutFlow();
        final Flow b = new TimeoutFlow();
        final Flow c = new TimeoutFlow();

        // lets create the flow with a custom join condition
        Flow flow = new JoinSupport(a, b, c) {
            @Override
            protected void onChildStateChange(int childCount, int stoppedCount, int failedCount) {
                
                if (a.isStopped() && (b.isStopped() || c.isStopped())) {
                    // lets stop the flow we're done
                    stop();
                }
            }
        };
        
        // lets start the flows
        flow.startWithTimeout(timer, timeout);
        
        // now lets test things behave properly
        assertFlowStarted(flow);

        a.stop();
        assertFlowStarted(flow);

        b.stop();
        assertFlowStopped(flow);
        // END SNIPPET: join
        
        // lets check things are still fine when c completes
        c.stop();
        assertFlowStopped(flow);
    }
}
