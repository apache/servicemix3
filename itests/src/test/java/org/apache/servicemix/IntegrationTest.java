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

import org.apache.servicemix.nmr.api.NMR;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;

public class IntegrationTest extends AbstractConfigurableBundleCreatorTests {

    private static final String TEST_FRAMEWORK_BUNDLES_CONF_FILE = "/org/apache/servicemix/boot-bundles.properties";

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
	protected String[] getTestBundlesNames() {
        String servicemixVersion = "4.0-SNAPSHOT";
        String camelVersion = "1.3-SNAPSHOT";
        return new String[] {
            "org.apache.geronimo.specs,geronimo-jms_1.1_spec,1.1.1-SNAPSHOT",
            "org.apache.geronimo.specs,geronimo-j2ee-management_1.1_spec,1.0.1-SNAPSHOT",
            "org.apache.geronimo.specs,geronimo-stax-api_1.0_spec,1.0.1-SNAPSHOT",
            "org.apache.geronimo.specs,geronimo-activation_1.1_spec,1.0.1-SNAPSHOT",
            "org.apache.servicemix.bundles,org.apache.servicemix.bundles.jaxb-api,2.0-" + servicemixVersion,
            "org.apache.servicemix.bundles,org.apache.servicemix.bundles.jaxb-impl,2.0.3-" + servicemixVersion,
            "org.apache.servicemix.bundles,org.apache.servicemix.bundles.httpcore,4.0-alpha5-" + servicemixVersion,
            "org.apache.activemq,activemq-core,5.0-SNAPSHOT",
            "org.springframework,spring-tx," + getSpringBundledVersion(),
            "org.springframework,spring-jms," + getSpringBundledVersion(),
            "org.apache.camel,camel-core," + camelVersion,
            "org.apache.camel,camel-spring," + camelVersion,
            "org.apache.camel,camel-osgi," + camelVersion,
            "org.apache.camel,camel-jms," + camelVersion,
            "org.apache.camel,camel-jhc," + camelVersion,
            "org.apache.servicemix.nmr,org.apache.servicemix.nmr.api," + servicemixVersion,
            "org.apache.servicemix.nmr,org.apache.servicemix.nmr.core," + servicemixVersion,
			"org.apache.servicemix.nmr,org.apache.servicemix.nmr.spring," + servicemixVersion,
            "org.apache.servicemix.nmr,org.apache.servicemix.nmr.osgi," + servicemixVersion,
            "org.apache.servicemix,org.apache.servicemix.camel," + servicemixVersion,
            "org.apache.servicemix.examples,org.apache.servicemix.examples.intermediary," + servicemixVersion,
		};
	}

    protected Resource getTestingFrameworkBundlesConfiguration() {
        return new InputStreamResource(getClass().getResourceAsStream(TEST_FRAMEWORK_BUNDLES_CONF_FILE));
    }
    
    protected String getSpringBundledVersion() {
        return "2.5-rc1";
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