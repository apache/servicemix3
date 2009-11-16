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
package org.apache.servicemix.jbi.jmx;

import java.lang.reflect.Method;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.security.auth.Subject;

import junit.framework.TestCase;
import org.apache.servicemix.jbi.security.GroupPrincipal;

/**
 * Test cases for {@link org.apache.servicemix.jbi.jmx.AdminReadWritePolicy}
 */
public class AdminReadWritePolicyTest extends TestCase {

    private AdminReadWritePolicy policy = new AdminReadWritePolicy();

    public void testCheckAuthorization() throws NoSuchMethodException, MalformedObjectNameException {
        Subject user = new Subject();
        Subject admin = new Subject();
        admin.getPrincipals().add(new GroupPrincipal("admin"));

        Method hashCode = Object.class.getMethod("hashCode");
        Method isRegistered = MBeanServer.class.getMethod("isRegistered", ObjectName.class);
        Method getAttribute = MBeanServer.class.getMethod("getAttribute", ObjectName.class, String.class);
        Method queryNames = MBeanServer.class.getMethod("queryNames", ObjectName.class, QueryExp.class);
        Method invoke =
            MBeanServer.class.getMethod("invoke", ObjectName.class, String.class, Object[].class, String[].class);

        ObjectName threading = ObjectName.getInstance("java.lang:type=Threading");

        // admin user is allowed to use all methods
        policy.checkAuthorization(admin, null, hashCode, null);
        policy.checkAuthorization(admin, null, isRegistered, null);
        policy.checkAuthorization(admin, null, getAttribute, null);
        policy.checkAuthorization(admin, null, queryNames, null);
        policy.checkAuthorization(admin, null, invoke, null);
        policy.checkAuthorization(admin, null, invoke, new Object[] {threading, "getThreadInfo"});

        // users are allowed to use these four methods...
        policy.checkAuthorization(user, null, hashCode, null);
        policy.checkAuthorization(user, null, isRegistered, null);
        policy.checkAuthorization(user, null, getAttribute, null);
        policy.checkAuthorization(user, null, queryNames, null);
        policy.checkAuthorization(user, null, invoke, new Object[] {threading, "getThreadInfo"});

        // ...but aren't allow to invoke any operations remotely
        try {
            policy.checkAuthorization(user, null, invoke, null);
            fail("Policy check should have thrown a SecurityException");
        } catch (SecurityException e) {
            // this is OK
        }

    }

}
