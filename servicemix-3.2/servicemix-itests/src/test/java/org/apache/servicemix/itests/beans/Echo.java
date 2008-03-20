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
package org.apache.servicemix.itests.beans;

import javax.activation.DataSource;
import javax.jws.WebMethod;
import javax.jws.WebService;

import org.apache.servicemix.jbi.util.ByteArrayDataSource;

@WebService
public class Echo {
    
    public static class Request {
        private String msg;
        public String getMsg() {
            return msg;
        }
        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    public static class Response {
        private String msg;
        public String getMsg() {
            return msg;
        }
        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    @WebMethod
    public Response echo(Request req) {
        Response r = new Response();
        r.setMsg("Hello: " + req.getMsg());
        return r;
    }
    
    @WebMethod
    public DataSource mtom(int id) {
        return new ByteArrayDataSource("<xsl:stylesheet />".getBytes(), "text/xml");
    }
}
