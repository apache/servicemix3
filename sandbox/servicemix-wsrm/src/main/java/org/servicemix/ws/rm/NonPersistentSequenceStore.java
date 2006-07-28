/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.servicemix.ws.rm;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

import org.apache.activemq.util.IdGenerator;
import org.xmlsoap.schemas.ws._2005._02.rm.Identifier;

/**
 * A simple implementation of the {@link SequenceStore} which does not persist messages
 * but is useful for testing.
 * 
 * @version $Revision$
 */
public class NonPersistentSequenceStore implements SequenceStore {

    private final IdGenerator idGenerator = new IdGenerator();
    private final ConcurrentHashMap sequences = new ConcurrentHashMap();
    
    public void create(Sequence s) {
        String id = idGenerator.generateId();
        Identifier identifier = new Identifier();
        identifier.setValue(id);
        s.setIdentifier(identifier);
        sequences.put(id,s);
    }

    public void delete(Identifier identifier) {
        sequences.remove(identifier.getValue());
    }

    public Sequence retrieve(Identifier identifier) {
        return (Sequence) sequences.get(identifier.getValue());
    }

    public void update(Sequence s) {
        sequences.put(s.getIdentifier().getValue(), s);
    }
}
