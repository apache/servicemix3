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
public class ExampleWorkflow extends Workflow {
    private static final Log log = LogFactory.getLog(ExampleWorkflow.class);

    private int loopCount;
    private long timeout = 500;
    private String userEmailAddress;

    // Workflow steps
    // -------------------------------------------------------------------------

    // This is the first step of a workflow by default
    // but you can start at any point by passing in a parameter
    // to the Workflow constructor
    public void startStep() {
        // lets use an explicit goTo() to tell the workflow
        // which step to go to next; though we can just return Strings
        goTo("loopStep");
    }

    // lets use the return value to specify the next step
    public String loopStep() {
        if (++loopCount > 3) {
            return "waitForUserInputStep";
        }
        // lets keep looping
        return "loopStep";
    }

    public void waitForUserInputStep() {
        // we are going to park here until a user
        // enters a valid email address
        // so lets park the workflow engine
    }

    public String afterEnteredEmailStep() {
        // we are going to park here until a user
        // enters a valid email address
        log.info("User entered email address: " + userEmailAddress);
        return "forkStep";
    }

    public void forkStep() {
        // lets fork some child flows
        TimeoutActivity a = new TimeoutActivity();
        TimeoutActivity b = new TimeoutActivity();
        TimeoutActivity c = new TimeoutActivity();

        log.info("Forking off processes a, b, c");
        fork(a, b, c);

        // now lets add some joins
        joinAll("aCompletedStep", timeout, a);
        joinAll("abcCompletedStep", timeout, a, b, c);
    }

    public void aCompletedStep() {
        log.info("child flow A completed!");
    }

    public String abcCompletedStep() {
        log.info("child flows A, B and C completed!");

        // we are completely done now
        return "stop";
    }

    // External events
    // -------------------------------------------------------------------------
    public void userEntered(String emailAddress) {
        if (emailAddress != null && emailAddress.indexOf("@") > 0) {
            this.userEmailAddress = emailAddress;

            log.info("Lets re-start the suspended workflow");
            goTo("afterEnteredEmailStep");
        }
    }
}
// END SNIPPET: workflow
