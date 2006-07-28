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
package org.apache.servicemix.bpe;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jbi.management.DeploymentException;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Message;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;

import org.apache.ode.bpe.bped.DeployTypeEnum;
import org.apache.ode.bpe.bped.EventDirector;
import org.apache.ode.bpe.bped.IDeployer;
import org.apache.ode.bpe.wsdl.extensions.BPEAction;
import org.apache.ode.bpe.wsdl.extensions.BPEActionSerializer;
import org.apache.ode.bpe.wsdl.extensions.BPEFault;
import org.apache.ode.bpe.wsdl.extensions.BPEFaultSerializer;
import org.apache.ode.bpe.wsdl.extensions.BPEInput;
import org.apache.ode.bpe.wsdl.extensions.BPEInputSerializer;
import org.apache.ode.bpe.wsdl.extensions.BPELProperty;
import org.apache.ode.bpe.wsdl.extensions.BPELPropertyAlias;
import org.apache.ode.bpe.wsdl.extensions.BPELPropertyAliasSerializer;
import org.apache.ode.bpe.wsdl.extensions.BPELPropertySerializer;
import org.apache.ode.bpe.wsdl.extensions.BPEOutput;
import org.apache.ode.bpe.wsdl.extensions.BPEOutputSerializer;
import org.apache.ode.bpe.wsdl.extensions.BPEVariableMap;
import org.apache.ode.bpe.wsdl.extensions.BPEVariableMapSerializer;
import org.apache.ode.bpe.wsdl.extensions.ExtentionConstants;
import org.apache.servicemix.bpe.util.FileSystemJarInputStream;
import org.apache.servicemix.common.AbstractDeployer;
import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.common.tools.wsdl.WSDLFlattener;
import org.w3c.dom.Document;

public class BPEDeployer extends AbstractDeployer {

    protected FilenameFilter filter;
    
	public BPEDeployer(BPEComponent component) {
		super(component);
        filter = new BpelFilter();
	}

	public boolean canDeploy(String serviceUnitName, String serviceUnitRootPath) {
        File[] bpels = new File(serviceUnitRootPath).listFiles(filter);
        return bpels != null && bpels.length == 1;
	}

	public ServiceUnit deploy(String serviceUnitName, String serviceUnitRootPath) throws DeploymentException {
		try {
			EventDirector ed = ((BPEComponent) component).getEventDirector();
			IDeployer deployer = ed.getDeployer(DeployTypeEnum.BPEL);
			deployer.loadDefinition(new FileSystemJarInputStream(new File(serviceUnitRootPath)), false);
			// Build the Service Unit
			BPEServiceUnit su = new BPEServiceUnit();
			su.setComponent(component);
            su.setName(serviceUnitName);
            su.setRootPath(serviceUnitRootPath);
            Definition rootDef = loadMainWsdl(serviceUnitRootPath);
            checkDefinition(rootDef, true);
            su.setDefinition(rootDef);
            WSDLWriter writer = WSDLFactory.newInstance().newWSDLWriter();
            WSDLFlattener flattener = new WSDLFlattener(rootDef);
            for (Iterator it = rootDef.getServices().values().iterator(); it.hasNext();) {
				Service svc = (Service) it.next();
				for (Iterator it2 = svc.getPorts().values().iterator(); it2.hasNext();) {
					Port pt = (Port) it2.next();
					BPEEndpoint ep = new BPEEndpoint();
					ep.setServiceUnit(su);
					ep.setInterfaceName(pt.getBinding().getPortType().getQName());
					ep.setService(svc.getQName());
					ep.setEndpoint(pt.getName());
                    Definition def = flattener.getDefinition(ep.getInterfaceName());
                    Document desc = writer.getDocument(def);
                    ep.setDefinition(def);
                    ep.setDescription(desc);
                    // Retrieve wsdl
					su.addEndpoint(ep);
				}
			}
			return su;
		} catch (DeploymentException e) {
			throw e;
		} catch (Exception e) {
			throw new DeploymentException(e);
		}
	}

	protected void checkDefinition(Definition rootDef, boolean main) throws DeploymentException {
        // Check that messages have only one part named "payload"
        Collection msgs = rootDef.getMessages().values();
        for (Iterator iter = msgs.iterator(); iter.hasNext();) {
            Message msg = (Message) iter.next();
            if (msg.isUndefined()) {
                throw failure("deploy", 
                        "WSDL Message '" + msg.getQName() + "' is undefined. Check namespaces.", null);
            }
            if (msg.getParts().size() > 1) {
                throw failure("deploy", 
                        "WSDL Message '" + msg.getQName() + "' has more than one part", null);
            }
        }
        // Check imported wsdls
        Collection imports = rootDef.getImports().values();
        for (Iterator iter = imports.iterator(); iter.hasNext();) {
            List imps = (List) iter.next();
            for (Iterator iterator = imps.iterator(); iterator.hasNext();) {
                Import imp = (Import) iterator.next();
                checkDefinition(imp.getDefinition(), false);
            }
        }
    }

    private Definition loadMainWsdl(String serviceUnitRootPath) throws WSDLException {
        File[] bpels = new File(serviceUnitRootPath).listFiles(filter);
        String bpel = bpels[0].getAbsoluteFile().toURI().toString();
        String wsdl = bpel.substring(0, bpel.length() - 4) + "wsdl";
		WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
	
		reader.setFeature("javax.wsdl.verbose",true);
		reader.setFeature("javax.wsdl.importDocuments",true);
					
		reader.setExtensionRegistry(getExtentionRegistry());
	
		// Parse the document and include any imported WSDL documents
		Definition ret = reader.readWSDL(null, wsdl);
		return ret;
	}
	
	private ExtensionRegistry getExtentionRegistry() {
		
		// Use IBM's implementation as a base registry. They have implemented
		// extensibility objects for SOAP,HTTP,MIME and we should not
		// loose these functions.
		ExtensionRegistry er = new com.ibm.wsdl.extensions.PopulatedExtensionRegistry();
		
		BPELPropertySerializer bpelPropSerializer = new BPELPropertySerializer();
		BPELPropertyAliasSerializer bpelPropAliasSerializer = new BPELPropertyAliasSerializer();
		BPEActionSerializer bpeActionSerializer = new BPEActionSerializer();
		BPEInputSerializer bpeInputSerializer = new BPEInputSerializer();
		BPEOutputSerializer bpeOutputSerializer = new BPEOutputSerializer();
		BPEFaultSerializer bpeFaultSerializer = new BPEFaultSerializer();
		BPEVariableMapSerializer bpeVariableSerializer = new BPEVariableMapSerializer();

		// Register the BPEL extension points
		er.registerSerializer(Definition.class,
						   ExtentionConstants.Q_ELEM_BPEL_PROPERTY,
						   bpelPropSerializer);
		er.registerDeserializer(Definition.class,
							 ExtentionConstants.Q_ELEM_BPEL_PROPERTY,
							 bpelPropSerializer);
		er.mapExtensionTypes(Definition.class,
						  ExtentionConstants.Q_ELEM_BPEL_PROPERTY,
							BPELProperty.class);
		er.registerSerializer(Definition.class,
						   ExtentionConstants.Q_ELEM_BPEL_PROPERTY_ALIAS,
						   bpelPropAliasSerializer);
		er.registerDeserializer(Definition.class,
							 ExtentionConstants.Q_ELEM_BPEL_PROPERTY_ALIAS,
							 bpelPropAliasSerializer);
		er.mapExtensionTypes(Definition.class,
						  ExtentionConstants.Q_ELEM_BPEL_PROPERTY_ALIAS,
							BPELPropertyAlias.class);
							
		// register the BPE extension points
		er.registerSerializer(BindingOperation.class,
							ExtentionConstants.Q_ELEM_BPE_ACTION,
							bpeActionSerializer);
		er.registerDeserializer(BindingOperation.class,
							ExtentionConstants.Q_ELEM_BPE_ACTION,
							bpeActionSerializer);
		er.mapExtensionTypes(BindingOperation.class,
							ExtentionConstants.Q_ELEM_BPE_ACTION,
							BPEAction.class);
		er.registerSerializer(BindingInput.class,
							ExtentionConstants.Q_ELEM_BPE_INPUT,
							bpeInputSerializer);
		er.registerDeserializer(BindingInput.class,
							ExtentionConstants.Q_ELEM_BPE_INPUT,
							bpeInputSerializer);
		er.mapExtensionTypes(BindingInput.class,
							ExtentionConstants.Q_ELEM_BPE_INPUT,
							BPEInput.class);
		er.registerSerializer(BindingOutput.class,
							ExtentionConstants.Q_ELEM_BPE_OUTPUT,
							bpeOutputSerializer);
		er.registerDeserializer(BindingOutput.class,
							ExtentionConstants.Q_ELEM_BPE_OUTPUT,
							bpeOutputSerializer);
		er.mapExtensionTypes(BindingOutput.class,
							ExtentionConstants.Q_ELEM_BPE_OUTPUT,
							BPEOutput.class);	
		
		er.registerSerializer(BindingFault.class,
							ExtentionConstants.Q_ELEM_BPE_FAULT,
							bpeFaultSerializer);
		er.registerDeserializer(BindingFault.class,
							ExtentionConstants.Q_ELEM_BPE_FAULT,
							bpeFaultSerializer);
		er.mapExtensionTypes(BindingFault.class,
							ExtentionConstants.Q_ELEM_BPE_FAULT,
							BPEFault.class);						
		
		er.registerSerializer(Definition.class,
							ExtentionConstants.Q_ELEM_BPE_VAR,
							bpeVariableSerializer);
		er.registerDeserializer(Definition.class,
							ExtentionConstants.Q_ELEM_BPE_VAR,
							bpeVariableSerializer);
		er.mapExtensionTypes(Definition.class,
							ExtentionConstants.Q_ELEM_BPE_VAR,
							BPEVariableMap.class);
							
		return er;
	}
	
    public static class BpelFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return name.endsWith(".bpel");
        }
        
    }
}
