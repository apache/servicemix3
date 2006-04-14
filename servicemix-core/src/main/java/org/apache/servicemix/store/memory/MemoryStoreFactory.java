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
package org.apache.servicemix.store.memory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.util.IdGenerator;
import org.apache.servicemix.store.Store;
import org.apache.servicemix.store.StoreFactory;

public class MemoryStoreFactory implements StoreFactory {

    private IdGenerator idGenerator = new IdGenerator();
    private Map stores = new HashMap();
    
    /* (non-Javadoc)
     * @see org.apache.servicemix.store.ExchangeStoreFactory#get(java.lang.String)
     */
    public synchronized Store get(String name) throws IOException {
        MemoryStore store = (MemoryStore) stores.get(name);
        if (store == null) {
            store = new MemoryStore(idGenerator);
            stores.put(name, store);
        }
        return store;
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.store.ExchangeStoreFactory#release(org.apache.servicemix.store.ExchangeStore)
     */
    public synchronized void release(Store store) throws IOException {
        stores.remove(store);
    }
    

}
