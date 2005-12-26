/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.apache.servicemix.jbi.jaxp;

import org.springframework.core.io.Resource;

import javax.xml.transform.stream.StreamSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * A JAXP {@link StreamSource} which uses a Spring {@link Resource} as the source of the input stream.
 * This implementation is re-entrant and can be used as many times as required to parse XML.
 * 
 * @version $Revision$
 */
public class ResourceSource extends StreamSource {

    private final Resource resource;

    public ResourceSource(Resource resource) {
        this.resource = resource;
    }

    public InputStream getInputStream() {
        try {
            return resource.getInputStream();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to open resource: " + resource + ". Reason: " + e, e); 
        }
    }

    public Reader getReader() {
        return new InputStreamReader(getInputStream());
    }

    
}
