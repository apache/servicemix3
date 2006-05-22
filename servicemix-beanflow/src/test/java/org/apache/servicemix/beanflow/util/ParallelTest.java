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
package org.apache.servicemix.beanflow.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.beanflow.annotations.Parallel;

/**
 * 
 * @version $Revision: $
 */
// START SNIPPET: workflow
public class ParallelTest extends ParallelTestCase {
    private static final Log log = LogFactory.getLog(ParallelBeanWithSyncs.class);

    private boolean methodOneSync1, methodOneSync2, methodTwoSync1, methodTwoSync2;

    @Override
    public void testParallelMethods() throws Exception {
        super.testParallelMethods();

        assertTrue("Did not reach sync1 for methodOne", methodOneSync1);
        assertTrue("Did not reach sync2 for methodOne", methodOneSync2);
        assertTrue("Did not reach sync1 for methodTwo", methodTwoSync1);
        assertTrue("Did not reach sync2 for methodTwo", methodTwoSync2);
    }

    @Parallel
    public void methodOne() {
        log.info("Called method one");
        sync();
        methodOneSync1 = true;
        log.info("methodOne: after sync1");

        // simulate a slow thing
        sleep(1000);

        sync();
        methodOneSync2 = true;
        log.info("methodOne: after sync2");
    }

    @Parallel
    public void methodTwo() {
        log.info("Called method two");

        // simulate a slow thing
        sleep(1000);

        sync();
        methodTwoSync1 = true;
        log.info("methodTwo: after sync1");

        sync();
        methodTwoSync2 = true;
        log.info("methodTwo: after sync2");
    }

}
// END SNIPPET: workflow
