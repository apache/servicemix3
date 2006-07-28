package ${packageName};

import javax.jbi.messaging.MessageExchange;

import org.apache.servicemix.common.ExchangeProcessor;

public class MyProviderProcessor implements ExchangeProcessor {

    private MyEndpoint endpoint;

    public MyProviderProcessor(MyEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void start() throws Exception {
        // TODO
    }
    
    public void stop() throws Exception {
        // TODO
    }
 
    public void process(MessageExchange exchange) throws Exception {
        // TODO
    }
 
}
