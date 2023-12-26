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

import javax.swing.text.html.HTML;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.maven.shared.utils.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test of {@link PrettyPrintXMLWriter}
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
class PrettyPrintXmlWriterTest {
    private StringWriter w = new StringWriter();
    private PrettyPrintXMLWriter writer = new PrettyPrintXMLWriter(w);

    @Test
    void noStartTag() throws IOException {

        try {
            writer.startElement("");
            fail("allowed empty name");
        } catch (IllegalArgumentException ex) {
            assertEquals("Element name cannot be empty", ex.getMessage());
        }
    }

    @Test
    void defaultPrettyPrintXMLWriter() throws IOException {
        writer.startElement(HTML.Tag.HTML.toString());

        writeXhtmlHead(writer);

        writeXhtmlBody(writer);

        writer.endElement(); // Tag.HTML

        assertEquals(expectedResult(), w.toString());
    }

    @Test
    void prettyPrintXMLWriterWithGivenLineSeparator() throws IOException {
        writer.setLineSeparator("\n");

        writer.startElement(HTML.Tag.HTML.toString());

        writeXhtmlHead(writer);

        writeXhtmlBody(writer);

        writer.endElement(); // Tag.HTML

        assertEquals(expectedResult(), w.toString());
    }

    @Test
    void prettyPrintXMLWriterWithGivenLineIndenter() throws IOException {
        writer.setLineIndenter("    ");

        writer.startElement(HTML.Tag.HTML.toString());

        writeXhtmlHead(writer);

        writeXhtmlBody(writer);

        writer.endElement(); // Tag.HTML

        assertEquals(expectedResult("    "), w.toString());
    }

    @Test
    void escapeXmlAttributeWindows() throws IOException {
        // Windows
        writer.startElement(HTML.Tag.DIV.toString());
        writer.addAttribute("class", "sect\r\nion");
        writer.endElement(); // Tag.DIV
        assertEquals("<div class=\"sect&#10;ion\"/>", w.toString());
    }

    @Test
    void escapeXmlAttributeMac() throws IOException {
        // Mac
        writer.startElement(HTML.Tag.DIV.toString());
        writer.addAttribute("class", "sect\rion");
        writer.endElement(); // Tag.DIV
        assertEquals("<div class=\"sect&#13;ion\"/>", w.toString());
    }

    @Test
    void escapeXmlAttributeTrailingCR() throws IOException {
        // Mac
        writer.startElement(HTML.Tag.DIV.toString());
        writer.addAttribute("class", "section\r");
        writer.endElement(); // Tag.DIV
        assertEquals("<div class=\"section&#13;\"/>", w.toString());
    }

    @Test
    void escapeXmlAttributeUnix() throws IOException {
        // Unix
        writer.startElement(HTML.Tag.DIV.toString());
        writer.addAttribute("class", "sect\nion");
        writer.endElement(); // Tag.DIV
        assertEquals("<div class=\"sect&#10;ion\"/>", w.toString());
    }

    private void writeXhtmlHead(XMLWriter writer) throws IOException {
        writer.startElement(HTML.Tag.HEAD.toString());
        writer.startElement(HTML.Tag.TITLE.toString());
        writer.writeText("title");
        writer.endElement(); // Tag.TITLE
        writer.startElement(HTML.Tag.META.toString());
        writer.addAttribute("name", "author");
        writer.addAttribute("content", "Author");
        writer.endElement(); // Tag.META
        writer.startElement(HTML.Tag.META.toString());
        writer.addAttribute("name", "date");
        writer.addAttribute("content", "Date");
        writer.endElement(); // Tag.META
        writer.endElement(); // Tag.HEAD
    }

    private void writeXhtmlBody(XMLWriter writer) throws IOException {
        writer.startElement(HTML.Tag.BODY.toString());
        writer.startElement(HTML.Tag.P.toString());
        writer.writeText("Paragraph 1, line 1. Paragraph 1, line 2.");
        writer.endElement(); // Tag.P
        writer.startElement(HTML.Tag.DIV.toString());
        writer.addAttribute("class", "section");
        writer.startElement(HTML.Tag.H2.toString());
        writer.writeText("Section title");
        writer.endElement(); // Tag.H2
        writer.endElement(); // Tag.DIV
        writer.endElement(); // Tag.BODY
    }

    private static String expectedResult() {
        return expectedResult("  ");
    }

    private static String expectedResult(String lineIndenter) {
        String lineSeparator = "\n";
        return "<html>" + lineSeparator + StringUtils.repeat(lineIndenter, 1)
                + "<head>" + lineSeparator + StringUtils.repeat(lineIndenter, 2)
                + "<title>title</title>"
                + lineSeparator
                + StringUtils.repeat(lineIndenter, 2)
                + "<meta name=\"author\" content=\"Author\"/>"
                + lineSeparator
                + StringUtils.repeat(lineIndenter, 2)
                + "<meta name=\"date\" content=\"Date\"/>"
                + lineSeparator
                + StringUtils.repeat(lineIndenter, 1)
                + "</head>" + lineSeparator + StringUtils.repeat(lineIndenter, 1)
                + "<body>" + lineSeparator + StringUtils.repeat(lineIndenter, 2)
                + "<p>Paragraph 1, line 1. Paragraph 1, line 2.</p>"
                + lineSeparator
                + StringUtils.repeat(lineIndenter, 2)
                + "<div class=\"section\">"
                + lineSeparator
                + StringUtils.repeat(lineIndenter, 3)
                + "<h2>Section title</h2>"
                + lineSeparator
                + StringUtils.repeat(lineIndenter, 2)
                + "</div>" + lineSeparator + StringUtils.repeat(lineIndenter, 1)
                + "</body>" + lineSeparator + "</html>";
    }
}
