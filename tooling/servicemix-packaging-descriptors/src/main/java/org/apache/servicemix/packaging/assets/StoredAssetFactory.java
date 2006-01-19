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
package org.apache.servicemix.packaging.assets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.jbi.component.ComponentContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * StoredAssetFactory
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class StoredAssetFactory {

	public static StoredAssets getStoredAssets(ComponentContext componentContext) {
		File descriptor = new File(componentContext.getInstallRoot()
				+ "/META-INF/stored-assets.xml");
		return loadAssets(descriptor);
	}

	private static StoredAssets loadAssets(File descriptor) {
		JAXBContext context;
		StoredAssets assets;
		try {

			context = JAXBContext.newInstance(StoredAssets.class.getPackage()
					.getName());
			Unmarshaller m = context.createUnmarshaller();
			assets = (StoredAssets) m
					.unmarshal(new FileInputStream(descriptor));
			return assets;
		} catch (JAXBException e) {
			throw new RuntimeException("Unable to build stored assets", e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unable to find stored assets file", e);
		}
	}

	public static StoredAssets getStoredAssets(String serviceUnitDir) {
		File descriptor = new File(serviceUnitDir
				+ "/META-INF/stored-assets.xml");
		return loadAssets(descriptor);
	}
}
