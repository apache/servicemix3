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
package org.apache.servicemix.examples.intermediary;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import static org.apache.camel.builder.xml.XPathBuilder.xpath;
import org.apache.camel.spring.SpringRouteBuilder;

/**
 * Reusable route
 */
public class IntermediaryRoutes extends SpringRouteBuilder {

    public void configure() throws Exception {
        errorHandler(loggingErrorHandler());

        from("jhc:http://localhost:8080/requests").
            group("Client Request").
            tryBlock().
                convertBodyTo(String.class).
                to("activemq:queue:requests").
                setOutBody(constant("<ack xmlns='urn:frontend'/>")).
            handle(Throwable.class).
                setOutBody(constant("<nack xmlns='urn:frontend'/>"));

        from("activemq:queue:requests?transacted=true").
            group("Backend request").
            process(new RequestTransformer()).
            to("jhc:http://localhost:9090/requests").
            convertBodyTo(String.class).
            filter(xpath("count(//nack) > 0")).
            process(new NackTransformer()).
            process(new DbStorer());


        from("jhc:http://localhost:8081/responses").
            group("Backend response").
            tryBlock().
                convertBodyTo(String.class).
                to("activemq:queue:responses").
                setOutBody(constant("<ack/>")).
            handle(Throwable.class).
                setFaultBody(constant("<nack/>"));

        from("activemq:queue:responses?transacted=true").
            group("Response processing").
            process(new ResponseTransformer()).
            process(new DbStorer());

        from("jhc:http://localhost:8082/responses").
            group("Client response").
            convertBodyTo(String.class).
            process(new DbLoader());
    }

    class RequestTransformer implements Processor {
        public void process(Exchange exchange) throws Exception {
            System.err.println("RequestTransformer: input: " + exchange.getIn().getBody());
            Document doc = exchange.getIn().getBody(Document.class);
            String id = doc.getDocumentElement().getAttribute("id");
            exchange.getIn().setBody("<request id='" + id + "' xmlns='urn:backend'/>");
            System.err.println("RequestTransformer: output: " + exchange.getIn().getBody());
        }
    }

    class NackTransformer implements Processor {
        public void process(Exchange exchange) throws Exception {
            System.err.println("NackTransformer: input: " + exchange.getIn().getBody());
            Document doc = exchange.getIn().getBody(Document.class);
            String id = doc.getDocumentElement().getAttribute("id");
            exchange.getIn().setBody("<nack id='" + id + "' xmlns='urn:frontend'/>");
            System.err.println("NackTransformer: output: " + exchange.getIn().getBody());
        }
    }

    class ResponseTransformer implements Processor {
        public void process(Exchange exchange) throws Exception {
            System.err.println("ResponseTransformer: input: " + exchange.getIn().getBody());
            Document doc = exchange.getIn().getBody(Document.class);
            String id = doc.getDocumentElement().getAttribute("id");
            exchange.getIn().setBody("<response id='" + id + "' xmlns='urn:frontend'/>");
            System.err.println("ResponseTransformer: output: " + exchange.getIn().getBody());
        }
    }

    private Map<String, Object> requests = new ConcurrentHashMap<String, Object>();

    class DbLoader implements Processor {
        public void process(Exchange exchange) throws Exception {
            System.err.println("DbLoader: input: " + exchange.getIn().getBody());
            Document doc = exchange.getIn().getBody(Document.class);
            String id = doc.getDocumentElement().getAttribute("id");
            Object rep = requests.remove(id);
            exchange.getOut().setBody(rep);
            System.err.println("DbLoader: output: " + exchange.getOut().getBody());
        }
    }

    class DbStorer implements Processor {
        public void process(Exchange exchange) throws Exception {
            System.err.println("DbStorer: input: " + exchange.getIn().getBody());
            Document doc = exchange.getIn().getBody(Document.class);
            String id = doc.getDocumentElement().getAttribute("id");
            requests.put(id, exchange.getIn().getBody());
        }
    }

}
