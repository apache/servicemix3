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
package org.apache.servicemix.xbean;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.xbean.classloader.JarFileClassLoader;
import org.apache.xbean.spring.context.SpringApplicationContext;
import org.apache.xbean.spring.context.SpringXmlPreprocessor;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * An advanced xml preprocessor that will create a default classloader for the SU if none
 * is configured.
 * 
 * @author gnodet
 */
public class ClassLoaderXmlPreprocessor implements SpringXmlPreprocessor {

    public static final String CLASSPATH_XML = "classpath.xml";
    public static final String LIB_DIR = "/lib";
    
    private final File root;
    
    public ClassLoaderXmlPreprocessor(File root) {
        this.root = root;
    }

    public void preprocess(SpringApplicationContext applicationContext, XmlBeanDefinitionReader reader, Document document) {
        // determine the classLoader
        ClassLoader classLoader;
        NodeList classpathElements = document.getDocumentElement().getElementsByTagName("classpath");
        if (classpathElements.getLength() == 0) {
            // Check if a classpath.xml file exists in the root of the SU
            URL url = getResource(CLASSPATH_XML);
            if (url != null) {
                try {
                    DocumentBuilder builder = new SourceTransformer().createDocumentBuilder();
                    Document doc = builder.parse(url.toString());
                    classLoader = getClassLoader(applicationContext, reader, doc);
                } catch (Exception e) {
                    throw new FatalBeanException("Unable to load classpath.xml file", e);
                }
            } else {
                try {
                    URL[] urls = getDefaultLocations();
                    ClassLoader parentLoader = getParentClassLoader(applicationContext);
                    classLoader = new JarFileClassLoader(applicationContext.getDisplayName(), urls, parentLoader);
                    // assign the class loader to the xml reader and the
                    // application context
                } catch (Exception e) {
                    throw new FatalBeanException("Unable to create default classloader for SU", e);
                }
            }
        } else {
            classLoader = getClassLoader(applicationContext, reader, document);
        }
        reader.setBeanClassLoader(classLoader);
        applicationContext.setClassLoader(classLoader);
        Thread.currentThread().setContextClassLoader(classLoader);
    }
    
    /**
     * <p>Replaces a String with another String inside a larger String,
     * for the first <code>max</code> values of the search String.</p>
     * 
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     * 
     * @param text text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement the String to replace it with, may be null
     * @param max maximum number of value to replace, or <code>-1</code> if no maximum
     * @return the text with any replacements processed, <code>null</code> if null String input
     */
    private static String replaceString(String text, String searchString, String replacement, int max) {
        if (text == null || text.length() == 0
                || searchString == null || searchString.length() == 0
                || replacement == null
                || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == -1) {
            return text;
        }
        int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = increase < 0 ? 0 : increase;
        increase *= max < 0 ? 16 : (max > 64 ? 64 : max);
        StringBuffer buffer = new StringBuffer(text.length() + increase);
        while (end != -1) {
            buffer.append(text.substring(start, end)).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(searchString, start);
        }
        buffer.append(text.substring(start));
        return buffer.toString();
    }

    /**
     * <p>
     * Get an URL from a String location.
     * </p>
     * 
     * @param location
     * @return
     */
    protected URL getResource(String location) {
        URI uri = root.toURI().resolve(location);
        File file = new File(uri);
        
        if (!file.canRead()) {
            return null;
        }
        
        try {
            return file.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed resource " + uri);
        }
    }
    
    /**
     * <p>
     * Get the URLs for a classpath location. This method supports standard
     * relative file location, <code>file:</code> URL location (including entries
     * regexp filter support), <code>jar:</code> URL location (including entries
     * regexp filter support).
     * </p>
     * 
     * @param location the location where to get the URLs
     * @return the URLs list
     */
    protected List<URL> getResources(String location) {
        // step 1: replace system properties in the location
        Properties systemProperties = System.getProperties();
        for (Iterator systemPropertiesIterator = systemProperties.keySet().iterator(); systemPropertiesIterator.hasNext();) {
            String property = (String) systemPropertiesIterator.next();
            String value = systemProperties.getProperty(property);
            location = ClassLoaderXmlPreprocessor.replaceString(location, "${" + property + "}", value, -1);
        }
        // step 2: apply regexp search for file: and jar: protocol based URL
        if (location.startsWith("jar:")) {
            return this.getJarResources(location);
        } else if (location.startsWith("file:")) {
            return this.getFileResources(location);
        } else {
            // relative location
            List<URL> urls = new LinkedList<URL>();
            URI uri = root.toURI().resolve(location);
            File file = new File(uri);
            if (file.canRead()) {
                try {
                    urls.add(file.toURL());
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("Malformed resource location " + uri);
                }
            }
            return urls;
        }
    }
    
    /**
     * <p>
     * Get the URLs for a jar: protocol based location. This method supports
     * regexp to add several entries.
     * </p>
     * 
     * <pre>
     * jar:file:/path/to/my.ear!/entry.jar
     * jar:http:/path/to/my.ear!/en*.jar
     * </pre>
     * 
     * @param location
     * @return
     */
    protected List<URL> getJarResources(String location) {
        List<URL> urls = new LinkedList<URL>();
        // get the !/ separator index
        int separatorIndex = location.indexOf("!/");
        if (separatorIndex == -1) {
            throw new IllegalArgumentException("The jar URL " + location + " is not valid. !/ separator not found.");
        }
        // extract the jar location
        String jarLocation = location.substring(4, separatorIndex);
        System.out.println("jarLocation: " + jarLocation);
        // extract the entry location
        String entryLocation = location.substring(separatorIndex + 2);
        System.out.println("entryLocation: " + entryLocation);
        if (jarLocation == null || jarLocation.trim().length() < 1
                || entryLocation == null || entryLocation.trim().length() < 1) {
            throw new IllegalArgumentException("The jar URL " + location + " is not valid. Jar URL or entry not found.");
        }
        // construct the Jar URL
        JarInputStream jarInputStream;
        try {
            jarInputStream = new JarInputStream(new URL(jarLocation).openStream());
        } catch (Exception e) {
            throw new IllegalArgumentException("The jar URL is not valid " + jarLocation + ".", e);
        }
        // iterate into the entries
        try {
            ZipEntry entry = jarInputStream.getNextEntry();
            while (entry != null) {
                if (entry.getName().matches(entryLocation)) {
                    // the entry matches the regexp, construct the entry URL
                    String entryUrl = "jar:" + jarLocation + "!/" + entry.getName();
                    // add the entry URL into the URLs list
                    urls.add(new URL(entryUrl));
                }
                entry = jarInputStream.getNextEntry();
            }
        } catch (IOException ioException) {
            throw new IllegalArgumentException("Can't read jar entries", ioException);
        }
        return urls;
    }
    
    /**
     * <p>
     * Get the URLs for a file: protocol based location. This method supports
     * regexp to add several entries.
     * </p>
     * 
     * <pre>
     * file:/path/to/my.jar
     * file:/path/to/dir/en*.jar
     * </pre>
     * 
     * @param location
     * @return
     */
    protected List<URL> getFileResources(String location) {
        List<URL> urls = new LinkedList<URL>();
        int starIndex = location.indexOf("*");
        if (starIndex == -1) {
            // no regexp pattern found
            try {
                urls.add(new URL(location));
            } catch (MalformedURLException urlException) {
                throw new IllegalArgumentException("Invalid URL " + location);
            }  
        } else {
            // user has defined a regexp file name filter
            int lastSeparatorIndex = location.lastIndexOf("/");
            if (starIndex < lastSeparatorIndex) {
                throw new IllegalArgumentException("Regexp is supported only on files name, not on directories.");
            }
            String dirPath = location.substring(0, lastSeparatorIndex);
            File dir = new File(dirPath);
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException("The regexp basedir is not a directory.");
            }
            File[] entries = dir.listFiles();
            String fileNameRegexp = location.substring(lastSeparatorIndex);
            for (int i = 0; i < entries.length; i++) {
                File entry = entries[i];
                if (entry.getName().matches(fileNameRegexp)) {
                    try {
                        urls.add(new URL(dirPath + "/" + entry.getName()));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid URL " + dirPath + "/" + entry.getName(), e);
                    }
                }
            }
        }
        return urls;
    }
    
    protected URL[] getDefaultLocations() {
        try {
            File[] jars = new File(root, LIB_DIR).listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    name = name.toLowerCase();
                    return name.endsWith(".jar") || name.endsWith(".zip");
                }
            });
            URL[] urls = new URL[jars != null ? jars.length + 1 : 1];
            urls[0] = root.toURL();
            if (jars != null) {
                for (int i = 0; i < jars.length; i++) {
                    urls[i + 1] = jars[i].toURL();
                }
            }
            return urls;
        } catch (MalformedURLException e) {
            throw new FatalBeanException("Unable to get default classpath locations", e);
        }
    }
    
    protected ClassLoader getClassLoader(SpringApplicationContext applicationContext, XmlBeanDefinitionReader reader, Document document) {
        // determine the classLoader
        ClassLoader classLoader;
        NodeList classpathElements = document.getDocumentElement().getElementsByTagName("classpath");
        if (classpathElements.getLength() < 1) {
            classLoader = getParentClassLoader(applicationContext);
        } else if (classpathElements.getLength() > 1) {
            throw new FatalBeanException("Expected only classpath element but found " + classpathElements.getLength());
        } else {
            Element classpathElement = (Element) classpathElements.item(0);
            
            String fileDelegation = classpathElement.getAttribute("file");
            if (fileDelegation != null) {
                URL url = getResource(fileDelegation);
                if (url != null) {
                    try {
                        DocumentBuilder builder = new SourceTransformer().createDocumentBuilder();
                        Document doc = builder.parse(url.toString());
                        classLoader = getClassLoader(applicationContext, reader, doc);
                        return classLoader;
                    } catch (Exception e) {
                        throw new FatalBeanException("Unable to load " + url + " file.", e);
                    }
                }
            }
            
            // Delegation mode
            boolean inverse = false;
            String inverseAttr = classpathElement.getAttribute("inverse");
            if (inverseAttr != null && "true".equalsIgnoreCase(inverseAttr)) {
                inverse = true;
            }

            // build hidden classes
            List<String> hidden = new ArrayList<String>();
            NodeList hiddenElems = classpathElement.getElementsByTagName("hidden");
            for (int i = 0; i < hiddenElems.getLength(); i++) {
                Element hiddenElement = (Element) hiddenElems.item(i);
                String pattern = ((Text) hiddenElement.getFirstChild()).getData().trim();
                hidden.add(pattern);
            }

            // build non overridable classes
            List<String> nonOverridable = new ArrayList<String>();
            NodeList nonOverridableElems = classpathElement.getElementsByTagName("nonOverridable");
            for (int i = 0; i < nonOverridableElems.getLength(); i++) {
                Element nonOverridableElement = (Element) nonOverridableElems.item(i);
                String pattern = ((Text) nonOverridableElement.getFirstChild()).getData().trim();
                nonOverridable.add(pattern);
            }

            // build the classpath
            List<String> classpath = new ArrayList<String>();
            NodeList locations = classpathElement.getElementsByTagName("location");
            for (int i = 0; i < locations.getLength(); i++) {
                Element locationElement = (Element) locations.item(i);
                String location = ((Text) locationElement.getFirstChild()).getData().trim();
                classpath.add(location);
            }
            
            // convert the paths to URLS
            URL[] urls;
            if (classpath.size() != 0) {
                List<URL> urlsList = new LinkedList<URL>();
                for (ListIterator<String> iterator = classpath.listIterator(); iterator.hasNext();) {
                    String location = iterator.next();
                    List<URL> locationUrls = getResources(location);
                    for (URL url : locationUrls) {
                        urlsList.add(url);
                    }
                }
                urls = urlsList.toArray(new URL[urlsList.size()]);
            } else {
                urls = getDefaultLocations();
            }

            // create the classloader
            List<ClassLoader> parents = new ArrayList<ClassLoader>();
            parents.add(getParentClassLoader(applicationContext));
            classLoader = new JarFileClassLoader(applicationContext.getDisplayName(),
                                                 urls, 
                                                 parents.toArray(new ClassLoader[parents.size()]),
                                                 inverse,
                                                 hidden.toArray(new String[hidden.size()]),
                                                 nonOverridable.toArray(new String[nonOverridable.size()]));

            // remove the classpath element so Spring doesn't get confused
            document.getDocumentElement().removeChild(classpathElement);
        }
        return classLoader;
    }

    private ClassLoader getParentClassLoader(SpringApplicationContext applicationContext) {
        ClassLoader classLoader = applicationContext.getClassLoader();
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        return classLoader;
    }
    
}
