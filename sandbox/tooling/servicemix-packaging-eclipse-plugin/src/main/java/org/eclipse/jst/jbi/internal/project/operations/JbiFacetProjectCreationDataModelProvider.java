package org.eclipse.jst.jbi.internal.project.operations;

import org.eclipse.jst.common.project.facet.IJavaFacetInstallDataModelProperties;
import org.eclipse.jst.common.project.facet.JavaFacetInstallDataModelProvider;
import org.eclipse.wst.common.componentcore.datamodel.FacetProjectCreationDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

public class JbiFacetProjectCreationDataModelProvider extends
		FacetProjectCreationDataModelProvider {

	public void init() {
		super.init();
		FacetDataModelMap map = (FacetDataModelMap) getProperty(FACET_DM_MAP);
		IDataModel javaFacet = DataModelFactory
				.createDataModel(new JavaFacetInstallDataModelProvider());
		map.add(javaFacet);
		IDataModel jbiFacet = DataModelFactory
				.createDataModel(new JbiFacetInstallDataModelProvider());
		map.add(jbiFacet);
		javaFacet
				.setProperty(
						IJavaFacetInstallDataModelProperties.SOURCE_FOLDER_NAME,
						jbiFacet
								.getStringProperty(IJbiFacetInstallDataModelProperties.JAVA_SOURCE_FOLDER));
	}
}
