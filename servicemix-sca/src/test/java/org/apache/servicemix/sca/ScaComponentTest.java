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
package org.apache.servicemix.sca;

import java.io.File;
import java.net.URL;

import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tuscany.core.invocation.spi.ProxyFactory;
import org.apache.tuscany.core.runtime.TuscanyModuleComponentContext;
import org.apache.tuscany.core.runtime.client.TuscanyRuntime;
import org.apache.tuscany.model.assembly.ConfiguredReference;
import org.apache.tuscany.model.assembly.ConfiguredService;
import org.apache.tuscany.model.assembly.EntryPoint;
import org.apache.tuscany.model.assembly.Module;
import org.osoa.sca.CurrentModuleContext;
import org.osoa.sca.ModuleContext;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.sca.ScaComponent;
import org.apache.servicemix.sca.bigbank.account.AccountService;

public class ScaComponentTest extends TestCase {

    private static Log logger =  LogFactory.getLog(ScaComponentTest.class);
    
    protected JBIContainer container;
    
    protected void setUp() throws Exception {
        container = new JBIContainer();
        container.setUseMBeanServer(false);
        container.setCreateMBeanServer(false);
        container.setMonitorInstallationDirectory(false);
        container.setNamingContext(new InitialContext());
        container.setEmbedded(true);
        container.init();
    }
    
    protected void tearDown() throws Exception {
        if (container != null) {
            container.shutDown();
        }
    }
    
    public void testDeploy() throws Exception {
        ScaComponent component = new ScaComponent();
        container.activateComponent(component, "JSR181Component");

        // Start container
        container.start();
        
        // Deploy SU
        component.getServiceUnitManager().deploy("su", getServiceUnitPath("org/apache/servicemix/sca/bigbank"));
        component.getServiceUnitManager().init("su", getServiceUnitPath("org/apache/servicemix/sca/bigbank"));
        component.getServiceUnitManager().start("su");
    }
     
    protected String getServiceUnitPath(String name) {
        URL url = getClass().getClassLoader().getResource(name + "/sca.module");
        File path = new File(url.getFile());
        path = path.getParentFile();
        return path.getAbsolutePath();
    }
    
    public static final void main(String[] args) throws Exception {

        //Obtain Tuscany runtime
        TuscanyRuntime tuscany = new TuscanyRuntime("hello", null);

        tuscany.start(); //Start the runtime.

        //Obtain SCA module context.
        ModuleContext moduleContext = CurrentModuleContext.getContext();
        TuscanyModuleComponentContext tModuleContext = (TuscanyModuleComponentContext) moduleContext; 
        Module module = tModuleContext.getModuleComponent().getModuleImplementation();
        for (EntryPoint entryPoint : module.getEntryPoints()) {
            ConfiguredReference referenceValue = entryPoint.getConfiguredReference();
            ConfiguredService targetServiceEndpoint = referenceValue.getConfiguredServices().get(0);
        	ProxyFactory proxyFactory = (ProxyFactory) targetServiceEndpoint.getProxyFactory();
            Object proxy = proxyFactory.createProxy();
            AccountService svc = (AccountService) proxy;
            svc.getAccountReport("customer");
		}
        
        tuscany.stop();
    }
	
	
}
