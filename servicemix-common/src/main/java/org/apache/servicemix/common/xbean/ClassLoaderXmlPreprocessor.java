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
package org.apache.servicemix.common.xbean;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.xbean.classloader.JarFileClassLoader;
import org.apache.xbean.server.repository.FileSystemRepository;
import org.apache.xbean.server.repository.Repository;
import org.apache.xbean.server.spring.loader.SpringLoader;
import org.apache.xbean.spring.context.SpringApplicationContext;
import org.apache.xbean.spring.context.SpringXmlPreprocessor;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * An advanced xml preprocessor that will create a default classloader for the SU if none
 * is configured.
 * 
 * @author gnodet
 */
public class ClassLoaderXmlPreprocessor implements SpringXmlPreprocessor {

    public static final String CLASSPATH_XML = "classpath.xml";
    public static final String LIB_DIR = "/lib";
    
    private final FileSystemRepository repository;
    private final SpringXmlPreprocessor preprocessor;
    
    public ClassLoaderXmlPreprocessor(Repository repository) {
        if (repository instanceof FileSystemRepository == false) {
            throw new IllegalArgumentException("repository must be a FileSystemRepository");
        }
        this.repository = (FileSystemRepository) repository;
        this.preprocessor = new org.apache.xbean.server.spring.configuration.ClassLoaderXmlPreprocessor(repository);
    }

    public void preprocess(SpringApplicationContext applicationContext, XmlBeanDefinitionReader reader, Document document) {
        // determine the classLoader
        NodeList classpathElements = document.getDocumentElement().getElementsByTagName("classpath");
        if (classpathElements.getLength() == 0) {
            // Check if a classpath.xml file exists in the root of the SU
            URL url = repository.getResource(CLASSPATH_XML);
            if (url != null) {
                try {
                    DocumentBuilder builder = new SourceTransformer().createDocumentBuilder();
                    Document doc = builder.parse(url.toString());
                    preprocessor.preprocess(applicationContext, reader, doc);
                } catch (Exception e) {
                    throw new BeanDefinitionStoreException("Unable to load classpath.xml file", e);
                }
            } else {
                try {
                    File root = repository.getRoot();
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
                            urls[i+1] = jars[i].toURL();
                        }
                    }
                    ClassLoader parentLoader = getClassLoader(applicationContext);
                    ClassLoader classLoader = new JarFileClassLoader(
                                                         applicationContext.getDisplayName(), 
                                                         urls, 
                                                         parentLoader);
                    // assign the class loader to the xml reader and the application context
                    reader.setBeanClassLoader(classLoader);
                    applicationContext.setClassLoader(classLoader);
                    Thread.currentThread().setContextClassLoader(classLoader);
                } catch (Exception e) {
                    throw new BeanDefinitionStoreException("Unable to create default classloader for SU", e);
                }
            }
        } else {
            preprocessor.preprocess(applicationContext, reader, document);
        }
    }

    private static ClassLoader getClassLoader(SpringApplicationContext applicationContext) {
        ClassLoader classLoader = applicationContext.getClassLoader();
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (classLoader == null) {
            classLoader = SpringLoader.class.getClassLoader();
        }
        return classLoader;
    }
    
}
