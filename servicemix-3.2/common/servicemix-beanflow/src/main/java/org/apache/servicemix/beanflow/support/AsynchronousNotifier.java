/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.concurrent.Executor;

/**
 * A {@link Notifier} which asynchronously notifies listeners to avoid large
 * amounts of deep recursion in complex state models and activities.
 * 
 * @version $Revision: $
 */
public class AsynchronousNotifier implements Notifier {

    private final Notifier delegate;
    private final Executor executor;

    public AsynchronousNotifier(Executor executor) {
        this(executor, new SynchronousNotifier());
    }

    public AsynchronousNotifier(Executor executor, Notifier delegate) {
        this.executor = executor;
        this.delegate = delegate;
    }

    public void addRunnable(Runnable listener) {
        delegate.addRunnable(listener);
    }

    public void removeRunnable(Runnable listener) {
        delegate.removeRunnable(listener);
    }

    public void run() {
        executor.execute(delegate);
    }

}
