package org.apache.servicemix.jbi.deployer.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.servicemix.jbi.deployer.SharedLibrary;
import org.apache.servicemix.jbi.deployer.descriptor.ClassPath;
import org.apache.xbean.classloader.JarFileClassLoader;
import org.apache.xbean.classloader.MultiParentClassLoader;
import org.osgi.framework.Bundle;
import org.springframework.osgi.internal.context.support.BundleDelegatingClassLoader;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Nov 8, 2007
 * Time: 1:06:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class SharedLibraryImpl implements SharedLibrary {

    private org.apache.servicemix.jbi.deployer.descriptor.SharedLibrary library;
    private Bundle bundle;

    public SharedLibraryImpl(org.apache.servicemix.jbi.deployer.descriptor.SharedLibrary library, Bundle bundle) {
        this.library = library;
        this.bundle = bundle;
    }

    public String getName() {
        return library.getIdentification().getName();
    }

    public String getDescription() {
        return library.getIdentification().getDescription();
    }

    public String getVersion() {
        return library.getVersion();
    }

    public ClassLoader createClassLoader() {
        // Make the current ClassLoader the parent
        ClassLoader parent = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle, getClass().getClassLoader());
        boolean parentFirst = library.isParentFirstClassLoaderDelegation();
        ClassPath cp = library.getSharedLibraryClassPath();
        String[] classPathNames = cp.getPathElements();
        URL[] urls = new URL[classPathNames.length];
        for (int i = 0; i < classPathNames.length; i++) {
            urls[i] = bundle.getResource(classPathNames[i]);
            if (urls[i] == null) {
                throw new IllegalArgumentException("SharedLibrary classpath entry not found: '" +  classPathNames[i] + "'");
            }
        }
        return new MultiParentClassLoader(
                        library.getIdentification().getName(),
                        urls,
                        parent,
                        !parentFirst,
                        new String[0],
                        new String[] {"java.", "javax." });
    }
}
