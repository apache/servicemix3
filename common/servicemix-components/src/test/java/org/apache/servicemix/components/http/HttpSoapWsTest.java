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
package org.apache.servicemix.components.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.servicemix.tck.TestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * @version $Revision$
 */
public class HttpSoapWsTest extends TestSupport {

    private static transient Logger logger = LoggerFactory.getLogger(HttpSoapWsTest.class);

    protected String quote = "SUNW";

    public void testWithURLConnection() throws Exception {
        URLConnection connection = new URL("http://localhost:8912").openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        OutputStream os = connection.getOutputStream();

        // Post the request file.
        InputStream fis = getClass().getResourceAsStream("soap-request.xml");
        int c;
        while ((c = fis.read()) >= 0) {
            os.write(c);
        }
        os.flush();
        os.close();
        fis.close();

        // Read the response.
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            logger.info(inputLine);
        }
        in.close();
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/http/ws-example.xml");
    }

}
