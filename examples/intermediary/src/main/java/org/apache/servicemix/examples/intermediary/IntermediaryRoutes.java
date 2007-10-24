package org.apache.servicemix.examples.intermediary;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.model.language.ExpressionType;
import org.apache.camel.spring.SpringRouteBuilder;

/**
 * Reusable route
 */
public class IntermediaryRoutes extends SpringRouteBuilder {

    public void configure() throws Exception {
        from("jhc:http://localhost:8080/requests").
            group("Client Request").
            tryBlock().
                to("activemq:queue:requests").
                setOutBody(constant("<ack/>")).
            handle(Throwable.class).
                setFaultBody(constant("<nack/>"));

        from("activemq:queue:requests?transacted=true").
            group("Backend request").
            to("ref:requestTransformer").
            to("jhc:http://localhost:9090/requests").
            filter(bean(Predicate.class, "isNackExpression")).
            to("ref:nackTransformer").
            to("seda:store");

        from("jhc:http://localhost:8081/responses").
            group("Backend response").
            tryBlock().
                to("activemq:queue:responses").
                setOutBody(constant("<ack/>")).
            handle(Throwable.class).
                setFaultBody(constant("<nack/>"));

        from("activemq:queue:responses?transacted=true").
            group("Response processing").
            to("ref:responseTransformer").
            to("ref:dbStore");

        from("jhc:http://localhost:8082/responses").
            group("Client response").
            to("ref:dbLoader");
    }

}
