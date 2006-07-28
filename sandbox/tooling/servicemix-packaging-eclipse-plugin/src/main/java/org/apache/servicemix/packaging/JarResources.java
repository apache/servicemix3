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
package org.apache.servicemix.packaging;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Holds the resources for a given JAR, bit of a memory intensive idea
 * 
 * TODO Look and importing so we are not holding the contenting in memory
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class JarResources {

	private Map<String, Integer> htSizes = new HashMap<String, Integer>();

	private Map<String, byte[]> htJarContents = new HashMap<String, byte[]>();

	private String jarFileName;

	public JarResources(String jarFileName) throws IOException {
		this.jarFileName = jarFileName;
		init();
	}

	public Map getResourceMap() {
		return htJarContents;
	}

	public byte[] getResource(String resourceName) {
		return (byte[]) htJarContents.get(resourceName);
	}

	public void init() throws IOException {

		ZipFile zf = new ZipFile(jarFileName);
		Enumeration e = zf.entries();
		while (e.hasMoreElements()) {
			ZipEntry ze = (ZipEntry) e.nextElement();
			htSizes.put(ze.getName(), new Integer((int) ze.getSize()));
		}
		zf.close();
		FileInputStream fis = new FileInputStream(jarFileName);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ZipInputStream zis = new ZipInputStream(bis);
		ZipEntry ze = null;
		while ((ze = zis.getNextEntry()) != null) {
			if (ze.isDirectory()) {
				continue;
			}
			int size = (int) ze.getSize();
			// -1 means unknown size.
			if (size == -1) {
				size = ((Integer) htSizes.get(ze.getName())).intValue();
			}
			byte[] b = new byte[(int) size];
			int rb = 0;
			int chunk = 0;
			while (((int) size - rb) > 0) {
				chunk = zis.read(b, rb, (int) size - rb);
				if (chunk == -1) {
					break;
				}
				rb += chunk;
			}
			// add to internal resource hashtable
			htJarContents.put(ze.getName(), b);
		}

	}
}
