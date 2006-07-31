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

import java.util.logging.Logger;

public class CountWorkflow extends Workflow<String> {
    
    private static Logger log = Logger.getLogger(CountWorkflow.class.getName());
    
    public static int count = 0;

    public CountWorkflow() { 
        super("startStep"); 
    }
    public String startStep() { 
        count += 1; 
        log.info("Workflow: Validation"); 
        // next step 
        return "persistenceStep";
    }
    public String persistenceStep() { 
        count += 1; 
        log.info("Workflow: Persistence"); 
        // next step 
        return "transferStep"; 
    }
    public String transferStep() { 
        count += 1; 
        log.info("Workflow: Transfer"); 
        // next step 
        return "stop"; 
    }
}