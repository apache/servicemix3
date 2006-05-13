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

import org.apache.servicemix.beanflow.annotations.Parallel;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

/**
 * An example parallel proces
 * 
 * @version $Revision: $
 */
// START SNIPPET: workflow
public class ExampleParallelBean {

    private CountDownLatch latch = new CountDownLatch(3);

    public void shouldNotBeRun() {
        throw new RuntimeException("Should not be ran");
    }

    @Parallel
    public void methodOne() {
        System.out.println("Called method one");
        latch.countDown();
    }

    @Parallel
    public void methodTwo() {
        System.out.println("Called method two");
        latch.countDown();
    }

    @Parallel
    public void methodThree() {
        System.out.println("Called method three");
        latch.countDown();
    }

    public void assertCompleted() throws InterruptedException {
        latch.await(3000, TimeUnit.MILLISECONDS);
        if (latch.getCount() > 0) {
            latch.await(300000, TimeUnit.MILLISECONDS);
        }
        Assert.assertEquals("Count down latch value", 0, latch.getCount());
    }
}
// END SNIPPET: workflow
