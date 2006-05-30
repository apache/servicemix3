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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An example workflow
 * 
 * @version $Revision: $
 */
// START SNIPPET: workflow
public class ExampleWorkflow extends Workflow<ExampleWorkflow.Step> {
    private static final Log log = LogFactory.getLog(ExampleWorkflow.class);

    private int loopCount;
    private long timeout = 500;
    private String userEmailAddress;

    public static enum Step {
        startStep, afterEnteredEmailStep, loopStep, waitForUserInputStep, forkStep, 
        aCompletedStep, abcCompletedStep, stop
    };

    public ExampleWorkflow() {
        super(Step.startStep);
    }

    // Workflow steps
    // -------------------------------------------------------------------------

    public void startStep() {
        // lets use an explicit goTo() to tell the workflow
        // which step to go to next; though we can just return Strings
        setNextStep(Step.loopStep);
    }

    // lets use the return value to specify the next step
    public Step loopStep() {
        if (++loopCount > 3) {
            return Step.waitForUserInputStep;
        }
        // lets keep looping
        return Step.loopStep;
    }

    public void waitForUserInputStep() {
        // we are going to park here until a user
        // enters a valid email address
        // so lets park the workflow engine
    }

    public Step afterEnteredEmailStep() {
        // we are going to park here until a user
        // enters a valid email address
        log.info("User entered email address: " + userEmailAddress);
        return Step.forkStep;
    }

    public void forkStep() {
        // lets fork some child flows
        TimeoutActivity a = new TimeoutActivity();
        TimeoutActivity b = new TimeoutActivity();
        TimeoutActivity c = new TimeoutActivity();

        log.info("Forking off processes a, b, c");
        fork(a, b, c);

        // now lets add some joins
        joinAll(Step.aCompletedStep, timeout, a);
        joinAll(Step.abcCompletedStep, timeout, a, b, c);
    }

    public void aCompletedStep() {
        log.info("child flow A completed!");
    }

    public Step abcCompletedStep() {
        log.info("child flows A, B and C completed!");

        // we are completely done now
        return Step.stop;
    }

    // External events
    // -------------------------------------------------------------------------
    public void userEntered(String emailAddress) {
        if (emailAddress != null && emailAddress.indexOf("@") > 0) {
            this.userEmailAddress = emailAddress;

            log.info("Lets re-start the suspended workflow");
            setNextStep(Step.afterEnteredEmailStep);
        }
    }
}
// END SNIPPET: workflow
