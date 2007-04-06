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
package org.apache.servicemix.web.view;

public class Dot {

    public static final String DEFAULT_DOT_PATH = "dot";

    private static boolean initialized = false;
    private static Exception initException;
    private static String dotPath = DEFAULT_DOT_PATH;

    public static String getDotPath() {
        return dotPath;
    }

    public static void setDotPath(String dotPath) {
        Dot.dotPath = dotPath;
    }


    public static void initialize() throws Exception {
        if (!initialized) {
            try {
                initException = null;
                run("-V");
            } catch (Exception e) {
                initException = e;
            } finally {
                initialized = true;
            }
        }
        if (initException != null) {
            throw initException;
        }
    }

    public static void run(String args) throws Exception {
        String cmdLine = getDotPath() + " " + args;
        Process p = Runtime.getRuntime().exec(cmdLine);
        p.waitFor();
    }

}
