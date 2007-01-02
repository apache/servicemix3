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
