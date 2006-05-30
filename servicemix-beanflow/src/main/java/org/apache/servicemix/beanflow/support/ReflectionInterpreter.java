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
package org.apache.servicemix.beanflow.support;

import org.apache.servicemix.beanflow.Workflow;
import org.apache.servicemix.beanflow.WorkflowStep;

import java.lang.reflect.Method;

/**
 * An interpreter strategy which detects step objects of types
 * 
 * @{link Runnable} or {@link WorkflowStep} otherwise the step object is turned
 *        into a String and reflection is used to map the step to a method on
 *        the workflow object
 * 
 * Thanks to Brian Goetz for this idea :)
 * 
 * @version $Revision: $
 */
public class ReflectionInterpreter<T> implements Interpreter<T> {

    protected static final Class[] NO_TYPE_ARGUMENTS = {};
    protected static final Object[] NO_PARAMETER_ARGUMENTS = {};

    @SuppressWarnings("unchecked")
    public void executeStep(T step, Workflow<T> workflow) {
        if (step instanceof WorkflowStep) {
            WorkflowStep<T> workflowStep = (WorkflowStep<T>) step;
            T nextStep = workflowStep.execute(workflow);
            if (nextStep != null) {
                workflow.setNextStep(nextStep);
            }
            else {
                workflow.suspend();
            }
        }
        else if (step instanceof Runnable) {
            Runnable runnable = (Runnable) step;
            runnable.run();
            goToNextSequence(step, workflow);
        }
        else if (step != null) {
            String name = step.toString();
            executeNamedStep(name, workflow);
        }
    }

    /**
     * If the workflow has been told to go to another step do nothing, else lets
     * go to the next enumeration if we are not suspended, otherwise lets
     * suspend.
     * 
     * @param workflow
     * @param nextStep
     */
    protected void goToNextSequence(T nextStep, Workflow<T> workflow) {
        //if (!workflow.isNextStepAvailable()) {

            // TODO we could automatically go to the next step in the enum list?
            workflow.suspend();
        //}
    }

    public void executeNamedStep(String step, Workflow<T> workflow) {
        Class<? extends Workflow> type = workflow.getClass();
        try {
            Method method = type.getMethod(step, NO_TYPE_ARGUMENTS);
            Object answer = method.invoke(workflow, NO_PARAMETER_ARGUMENTS);
            handleStepResult(step, workflow, answer);
        }
        catch (Exception e) {
            workflow.onStepException(step, e);
        }
    }

    public void validateStepsExist(Object[] stepValues, Workflow<T> workflow) {
        Class<? extends Workflow> type = workflow.getClass();
        for (int i = 0; i < stepValues.length; i++) {
            Object value = stepValues[i];
            if (!isValidStep(value)) {
                String step = value.toString();
                try {
                    type.getMethod(step, NO_TYPE_ARGUMENTS);
                }
                catch (Exception e) {
                    workflow.fail("No " + step + "() method is available in class: " + type.getName()
                            + " so unable to bind the code to the enumeration of steps", e);
                }
            }
        }
    }

    /**
     * Returns true if the step object is capable of being run directly as
     * opposed to via reflection
     */
    protected boolean isValidStep(Object value) {
        return value instanceof WorkflowStep || value instanceof Runnable;
    }

    @SuppressWarnings("unchecked")
    protected void handleStepResult(String step, Workflow workflow, Object result) {
        if (result != null) {
            workflow.setNextStep(result);
        }
        else {
            workflow.suspend();
        }
    }
}
