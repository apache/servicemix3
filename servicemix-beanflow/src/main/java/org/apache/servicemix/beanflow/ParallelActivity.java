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

import org.apache.servicemix.beanflow.support.CallablesFactory;
import org.apache.servicemix.beanflow.support.FindCallableMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An activity which invokes a collection of {@link Callable<T>} methods.
 * 
 * @version $Revision: $
 */
public class ParallelActivity<T> extends ProxyActivity {
    private JoinSupport joinActivity;
    private List<CallableActivity<T>> activities;
    private AtomicBoolean started = new AtomicBoolean();

    /**
     * A helper method to create a new {@link ParallelActivity} which invokes a
     * number of methods on a POJO in parallel and then joins on them all
     * completing
     */
    public static ParallelActivity newParallelMethodActivity(Executor executor, Object bean) {
        return newParallelMethodActivity(new JoinAll(), executor, bean);
    }

    /**
     * A helper method to create a new {@link ParallelActivity} which invokes a
     * number of methods on a POJO in parallel and then performs a custom join
     */
    @SuppressWarnings("unchecked")
    public static ParallelActivity newParallelMethodActivity(JoinSupport join, Executor executor, Object bean) {
        FindCallableMethods factory = new FindCallableMethods(bean);
        return new ParallelActivity(join, executor, factory);
    }

    public ParallelActivity(JoinSupport activity, Executor executor, CallablesFactory<T> callablesFactory) {
        this(activity, executor, callablesFactory.createCallables());
    }

    public ParallelActivity(JoinSupport activity, Executor executor, List<Callable<T>> callables) {
        super(activity);
        this.joinActivity = activity;
        this.activities = new ArrayList<CallableActivity<T>>();
        for (Callable<T> callable : callables) {
            activities.add(new CallableActivity<T>(executor, callable));
        }
    }

    public ParallelActivity(JoinSupport activity, List<CallableActivity<T>> activities) {
        super(activity);
        this.joinActivity = activity;
        this.activities = activities;
    }

    public List<Future<T>> getFutures() {
        List<Future<T>> answer = new ArrayList<Future<T>>();
        for (CallableActivity<T> activity : activities) {
            answer.add(activity.getFuture());
        }
        return answer;
    }

    @Override
    public void start() {
        super.start();
        init();
    }

    @Override
    public void startWithTimeout(Timer timer, long timeout) {
        super.startWithTimeout(timer, timeout);
        init();
    }

    private void init() {
        if (started.compareAndSet(false, true)) {
            doStart();
        }
    }

    protected void doStart() {
        for (CallableActivity<T> activity : activities) {
            joinActivity.fork(activity);
        }
    }

}
