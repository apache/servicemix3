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
package org.apache.servicemix.jbi.deployment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.servicemix.jbi.util.DOMUtil;
import org.apache.servicemix.jbi.util.FileUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Revision$
 */
public final class DescriptorFactory {

    public static final String DESCRIPTOR_FILE = "META-INF/jbi.xml";

    /**
     * JAXP attribute value indicating the XSD schema language.
     */
    private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorFactory.class);

    private DescriptorFactory() {
    }
    
    /**
     * Build a jbi descriptor from a file archive
     * 
     * @param descriptorFile
     *            path to the jbi descriptor, or to the root directory
     * @return the Descriptor object
     */
    public static Descriptor buildDescriptor(File descriptorFile) {
        if (descriptorFile.isDirectory()) {
            descriptorFile = new File(descriptorFile, DESCRIPTOR_FILE);
        }
        if (descriptorFile.isFile()) {
            try {
                return buildDescriptor(descriptorFile.toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException("There is a bug here...", e);
            }
        }
        return null;
    }

    /**
     * Build a jbi descriptor from the specified URL
     * 
     * @param url
     *            url to the jbi descriptor
     * @return the Descriptor object
     */
    public static Descriptor buildDescriptor(final URL url) {
        try {
            // Read descriptor
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copyInputStream(url.openStream(), baos);
            // Validate descriptor
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XSD_SCHEMA_LANGUAGE);
            Schema schema = schemaFactory.newSchema(DescriptorFactory.class.getResource("/jbi-descriptor.xsd"));
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
                public void warning(SAXParseException exception) throws SAXException {
                    LOGGER.debug("Validation warning on {}", url, exception);
                }
                public void error(SAXParseException exception) throws SAXException {
                    LOGGER.info("Validation error on {}", url, exception);
                }
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }
            });
            validator.validate(new StreamSource(new ByteArrayInputStream(baos.toByteArray())));
            // Parse descriptor
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(new ByteArrayInputStream(baos.toByteArray()));
            Element jbi = doc.getDocumentElement();
            Descriptor desc = new Descriptor();
            desc.setVersion(Double.parseDouble(getAttribute(jbi, "version")));
            Element child = DOMUtil.getFirstChildElement(jbi);
            if ("component".equals(child.getLocalName())) {
                Component component = parseComponent(child);
                desc.setComponent(component);
            } else if ("shared-library".equals(child.getLocalName())) {
                SharedLibrary sharedLibrary = parseSharedLibrary(child);
                desc.setSharedLibrary(sharedLibrary);
            } else if ("service-assembly".equals(child.getLocalName())) {
                ServiceAssembly serviceAssembly = parseServiceAssembly(child);
                desc.setServiceAssembly(serviceAssembly);
            } else if ("services".equals(child.getLocalName())) {
                Services services = parseServiceUnit(child);
                desc.setServices(services);
            }
            checkDescriptor(desc);
            return desc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Services parseServiceUnit(Element child) {
        Services services = new Services();
        services.setBindingComponent(Boolean.valueOf(getAttribute(child, "binding-component")).booleanValue());
        List<Provides> provides = new ArrayList<Provides>();
        List<Consumes> consumes = new ArrayList<Consumes>();
        for (Element e = DOMUtil.getFirstChildElement(child); e != null; e = DOMUtil.getNextSiblingElement(e)) {
            if ("provides".equals(e.getLocalName())) {
                Provides p = new Provides();
                p.setInterfaceName(readAttributeQName(e, "interface-name"));
                p.setServiceName(readAttributeQName(e, "service-name"));
                p.setEndpointName(getAttribute(e, "endpoint-name"));
                provides.add(p);
            } else if ("consumes".equals(e.getLocalName())) {
                Consumes c = new Consumes();
                c.setInterfaceName(readAttributeQName(e, "interface-name"));
                c.setServiceName(readAttributeQName(e, "service-name"));
                c.setEndpointName(getAttribute(e, "endpoint-name"));
                c.setLinkType(getAttribute(e, "link-type"));
                consumes.add(c);
            }
        }
        services.setProvides(provides.toArray(new Provides[provides.size()]));
        services.setConsumes(consumes.toArray(new Consumes[consumes.size()]));
        return services;
    }

    private static ServiceAssembly parseServiceAssembly(Element child) {
        ServiceAssembly serviceAssembly = new ServiceAssembly();
        List<ServiceUnit> sus = new ArrayList<ServiceUnit>();
        for (Element e = DOMUtil.getFirstChildElement(child); e != null; e = DOMUtil.getNextSiblingElement(e)) {
            if ("identification".equals(e.getLocalName())) {
                serviceAssembly.setIdentification(readIdentification(e));
            } else if ("service-unit".equals(e.getLocalName())) {
                ServiceUnit su = new ServiceUnit();
                for (Element e2 = DOMUtil.getFirstChildElement(e); e2 != null; e2 = DOMUtil.getNextSiblingElement(e2)) {
                    if ("identification".equals(e2.getLocalName())) {
                        su.setIdentification(readIdentification(e2));
                    } else if ("target".equals(e2.getLocalName())) {
                        Target target = new Target();
                        for (Element e3 = DOMUtil.getFirstChildElement(e2); e3 != null; e3 = DOMUtil.getNextSiblingElement(e3)) {
                            if ("artifacts-zip".equals(e3.getLocalName())) {
                                target.setArtifactsZip(getText(e3));
                            } else if ("component-name".equals(e3.getLocalName())) {
                                target.setComponentName(getText(e3));
                            }
                        }
                        su.setTarget(target);
                    }
                }
                sus.add(su);
            } else if ("connections".equals(e.getLocalName())) {
                Connections connections = new Connections();
                List<Connection> cns = new ArrayList<Connection>();
                for (Element e2 = DOMUtil.getFirstChildElement(e); e2 != null; e2 = DOMUtil.getNextSiblingElement(e2)) {
                    if ("connection".equals(e2.getLocalName())) {
                        Connection cn = new Connection();
                        for (Element e3 = DOMUtil.getFirstChildElement(e2); e3 != null; e3 = DOMUtil.getNextSiblingElement(e3)) {
                            if ("consumer".equals(e3.getLocalName())) {
                                Consumer consumer = new Consumer();
                                consumer.setInterfaceName(readAttributeQName(e3, "interface-name"));
                                consumer.setServiceName(readAttributeQName(e3, "service-name"));
                                consumer.setEndpointName(getAttribute(e3, "endpoint-name"));
                                cn.setConsumer(consumer);
                            } else if ("provider".equals(e3.getLocalName())) {
                                Provider provider = new Provider();
                                provider.setServiceName(readAttributeQName(e3, "service-name"));
                                provider.setEndpointName(getAttribute(e3, "endpoint-name"));
                                cn.setProvider(provider);
                            }
                        }
                        cns.add(cn);
                    }
                }
                connections.setConnections(cns.toArray(new Connection[cns.size()]));
                serviceAssembly.setConnections(connections);
            }
        }
        serviceAssembly.setServiceUnits(sus.toArray(new ServiceUnit[sus.size()]));
        return serviceAssembly;
    }

    private static SharedLibrary parseSharedLibrary(Element child) {
        SharedLibrary sharedLibrary = new SharedLibrary();
        sharedLibrary.setClassLoaderDelegation(getAttribute(child, "class-loader-delegation"));
        sharedLibrary.setVersion(getAttribute(child, "version"));
        for (Element e = DOMUtil.getFirstChildElement(child); e != null; e = DOMUtil.getNextSiblingElement(e)) {
            if ("identification".equals(e.getLocalName())) {
                sharedLibrary.setIdentification(readIdentification(e));
            } else if ("shared-library-class-path".equals(e.getLocalName())) {
                ClassPath sharedLibraryClassPath = new ClassPath();
                List<String> l = new ArrayList<String>();
                for (Element e2 = DOMUtil.getFirstChildElement(e); e2 != null; e2 = DOMUtil.getNextSiblingElement(e2)) {
                    if ("path-element".equals(e2.getLocalName())) {
                        l.add(getText(e2));
                    }
                }
                sharedLibraryClassPath.setPathList(l);
                sharedLibrary.setSharedLibraryClassPath(sharedLibraryClassPath);
            }
        }
        return sharedLibrary;
    }

    private static Component parseComponent(Element child) {
        Component component = new Component();
        component.setType(child.getAttribute("type"));
        component.setComponentClassLoaderDelegation(getAttribute(child, "component-class-loader-delegation"));
        component.setBootstrapClassLoaderDelegation(getAttribute(child, "bootstrap-class-loader-delegation"));
        List<SharedLibraryList> sls = new ArrayList<SharedLibraryList>();
        DocumentFragment ext = null;
        for (Element e = DOMUtil.getFirstChildElement(child); e != null; e = DOMUtil.getNextSiblingElement(e)) {
            if ("identification".equals(e.getLocalName())) {
                component.setIdentification(readIdentification(e));
            } else if ("component-class-name".equals(e.getLocalName())) {
                component.setComponentClassName(getText(e));
                component.setDescription(getAttribute(e, "description"));
            } else if ("component-class-path".equals(e.getLocalName())) {
                ClassPath componentClassPath = new ClassPath();
                List<String> l = new ArrayList<String>();
                for (Element e2 = DOMUtil.getFirstChildElement(e); e2 != null; e2 = DOMUtil.getNextSiblingElement(e2)) {
                    if ("path-element".equals(e2.getLocalName())) {
                        l.add(getText(e2));
                    }
                }
                componentClassPath.setPathList(l);
                component.setComponentClassPath(componentClassPath);
            } else if ("bootstrap-class-name".equals(e.getLocalName())) {
                component.setBootstrapClassName(getText(e));
            } else if ("bootstrap-class-path".equals(e.getLocalName())) {
                ClassPath bootstrapClassPath = new ClassPath();
                List<String> l = new ArrayList<String>();
                for (Element e2 = DOMUtil.getFirstChildElement(e); e2 != null; e2 = DOMUtil.getNextSiblingElement(e2)) {
                    if ("path-element".equals(e2.getLocalName())) {
                        l.add(getText(e2));
                    }
                }
                bootstrapClassPath.setPathList(l);
                component.setBootstrapClassPath(bootstrapClassPath);
            } else if ("shared-library".equals(e.getLocalName())) {
                SharedLibraryList sl = new SharedLibraryList();
                sl.setName(getText(e));
                sl.setVersion(getAttribute(e, "version"));
                sls.add(sl);
            } else {
                if (ext == null) {
                    ext = child.getOwnerDocument().createDocumentFragment();
                }
                ext.appendChild(e);
            }
        }
        component.setSharedLibraries(sls.toArray(new SharedLibraryList[sls.size()]));
        if (ext != null) {
            InstallationDescriptorExtension descriptorExtension = new InstallationDescriptorExtension();
            descriptorExtension.setDescriptorExtension(ext);
            component.setDescriptorExtension(descriptorExtension);
        }
        return component;
    }
    
    private static String getAttribute(Element e, String name) {
        if (e.hasAttribute(name)) {
            return e.getAttribute(name);
        } else {
            return null;
        }
    }
    
    private static QName readAttributeQName(Element e, String name) {
        String attr = getAttribute(e, name);
        if (attr != null) {
            return DOMUtil.createQName(e, attr);
        } else {
            return null;
        }
    }
    
    private static String getText(Element e) {
        return DOMUtil.getElementText(e).trim();
    }
    
    private static Identification readIdentification(Element e) {
        Identification ident = new Identification();
        for (Element e2 = DOMUtil.getFirstChildElement(e); e2 != null; e2 = DOMUtil.getNextSiblingElement(e2)) {
            if ("name".equals(e2.getLocalName())) {
                ident.setName(DOMUtil.getElementText(e2));
            } else if ("description".equals(e2.getLocalName())) {
                ident.setDescription(DOMUtil.getElementText(e2));
            }
        }
        return ident;
    }

    /**
     * Check validity of the JBI descriptor
     * 
     * @param descriptor
     *            the descriptor to check
     * @throws Exception
     *             if the descriptor is not valid
     */
    public static void checkDescriptor(Descriptor descriptor) {
        List<String> violations = new ArrayList<String>();

        if (descriptor.getVersion() != 1.0) {
            violations.add("JBI descriptor version should be set to '1.0' but is " + descriptor.getVersion());
        }

        if (descriptor.getComponent() != null) {
            checkComponent(violations, descriptor.getComponent());
        } else if (descriptor.getServiceAssembly() != null) {
            checkServiceAssembly(violations, descriptor.getServiceAssembly());
        } else if (descriptor.getServices() != null) {
            checkServiceUnit(violations, descriptor.getServices());
        } else if (descriptor.getSharedLibrary() != null) {
            checkSharedLibrary(violations, descriptor.getSharedLibrary());
        } else {
            violations.add("The jbi descriptor does not contain any informations");
        }

        if (violations.size() > 0) {
            throw new RuntimeException("The JBI descriptor is not valid, please correct these violations "
                            + violations.toString());
        }
    }

    /**
     * Checks that the component is valid
     * 
     * @param violations
     *            A list of violations that the check can add to
     * 
     * @param component
     *            The component descriptor that is being checked
     */
    private static void checkComponent(List<String> violations, Component component) {
        if (component.getIdentification() == null) {
            violations.add("The component has not identification");
        } else {
            if (isBlank(component.getIdentification().getName())) {
                violations.add("The component name is not set");
            }
        }
        if (component.getBootstrapClassName() == null) {
            violations.add("The component has not defined a boot-strap class name");
        }
        if (component.getBootstrapClassPath() == null || component.getBootstrapClassPath().getPathElements() == null) {
            violations.add("The component has not defined any boot-strap class path elements");
        }
    }

    /**
     * Checks that the service assembly is valid
     * 
     * @param violations
     *            A list of violations that the check can add to
     * 
     * @param serviceAssembly
     *            The service assembly descriptor that is being checked
     */
    private static void checkServiceAssembly(List<String> violations, ServiceAssembly serviceAssembly) {
        if (serviceAssembly.getIdentification() == null) {
            violations.add("The service assembly has not identification");
        } else {
            if (isBlank(serviceAssembly.getIdentification().getName())) {
                violations.add("The service assembly name is not set"); 
            }
        }
    }

    /**
     * Checks that the service unit is valid
     * 
     * @param violations
     *            A list of violations that the check can add to
     * 
     * @param services
     *            The service unit descriptor that is being checked
     */
    private static void checkServiceUnit(List<String> violations, Services services) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Checks that the shared library is valid
     * 
     * @param violations
     *            A list of violations that the check can add to
     * 
     * @param sharedLibrary
     *            The shared library descriptor that is being checked
     */
    private static void checkSharedLibrary(List<String> violations, SharedLibrary sharedLibrary) {
        if (sharedLibrary.getIdentification() == null) {
            violations.add("The shared library has not identification");
        } else {
            if (isBlank(sharedLibrary.getIdentification().getName())) {
                violations.add("The shared library name is not set"); 
            }
        }
    }

    /**
     * Retrieves the jbi descriptor as a string
     * 
     * @param descriptorFile
     *            path to the jbi descriptor, or to the root directory
     * @return the contents of the jbi descriptor
     */
    public static String getDescriptorAsText(File descriptorFile) {
        if (descriptorFile.isDirectory()) {
            descriptorFile = new File(descriptorFile, DESCRIPTOR_FILE);
        }
        if (descriptorFile.isFile()) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                InputStream is = new FileInputStream(descriptorFile);
                FileUtil.copyInputStream(is, os);
                return os.toString();
            } catch (Exception e) {
                LOGGER.debug("Error reading jbi descritor: {}", descriptorFile, e);
            }
        }
        return null;
    }

    /**
     * <p>Checks if a String is whitespace, empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     * 
     * Copied from org.apache.commons.lang.StringUtils#isBlanck
     */
    private static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        int strLen = str.length();
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!(Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

}
