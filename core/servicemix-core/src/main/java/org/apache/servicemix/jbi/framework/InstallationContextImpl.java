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
package org.apache.servicemix.jbi.framework;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.jbi.component.Bootstrap;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.InstallationContext;

import org.w3c.dom.DocumentFragment;

import org.apache.servicemix.jbi.deployment.Component;
import org.apache.servicemix.jbi.deployment.InstallationDescriptorExtension;
import org.apache.servicemix.jbi.deployment.SharedLibraryList;

/**
 * This context contains information necessary for a JBI component to perform its installation/uninstallation
 * processing. This is provided to the init() method of the component {@link Bootstrap}interface.
 * 
 * @version $Revision$
 */
public class InstallationContextImpl implements InstallationContext {
    
    private Component descriptor;
    private File installRoot;
    private List<String> classPathElements = Collections.emptyList();
    private ComponentContext context;
    private boolean install = true;
    
    public InstallationContextImpl(Component descriptor) {
        this.descriptor = descriptor;
        if (descriptor.getComponentClassPath() != null &&
            descriptor.getComponentClassPath().getPathElements() != null &&
            descriptor.getComponentClassPath().getPathElements().length > 0) {
            String[] elems = descriptor.getComponentClassPath().getPathElements();
            for (int i = 0; i < elems.length; i++) {
                if (File.separatorChar == '\\') {
                    elems[i] = elems[i].replace('/', '\\');
                } else {
                    elems[i] = elems[i].replace('\\', '/');
                }
            }
            setClassPathElements(Arrays.asList(elems));
        }
    }

    /**
     * @return the descriptor
     */
    public Component getDescriptor() {
        return descriptor;
    }

    /**
     * @return the sharedLibraries
     */
    public String[] getSharedLibraries() {
        return getSharedLibraries(descriptor.getSharedLibraries());
    }

    /**
     * Get the name of the class that implements the {@link Component}interface for this component. This must be the
     * component class name given in the component's installation descriptor.
     * 
     * @return the {@link Component}implementation class name, which must be non-null and non-empty.
     */
    public String getComponentClassName() {
        return descriptor.getComponentClassName();
    }

    /**
     * Get a list of elements that comprise the class path for this component. Each element represents either a
     * directory (containing class files) or a library file. All elements are reachable from the install root. These
     * elements represent class path items that the component's execution-time component class loader uses, in search
     * order. All path elements must use the file separator character appropriate to the system (i.e.,
     * <code>File.separator</code>).
     * 
     * @return a list of String objects, each of which contains a class path elements. The list must contain at least
     * one class path element.
     */
    public List getClassPathElements() {
        return classPathElements;
    }

    /**
     * Get the unique name assigned to this component. This name must be assigned from the component's installation
     * descriptor identification section.
     * 
     * @return the unique component name, which must be non-null and non-empty.
     */
    public String getComponentName() {
        return descriptor.getIdentification().getName();
    }

    /**
     * Get the JBI context for this component. The following methods are valid to use on the context:
     * <ul>
     * <li>{@link ComponentContext#getMBeanNames()}</li>
     * <li>{@link ComponentContext#getMBeanServer()}</li>
     * <li>{@link ComponentContext#getNamingContext()}</li>
     * <li>{@link ComponentContext#getTransactionManager()}</li>
     * </ul>
     * All other methods on the returned context must throw a <code>IllegalStateException</code> exception if invoked.
     * 
     * @return the JBI context for this component, which must be non-null.
     */
    public ComponentContext getContext() {
        return context;
    }

    /**
     * Get the installation root directory full path name for this component. This path name must be formatted for the
     * platform the JBI environment is running on.
     * 
     * @return the installation root directory name, which must be non-null and non-empty.
     */
    public String getInstallRoot() {
        return installRoot != null ? installRoot.getAbsolutePath() : ".";
    }
    
    /**
     * 
     * @return Return the install root
     */
    public File getInstallRootAsDir(){
        return installRoot;
    }

    /**
     * Return a DOM document fragment representing the installation descriptor (jbi.xml) extension data for the
     * component, if any.
     * <p>
     * The Installation Descriptor Extension data are located at the end of the &lt;component&gt; element of the
     * installation descriptor.
     * 
     * @return a DOM document fragment containing the installation descriptor (jbi.xml) extension data, or
     * <code>null</code> if none is present in the descriptor.
     */
    public DocumentFragment getInstallationDescriptorExtension() {
        InstallationDescriptorExtension desc = descriptor.getDescriptorExtension();
        return desc != null ? desc.getDescriptorExtension() : null;
    }

    /**
     * Returns <code>true</code> if this context was created in order to install a component into the JBI environment.
     * Returns <code>false</code> if this context was created to uninstall a previously installed component.
     * <p>
     * This method is provided to allow {@link Bootstrap}implementations to tailor their behaviour according to use
     * case. For example, the {@link Bootstrap#init(InstallationContext)}method implementation may create different
     * types of extension MBeans, depending on the use case specified by this method.
     * 
     * @return <code>true</code> if this context was created in order to install a component into the JBI environment;
     * otherwise the context was created to uninstall an existing component.
     */
    public boolean isInstall() {
        return install;
    }

    /**
     * Set the list of elements that comprise the class path for this component. Each element represents either a
     * directory (containing class files) or a library file. Elements are reached from the install root. These elements
     * represent class path items that the component's execution-time component class loader uses, in search order. All
     * file paths are relative to the install root of the component.
     * <p>
     * This method allows the component's bootstrap to alter the execution-time class path specified by the component's
     * installation descriptor. The component configuration determined during installation can affect the class path
     * needed by the component at execution-time. All path elements must use the file separator character appropriate to
     * the system (i.e., <code>File.separator</code>.
     * 
     * @param classPathElements a list of String objects, each of which contains a class path elements; the list must be
     * non-null and contain at least one class path element.
     * @exception IllegalArgumentException if the class path elements is null, empty, or if an individual element is
     * ill-formed.
     */
    public void setClassPathElements(List classPathElements) {
        if (classPathElements == null) {
            throw new IllegalArgumentException("classPathElements is null");
        }
        if (classPathElements.isEmpty()) {
            throw new IllegalArgumentException("classPathElements is empty");
        }
        for (Iterator iter = classPathElements.iterator(); iter.hasNext();) {
            Object obj = iter.next();
            if (obj instanceof String == false) {
                throw new IllegalArgumentException("classPathElements must contain element of type String");
            }
            String element = (String) obj;
            String sep = File.separator.equals("\\") ? "/" : "\\";
            int offset = element.indexOf(sep);
            if ( offset > -1 ) {
                throw new IllegalArgumentException("classPathElements contains an invalid file separator '" + sep + "'"); 
            }
            File f = new File((String) element);
            if (f.isAbsolute()) {
                throw new IllegalArgumentException("classPathElements should not contain absolute paths");
            }
        }
        this.classPathElements = new ArrayList<String>(classPathElements);
    }
    
    
    /**
     * @param context The context to set.
     */
    public void setContext(ComponentContext context) {
        this.context = context;
    }
    /**
     * @param install The install to set.
     */
    public void setInstall(boolean install) {
        this.install = install;
    }
    /**
     * @param installRoot The installRoot to set.
     */
    public void setInstallRoot(File installRoot) {
        this.installRoot = installRoot;
    }
    /**
     * @return Returns the binding.
     */
    public boolean isBinding() {
        return descriptor.isBindingComponent();
    }
    /**
     * @return Returns the engine.
     */
    public boolean isEngine() {
        return descriptor.isServiceEngine();
    }
    /**
     * @return Returns the componentDescription.
     */
    public String getComponentDescription() {
        return descriptor.getIdentification().getDescription();
    }

    private static String[] getSharedLibraries(SharedLibraryList[] sharedLibraries) {
        if (sharedLibraries == null || sharedLibraries.length == 0) {
            return null;
        }
        String[] names = new String[sharedLibraries.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = sharedLibraries[i].getName();
        }
        return names;
    }

}
