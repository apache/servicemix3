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

import org.apache.servicemix.beanflow.support.FindCallableMethods;

import java.util.concurrent.*;

/**
 * 
 * @version $Revision: $
 */
public class ParallelActivityTest extends FlowTestSupport {

    protected Executor executor = Executors.newFixedThreadPool(10);

    @SuppressWarnings("unchecked")
    public void test() throws Exception {
        ExampleParallelBean parallelBean = new ExampleParallelBean();
        FindCallableMethods factory = new FindCallableMethods(parallelBean);

        ParallelActivity activity = new ParallelActivity(new JoinAll(), executor, factory);
        activity.startWithTimeout(timer, -1);

        assertFlowStarted(activity);

        parallelBean.assertCompleted();
        
        // OK the latch may be completed but the 
        // join might not have completed yet
        Thread.sleep(5000);
        
        // TODO FIXME!
        //assertFlowStopped(activity);
    }

}
