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

import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

/**
 * A JUnit test case which supports the {@link Parallel} annotations for running
 * concurrent threads in a test case along with the methods {@link #sync()} or
 * the {@link #sync(long)} which provide a simple cross-thread synchronisation
 * mechanism
 * 
 * @version $Revision: $
 */
public abstract class ParallelTestCase extends ActivityTestSupport {
    private ParallelActivity activity;
    private Executor executor;
    private JoinSupport join;
    private int maxThreadPoolSize = 20;
    private long testTimeout = 10000;

    @SuppressWarnings("unchecked")
    public void testParallelMethods() throws Exception {
        startActivity(getActivity(), testTimeout);
        getActivity().join();
        assertStopped(getActivity());
    }

    /**
     * Blocks the parallel thread until all the other parallel threads have
     * reached the same synchronisation point before continuing.
     */
    @SuppressWarnings("unchecked")
    public void sync() {
        getActivity().sync();
    }

    /**
     * Blocks up to the give timeout value in the parallel thread until all the
     * other parallel threads have reached the same synchronisation point before
     * continuing.
     * 
     * @return true if the sync completed otherwise false indicating a timeout
     */
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

    public int getMaxThreadPoolSize() {
        return maxThreadPoolSize;
    }

    public void setMaxThreadPoolSize(int maxThreadPoolSize) {
        this.maxThreadPoolSize = maxThreadPoolSize;
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
