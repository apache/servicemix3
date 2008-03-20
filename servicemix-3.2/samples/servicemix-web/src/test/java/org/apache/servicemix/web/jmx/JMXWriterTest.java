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
package org.apache.servicemix.web.jmx;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 356269 $
 */
public class JMXWriterTest extends TestCase {

    protected PrintWriter writer;
    private JMXWriter jmxWriter;
    private ManagementContext managementContext = new ManagementContext();

    public void testDetail() throws Exception {
        Set names = managementContext.getMBeanServer().queryNames(null, null);
        jmxWriter.outputDetail(names);
    }

    public void testQuery() throws Exception {
        MBeanServer beanServer = managementContext.getMBeanServer();

        beanServer.registerMBean(new Foo("James"), new ObjectName("Bar:type=Foo,name=James"));
        beanServer.registerMBean(new Foo("Rob"), new ObjectName("Bar:type=Foo,name=Rob"));
        beanServer.registerMBean(new Foo("Hiram"), new ObjectName("Bar:type=Foo,name=Hiram"));


        // now lets try find some MBeans using a query

        Set result = beanServer.queryMBeans(null, new ObjectName("*:type=Foo,*"));
        assertEquals("Size of results: " + result, 3, result.size());
    }

    protected void setUp() throws Exception {
        writer = new PrintWriter(new OutputStreamWriter(System.out));
        jmxWriter = new JMXWriter(writer, managementContext);
    }

    protected void tearDown() throws Exception {
        if (writer != null) {
            writer.close();
        }
    }



}
