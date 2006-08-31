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
package org.apache.servicemix.jbi.management;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

public class PasswordAuthenticatorFactoryBean implements FactoryBean {

    private Resource passwords;
    private PasswordAuthenticator authenticator;
    
    /**
     * @return the passwords
     */
    public Resource getPasswords() {
        return passwords;
    }

    /**
     * @param passwords the passwords to set
     */
    public void setPasswords(Resource passwords) {
        this.passwords = passwords;
    }

    public Object getObject() throws Exception {
        if (authenticator == null) {
            authenticator = new PasswordAuthenticator(passwords.getInputStream());
        }
        return authenticator;
    }

    public Class getObjectType() {
        return PasswordAuthenticator.class;
    }

    public boolean isSingleton() {
        return true;
    }

}
