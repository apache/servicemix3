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
package org.servicemix.jboss.deployment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployer;
import org.jboss.deployment.SubDeployerSupport;
import org.jboss.mx.loading.LoaderRepositoryFactory;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;

/**
 * This is the deployer that handles picking up and deploying a JBI package out
 * to the ServiceMix container.
 * 
 * @author <a href="mailto:philip.dodds@unity-systems.com">Philip Dodds</a>
 * 
 * @jmx.mbean name="jboss.system:service=ServiceMixDeployer"
 *            extends="org.jboss.deployment.SubDeployerMBean"
 */
public class JBIDeployer extends SubDeployerSupport implements SubDeployer,
		JBIDeployerMBean {

	private ObjectName deploymentService = null;

	private ObjectName jbiContainerMBean = null;

	/**
	 * Returns true if this deployer can deploy the given DeploymentInfo, for
	 * the time being we look for the extension iar.
	 * 
	 * @return True if this deployer can deploy the given DeploymentInfo.
	 * 
	 * @jmx.managed-operation
	 */
	public boolean accepts(DeploymentInfo di) {
		// To be accepted the deployment's root name must end in jar or zip
		String urlStr = di.url.getFile();
		if (!urlStr.endsWith(".jar") && !urlStr.endsWith(".zip")
				&& !urlStr.endsWith("-sm.xml")) {
			return false;
		}

		boolean accepts = false;
		if (urlStr.endsWith(".jar") || urlStr.endsWith(".zip")) {
			// However the jar/zip must also contain an META-INF/jbi.xml
			try {
				URL dd = di.localCl.findResource("META-INF/jbi.xml");
				if (dd != null) {
					log.debug("Found a META-INF/jbi.xml file, di: " + di);
					accepts = true;
				}
			} catch (Exception ignore) {
			}
		} else {
			// If is ends with -sm.xml then we just accept it
			accepts = true;
		}

		return accepts;
	}

	/**
	 * Describe <code>create</code> method here.
	 * 
	 * @param di
	 *            a <code>DeploymentInfo</code> value
	 * @exception DeploymentException
	 *                if an error occurs
	 * @jmx.managed-operation
	 */
	public void create(DeploymentInfo di) throws DeploymentException {
		try {
			String urlStr = di.url.getFile();

			if (jbiContainerMBean == null) {
				throw new DeploymentException(
						"The ServiceMix JBI container is not defined in Deployer");
			}
			log
					.info("ServiceMix deployer passing deployment to JBI container ["
							+ di.url.toExternalForm() + "]");
			if (urlStr.endsWith(".jar") || urlStr.endsWith(".zip")) {
				getServer().invoke(jbiContainerMBean, "installArchive",
						new Object[] { di.url.toExternalForm() },
						new String[] { "java.lang.String" });
			} else {
				getServer().invoke(jbiContainerMBean, "installServiceMixXml",
						new Object[] { di.url.toExternalForm() },
						new String[] { "java.lang.String" });
			}

		} catch (Exception e) {
			throw new DeploymentException(
					"ServiceMix deployer unable to deploy: " + di, e);
		}
	}

	public void destroy() {
		log.info("Destroying ServiceMix deployer");
		super.destroy();
	}

	/**
	 * The destroy method invokes destroy on the mbeans associated with the
	 * deployment in reverse order relative to create.
	 * 
	 * @param di
	 *            a <code>DeploymentInfo</code> value
	 * @jmx.managed-operation
	 */
	public void destroy(DeploymentInfo di) {
		try {
			String urlStr = di.url.getFile();

			if (jbiContainerMBean == null) {
				throw new DeploymentException(
						"The ServiceMix JBI container is not defined in Deployer");
			}
			log
					.info("ServiceMix deployer passing deployment to JBI container ["
							+ di.url.toExternalForm() + "]");
			if (urlStr.endsWith(".jar") || urlStr.endsWith(".zip")) {
				getServer().invoke(jbiContainerMBean, "uninstallArchive",
						new Object[] { di.url.toExternalForm() },
						new String[] { "java.lang.String" });
			} else {
				getServer().invoke(jbiContainerMBean, "uninstallServiceMixXml",
						new Object[] { di.url.toExternalForm() },
						new String[] { "java.lang.String" });
			}
		} catch (Exception e) {
			log.error("Unable to destroy deployment [" + di.getCanonicalName()
					+ "]");
		}
	}

	/**
	 * Gets the JBI deployment service object name
	 * 
	 * @return The JBI deployment service object name
	 */
	public ObjectName getDeploymentService() {
		return deploymentService;
	}

	/**
	 * Gets the ObjectName of the JBI Container's MBean
	 * 
	 * @return The ObjectName of the JBI Container MBean
	 * 
	 * @jmx.managed-attribute
	 */
	public ObjectName getJBIContainer() {
		return jbiContainerMBean;
	}

	/**
	 * The destroy method invokes destroy on the mbeans associated with the
	 * deployment in reverse order relative to create.
	 * 
	 * @param serviceName
	 *            a service object name
	 * @return The associated service DeploymentInfo if found, null otherwise
	 * @jmx.managed-operation
	 */
	public DeploymentInfo getService(ObjectName serviceName) {
		return null;
	}

	/**
	 * Describe <code>init</code> method here.
	 * 
	 * @param di
	 *            a <code>DeploymentInfo</code> value
	 * @exception DeploymentException
	 *                if an error occurs
	 * @jmx.managed-operation
	 */
	public void init(DeploymentInfo di) throws DeploymentException {
		try {
		  log.info("Init ServiceMix JBI Component: " + di.url);
      log.info("Init Watcher");
      initWatcher(di);
      log.info("Init LoaderRepository");
      initLoaderRepository(di);
		} catch (Exception e) {
		  log.error(e.getMessage());
			throw new DeploymentException("Error in accessing application metadata: ", e);
		}

		// invoke super-class initialization
		super.init(di);
	}

  private void initWatcher(DeploymentInfo di) throws MalformedURLException {
    // resolve the watch
    if (di.url.getProtocol().equals("file")) {
      File file = new File(di.url.getFile());
      // If not directory we watch the package
      if (!file.isDirectory()) { 
        di.watch = di.url; 
      }
      // If directory we watch the xml files
      else { 
        di.watch = new URL(di.url, "META-INF/jbi.xml"); 
      }
    }
    else { 
      // We watch the top only, no directory support 
      di.watch = di.url; 
    }
  }

	/**
	 * Sets the JBI deployment service object name
	 * 
	 * @param deploymentService
	 */
	public void setDeploymentService(ObjectName deploymentService) {
		this.deploymentService = deploymentService;
	}

	/**
	 * Sets the ObjectName of the JBI Container's MBean
	 * 
	 * @param jbiContainerMBean
	 *            The ObjectName of the JBI Container's MBean
	 * 
	 * @jmx.managed-attribute
	 */
	public void setJBIContainer(ObjectName jbiContainerMBean) {
		this.jbiContainerMBean = jbiContainerMBean;
	}

	/**
	 * The <code>start</code> method starts all the mbeans in this
	 * DeploymentInfo..
	 * 
	 * @param di
	 *            a <code>DeploymentInfo</code> value
	 * @exception DeploymentException
	 *                if an error occurs
	 * @jmx.managed-operation
	 */
	public void start(DeploymentInfo di) throws DeploymentException {

	}

	/**
	 * The stop method invokes stop on the mbeans associatedw ith the deployment
	 * in reverse order relative to create.
	 * 
	 * @param di
	 *            the <code>DeploymentInfo</code> value to stop.
	 * @jmx.managed-operation
	 */
	public void stop(DeploymentInfo di) {

	}
	
 /**
  * Add the jbi scoped repository
  *
  * @param di
  * the deployment info passed to deploy
  * @throws Exception
  */
  protected void initLoaderRepository(DeploymentInfo di) throws Exception { 
    LoaderRepositoryConfig lrConfig = new LoaderRepositoryFactory.LoaderRepositoryConfig(); 
    lrConfig.repositoryName = new ObjectName("org.servicemix:loader-repository=JBIContainer"); 
    di.setRepositoryInfo(lrConfig); 
  }

}
