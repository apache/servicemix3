/** 
 * <a href="http://servicemix.org">ServiceMix: The open source ESB</a> 
 * 
 * Copyright 2005 RAJD Consultancy Ltd
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
 * 
 **/

package org.servicemix.jbi.nmr.flow;

import javax.jbi.JBIException;
import org.servicemix.jbi.nmr.flow.jms.JMSFlow;
import org.servicemix.jbi.nmr.flow.seda.SedaFlow;
import org.servicemix.jbi.nmr.flow.st.STFlow;
import junit.framework.TestCase;

public class FlowProviderTest extends TestCase{
    
    public void testGetFlowName(){
        String name = "fred";
        String query ="props=foo";
        String nameAndQuery = name + "?" + query;
        assertTrue(FlowProvider.getFlowName(name).equals(name));
        assertTrue(FlowProvider.getFlowName(nameAndQuery).equals(name));
        assertTrue(FlowProvider.getQuery(nameAndQuery).equals(query));
       
    }
    
    public void testGetFlows() throws Exception{
        Flow flow = FlowProvider.getFlow("st");
        assertTrue(flow instanceof STFlow);
        flow = FlowProvider.getFlow("seda");
        assertTrue(flow instanceof SedaFlow);
        flow = FlowProvider.getFlow("jms");
        assertTrue(flow instanceof JMSFlow);
        flow = FlowProvider.getFlow("cluster");
        assertTrue(flow instanceof JMSFlow);
    }
    
    public void testSetProperties() throws Exception {
        String jmsURL = "reliable://tcp://fred:666";
        Flow flow = FlowProvider.getFlow("jms?jmsURL="+jmsURL);
        assertTrue(flow instanceof JMSFlow);
        JMSFlow jmsFlow = (JMSFlow)flow;
        assertTrue(jmsFlow.getJmsURL().equals(jmsURL));
    }
}
