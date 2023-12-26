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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.maven.shared.utils.StringUtils;
import org.apache.maven.shared.utils.WriterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 *
 */
class XmlWriterUtilTest {
    private OutputStream output;

    private Writer writer;

    private XMLWriter xmlWriter;

    /** {@inheritDoc} */
    @BeforeEach
    void setUp() throws Exception {

        output = new ByteArrayOutputStream();
        writer = WriterFactory.newXmlWriter(output);
        xmlWriter = new PrettyPrintXMLWriter(writer);
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeLineBreak(XMLWriter)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeLineBreakXMLWriter() throws Exception {
        XmlWriterUtil.writeLineBreak(xmlWriter);
        writer.close();
        assertEquals(1, StringUtils.countMatches(output.toString(), "\r\n"));
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeLineBreak(XMLWriter, int)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeLineBreakXMLWriterInt() throws Exception {
        XmlWriterUtil.writeLineBreak(xmlWriter, 10);
        writer.close();
        assertEquals(10, StringUtils.countMatches(output.toString(), "\r\n"));
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeLineBreak(XMLWriter, int, int)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeLineBreakXMLWriterIntInt() throws Exception {
        XmlWriterUtil.writeLineBreak(xmlWriter, 10, 2);
        writer.close();
        assertEquals(10, StringUtils.countMatches(output.toString(), "\r\n"));
        assertEquals(
                1,
                StringUtils.countMatches(
                        output.toString(), StringUtils.repeat(" ", 2 * XmlWriterUtil.DEFAULT_INDENTATION_SIZE)));
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeLineBreak(XMLWriter, int, int, int)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeLineBreakXMLWriterIntIntInt() throws Exception {
        XmlWriterUtil.writeLineBreak(xmlWriter, 10, 2, 4);
        writer.close();
        assertEquals(10, StringUtils.countMatches(output.toString(), "\r\n"));
        assertEquals(1, StringUtils.countMatches(output.toString(), StringUtils.repeat(" ", 2 * 4)));
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeCommentLineBreak(XMLWriter)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeCommentLineBreakXMLWriter() throws Exception {
        XmlWriterUtil.writeCommentLineBreak(xmlWriter);
        writer.close();
        String sb = "<!-- ====================================================================== -->" + "\r\n";
        assertEquals(output.toString(), sb);
        assertEquals(output.toString().length(), XmlWriterUtil.DEFAULT_COLUMN_LINE - 1 + "\r\n".length());
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeCommentLineBreak(XMLWriter, int)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeCommentLineBreakXMLWriterInt() throws Exception {
        XmlWriterUtil.writeCommentLineBreak(xmlWriter, 20);
        writer.close();
        assertEquals(output.toString(), "<!-- ========== -->" + "\r\n");
    }

    @Test
    void writeCommentLineBreak() throws IOException {
        XmlWriterUtil.writeCommentLineBreak(xmlWriter, 10);
        writer.close();
        assertEquals(output.toString(), "<!--  -->" + "\r\n", output.toString());
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeComment(XMLWriter, java.lang.String)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeCommentXMLWriterString() throws Exception {
        XmlWriterUtil.writeComment(xmlWriter, "hello");
        writer.close();
        String sb = "<!-- hello                                                                  -->" + "\r\n";
        assertEquals(output.toString(), sb);
        assertEquals(output.toString().length(), XmlWriterUtil.DEFAULT_COLUMN_LINE - 1 + "\r\n".length());
    }

    @Test
    void writeComment() throws IOException {
        XmlWriterUtil.writeComment(
                xmlWriter, "hellooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
        writer.close();
        String sb = "<!-- hellooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo -->" + "\r\n";
        assertEquals(output.toString(), sb);
        assertTrue(output.toString().length() >= XmlWriterUtil.DEFAULT_COLUMN_LINE);
    }

    @Test
    void writeComment_2() throws IOException {
        XmlWriterUtil.writeComment(xmlWriter, "hello\nworld");
        writer.close();
        String sb = "<!-- hello                                                                  -->" + "\r\n"
                + "<!-- world                                                                  -->"
                + "\r\n";
        assertEquals(output.toString(), sb);
        assertEquals(output.toString().length(), 2 * (XmlWriterUtil.DEFAULT_COLUMN_LINE - 1 + "\r\n".length()));
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeComment(XMLWriter, java.lang.String, int)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeCommentXMLWriterStringInt() throws Exception {
        String indent = StringUtils.repeat(" ", 2 * XmlWriterUtil.DEFAULT_INDENTATION_SIZE);

        XmlWriterUtil.writeComment(xmlWriter, "hello", 2);
        writer.close();
        String sb = indent + "<!-- hello                                                                  -->" + "\r\n";
        assertEquals(output.toString(), sb);
        assertEquals(
                output.toString().length(),
                XmlWriterUtil.DEFAULT_COLUMN_LINE - 1 + "\r\n".length() + 2 * XmlWriterUtil.DEFAULT_INDENTATION_SIZE);
    }

    @Test
    void writeComment_3() throws IOException {
        String indent = StringUtils.repeat(" ", 2 * XmlWriterUtil.DEFAULT_INDENTATION_SIZE);
        XmlWriterUtil.writeComment(xmlWriter, "hello\nworld", 2);
        writer.close();
        String sb = indent + "<!-- hello                                                                  -->"
                + "\r\n"
                + indent
                + "<!-- world                                                                  -->"
                + "\r\n";
        assertEquals(output.toString(), sb);
        assertEquals(
                output.toString().length(),
                2 * (XmlWriterUtil.DEFAULT_COLUMN_LINE - 1 + "\r\n".length()) + 2 * indent.length());
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeComment(XMLWriter, java.lang.String, int, int)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeCommentXMLWriterStringIntInt() throws Exception {
        String repeat = StringUtils.repeat(" ", 2 * 4);

        XmlWriterUtil.writeComment(xmlWriter, "hello", 2, 4);
        writer.close();
        String sb = repeat + "<!-- hello                                                                  -->" + "\r\n";
        assertEquals(output.toString(), sb);
        assertEquals(output.toString().length(), XmlWriterUtil.DEFAULT_COLUMN_LINE - 1 + "\r\n".length() + 2 * 4);
    }

    @Test
    void writeCommentXMLWriterStringIntInt_2() throws IOException {
        String repeat = StringUtils.repeat(" ", 2 * 4);
        XmlWriterUtil.writeComment(xmlWriter, "hello\nworld", 2, 4);
        writer.close();
        String sb = repeat + "<!-- hello                                                                  -->"
                + "\r\n"
                + repeat
                + "<!-- world                                                                  -->"
                + "\r\n";
        assertEquals(output.toString(), sb);
        assertEquals(
                output.toString().length(),
                2 * (XmlWriterUtil.DEFAULT_COLUMN_LINE - 1 + "\r\n".length()) + 2 * repeat.length());
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeComment(XMLWriter, java.lang.String, int, int, int)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeCommentXMLWriterStringIntIntInt() throws Exception {
        String indent = StringUtils.repeat(" ", 2 * 4);

        XmlWriterUtil.writeComment(xmlWriter, "hello", 2, 4, 50);
        writer.close();
        String sb = indent + "<!-- hello                                    -->" + "\r\n";
        assertEquals(output.toString(), sb);
        assertEquals(output.toString().length(), 50 - 1 + "\r\n".length() + 2 * 4);
    }

    @Test
    void writeCommentXMLWriterStringIntIntInt_2() throws IOException {
        String indent = StringUtils.repeat(" ", 2 * 4);
        XmlWriterUtil.writeComment(xmlWriter, "hello", 2, 4, 10);
        writer.close();
        String sb = indent + "<!-- hello -->" + "\r\n";
        assertEquals(output.toString(), sb);
        assertTrue(output.toString().length() >= 10 + 2 * 4);
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeCommentText(XMLWriter, java.lang.String, int)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeCommentTextXMLWriterStringInt() throws Exception {
        XmlWriterUtil.writeCommentText(xmlWriter, "hello", 0);
        writer.close();
        String sb = "\r\n" + "<!-- ====================================================================== -->"
                + "\r\n"
                + "<!-- hello                                                                  -->"
                + "\r\n"
                + "<!-- ====================================================================== -->"
                + "\r\n"
                + "\r\n";
        assertEquals(output.toString(), sb);
        assertEquals(output.toString().length(), 3 * (80 - 1 + "\r\n".length()) + 2 * "\r\n".length());
    }

    @Test
    void writeCommentTextXMLWriterStringInt_2() throws IOException {
        String indent = StringUtils.repeat(" ", 2 * 2);

        XmlWriterUtil.writeCommentText(
                xmlWriter,
                "hello world with end of line\n and "
                        + "loooooooooooooooooooooooooooooooooooooooooooooooooooooonnnnnnnnnnong line",
                2);
        writer.close();
        String sb = "\r\n" + indent
                + "<!-- ====================================================================== -->"
                + "\r\n"
                + indent
                + "<!-- hello world with end of line                                           -->"
                + "\r\n"
                + indent
                + "<!-- and                                                                    -->"
                + "\r\n"
                + indent
                + "<!-- loooooooooooooooooooooooooooooooooooooooooooooooooooooonnnnnnnnnnong   -->"
                + "\r\n"
                + indent
                + "<!-- line                                                                   -->"
                + "\r\n"
                + indent
                + "<!-- ====================================================================== -->"
                + "\r\n"
                + "\r\n"
                + indent;
        assertEquals(output.toString(), sb);
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeCommentText(XMLWriter, java.lang.String, int, int)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeCommentTextXMLWriterStringIntInt() throws Exception {
        String indent = StringUtils.repeat(" ", 2 * 4);

        XmlWriterUtil.writeCommentText(xmlWriter, "hello", 2, 4);
        writer.close();
        String sb = "\r\n" + indent
                + "<!-- ====================================================================== -->"
                + "\r\n"
                + indent
                + "<!-- hello                                                                  -->"
                + "\r\n"
                + indent
                + "<!-- ====================================================================== -->"
                + "\r\n"
                + "\r\n"
                + indent;
        assertEquals(output.toString(), sb);
        assertEquals(output.toString().length(), 3 * (80 - 1 + "\r\n".length()) + 4 * 2 * 4 + 2 * "\r\n".length());
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeCommentText(XMLWriter, java.lang.String, int, int, int)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeCommentTextXMLWriterStringIntIntInt() throws Exception {
        String indent = StringUtils.repeat(" ", 2 * 4);

        XmlWriterUtil.writeCommentText(xmlWriter, "hello", 2, 4, 50);
        writer.close();
        String sb = "\r\n" + indent
                + "<!-- ======================================== -->"
                + "\r\n"
                + indent
                + "<!-- hello                                    -->"
                + "\r\n"
                + indent
                + "<!-- ======================================== -->"
                + "\r\n"
                + "\r\n"
                + indent;
        assertEquals(output.toString(), sb);
        assertEquals(output.toString().length(), 3 * (50 - 1 + "\r\n".length()) + 4 * 2 * 4 + 2 * "\r\n".length());
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeComment(XMLWriter, java.lang.String)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeCommentNull() throws Exception {
        XmlWriterUtil.writeComment(xmlWriter, null);
        writer.close();
        String sb = "<!-- null                                                                   -->" + "\r\n";
        assertEquals(output.toString(), sb);
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeComment(XMLWriter, java.lang.String)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeCommentShort() throws Exception {
        XmlWriterUtil.writeComment(xmlWriter, "This is a short text");
        writer.close();
        String sb = "<!-- This is a short text                                                   -->" + "\r\n";
        assertEquals(output.toString(), sb);
    }

    /**
     * Test method for {@link org.apache.maven.shared.utils.xml.XmlWriterUtil#writeComment(XMLWriter, java.lang.String)}.
     *
     * @throws Exception if any
     */
    @Test
    void writeCommentLong() throws Exception {
        XmlWriterUtil.writeComment(
                xmlWriter,
                "Maven is a software project management and comprehension tool. "
                        + "Based on the concept of a project object model (POM), Maven can manage a project's build, reporting "
                        + "and documentation from a central piece of information.");
        writer.close();
        String sb = "<!-- Maven is a software project management and comprehension tool. Based   -->" + "\r\n"
                + "<!-- on the concept of a project object model (POM), Maven can manage a     -->"
                + "\r\n"
                + "<!-- project's build, reporting and documentation from a central piece of   -->"
                + "\r\n"
                + "<!-- information.                                                           -->"
                + "\r\n";
        assertEquals(output.toString(), sb);
    }
}
