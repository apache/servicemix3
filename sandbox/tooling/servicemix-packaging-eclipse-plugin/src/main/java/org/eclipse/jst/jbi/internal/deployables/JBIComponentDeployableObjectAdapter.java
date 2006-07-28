package org.eclipse.jst.jbi.internal.deployables;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.model.ModuleArtifactAdapterDelegate;

public class JBIComponentDeployableObjectAdapter extends
		ModuleArtifactAdapterDelegate implements IAdapterFactory {

	public JBIComponentDeployableObjectAdapter() {
		super();
	}

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IJBIComponentArtifact.class };
	}

	public IModuleArtifact getModuleArtifact(Object obj) {
		return JBIComponentDeployableObjectAdapterUtil.getModuleObject(obj);
	}
}
