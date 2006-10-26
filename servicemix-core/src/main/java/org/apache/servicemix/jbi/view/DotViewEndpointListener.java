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
package org.apache.servicemix.jbi.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.container.ContainerAware;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.jbi.framework.Endpoint;
import org.apache.servicemix.jbi.framework.Registry;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Creates a <a href="http://www.graphviz.org/">DOT</a> file showing the various components
 * and endpoints within ServiceMix

 * @org.apache.xbean.XBean 
 * description="Generates DOT visualisations of the components and endpoints available inside ServiceMix"
 * 
 * @version $Revision$
 */
public class DotViewEndpointListener extends EndpointViewRenderer implements ContainerAware {

    private static final Log log = LogFactory.getLog(DotViewEndpointListener.class);

    private JBIContainer container;
    private String file = "ServiceMixComponents.dot";
    

    public JBIContainer getContainer() {
        return container;
    }

    public void setContainer(JBIContainer container) {
        this.container = container;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    protected void doRender() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Creating DOT file at: " + file);
        }
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        try {
            generateFile(writer);
        }
        finally {
            writer.close();
        }
    }

    protected void generateFile(PrintWriter writer) throws Exception {
        writer.println("digraph \"Apache ServiceMix\" {");
        writer.println();
        writer.println("node [ shape = box, style = \"rounded,filled\", fontname = \"Helvetica-Oblique\", fontsize = 8 ];");
        writer.println();
        writer.println("jbi [ fillcolor = \"#FFFF99\", label=\"Apache ServiceMix: " + container.getName() + "\" ];");
        writer.println();

        List brokerLinks = new ArrayList();
        Registry registry = container.getRegistry();
        Collection components = registry.getComponents();
        for (Iterator iter = components.iterator(); iter.hasNext();) {
            ComponentMBeanImpl component = (ComponentMBeanImpl) iter.next();
            String name = component.getName();
            String id = encode(name);

            writer.print(id);
            writer.print(" [ fillcolor = gray, label = \"");
            writer.print(name);
            writer.println("\" ];");

            brokerLinks.add("jbi -> " + id);
        }
        writer.println();
        generateLinks(writer, brokerLinks);

        writer.println();

        List componentEndpointLinks = new ArrayList();
        Collection endpointMBeans = registry.getEndpointRegistry().getEndpointMBeans();
        for (Iterator iter = endpointMBeans.iterator(); iter.hasNext();) {
            Endpoint endpoint = (Endpoint) iter.next();
            String key = endpoint.getSubType().toLowerCase() + ":{" + 
                                endpoint.getServiceName().getNamespaceURI() + "}" + 
                                endpoint.getServiceName().getLocalPart() + ":" + 
                                endpoint.getEndpointName(); 
            String componentName = encode(endpoint.getComponentName());
            String id = encode(key);
            writer.print(id);
            String epname = formatEndpoint(key);
            String color = "lightgray";
            if (epname.startsWith("internal")) {
                epname = epname.substring(10);
                color = "#6699ff";
            } else if (epname.startsWith("external")) {
                epname = epname.substring(10);
                color = "#66ccff";
            } else if (epname.startsWith("dynamic")) {
                epname = epname.substring(9);
                color = "#6666ff";
            } else if (epname.startsWith("linked")) {
                epname = epname.substring(8);
                color = "#66ffff";
            } else {
                color = "#f3f3f3";
            }
            writer.print(" [ fillcolor = \"" + color + "\", label = \"");
            writer.print(epname);
            writer.println("\" ];");
            
            componentEndpointLinks.add(componentName + " -> " + id);
        }
        generateLinks(writer, componentEndpointLinks);
        
        writer.println("}");
    }

    protected String formatEndpoint(String key) {
        int i1 = key.indexOf('{');
        int i2 = key.indexOf('}');
        int i3 = key.indexOf(':', i2);
        String type = key.charAt(i1 - 1) == ':' ? key.substring(0, i1 - 1) : null;
        String uri = key.substring(i1 + 1, i2);
        String svc = key.substring(i2 + 1, i3);
        String ep = key.substring(i3 + 1);
        if (type != null) {
            return type + "\\n" + uri + "\\n" + svc + "\\n" + ep;
        } else {
            return uri + "\\n" + svc + "\\n" + ep;
        }
    }

    protected void generateLinks(PrintWriter writer, Collection lines, String style) {
        for (Iterator iter = lines.iterator(); iter.hasNext();) {
            writer.print(iter.next().toString());
            if (style != null) {
                writer.print(" [" + style + "]");
            }
            writer.println(";");
        }
        writer.println();
    }

    protected void generateLinks(PrintWriter writer, Collection lines) {
        generateLinks(writer, lines, null);
    }

    /**
     * Lets strip out any non supported characters
     */
    protected String encode(String name) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, size = name.length(); i < size; i++) {
            char ch = name.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '_') {
                buffer.append(ch);
            }
            else {
                buffer.append('_');
            }
        }
        return buffer.toString();
    }


}
