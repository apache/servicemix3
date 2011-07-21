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
import java.security.Principal;
import javax.security.auth.Subject;

import org.apache.servicemix.jbi.security.GroupPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Policy implementation that grants read-write access to members of the 'admin' group
 * and read-only access to all other users.
 *
 * @org.apache.xbean.XBean element="adminReadWritePolicy"
 */
public class AdminReadWritePolicy extends Policy {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(AdminReadWritePolicy.class);

    private static final String INVOKE = "invoke";

    /**
     * {@inheritDoc} 
     */
    public void checkAuthorization(Subject subject, Object target, Method method, Object[] args)
        throws SecurityException {
        if (isReadOnly(method) || isAdmin(subject) || isInvokeReadOnly(method, args)) {
            // allow the method invocation
        } else {
            LOGGER.warn(String.format("Denied access to MBeanServer.%s(%s) to %s",
                                   method.getName(), explode(args), subject));
            throw new SecurityException("Not authorized to run MBeanServer." + method.getName()
                                        + "\n(" + explode(args) + ")");
        }
    }

    /*
     * Check if the call to invoke target a read-only method
     */
    private boolean isInvokeReadOnly(Method method, Object[] args) {
        return INVOKE.equals(method.getName()) && args != null && args.length >= 2 && isReadOnly((String) args[1]);
    }

    /*
     * Explode the array of arguments into a ,-separated String
     */
    private String explode(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        } else {
            StringBuffer buffer = new StringBuffer();
            Object last = args[args.length - 1];
            for (Object arg : args) {
                buffer.append(arg);
                if (arg != last) {
                    buffer.append(", ");
                }
            }
            return buffer.toString();
        }
    }

    /*
     * Check if the subject is a member of the 'admin' group
     */
    private boolean isAdmin(Subject subject) {
        for (Principal principal : subject.getPrincipals()) {
            if (principal instanceof GroupPrincipal && "admin".equals(principal.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "admin group read-write access";
    }

}
