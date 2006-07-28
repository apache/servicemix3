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
