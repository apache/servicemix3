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
 * An example workflow
 * 
 * @version $Revision: $
 */
// START SNIPPET: workflow
public class ExampleWorkflow extends Workflow {

    private int loopCount;
    private long timeout = 500;
    private String userEmailAddress;

    // Workflow steps
    // -------------------------------------------------------------------------

    // lets use an explicit goTo() 
    // to tell the workflow which step to go to next
    public void startStep() {
        goTo("loopStep");
    }

    // lets use the return value to
    // specify the next step
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
        System.out.println("User entered email address: " + userEmailAddress);
        return "forkStep";
    }
    
    public void forkStep() {
        // lets fork some child flows
        TimeoutFlow a = new TimeoutFlow();
        TimeoutFlow b = new TimeoutFlow();
        TimeoutFlow c = new TimeoutFlow();
        
        System.out.println("Forking off processes a, b, c");
        fork(a, b, c);
        
        // now lets add some joins
        joinAll("aCompletedStep",timeout, a);
        joinAll("abcCompletedStep", timeout, a, b, c);
    }
    
    public void aCompletedStep() {
        System.out.println("child flow A completed!");
    }
    
    public String abcCompletedStep() {
        System.out.println("child flows A, B and C completed!");
        
        // we are completely done now
        return "stop";
    }

    // External events
    // -------------------------------------------------------------------------
    public void userEntered(String emailAddress) {
        if (emailAddress != null && emailAddress.indexOf("@") > 0) {
            this.userEmailAddress = emailAddress;
            
            System.out.println("Lets re-start the suspended workflow");
            goTo("afterEnteredEmailStep");
        }
    }
}
// END SNIPPET: workflow
