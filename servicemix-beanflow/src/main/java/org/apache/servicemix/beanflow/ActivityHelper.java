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
 * Some helper methods for working with activities
 * 
 * @version $Revision: $
 */
public class ActivityHelper {
    
    /**
     * Ensures that all the activities are started
     */
    public static void start(Activity... activities) {
        for (Activity activity : activities) {
            activity.start();
        }
    }
    
    /**
     * Ensures that all the activities are started
     */
    public static void start(List<Activity> activities) {
        for (Activity activity : activities) {
            activity.start();
        }
    }

    /**
     * Ensures that all the activities are stopped
     */
    public static void stop(Activity... activities) {
        for (Activity activity : activities) {
            activity.stop();
        }
    }

    /**
     * Ensures that all the activities are stopped
     */
    public static void stop(List<Activity> activities) {
        for (Activity activity : activities) {
            activity.stop();
        }
    }
}
