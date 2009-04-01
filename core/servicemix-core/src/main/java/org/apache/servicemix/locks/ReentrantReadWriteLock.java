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
package org.apache.servicemix.locks;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;


public class ReentrantReadWriteLock implements ReadWriteLock,
        java.io.Serializable {
    
    /* 
     * Read vs write count extraction constants and functions.
     * Lock state is logically divided into two shorts: The lower
     * one representing the exclusive (writer) lock hold count,
     * and the upper the shared (reader) hold count.
     */

    static final int SHARED_SHIFT = 16;

    static final int SHARED_UNIT = 1 << SHARED_SHIFT;

    static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;
    
    private static final long serialVersionUID = -6992448646407690164L;

    /** Inner class providing readlock */
    private final ReentrantReadWriteLock.ReadLock readerLock;

    /** Inner class providing writelock */
    private final ReentrantReadWriteLock.WriteLock writerLock;

    /** Performs all synchronization mechanics */
    private final Sync sync;

    /**
     * Creates a new <tt>ReentrantReadWriteLock</tt> with
     * default ordering properties.
     */
    public ReentrantReadWriteLock() {
        sync = new NonfairSync();
        readerLock = new ReadLock(this);
        writerLock = new WriteLock(this);
    }

    /**
     * Creates a new <tt>ReentrantReadWriteLock</tt> with
     * the given fairness policy.
     *
     * @param fair true if this lock should use a fair ordering policy
     */
    public ReentrantReadWriteLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
        readerLock = new ReadLock(this);
        writerLock = new WriteLock(this);
    }

    public ReentrantReadWriteLock.WriteLock writeLock() {
        return writerLock;
    }

    public ReentrantReadWriteLock.ReadLock readLock() {
        return readerLock;
    }

    

    /** Returns the number of shared holds represented in count  */
    static int sharedCount(int c) {
        return c >>> SHARED_SHIFT;
    }

    /** Returns the number of exclusive holds represented in count  */
    static int exclusiveCount(int c) {
        return c & EXCLUSIVE_MASK;
    }

    /** 
     * Synchronization implementation for ReentrantReadWriteLock.
     * Subclassed into fair and nonfair versions.
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        /** Current (exclusive) owner thread */
        transient Thread owner;

        /**
         * Perform write lock. Allows fast path in non-fair version.
         */
        abstract void wlock();

        /** 
         * Perform non-fair tryLock for write.  tryAcquire is
         * implemented in subclasses, but both versions need nonfair
         * try for trylock method
         */
        final boolean nonfairTryAcquire(int acquires) {
            // mask out readlocks if called from condition methods
            acquires = exclusiveCount(acquires);
            Thread current = Thread.currentThread();
            int c = getState();
            int w = exclusiveCount(c);
            if (w + acquires >= SHARED_UNIT) {
                throw new Error("Maximum lock count exceeded");
            }
            if (c != 0 && (w == 0 || current != owner)) {
                return false;
            }
            if (!compareAndSetState(c, c + acquires)) {
                return false;
            }
            owner = current;
            return true;
        }

        /** 
         * Perform nonfair tryLock for read. 
         */
        final int nonfairTryAcquireShared(int acquires) {
            for (;;) {
                int c = getState();
                int nextc = c + (acquires << SHARED_SHIFT);
                if (nextc < c) {
                    throw new Error("Maximum lock count exceeded");
                }
                if (exclusiveCount(c) != 0 && owner != Thread.currentThread()) {
                    return -1;
                }
                if (compareAndSetState(c, nextc)) {
                    return 1;
                }
                // Recheck count if lost CAS
            }
        }

        protected final boolean tryRelease(int releases) {
            Thread current = Thread.currentThread();
            int c = getState();
            if (owner != current) {
                throw new IllegalMonitorStateException();
            }
            int nextc = c - releases;
            boolean free = false;
            if (exclusiveCount(c) == releases) {
                free = true;
                owner = null;
            }
            setState(nextc);
            return free;
        }

        protected final boolean tryReleaseShared(int releases) {
            for (;;) {
                int c = getState();
                int nextc = c - (releases << SHARED_SHIFT);
                if (nextc < 0) {
                    throw new IllegalMonitorStateException();
                }
                if (compareAndSetState(c, nextc)) {
                    return nextc == 0;
                }
            }
        }

        protected final boolean isHeldExclusively() {
            return exclusiveCount(getState()) != 0
                    && owner == Thread.currentThread();
        }

        // Methods relayed to outer class

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        final Thread getOwner() {
            int c = exclusiveCount(getState());
            Thread o = owner;
            return (c == 0) ? null : o;
        }

        final int getReadLockCount() {
            return sharedCount(getState());
        }

        final boolean isWriteLocked() {
            return exclusiveCount(getState()) != 0;
        }

        final int getWriteHoldCount() {
            int c = exclusiveCount(getState());
            Thread o = owner;
            return (o == Thread.currentThread()) ? c : 0;
        }

        /**
         * Reconstitute this lock instance from a stream
         * @param s the stream
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            setState(0); // reset to unlocked state
        }

        final int getCount() {
            return getState();
        }
    }

    /** 
     * Nonfair version of Sync
     */
    static final class NonfairSync extends Sync {
        protected boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }

        protected int tryAcquireShared(int acquires) {
            return nonfairTryAcquireShared(acquires);
        }

        // Use fastpath for main write lock method
        void wlock() {
            if (compareAndSetState(0, 1)) {
                owner = Thread.currentThread();
            } else {
                acquire(1);
            }
        }
    }

    /** 
     * Fair version of Sync
     */
    static final class FairSync extends Sync {
        protected boolean tryAcquire(int acquires) {
            // mask out readlocks if called from condition methods
            acquires = exclusiveCount(acquires);
            Thread current = Thread.currentThread();
            Thread first;
            int c = getState();
            int w = exclusiveCount(c);
            if (w + acquires >= SHARED_UNIT) {
                throw new Error("Maximum lock count exceeded");
            }
            first = getFirstQueuedThread();
            if ((w == 0 || current != owner) 
                    && (c != 0 || (first != null && first != current))) {
                return false;
            }
            if (!compareAndSetState(c, c + acquires)) {
                return false;
            }
            owner = current;
            return true;
        }

        protected int tryAcquireShared(int acquires) {
            Thread current = Thread.currentThread();
            for (;;) {
                Thread first = getFirstQueuedThread();
                if (first != null && first != current) {
                    return -1;
                }
                int c = getState();
                int nextc = c + (acquires << SHARED_SHIFT);
                if (nextc < c) {
                    throw new Error("Maximum lock count exceeded");
                }
                if (exclusiveCount(c) != 0 && owner != Thread.currentThread()) {
                    return -1;
                }
                if (compareAndSetState(c, nextc)) {
                    return 1;
                // Recheck count if lost CAS
                }
            }
        }

        void wlock() { // no fast path
            acquire(1);
        }
    }

    /**
     * The lock returned by method {@link ReentrantReadWriteLock#readLock}.
     */
    public static class ReadLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -5992448646407690164L;

        private final Sync sync;

        /** 
         * Constructor for use by subclasses 
         * @param lock the outer lock object
         * @throws NullPointerException if lock null
         */
        protected ReadLock(ReentrantReadWriteLock lock) {
            sync = lock.sync;
        }

        /**
         * Acquires the shared lock. 
         *
         * <p>Acquires the lock if it is not held exclusively by
         * another thread and returns immediately.
         *
         * <p>If the lock is held exclusively by another thread then
         * the current thread becomes disabled for thread scheduling
         * purposes and lies dormant until the lock has been acquired.
         */
        public void lock() {
            sync.acquireShared(1);
        }

        /**
         * Acquires the shared lock unless the current thread is 
         * {@link Thread#interrupt interrupted}.
         *
         * <p>Acquires the shared lock if it is not held exclusively
         * by another thread and returns immediately.
         *
         * <p>If the lock is held by another thread then the
         * current thread becomes disabled for thread scheduling 
         * purposes and lies dormant until one of two things happens:
         *
         * <ul>
         *
         * <li>The lock is acquired by the current thread; or
         *
         * <li>Some other thread {@link Thread#interrupt interrupts}
         * the current thread.
         *
         * </ul>
         *
         * <p>If the current thread:
         *
         * <ul>
         *
         * <li>has its interrupted status set on entry to this method; or 
         *
         * <li>is {@link Thread#interrupt interrupted} while acquiring 
         * the lock,
         *
         * </ul>
         *
         * then {@link InterruptedException} is thrown and the current
         * thread's interrupted status is cleared.
         *
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock.
         *
         * @throws InterruptedException if the current thread is interrupted
         */
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
        }

        /**
         * Acquires the shared lock only if it is not held exclusively by
         * another thread at the time of invocation.
         *
         * <p>Acquires the lock if it is not held exclusively by
         * another thread and returns immediately with the value
         * <tt>true</tt>. Even when this lock has been set to use a
         * fair ordering policy, a call to <tt>tryLock()</tt>
         * <em>will</em> immediately acquire the lock if it is
         * available, whether or not other threads are currently
         * waiting for the lock.  This &quot;barging&quot; behavior
         * can be useful in certain circumstances, even though it
         * breaks fairness. If you want to honor the fairness setting
         * for this lock, then use {@link #tryLock(long, TimeUnit)
         * tryLock(0, TimeUnit.SECONDS) } which is almost equivalent
         * (it also detects interruption).
         *
         * <p>If the lock is held exclusively by another thread then
         * this method will return immediately with the value
         * <tt>false</tt>.
         *
         * @return <tt>true</tt> if the lock was acquired.
         */
        public boolean tryLock() {
            return sync.nonfairTryAcquireShared(1) >= 0;
        }

        /**
         * Acquires the shared lock if it is not held exclusively by
         * another thread within the given waiting time and the
         * current thread has not been {@link Thread#interrupt
         * interrupted}.
         *
         * <p>Acquires the lock if it is not held exclusively by
         * another thread and returns immediately with the value
         * <tt>true</tt>. If this lock has been set to use a fair
         * ordering policy then an available lock <em>will not</em> be
         * acquired if any other threads are waiting for the
         * lock. This is in contrast to the {@link #tryLock()}
         * method. If you want a timed <tt>tryLock</tt> that does
         * permit barging on a fair lock then combine the timed and
         * un-timed forms together:
         *
         * <pre>if (lock.tryLock() || lock.tryLock(timeout, unit) ) { ... }
         * </pre>
         *
         * <p>If the lock is held exclusively by another thread then the
         * current thread becomes disabled for thread scheduling 
         * purposes and lies dormant until one of three things happens:
         *
         * <ul>
         *
         * <li>The lock is acquired by the current thread; or
         *
         * <li>Some other thread {@link Thread#interrupt interrupts} the current
         * thread; or
         *
         * <li>The specified waiting time elapses
         *
         * </ul>
         *
         * <p>If the lock is acquired then the value <tt>true</tt> is
         * returned.
         *
         * <p>If the current thread:
         *
         * <ul>
         *
         * <li>has its interrupted status set on entry to this method; or 
         *
         * <li>is {@link Thread#interrupt interrupted} while acquiring
         * the lock,
         *
         * </ul> then {@link InterruptedException} is thrown and the
         * current thread's interrupted status is cleared.
         *
         * <p>If the specified waiting time elapses then the value
         * <tt>false</tt> is returned.  If the time is less than or
         * equal to zero, the method will not wait at all.
         *
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock, and over reporting the elapse of the waiting time.
         *
         * @param timeout the time to wait for the lock
         * @param unit the time unit of the timeout argument
         *
         * @return <tt>true</tt> if the lock was acquired.
         *
         * @throws InterruptedException if the current thread is interrupted
         * @throws NullPointerException if unit is null
         *
         */
        public boolean tryLock(long timeout, TimeUnit unit)
            throws InterruptedException {
            return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
        }

        /**
         * Attempts to release this lock.  
         *
         * <p> If the number of readers is now zero then the lock
         * is made available for other lock attempts.
         */
        public void unlock() {
            sync.releaseShared(1);
        }

        /**
         * Throws UnsupportedOperationException because ReadLocks
         * do not support conditions.
         * @throws UnsupportedOperationException always
         */
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns a string identifying this lock, as well as its lock state.
         * The state, in brackets, includes the String 
         * &quot;Read locks =&quot; followed by the number of held
         * read locks.
         * @return a string identifying this lock, as well as its lock state.
         */
        public String toString() {
            int r = sync.getReadLockCount();
            return super.toString() + "[Read locks = " + r + "]";
        }

    }

    /**
     * The lock returned by method {@link ReentrantReadWriteLock#writeLock}.
     */
    public static class WriteLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -4992448646407690164L;

        private final Sync sync;

        /** 
         * Constructor for use by subclasses 
         * @param lock the outer lock object
         * @throws NullPointerException if lock null
         */
        protected WriteLock(ReentrantReadWriteLock lock) {
            sync = lock.sync;
        }

        /**
         * Acquire the lock. 
         *
         * <p>Acquires the lock if it is not held by another thread
         * and returns immediately, setting the lock hold count to
         * one.
         *
         * <p>If the current thread already holds the lock then the
         * hold count is incremented by one and the method returns
         * immediately.
         *
         * <p>If the lock is held by another thread then the current
         * thread becomes disabled for thread scheduling purposes and
         * lies dormant until the lock has been acquired, at which
         * time the lock hold count is set to one.
         */
        public void lock() {
            sync.wlock();
        }

        /**
         * Acquires the lock unless the current thread is {@link
         * Thread#interrupt interrupted}.
         *
         * <p>Acquires the lock if it is not held by another thread
         * and returns immediately, setting the lock hold count to
         * one.
         *
         * <p>If the current thread already holds this lock then the
         * hold count is incremented by one and the method returns
         * immediately.
         *
         * <p>If the lock is held by another thread then the current
         * thread becomes disabled for thread scheduling purposes and
         * lies dormant until one of two things happens:
         *
         * <ul>
         *
         * <li>The lock is acquired by the current thread; or
         *
         * <li>Some other thread {@link Thread#interrupt interrupts}
         * the current thread.
         *
         * </ul>
         *
         * <p>If the lock is acquired by the current thread then the
         * lock hold count is set to one.
         *
         * <p>If the current thread:
         *
         * <ul>
         *
         * <li>has its interrupted status set on entry to this method;
         * or
         *
         * <li>is {@link Thread#interrupt interrupted} while acquiring
         * the lock,
         *
         * </ul>
         *
         * then {@link InterruptedException} is thrown and the current
         * thread's interrupted status is cleared.
         *
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock.
         *
         * @throws InterruptedException if the current thread is interrupted
         */
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }

        /**
         * Acquires the lock only if it is not held by another thread
         * at the time of invocation.
         *
         * <p>Acquires the lock if it is not held by another thread
         * and returns immediately with the value <tt>true</tt>,
         * setting the lock hold count to one. Even when this lock has
         * been set to use a fair ordering policy, a call to
         * <tt>tryLock()</tt> <em>will</em> immediately acquire the
         * lock if it is available, whether or not other threads are
         * currently waiting for the lock.  This &quot;barging&quot;
         * behavior can be useful in certain circumstances, even
         * though it breaks fairness. If you want to honor the
         * fairness setting for this lock, then use {@link
         * #tryLock(long, TimeUnit) tryLock(0, TimeUnit.SECONDS) }
         * which is almost equivalent (it also detects interruption).
         *
         * <p> If the current thread already holds this lock then the
         * hold count is incremented by one and the method returns
         * <tt>true</tt>.
         *
         * <p>If the lock is held by another thread then this method
         * will return immediately with the value <tt>false</tt>.
         *
         * @return <tt>true</tt> if the lock was free and was acquired by the
         * current thread, or the lock was already held by the current thread; and
         * <tt>false</tt> otherwise.
         */
        public boolean tryLock() {
            return sync.nonfairTryAcquire(1);
        }

        /**
         * Acquires the lock if it is not held by another thread
         * within the given waiting time and the current thread has
         * not been {@link Thread#interrupt interrupted}.
         *
         * <p>Acquires the lock if it is not held by another thread
         * and returns immediately with the value <tt>true</tt>,
         * setting the lock hold count to one. If this lock has been
         * set to use a fair ordering policy then an available lock
         * <em>will not</em> be acquired if any other threads are
         * waiting for the lock. This is in contrast to the {@link
         * #tryLock()} method. If you want a timed <tt>tryLock</tt>
         * that does permit barging on a fair lock then combine the
         * timed and un-timed forms together:
         *
         * <pre>if (lock.tryLock() || lock.tryLock(timeout, unit) ) { ... }
         * </pre>
         *
         * <p>If the current thread already holds this lock then the
         * hold count is incremented by one and the method returns
         * <tt>true</tt>.
         *
         * <p>If the lock is held by another thread then the current
         * thread becomes disabled for thread scheduling purposes and
         * lies dormant until one of three things happens:
         *
         * <ul>
         *
         * <li>The lock is acquired by the current thread; or
         *
         * <li>Some other thread {@link Thread#interrupt interrupts}
         * the current thread; or
         *
         * <li>The specified waiting time elapses
         *
         * </ul>
         *
         * <p>If the lock is acquired then the value <tt>true</tt> is
         * returned and the lock hold count is set to one.
         *
         * <p>If the current thread:
         *
         * <ul>
         *
         * <li>has its interrupted status set on entry to this method;
         * or
         *
         * <li>is {@link Thread#interrupt interrupted} while acquiring
         * the lock,
         *
         * </ul> 
         *
         * then {@link InterruptedException} is thrown and the current
         * thread's interrupted status is cleared.
         *
         * <p>If the specified waiting time elapses then the value
         * <tt>false</tt> is returned.  If the time is less than or
         * equal to zero, the method will not wait at all.
         *
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock, and over reporting the elapse of the waiting time.
         *
         * @param timeout the time to wait for the lock
         * @param unit the time unit of the timeout argument
         *
         * @return <tt>true</tt> if the lock was free and was acquired
         * by the current thread, or the lock was already held by the
         * current thread; and <tt>false</tt> if the waiting time
         * elapsed before the lock could be acquired.
         *
         * @throws InterruptedException if the current thread is interrupted
         * @throws NullPointerException if unit is null
         *
         */
        public boolean tryLock(long timeout, TimeUnit unit)
            throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(timeout));
        }

        /**
         * Attempts to release this lock.  
         *
         * <p>If the current thread is the holder of this lock then
         * the hold count is decremented. If the hold count is now
         * zero then the lock is released.  If the current thread is
         * not the holder of this lock then {@link
         * IllegalMonitorStateException} is thrown.
         * @throws IllegalMonitorStateException if the current thread does not
         * hold this lock.
         */
        public void unlock() {
            sync.release(1);
        }

        /**
         * Returns a {@link Condition} instance for use with this
         * {@link Lock} instance. 
         * <p>The returned {@link Condition} instance supports the same
         * usages as do the {@link Object} monitor methods ({@link
         * Object#wait() wait}, {@link Object#notify notify}, and {@link
         * Object#notifyAll notifyAll}) when used with the built-in
         * monitor lock.
         *
         * <ul>
         *
         * <li>If this write lock is not held when any {@link
         * Condition} method is called then an {@link
         * IllegalMonitorStateException} is thrown.  (Read locks are
         * held independently of write locks, so are not checked or
         * affected. However it is essentially always an error to
         * invoke a condition waiting method when the current thread
         * has also acquired read locks, since other threads that
         * could unblock it will not be able to access the write
         * lock.)
         *
         * <li>When the condition {@link Condition#await() waiting}
         * methods are called the write lock is released and, before
         * they return, the write lock is reacquired and the lock hold
         * count restored to what it was when the method was called.
         *
         * <li>If a thread is {@link Thread#interrupt interrupted} while
         * waiting then the wait will terminate, an {@link
         * InterruptedException} will be thrown, and the thread's
         * interrupted status will be cleared.
         *
         * <li> Waiting threads are signalled in FIFO order
         *
         * <li>The ordering of lock reacquisition for threads returning
         * from waiting methods is the same as for threads initially
         * acquiring the lock, which is in the default case not specified,
         * but for <em>fair</em> locks favors those threads that have been
         * waiting the longest.
         * 
         * </ul>
         * @return the Condition object
         */
        public Condition newCondition() {
            return sync.newCondition();
        }

        /**
         * Returns a string identifying this lock, as well as its lock
         * state.  The state, in brackets includes either the String
         * &quot;Unlocked&quot; or the String &quot;Locked by&quot;
         * followed by the {@link Thread#getName} of the owning thread.
         * @return a string identifying this lock, as well as its lock state.
         */
        public String toString() {
            Thread owner = sync.getOwner();
            return super.toString()
                    + ((owner == null) ? "[Unlocked]" : "[Locked by thread "
                            + owner.getName() + "]");
        }

    }

    // Instrumentation and status

    /**
     * Returns true if this lock has fairness set true.
     * @return true if this lock has fairness set true.
     */
    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * Returns the thread that currently owns the exclusive lock, or
     * <tt>null</tt> if not owned. Note that the owner may be
     * momentarily <tt>null</tt> even if there are threads trying to
     * acquire the lock but have not yet done so.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive lock monitoring facilities.
     * @return the owner, or <tt>null</tt> if not owned.
     */
    protected Thread getOwner() {
        return sync.getOwner();
    }

    /**
     * Queries the number of read locks held for this lock. This
     * method is designed for use in monitoring system state, not for
     * synchronization control.
     * @return the number of read locks held.
     */
    public int getReadLockCount() {
        return sync.getReadLockCount();
    }

    /**
     * Queries if the write lock is held by any thread. This method is
     * designed for use in monitoring system state, not for
     * synchronization control.
     * @return <tt>true</tt> if any thread holds write lock and 
     * <tt>false</tt> otherwise.
     */
    public boolean isWriteLocked() {
        return sync.isWriteLocked();
    }

    /**
     * Queries if the write lock is held by the current thread. 
     * @return <tt>true</tt> if current thread holds this lock and 
     * <tt>false</tt> otherwise.
     */
    public boolean isWriteLockedByCurrentThread() {
        return sync.isHeldExclusively();
    }

    /**
     * Queries the number of reentrant write holds on this lock by the
     * current thread.  A writer thread has a hold on a lock for
     * each lock action that is not matched by an unlock action.
     *
     * @return the number of holds on this lock by the current thread,
     * or zero if this lock is not held by the current thread.
     */
    public int getWriteHoldCount() {
        return sync.getWriteHoldCount();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire the write lock.  Because the actual set of threads may
     * change dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive lock monitoring facilities.
     * @return the collection of threads
     */
    protected Collection<Thread> getQueuedWriterThreads() {
        return sync.getExclusiveQueuedThreads();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire the read lock.  Because the actual set of threads may
     * change dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive lock monitoring facilities.
     * @return the collection of threads
     */
    protected Collection<Thread> getQueuedReaderThreads() {
        return sync.getSharedQueuedThreads();
    }

    /**
     * Queries whether any threads are waiting to acquire. Note that
     * because cancellations may occur at any time, a <tt>true</tt>
     * return does not guarantee that any other thread will ever
     * acquire.  This method is designed primarily for use in
     * monitoring of the system state.
     *
     * @return true if there may be other threads waiting to acquire
     * the lock.
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * Queries whether the given thread is waiting to acquire this
     * lock. Note that because cancellations may occur at any time, a
     * <tt>true</tt> return does not guarantee that this thread
     * will ever acquire.  This method is designed primarily for use
     * in monitoring of the system state.
     *
     * @param thread the thread
     * @return true if the given thread is queued waiting for this lock.
     * @throws NullPointerException if thread is null
     */
    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    /**
     * Returns an estimate of the number of threads waiting to
     * acquire.  The value is only an estimate because the number of
     * threads may change dynamically while this method traverses
     * internal data structures.  This method is designed for use in
     * monitoring of the system state, not for synchronization
     * control.
     * @return the estimated number of threads waiting for this lock
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire.  Because the actual set of threads may change
     * dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive monitoring facilities.
     * @return the collection of threads
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     * Queries whether any threads are waiting on the given condition
     * associated with the write lock. Note that because timeouts and
     * interrupts may occur at any time, a <tt>true</tt> return does
     * not guarantee that a future <tt>signal</tt> will awaken any
     * threads.  This method is designed primarily for use in
     * monitoring of the system state.
     * @param condition the condition
     * @return <tt>true</tt> if there are any waiting threads.
     * @throws IllegalMonitorStateException if this lock 
     * is not held
     * @throws IllegalArgumentException if the given condition is
     * not associated with this lock
     * @throws NullPointerException if condition null
     */
    public boolean hasWaiters(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        }
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) {
            throw new IllegalArgumentException("not owner");
        }
        return sync
                .hasWaiters((AbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /**
     * Returns an estimate of the number of threads waiting on the
     * given condition associated with the write lock. Note that because
     * timeouts and interrupts may occur at any time, the estimate
     * serves only as an upper bound on the actual number of waiters.
     * This method is designed for use in monitoring of the system
     * state, not for synchronization control.
     * @param condition the condition
     * @return the estimated number of waiting threads.
     * @throws IllegalMonitorStateException if this lock 
     * is not held
     * @throws IllegalArgumentException if the given condition is
     * not associated with this lock
     * @throws NullPointerException if condition null
     */
    public int getWaitQueueLength(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        }
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) {
            throw new IllegalArgumentException("not owner");
        }
        return sync
                .getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /**
     * Returns a collection containing those threads that may be
     * waiting on the given condition associated with the write lock.
     * Because the actual set of threads may change dynamically while
     * constructing this result, the returned collection is only a
     * best-effort estimate. The elements of the returned collection
     * are in no particular order.  This method is designed to
     * facilitate construction of subclasses that provide more
     * extensive condition monitoring facilities.
     * @param condition the condition
     * @return the collection of threads
     * @throws IllegalMonitorStateException if this lock 
     * is not held
     * @throws IllegalArgumentException if the given condition is
     * not associated with this lock
     * @throws NullPointerException if condition null
     */
    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        }
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) {
            throw new IllegalArgumentException("not owner");
        }
        return sync
                .getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /**
     * Returns a string identifying this lock, as well as its lock state.
     * The state, in brackets, includes the String &quot;Write locks =&quot;
     * follwed by the number of reentrantly held write locks, and the
     * String &quot;Read locks =&quot; followed by the number of held
     * read locks.
     * @return a string identifying this lock, as well as its lock state.
     */
    public String toString() {
        int c = sync.getCount();
        int w = exclusiveCount(c);
        int r = sharedCount(c);

        return super.toString() + "[Write locks = " + w + ", Read locks = " + r
                + "]";
    }

}
