package org.apache.servicemix.common.xbean;

import javax.jbi.messaging.MessageExchange.Role;

import org.apache.servicemix.common.Endpoint;
import org.apache.servicemix.common.ExchangeProcessor;

public class XBeanEndpoint extends Endpoint {

    private String prop;
    
    /**
     * @return the prop
     */
    public String getProp() {
        return prop;
    }

    /**
     * @param prop the prop to set
     */
    public void setProp(String prop) {
        this.prop = prop;
    }

    public void activate() throws Exception {
    }

    public void deactivate() throws Exception {
    }

    public ExchangeProcessor getProcessor() {
        return null;
    }

    public Role getRole() {
        return null;
    }

}
