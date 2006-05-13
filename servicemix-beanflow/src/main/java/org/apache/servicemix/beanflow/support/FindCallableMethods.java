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
package org.apache.servicemix.beanflow.support;

import org.apache.servicemix.beanflow.annotations.Parallel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * A helper class to create callables from an object using methods with a
 * matching annotation.
 * 
 * @version $Revision: $
 */
public class FindCallableMethods<T> implements CallablesFactory<T> {

    private final Object source;
    private List<Class> annotations;

    public FindCallableMethods(Object source) {
        this(source, Parallel.class);
    }

    public FindCallableMethods(Object source, Class annotation) {
        this(source, Collections.singletonList(annotation));
    }

    public FindCallableMethods(Object source, List<Class> annotations) {
        this.annotations = annotations;
        this.source = source;
    }

    public List<Callable<T>> createCallables() {
        List<Callable<T>> answer = new ArrayList<Callable<T>>();
        if (source != null) {
            for (Class type = source.getClass(); type != Object.class && type != null; type = type.getSuperclass()) {
                Method[] methods = type.getDeclaredMethods();
                for (Method method : methods) {
                    if (isValidMethod(source, method)) {
                        MethodReflector<T> reflector = new MethodReflector<T>(source, method);
                        answer.add(reflector);
                    }
                }
            }
        }
        return answer;
    }

    /**
     * Returns the annotations used to detect callable methods
     */
    public List<Class> getAnnotations() {
        return annotations;
    }

    @SuppressWarnings("unchecked")
    protected boolean isValidMethod(Object source, Method method) {
        if (method.getParameterTypes().length == 0) {
            for (Class annotationType : annotations) {
                Annotation annotation = method.getAnnotation(annotationType);
                if (annotation != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
