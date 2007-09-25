package org.apache.servicemix.examples.intermediary;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.model.language.ExpressionType;
import org.apache.camel.spring.SpringRouteBuilder;

/**
 * Reusable route
 */
public class IntermediaryRoutes extends SpringRouteBuilder {

    private String name;
    private String ack;
    private String nack;

    public void configure() throws Exception {
        // Endpoint ids
        String request             = "ref:" + name + ".request";
        String requestProvider     = "ref:" + name + ".requestProvider";
        String responseConsumer    = "ref:" + name + ".responseConsumer";
        String response            = "ref:" + name + ".response";
        String dbStorer            = "ref:" + name + ".dbStorer";
        String dbLoader            = "ref:" + name + ".dbLoader";
        String requestTransformer  = "ref:" + name + ".requestTransformer";
        String responseTransformer = "ref:" + name + ".responseTransformer";
        String nackTransformer     = "ref:" + name + ".nackTransformer";
        // Built-in endpoints
        String requestStorage      = "activemq:queue:" + name + ".requests?transacted=true";
        String responseStorage     = "activemq:queue:" + name + ".responses?transacted=true";
        // Bean references
        Predicate isNack           = bean(Predicate.class, name + ".isNackExpression");

        from(request).
            group(name + ": Client Request").
            tryBlock().
                to(requestStorage).
                setOutBody(constant(ack)).
            handle(Throwable.class).
                setFaultBody(constant(nack));

        from(requestStorage).
            group(name + ": Backend request").
            to(requestTransformer).
            to(requestProvider).
            filter(isNack).
            to(nackTransformer).
            to(dbStorer);

        from(responseConsumer).
            group(name + ": Backend response").
            tryBlock().
                to(responseStorage).
                setOutBody(constant(ack)).
            handle(Throwable.class).
                setFaultBody(constant(nack));

        from(responseStorage).
            group(name + ": Response processing").
            to(responseTransformer).
            to(dbStorer);

        from(response).
            group(name + ": Client response").
            to(dbLoader);
    }

    public String getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }

    public String getNack() {
        return nack;
    }

    public void setNack(String nack) {
        this.nack = nack;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
