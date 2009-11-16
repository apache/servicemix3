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
import javax.security.auth.Subject;

/**
 * Remote JMX access policy.
 *
 * When configured on a {@link org.apache.servicemix.jbi.jmx.ConnectorServerFactoryBean}, all calls to the
 * MBean server will first be authorized by calling the
 * {@link #checkAuthorization(javax.security.auth.Subject, Object, java.lang.reflect.Method, Object[])} method
 */
public abstract class Policy {

    /**
     * Check if the subject is authorized to call the method.
     *
     * @param subject the subject that want to invoke the method
     * @param target the object on which the method is to be invoked
     * @param method the method that is to be invoked
     * @param args the method parameters
     *
     * @throws SecurityException when the subject is not authorized to call this method
     */
    public abstract void checkAuthorization(Subject subject, Object target, Method method, Object[] args) 
        throws SecurityException;

    /**
     * Does the method represent a read-only operation?
     *
     * @param method the method
     * @return
     */
    protected boolean isReadOnly(Method method) {
        return isReadOnly(method.getName());
    }

    /**
     * Does the method name represent a read-only operation?
     *
     * @param method the method name
     * @return
     */
    protected boolean isReadOnly(String method) {
        return method.startsWith("get")
               || method.startsWith("is")
               || method.startsWith("query")
               || method.startsWith("hashCode");
    }
}
