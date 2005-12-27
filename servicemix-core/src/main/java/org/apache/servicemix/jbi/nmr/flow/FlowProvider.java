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
package org.apache.servicemix.jbi.nmr.flow;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import javax.jbi.JBIException;
import org.activeio.FactoryFinder;
import org.activemq.util.IntrospectionSupport;
import org.activemq.util.URISupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Find a Flow by Name
 * 
 * @version $Revision$
 */
public class FlowProvider{
    private static final Log log=LogFactory.getLog(FlowProvider.class);
    private static FactoryFinder finder=new FactoryFinder("META-INF/services/org/apache/servicemix/jbi/nmr/flow/");

    /**
     * Locate a Flow
     * 
     * @param flow
     * @return the Flow
     * @throws JBIException
     */
    public static Flow getFlow(String flow) throws JBIException{
        Object value;
        String flowName=getFlowName(flow);
        try{
            value=finder.newInstance(flowName);
            if(value!=null&&value instanceof Flow){
                String query=getQuery(flow);
                if(query!=null){
                    Map map=URISupport.parseQuery(query);
                    if(map!=null&&!map.isEmpty()){
                        IntrospectionSupport.setProperties(value,map);
                    }
                }
                return (Flow) value;
            }
            throw new JBIException("No implementation found for: "+flow);
        }catch(IllegalAccessException e){
            log.error("getFlow("+flow+" failed: "+e,e);
            throw new JBIException(e);
        }catch(InstantiationException e){
            log.error("getFlow("+flow+" failed: "+e,e);
            throw new JBIException(e);
        }catch(IOException e){
            log.error("getFlow("+flow+" failed: "+e,e);
            throw new JBIException(e);
        }catch(ClassNotFoundException e){
            log.error("getFlow("+flow+" failed: "+e,e);
            throw new JBIException(e);
        }catch(URISyntaxException e){
            log.error("getFlow("+flow+" failed: "+e,e);
            throw new JBIException(e);
        }
    }

    protected static String getFlowName(String str){
        String result=str;
        int index=str.indexOf('?');
        if(index>=0){
            result=str.substring(0,index);
        }
        return result;
    }

    protected static String getQuery(String str){
        String result=null;
        int index=str.indexOf('?');
        if(index>=0&&(index+1)<str.length()){
            result=str.substring(index+1);
        }
        return result;
    }
}