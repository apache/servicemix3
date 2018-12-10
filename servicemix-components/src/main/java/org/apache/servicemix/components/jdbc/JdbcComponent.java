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
package org.apache.servicemix.components.jdbc;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.TransformComponentSupport;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Node;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.sql.DataSource;
import javax.xml.transform.Source;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;

public class JdbcComponent extends TransformComponentSupport implements MessageExchangeListener {
    private static final Log log = LogFactory.getLog(JdbcComponent.class);

    private DataSource dataSource;
    private boolean responseRequired = false;

    public boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        log.debug("Received a JDBC request. Datasource=" + dataSource + ", ResponseRequired=" + responseRequired);
        Connection conn   = null;
        Statement stmt    = null;
        ResultSet rs      = null;

        try {

            SourceTransformer domTransform = new SourceTransformer();
            Node domNode = domTransform.toDOMNode(in);

            // Return the exception message
//            if (isExceptionXml(domNode)) {
//                log.debug("Found an exception message: " + domNode);
//                out.setContent(in.getContent());
//                return true;
//            }

            String query = getQuery(domNode);
            log.debug("Retrieved query: " + query);

            conn = dataSource.getConnection();

            stmt  = conn.createStatement();

            Source outMsg = null;
            if (query != null && query.length() > 0) {
                if (stmt.execute(query)) {
                    // Result is a ResultSet object
                    rs = stmt.getResultSet();

                    log.debug("Formatting ResultSet: " + rs);
                    outMsg = toXmlSource(rs);
                } else {
                    int updateCount = stmt.getUpdateCount();
                    if (updateCount > -1) {
                        log.debug("Formatting UpdateCount: " + updateCount);
                        // Result is an update count
                        outMsg = toXmlSource(updateCount);
                    } else {
                        log.debug("Formatting NoResult.");
                        // Result is neither a result set nor an update count
                        outMsg = null;
                    }
                }
            }

            if (outMsg != null) {
                // There is a valid response
                log.debug("Response: " + domTransform.toString(outMsg));
                out.setContent(outMsg);
                return true;

            } else if (responseRequired) {
                // Create an empty <sqlResult> element
                log.debug("Response: Empty Response");
                out.setContent(toXmlSource());
                return true;

            } else {
                log.debug("Response: No Response");
                // There is no valid response
                return false;
            }
        } catch (Exception e) {
            log.error("JDBC Component Exception: ", e);
//            out.setContent(createExceptionXml(e));
//            return true;
            throw new MessagingException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }

    public String getQuery(Node node) throws Exception {
        CachedXPathAPI xpath = new CachedXPathAPI();

        node = xpath.selectSingleNode(node, "sql/child::text()");

        // First child should be <sql></sql> element
        if (node == null) {
            throw new IllegalStateException("Expecting <sql></sql> node. Found: " + node);
        }

        return node.getNodeValue();
    }

    public void setDataSource(DataSource ds) {
        dataSource = ds;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * If true, an empty <sqlResult> element is created and send as a response if there is no result
     * @param val
     */
    public void setResponseRequired(boolean val) {
        responseRequired = val;
    }

    public boolean getResponseRequired() {
        return responseRequired;
    }

    protected Source toXmlSource(ResultSet rs) throws Exception {

        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        StringBuffer buff = new StringBuffer("");

        while (rs.next()) {
            buff.append("<row ");
            for (int i=1; i<=colCount; i++) {
                String colName = meta.getColumnName(i);
                if (colName.equals("")) {
                    colName = "__unknown_column__";
                }
                buff.append(colName.toLowerCase() + "='" + rs.getString(i) + "' ");
            }
            buff.append("/>");
        }

        if (buff.length() > 0) {
            // If non-empty result, insert parent tags
            buff.insert(0, "<sqlResult><resultSet>");
            buff.append("</resultSet></sqlResult>");
        } else {
            // If empty result, return null source
            return null;
        }

        return new StringSource(buff.toString());
    }

    protected Source toXmlSource(int updateCount) throws Exception {
        return new StringSource("<sqlResult><updateCount value='" + updateCount + "'/></sqlResult>");
    }

    protected Source toXmlSource() throws Exception {
        return new StringSource("<sqlResult></sqlResult>");
    }


    // Custom method to create and handle exception
    /*
    public boolean isExceptionXml(Node document) {
        return document.getFirstChild().getNodeName().equalsIgnoreCase("exception");
    }

    public Source createExceptionXml(Throwable e) {
        return new StringSource("<exception class='" + e.getClass() + "'>'" + e.getMessage() + "'</exception>");
    } */
}
