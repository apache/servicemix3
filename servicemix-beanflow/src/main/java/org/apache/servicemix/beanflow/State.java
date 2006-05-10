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

/**
 * Represents a piece of state which can be accessed in a multi threaded way
 * rather like {@link java.util.concurrent.atomic.AtomicReference} but which also 
 * supports the use of listeners to be notified when the state changes.
 * 
 * @version $Revision: $
 */
public interface State<T> {

    /**
     * Returns the current value of the state
     */
    public T get();

    /**
     * Sets the current value
     */
    public void set(T value);

    /**
     * Sets the state to a new value and return the old value
     */
    public T getAndSet(T value);

    /**
     * If the current state is the expected value then set it to the given new
     * value and return true otherwise return false
     */
    public boolean compareAndSet(T expected, T newValue);

    /**
     * Add a task to be executed if the state changes
     */
    public void addRunnable(Runnable listener);

    /**
     * Remove a task
     */
    public void removeRunnable(Runnable listener);

    /**
     * Returns true if the current value is equal to the given value
     */
    public boolean is(T that);

    /**
     * Return true if the current value is equal to any of the given values
     */
    public boolean isAny(T... values);

}