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
package org.apache.servicemix.schemas.deployment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 *
 * @author gnodet (Guillaume Nodet)
 * @version $Revision: 359151 $
 */ 
public class DescriptorFactory {

	public static final String TYPE_SERVICE_ENGINE = "service-engine";
	public static final String TYPE_BINDING_COMPONENT = "binding-component";
	
	public static final String DELEGATION_PARENT_FIRST = "parent-first";
	public static final String DELEGATION_SELF_FIRST = "self-first";
	
    public static final String DESCRIPTOR_FILE = "META-INF/jbi.xml";

    private static Log log = LogFactory.getLog(DescriptorFactory.class);
    
    /**
     * Build a jbi descriptor from a file archive
     * 
     * @param descriptorFile path to the jbi descriptor, or to the root directory
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
     * @param url url to the jbi descriptor
     * @return the Descriptor object
     */
    public static Descriptor buildDescriptor(URL url) {
        try {
            JAXBContext context = JAXBContext.newInstance(Descriptor.class);
            return (Descriptor) context.createUnmarshaller().unmarshal(url);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create descriptor from " + url, e);
        }
    }
    
    /**
     * Check validity of the JBI descriptor
     * @param descriptor the descriptor to check
     * @throws Exception if the descriptor is not valid
     */
    public static void checkDescriptor(Descriptor descriptor) throws Exception {
        // TODO
    }

    /**
     * Retrieves the jbi descriptor as a string
     * 
     * @param descriptorFile path to the jbi descriptor, or to the root directory
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
                try {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = is.read(buffer)) >= 0) {
                        os.write(buffer, 0, len);
                    }
                } finally {
                    is.close();
                    os.close();
                }
                return os.toString();
            } catch (Exception e) {
                log.debug("Error reading jbi descritor: " + descriptorFile, e);
            }
        }
        return null;
    }

	public static DocumentFragment getDescriptorExtension(Component descriptor) {
		try {
			DocumentFragment df = null;
			if (descriptor.getAnyOrAny().size() > 0) {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				df = doc.createDocumentFragment();
				for (Element element : descriptor.getAnyOrAny()) {
					df.appendChild(doc.importNode(element, true));
				}
			}
			return df;
		} catch (Exception e) {
			throw new RuntimeException("Unable to create descriptor extension", e);
		}
	}
	
	public static boolean isBindingComponent(Component component) {
		return TYPE_BINDING_COMPONENT.equals(component.getType());
	}

	public static boolean isServiceEngine(Component component) {
		return TYPE_SERVICE_ENGINE.equals(component.getType());
	}
	
	public static boolean isBCLParentFirst(Component component) {
		return DELEGATION_PARENT_FIRST.equals(component.getBootstrapClassLoaderDelegation());
	}

	public static boolean isBCLSelfFirst(Component component) {
		return DELEGATION_SELF_FIRST.equals(component.getBootstrapClassLoaderDelegation());
	}

	public static boolean isCCLParentFirst(Component component) {
		return DELEGATION_PARENT_FIRST.equals(component.getComponentClassLoaderDelegation());
	}

	public static boolean isCCLSelfFirst(Component component) {
		return DELEGATION_SELF_FIRST.equals(component.getComponentClassLoaderDelegation());
	}
	
	public static boolean isSLParentFirst(Descriptor.SharedLibrary sl) {
		return DELEGATION_PARENT_FIRST.equals(sl.getClassLoaderDelegation());
	}

}
