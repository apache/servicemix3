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
package org.apache.servicemix.jbi.security.acl.impl;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.security.acl.AuthorizationMap;


/**
 * 
 * @author gnodet
 * @org.apache.xbean.XBean element="authorizationMap"
 */
public class DefaultAuthorizationMap implements AuthorizationMap {

    private AuthorizationEntry defaultEntry;
    private List<AuthorizationEntry> authorizationEntries;

    public DefaultAuthorizationMap() {
    }
    
    public DefaultAuthorizationMap(List<AuthorizationEntry> authorizationEntries) {
        this.authorizationEntries = authorizationEntries;
    }
    
    /**
     * @return the authorizationEntries
     */
    public List<AuthorizationEntry> getAuthorizationEntries() {
        return authorizationEntries;
    }

    /**
     * @param authorizationEntries the authorizationEntries to set
     * @org.apache.xbean.Property nestedType=""
     */
    public void setAuthorizationEntries(List<AuthorizationEntry> authorizationEntries) {
        this.authorizationEntries = authorizationEntries;
    }

    /**
     * @return the defaultEntry
     */
    public AuthorizationEntry getDefaultEntry() {
        return defaultEntry;
    }

    /**
     * @param defaultEntry the defaultEntry to set
     */
    public void setDefaultEntry(AuthorizationEntry defaultEntry) {
        this.defaultEntry = defaultEntry;
    }

    public Set<Principal> getAcls(ServiceEndpoint endpoint, QName operation) {
        Set<Principal> acls = new HashSet<Principal>();
        if (defaultEntry != null) {
            acls.addAll(defaultEntry.getAcls());
        }
        for (AuthorizationEntry entry : authorizationEntries) {
            if (match(entry, endpoint, operation)) {
                if (AuthorizationEntry.TYPE_ADD.equalsIgnoreCase(entry.getType())) {
                    acls.addAll(entry.getAcls());
                } else if (AuthorizationEntry.TYPE_SET.equalsIgnoreCase(entry.getType())) {
                    acls.clear();
                    acls.addAll(entry.getAcls());
                } else if (AuthorizationEntry.TYPE_REM.equalsIgnoreCase(entry.getType())) {
                    acls.removeAll(entry.getAcls());
                }
            }
        }
        return acls;
    }

    protected boolean match(AuthorizationEntry entry, ServiceEndpoint endpoint, QName operation) {
        return match(entry.getService(), endpoint.getServiceName())
            && match(entry.getEndpoint(), endpoint.getEndpointName())
            && (entry.getOperation() == null || operation == null || match(entry.getOperation(), operation));
    }

    private boolean match(QName acl, QName target) {
        return match(acl.getNamespaceURI(), target.getNamespaceURI())
            && match(acl.getLocalPart(), target.getLocalPart());
    }

    private boolean match(String acl, String target) {
        return acl == null
            || acl.equals("*")
            || Pattern.matches(acl, target);
    }

}
