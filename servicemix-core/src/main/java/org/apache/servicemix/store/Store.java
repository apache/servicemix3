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
package org.apache.servicemix.store;

import java.io.IOException;

public interface Store {

    String PERSISTENT = "Persistent";
    
    String CLUSTERED = "Clustered";
    
    public boolean hasFeature(String name);
    
    public void store(String id, Object data) throws IOException;
    
    public String store(Object data) throws IOException;
    
    public Object load(String id) throws IOException;
    
    public void remove(String id) throws IOException;
    
}
