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
package org.eclipse.jst.jbi.ui.internal.util;

import org.eclipse.osgi.util.NLS;

public class JBIUIMessages extends NLS {

	private static final String BUNDLE_NAME = "jbi_ui";//$NON-NLS-1$

	private JBIUIMessages() {
		// Do not instantiate
	}

	public static String KEY_0;

	public static String KEY_1;

	public static String KEY_2;

	public static String KEY_3;

	public static String KEY_4;

	public static String KEY_5;

	public static String KEY_6;

	static {
		NLS.initializeMessages(BUNDLE_NAME, JBIUIMessages.class);
	}

	public static final String IMAGE_LOAD_ERROR = KEY_0;

	public static final String JBI_PROJECT_MAIN_PG_TITLE = KEY_3;

	public static final String JBI_PROJECT_MAIN_PG_DESC = KEY_4;

	public static final String JBI_INSTALL_TITLE = KEY_5;

	public static final String JBI_INSTALL_DESCRIPTION = KEY_6;

}
