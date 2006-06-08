/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.soap.handlers.security;

import java.io.File;
import java.net.URL;
import java.security.Principal;
import java.util.List;

import junit.framework.TestCase;

import org.apache.servicemix.soap.Context;
import org.apache.servicemix.soap.marshalers.SoapMarshaler;
import org.apache.servicemix.soap.marshalers.SoapMessage;
import org.apache.servicemix.soap.marshalers.SoapReader;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

public class WSSecurityHandlerTest extends TestCase {
    
    static {
        String path = System.getProperty("java.security.auth.login.config");
        if (path == null) {
            URL resource = WSSecurityHandlerTest.class.getClassLoader().getResource("login.properties");
            if (resource != null) {
                path = new File(resource.getFile()).getAbsolutePath();
                System.setProperty("java.security.auth.login.config", path);
            }
        }
        System.out.println("Path to login config: " + path);
    }

    public void testUserNameToken() throws Exception {
        SoapMarshaler marshaler = new SoapMarshaler(true, true);
        SoapReader reader = marshaler.createReader();
        SoapMessage msg = reader.read(getClass().getResourceAsStream("sample-wsse-request.xml"));
        Context ctx = new Context();
        ctx.setInMessage(msg);
        
        WSSecurityHandler handler = new WSSecurityHandler();
        handler.setReceiveAction(WSHandlerConstants.USERNAME_TOKEN);
        handler.onReceive(ctx);
        List l = (List) ctx.getProperty(WSHandlerConstants.RECV_RESULTS);
        assertNotNull(l);
        assertEquals(1, l.size());
        WSHandlerResult result = (WSHandlerResult) l.get(0);
        assertNotNull(result);
        assertNotNull(result.getResults());
        assertEquals(1, result.getResults().size());
        WSSecurityEngineResult engResult = (WSSecurityEngineResult) result.getResults().get(0);
        assertNotNull(engResult);
        Principal principal = engResult.getPrincipal();
        assertNotNull(principal);
        assertTrue(principal instanceof WSUsernameTokenPrincipal);
        assertEquals("first", ((WSUsernameTokenPrincipal) principal).getName());
        assertEquals("secret", ((WSUsernameTokenPrincipal) principal).getPassword());
        assertNotNull(ctx.getInMessage().getSubject());
        assertNotNull(ctx.getInMessage().getSubject().getPrincipals());
        assertTrue(ctx.getInMessage().getSubject().getPrincipals().size() > 0);
    }
    
}
