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
package org.apache.servicemix.jbi.audit;

import javax.jbi.messaging.ExchangeStatus;

/**
 * Main interface for ServiceMix auditor query.
 * This interface may be used to query upon exchanges.
 * 
 * @author George Gastaldi (gastaldi)
 * @since 2.1
 * @version $Revision$
 */
public interface AuditorQueryMBean extends AuditorMBean {
	String[] findExchangesIDsByStatus(ExchangeStatus status) throws AuditorException;	
	String[] findExchangesIDsByMessageContent(String type, String content) throws AuditorException;
	String[] findExchangesIDsByMessageProperty(String type, String property, String value) throws AuditorException;

	/**
	 * Searches for Exchanges IDs using the supplied key-field and the expected content of the field 
	 * @param field
	 * @param fieldValue
	 * @return exchange ids
	 * @throws AuditorException if an error occurs
	 */
	String[] getExchangeIds(String field, String fieldValue) throws AuditorException;
}