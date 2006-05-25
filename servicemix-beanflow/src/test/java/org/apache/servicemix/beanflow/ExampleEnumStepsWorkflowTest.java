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

import org.apache.servicemix.beanflow.util.ActivityTestSupport;

/**
 * 
 * @version $Revision: $
 */
public class ExampleEnumStepsWorkflowTest extends ActivityTestSupport {

    public void testWorkflow() throws Exception {
        Workflow<ExampleEnumSteps> workflow = new Workflow<ExampleEnumSteps>(ExampleEnumSteps.class);
        workflow.start();

        Thread.sleep(2000);

        assertStopped(workflow);
    }
}
