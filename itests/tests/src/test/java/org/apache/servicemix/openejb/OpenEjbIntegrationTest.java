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
package org.apache.servicemix.openejb;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openejb.config.ReadDescriptors;
import org.apache.servicemix.AbstractIntegrationTest;

public class OpenEjbIntegrationTest extends AbstractIntegrationTest {

    private static final Log LOGGER = LogFactory.getLog(OpenEjbIntegrationTest.class);

    private Properties dependencies;

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
		return "classpath:org/apache/servicemix/openejb/MANIFEST.MF";
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
        return new String[] {
            getBundle("org.springframework", "spring-tx"),
            getBundle("org.apache.xbean", "xbean-finder"),
            getBundle("org.apache.xbean", "xbean-naming"),
            getBundle("org.apache.xbean", "xbean-reflect"),
            getBundle("org.apache.ws.commons.schema", "XmlSchema"),
            getBundle("org.apache.geronimo.specs", "geronimo-activation_1.1_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-annotation_1.0_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-ejb_3.0_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-interceptor_3.0_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-j2ee-connector_1.5_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-j2ee-management_1.1_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-jacc_1.1_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-javamail_1.4_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-jaxr_1.0_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-jms_1.1_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-jpa_3.0_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-saaj_1.1_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-stax-api_1.0_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-ws-metadata_2.0_spec"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.cxf"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jaxb-api"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jaxb-impl"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jaxws-api"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.openejb"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.openjpa"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.wsdl4j"),
            getBundle("org.apache.servicemix.jbi", "org.apache.servicemix.jbi.api"),
            getBundle("org.apache.servicemix.nmr", "org.apache.servicemix.nmr.api"),
            getBundle("org.apache.servicemix.nmr", "org.apache.servicemix.nmr.core"),
            getBundle("org.apache.servicemix.nmr", "org.apache.servicemix.nmr.spring"),
            getBundle("org.apache.servicemix.nmr", "org.apache.servicemix.nmr.osgi"),
            getBundle("org.apache.servicemix", "org.apache.servicemix.transaction"),
            getBundle("org.ops4j.pax.web", "pax-web-service"),
        };
    }

    /**
	 * The superclass provides us access to the root bundle
	 * context via the 'getBundleContext' operation
	 */
	public void testOSGiStartedOk() throws Exception {
        try {
            Thread.sleep(2000);
            System.out.println("Installing openejb");
            installBundle("org.apache.servicemix.openejb", "org.apache.servicemix.openejb", null, "jar");
            Thread.sleep(2000);
            System.out.println("Checking that EJB bundle is started");
            checkBundleStarted("org.apache.servicemix.openejb");
            Thread.sleep(2000);
            System.out.println("Installing ejbjar bundle");
            installBundle("org.apache.servicemix.itests", "org.apache.servicemix.itests.ejbjar", null, "jar");
            System.out.println("ejbjar bundle installed");
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage()) ;
        }
    }

}