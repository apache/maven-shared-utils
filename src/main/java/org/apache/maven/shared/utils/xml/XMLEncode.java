/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.shared.utils.xml;

import java.io.IOException;
import java.io.Writer;

/**
 * Collection of XML encoding/decoding helpers. <br>
 * This is all about the special characters &amp; and &lt;, and for attributes
 * &quot; and &apos;. These must be encoded/decoded from/to XML.
 */
@Deprecated
final class XMLEncode {

    private static final int CDATA_BLOCK_THRESHOLD_LENGTH = 12;

    private static final char DEFAULT_QUOTE_CHAR = '"';

    static void xmlEncodeText(String text, Writer writer) throws IOException {
        if (text == null) {
            return;
        }

        if (!needsEncoding(text)) {
            writer.write(text);
            return;
        } else {
            // only encode as cdata if is is longer than CDATA block overhead:
            if (text.length() > CDATA_BLOCK_THRESHOLD_LENGTH) {
                String cdata = xmlEncodeTextAsCDATABlock(text);
                if (cdata != null) {
                    writer.write(cdata);
                    return;
                }
            }
        }

        // if every thing else fails, do it the save way...
        xmlEncodeTextAsPCDATA(text, false, DEFAULT_QUOTE_CHAR, writer);
    }

    static void xmlEncodeTextAsPCDATA(String text, boolean forAttribute, char quoteChar, Writer n) throws IOException {
        if (text == null) {
            return;
        }

        int length = text.length();
        if (forAttribute) {
            n.append(quoteChar);
        }

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    n.append("&amp;");
                    break;
                case '<':
                    n.append("&lt;");
                    break;
                case '>': // FIX for sourceforge bug #802520 ("]]>" needs encoding)
                    n.append("&gt;");
                    break;
                case '"':
                    if (forAttribute) {
                        n.append("&quot;");
                    } else {
                        n.append(c);
                    }
                    break;
                case '\'':
                    if (forAttribute) {
                        n.append("&apos;");
                    } else {
                        n.append(c);
                    }
                    break;
                case '\r':
                    if (forAttribute) {
                        if (i == (length - 1) || text.charAt(i + 1) != '\n') {
                            n.append("&#13;");
                        }
                    } else {
                        n.append(c);
                    }
                    // but skip the \r in \r\n

                    break;
                case '\n':
                    if (forAttribute) {
                        n.append("&#10;");
                    }
                    break;

                default:
                    n.append(c);
                    break;
            }
        }

        if (forAttribute) {
            n.append(quoteChar);
        }
    }

    /**
     * Returns string as CDATA block if possible, otherwise null.
     */
    private static String xmlEncodeTextAsCDATABlock(String text) {
        if (text == null) {
            return null;
        }
        if (!text.contains("]]>")) {
            return "<![CDATA[" + text + "]]>";
        } else {
            return null;
        }
    }

    /**
     * Checks if this text needs encoding in order to be represented in XML.
     */
    private static boolean needsEncoding(String text) {
        if (text == null) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&' || c == '<') {
                return true;
            }
        }
        return false;
    }
}
