package org.eclipse.jst.jbi.ui.project.facet;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jst.jbi.internal.project.operations.JbiFacetProjectCreationDataModelProvider;
import org.eclipse.jst.jbi.ui.internal.util.JBIUIMessages;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectTemplate;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.web.ui.internal.wizards.NewProjectDataModelFacetWizard;
import org.osgi.framework.Bundle;

public class JbiProjectWizard extends NewProjectDataModelFacetWizard {

	public JbiProjectWizard(IDataModel model) {
		super(model);
		setWindowTitle(JBIUIMessages.KEY_1);
	}

	public JbiProjectWizard() {
		super();
		setWindowTitle(JBIUIMessages.KEY_1);
	}

	protected IDataModel createDataModel() {
		return DataModelFactory
				.createDataModel(new JbiFacetProjectCreationDataModelProvider());
	}

	protected IFacetedProjectTemplate getTemplate() {
		return ProjectFacetsManager.getTemplate("template.jst.jbi.component"); //$NON-NLS-1$
	}

	protected IWizardPage createFirstPage() {
		return new JbiProjectFirstPage(model, "first.page"); //$NON-NLS-1$
	}

	protected ImageDescriptor getDefaultPageImageDescriptor() {
		final Bundle bundle = Platform.getBundle("org.eclipse.jst.jbi.ui"); //$NON-NLS-1$
		final URL url = bundle.getEntry("icons/servicemix.gif"); //$NON-NLS-1$
		return ImageDescriptor.createFromURL(url);
	}

}
