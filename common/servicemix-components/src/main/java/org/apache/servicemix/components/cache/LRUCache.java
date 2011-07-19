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
package org.apache.servicemix.components.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents an LRUCache of a fixed maximum size which by default will
 * remove items based on access order but can be used to use insertion order
 *
 * @version $Revision$
 */
public class LRUCache extends LinkedHashMap {

    private static final long serialVersionUID = -5754338187296859149L;

    protected static final int DEFAULT_INITIAL_CAPACITY = 1000;
    protected static final float DEFAULT_LOAD_FACTOR = (float) 0.75;

    private int maxSize;

    public LRUCache(int initialCapacity, float loadFactor, boolean accessOrder, int maxSize) {
        super(initialCapacity, loadFactor, accessOrder);
        this.maxSize = maxSize;
    }

    public LRUCache(int maxSize) {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true, maxSize);
    }

    public LRUCache(int maxSize, boolean accessOrder) {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, accessOrder, maxSize);
    }

    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > maxSize;
    }

}
