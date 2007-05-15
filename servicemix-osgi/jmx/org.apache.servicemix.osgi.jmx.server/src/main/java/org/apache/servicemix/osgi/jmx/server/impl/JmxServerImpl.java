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
package org.apache.servicemix.osgi.jmx.server.impl;

import java.io.ObjectInputStream;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;

import org.apache.servicemix.osgi.jmx.server.JmxServer;

/**
 * 
 * @author gnodet
 */
public class JmxServerImpl implements JmxServer {

    private MBeanServer server;

    public JmxServerImpl(MBeanServer server) {
        this.server = server;
    }

    /**
     * @param name
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#addNotificationListener(javax.management.ObjectName, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException {
        server.addNotificationListener(name, listener, filter, handback);
    }

    /**
     * @param name
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#addNotificationListener(javax.management.ObjectName, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException {
        server.addNotificationListener(name, listener, filter, handback);
    }

    /**
     * @param className
     * @param name
     * @param params
     * @param signature
     * @return
     * @throws ReflectionException
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws MBeanException
     * @throws NotCompliantMBeanException
     * @see javax.management.MBeanServer#createMBean(java.lang.String, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
     */
    public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
        return server.createMBean(className, name, params, signature);
    }

    /**
     * @param className
     * @param name
     * @param loaderName
     * @param params
     * @param signature
     * @return
     * @throws ReflectionException
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws MBeanException
     * @throws NotCompliantMBeanException
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
     */
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
        return server.createMBean(className, name, loaderName, params, signature);
    }

    /**
     * @param className
     * @param name
     * @param loaderName
     * @return
     * @throws ReflectionException
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws MBeanException
     * @throws NotCompliantMBeanException
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName)
     */
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
        return server.createMBean(className, name, loaderName);
    }

    /**
     * @param className
     * @param name
     * @return
     * @throws ReflectionException
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws MBeanException
     * @throws NotCompliantMBeanException
     * @see javax.management.MBeanServer#createMBean(java.lang.String, javax.management.ObjectName)
     */
    public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
        return server.createMBean(className, name);
    }

    /**
     * @param name
     * @param data
     * @return
     * @throws InstanceNotFoundException
     * @throws OperationsException
     * @deprecated
     * @see javax.management.MBeanServer#deserialize(javax.management.ObjectName, byte[])
     */
    public ObjectInputStream deserialize(ObjectName name, byte[] data) throws InstanceNotFoundException, OperationsException {
        return server.deserialize(name, data);
    }

    /**
     * @param className
     * @param data
     * @return
     * @throws OperationsException
     * @throws ReflectionException
     * @deprecated
     * @see javax.management.MBeanServer#deserialize(java.lang.String, byte[])
     */
    public ObjectInputStream deserialize(String className, byte[] data) throws OperationsException, ReflectionException {
        return server.deserialize(className, data);
    }

    /**
     * @param className
     * @param loaderName
     * @param data
     * @return
     * @throws InstanceNotFoundException
     * @throws OperationsException
     * @throws ReflectionException
     * @deprecated
     * @see javax.management.MBeanServer#deserialize(java.lang.String, javax.management.ObjectName, byte[])
     */
    public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] data) throws InstanceNotFoundException, OperationsException, ReflectionException {
        return server.deserialize(className, loaderName, data);
    }

    /**
     * @param name
     * @param attribute
     * @return
     * @throws MBeanException
     * @throws AttributeNotFoundException
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @see javax.management.MBeanServer#getAttribute(javax.management.ObjectName, java.lang.String)
     */
    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        return server.getAttribute(name, attribute);
    }

    /**
     * @param name
     * @param attributes
     * @return
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @see javax.management.MBeanServer#getAttributes(javax.management.ObjectName, java.lang.String[])
     */
    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException {
        return server.getAttributes(name, attributes);
    }

    /**
     * @param loaderName
     * @return
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#getClassLoader(javax.management.ObjectName)
     */
    public ClassLoader getClassLoader(ObjectName loaderName) throws InstanceNotFoundException {
        return server.getClassLoader(loaderName);
    }

    /**
     * @param mbeanName
     * @return
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#getClassLoaderFor(javax.management.ObjectName)
     */
    public ClassLoader getClassLoaderFor(ObjectName mbeanName) throws InstanceNotFoundException {
        return server.getClassLoaderFor(mbeanName);
    }

    /**
     * @return
     * @see javax.management.MBeanServer#getClassLoaderRepository()
     */
    public ClassLoaderRepository getClassLoaderRepository() {
        return server.getClassLoaderRepository();
    }

    /**
     * @return
     * @see javax.management.MBeanServer#getDefaultDomain()
     */
    public String getDefaultDomain() {
        return server.getDefaultDomain();
    }

    /**
     * @return
     * @see javax.management.MBeanServer#getDomains()
     */
    public String[] getDomains() {
        return server.getDomains();
    }

    /**
     * @return
     * @see javax.management.MBeanServer#getMBeanCount()
     */
    public Integer getMBeanCount() {
        return server.getMBeanCount();
    }

    /**
     * @param name
     * @return
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @see javax.management.MBeanServer#getMBeanInfo(javax.management.ObjectName)
     */
    public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
        return server.getMBeanInfo(name);
    }

    /**
     * @param name
     * @return
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#getObjectInstance(javax.management.ObjectName)
     */
    public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException {
        return server.getObjectInstance(name);
    }

    /**
     * @param className
     * @param params
     * @param signature
     * @return
     * @throws ReflectionException
     * @throws MBeanException
     * @see javax.management.MBeanServer#instantiate(java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public Object instantiate(String className, Object[] params, String[] signature) throws ReflectionException, MBeanException {
        return server.instantiate(className, params, signature);
    }

    /**
     * @param className
     * @param loaderName
     * @param params
     * @param signature
     * @return
     * @throws ReflectionException
     * @throws MBeanException
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#instantiate(java.lang.String, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
     */
    public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, MBeanException, InstanceNotFoundException {
        return server.instantiate(className, loaderName, params, signature);
    }

    /**
     * @param className
     * @param loaderName
     * @return
     * @throws ReflectionException
     * @throws MBeanException
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#instantiate(java.lang.String, javax.management.ObjectName)
     */
    public Object instantiate(String className, ObjectName loaderName) throws ReflectionException, MBeanException, InstanceNotFoundException {
        return server.instantiate(className, loaderName);
    }

    /**
     * @param className
     * @return
     * @throws ReflectionException
     * @throws MBeanException
     * @see javax.management.MBeanServer#instantiate(java.lang.String)
     */
    public Object instantiate(String className) throws ReflectionException, MBeanException {
        return server.instantiate(className);
    }

    /**
     * @param name
     * @param operationName
     * @param params
     * @param signature
     * @return
     * @throws InstanceNotFoundException
     * @throws MBeanException
     * @throws ReflectionException
     * @see javax.management.MBeanServer#invoke(javax.management.ObjectName, java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException {
        return server.invoke(name, operationName, params, signature);
    }

    /**
     * @param name
     * @param className
     * @return
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#isInstanceOf(javax.management.ObjectName, java.lang.String)
     */
    public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException {
        return server.isInstanceOf(name, className);
    }

    /**
     * @param name
     * @return
     * @see javax.management.MBeanServer#isRegistered(javax.management.ObjectName)
     */
    public boolean isRegistered(ObjectName name) {
        return server.isRegistered(name);
    }

    /**
     * @param name
     * @param query
     * @return
     * @see javax.management.MBeanServer#queryMBeans(javax.management.ObjectName, javax.management.QueryExp)
     */
    public Set queryMBeans(ObjectName name, QueryExp query) {
        return server.queryMBeans(name, query);
    }

    /**
     * @param name
     * @param query
     * @return
     * @see javax.management.MBeanServer#queryNames(javax.management.ObjectName, javax.management.QueryExp)
     */
    public Set queryNames(ObjectName name, QueryExp query) {
        return server.queryNames(name, query);
    }

    /**
     * @param object
     * @param name
     * @return
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws NotCompliantMBeanException
     * @see javax.management.MBeanServer#registerMBean(java.lang.Object, javax.management.ObjectName)
     */
    public ObjectInstance registerMBean(Object object, ObjectName name) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        return server.registerMBean(object, name);
    }

    /**
     * @param name
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
        server.removeNotificationListener(name, listener, filter, handback);
    }

    /**
     * @param name
     * @param listener
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener)
     */
    public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException {
        server.removeNotificationListener(name, listener);
    }

    /**
     * @param name
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
        server.removeNotificationListener(name, listener, filter, handback);
    }

    /**
     * @param name
     * @param listener
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName, javax.management.ObjectName)
     */
    public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException {
        server.removeNotificationListener(name, listener);
    }

    /**
     * @param name
     * @param attribute
     * @throws InstanceNotFoundException
     * @throws AttributeNotFoundException
     * @throws InvalidAttributeValueException
     * @throws MBeanException
     * @throws ReflectionException
     * @see javax.management.MBeanServer#setAttribute(javax.management.ObjectName, javax.management.Attribute)
     */
    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        server.setAttribute(name, attribute);
    }

    /**
     * @param name
     * @param attributes
     * @return
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @see javax.management.MBeanServer#setAttributes(javax.management.ObjectName, javax.management.AttributeList)
     */
    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException {
        return server.setAttributes(name, attributes);
    }

    /**
     * @param name
     * @throws InstanceNotFoundException
     * @throws MBeanRegistrationException
     * @see javax.management.MBeanServer#unregisterMBean(javax.management.ObjectName)
     */
    public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException {
        server.unregisterMBean(name);
    }

}
