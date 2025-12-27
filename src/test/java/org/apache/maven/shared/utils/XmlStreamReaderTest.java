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
package org.apache.maven.shared.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.shared.utils.xml.XmlStreamReader;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author <a href="mailto:hboutemy@apache.org">Hervé Boutemy</a>
 */
@SuppressWarnings("deprecation")
public class XmlStreamReaderTest {
    /**
     * french
     */
    private static final String TEXT_LATIN1 = "eacute: é";
    /**
     * greek
     */
    private static final String TEXT_LATIN7 = "alpha: α";
    /**
     * euro support
     */
    private static final String TEXT_LATIN15 = "euro: €";
    /**
     * japanese
     */
    private static final String TEXT_EUC_JP = "hiragana A: あ";
    /**
     * Unicode: support everything
     */
    private static final String TEXT_UNICODE =
            TEXT_LATIN1 + ", " + TEXT_LATIN7 + ", " + TEXT_LATIN15 + ", " + TEXT_EUC_JP;
    /**
     * see http://unicode.org/faq/utf_bom.html#BOM
     */
    private static final byte[] BOM_UTF8 = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private static final byte[] BOM_UTF16BE = {(byte) 0xFE, (byte) 0xFF};
    private static final byte[] BOM_UTF16LE = {(byte) 0xFF, (byte) 0xFE};

    private static String createXmlContent(String text, String encoding) {
        String xmlDecl = "<?xml version=\"1.0\"?>";
        if (encoding != null) {
            xmlDecl = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
        }
        return xmlDecl + "\n<text>" + text + "</text>";
    }

    private static void checkXmlContent(String xml, String encoding) throws IOException {
        checkXmlContent(xml, encoding, null);
    }

    private static void checkXmlContent(String xml, String encoding, byte[] bom) throws IOException {
        byte[] xmlContent = xml.getBytes(encoding);
        InputStream in = new ByteArrayInputStream(xmlContent);

        if (bom != null) {
            in = new SequenceInputStream(new ByteArrayInputStream(bom), in);
        }

        XmlStreamReader reader = new XmlStreamReader(in);
        assertEquals(encoding, reader.getEncoding());
        String result = IOUtils.toString(reader);
        assertEquals(xml, result);
    }

    private static void checkXmlStreamReader(String text, String encoding) throws IOException {
        checkXmlStreamReader(text, encoding, encoding, null);
    }

    private static void checkXmlStreamReader(String text, String encoding, String effectiveEncoding, byte[] bom)
            throws IOException {
        String xml = createXmlContent(text, encoding);
        checkXmlContent(xml, effectiveEncoding, bom);
    }

    @Test
    public void testNoXmlHeader() throws IOException {
        String xml = "<text>text with no XML header</text>";
        checkXmlContent(xml, "UTF-8");
        checkXmlContent(xml, "UTF-8", BOM_UTF8);
    }

    @Test
    public void testDefaultEncoding() throws IOException {
        checkXmlStreamReader(TEXT_UNICODE, (String) null, "UTF-8", null);
        checkXmlStreamReader(TEXT_UNICODE, null, "UTF-8", BOM_UTF8);
    }

    @Test
    public void testUTF8Encoding() throws IOException {
        checkXmlStreamReader(TEXT_UNICODE, "UTF-8");
        checkXmlStreamReader(TEXT_UNICODE, "UTF-8", "UTF-8", BOM_UTF8);
    }

    @Test
    public void testUTF16Encoding() throws IOException {
        checkXmlStreamReader(TEXT_UNICODE, "UTF-16", "UTF-16BE", null);
        checkXmlStreamReader(TEXT_UNICODE, "UTF-16", "UTF-16LE", BOM_UTF16LE);
        checkXmlStreamReader(TEXT_UNICODE, "UTF-16", "UTF-16BE", BOM_UTF16BE);
    }

    @Test
    public void testUTF16BEEncoding() throws IOException {
        checkXmlStreamReader(TEXT_UNICODE, "UTF-16BE");
    }

    @Test
    public void testUTF16LEEncoding() throws IOException {
        checkXmlStreamReader(TEXT_UNICODE, "UTF-16LE");
    }

    @Test
    public void testLatin1Encoding() throws IOException {
        checkXmlStreamReader(TEXT_LATIN1, "ISO-8859-1");
    }

    @Test
    public void testLatin7Encoding() throws IOException {
        checkXmlStreamReader(TEXT_LATIN7, "ISO-8859-7");
    }

    @Test
    public void testLatin15Encoding() throws IOException {
        checkXmlStreamReader(TEXT_LATIN15, "ISO-8859-15");
    }

    @Test
    public void testEUCJPEncoding() throws IOException {
        checkXmlStreamReader(TEXT_EUC_JP, "EUC-JP");
    }

    @Test
    public void testEBCDICEncoding() throws IOException {
        checkXmlStreamReader("simple text in EBCDIC", "CP1047");
    }

    @Test
    public void testInappropriateEncoding() throws IOException {
        try {
            checkXmlStreamReader(TEXT_UNICODE, "ISO-8859-2");
            fail("Check should have failed, since some characters are not available in the specified encoding");
        } catch (AssertionFailedError cf) {
            // expected failure, since the encoding does not contain some characters
        }
    }

    @Test
    public void testEncodingAttribute() throws IOException {
        checkXmlContent("<?xml version='1.0' encoding='US-ASCII'?><element encoding='attribute value'/>", "US-ASCII");
        checkXmlContent(
                "<?xml version='1.0' encoding  =  'US-ASCII'  ?><element encoding='attribute value'/>", "US-ASCII");
        checkXmlContent("<?xml version='1.0'?><element encoding='attribute value'/>", "UTF-8");
        checkXmlContent(
                "<?xml\nversion='1.0'\nencoding\n=\n'US-ASCII'\n?>\n<element encoding='attribute value'/>", "US-ASCII");
        checkXmlContent("<?xml\nversion='1.0'\n?>\n<element encoding='attribute value'/>", "UTF-8");
        checkXmlContent("<element encoding='attribute value'/>", "UTF-8");
    }
}
