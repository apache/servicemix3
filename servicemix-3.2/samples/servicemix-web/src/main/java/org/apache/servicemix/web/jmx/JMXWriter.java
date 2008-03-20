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
package org.apache.servicemix.web.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * A useful class for turning JMX statistics into XML and XHTML
 *
 * @version $Revision: 356269 $
 */
public class JMXWriter {
    private static final Log log = LogFactory.getLog(JMXWriter.class);

    private PrintWriter writer;
    private ManagementContext managementContext;
    private String unknownValue = "Unknown";

    public JMXWriter(PrintWriter writer, ManagementContext context) {
        this.writer = writer;
        managementContext = context;
    }

    public MBeanServer getMBeanServer() {
        return managementContext.getMBeanServer();
    }

    public ManagementContext getManagementContext() {
        return managementContext;
    }

    public void setManagementContext(ManagementContext managementContext) {
        this.managementContext = managementContext;
    }

    public void outputHtmlNamesByDomain(Collection names) throws IOException {
        Map map = new TreeMap();
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            ObjectName name = (ObjectName) iter.next();
            String domain = name.getDomain();
            List list = (List) map.get(domain);
            if (list == null) {
                list = new ArrayList();
                map.put(domain, list);
            }
            list.add(name);
        }
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String domain = (String) entry.getKey();
            names = (List) entry.getValue();

            writer.print("<li>");
            writer.print(domain);

            writer.print("<ul>");
            outputHtmlNamesByProperty(names, "Type");
            writer.print("</ul>");
            writer.print("</li>");
        }
    }

    public void outputHtmlNamesByProperty(Collection names, String property) throws IOException {
        Map map = new TreeMap();
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            ObjectName name = (ObjectName) iter.next();
            String propertyValue = name.getKeyProperty(property);
            if (propertyValue == null) {
                propertyValue = unknownValue;
            }
            List list = (List) map.get(propertyValue);
            if (list == null) {
                list = new ArrayList();
                map.put(propertyValue, list);
            }
            list.add(name);
        }
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String propertyValue = (String) entry.getKey();
            names = (List) entry.getValue();

            if (names.size() > 1) {
                writer.print("<li><a href='");
                printDetailURL(property, propertyValue);
                writer.print("'>");
                writer.print(propertyValue);
                writer.print("</a><ul>");

                // outputHtmlNames(names);
                outputHtmlNamesSortedByShortName(names);
                writer.print("</ul>");
                writer.print("</li>");
            }
            else if (names.size() == 1) {
                ObjectName name = (ObjectName) ((List) names).get(0);
                outputHtmlName(name, propertyValue);
            }
        }
    }

    public void outputHtmlNamesSortedByShortName(Collection names) throws IOException {
        Map map = new TreeMap();
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            ObjectName name = (ObjectName) iter.next();
            String propertyValue = getShortName(name);
            map.put(propertyValue, name);
        }
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String description = (String) entry.getKey();
            ObjectName name = (ObjectName) entry.getValue();
            outputHtmlName(name, description);
        }
    }

    public void outputHtmlNames(Collection names) throws IOException {
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            outputHtmlNames((ObjectName) iter.next());
        }
    }

    public void outputHtmlNames(ObjectName name) throws IOException {
        outputHtmlName(name, getShortName(name));
    }

    public void outputHtmlName(ObjectName name, String shortName) throws IOException {
        writer.print("<li><a href='");
        printDetailURL(name);

        /*
         * Map properties = name.getKeyPropertyList(); for (Iterator iter =
         * properties.entrySet().iterator(); iter.hasNext();) { Map.Entry entry =
         * (Map.Entry) iter.next(); writer.print("<property name='");
         * writer.print(entry.getKey()); writer.print("'>");
         * writer.print(entry.getValue()); writer.println("</property>"); }
         */

        writer.print("'>");
        // writer.print(name.getCanonicalKeyPropertyListString());
        writer.print(shortName);
        writer.print("</a></li>");
    }

    /**
     * Returns a short descriptive name of the ObjectName without the domain
     */
    protected String getShortName(ObjectName name) {
        // TODO Auto-generated method stub
        String answer = name.toString();
        int idx = answer.indexOf(':');
        if (idx >= 0) {
            return answer.substring(idx + 1);
        }
        return answer;
    }

    public void outputNames(Collection names) throws IOException {
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            outputNames((ObjectName) iter.next());
        }
    }

    public void outputNames(ObjectName name) throws IOException {
        writer.print("<mbean name='");
        writer.print(name.getCanonicalName());
        writer.print("' domain='");
        writer.print(name.getDomain());
        writer.println("'>");
        Map properties = name.getKeyPropertyList();
        for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            writer.print("<property name='");
            writer.print(entry.getKey());
            writer.print("'>");
            writer.print(entry.getValue());
            writer.println("</property>");
        }
        writer.println("</mbean>");
    }

    public void outputDetail(Set names) throws IOException, JMException {

        for (Iterator iter = names.iterator(); iter.hasNext();) {
            outputDetail((ObjectName) iter.next());
        }
    }

    public void outputDetail(ObjectName name) throws JMException, IOException {

        MBeanServer beanServer = getMBeanServer();
        MBeanInfo beanInfo = beanServer.getMBeanInfo(name);
        writer.print("<mbean name='");
        writer.print(name.getCanonicalName());
        writer.print("' domain='");
        writer.print(name.getDomain());
        writer.println("'>");

        MBeanAttributeInfo[] attributes = beanInfo.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            MBeanAttributeInfo info = attributes[i];
            if (info.isReadable()) {
                String attributeName = info.getName();
                Object value = getAttributeValue(name, attributeName);
                if (value != null) {
                    writer.print("<attribute name='");
                    writer.print(attributeName);
                    writer.print("' type='");
                    writer.print(info.getType());
                    writer.print("'>");
                    printEncodedValue(value);
                    writer.println("</attribute>");
                }
            }
        }

        writer.println("</mbean>");
    }

    public void outputHtmlProperties(Set names) throws JMException, IOException {
        if (names.size() <= 1) {
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            outputHtmlProperties((ObjectName) iter.next());
        }
        }else {
            outputHtmlPropertiesGrid(names);
        }
    }

    public void outputHtmlPropertiesGrid(Set names) throws JMException {
        Set propertyNames = new TreeSet();
        Map[] propertyNamesPerMBeanArray = new Map[names.size()];
        int beanCounter = 0;
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            ObjectName name = (ObjectName) iter.next();
            Hashtable keyMap = name.getKeyPropertyList();
            propertyNamesPerMBeanArray[beanCounter++] = keyMap;
            propertyNames.addAll(keyMap.keySet());
        }

        writer.println("<table>");
        writer.println("<tr>");
        writer.print("<th>Domain</th>");
        for (Iterator iter = propertyNames.iterator(); iter.hasNext();) {
            writer.print("<th>");
            writer.print(iter.next());
            writer.print("</th>");
        }
        writer.println();
        writer.println("</tr>");

        beanCounter = 0;
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            ObjectName name = (ObjectName) iter.next();

            writer.print("<tr><td class='domainName'>");
            writer.print(name.getDomain());
            writer.print("</td>");

            for (Iterator iter2 = propertyNames.iterator(); iter2.hasNext();) {
                String propertyName = (String) iter2.next();

                if (propertyNamesPerMBeanArray[beanCounter].containsKey(propertyName)) {
                    String value = name.getKeyProperty(propertyName);
                    writer.print("<td class='");
                    writer.print(propertyName);
                    writer.print("'>");
                    if (value != null) {
                        printEncodedValue(value);
                    }
                    writer.print("</td>");
                }
            }
            writer.print("</tr>");
            beanCounter++;
        }
        writer.println("</table>");
    }


    public void outputHtmlProperties(ObjectName name) throws JMException, IOException {
        writer.println("<table>");
        writer.println("<tr>");
        writer.println("<th>Property</th><th>Value</th>");
        writer.println("</tr>");

        writer.print("<tr><td>Domain</td><td>");
        String domain = name.getDomain();
        if (domain != null) {
            printEncodedValue(domain);
        }
        writer.print("</td></tr>");

        Map map = name.getKeyPropertyList();
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Entry) iter.next();
            String attributeName = (String) entry.getKey();
            String value = (String) entry.getValue();

            writer.print("<tr><td>");
            writer.print(attributeName);
            writer.print("</td><td>");
            if (value != null) {
                printEncodedValue(value);
            }
            writer.print("</td></tr>");
        }

        writer.println("</table>");
    }

    public void outputHtmlAttributes(Set names) throws IOException, JMException {
        if (names.size() <= 1) {
            for (Iterator iter = names.iterator(); iter.hasNext();) {
                outputHtmlAttributes((ObjectName) iter.next());
            }
        }
        else {
            outputHtmlAttributeGrid(names);
        }
    }

    public void outputHtmlAttributeGrid(Set names) throws JMException {
        MBeanServer beanServer = getMBeanServer();
        Set attributeNames = new TreeSet();
        Set[] attributeNamesPerMBeanArray = new Set[names.size()];
        int beanCounter = 0;
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            ObjectName name = (ObjectName) iter.next();
            MBeanInfo beanInfo = beanServer.getMBeanInfo(name);
            MBeanAttributeInfo[] attributes = beanInfo.getAttributes();

            Set availableNamesPerMBean = new HashSet();
            attributeNamesPerMBeanArray[beanCounter++] = availableNamesPerMBean;
            for (int i = 0; i < attributes.length; i++) {
                MBeanAttributeInfo info = attributes[i];
                if (info.isReadable()) {
                    String attributeName = info.getName();
                    availableNamesPerMBean.add(attributeName);
                    attributeNames.add(attributeName);
                }
            }
        }

        writer.println("<table>");
        writer.println("<tr>");
        writer.print("<th>MBean</th>");
        for (Iterator iter = attributeNames.iterator(); iter.hasNext();) {
            writer.print("<th>");
            writer.print(iter.next());
            writer.print("</th>");
        }
        writer.println();
        writer.println("</tr>");

        beanCounter = 0;
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            ObjectName name = (ObjectName) iter.next();

            writer.print("<tr><td class='mbeanName'>");
            writer.print(name);
            writer.print("</td>");

            for (Iterator iter2 = attributeNames.iterator(); iter2.hasNext();) {
                String attributeName = (String) iter2.next();

                if (attributeNamesPerMBeanArray[beanCounter].contains(attributeName)) {
                    Object value = getAttributeValue(name, attributeName);
                    writer.print("<td class='");
                    writer.print(attributeName);
                    writer.print("'>");
                    if (value != null) {
                        printEncodedValue(value);
                    }
                    writer.print("</td>");
                }
            }
            writer.print("</tr>");
            beanCounter++;
        }
        writer.println("</table>");
    }

    public void outputHtmlAttributes(ObjectName name) throws JMException, IOException {
        MBeanServer beanServer = getMBeanServer();
        MBeanInfo beanInfo = beanServer.getMBeanInfo(name);
        writer.println("<table>");
        writer.println("<tr>");
        writer.println("<th>Attribute</th><th>Value</th><th>Type</th>");
        writer.println("</tr>");

        MBeanAttributeInfo[] attributes = beanInfo.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            MBeanAttributeInfo info = attributes[i];
            if (info.isReadable()) {
                String attributeName = info.getName();

                Object value = getAttributeValue(name, attributeName);
                writer.print("<tr><td>");
                writer.print(attributeName);
                writer.print("</td><td>");
                if (value != null) {
                    printEncodedValue(value);
                }
                writer.print("</td><td>");
                writer.print(info.getType());
                writer.print("</td></tr>");
            }
        }

        writer.println("</table>");
    }

    public void outputMBeans(Collection names) throws IOException {
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            outputMBeans((ObjectName) iter.next());
        }
    }

    public void outputMBeans(ObjectName name) throws IOException {
        Map properties = name.getKeyPropertyList();
        for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            writer.print("<mbean name='");
            writer.print(entry.getKey());
            ObjectInstance objectInstance = (ObjectInstance) entry.getValue();
            if (objectInstance != null) {
                writer.print("' className='");
                writer.print(objectInstance.getClassName());
            }
            writer.println("'/>");
        }
    }

    public void outputHeader() throws IOException {
        writer.println("<?xml version='1.0'?>");
        writer.println("<mbeans>");
    }

    public void outputFooter() throws IOException {
        writer.println("</mbeans>");
    }

    /**
     * Encodes the value as a String and ensures that there are no bad XML
     * characters like < or > which are encoded.
     */
    public void printEncodedValue(Object value) {
        if (value != null) {
            String text = value.toString();
            for (int i = 0, size = text.length(); i < size; i++) {
                char ch = text.charAt(i);
                switch (ch) {
                case '<':
                    writer.print("&lt;");
                    break;

                case '>':
                    writer.print("&gt;");
                    break;

                case '&':
                    writer.print("&amp;");
                    break;

                // used in ObjectName
                case ',':
                    writer.print("%2C");
                    break;

                case ':':
                    writer.print("%3A");
                    break;

                case '=':
                    writer.print("%3D");
                    break;

                default:
                    writer.print(ch);
                }
            }
        }
    }

    /**
     * Prints a HTTP encoded ObjectName suitable for use inside URLs
     */
    public void printEncodedObjectName(ObjectName name) {
        printEncodedValue(name);
    }

    /**
     * Outputs a URL to the detail JMX stats view
     */
    protected void printDetailURL(ObjectName name) {
        writer.print("mbeanDetail.jsp?view=detail&style=html&name=");
        printEncodedObjectName(name);
    }

    /**
     * Outputs a URL to the detail JMX stats view
     */
    protected void printDetailURL(String propertyName, String propertyValue) {
        writer.print("mbeanDetail.jsp?view=detail&style=html&query=");
        printEncodedValue("*:" + propertyName + "=" + propertyValue + ",*");
    }

    protected Object getAttributeValue(ObjectName name, String attributeName) throws MBeanException {
        MBeanServer beanServer = getMBeanServer();
        Object value = null;
        try {
            value = beanServer.getAttribute(name, attributeName);
        }
        catch (AttributeNotFoundException e) {
            log.warn("Caught: " + e, e);
        }
        catch (InstanceNotFoundException e) {
            log.warn("Caught: " + e, e);
        }
        catch (ReflectionException e) {
            log.warn("Caught: " + e, e);
        }
        return value;
    }

}
