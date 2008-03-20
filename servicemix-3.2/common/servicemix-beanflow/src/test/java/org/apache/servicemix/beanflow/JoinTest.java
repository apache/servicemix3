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

import org.apache.servicemix.beanflow.util.ActivityTestSupport;

import java.util.concurrent.TimeUnit;

/**
 * 
 * @version $Revision$
 */
public class JoinTest extends ActivityTestSupport {

    public static class JoinFlow extends Workflow<JoinFlow.Step> {

        public static enum Step {
            first, stop
        }

        public JoinFlow() {
            super(Step.first);
        }

        public void first() {
            final Activity a = new TimeoutActivity() {
                protected void onValidStateChange() {
                    System.out.println("in a");
                    stop();
                    System.out.println("a now stopped");
                }
            };
            final Activity b = new TimeoutActivity() {
                protected void onValidStateChange() {
                    System.out.println("in b");
                    stop();
                    System.out.println("b now stopped");
                }
            };
            System.out.println("in first");
            joinAll(Step.stop, 10000, a, b);
            System.out.println("after join");
        }
    }

    public void testJoin() throws Exception {
        JoinFlow flow = new JoinFlow();
        flow.start();
        System.out.println("waiting for top flow to stop");
        flow.join(20, TimeUnit.SECONDS);
        assertStopped(flow);
        System.out.println("complete!");
    }

}