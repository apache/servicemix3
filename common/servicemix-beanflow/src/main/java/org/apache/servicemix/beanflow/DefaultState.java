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
package org.apache.servicemix.beanflow;

import org.apache.servicemix.beanflow.support.Notifier;
import org.apache.servicemix.beanflow.support.SynchronousNotifier;

/**
 * A default implementation where the state changes are thread safe and the
 * notifications are made outside of the synchronized block to be reentrant.
 * 
 * @version $Revision: $
 */
public class DefaultState<T> implements State<T> {

    private T value;
    private Object lock = new Object();
    private Notifier notifier;

    public DefaultState() {
        notifier = new SynchronousNotifier();
    }

    public DefaultState(T value) {
        this();
        this.value = value;
    }

    public DefaultState(Notifier notifier) {
        this.notifier = notifier;
    }

    public DefaultState(T value, Notifier notifier) {
        this.value = value;
        this.notifier = notifier;
    }

    public T get() {
        synchronized (lock) {
            return value;
        }
    }

    public T getAndSet(T value) {
        T answer = null;
        synchronized (lock) {
            answer = this.value;
            this.value = value;
        }

        notifier.run();
        return answer;
    }

    public void set(T value) {
        synchronized (lock) {
            this.value = value;
        }

        notifier.run();
    }

    public boolean compareAndSet(T expected, T newValue) {
        synchronized (lock) {
            if (equals(value, expected)) {
                this.value = newValue;
                notifier.run();
                return true;
            }
        }
        return false;
    }

    public void addRunnable(Runnable listener) {
        notifier.addRunnable(listener);
    }

    public void removeRunnable(Runnable listener) {
        notifier.removeRunnable(listener);
    }

    public String toString() {
        T currentValue = get();
        if (currentValue == null) {
            return "null";
        }
        else {
            return currentValue.toString();
        }
    }

    /**
     * Returns true if the current value is equal to the given value
     */
    public boolean is(T that) {
        T currentValue = get();
        return equals(that, currentValue);
    }

    public boolean isAny(T... values) {
        T currentValue = get();
        for (T value : values) {
            if (equals(currentValue, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the two values are equal, handling null pointers
     * gracefully
     */
    public static boolean equals(Object value1, Object value2) {
        if (value1 == value2) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }
        return value1.equals(value2);
    }
}
