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

import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A flow which implements a more traditional workflow model where each method
 * represents a transition.
 * 
 * @version $Revision: $
 */
public class Workflow extends JoinSupport {
    private static final Log log = LogFactory.getLog(Workflow.class);

    public static final String DEFAULT_START_STEP = "startStep";

    private Executor executor;
    private Interpreter interpreter;
    private State<String> step;
    private String nextStep;
    private Timer timer = new Timer();
    private AtomicBoolean suspended = new AtomicBoolean();

    public Workflow() {
        this(Executors.newSingleThreadExecutor(), DEFAULT_START_STEP);
    }

    public Workflow(Executor executor, String firstStep) {
        this(executor, new ReflectionInterpreter(), new DefaultState<String>(firstStep));
    }

    public Workflow(Executor executor, Interpreter interpreter, State<String> step) {
        this.executor = executor;
        this.interpreter = interpreter;
        this.step = step;
    }

    /**
     * Sets the next step to be executed when the current step completes
     */
    public void goTo(String stepName) {
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

            interpreter.executeStep(nextStep, this);

            nextStep();
        }
    }

    public void nextStep() {
        // lets fire any conditions
        step.set(nextStep);

        // if we are not stoped lets add a task to re-evaluate ourself
        if (!isStopped() && !isSuspended()) {
            executor.execute(this);
        }
    }

    /**
     * Forks one or more child flows
     */
    public void fork(TimeoutFlow... flows) {
        for (TimeoutFlow flow : flows) {
            flow.start();
        }
    }
    
    /**
     * Forks one or more child flows
     */
    public void fork(long timeout, TimeoutFlow... flows) {
        for (TimeoutFlow flow : flows) {
            flow.scheduleTimeout(timer, timeout);
            flow.start();
        }
    }

    /**
     * Creates a join such that when all of the flows are completed the given
     * step will be executed
     */
    public void joinAll(final String joinedStep, long timeout, Flow... flows) {
        JoinAll joinFlow = new JoinAll(flows);
        join(joinFlow, joinedStep, timeout);
    }

    /**
     * Performs a join with the given join flow condition, advancing to the
     * specified joinedStep when the join takes place using the given timeout to
     * the join
     */
    public void join(JoinSupport joinFlow, final String joinedStep, long timeout) {
        // start the join flow and register the timeout
        fork(timeout, joinFlow);

        // when the join completes move to the next step
        joinFlow.onStop(createGoToStepTask(joinedStep));
    }

    /**
     * Suspends the workflow processing. The workflow will then wait for an
     * external event before restarting the flow
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
     * Creates a task which will move to the given step
     */
    public Runnable createGoToStepTask(final String joinedStep) {
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
}
