/**
 * <a href="http://servicemix.org">ServiceMix: The open source ESB</a> 
 * 
 * Copyright 2005 RAJD Consultancy Ltd
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

package org.servicemix.jbi.framework;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Loadsfrom parent class loader first
 * 
 * @version $Revision$
 */
public class ParentFirstClassLoader extends InstallationClassLoader {
    private static final Log log = LogFactory.getLog(ParentFirstClassLoader.class);
   

    /**
     * Constructor
     * 
     * @param urls
     * @param parent
     */
    public ParentFirstClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
    
    

    /**
     * load a class
     * 
     * @param name
     * @param resolve
     * @return Class
     * @throws ClassNotFoundException
     */
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        
        Class result = null;
        try {
            result = super.loadClass(name, resolve);
        }
        catch (ClassNotFoundException e) {
        }
        if (result == null) {
        	try {
        		result = parentLoader.loadClass(name);
        		if (result != null && resolve) {
        			resolveClass(result);
        		}
        	}
        	catch (ClassNotFoundException e) {
        	}
        }
        if (result == null) {
            try {
                result = findClass(name);
                if (result != null && resolve) {
                    resolveClass(result);
                }
            }
            catch (ClassNotFoundException e) {
            }
        }
        return result;
    }

    /**
     * Get a resource
     * 
     * @param name
     * @return the resource
     */
    public URL getResource(String name) {
        URL result = null;
        result = parentLoader.getResource(name);
        if (result == null) {
            result = findResource(name);
        }
        return result;
    }

    /**
     * Get a Resource as a Stream
     * 
     * @param name
     * @return InputStream
     */
    public InputStream getResourceAsStream(String name) {
        InputStream result = parentLoader.getResourceAsStream(name);
        if (result == null) {
            URL url = findResource(name);
            if (url != null) {
                try {
                    result = url.openStream();
                }
                catch (IOException e) {
                    log.warn("failed to open stream for URL: " + url, e);
                }
            }
        }
        return result;
    }
}
