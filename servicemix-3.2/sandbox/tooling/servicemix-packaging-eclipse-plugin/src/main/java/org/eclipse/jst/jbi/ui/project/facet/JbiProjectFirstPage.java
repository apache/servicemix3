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
package org.eclipse.jst.jbi.ui.project.facet;

import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPluginIcons;
import org.eclipse.jst.j2ee.internal.wizard.J2EEComponentFacetCreationWizardPage;
import org.eclipse.jst.jbi.ui.internal.util.JBIUIMessages;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

public class JbiProjectFirstPage extends J2EEComponentFacetCreationWizardPage {

	protected String getModuleFacetID() {
		return "jst.jbi.component";
	}

	public JbiProjectFirstPage(IDataModel model, String pageName) {
		super(model, pageName);
		setTitle(JBIUIMessages.JBI_PROJECT_MAIN_PG_TITLE);
		setDescription(JBIUIMessages.JBI_PROJECT_MAIN_PG_DESC);
		setImageDescriptor(J2EEUIPlugin.getDefault().getImageDescriptor(
				J2EEUIPluginIcons.EJB_PROJECT_WIZARD_BANNER));
	}

}
