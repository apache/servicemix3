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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.jar.JarOutputStream;

import org.apache.servicemix.jbi.util.FileUtil;

public class FileSystemJarInputStream extends InputStream implements Runnable {

	private File root;
	private PipedInputStream input;
	private PipedOutputStream output;
	private Thread runner;
	private IOException exception;
	
	public FileSystemJarInputStream(File root) throws IOException {
		this.root = root;
		input = new PipedInputStream();
		output = new PipedOutputStream(input);
	}

	public int read() throws IOException {
		if (runner == null) {
			runner = new Thread(this);
			runner.setDaemon(true);
			runner.start();
		}
		if (exception != null) {
			throw exception;
		}
		return input.read();
	}

	public void run() {
		try {
			JarOutputStream jos = new JarOutputStream(output);
			FileUtil.zipDir(root.getAbsolutePath(), jos, "");
			jos.close();
		} catch (IOException e) {
			exception = e;
            try {
                output.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
		}
	}

}
