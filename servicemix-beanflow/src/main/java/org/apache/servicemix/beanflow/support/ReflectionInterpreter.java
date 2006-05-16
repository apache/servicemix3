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

import java.lang.reflect.Method;

/**
 * An interpreter strategy which uses reflection to map step names to method
 * names.
 * 
 * @version $Revision: $
 */
public class ReflectionInterpreter implements Interpreter {

    protected static final Class[] NO_TYPE_ARGUMENTS = {};
    protected static final Object[] NO_PARAMETER_ARGUMENTS = {};

    public void executeStep(String step, Workflow workflow) {
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

    protected void handleStepResult(String step, Workflow workflow, Object result) {
        if (result instanceof String) {
            workflow.goTo((String) result);
        }
        else {
            workflow.suspend();
        }
    }

}
