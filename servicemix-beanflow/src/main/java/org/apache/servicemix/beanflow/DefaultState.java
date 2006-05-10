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
package org.apache.servicemix.beanflow;

import java.util.ArrayList;
import java.util.List;

/**
 * A default implementation where the state changes are thread safe and the
 * notifications are made outside of the synchronized block to be reentrant.
 * 
 * @version $Revision: $
 */
public class DefaultState<T> implements State<T> {

    private T value;
    private List<Runnable> listeners = new ArrayList<Runnable>();
    private Object lock = new Object();

    public DefaultState() {
    }

    public DefaultState(T value) {
        this.value = value;
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

        fireEvents();
        return answer;
    }

    public void set(T value) {
        synchronized (lock) {
            this.value = value;
        }

        fireEvents();
    }

    public boolean compareAndSet(T expected, T newValue) {
        synchronized (lock) {
            if (equals(value, expected)) {
                this.value = newValue;
                return true;
            }
        }
        return false;
    }

    public void addRunnable(Runnable listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeRunnable(Runnable listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
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

    protected void fireEvents() {
        Runnable[] array = null;
        synchronized (listeners) {
            array = new Runnable[listeners.size()];
            listeners.toArray(array);
        }
        for (Runnable listener : array) {
            listener.run();
        }
    }
}
