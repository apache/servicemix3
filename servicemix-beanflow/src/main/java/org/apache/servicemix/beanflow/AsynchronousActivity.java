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
package org.apache.servicemix.beanflow;

import java.util.concurrent.Executor;

/**
 * A simple activity which executes a runnable task in another thread and then
 * completes.
 * 
 * @version $Revision: $
 */
public class AsynchronousActivity extends TimeoutActivity {

    private final Executor executor;
    private final Runnable runnable;

    public AsynchronousActivity(Executor executor, Runnable runnable) {
        this.executor = executor;
        this.runnable = runnable;
    }

    @Override
    protected void doStart() {
        super.doStart();
        executor.execute(new Runnable() {
            public void run() {
                try {
                    runnable.run();
                    //System.out.println("About to call stop on: " + this);
                    stop();
                    //System.out.println("Activity completed: " + this + " status now: " + getState());
                }
                catch (Throwable e) {
                    fail("Failed to run task: " + runnable + ". Cause: " + e, e);
                }
            }
        });
    }
}
