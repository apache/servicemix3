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
import java.util.Map;

import org.apache.servicemix.id.IdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.store.Store;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

/**
 * A simple memory store implementation based on a simple map.
 * This store is neither clusterable, nor persistent, nor transactional.
 * 
 * @author gnodet
 */
public class MemoryStore implements Store {
    
    private static final Log log = LogFactory.getLog(MemoryStore.class);

    private Map datas = new ConcurrentHashMap();
    private IdGenerator idGenerator;
    
    public MemoryStore(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
    
    public boolean hasFeature(String name) {
        return false;
    }

    public void store(String id, Object data) throws IOException {
        log.debug("Storing object with id: " + id);
        datas.put(id, data);
    }
    
    public String store(Object data) throws IOException {
        String id = idGenerator.generateId();
        store(id, data);
        return id;
    }

    public Object load(String id) throws IOException {
        log.debug("Loading object with id: " + id);
        return datas.remove(id);
    }
    
}
