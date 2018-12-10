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
package org.apache.servicemix.bpe.external;

import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.ode.bpe.action.bpel.ExternalServiceAction;
import org.apache.ode.bpe.context.resolver.ContextResolver;
import org.apache.ode.bpe.definition.IPMDProcess;
import org.apache.ode.bpe.deployment.bpel.BPELAttributes;
import org.apache.ode.bpe.deployment.bpel.WSDLOperationKey;
import org.apache.ode.bpe.engine.IEvaluationContext;
import org.apache.ode.bpe.engine.IProcessCallBack;
import org.apache.ode.bpe.instance.IPMIProcess;
import org.apache.ode.bpe.util.BPException;

public class JbiExternalAction extends ExternalServiceAction {

    private static Log log = LogFactory.getLog(JbiExternalAction.class);
    
    public static final String SM_NS = "http://servicemix.apache.org/schemas/bpe/1.0";
    
    public static final String SM_ENDPOINT = "endpoint";
    public static final String SM_SERVICE = "service";
    public static final String SM_MEP = "mep";
    
    public JbiExternalAction() {
        super();
    }
    
    public void init(Properties props) throws BPException {
        if (log.isDebugEnabled()) {
            log.debug("init");
        }
        extractInformations(props);
        // Do not store informations about operation
        props.remove(ExternalServiceAction.OPERATION_KEY);
        super.init(props);
        if (log.isDebugEnabled()) {
            log.debug("properties: " + props);
        }
    }
    
    protected void extractInformations(Properties properties) {
        Properties extProps = (Properties) properties.get(EXT_ACTION_PROPS);
        BPELAttributes attrs = (BPELAttributes) properties.get(INVOKE_ATTRIBUTES);
        WSDLOperationKey opKey = (WSDLOperationKey) properties.get(ExternalServiceAction.OPERATION_KEY);
        extProps.setProperty(JbiInvokeAction.INTERFACE_NAMESPACE, opKey.getNameSpace());
        extProps.setProperty(JbiInvokeAction.INTERFACE_LOCALNAME, opKey.getPortType());
        extProps.setProperty(JbiInvokeAction.OPERATION_NAMESPACE, opKey.getNameSpace());
        extProps.setProperty(JbiInvokeAction.OPERATION_LOCALNAME, opKey.getOperationName());
        for (Enumeration en = attrs.propertyNames(); en.hasMoreElements();) {
            String qn = (String) en.nextElement();
            String uri = attrs.getURI(qn);
            String val = attrs.getProperty(qn);
            if (SM_NS.equals(uri)) {
                if (qn.indexOf(':') > 0) {
                    qn = qn.substring(qn.indexOf(':') + 1);
                }
                if (SM_ENDPOINT.equals(qn)) {
                    String[] parts = split3(val);
                    extProps.setProperty(JbiInvokeAction.SERVICE_NAMESPACE, parts[0]);
                    extProps.setProperty(JbiInvokeAction.SERVICE_LOCALNAME, parts[1]);
                    extProps.setProperty(JbiInvokeAction.ENDPOINT_NAME, parts[2]);
                } else if (SM_SERVICE.equals(qn)) {
                    String[] parts = split2(val);
                    extProps.setProperty(JbiInvokeAction.SERVICE_NAMESPACE, parts[0]);
                    extProps.setProperty(JbiInvokeAction.SERVICE_LOCALNAME, parts[1]);
                } else if (SM_MEP.equals(qn)) {
                    extProps.setProperty(JbiInvokeAction.MEP, val);
                }
            }
        }
    }

    protected String[] split3(String uri) {
        char sep;
        if (uri.indexOf('/') > 0) {
            sep = '/';
        } else {
            sep = ':';
        }
        int idx1 = uri.lastIndexOf(sep);
        int idx2 = uri.lastIndexOf(sep, idx1 - 1);
        String epName = uri.substring(idx1 + 1);
        String svcName = uri.substring(idx2 + 1, idx1);
        String nsUri   = uri.substring(0, idx2);
        return new String[] { nsUri, svcName, epName };
    }
    
    protected String[] split2(String uri) {
        char sep;
        if (uri.indexOf('/') > 0) {
            sep = '/';
        } else {
            sep = ':';
        }
        int idx1 = uri.lastIndexOf(sep);
        String svcName = uri.substring(idx1 + 1);
        String nsUri   = uri.substring(0, idx1);
        return new String[] { nsUri, svcName };
    }
    
    public boolean execute(
            ContextResolver resolver,
            IEvaluationContext ec,
            IProcessCallBack pcb,
            IPMIProcess processInstance,
            IPMDProcess processDefinition)
            throws BPException {
        return super.execute(resolver, ec, pcb, processInstance, processDefinition);
    }
}
