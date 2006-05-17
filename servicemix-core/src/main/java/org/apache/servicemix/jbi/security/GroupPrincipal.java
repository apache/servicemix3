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

import java.security.Principal;


/**
 * 
 */
public class GroupPrincipal implements Principal {

    private final String name;
    private transient int hash;

    public GroupPrincipal(String name) {
        if (name == null) throw new IllegalArgumentException("name cannot be null");
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GroupPrincipal that = (GroupPrincipal) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    public int hashCode() {
        if (hash == 0) {
            hash = name.hashCode();
        }
        return hash;
    }

    public String toString() {
        return name;
    }
}
