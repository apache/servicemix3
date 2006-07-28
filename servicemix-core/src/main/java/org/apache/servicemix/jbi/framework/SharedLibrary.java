package org.apache.servicemix.jbi.framework;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

import org.apache.servicemix.jbi.deployment.ClassPath;
import org.apache.servicemix.jbi.loaders.JarFileClassLoader;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.MBeanInfoProvider;

public class SharedLibrary implements SharedLibraryMBean, MBeanInfoProvider {

    private org.apache.servicemix.jbi.deployment.SharedLibrary library;
    private File installationDir;
    private ClassLoader classLoader;
    
    public SharedLibrary(org.apache.servicemix.jbi.deployment.SharedLibrary library,
                         File installationDir) {
        this.library = library;
        this.installationDir = installationDir;
        this.classLoader = createClassLoader();
    }

    /**
     * @return the library
     */
    public org.apache.servicemix.jbi.deployment.SharedLibrary getLibrary() {
        return library;
    }
    
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
    
    protected ClassLoader createClassLoader() {
        boolean parentFirst = library.isParentFirstClassLoaderDelegation();
        // Make the current ClassLoader the parent
        ClassLoader parent = getClass().getClassLoader();       
        
        ClassPath cp = library.getSharedLibraryClassPath();
        String[] classPathNames = cp.getPathElements();
        URL[] urls = new URL[classPathNames.length];
        for (int i = 0; i < classPathNames.length; i++) {
            File file = new File(installationDir, classPathNames[i]);
            try {
                urls[i] = file.toURL();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(classPathNames[i], e);
            }
        }
        return new JarFileClassLoader(
                        getName(), 
                        urls, 
                        parent, 
                        !parentFirst, 
                        new String[0], 
                        new String[] { "java.", "javax." });
    }

    public String getDescription() {
        return library.getIdentification().getDescription();
    }

    public String getName() {
        return library.getIdentification().getName();
    }

    public Object getObjectToManage() {
        return this;
    }

    public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute(getObjectToManage(), "name", "name of the service unit");
        helper.addAttribute(getObjectToManage(), "description", "description of the service unit");
        return helper.getAttributeInfos();
    }

    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        return null;
    }

    public String getSubType() {
        return null;
    }

    public String getType() {
        return "SharedLibrary";
    }

    public void setPropertyChangeListener(PropertyChangeListener l) {
        // We do not fire property events, so need to keep
        // a reference
    }

}
