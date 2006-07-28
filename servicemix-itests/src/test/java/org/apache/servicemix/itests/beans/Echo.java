package org.apache.servicemix.itests.beans;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public class Echo {

    @WebMethod
    public String echo(String msg) {
        return "Hello: " + msg;
    }
}
