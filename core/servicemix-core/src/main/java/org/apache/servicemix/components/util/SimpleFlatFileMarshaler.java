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
package org.apache.servicemix.components.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import java.util.NoSuchElementException;
import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.servicemix.jbi.jaxp.StringSource;

/**
 * A simple flat file marshaler that can read fixed-length and csv text files
 * and converts them to XML
 * 
 * @author Juergen Mayrbaeurl
 * @since 3.2
 */
public class SimpleFlatFileMarshaler extends DefaultFileMarshaler {

    public static final String XMLDECLARATION_LINE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    public static final ContentConverter NO_CONVERSION = new NoConversion();

    public static final ContentConverter TEXT_STRIPPER = new TextStripConverter();

    public static final int LINEFORMAT_FIXLENGTH = 0;
    public static final int LINEFORMAT_CSV = 1;
    public static final int LINEFORMAT_VARIABLE = 2;
    public static final int LINEFORMAT_DEFAULT = LINEFORMAT_FIXLENGTH;

    private static final String XML_OPEN = "<";
    private static final String XML_OPEN_END = "</";
    private static final String XML_CLOSE = ">";
    private static final String XML_CLOSE_NEWLINE = ">\n";
    private static final String XML_CLOSE_ATTR_NEWLINE = "\">\n";
    private static final String XML_CLOSE_ATTR = "\">";

    private boolean xmlDeclaration = true;

    /**
     * Encoding for the input file. Can be null for default encoding
     */
    private String encoding;

    private String docElementNamespace;

    /**
     * XML element name of the root element
     */
    private String docElementname = "File";

    /**
     * XML element name for contents of a line
     */
    private String lineElementname = "Line";

    /**
     * XML element name for contents of a column
     */
    private String columnElementname = "Column";

    private int lineFormat = LINEFORMAT_DEFAULT;

    /**
     * Column separator for csv flat files
     */
    private String columnSeparator = ";";

    /**
     * Line separator for csv flat files, be carefull when changing the default.
     * Null means the default Java line separator (See BufferedReader.readLine)
     */
    private String lineSeparator;
    private ColumnExtractor columnExtractor;

    private int[] columnLengths;

    private List columnConverters;

    private String[] columnNames;

    private boolean insertLineNumbers = true;

    private boolean insertColNumbers;

    private boolean skipKnownEmptyCols = true;

    private boolean alwaysStripColContents = true;

    private boolean insertRawData;

    private boolean insertColContentInAttribut;

    private int headerlinesCount;

    private static class CustomEndOfLineIterator
            implements Iterator {

        private InputStreamReader inr;
        private String lineSeparator;
        private String next;
        private boolean eof;

        public CustomEndOfLineIterator(InputStream in, String encoding,
                String lineSeparator) {

            if (encoding == null) {
                inr = new InputStreamReader(in);
            } else {
                try {
                    inr = new InputStreamReader(in, encoding);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            this.lineSeparator = lineSeparator;
            this.eof = false;
            readNext();
        }

        public void close() {
            eof = true;
            IOUtils.closeQuietly(inr);
            next = null;
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove unsupported on CustomEndOfLineIterator");
        }

        public boolean hasNext() {
            if (next == null && !eof) {
                readNext();
            }
            return !eof;
        }

        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            String res = next;
            readNext();
            return res;
        }

        private void readNext() {
            if (eof) {
                next = null;
                return;
            }
            StringBuilder sb = new StringBuilder();
            while (true) {
                int b;
                try {
                    b = inr.read();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (b == -1) {
                    eof = true;
                    break;
                }

                sb.append((char) b);

                if ((char) b == lineSeparator.charAt(0)) {
                    break; //FIXME: handle multi-character line separators
                }
            }
            next = sb.toString();
        }
    }

    public void readMessage(MessageExchange exchange,
                            NormalizedMessage message, 
                            InputStream in, 
                            String path) throws IOException, JBIException {
        message.setContent(new StringSource(this.convertLinesToString(message, in, path)));
        message.setProperty(FILE_NAME_PROPERTY, new File(path).getName());
        message.setProperty(FILE_PATH_PROPERTY, path);
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected InputStream convertLinesToStream(NormalizedMessage message,
                                               InputStream in, String path) throws IOException {
        return IOUtils.toInputStream(this.convertLinesToString(message, in,
                path), "UTF-8");
    }

    protected InputStream convertLines(NormalizedMessage message,
                                       InputStream in, String path) throws IOException {
        return this.convertLinesToStream(message, in, path);
    }

    protected String convertLinesToString(NormalizedMessage message,
                                          InputStream in, String path) throws IOException {
        Iterator lines;
        if (lineSeparator == null) {
            lines = IOUtils.lineIterator(in, this.encoding);
        } else {
            lines = new CustomEndOfLineIterator(in, this.encoding, lineSeparator);
        }

        StringBuffer aBuffer = new StringBuffer(1024);

        if (this.xmlDeclaration) {
            aBuffer.append(XMLDECLARATION_LINE);
        }

        aBuffer.append(XML_OPEN + this.docElementname);

        if (this.docElementNamespace != null) {
            aBuffer.append("xmlns=\"");
            aBuffer.append(this.docElementNamespace);
            aBuffer.append("\"");
        }

        aBuffer.append(" name=\"");
        aBuffer.append(new File(path).getName());
        aBuffer.append("\"");

        aBuffer.append(" location=\"");
        aBuffer.append(path);
        aBuffer.append(XML_CLOSE_ATTR_NEWLINE);

        this.processHeaderLines(aBuffer, lines);

        int lineNumber = 1;
        while (lines.hasNext()) {
            String lineText = (String) lines.next();
            aBuffer.append(XML_OPEN + this.lineElementname);

            if (this.insertLineNumbers || this.insertRawData) {
                if (this.insertLineNumbers) {
                    aBuffer.append(" number=\"");
                    aBuffer.append(lineNumber);
                    if (!this.insertRawData) {
                        aBuffer.append(XML_CLOSE_ATTR);
                    } else {
                        aBuffer.append("\"");
                    }
                }
                if (this.insertRawData) {
                    aBuffer.append(" raw=\"");
                    aBuffer.append(lineText);
                    aBuffer.append(XML_CLOSE_ATTR);
                }
            } else {
                aBuffer.append(XML_CLOSE);
            }

            if ((this.columnLengths != null)
                    || (this.lineFormat != LINEFORMAT_FIXLENGTH)) {
                this.extractColumns(aBuffer, lineText, lines);
            } else {
                aBuffer.append(lineText);
            }
            aBuffer.append(XML_OPEN_END + this.lineElementname
                    + XML_CLOSE_NEWLINE);

            lineNumber++;
        }

        aBuffer.append(XML_OPEN_END + this.docElementname + XML_CLOSE_NEWLINE);

        return aBuffer.toString();
    }

    protected void processHeaderLines(StringBuffer buffer, Iterator lines) {
        if ((this.headerlinesCount > 0) && (lines.hasNext())) {
            int counter = 0;
            do {
                String headerline = (String) lines.next();
                this.convertHeaderline(buffer, headerline);

                counter++;
            } while ((counter < this.headerlinesCount) && (lines.hasNext()));
        }
    }

    protected void convertHeaderline(StringBuffer buffer, String headerline) {
    }

    protected void extractColumns(StringBuffer buffer, String lineText,
            Iterator lines) {
        String[] rawcolContents = this.extractColumnContents(lineText, lines);

        if ((rawcolContents != null) && (rawcolContents.length > 0)) {
            for (int i = 0; i < rawcolContents.length; i++) {
                String colText = rawcolContents[i];

                String colName = this.findColumnname(i);
                String colContents = this.convertColumnContents(i, colText);

                if (colContents == null) {
                    // Simple skip
                    // Or maybe insert NULL Element
                } else {
                    if (!((colContents.length() == 0)
                            && (this.skipKnownEmptyCols) && (!this.columnElementname
                            .equals(colName)))) {
                        if (this.insertColContentInAttribut) {
                            buffer.append(XML_OPEN + colName);

                            if (this.insertColNumbers) {
                                buffer.append(" number=\"");
                                buffer.append(i + 1);
                                buffer.append("\"");
                            }
                            buffer.append(" value=\"");
                            buffer.append(colContents);
                            buffer.append("\"/>");
                        } else {
                            if (!this.insertColNumbers) {
                                buffer.append(XML_OPEN + colName + XML_CLOSE);
                            } else {
                                buffer.append(XML_OPEN + colName);
                                buffer.append(" number=\"");
                                buffer.append(i + 1);
                                buffer.append("\"/>");
                            }
                            buffer.append(colContents);
                            buffer.append(XML_OPEN_END + colName + XML_CLOSE);
                        }
                    }
                }
            }
        }
    }

    protected String[] extractColumnContents(String lineText, Iterator lines) {
        String[] result = null;

        if ((lineText != null) && (lineText.length() > 0)) {
            if (this.lineFormat == LINEFORMAT_FIXLENGTH) {
                if ((this.columnLengths != null)
                        && (this.columnLengths.length > 0)) {
                    result = new String[this.columnLengths.length];
                    int curIndex = 0;

                    for (int i = 0; i < this.columnLengths.length; i++) {
                        try {
                            result[i] = lineText.substring(curIndex, curIndex
                                    + this.columnLengths[i]);

                            curIndex += this.columnLengths[i];

                        } catch (StringIndexOutOfBoundsException e) {
                            break;
                        }
                    }
                }
            } else if (this.lineFormat == LINEFORMAT_CSV) {
                result = StringUtils.splitPreserveAllTokens(lineText,
                        this.columnSeparator);
            } else if (this.lineFormat == LINEFORMAT_VARIABLE) {
                if (this.columnExtractor == null) {
                    throw new IllegalStateException("No Column Extractor defined");
                }
                result = this.columnExtractor.extractColumns(lineText);
            } else {
                throw new IllegalStateException("Unknown line format '" + this.lineFormat + "'");
            }
        }

        return result;
    }

    protected String findColumnname(int index) {
        String result = this.columnElementname;

        if ((this.columnNames != null) && (this.columnNames.length > index)
                && (this.columnNames[index] != null)) {
            return this.columnNames[index];
        }

        return result;
    }

    protected String convertColumnContents(int index, String contents) {
        if ((this.columnConverters != null)
                && (this.columnConverters.size() > index)
                && (this.columnConverters.get(index) != null)) {
            ContentConverter converter = (ContentConverter) this.columnConverters.get(index);
            return converter.convertToXml(contents);
        } else {
            if (this.alwaysStripColContents) {
                return TEXT_STRIPPER.convertToXml(contents);
            } else {
                return NO_CONVERSION.convertToXml(contents);
            }
        }
    }

    // Properties
    // -------------------------------------------------------------------------
    public final String getEncoding() {
        return encoding;
    }

    public final void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public final String getColumnElementname() {
        return columnElementname;
    }

    public final void setColumnElementname(String columnElementname) {
        this.columnElementname = columnElementname;
    }

    public final String getDocElementname() {
        return docElementname;
    }

    public final void setDocElementname(String docElementname) {
        this.docElementname = docElementname;
    }

    public final String getLineElementname() {
        return lineElementname;
    }

    public final void setLineElementname(String lineElementname) {
        this.lineElementname = lineElementname;
    }

    public final void setColumnLengths(String[] columnLengths) {
        this.columnLengths = new int[columnLengths.length];
        for (int i = 0; i < columnLengths.length; i++) {
            this.columnLengths[i] = Integer.parseInt(columnLengths[i]);
        }
    }

    public final boolean isXmlDeclaration() {
        return xmlDeclaration;
    }

    public final void setXmlDeclaration(boolean xmlDeclaration) {
        this.xmlDeclaration = xmlDeclaration;
    }

    public final void setInsertLineNumbers(boolean insertLineNumbers) {
        this.insertLineNumbers = insertLineNumbers;
    }

    public final void setColumnConverters(List columnConverters) {
        this.columnConverters = columnConverters;
    }

    public final void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public final boolean isSkipKnownEmptyCols() {
        return skipKnownEmptyCols;
    }

    public final void setSkipKnownEmptyCols(boolean skipKnownEmptyCols) {
        this.skipKnownEmptyCols = skipKnownEmptyCols;
    }

    public final void setInsertRawData(boolean insertRawData) {
        this.insertRawData = insertRawData;
    }

    public final boolean isAlwaysStripColContents() {
        return alwaysStripColContents;
    }

    public final void setAlwaysStripColContents(boolean alwaysStripColContents) {
        this.alwaysStripColContents = alwaysStripColContents;
    }

    public final int getLineFormat() {
        return lineFormat;
    }

    public final void setLineFormat(int lineFormat) {
        this.lineFormat = lineFormat;
    }

    public final String getColumnSeparator() {
        return columnSeparator;
    }

    public final void setColumnSeparator(String columnSeparator) {
        this.columnSeparator = columnSeparator;
    }

    public final void setColumnSeparatorCode(int columnSeparatorCode) {
        this.columnSeparator = new String(new char[]{(char) columnSeparatorCode});
    }

    public final String getLineSeparator() {
        return lineSeparator;
    }

    public final void setLineSeparator(String lineSeparator) {
        if (lineSeparator != null && lineSeparator.length() > 1) {
            throw new IllegalArgumentException("Currently only 1 character separators are supported, or null.");
        }
        this.lineSeparator = lineSeparator;
    }

    public final void setLineSeparatorCode(int lineSeparatorCode) {
        this.lineSeparator = new String(new char[]{(char) lineSeparatorCode});
    }

    public final String getDocElementNamespace() {
        return docElementNamespace;
    }

    public final void setDocElementNamespace(String docElementNamespace) {
        this.docElementNamespace = docElementNamespace;
    }

    public final int getHeaderlinesCount() {
        return headerlinesCount;
    }

    public final void setHeaderlinesCount(int headerlinesCount) {
        this.headerlinesCount = headerlinesCount;
    }

    public final boolean isInsertColContentInAttribut() {
        return insertColContentInAttribut;
    }

    public final void setInsertColContentInAttribut(
            boolean insertColContentInAttribut) {
        this.insertColContentInAttribut = insertColContentInAttribut;
    }

    public final boolean isInsertColNumbers() {
        return insertColNumbers;
    }

    public final void setInsertColNumbers(boolean insertColNumbers) {
        this.insertColNumbers = insertColNumbers;
    }

    public final void setColumnExtractor(ColumnExtractor columnExtractor) {
        this.columnExtractor = columnExtractor;
    }

}
