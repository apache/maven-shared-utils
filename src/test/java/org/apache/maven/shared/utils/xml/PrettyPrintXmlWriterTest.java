package org.apache.maven.shared.utils.xml;

import java.io.IOException;
import javax.swing.text.html.HTML;
import java.io.StringWriter;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */



import org.apache.maven.shared.utils.Os;
import org.apache.maven.shared.utils.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test of {@link PrettyPrintXMLWriter}
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id$
 */
public class PrettyPrintXmlWriterTest
{
    StringWriter w;

    PrettyPrintXMLWriter writer;

    @Before
    public void before()
            throws Exception
    {
        w = new StringWriter();
        writer = new PrettyPrintXMLWriter( w );
    }

    @After
    public void after()
            throws Exception
    {
        writer = null;
        w = null;
    }

    @Test
    public void testDefaultPrettyPrintXMLWriter() throws IOException
    {
        writer.startElement( HTML.Tag.HTML.toString() );

        writeXhtmlHead( writer );

        writeXhtmlBody( writer );

        writer.endElement(); // Tag.HTML

        Assert.assertEquals( expectedResult( Os.LINE_SEP ), w.toString() );
    }

    @Test
    public void testPrettyPrintXMLWriterWithGivenLineSeparator() throws IOException
    {
        writer.setLineSeparator( "\n" );

        writer.startElement( HTML.Tag.HTML.toString() );

        writeXhtmlHead( writer );

        writeXhtmlBody( writer );

        writer.endElement(); // Tag.HTML

        Assert.assertEquals( expectedResult( "\n" ), w.toString() );
    }

    @Test
    public void testPrettyPrintXMLWriterWithGivenLineIndenter() throws IOException
    {
        writer.setLineIndenter( "    " );

        writer.startElement( HTML.Tag.HTML.toString() );

        writeXhtmlHead( writer );

        writeXhtmlBody( writer );

        writer.endElement(); // Tag.HTML

        Assert.assertEquals( expectedResult( "    ", Os.LINE_SEP ), w.toString() );
    }

    @Test
    public void testEscapeXmlAttributeWindows() throws IOException
    {
        // Windows
        writer.startElement( HTML.Tag.DIV.toString() );
        writer.addAttribute( "class", "sect\r\nion" );
        writer.endElement(); // Tag.DIV
        Assert.assertEquals( "<div class=\"sect&#10;ion\"/>", w.toString() );
    }

    @Test
    public void testEscapeXmlAttributeMac() throws IOException
    {
        // Mac
        writer.startElement( HTML.Tag.DIV.toString() );
        writer.addAttribute( "class", "sect\rion" );
        writer.endElement(); // Tag.DIV
        Assert.assertEquals( "<div class=\"sect&#13;ion\"/>", w.toString() );
    }

    @Test
    public void testEscapeXmlAttributeTrailingCR() throws IOException
    {
        // Mac
        writer.startElement( HTML.Tag.DIV.toString() );
        writer.addAttribute( "class", "section\r" );
        writer.endElement(); // Tag.DIV
        Assert.assertEquals( "<div class=\"section&#13;\"/>", w.toString() );
    }

    @Test
    public void testEscapeXmlAttributeUnix() throws IOException
    {
        // Unix
        writer.startElement( HTML.Tag.DIV.toString() );
        writer.addAttribute( "class", "sect\nion" );
        writer.endElement(); // Tag.DIV
        Assert.assertEquals( "<div class=\"sect&#10;ion\"/>", w.toString() );
    }

    private void writeXhtmlHead( XMLWriter writer ) throws IOException
    {
        writer.startElement( HTML.Tag.HEAD.toString() );
        writer.startElement( HTML.Tag.TITLE.toString() );
        writer.writeText( "title" );
        writer.endElement(); // Tag.TITLE
        writer.startElement( HTML.Tag.META.toString() );
        writer.addAttribute( "name", "author" );
        writer.addAttribute( "content", "Author" );
        writer.endElement(); // Tag.META
        writer.startElement( HTML.Tag.META.toString() );
        writer.addAttribute( "name", "date" );
        writer.addAttribute( "content", "Date" );
        writer.endElement(); // Tag.META
        writer.endElement(); // Tag.HEAD
    }

    private void writeXhtmlBody( XMLWriter writer ) throws IOException
    {
        writer.startElement( HTML.Tag.BODY.toString() );
        writer.startElement( HTML.Tag.P.toString() );
        writer.writeText( "Paragraph 1, line 1. Paragraph 1, line 2." );
        writer.endElement(); // Tag.P
        writer.startElement( HTML.Tag.DIV.toString() );
        writer.addAttribute( "class", "section" );
        writer.startElement( HTML.Tag.H2.toString() );
        writer.writeText( "Section title" );
        writer.endElement(); // Tag.H2
        writer.endElement(); // Tag.DIV
        writer.endElement(); // Tag.BODY
    }

    private String expectedResult( String lineSeparator )
    {
        return expectedResult( "  ", lineSeparator );
    }

    private String expectedResult( String lineIndenter, String lineSeparator )
    {
        StringBuilder expected = new StringBuilder();

        expected.append( "<html>" ).append( lineSeparator );
        expected.append( StringUtils.repeat( lineIndenter, 1 ) ).append( "<head>" ).append( lineSeparator );
        expected.append( StringUtils.repeat( lineIndenter, 2 ) ).append( "<title>title</title>" )
                .append( lineSeparator );
        expected.append( StringUtils.repeat( lineIndenter, 2 ) )
                .append( "<meta name=\"author\" content=\"Author\"/>" ).append( lineSeparator );
        expected.append( StringUtils.repeat( lineIndenter, 2 ) ).append( "<meta name=\"date\" content=\"Date\"/>" )
                .append( lineSeparator );
        expected.append( StringUtils.repeat( lineIndenter, 1 ) ).append( "</head>" ).append( lineSeparator );
        expected.append( StringUtils.repeat( lineIndenter, 1 ) ).append( "<body>" ).append( lineSeparator );
        expected.append( StringUtils.repeat( lineIndenter, 2 ) )
                .append( "<p>Paragraph 1, line 1. Paragraph 1, line 2.</p>" ).append( lineSeparator );
        expected.append( StringUtils.repeat( lineIndenter, 2 ) ).append( "<div class=\"section\">" )
                .append( lineSeparator );
        expected.append( StringUtils.repeat( lineIndenter, 3 ) ).append( "<h2>Section title</h2>" )
                .append( lineSeparator );
        expected.append( StringUtils.repeat( lineIndenter, 2 ) ).append( "</div>" ).append( lineSeparator );
        expected.append( StringUtils.repeat( lineIndenter, 1 ) ).append( "</body>" ).append( lineSeparator );
        expected.append( "</html>" );

        return expected.toString();
    }
}
