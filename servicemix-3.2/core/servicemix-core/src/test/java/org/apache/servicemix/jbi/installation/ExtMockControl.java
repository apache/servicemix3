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
package org.apache.servicemix.jbi.installation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import junit.framework.AssertionFailedError;

import org.easymock.ArgumentsMatcher;
import org.easymock.MockControl;
import org.easymock.internal.Range;

public class ExtMockControl implements InvocationHandler {

    private MockControl control;
    private Class clazz;
    private AssertionFailedError error;
    private Object mock;
    
    public ExtMockControl(MockControl control, Class clazz) {
        this.control = control;
        this.clazz = clazz;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(control.getMock(), args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof AssertionFailedError && error == null) {
                error = (AssertionFailedError) e.getCause();
            }
            throw e;
        }
    }
    
    public static ExtMockControl createControl(Class clazz) {
        MockControl mock = MockControl.createControl(clazz);
        return new ExtMockControl(mock, clazz);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndDefaultReturn(boolean, boolean)
     */
    public void expectAndDefaultReturn(boolean arg0, boolean arg1) {
        control.expectAndDefaultReturn(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndDefaultReturn(double, double)
     */
    public void expectAndDefaultReturn(double arg0, double arg1) {
        control.expectAndDefaultReturn(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndDefaultReturn(float, float)
     */
    public void expectAndDefaultReturn(float arg0, float arg1) {
        control.expectAndDefaultReturn(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndDefaultReturn(long, long)
     */
    public void expectAndDefaultReturn(long arg0, long arg1) {
        control.expectAndDefaultReturn(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndDefaultReturn(java.lang.Object, java.lang.Object)
     */
    public void expectAndDefaultReturn(Object arg0, Object arg1) {
        control.expectAndDefaultReturn(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndDefaultThrow(boolean, java.lang.Throwable)
     */
    public void expectAndDefaultThrow(boolean arg0, Throwable arg1) {
        control.expectAndDefaultThrow(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndDefaultThrow(double, java.lang.Throwable)
     */
    public void expectAndDefaultThrow(double arg0, Throwable arg1) {
        control.expectAndDefaultThrow(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndDefaultThrow(float, java.lang.Throwable)
     */
    public void expectAndDefaultThrow(float arg0, Throwable arg1) {
        control.expectAndDefaultThrow(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndDefaultThrow(long, java.lang.Throwable)
     */
    public void expectAndDefaultThrow(long arg0, Throwable arg1) {
        control.expectAndDefaultThrow(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndDefaultThrow(java.lang.Object, java.lang.Throwable)
     */
    public void expectAndDefaultThrow(Object arg0, Throwable arg1) {
        control.expectAndDefaultThrow(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(boolean, boolean, int, int)
     */
    public void expectAndReturn(boolean arg0, boolean arg1, int arg2, int arg3) {
        control.expectAndReturn(arg0, arg1, arg2, arg3);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(boolean, boolean, int)
     */
    public void expectAndReturn(boolean arg0, boolean arg1, int arg2) {
        control.expectAndReturn(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(boolean, boolean, org.easymock.internal.Range)
     */
    public void expectAndReturn(boolean arg0, boolean arg1, Range arg2) {
        control.expectAndReturn(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(boolean, boolean)
     */
    public void expectAndReturn(boolean arg0, boolean arg1) {
        control.expectAndReturn(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(double, double, int, int)
     */
    public void expectAndReturn(double arg0, double arg1, int arg2, int arg3) {
        control.expectAndReturn(arg0, arg1, arg2, arg3);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(double, double, int)
     */
    public void expectAndReturn(double arg0, double arg1, int arg2) {
        control.expectAndReturn(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(double, double, org.easymock.internal.Range)
     */
    public void expectAndReturn(double arg0, double arg1, Range arg2) {
        control.expectAndReturn(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(double, double)
     */
    public void expectAndReturn(double arg0, double arg1) {
        control.expectAndReturn(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(float, float, int, int)
     */
    public void expectAndReturn(float arg0, float arg1, int arg2, int arg3) {
        control.expectAndReturn(arg0, arg1, arg2, arg3);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(float, float, int)
     */
    public void expectAndReturn(float arg0, float arg1, int arg2) {
        control.expectAndReturn(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(float, float, org.easymock.internal.Range)
     */
    public void expectAndReturn(float arg0, float arg1, Range arg2) {
        control.expectAndReturn(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(float, float)
     */
    public void expectAndReturn(float arg0, float arg1) {
        control.expectAndReturn(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(long, long, int, int)
     */
    public void expectAndReturn(long arg0, long arg1, int arg2, int arg3) {
        control.expectAndReturn(arg0, arg1, arg2, arg3);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(long, long, int)
     */
    public void expectAndReturn(long arg0, long arg1, int arg2) {
        control.expectAndReturn(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(long, long, org.easymock.internal.Range)
     */
    public void expectAndReturn(long arg0, long arg1, Range arg2) {
        control.expectAndReturn(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(long, long)
     */
    public void expectAndReturn(long arg0, long arg1) {
        control.expectAndReturn(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(java.lang.Object, java.lang.Object, int, int)
     */
    public void expectAndReturn(Object arg0, Object arg1, int arg2, int arg3) {
        control.expectAndReturn(arg0, arg1, arg2, arg3);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(java.lang.Object, java.lang.Object, int)
     */
    public void expectAndReturn(Object arg0, Object arg1, int arg2) {
        control.expectAndReturn(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(java.lang.Object, java.lang.Object, org.easymock.internal.Range)
     */
    public void expectAndReturn(Object arg0, Object arg1, Range arg2) {
        control.expectAndReturn(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndReturn(java.lang.Object, java.lang.Object)
     */
    public void expectAndReturn(Object arg0, Object arg1) {
        control.expectAndReturn(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(boolean, java.lang.Throwable, int, int)
     */
    public void expectAndThrow(boolean arg0, Throwable arg1, int arg2, int arg3) {
        control.expectAndThrow(arg0, arg1, arg2, arg3);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(boolean, java.lang.Throwable, int)
     */
    public void expectAndThrow(boolean arg0, Throwable arg1, int arg2) {
        control.expectAndThrow(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(boolean, java.lang.Throwable, org.easymock.internal.Range)
     */
    public void expectAndThrow(boolean arg0, Throwable arg1, Range arg2) {
        control.expectAndThrow(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(boolean, java.lang.Throwable)
     */
    public void expectAndThrow(boolean arg0, Throwable arg1) {
        control.expectAndThrow(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(double, java.lang.Throwable, int, int)
     */
    public void expectAndThrow(double arg0, Throwable arg1, int arg2, int arg3) {
        control.expectAndThrow(arg0, arg1, arg2, arg3);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(double, java.lang.Throwable, int)
     */
    public void expectAndThrow(double arg0, Throwable arg1, int arg2) {
        control.expectAndThrow(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(double, java.lang.Throwable, org.easymock.internal.Range)
     */
    public void expectAndThrow(double arg0, Throwable arg1, Range arg2) {
        control.expectAndThrow(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(double, java.lang.Throwable)
     */
    public void expectAndThrow(double arg0, Throwable arg1) {
        control.expectAndThrow(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(float, java.lang.Throwable, int, int)
     */
    public void expectAndThrow(float arg0, Throwable arg1, int arg2, int arg3) {
        control.expectAndThrow(arg0, arg1, arg2, arg3);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(float, java.lang.Throwable, int)
     */
    public void expectAndThrow(float arg0, Throwable arg1, int arg2) {
        control.expectAndThrow(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(float, java.lang.Throwable, org.easymock.internal.Range)
     */
    public void expectAndThrow(float arg0, Throwable arg1, Range arg2) {
        control.expectAndThrow(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(float, java.lang.Throwable)
     */
    public void expectAndThrow(float arg0, Throwable arg1) {
        control.expectAndThrow(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(long, java.lang.Throwable, int, int)
     */
    public void expectAndThrow(long arg0, Throwable arg1, int arg2, int arg3) {
        control.expectAndThrow(arg0, arg1, arg2, arg3);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(long, java.lang.Throwable, int)
     */
    public void expectAndThrow(long arg0, Throwable arg1, int arg2) {
        control.expectAndThrow(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(long, java.lang.Throwable, org.easymock.internal.Range)
     */
    public void expectAndThrow(long arg0, Throwable arg1, Range arg2) {
        control.expectAndThrow(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(long, java.lang.Throwable)
     */
    public void expectAndThrow(long arg0, Throwable arg1) {
        control.expectAndThrow(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(java.lang.Object, java.lang.Throwable, int, int)
     */
    public void expectAndThrow(Object arg0, Throwable arg1, int arg2, int arg3) {
        control.expectAndThrow(arg0, arg1, arg2, arg3);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(java.lang.Object, java.lang.Throwable, int)
     */
    public void expectAndThrow(Object arg0, Throwable arg1, int arg2) {
        control.expectAndThrow(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(java.lang.Object, java.lang.Throwable, org.easymock.internal.Range)
     */
    public void expectAndThrow(Object arg0, Throwable arg1, Range arg2) {
        control.expectAndThrow(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#expectAndThrow(java.lang.Object, java.lang.Throwable)
     */
    public void expectAndThrow(Object arg0, Throwable arg1) {
        control.expectAndThrow(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#getMock()
     */
    public Object getMock() {
        if (mock == null) {
            mock = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz },  this);
        }
        return mock;
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#replay()
     */
    public void replay() {
        control.replay();
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#reset()
     */
    public void reset() {
        control.reset();
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setDefaultMatcher(org.easymock.ArgumentsMatcher)
     */
    public void setDefaultMatcher(ArgumentsMatcher arg0) {
        control.setDefaultMatcher(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setDefaultReturnValue(boolean)
     */
    public void setDefaultReturnValue(boolean arg0) {
        control.setDefaultReturnValue(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setDefaultReturnValue(double)
     */
    public void setDefaultReturnValue(double arg0) {
        control.setDefaultReturnValue(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setDefaultReturnValue(float)
     */
    public void setDefaultReturnValue(float arg0) {
        control.setDefaultReturnValue(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setDefaultReturnValue(long)
     */
    public void setDefaultReturnValue(long arg0) {
        control.setDefaultReturnValue(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setDefaultReturnValue(java.lang.Object)
     */
    public void setDefaultReturnValue(Object arg0) {
        control.setDefaultReturnValue(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setDefaultThrowable(java.lang.Throwable)
     */
    public void setDefaultThrowable(Throwable arg0) {
        control.setDefaultThrowable(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setDefaultVoidCallable()
     */
    public void setDefaultVoidCallable() {
        control.setDefaultVoidCallable();
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setMatcher(org.easymock.ArgumentsMatcher)
     */
    public void setMatcher(ArgumentsMatcher arg0) {
        control.setMatcher(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(boolean, int, int)
     */
    public void setReturnValue(boolean arg0, int arg1, int arg2) {
        control.setReturnValue(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(boolean, int)
     */
    public void setReturnValue(boolean arg0, int arg1) {
        control.setReturnValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(boolean, org.easymock.internal.Range)
     */
    public void setReturnValue(boolean arg0, Range arg1) {
        control.setReturnValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(boolean)
     */
    public void setReturnValue(boolean arg0) {
        control.setReturnValue(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(double, int, int)
     */
    public void setReturnValue(double arg0, int arg1, int arg2) {
        control.setReturnValue(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(double, int)
     */
    public void setReturnValue(double arg0, int arg1) {
        control.setReturnValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(double, org.easymock.internal.Range)
     */
    public void setReturnValue(double arg0, Range arg1) {
        control.setReturnValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(double)
     */
    public void setReturnValue(double arg0) {
        control.setReturnValue(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(float, int, int)
     */
    public void setReturnValue(float arg0, int arg1, int arg2) {
        control.setReturnValue(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(float, int)
     */
    public void setReturnValue(float arg0, int arg1) {
        control.setReturnValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(float, org.easymock.internal.Range)
     */
    public void setReturnValue(float arg0, Range arg1) {
        control.setReturnValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(float)
     */
    public void setReturnValue(float arg0) {
        control.setReturnValue(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(long, int, int)
     */
    public void setReturnValue(long arg0, int arg1, int arg2) {
        control.setReturnValue(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(long, int)
     */
    public void setReturnValue(long arg0, int arg1) {
        control.setReturnValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(long, org.easymock.internal.Range)
     */
    public void setReturnValue(long arg0, Range arg1) {
        control.setReturnValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(long)
     */
    public void setReturnValue(long arg0) {
        control.setReturnValue(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(java.lang.Object, int, int)
     */
    public void setReturnValue(Object arg0, int arg1, int arg2) {
        control.setReturnValue(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(java.lang.Object, int)
     */
    public void setReturnValue(Object arg0, int arg1) {
        control.setReturnValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(java.lang.Object, org.easymock.internal.Range)
     */
    public void setReturnValue(Object arg0, Range arg1) {
        control.setReturnValue(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setReturnValue(java.lang.Object)
     */
    public void setReturnValue(Object arg0) {
        control.setReturnValue(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setThrowable(java.lang.Throwable, int, int)
     */
    public void setThrowable(Throwable arg0, int arg1, int arg2) {
        control.setThrowable(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setThrowable(java.lang.Throwable, int)
     */
    public void setThrowable(Throwable arg0, int arg1) {
        control.setThrowable(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setThrowable(java.lang.Throwable, org.easymock.internal.Range)
     */
    public void setThrowable(Throwable arg0, Range arg1) {
        control.setThrowable(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setThrowable(java.lang.Throwable)
     */
    public void setThrowable(Throwable arg0) {
        control.setThrowable(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setVoidCallable()
     */
    public void setVoidCallable() {
        control.setVoidCallable();
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setVoidCallable(int, int)
     */
    public void setVoidCallable(int arg0, int arg1) {
        control.setVoidCallable(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setVoidCallable(int)
     */
    public void setVoidCallable(int arg0) {
        control.setVoidCallable(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#setVoidCallable(org.easymock.internal.Range)
     */
    public void setVoidCallable(Range arg0) {
        control.setVoidCallable(arg0);
    }

    /* (non-Javadoc)
     * @see org.easymock.MockControl#verify()
     */
    public void verify() {
        if (error != null) {
            throw error;
        }
        control.verify();
    }

}
