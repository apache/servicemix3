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

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * An asynchronous activity which is capable of returning a future result
 * 
 * @version $Revision: $
 */
public class CallableActivity<T> extends AsynchronousActivity {

    private final Future<T> future;

    public CallableActivity(Executor executor, Callable<T> callable) {
        this(executor, new FutureTask<T>(callable));
    }

    public CallableActivity(Executor executor, FutureTask<T> futureTask) {
        super(executor, futureTask);
        this.future = futureTask;
    }

    /**
     * Returns the future object for the result value of the callable task
     */
    public Future<T> getFuture() {
        return future;
    }

}
