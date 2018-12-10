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
 * A simple example
 * 
 * @version $Revision: $
 */
// START SNIPPET: workflow
public class SimpleWorkflow extends Workflow<SimpleWorkflow.Step> {

    public static enum Step {
        stepA, stepB, stepC, stop
    };

    public SimpleWorkflow() {
        super(Step.stepA);
    }

    // Workflow steps
    // -------------------------------------------------------------------------
    public Step stepA() {
        return Step.stepB;
    }
    
    public Step stepB() {
        return Step.stepC;
    }
    
    public Step stepC() {
        return Step.stop;
    }
}
// END SNIPPET: workflow
