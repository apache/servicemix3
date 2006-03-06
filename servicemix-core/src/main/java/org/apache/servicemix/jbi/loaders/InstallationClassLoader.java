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
package org.apache.servicemix.jbi.loaders;

import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArrayList;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for the custom class loaders
 * 
 * @version $Revision$
 */
public abstract class InstallationClassLoader extends URLClassLoader {
    /**
     * handle to parent of this class loader
     */
    private List sharedlibs = new CopyOnWriteArrayList();
    protected ClassLoader parentLoader;

    protected InstallationClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        parentLoader = (parent != null) ? parent : getSystemClassLoader();
    }
    
    /**
     * Utility method to dump urls to system.out
     */
    public void dumpURLS(){
        for (int i =0; i < getURLs().length; i++){
            System.out.println(getURLs()[i]);
        }
    }

    /**
     * Add a shared library
     * 
     * @param cl
     */
    public void addSharedLibraryLoader(ClassLoader cl) {
        sharedlibs.add(cl);
    }

    /**
     * Remove a shared library
     * 
     * @param cl
     */
    public void removeSharedLibraryLoader(ClassLoader cl) {
        sharedlibs.add(cl);
    }

    /**
     * @param name
     * @return Class
     * @throws ClassNotFoundException
     */
    public Class findClass(String name) throws ClassNotFoundException {
        Class result = null;
        for (Iterator i = sharedlibs.iterator();i.hasNext();) {
            try {
                ClassLoader cl = (ClassLoader) i.next();
                result = cl.loadClass(name);
                if (result != null) {
                    break;
                }
            }
            catch (ClassNotFoundException cnfe) {
            }
        }
        if (result == null) {
            result = super.findClass(name);
        }
        return result;
    }

    /**
     * @param name
     * @return the loaded Class
     * @throws ClassNotFoundException
     */
    public Class loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
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
        Class result = findLoadedClass(name);
        if (result == null) {
            //is it a reserved class ?
            if (name.startsWith("java.") || name.startsWith("javax.")) {
                result = parentLoader.loadClass(name);
            }
        }
        else if (resolve) {
            resolveClass(result);
        }
        return result;
    }
}
