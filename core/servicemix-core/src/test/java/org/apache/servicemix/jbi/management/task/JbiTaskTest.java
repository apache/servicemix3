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
package org.apache.servicemix.jbi.management.task;

import java.util.Map;
import javax.management.remote.JMXConnector;

import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;
import org.apache.tools.ant.Project;

/**
 *
 * JbiTaskTest
 * @version $Revision$
 */
public class JbiTaskTest extends JbiTaskSupport {

    private static final String USERNAME = "user";
    private static final String PASSWORD = "passw0rd";

    private JbiTask jbiTask;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();        
        jbiTask = new JbiTask() {
            protected void doExecute(AdminCommandsServiceMBean acs) throws Exception {
            }
        };
        jbiTask.setProject(new Project());
        jbiTask.init();
        jbiTask.connect();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        jbiTask.close();
        super.tearDown();
    }

    public void testGetAdminCommandsService() throws Exception {
        AdminCommandsServiceMBean mbean = jbiTask.getAdminCommandsService();
        assertNotNull(mbean);
    }

    public void testGetEnvironmentMapOnlyCredentials() throws Exception {
        jbiTask.setUsername(USERNAME);
        jbiTask.setPassword(PASSWORD);

        // support null ...
        jbiTask.setEnvironment(null);
        assertOnlyCredentialsInMap(jbiTask.getEnvironmentMap());

        // ... as well as an empty string
        jbiTask.setEnvironment("   ");
        assertOnlyCredentialsInMap(jbiTask.getEnvironmentMap());
    }

    public void testGetEnvironmentMapExtraInformationAdded() throws Exception {
        jbiTask.setUsername(USERNAME);
        jbiTask.setPassword(PASSWORD);

        // one extra value
        jbiTask.setEnvironment("key=value");
        Map<String, Object> map = jbiTask.getEnvironmentMap();
        assertCredentialsInMap(map);
        assertEquals("Extra value should be in map", "value", map.get("key"));

        // two extra values
        jbiTask.setEnvironment("key=value, another_key=another_value");
        map = jbiTask.getEnvironmentMap();
        assertCredentialsInMap(map);
        assertEquals("Extra value should be in map", "value", map.get("key"));
        assertEquals("Extra value should be in map", "another_value", map.get("another_key"));
    }

    private void assertOnlyCredentialsInMap(Map<String, Object> map) {
        assertEquals("Map contains 1 entry", 1, map.size());
        assertCredentialsInMap(map);
    }

    private void assertCredentialsInMap(Map<String, Object> map) {
        String[] credentials = (String[]) map.get(JMXConnector.CREDENTIALS);
        assertNotNull("Credentials should be in environment map", credentials);
        assertEquals(USERNAME, credentials[0]);
        assertEquals(PASSWORD, credentials[1]);
    }
}