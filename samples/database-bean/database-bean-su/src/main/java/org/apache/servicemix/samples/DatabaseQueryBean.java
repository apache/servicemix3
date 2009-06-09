package org.apache.servicemix.samples;

import javax.annotation.Resource;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.sql.DataSource;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.listener.MessageExchangeListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class DatabaseQueryBean implements MessageExchangeListener {
    
    @Resource
    private DeliveryChannel channel;
    
    private JdbcTemplate jdbcTemplate;
    
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.servicemix.jbi.listener.MessageExchangeListener#onMessageExchange(javax.jbi.messaging.MessageExchange)
     */
    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
            // the exchange is active.
            // get the in message.
            NormalizedMessage message = exchange.getMessage("in");
            // use the marshaler to get the query embedded in the "in" message
            String query = null;
            try {
                query = this.fromMessageToQuery(message);
            } catch (TransformerException te) {
                throw new MessagingException("Error while parsing incoming message.", te);
            }
            // execute the query on the database and get result set
            String result = (String)jdbcTemplate.queryForObject(query, String.class);
            // send content in out message (depending of the MEP)
            message.setContent(new StringSource("<result>" + result + "</result>"));
            exchange.setMessage(message, "out");
            channel.send(exchange);
        }
    }
    
    /**
     * <p>
     * Parse the given normalized message to construct the SQL query.
     * </p>
     * 
     * @param message the <code>NormalizedMessage</code>.
     * @return the SQL query.
     * @throws MessagingException in case of parsing error.
     */
    protected String fromMessageToQuery(NormalizedMessage message) throws TransformerException {
        String query = null;
        
        try {
            Source content = message.getContent();
            SourceTransformer transformer = new SourceTransformer();
        
            // transform the message content to a DOM document
            Document document = transformer.toDOMDocument(content);
            document.getDocumentElement().normalize();
        
            // get the query node
            NodeList queryNode = document.getElementsByTagName("query");
            if (queryNode == null) {
                throw new TransformerException("Invalid message content. The message doesn't contain query tag.");
            }
            if (queryNode.getLength() > 1) {
                throw new TransformerException("Invalid message content. Only one query tag is allowed.");
            }
        
            // return the query
            query = queryNode.item(0).getChildNodes().item(0).getNodeValue();
        } catch (Exception e) {
            throw new TransformerException(e);
        }
        return query;
    }

}
