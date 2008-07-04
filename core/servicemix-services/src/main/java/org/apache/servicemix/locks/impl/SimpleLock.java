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
package org.apache.servicemix.locks.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author lhein
 */
public class SimpleLock implements Lock {

    private AtomicBoolean lock;

    /**
     * default constructor
     */
    public SimpleLock() {
        this.lock = new AtomicBoolean(false);
    }
    
    /* (non-Javadoc)
     * @see java.util.concurrent.locks.Lock#lock()
     */
    public void lock() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.locks.Lock#lockInterruptibly()
     */
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.locks.Lock#newCondition()
     */
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.locks.Lock#tryLock()
     */
    public boolean tryLock() {
        return this.lock.compareAndSet(false, true);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.locks.Lock#tryLock(long, java.util.concurrent.TimeUnit)
     */
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.locks.Lock#unlock()
     */
    public void unlock() {
        this.lock.compareAndSet(true, false);
    }
}
