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
package org.apache.servicemix.jbi.security;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

/**
 * 
 * @author gnodet
 * @org.apache.xbean.XBean element="authorizationMap"
 */
public class DefaultAuthorizationMap implements AuthorizationMap {

    private AuthorizationEntry defaultEntry;
    private List authorizationEntries;

    public DefaultAuthorizationMap() {
    }
    
    public DefaultAuthorizationMap(List authorizationEntries) {
        this.authorizationEntries = authorizationEntries;
    }
    
    /**
     * @return the authorizationEntries
     */
    public List getAuthorizationEntries() {
        return authorizationEntries;
    }

    /**
     * @param authorizationEntries the authorizationEntries to set
     * @org.apache.xbean.ElementType class="org.apache.servicemix.jbi.security.AuthorizationEntry"
     */
    public void setAuthorizationEntries(List authorizationEntries) {
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

    public Set getAcls(ServiceEndpoint endpoint) {
        Set acls = new HashSet();
        if (defaultEntry != null) {
            acls.add(defaultEntry);
        }
        for (Iterator iter = authorizationEntries.iterator(); iter.hasNext();) {
            AuthorizationEntry entry = (AuthorizationEntry) iter.next();
            if (match(entry, endpoint)) {
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

    protected boolean match(AuthorizationEntry entry, ServiceEndpoint endpoint) {
        return match(entry.getService(), endpoint.getServiceName()) &&
               match(entry.getEndpoint(), endpoint.getEndpointName());
    }

    private boolean match(QName acl, QName target) {
        return match(acl.getNamespaceURI(), target.getNamespaceURI()) &&
               match(acl.getLocalPart(), target.getLocalPart());
    }

    private boolean match(String acl, String target) {
        return acl == null ||
               acl.equals("*") ||
               Pattern.matches(acl, target);
    }

}
