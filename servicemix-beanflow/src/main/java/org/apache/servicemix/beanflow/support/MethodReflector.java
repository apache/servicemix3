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
package org.apache.servicemix.beanflow.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * A reflection based implementation of {@link Callable}
 * 
 * @version $Revision: $
 */
public class MethodReflector<T> implements Callable<T> {

    protected static final Object[] NO_ARGUMENTS = {};

    private final Object source;
    private final Method method;
    private final Object[] arguments;

    public MethodReflector(Object source, Method method) {
        this(source, method, NO_ARGUMENTS);
    }

    public MethodReflector(Object source, Method method, Object[] arguments) {
        this.source = source;
        this.method = method;
        this.arguments = arguments;
    }

    @SuppressWarnings("unchecked")
    public T call() throws Exception {
        try {
            return (T) method.invoke(source, arguments);
        }
        catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof Exception) {
                throw (Exception) targetException;
            }
            else {
                throw e;
            }
        }
    }
}
