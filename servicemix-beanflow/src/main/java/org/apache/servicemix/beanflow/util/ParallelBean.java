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
package org.apache.servicemix.beanflow.util;

import org.apache.servicemix.beanflow.JoinAll;
import org.apache.servicemix.beanflow.JoinSupport;
import org.apache.servicemix.beanflow.ParallelActivity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Represents a POJO which contains {@link Parallel} annotations to specify the
 * methods to be executed concurrently. Each parallel thread can then
 * synchronize with the group using the {@link #sync()} or {@link #sync(long)}
 * methods
 * 
 * @version $Revision: $
 * @param <T>
 */
public abstract class ParallelBean {

    private ParallelActivity activity;
    private Executor executor;
    private JoinSupport join;
    private int maxThreadPoolSize = 20;

    public ParallelBean() {
    }

    public ParallelBean(Executor executor) {
        this.executor = executor;
    }

    public ParallelBean(Executor executor, JoinSupport join) {
        this.executor = executor;
        this.join = join;
    }

    @SuppressWarnings("unchecked")
    public void start() {
        getActivity().start();
    }

    @SuppressWarnings("unchecked")
    public void sync() {
        getActivity().sync();
    }

    public boolean sync(long millis) {
        return getActivity().sync(millis);
    }

    // Properties
    // -------------------------------------------------------------------------

    public ParallelActivity getActivity() {
        if (activity == null) {
            activity = createActivity();
        }
        return activity;
    }

    public Executor getExecutor() {
        if (executor == null) {
            executor = createExecutor();
        }
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public JoinSupport getJoin() {
        if (join == null) {
            join = createJoin();
        }
        return join;
    }

    public void setJoin(JoinSupport join) {
        this.join = join;
    }

    // Factory methods
    // -------------------------------------------------------------------------
    protected ParallelActivity createActivity() {
        return ParallelActivity.newParallelMethodActivity(getJoin(), getExecutor(), this);
    }

    protected Executor createExecutor() {
        return Executors.newFixedThreadPool(maxThreadPoolSize);
    }

    protected JoinSupport createJoin() {
        return new JoinAll();
    }

    protected void sleep(int timeout) {
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
