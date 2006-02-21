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
package org.apache.servicemix.sca;

import org.apache.commons.logging.LogFactory;
import org.apache.tuscany.common.logging.Log;
import org.apache.tuscany.common.logging.LogProvider;

public class CommonsLoggingLogProvider implements LogProvider {

	public Log getLog(Class clazz) {
		return new CommonsLoggingLog(LogFactory.getLog(clazz));
	}

	public Log getLog(Class clazz, String resourceBundleName) {
		return new CommonsLoggingLog(LogFactory.getLog(clazz));
	}
	
	public static class CommonsLoggingLog implements Log {

		org.apache.commons.logging.Log log;
		
		public CommonsLoggingLog(org.apache.commons.logging.Log log) {
			this.log = log;
		}
		
		public boolean isDebugEnabled() {
			return log.isDebugEnabled();
		}

		public boolean isEntryEnabled() {
			return log.isTraceEnabled();
		}

		public boolean isEventEnabled() {
			return log.isTraceEnabled();
		}

		public void debug(String message) {
			log.debug(message);
		}

		public void debug(String message, Object obj) {
			if (obj instanceof Throwable) {
				log.debug(message, (Throwable) obj);
			} else {
				log.debug(message + ": " + obj);
			}
		}

		public void event(String message) {
			log.trace(message);
		}

		public void event(String message, Object obj) {
			if (obj instanceof Throwable) {
				log.trace(message, (Throwable) obj);
			} else {
				log.trace(message + ": " + obj);
			}
		}

		public void entry(String message) {
			log.debug(message);
		}

		public void entry(String message, Object obj) {
			if (obj instanceof Throwable) {
				log.trace(message, (Throwable) obj);
			} else {
				log.trace(message + ": " + obj);
			}
		}

		public void exit(String message) {
			log.debug(message);
		}

		public void exit(String message, Object obj) {
			if (obj instanceof Throwable) {
				log.trace(message, (Throwable) obj);
			} else {
				log.trace(message + ": " + obj);
			}
		}

		public void info(String messageKey) {
			log.info(messageKey);
		}

		public void info(String messageKey, Object[] objs) {
			log.info(messageKey + ": " + objs);
		}

		public void warning(String messageKey) {
			log.warn(messageKey);
		}

		public void warning(String messageKey, Object[] objs) {
			log.warn(messageKey);
		}

		public void error(String messageKey) {
			log.error(messageKey);
		}

		public void error(String messageKey, Object[] objs) {
			log.error(messageKey);
		}

		public void error(String messageKey, Throwable throwable) {
			log.error(messageKey, throwable);
		}

		public void fatal(Throwable throwable) {
			log.fatal(null, throwable);
		}

		public void fatal(Throwable throwable, Object[] objs) {
			log.fatal(null, throwable);
		}
		
	}

}
