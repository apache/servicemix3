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
