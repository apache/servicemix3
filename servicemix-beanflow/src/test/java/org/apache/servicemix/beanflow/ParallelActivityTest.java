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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 
 * @version $Revision: $
 */
public class ParallelActivityTest extends ActivityTestSupport {

    protected Executor executor = Executors.newFixedThreadPool(10);

    @SuppressWarnings("unchecked")
    public void testParallelWorkflow() throws Exception {

        // START SNIPPET: example
        ExampleParallelBean parallelBean = new ExampleParallelBean();
        ParallelActivity activity = ParallelActivity.newParallelMethodActivity(executor, parallelBean);
        activity.startWithTimeout(timer, 20000);
        // END SNIPPET: example

        activity.join();
        parallelBean.assertCompleted();
        assertStopped(activity);
    }
}
