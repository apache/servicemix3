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
package org.apache.servicemix.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.activeio.util.FactoryFinder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jdbc.adapter.DefaultJDBCAdapter;

public class JDBCAdapterFactory {

    private static final Log log = LogFactory.getLog(JDBCAdapterFactory.class);
    private static FactoryFinder factoryFinder = new FactoryFinder("META-INF/services/org/apache/servicemix/jdbc/");

    public static JDBCAdapter getAdapter(Connection connection) {
        JDBCAdapter adapter = null;
        try {

            // Make the filename file system safe.
            String driverName = connection.getMetaData().getDriverName();
            driverName = driverName.replaceAll("[^a-zA-Z0-9\\-]", "_").toLowerCase();

            try {
                adapter = (JDBCAdapter) factoryFinder.newInstance(driverName);
                log.info("Database driver recognized: [" + driverName + "]");
            } catch (Throwable e) {
                log.warn("Database driver NOT recognized: [" + driverName
                        + "].  Will use default JDBC implementation.");
            }

        } catch (SQLException e) {
            log.warn("JDBC error occurred while trying to detect database type.  Will use default JDBC implementation: "
                            + e.getMessage());
            log("Failure details: ", e);
        }

        // Use the default JDBC adapter if the
        // Database type is not recognized.
        if (adapter == null) {
            adapter = new DefaultJDBCAdapter();
        }
        
        return adapter;
    }
    
    public static void log(String msg, SQLException e) {
        if (log.isDebugEnabled()) {
            if (log.isDebugEnabled()) {
                String s = msg + e.getMessage();
                while (e.getNextException() != null) {
                    e = e.getNextException();
                    s += ", due to: " + e.getMessage();
                }
                log.debug(s, e);
            }
        }
    }
}
