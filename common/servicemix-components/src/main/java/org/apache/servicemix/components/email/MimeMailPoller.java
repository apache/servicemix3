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
package org.apache.servicemix.components.email;

import javax.jbi.JBIException;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.NormalizedMessage;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import org.apache.servicemix.components.util.PollingComponentSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A polling component which looks for emails in a mail server and sends them
 * into the JBI bus as messages, deleting the messages by default when they
 * are processed.
 * 
 * @version $Revision$
 */
public class MimeMailPoller extends PollingComponentSupport {
	
	private static Logger logger = LoggerFactory.getLogger(MimeMailPoller.class);
	
	private Session session;
	private String hostName;
	private String userName;
	private String password;
	private String mailBox;
	private boolean debug;
	private int maxFetchSize = 5;
	private MimeMailMarshaler marshaler = new MimeMailMarshaler();

	protected void init() throws JBIException {
		super.init();
		if (session == null)  {
			logger.debug("No Session informed. Using default instance");
			this.session = Session.getDefaultInstance(System.getProperties());
		}
		if (mailBox == null) {
			logger.debug("No mailbox informed. Using INBOX");
			mailBox = "INBOX";
		}
		if (hostName == null) {
			throw new JBIException("HostName not informed");
		}
		if (userName == null) {
			throw new JBIException("UserName not informed");
		}
		if (password == null) {
			throw new JBIException("Password not informed");
		}
		if (maxFetchSize < 1) {
			throw new JBIException("Fetch Size must be at least 1");
		}
	}
	
	/**
	 * @return Returns the hostName.
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @param hostName The hostName to set.
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @return Returns the marshaler.
	 */
	public MimeMailMarshaler getMarshaler() {
		return marshaler;
	}

	/**
	 * @param marshaler The marshaler to set.
	 */
	public void setMarshaler(MimeMailMarshaler marshaler) {
		this.marshaler = marshaler;
	}

	public void poll() throws Exception {
		Store store = null;
		Folder folder = null;
		try {
			session.setDebug(isDebug());
			store = session.getStore((mailBox.equals("INBOX")) ? "pop3"
					: "imap");
			store.connect(hostName, userName, password);
			folder = store.getFolder(mailBox);
			if (folder == null || !folder.exists()) {
				throw new Exception("Folder not found or invalid: " + mailBox);
			}
			folder.open(Folder.READ_WRITE);
			int msgCount = Math.min(folder.getMessageCount(),maxFetchSize);
			DeliveryChannel channel = getDeliveryChannel();
			MessageExchangeFactory mef = getExchangeFactory();
		    for(int i=1; i <= msgCount;i++) {
		    	MimeMessage mailMsg = (MimeMessage) folder.getMessage(i);
		    	InOnly io = mef.createInOnlyExchange();
		    	NormalizedMessage normalizedMessage = io.createMessage();
		    	this.marshaler.prepareExchange(io,normalizedMessage,mailMsg);
		    	io.setInMessage(normalizedMessage);
		    	channel.send(io);
		        mailMsg.setFlag(Flags.Flag.DELETED,true);
		    }
		} finally {
			try {
				if (folder != null) {
					folder.close(true);
				}
				if (store != null) {
					store.close();
				}
			} catch (Exception ignored) {}
		}
	}

	/**
	 * @return Returns the debug.
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param debug
	 *            The debug to set.
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * @return Returns the mailBox.
	 */
	public String getMailBox() {
		return mailBox;
	}

	/**
	 * @param mailBox
	 *            The mailBox to set.
	 */
	public void setMailBox(String mailBox) {
		this.mailBox = mailBox;
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return Returns the userName.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            The userName to set.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return Returns the session.
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * @param session
	 *            The session to set.
	 */
	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * @return Returns the maxFetchSize.
	 */
	public int getMaxFetchSize() {
		return maxFetchSize;
	}

	/**
	 * @param maxFetchSize The maxFetchSize to set.
	 */
	public void setMaxFetchSize(int maxFetchSize) {
		this.maxFetchSize = maxFetchSize;
	}

}
