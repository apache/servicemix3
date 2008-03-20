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

/**
 * An enumeration which includes code so that each step is executable directly
 * by the workflow
 * 
 * @version $Revision: $
 */
public enum RunnableSteps implements Runnable {

    START {
        public void run() {
            System.out.println("Start");
        }
    },
    STEP1 {
        public void run() {
            System.out.println("Step 1");
        }
    },
    STEP2 {
        public void run() {
            System.out.println("Step 2");
        }
    },
    STOP {
        public void run() {
            System.out.println("Stop!");
        }
    }
}
