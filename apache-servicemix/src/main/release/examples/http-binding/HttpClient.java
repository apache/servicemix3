/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * @version $Revision$
 */
public class HttpClient {

    public static void main(String[] args) throws Exception {

        URLConnection connection = new URL("http://localhost:8912").openConnection();
        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();

        // Post the request file.
        FileInputStream fis = new FileInputStream("request.xml");
        
        //Buffer
        byte[] buf = new byte[256];
        for (int c = fis.read(buf); c != -1; c = fis.read(buf)) {
        	os.write(buf,0,c);
        }
        os.close();
        fis.close();

        // Read the response.
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
        }
        in.close();

    }
}
