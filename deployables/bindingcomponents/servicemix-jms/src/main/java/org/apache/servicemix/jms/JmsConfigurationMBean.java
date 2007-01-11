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
package org.apache.servicemix.jms;

public interface JmsConfigurationMBean {

    /**
     * @return Returns the password.
     */
    public String getPassword();
    /**
     * @param password The password to set.
     */
    public void setPassword(String password);
    /**
     * @return Returns the userName.
     */
    public String getUserName();
    /**
     * @param userName The userName to set.
     */
    public void setUserName(String userName);
    /**
     * @return Returns the jndiConnectionFactoryName.
     */
    public String getJndiConnectionFactoryName();
    /**
     * @param jndiConnectionFactoryName The jndiName to set.
     */
    public void setJndiConnectionFactoryName(String jndiConnectionFactoryName);
    /**
     * @return Returns the jndiInitialContextFactory.
     */
    public String getJndiInitialContextFactory();
    /**
     * @param jndiInitialContextFactory The jndiInitialContextFactory to set.
     */
    public void setJndiInitialContextFactory(String jndiInitialContextFactory);
    /**
     * @return Returns the jndiProviderUrl.
     */
    public String getJndiProviderUrl();
    /**
     * @param jndiProviderUrl The jndiProviderUrl to set.
     */
    public void setJndiProviderUrl(String jndiProviderUrl);
    /**
     * @return Returns the processName.
     */
    public String getProcessorName();
    /**
     * @param processorName The processorName to set.
     */
    public void setProcessorName(String processorName);

    public String getKeystoreManagerName();
    
    public void setKeystoreManagerName(String name);
    
    public String getAuthenticationServiceName();
    
    public void setAuthenticationServiceName(String name);
    
}