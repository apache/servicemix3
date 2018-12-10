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
package org.apache.servicemix.bpe.uuid;

import org.apache.activemq.util.IdGenerator;
import org.apache.ode.bpe.util.BPEProperties;
import org.apache.ode.bpe.uuid.UUIDService;
import org.apache.ode.bpe.uuid.UUIDServiceException;


public class ActiveMQUUIDService implements UUIDService {

    private IdGenerator idGenerator;
    
    public ActiveMQUUIDService() {
        idGenerator = new IdGenerator();
    }

    public String getUUID() {
        return idGenerator.generateId();
    }

    public void init(BPEProperties props) throws UUIDServiceException {
    }

    public void close() {
    }

}
