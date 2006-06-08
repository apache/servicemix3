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
package org.apache.servicemix.soap.handlers;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import junit.framework.TestCase;

import org.apache.servicemix.soap.Context;
import org.apache.servicemix.soap.handlers.security.WSSecurityHandler;
import org.apache.servicemix.soap.marshalers.SoapMarshaler;
import org.apache.servicemix.soap.marshalers.SoapMessage;
import org.apache.servicemix.soap.marshalers.SoapReader;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

public class WSSecurityHandlerTest extends TestCase {
    
    public void testUserNameToken() throws Exception {
        SoapMarshaler marshaler = new SoapMarshaler(true, true);
        SoapReader reader = marshaler.createReader();
        SoapMessage msg = reader.read(getClass().getResourceAsStream("sample-wsse-request.xml"));
        Context ctx = new Context();
        ctx.setInMessage(msg);
        
        WSSecurityHandler handler = new WSSecurityHandler();
        handler.setOption(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        ctx.setProperty(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                System.err.println("Callback");
            } 
        });
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
        assertEquals("cupareq", ((WSUsernameTokenPrincipal) principal).getName());
        assertEquals("cupareq1", ((WSUsernameTokenPrincipal) principal).getPassword());
        assertNotNull(ctx.getInMessage().getSubject());
        assertNotNull(ctx.getInMessage().getSubject().getPrincipals());
        assertEquals(1, ctx.getInMessage().getSubject().getPrincipals().size());
    }

}
