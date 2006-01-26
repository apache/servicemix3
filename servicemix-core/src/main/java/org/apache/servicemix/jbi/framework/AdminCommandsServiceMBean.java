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
package org.apache.servicemix.jbi.framework;

import javax.jbi.management.LifeCycleMBean;

import java.util.Properties;

/**
 * Provides a simple interface to access ServiceMix administration commands.
 * 
 * @version $Revision: 657 $
 */
public interface AdminCommandsServiceMBean extends LifeCycleMBean {

    String installComponent(String installJarURL, Properties props);
    
    String uninstallComponent(String componentName);
    
    String installSharedLibrary(String installJarURL);
    
    String uninstallSharedLibrary(String sharedLibraryName);
    
    String startComponent(String componentName);
    
    String stopComponent(String componentName);
    
    String shutdownComponent(String componentName, boolean force);
    
    String deployServiceAssembly(String installJarURL);
    
    String undeployServiceAssembly(String serviceAssemblyName);
    
    String startServiceAssembly(String serviceAssemblyName);
    
    String stopServiceAssembly(String serviceAssemblyName);
    
    String shutdownServiceAssembly(String serviceAssemblyName);
    
    String listComponents(boolean serviceEngines, 
                          boolean bindingComponents,
                          String state,
                          String sharedLibraryName,
                          String serviceAssemblyName);
    
    String listSharedLibraries(String componentName);
    
    String listServiceAssemblies(String state, String componentName);
}
