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
package org.apache.servicemix.jbi.config;

import java.io.IOException;
import java.util.List;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * A debugging version of the class to add tracing of the generated XML.
 *
 * @version $Revision$
 */
public class DebugClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {
    public DebugClassPathXmlApplicationContext(String id) throws BeansException {
        super(id);
        //super(new String[] {id}, false);
    }

    public DebugClassPathXmlApplicationContext(String id, List processors) throws BeansException {
        super(id, processors);
        //super(new String[] {id}, false);
    }

    public DebugClassPathXmlApplicationContext(String[] strings) throws BeansException {
        super(strings);
    }

    public DebugClassPathXmlApplicationContext(String[] strings, boolean b) throws BeansException {
        super(strings, b);
    }

    public DebugClassPathXmlApplicationContext(String[] strings, ApplicationContext applicationContext) throws BeansException {
        super(strings, applicationContext);
    }

    public DebugClassPathXmlApplicationContext(String[] strings, boolean b, ApplicationContext applicationContext) throws BeansException {
        super(strings, b, applicationContext);
    }

    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException {
        super.loadBeanDefinitions(beanFactory);
        /*
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        beanDefinitionReader.setParserClass(DebugXmlBeanDefinitionParser.class);
        beanDefinitionReader.setResourceLoader(this);
        beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));
        initBeanDefinitionReader(beanDefinitionReader);
        loadBeanDefinitions(beanDefinitionReader);
        */
    }
}
