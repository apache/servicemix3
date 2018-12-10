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
package org.apache.servicemix.bpe.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.servicemix.bpe.util.FileSystemJarInputStream;

import junit.framework.TestCase;

public class FileSystemJarInputStreamTest extends TestCase {

	private static final int BUFFER = 2048;

	public void testInputStream() throws Exception {
		File f = new File("target/test-data");
		f.mkdirs();
		Writer w = new OutputStreamWriter(new FileOutputStream(new File(f, "test.txt")));
		w.write("<hello>world</hello>");
		w.close();
		
		FileSystemJarInputStream fsjis = new FileSystemJarInputStream(f);
		JarInputStream jis = new JarInputStream(fsjis);

		JarEntry entry = jis.getNextJarEntry();
		assertNotNull(entry);
		assertEquals("test.txt", entry.getName());

		// Copy data from jar file into byte array
		BufferedOutputStream dest = null;
		ByteArrayOutputStream baos = null;
		int count; // buffer counter
		byte data[] = new byte[BUFFER];
		baos = new ByteArrayOutputStream();
		dest = new BufferedOutputStream(baos, BUFFER);
		while ((count = jis.read(data, 0, BUFFER)) != -1) {
			dest.write(data, 0, count);
		}
		dest.close();
		System.out.println(entry.getName() + ": " +  baos.toString());
		
		assertEquals("<hello>world</hello>", baos.toString());
	}

}
