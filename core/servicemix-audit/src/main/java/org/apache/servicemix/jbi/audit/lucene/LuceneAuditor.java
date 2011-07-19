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
package org.apache.servicemix.jbi.audit.lucene;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.servicemix.jbi.audit.AbstractAuditor;
import org.apache.servicemix.jbi.audit.AuditorException;
import org.apache.servicemix.jbi.audit.AuditorMBean;
import org.apache.servicemix.jbi.audit.AuditorQueryMBean;
import org.apache.servicemix.jbi.event.ExchangeEvent;
import org.apache.servicemix.jbi.event.ExchangeListener;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;

/**
 * Lucene AuditorQuery implementation. It uses Lucene as the indexing mechanism
 * for searching Exchanges and needs a delegated AuditorMBean to persist
 * Exchanges.
 * 
 * The Content of messages are stored as: 
 *  - org.apache.servicemix.in.contents
 *  - org.apache.servicemix.out.contents, if exists
 *  - org.apache.servicemix.fault.contents, if exists
 * 
 * Properties for IN Messages are stored as: 
 *  - org.apache.servicemix.in.propertyname
 *  - org.apache.servicemix.out.propertyname, if exists
 *  - org.apache.servicemix.fault.propertyname, if exists
 * 
 * @author George Gastaldi
 * @since 2.1
 * @version $Revision$
 */
public class LuceneAuditor extends AbstractAuditor implements AuditorQueryMBean {

    private AuditorMBean delegatedAuditor;

    private LuceneIndexer luceneIndexer = new LuceneIndexer();

    protected void doStart() throws JBIException {
        super.doStart();
        if (delegatedAuditor == null) {
            throw new JBIException("A delegated auditor must be provided");
        }
        this.delegatedAuditor.start();
    }

    protected void doStop() throws JBIException {
        super.doStop();
        this.delegatedAuditor.stop();
    }

    /**
     * @return Returns the luceneIndexer.
     */
    public LuceneIndexer getLuceneIndexer() {
        return luceneIndexer;
    }

    /**
     * @param luceneIndexer
     *            The luceneIndexer to set.
     */
    public void setLuceneIndexer(LuceneIndexer luceneIndexer) {
        this.luceneIndexer = luceneIndexer;
    }

    /**
     * @return Returns the delegatedAuditor.
     */
    public AuditorMBean getDelegatedAuditor() {
        return delegatedAuditor;
    }

    /**
     * @param delegatedAuditor
     *            The delegatedAuditor to set.
     */
    public void setDelegatedAuditor(AuditorMBean delegatedAuditor) {
        this.delegatedAuditor = delegatedAuditor;
        if (delegatedAuditor instanceof AbstractAuditor) {
            ((AbstractAuditor) delegatedAuditor).setAsContainerListener(false);
        }
    }

    public int getExchangeCount() throws AuditorException {
        return this.delegatedAuditor.getExchangeCount();
    }

    public String[] getExchangeIdsByRange(int fromIndex, int toIndex) throws AuditorException {
        return this.delegatedAuditor.getExchangeIdsByRange(fromIndex, toIndex);
    }

    public MessageExchange[] getExchangesByIds(String[] ids) throws AuditorException {
        return this.delegatedAuditor.getExchangesByIds(ids);
    }

    public int deleteExchangesByRange(int fromIndex, int toIndex) throws AuditorException {
        // TODO: Remove ids from Lucene Index
        return this.delegatedAuditor.deleteExchangesByRange(fromIndex, toIndex);
    }

    public int deleteExchangesByIds(String[] ids) throws AuditorException {
        try {
            this.luceneIndexer.remove(ids);
        } catch (IOException io) {
            throw new AuditorException(io);
        }
        return this.delegatedAuditor.deleteExchangesByIds(ids);
    }

    public void exchangeSent(ExchangeEvent event) {
        MessageExchange exchange = event.getExchange();
        try {
            Document doc = createDocument(exchange);
            this.luceneIndexer.add(doc, exchange.getExchangeId());
            if (delegatedAuditor instanceof ExchangeListener) {
                ((ExchangeListener) delegatedAuditor).exchangeSent(event);
            }
        } catch (Exception e) {
            LOGGER.error("Error while adding to lucene", e);
        }
    }

    public String getDescription() {
        return "Lucene Auditor";
    }

    public String[] findExchangesIDsByStatus(ExchangeStatus status) throws AuditorException {
        String field = "org.apache.servicemix.exchangestatus";
        return getExchangeIds(field, String.valueOf(status));
    }

    public String[] findExchangesIDsByMessageContent(String type, String content) throws AuditorException {
        String field = "org.apache.servicemix." + type + ".contents";
        return getExchangeIds(field, content);
    }

    public String[] findExchangesIDsByMessageProperty(String type, 
                                                      String property, 
                                                      String value) throws AuditorException {
        if (property != null && !property.startsWith("org.apache.servicemix")) {
            property = "org.apache.servicemix." + type + "." + property;
        }
        return getExchangeIds(property, value);
    }

    protected Document createDocument(MessageExchange me) throws MessagingException {
        try {
            // This could be in a separated class (a LuceneDocumentProvider)
            SourceTransformer st = new SourceTransformer();
            Document d = new Document();
            d.add(Field.Keyword("org.apache.servicemix.exchangeid", me.getExchangeId()));
            d.add(Field.Keyword("org.apache.servicemix.exchangestatus", String.valueOf(me.getStatus())));

            String[] types = {"in", "out", "fault" };
            for (int i = 0; i < types.length; i++) {
                String type = types[i];
                NormalizedMessage nm = me.getMessage(type);
                if (nm != null) {
                    d.add(Field.UnStored("org.apache.servicemix." + type + ".contents", st.contentToString(nm)));
                    addMessagePropertiesToDocument(nm, d, type);
                }
            }
            return d;
        } catch (MessagingException mse) {
            throw mse;
        } catch (Exception ex) {
            throw new MessagingException("Error while creating Lucene Document", ex);
        }
    }

    protected void addMessagePropertiesToDocument(NormalizedMessage nm, 
                                                  Document document, 
                                                  String type) throws MessagingException {
        Set propertyNames = nm.getPropertyNames();
        for (Iterator iter = propertyNames.iterator(); iter.hasNext();) {
            String propertyName = (String) iter.next();
            Object value = nm.getProperty(propertyName);
            if (value instanceof String) {
                //org.apache.servicemix.out.myproperty
                document.add(Field.Keyword("org.apache.servicemix." + type + "." + propertyName, String.valueOf(value)));
            }
        }
    }

    public String[] getExchangeIds(String queryContent, String field) throws AuditorException {
        DefaultLuceneCallback dfc = new DefaultLuceneCallback(queryContent, field);
        try {
            return (String[]) luceneIndexer.search(dfc);
        } catch (IOException e) {
            throw new AuditorException("Error while getting Exchange IDs", e);
        }
    }
}
