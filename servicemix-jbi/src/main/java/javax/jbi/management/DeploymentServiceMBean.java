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
package javax.jbi.management;

public interface DeploymentServiceMBean
{
    
    String deploy (String saZipURL) throws Exception;

    String undeploy (String saName) throws Exception;

    String[] getDeployedServiceUnitList (String componentName) throws Exception;

    String[] getDeployedServiceAssemblies () throws Exception;
    
    String getServiceAssemblyDescriptor (String saName) throws Exception;
   
    String[] getDeployedServiceAssembliesForComponent (String componentName)
        throws Exception;
    
    String[] getComponentsForDeployedServiceAssembly (String saName) throws Exception;
    
    boolean isDeployedServiceUnit (String componentName, String suName) throws Exception;
    
    boolean canDeployToComponent (String componentName);
    
    String start(String serviceAssemblyName) throws Exception;
    
    String stop(String serviceAssemblyName) throws Exception;
    
    String shutDown(String serviceAssemblyName) throws Exception;
    
    String getState(String serviceAssemblyName) throws Exception;

    static final String STARTED   = "Started";
    
    static final String SHUTDOWN = "Shutdown";

    static final String STOPPED   = "Stopped";
}
