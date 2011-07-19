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
package org.apache.servicemix.components.rss;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.jbi.JBIException;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import org.apache.servicemix.components.util.PollingComponentSupport;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RssPollingComponent polls for updates to RSS feeds
 * 
 * @version $Revision$
 */
public class RssPollingComponent extends PollingComponentSupport {

    private static final Logger logger = LoggerFactory.getLogger(RssPollingComponent.class);

    private List urlStrings = new ArrayList();
    private List urls = new ArrayList();
    private Date lastPolledDate = new Date();
    private String outputType = "rss_2.0";

    /**
     * @return Returns the urlStrings.
     */
    public List getUrlStrings() {
        return urlStrings;
    }

    /**
     * @param urlStrings The urlStrings to set.
     */
    public void setUrlStrings(List urlStrings) {
        this.urlStrings = urlStrings;
    }

    /**
     * @return Returns the outputType.
     */
    public String getOutputType() {
        return outputType;
    }

    /**
     * @param outputType The outputType to set.
     */
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    /**
     * @return Returns the lastPolledDate.
     */
    public Date getLastPolledDate() {
        return lastPolledDate;
    }

    /**
     * @param lastPolledDate The lastPolledDate to set.
     */
    public void setLastPolledDate(Date lastPolledDate) {
        this.lastPolledDate = lastPolledDate;
    }

   
    
    protected void init() throws JBIException {
        urls.clear();
        if (urlStrings != null) {
            for (int i = 0;i < urlStrings.size();i++) {
                try {
                    urls.add(new URL(urlStrings.get(i).toString()));
                }
                catch (MalformedURLException e) {
                    logger.warn("URL: {} is badly formed", urlStrings.get(i), e);
                }
            }
        }
        super.init();
    }

    /**
     * Poll for updates
     */
    public void poll() {
        List list = getLastesEntries();
        if (list != null && !list.isEmpty()) {
            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(outputType);
            feed.setTitle("Aggregated Feed");
            feed.setDescription("Anonymous Aggregated Feed");
            feed.setAuthor("servicemix");
            feed.setLink("http://www.servicemix.org");
            feed.setEntries(list);
            // send on to the nmr ...
            SyndFeedOutput output = new SyndFeedOutput();
            try {
                Source source = new DOMSource(output.outputW3CDom(feed));
                InOnly exchange = getExchangeFactory().createInOnlyExchange();
                NormalizedMessage message = exchange.createMessage();
                message.setContent(source);
                exchange.setInMessage(message);
                send(exchange);
            }
            catch (Exception e) {
                logger.error("Failed to send RSS message to the NMR");
            }
            finally {
                lastPolledDate = new Date();
            }
        }
    }

    protected List getLastesEntries() {
        List result = new ArrayList();
        SyndFeedInput input = new SyndFeedInput();
        for (int i = 0;i < urls.size();i++) {
            URL inputUrl = (URL) urls.get(i);
            SyndFeed inFeed;
            try {
                inFeed = input.build(new XmlReader(inputUrl));
                List entries = inFeed.getEntries();
                for (int k = 0;k < entries.size();k++) {
                    SyndEntry entry = (SyndEntry) entries.get(k);
                    if (entry.getPublishedDate().after(getLastPolledDate())) {
                        result.add(entry);
                    }
                }
            }
            catch (Exception e) {
                logger.warn("Failed to process feed from: {}", inputUrl, e);
            }
        }
        return result;
    }

}
