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
import org.apache.servicemix.beanflow.support.Interpreter;
import org.apache.servicemix.beanflow.support.ReflectionInterpreter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An activity which implements a more traditional workflow model where each
 * method represents a transition.
 * 
 * @version $Revision: $
 */
public class Workflow<T> extends JoinSupport {
    private static final Log log = LogFactory.getLog(Workflow.class);

    private static final Class[] NO_PARAMETER_TYPES = {};
    private static final Object[] NO_PARAMETER_VALUES = {};

    private Executor executor;
    private Interpreter interpreter;
    private State<T> step;
    private T nextStep;
    private Timer timer = new Timer();
    private AtomicBoolean suspended = new AtomicBoolean();

    /**
     * TODO is there a way to reference the parameter type of this class? 
    
    public Workflow() {
        this(T);
    }
    */

    @SuppressWarnings("unchecked")
    public Workflow(Class<T> enumType) {
        this((T) getFirstStep(enumType));
    }

    public Workflow(T firstStep) {
        this(Executors.newSingleThreadExecutor(), firstStep);
    }

    public Workflow(Executor executor, T firstStep) {
        this(executor, new ReflectionInterpreter(), new DefaultState<T>(firstStep));
    }

    public Workflow(Executor executor, Interpreter interpreter, State<T> step) {
        this.executor = executor;
        this.interpreter = interpreter;
        this.step = step;

        T firstStep = step.get();
        if (firstStep instanceof Enum) {
            validateStepsExist(firstStep.getClass());
        }
    }

    /**
     * Sets the next step to be executed when the current step completes
     */
    public void goTo(T stepName) {
        this.nextStep = stepName;
        suspended.set(false);
        nextStep();
    }

    public void run() {
        if (!isSuspended() && !isStopped()) {
            if (nextStep == null) {
                nextStep = step.get();
            }
            log.debug("About to execute step: " + nextStep);

            if (nextStep != null) {
                interpreter.executeStep(nextStep, this);

                nextStep();
            }
        }
    }

    public void nextStep() {
        // lets fire any conditions
        T temp = nextStep;
        nextStep = null;
        step.set(temp);

        // if we are not stoped lets add a task to re-evaluate ourself
        if (!isStopped() && !isSuspended()) {
            executor.execute(this);
        }
    }

    /**
     * Forks one or more child activities
     */
    public void fork(TimeoutActivity... activities) {
        for (TimeoutActivity activity : activities) {
            activity.start();
        }
    }

    /**
     * Forks one or more child activities
     */
    public void fork(long timeout, TimeoutActivity... activities) {
        for (TimeoutActivity activity : activities) {
            activity.scheduleTimeout(timer, timeout);
            activity.start();
        }
    }

    /**
     * Creates a join such that when all of the activities are completed the
     * given step will be executed
     */
    public void joinAll(T joinedStep, long timeout, Activity... activities) {
        JoinAll joinFlow = new JoinAll(activities);
        join(joinFlow, joinedStep, timeout);
    }

    /**
     * Performs a join with the given join activity condition, advancing to the
     * specified joinedStep when the join takes place using the given timeout to
     * the join
     */
    public void join(JoinSupport joinFlow, T joinedStep, long timeout) {
        // start the join activity and register the timeout
        fork(timeout, joinFlow);

        // when the join completes move to the next step
        joinFlow.onStop(createGoToStepTask(joinedStep));
    }

    /**
     * Suspends the workflow processing. The workflow will then wait for an
     * external event before restarting the activity
     */
    public void suspend() {
        suspended.set(true);
    }

    /**
     * Returns true if the workflow is in a suspended state where it is waiting
     * for an external event to cause the workflow to resume
     */
    public boolean isSuspended() {
        return suspended.get();
    }

    /**
     * Returns true if this workflow has a next step to execute
     */
    public boolean isNextStepAvailable() {
        return nextStep != null;
    }
    
    /**
     * Creates a task which will move to the given step
     */
    public Runnable createGoToStepTask(final T joinedStep) {
        return new Runnable() {

            public void run() {
                goTo(joinedStep);
                nextStep();
            }
        };
    }

    /**
     * Called when a step fails to execute
     */
    public void onStepException(String stepName, Exception e) {
        log.warn("Step failed: " + stepName + ". Reason: " + e, e);
        suspend();
        fail("Failed to execute step: " + stepName + ". Reason: " + e, e);
    }

    @Override
    protected void onChildStateChange(int childCount, int stoppedCount, int failedCount) {
    }

    /**
     * Lets validate the steps exist on an enumerated type.
     *  
     * Thanks to Sam Pullara for this idea :)
     */
    protected void validateStepsExist(Class enumType) {
        Object[] values = null;
        try {
            values = getEnumValues(enumType);
        }
        catch (Exception e) {
            fail("Cannot get the values of the enumeration: " + enumType.getName(), e);
        }
        if (values != null) {
            interpreter.validateStepsExist(values, this);
        }
    }

    protected static Object getFirstStep(Class enumType) {
        try {
            Object[] values = getEnumValues(enumType);
            return values[0];
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Could not find the values for the enumeration: " + enumType.getName()
                    + ". Reason: " + e, e);
        }
    }

    protected static Object[] getEnumValues(Class enumType) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        Method method = enumType.getMethod("values", NO_PARAMETER_TYPES);
        return (Object[]) method.invoke(null, NO_PARAMETER_VALUES);
    }
}
