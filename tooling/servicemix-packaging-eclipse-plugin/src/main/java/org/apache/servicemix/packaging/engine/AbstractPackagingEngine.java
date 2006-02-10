package org.apache.servicemix.packaging.engine;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.servicemix.descriptors.packaging.assets.Assets;
import org.apache.servicemix.descriptors.packaging.assets.Components.Component;
import org.apache.servicemix.packaging.ComponentArtifact;
import org.apache.servicemix.packaging.ComponentArtifactFactory;
import org.apache.servicemix.packaging.model.AbstractComponent;
import org.apache.servicemix.packaging.model.DeploymentDiagram;
import org.apache.servicemix.packaging.model.ModelElement;

public abstract class AbstractPackagingEngine implements PackagingEngine {

	private ComponentArtifact artifact;

	private List injectors = new ArrayList();

	public ComponentArtifact getArtifact() {
		return artifact;
	}

	public ComponentArtifact getArtifactForComponent(Component serviceToLookup) {
		for (ComponentArtifact artifact : ComponentArtifactFactory
				.getComponentArtifacts()) {
			for (Component component : artifact.getComponents().getComponent()) {
				if (component.getName().equals(serviceToLookup.getName())) {
					return artifact;
				}
			}
		}
		return null;
	}

	public String getDeploymentDir(ModelElement component) {
		String path = null;
		if (component instanceof DeploymentDiagram)
			path = ((DeploymentDiagram) component).getDeployPath();
		else if (component instanceof AbstractComponent) {
			path = getDeploymentDir(((AbstractComponent) component)
					.getParentModelElement());
		}
		if (path != null)
			new File(path).mkdirs();

		return path;
	}

	public String getInstallPath(ModelElement component) {
		String path = null;
		if (component instanceof DeploymentDiagram)
			path = ((DeploymentDiagram) component).getInstallPath();
		else if (component instanceof AbstractComponent) {
			path = getInstallPath(((AbstractComponent) component)
					.getParentModelElement());
		}

		if (path != null)
			new File(path).mkdirs();

		return path;
	}

	protected void injectComponentFiles(ZipOutputStream out,
			String componentName) throws IOException {
		for (String fileName : getArtifact().getResourceMap().keySet()) {
			if (!fileName.equals("META-INF/components.xml")) {
				if (fileName.equals(getArtifact().getComponentDefinitionByName(
						componentName).getAssets().getJbiDescriptor())) {
					out.putNextEntry(new ZipEntry("META-INF/jbi.xml"));
				} else
					out.putNextEntry(new ZipEntry(fileName));
				out.write(artifact.getResourceMap().get(fileName));
				out.closeEntry();
			}
		}
	}

	protected void injectAssets(Assets assets, ZipOutputStream out)
			throws Exception {
		JAXBContext context = JAXBContext.newInstance(Assets.class.getPackage()
				.getName());
		Marshaller m = context.createMarshaller();
		final StringWriter write = new StringWriter();
		m.marshal(assets, write);
		out.putNextEntry(new ZipEntry("META-INF/bundled-assets.xml"));
		out.write(write.toString().getBytes());
		out.closeEntry();
	}

	public void setArtifact(ComponentArtifact artifact) {
		this.artifact = artifact;
	}

	public void setInjectors(List<PackagingInjector> injectors) {
		this.injectors = injectors;
	}

	public List<PackagingInjector> getInjectors() {
		return injectors;
	}
}
