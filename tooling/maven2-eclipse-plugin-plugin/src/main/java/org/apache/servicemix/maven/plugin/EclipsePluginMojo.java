/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.maven.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Resource;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Basic Mojo that will leverage the project descriptor to generate an eclipse
 * manifest
 * 
 * @goal create-manifest
 * @description generates the manifest
 * @phase generate-resources
 * @requiresDependencyResolution runtime
 */
public class EclipsePluginMojo extends AbstractMojo {

	/**
	 * The package name
	 * 
	 * @parameter
	 * @required
	 */
	private String packageName;

	/**
	 * The plugin class
	 * 
	 * @parameter
	 * @required
	 */
	private String pluginClass;

	/**
	 * Directory where the plugin.xml file will be auto-generated.
	 * 
	 * @parameter expression="${project.build.directory}/eclipse"
	 */
	private File outputDir;

	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Constructing a MANIFEST.MF for Eclipse 3.0");

		try {

			File metaInfDir = new File(outputDir, "META-INF");
			metaInfDir.mkdirs();
			FileWriter fileWriter = new FileWriter(new File(metaInfDir, "MANIFEST.MF"));

			BufferedWriter writer = new BufferedWriter(fileWriter);

			writer.write("Manifest-Version: 1.0\nBundle-ManifestVersion: 2");
			writer.write("\nBundle-Name: ");
			writer.write(project.getName());
			writer.write("\nBundle-SymbolicName: ");
			writer.write(packageName);
			writer.write("; singleton:=true");
			writer.write("\nBundle-Version: ");
			writer.write(project.getVersion().replaceAll("-SNAPSHOT", ""));
			writer.write("\nBundle-ClassPath:");
			for (Iterator it = getEmbeddedDependencies().iterator(); it
					.hasNext();) {
				writer.write(" ");
				writer.write(it.next().toString());
				if (it.hasNext())
					writer.write(",\n");
			}

			writer.write("\nBundle-Activator: ");
			writer.write(pluginClass);
			writer.write("\nBundle-Vendor: ");
			writer.write(project.getOrganization().getName());
			writer.write("\nBundle-Localization: plugin");
			writer.write("\nExport-Package: .");
			writer.write("\nRequire-Bundle:");

			for (Iterator it = getEclipseDependencies().iterator(); it
					.hasNext();) {
				writer.write(" ");
				writer.write(it.next().toString());
				writer.write(";visibility:=reexport");
				if (it.hasNext())
					writer.write(",\n");
			}

			writer.write("\nEclipse-AutoStart: true\n");
			writer.close();
			fileWriter.close();

			FileWriter buildPropertiesFileWriter = new FileWriter(new File(outputDir, "build.properties"));
			BufferedWriter buildPropertiesWriter = new BufferedWriter(buildPropertiesFileWriter);
			buildPropertiesWriter.write("bin.includes = /META-INF");
			buildPropertiesWriter.close();
			buildPropertiesFileWriter.close();

      Resource res = new Resource();
      res.setDirectory(outputDir.toString());
      project.addResource(res);

		} catch (Exception e) {
			throw new MojoFailureException(
					"Unable to generate Eclipse 3.0 manifest, "
							+ e.getMessage());
		}

	}

	/**
	 * Helper method that returns a list of the artifacts that need to be
	 * embedded into the plugin
	 * 
	 * @return
	 */
	private List getEmbeddedDependencies() {
		List uris = new ArrayList();
		Set artifacts = project.getArtifacts();

    uris.add(".");
		for (Iterator iter = artifacts.iterator(); iter.hasNext();) {
			Artifact artifact = (Artifact) iter.next();
			ArtifactFilter filter = new ScopeArtifactFilter(
					Artifact.SCOPE_RUNTIME);
			try {
				if (!artifact.isOptional() && filter.include(artifact)) {
					String type = artifact.getType();
					if ("jar".equals(type)) {
						uris.add(artifact.getFile().getName());
						FileUtils.copyFileToDirectory(artifact.getFile(),
								outputDir);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException("Unable to handle dependency : "
						+ artifact.getArtifactId(), e);
			}
		}

		return uris;
	}

	/**
	 * Helper method that returns a list of the dependencies that need to be
	 * referenced
	 * 
	 * @return
	 */
	private List getEclipseDependencies() {
		List uris = new ArrayList();
		List artifacts = project.getDependencies();

		for (Iterator iter = artifacts.iterator(); iter.hasNext();) {
			Dependency dependency = (Dependency) iter.next();
			if (!dependency.isOptional()
					&& Artifact.SCOPE_PROVIDED.equals(dependency.getScope())) {
				uris.add(dependency.getArtifactId());
			}
		}
		return uris;
	}
}
