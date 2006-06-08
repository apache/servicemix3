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

import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.servicemix.soap.Context;
import org.apache.servicemix.soap.Handler;
import org.apache.servicemix.soap.SoapFault;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

/**
 * WS-Security handler.
 * This handler is heavily based on xfire-ws-security project.
 * 
 * @org.apache.xbean.XBean element="ws-security"
 */
public class WSSecurityHandler extends WSHandler implements Handler {

    private Map properties = new HashMap();
    private String domain = "servicemix-domain";

    private boolean required;
    private String sendAction;
    private String receiveAction;
    private String actor;
    private CallbackHandler handler = new DefaultHandler();
    
    private ThreadLocal currentSubject = new ThreadLocal();

    /**
     * @return the actor
     */
    public String getActor() {
        return actor;
    }

    /**
     * @param actor the actor to set
     */
    public void setActor(String actor) {
        this.actor = actor;
    }

    /**
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * @return the receiveAction
     */
    public String getReceiveAction() {
        return receiveAction;
    }

    /**
     * @param receiveAction the receiveAction to set
     */
    public void setReceiveAction(String receiveAction) {
        this.receiveAction = receiveAction;
    }

    /**
     * @return the action
     */
    public String getSendAction() {
        return sendAction;
    }

    /**
     * @param action the action to set
     */
    public void setSendAction(String action) {
        this.sendAction = action;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    public boolean requireDOM() {
        return true;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    public Object getOption(String key) {
        return properties.get(key);
    }

    public void setOption(String key, Object value) {
        this.properties.put(key, value);
    }

    public String getPassword(Object msgContext) {
        return (String) ((Context) msgContext).getProperty("password");
    }

    public Object getProperty(Object msgContext, String key) {
        return ((Context) msgContext).getProperty(key); 
    }

    public void setPassword(Object msgContext, String password) {
        ((Context) msgContext).setProperty("password", password);
    }

    public void setProperty(Object msgContext, String key, Object value) {
        ((Context) msgContext).setProperty(key, value);
    }

    public void onComplete(Context context) {
        // TODO Auto-generated method stub

    }

    public void onException(Context context, Exception e) {
        // TODO Auto-generated method stub

    }

    public void onFault(Context context) throws Exception {
        // TODO Auto-generated method stub

    }

    public void onReceive(Context context) throws Exception {
        RequestData reqData = new RequestData();
        currentSubject.set(null);
        try {
            reqData.setMsgContext(context);

            Vector actions = new Vector();
            String action = this.receiveAction;
            if (action == null) {
                throw new IllegalStateException("WSSecurityHandler: No receiveAction defined");
            }
            int doAction = WSSecurityUtil.decodeAction(action, actions);

            Source src = context.getInMessage().getSource();
            if (src instanceof DOMSource == false) {
                throw new IllegalStateException("WSSecurityHandler: The soap message has not been parsed using DOM");
            }
            Document doc = ((DOMSource) src).getNode().getOwnerDocument();

            /*
             * Get and check the Signature specific parameters first because
             * they may be used for encryption too.
             */
            doReceiverAction(doAction, reqData);

            Vector wsResult = null;

            try {
                wsResult = secEngine.processSecurityHeader(
                                doc, actor, handler, 
                                reqData.getSigCrypto(), 
                                reqData.getDecCrypto());
            } catch (WSSecurityException ex) {
                throw new SoapFault(ex);
            }

            if (wsResult == null) { // no security header found
                if (doAction == WSConstants.NO_SECURITY) {
                    return;
                } else {
                    throw new SoapFault(new WSSecurityException(
                                    "WSSecurityHandler: Request does not contain required Security header"));
                }
            }

            if (reqData.getWssConfig().isEnableSignatureConfirmation()) {
                checkSignatureConfirmation(reqData, wsResult);
            }

            /*
             * Now we can check the certificate used to sign the message. In the
             * following implementation the certificate is only trusted if
             * either it itself or the certificate of the issuer is installed in
             * the keystore.
             * 
             * Note: the method verifyTrust(X509Certificate) allows custom
             * implementations with other validation algorithms for subclasses.
             */

            // Extract the signature action result from the action vector
            WSSecurityEngineResult actionResult = WSSecurityUtil.fetchActionResult(wsResult, WSConstants.SIGN);

            if (actionResult != null) {
                X509Certificate returnCert = actionResult.getCertificate();

                if (returnCert != null) {
                    if (!verifyTrust(returnCert, reqData)) {
                        throw new SoapFault(new WSSecurityException(
                                        "WSSecurityHandler: the certificate used for the signature is not trusted"));
                    }
                }
            }

            /*
             * Perform further checks on the timestamp that was transmitted in
             * the header. In the following implementation the timestamp is
             * valid if it was created after (now-ttl), where ttl is set on
             * server side, not by the client.
             * 
             * Note: the method verifyTimestamp(Timestamp) allows custom
             * implementations with other validation algorithms for subclasses.
             */

            // Extract the timestamp action result from the action vector
            actionResult = WSSecurityUtil.fetchActionResult(wsResult, WSConstants.TS);

            if (actionResult != null) {
                Timestamp timestamp = actionResult.getTimestamp();

                if (timestamp != null) {
                    if (!verifyTimestamp(timestamp, decodeTimeToLive(reqData))) {
                        throw new SoapFault(new WSSecurityException(
                                        "WSSecurityHandler: the timestamp could not be validated"));
                    }
                }
            }

            /*
             * now check the security actions: do they match, in right order?
             */
            if (!checkReceiverResults(wsResult, actions)) {
                throw new SoapFault(new WSSecurityException(
                                "WSSecurityHandler: security processing failed (actions mismatch)"));

            }
            /*
             * All ok up to this point. Now construct and setup the security
             * result structure. The service may fetch this and check it.
             */
            Vector results = null;
            if ((results = (Vector) context.getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
                results = new Vector();
                context.setProperty(WSHandlerConstants.RECV_RESULTS, results);
            }
            WSHandlerResult rResult = new WSHandlerResult(actor, wsResult);
            results.add(0, rResult);

            // Add principals to the message
            for (Iterator iter = results.iterator(); iter.hasNext();) {
                WSHandlerResult hr = (WSHandlerResult) iter.next();
                for (Iterator it = hr.getResults().iterator(); it.hasNext();) {
                    WSSecurityEngineResult er = (WSSecurityEngineResult) it.next();
                    if (er.getPrincipal() != null) {
                        context.getInMessage().addPrincipal(er.getPrincipal());
                    }
                }
            }
            Subject s = (Subject) currentSubject.get();
            if (s != null) {
                for (Iterator iterator = s.getPrincipals().iterator(); iterator.hasNext();) {
                    Principal p = (Principal) iterator.next();
                    context.getInMessage().addPrincipal(p);
                }
            }

        } finally {
            reqData.clear();
            currentSubject.set(null);
        }
    }

    public void onReply(Context context) throws Exception {
        // TODO Auto-generated method stub

    }
    
    protected class DefaultHandler extends BaseSecurityCallbackHandler {

        protected void processUsernameTokenUnkown(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
            /* either an not specified 
             * password type or a password type passwordText. In these both cases <b>only</b>
             * the password variable is <b>set</>. The callback class now may check if
             * the username and password match. If they don't match the callback class must
             * throw an exception. The exception can be a UnsupportedCallbackException or
             * an IOException.</li>
             */
            final String username = callback.getIdentifer();
            final String password = callback.getPassword();
            Subject subject = (Subject) currentSubject.get();
            if (subject == null) {
                subject = new Subject();
                currentSubject.set(subject);
            }
            try {
                LoginContext loginContext = new LoginContext(domain, subject, new CallbackHandler() {
                    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                        for (int i = 0; i < callbacks.length; i++) {
                            if (callbacks[i] instanceof NameCallback) {
                                ((NameCallback) callbacks[i]).setName(username);
                            } else if (callbacks[i] instanceof PasswordCallback) {
                                ((PasswordCallback) callbacks[i]).setPassword(password.toCharArray());
                            } else {
                                throw new UnsupportedCallbackException(callbacks[i]);
                            }
                        }
                    }
                });
                loginContext.login();
            } catch (LoginException e) {
                throw new UnsupportedCallbackException(callback, "Unable to authenticate user");
            }
        }
        
    }

}
