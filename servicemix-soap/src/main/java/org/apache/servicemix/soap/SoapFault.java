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
package org.apache.servicemix.soap;

import javax.xml.transform.Source;

/**
 * Represents a SOAP fault which occurred while processing the
 * message.
 *
 * @author Guillaume Nodet
 * @version $Revision: 1.5 $
 * @since 3.0
 */
public class SoapFault extends Exception {
	
	public static final String SENDER = "Sender";
	public static final String RECEIVER = "Receiver";
	
    private String code;
    private String subcode;
    private String reason;
    private String node;
    private String role;
    private Source details;

    public SoapFault(Exception cause) {
        super(cause);
    }

    public SoapFault(String code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public SoapFault(String code, String subcode, String reason) {
        this.code = code;
        this.subcode = subcode;
        this.reason = reason;
    }

    public SoapFault(String code, String reason, String node, String role) {
        this.code = code;
        this.reason = reason;
        this.node = node;
        this.role = role;
    }

    public SoapFault(String code, String reason, String node, String role, Source details) {
        this.code = code;
        this.reason = reason;
        this.node = node;
        this.role = role;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public String getSubcode() {
        return subcode;
    }

    public String getReason() {
        return reason;
    }

    public String getNode() {
        return node;
    }

    public String getRole() {
        return role;
    }

	public Source getDetails() {
		return details;
	}
}
