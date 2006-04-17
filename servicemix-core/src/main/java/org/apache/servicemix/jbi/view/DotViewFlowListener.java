/**
 *
 * Copyright 2005-2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.jbi.view;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.jbi.event.ComponentEvent;
import org.apache.servicemix.jbi.event.ComponentListener;
import org.apache.servicemix.jbi.event.EndpointEvent;
import org.apache.servicemix.jbi.event.ExchangeEvent;
import org.apache.servicemix.jbi.event.ExchangeListener;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.jbi.framework.Registry;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.servicedesc.EndpointSupport;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet;

/**
 * Creates a <a href="http://www.graphviz.org/">DOT</a> file showing the JBI MessageExchange
 * flow within the JBI Container.

 * @org.apache.xbean.XBean 
 * description="Generates DOT visualisations of the exchanges flow inside ServiceMix"
 * 
 * @version $Revision: 391707 $
 */
public class DotViewFlowListener extends DotViewEndpointListener 
    implements ExchangeListener, ComponentListener {

    private Map flow;
    private Set flowLinks;
    
    public DotViewFlowListener() {
        setFile("ServiceMixFlow.dot");
        flow = new ConcurrentHashMap();
        flowLinks = new CopyOnWriteArraySet();
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    protected void generateFile(PrintWriter writer) throws Exception {
        writer.println("digraph \"Apache ServiceMix\" {");
        writer.println();
        writer.println("label = \"Apache ServiceMix flow\";");
        writer.println("node [style = \"rounded,filled\", fillcolor = yellow, fontname=\"Helvetica-Oblique\"];");
        writer.println();

        List brokerLinks = new ArrayList();
        Registry registry = getContainer().getRegistry();
        Collection components = registry.getComponents();
        for (Iterator iter = components.iterator(); iter.hasNext();) {
            ComponentMBeanImpl component = (ComponentMBeanImpl) iter.next();
            ServiceEndpoint[] ses = registry.getEndpointsForComponent(component.getComponentNameSpace());

            String name = component.getName();
            String id = encode(name);

            writer.println("subgraph cluster_" + id + " {");
            writer.println("  label=\"" + name + "\";");
            writer.println("  node [fillcolor = green];");
            writer.println("  " + id + " [label=\"" + name + "\"];");
            writer.println("  node [fillcolor = red];");
            
            for (int i = 0; i < ses.length; i++) {
                String epname = EndpointSupport.getKey(ses[i]);
                String epid = encode(epname);
                writer.println("  " + epid + " [label=\"" + epname + "\"];");
            }
            
            writer.println("}");
        }
        writer.println();
        generateLinks(writer, brokerLinks);

        writer.println();

        generateLinks(writer, flowLinks);
        
        writer.println("}");
        
    }

    public void exchangeSent(ExchangeEvent event) {
        MessageExchange me = event.getExchange();
        if (me.getEndpoint() != null &&
            me instanceof MessageExchangeImpl) {
            MessageExchangeImpl mei = (MessageExchangeImpl) me;
            String source = (String) me.getProperty(JbiConstants.SENDER_ENDPOINT);
            if (source == null) {
                source = mei.getSourceId().getName();
            }
            String dest = EndpointSupport.getKey(mei.getEndpoint());
            Map componentFlow = (Map) flow.get(source);
            if (componentFlow.put(dest, Boolean.TRUE) == null) {
                flowLinks.add(encode(source) + " -> " + encode(dest));
                viewIsDirty(mei.getEndpoint());
            }
        }
        
    }
    
    protected void createSource(String name) {
        synchronized (flow) {
            Map componentFlow = (Map) flow.get(name);
            if (componentFlow == null) {
                componentFlow = new ConcurrentHashMap();
                flow.put(name, componentFlow);
            }
        }
    }

    public void internalEndpointRegistered(EndpointEvent event) {
        String key = EndpointSupport.getKey(event.getEndpoint());
        createSource(key);
        super.internalEndpointRegistered(event);
    }
    
    public void externalEndpointRegistered(EndpointEvent event) {
        String key = EndpointSupport.getKey(event.getEndpoint());
        createSource(key);
        super.externalEndpointRegistered(event);
    }
    
    public void linkedEndpointRegistered(EndpointEvent event) {
        String key = EndpointSupport.getKey(event.getEndpoint());
        createSource(key);
        super.linkedEndpointRegistered(event);
    }
    
    public void componentInstalled(ComponentEvent event) {
        createSource(event.getComponent().getName());
    }

    public void componentStarted(ComponentEvent event) {
        createSource(event.getComponent().getName());
    }

    public void componentStopped(ComponentEvent event) {
    }

    public void componentShutDown(ComponentEvent event) {
    }

    public void componentUninstalled(ComponentEvent event) {
    }

}
