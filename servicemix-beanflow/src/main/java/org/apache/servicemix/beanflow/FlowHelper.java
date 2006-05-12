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

import java.util.List;

/**
 * Some helper methods for working with flows
 * 
 * @version $Revision: $
 */
public class FlowHelper {
    
    /**
     * Ensures that all the flows are started
     */
    public static void start(Flow... flows) {
        for (Flow flow : flows) {
            flow.start();
        }
    }
    
    /**
     * Ensures that all the flows are started
     */
    public static void start(List<Flow> flows) {
        for (Flow flow : flows) {
            flow.start();
        }
    }

    /**
     * Ensures that all the flows are stopped
     */
    public static void stop(Flow... flows) {
        for (Flow flow : flows) {
            flow.stop();
        }
    }

    /**
     * Ensures that all the flows are stopped
     */
    public static void stop(List<Flow> flows) {
        for (Flow flow : flows) {
            flow.stop();
        }
    }
}
