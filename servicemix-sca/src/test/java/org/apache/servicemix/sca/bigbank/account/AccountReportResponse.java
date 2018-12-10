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
package org.apache.servicemix.sca.bigbank.account;

import java.util.List;

import javax.xml.bind.annotation.AccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(AccessType.FIELD)
@XmlType(name = "", propOrder = { "accountSummaries" })
@XmlRootElement(name = "AccountReportResponse")
public class AccountReportResponse {

	@XmlElement(name = "AccountSummaries")
	private List<AccountSummary> accountSummaries;
	
	public List<AccountSummary> getAccountSummaries() {
		return accountSummaries;
	}

	public void setAccountSummaries(List<AccountSummary> accountSummaries) {
		this.accountSummaries = accountSummaries;
	} 
	
}
