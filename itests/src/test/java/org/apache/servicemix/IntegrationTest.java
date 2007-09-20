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
package org.apache.servicemix;

import org.apache.servicemix.api.NMR;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;

import java.net.URL;

public class IntegrationTest extends AbstractConfigurableBundleCreatorTests {

    /**
	 * The manifest to use for the "virtual bundle" created
	 * out of the test classes and resources in this project
	 * 
	 * This is actually the boilerplate manifest with one additional
	 * import-package added. We should provide a simpler customization
	 * point for such use cases that doesn't require duplication
	 * of the entire manifest...
	 */
	protected String getManifestLocation() { 
		return "classpath:org/apache/servicemix/MANIFEST.MF";
	}
	
	/**
	 * The location of the packaged OSGi bundles to be installed
	 * for this test. Values are Spring resource paths. The bundles
	 * we want to use are part of the same multi-project maven
	 * build as this project is. Hence we use the localMavenArtifact
	 * helper method to find the bundles produced by the package
	 * phase of the maven build (these tests will run after the
	 * packaging phase, in the integration-test phase). 
	 * 
	 * JUnit, commons-logging, spring-core and the spring OSGi
	 * test bundle are automatically included so do not need
	 * to be specified here.
	 */
	protected String[] getBundles() {
		return new String[] {
            localMavenArtifact("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jms", "1.1-4.0-SNAPSHOT"),
            localMavenArtifact("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.j2ee-management", "1.0-4.0-SNAPSHOT"),
            localMavenArtifact("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.stax-api", "1.0.1-4.0-SNAPSHOT"),
            localMavenArtifact("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.activation", "1.1-4.0-SNAPSHOT"),
            localMavenArtifact("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jaxb-api", "2.0-4.0-SNAPSHOT"),
            localMavenArtifact("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jaxb-impl", "2.0.3-4.0-SNAPSHOT"),
            localMavenArtifact("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.httpcore", "4.0-alpha5-4.0-SNAPSHOT"),
            localMavenArtifact("org.apache.activemq", "activemq-core", "5.0-SNAPSHOT"),
            localMavenArtifact("org.springframework", "spring-tx", "2.1-m4"),
            localMavenArtifact("org.springframework", "spring-jms", "2.1-m4"),
            localMavenArtifact("org.apache.camel", "camel-core", "1.1-SNAPSHOT"),
            localMavenArtifact("org.apache.camel", "camel-spring", "1.1-SNAPSHOT"),
            localMavenArtifact("org.apache.camel", "camel-osgi", "1.1-SNAPSHOT"),
            localMavenArtifact("org.apache.camel", "camel-jms", "1.1-SNAPSHOT"),
            localMavenArtifact("org.apache.camel", "camel-jhc", "1.1-SNAPSHOT"),
            localMavenArtifact("org.apache.servicemix", "org.apache.servicemix.api", "4.0-SNAPSHOT"),
            localMavenArtifact("org.apache.servicemix", "org.apache.servicemix.core", "4.0-SNAPSHOT"),
			localMavenArtifact("org.apache.servicemix", "org.apache.servicemix.spring", "4.0-SNAPSHOT"),
            localMavenArtifact("org.apache.servicemix", "org.apache.servicemix.nmr", "4.0-SNAPSHOT"),
            localMavenArtifact("org.apache.servicemix", "org.apache.servicemix.camel", "4.0-SNAPSHOT"),
            localMavenArtifact("org.apache.servicemix.examples", "org.apache.servicemix.examples.intermediary", "4.0-SNAPSHOT"),
		};
	}
	
	/**
	 * The superclass provides us access to the root bundle
	 * context via the 'getBundleContext' operation
	 */
	public void testOSGiStartedOk() {
		BundleContext bundleContext = getBundleContext();
		assertNotNull(bundleContext);
	}
	
	/**
	 * The simple service should have been exported as an
	 * OSGi service, which we can verify using the OSGi
	 * service APIs.
	 *
	 * In a Spring bundle, using osgi:reference is a much
	 * easier way to get a reference to a published service.
	 * 
	 */
	public void testSimpleServiceExported() {
		waitOnContextCreation("org.apache.servicemix.nmr");
		waitOnContextCreation("org.apache.servicemix.examples.intermediary");
		BundleContext context = getBundleContext();
        ServiceReference ref = context.getServiceReference(NMR.class.getName());
        assertNotNull("Service Reference is null", ref);
        try {
            NMR nmr = (NMR) context.getService(ref);
            assertNotNull("Cannot find the service", nmr);
        } finally {
            context.ungetService(ref);
        }
	}

}