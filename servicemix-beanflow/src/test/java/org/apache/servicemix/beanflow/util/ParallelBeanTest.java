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


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 
 * @version $Revision: $
 */
public class ParallelBeanTest extends ActivityTestSupport {

    public void testParallelWithSyncs() throws Exception {
        ParallelBeanWithSyncs bean = new ParallelBeanWithSyncs();
        startActivity(bean.getActivity(), 10000);
        bean.getActivity().join();

        bean.assertWorked();
        assertStopped(bean.getActivity());
    }
}
