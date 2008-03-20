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
package org.apache.servicemix.jbi.jmx;

/*
 * Copyright (C) The MX4J Contributors. All rights reserved.
 * 
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this
 * software.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

import mx4j.util.Base64Codec;

/**
 * Implementation of the JMXAuthenticator interface to be used on server side to
 * secure access to
 * {@link javax.management.remote.JMXConnectorServer JMXConnectorServer}s.
 * <br/> Usage:
 * 
 * <pre>
 * JMXAuthenticator authenticator = new PasswordAuthenticator(new File(&quot;users.properties&quot;));
 * Map environment = new HashMap();
 * environment.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
 * JMXServiceURL address = new JMXServiceURL(&quot;rmi&quot;, &quot;localhost&quot;, 0);
 * MBeanServer server = ...;
 * JMXConnectorServer cntorServer = JMXConnectorServerFactory.newJMXConnectorServer(address, environment, server);
 * </pre>
 * 
 * The format of the users.properties file is that of a standard properties
 * file: <br/> &lt;user&gt;=&lt;password&gt;<br/> where &lt;password&gt; can be
 * stored in 2 ways:
 * <ul>
 * <li>Clear text: the password is written in clear text</li>
 * <li>Obfuscated text: the password is obfuscated</li>
 * </ul>
 * The obfuscated form can be obtained running this class as a main class:
 * 
 * <pre>
 * java -cp mx4j-remote.jar mx4j.tools.remote.PasswordAuthenticator
 * </pre>
 * 
 * and following the instructions printed on the console. The output will be a
 * string that should be copy/pasted as the password into the properties file.<br/>
 * The obfuscated password is obtained by digesting the clear text password
 * using a {@link java.security.MessageDigest} algorithm, and then by
 * Base64-encoding the resulting bytes.<br/> <br/> On client side, you are
 * allowed to connect to a server side secured with the PasswordAuthenticator
 * only if you provide the correct credentials:
 * 
 * <pre>
 * String[] credentials = new String[2];
 * // The user will travel as clear text
 * credentials[0] = &quot;user&quot;;
 * // You may send the password in clear text, but it's better to obfuscate it
 * credentials[1] = PasswordAuthenticator.obfuscatePassword(&quot;password&quot;);
 * Map environment = new HashMap();
 * environment.put(JMXConnector.CREDENTIALS, credentials);
 * JMXServiceURL address = ...;
 * JMXConnector cntor = JMXConnectorFactory.connect(address, environment);
 * </pre>
 * 
 * Note that
 * {@link #obfuscatePassword(java.lang.String,java.lang.String) obfuscating} the
 * passwords only works if the server side has been setup with the
 * PasswordAuthenticator. However, the PasswordAuthenticator can be used with
 * other JSR 160 implementations, such as Sun's reference implementation.
 * 
 * @version $Revision: 1.3 $
 */
public class PasswordAuthenticator implements JMXAuthenticator {

    private static final String LEFT_DELIMITER = "OBF(";
    private static final String RIGHT_DELIMITER = "):";

    private Map passwords;

    /**
     * Creates a new PasswordAuthenticator that reads user/password pairs from
     * the specified properties file. The file format is described in the
     * javadoc of this class.
     * 
     * @see #obfuscatePassword
     */
    public PasswordAuthenticator(File passwordFile) throws IOException {
        this(new FileInputStream(passwordFile));
    }

    /**
     * Creates a new PasswordAuthenticator that reads user/password pairs from
     * the specified InputStream. The file format is described in the javadoc of
     * this class.
     * 
     * @see #obfuscatePassword
     */
    public PasswordAuthenticator(InputStream is) throws IOException {
        passwords = readPasswords(is);
    }

    /**
     * Runs this class as main class to obfuscate passwords. When no arguments
     * are provided, it prints out the usage.
     * 
     * @see #obfuscatePassword(java.lang.String,java.lang.String)
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && !"-help".equals(args[0])) {
            printPassword("MD5", args[0]);
            return;
        } else if (args.length == 3 && "-alg".equals(args[0])) {
            printPassword(args[1], args[2]);
            return;
        }
        printUsage();
    }

    private static void printPassword(String algorithm, String input) {
        String password = obfuscatePassword(input, algorithm);
        System.out.println(password);
    }

    private static void printUsage() {
        System.out.println();
        System.out.println("Usage: java -cp <lib>/mx4j-tools.jar mx4j.tools.remote.PasswordAuthenticator <options> <password>");
        System.out.println("Where <options> is one of the following:");
        System.out.println("   -help                     Prints this message");
        System.out.println("   -alg <digest algorithm>   Specifies the digest algorithm (default is MD5)");
        System.out.println();
    }

    /**
     * Obfuscates the given password using MD5 as digest algorithm
     * 
     * @see #obfuscatePassword(java.lang.String,java.lang.String)
     */
    public static String obfuscatePassword(String password) {
        return obfuscatePassword(password, "MD5");
    }

    /**
     * Obfuscates the given password using the given digest algorithm.<br/>
     * Obfuscation consists of 2 steps: first the clear text password is
     * {@link java.security.MessageDigest#digest digested} using the specified
     * algorithm, then the resulting bytes are Base64-encoded.<br/> For
     * example, the obfuscated version of the password "password" is
     * "OBF(MD5):X03MO1qnZdYdgyfeuILPmQ==" or
     * "OBF(SHA-1):W6ph5Mm5Pz8GgiULbPgzG37mj9g=". <br/> OBF stands for
     * "obfuscated", in parenthesis the algorithm used to digest the password.
     */
    public static String obfuscatePassword(String password, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] digestedBytes = digest.digest(password.getBytes());
            byte[] obfuscatedBytes = Base64Codec.encodeBase64(digestedBytes);
            return LEFT_DELIMITER + algorithm + RIGHT_DELIMITER + new String(obfuscatedBytes);
        } catch (NoSuchAlgorithmException x) {
            throw new SecurityException("Could not find digest algorithm " + algorithm);
        }
    }

    private Map readPasswords(InputStream is) throws IOException {
        Properties properties = new Properties();
        try {
            properties.load(is);
        } finally {
            is.close();
        }
        return new HashMap(properties);
    }

    public Subject authenticate(Object credentials) throws SecurityException {
        if (!(credentials instanceof String[])) {
            throw new SecurityException("Bad credentials");
        }
        String[] creds = (String[]) credentials;
        if (creds.length != 2) {
            throw new SecurityException("Bad credentials");
        }

        String user = creds[0];
        String password = creds[1];

        if (password == null) {
            throw new SecurityException("Bad password");
        }

        if (!passwords.containsKey(user)) {
            throw new SecurityException("Unknown user " + user);
        }

        String storedPassword = (String) passwords.get(user);
        if (!isPasswordCorrect(password, storedPassword)) {
            throw new SecurityException("Bad password");
        }

        Set principals = new HashSet();
        principals.add(new JMXPrincipal(user));
        return new Subject(true, principals, Collections.EMPTY_SET, Collections.EMPTY_SET);
    }

    private boolean isPasswordCorrect(String password, String storedPassword) {
        if (password.startsWith(LEFT_DELIMITER)) {
            if (storedPassword.startsWith(LEFT_DELIMITER)) {
                return password.equals(storedPassword);
            } else {
                String algorithm = getAlgorithm(password);
                String obfuscated = obfuscatePassword(storedPassword, algorithm);
                return password.equals(obfuscated);
            }
        } else {
            if (storedPassword.startsWith(LEFT_DELIMITER)) {
                // Password was sent in clear, bad practice
                String algorithm = getAlgorithm(storedPassword);
                String obfuscated = obfuscatePassword(password, algorithm);
                return obfuscated.equals(storedPassword);
            } else {
                return password.equals(storedPassword);
            }
        }
    }

    private String getAlgorithm(String obfuscatedPassword) {
        try {
            return obfuscatedPassword.substring(LEFT_DELIMITER.length(), obfuscatedPassword.indexOf(RIGHT_DELIMITER));
        } catch (IndexOutOfBoundsException x) {
            throw new SecurityException("Bad password");
        }
    }
}
