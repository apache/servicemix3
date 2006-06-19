package org.eclipse.jst.jbi.internal.project.operations;

import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
import org.eclipse.jst.j2ee.project.facet.J2EEModuleFacetInstallDataModelProvider;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

public class JbiFacetInstallDataModelProvider extends
		J2EEModuleFacetInstallDataModelProvider {

	private static final String JBI_V1_0 = "1.0";

	private static final String JBI_PROJECT_FACET = "jst.jbi.component";

	public static final IProjectFacetVersion JBI_10 = ProjectFacetsManager
			.getProjectFacet(JBI_PROJECT_FACET).getVersion(JBI_V1_0); //$NON-NLS-1$

	public Set getPropertyNames() {
		Set names = super.getPropertyNames();
		names.add(CONFIG_FOLDER);
		names.add(IJbiFacetInstallDataModelProperties.JAVA_SOURCE_FOLDER);
		return names;
	}

	public Object getDefaultProperty(String propertyName) {
		if (propertyName.equals(FACET_ID)) {
			return JBI_PROJECT_FACET;
		} else if (propertyName.equals(CONFIG_FOLDER)) {
			return "src/main/resources";
		} else if (propertyName
				.equals(IJbiFacetInstallDataModelProperties.JAVA_SOURCE_FOLDER)) {
			return "src/main/java";
		}
		return super.getDefaultProperty(propertyName);
	}

	protected int convertFacetVersionToJ2EEVersion(IProjectFacetVersion version) {
		return J2EEVersionConstants.J2EE_1_4_ID;
	}

	public boolean isPropertyEnabled(String propertyName) {
		return super.isPropertyEnabled(propertyName);
	}

	public boolean propertySet(String propertyName, Object propertyValue) {
		boolean status = false;
		status = super.propertySet(propertyName, propertyValue);
		return status;
	}

	public IStatus validate(String propertyName) {
		return OK_STATUS;
	}

}
