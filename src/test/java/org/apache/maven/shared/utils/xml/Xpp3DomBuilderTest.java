package org.apache.maven.shared.utils.xml;

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

import org.apache.maven.shared.utils.StringUtils;
import org.apache.maven.shared.utils.xml.pull.XmlPullParserException;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * @author Kristian Rosenvold
 */
public class Xpp3DomBuilderTest
{

    private static final String LS = System.getProperty( "line.separator" );

    private static final String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

        @Test
    public void selfClosingTag()
        throws Exception
    {

        // Todo:  http://stackoverflow.com/questions/12968390/detecting-self-closing-tags-in-sax
        String domString = selfClosingTagSource();

        Xpp3Dom dom = Xpp3DomBuilder.build( new StringReader( domString ) );

        String expected = expectedSelfClosingTag();
        String dom1Str = dom.toString();
        assertEquals( "check DOMs match", expected, dom1Str );
    }

    @Test
    public void trimming()
        throws Exception
    {
        String domString = createDomString();

        Xpp3Dom dom = Xpp3DomBuilder.build( new StringReader( domString ), true );

        assertEquals( "element1value", dom.getChild( "element1" ).getValue() );

        assertEquals( "  preserve space  ", dom.getChild( "element6" ).getValue() );

        dom = Xpp3DomBuilder.build( new StringReader( domString ), false );

        assertEquals( " element1value\n ", dom.getChild( "element1" ).getValue() );

        assertEquals( "  preserve space  ", dom.getChild( "element6" ).getValue() );
    }

    @Test(expected = XmlPullParserException.class)
    public void malformedXml()
    {
        Xpp3DomBuilder.build( new StringReader( "<newRoot>" + createDomString() ) );
        fail( "We're supposed to fail" );
    }

    @Test
    public void attributeEscaping()
        throws IOException, XmlPullParserException
    {
        String s = getAttributeEncodedString();
        Xpp3Dom dom = Xpp3DomBuilder.build( new StringReader( s ) );

        assertEquals( "<foo>", dom.getChild( "el" ).getAttribute( "att" ) );
        StringWriter w = new StringWriter();
        Xpp3DomWriter.write( w, dom );
        String newString = w.toString();
        assertEquals( newString, s );
    }

    @Test
    public void contentEscaping()
        throws IOException, XmlPullParserException
    {
        Xpp3Dom dom = Xpp3DomBuilder.build( new StringReader( getEncodedString() ) );

        assertEquals( "\"msg\"", dom.getChild( "a1" ).getValue() );
        assertEquals( "<b>\"msg\"</b>", dom.getChild( "a2" ).getValue() );
        assertEquals( "<b>\"msg\"</b>", dom.getChild( "a3" ).getValue() );

        StringWriter w = new StringWriter();
        Xpp3DomWriter.write( w, dom );
        assertEquals( getExpectedString(), w.toString() );
    }

    private static String getAttributeEncodedString()
    {
        StringBuilder domString = new StringBuilder();
        domString.append( "<root>" );
        domString.append( LS );
        domString.append( "  <el att=\"&lt;foo&gt;\">bar</el>" );
        domString.append( LS );
        domString.append( "</root>" );

        return domString.toString();
    }

    private static String getEncodedString()
    {
        StringBuilder domString = new StringBuilder();
        domString.append( "<root>\n" );
        domString.append( "  <a1>\"msg\"</a1>\n" );
        domString.append( "  <a2><![CDATA[<b>\"msg\"</b>]]></a2>\n" );
        domString.append( "  <a3>&lt;b&gt;&quot;msg&quot;&lt;/b&gt;</a3>\n" );
        domString.append( "</root>" );

        return domString.toString();
    }

    private static String getExpectedString()
    {
        StringBuilder domString = new StringBuilder();
        domString.append( "<root>" );
        domString.append( LS );
        domString.append( "  <a1>\"msg\"</a1>" );
        domString.append( LS );
        domString.append( "  <a2>&lt;b&gt;\"msg\"&lt;/b&gt;</a2>" );
        domString.append( LS );
        domString.append( "  <a3>&lt;b&gt;\"msg\"&lt;/b&gt;</a3>" );
        domString.append( LS );
        domString.append( "</root>" );
        return domString.toString();
    }

    private static String createDomString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append( "<root>\n" );
        buf.append( " <element1> element1value\n </element1>\n" );
        buf.append( " <element2 att2='attribute2&#10;nextline'>\n" );
        buf.append( "  <element3 att3='attribute3'>element3value</element3>\n" );
        buf.append( " </element2>\n" );
        buf.append( " <element4></element4>\n" );
        buf.append( " <element5/>\n" );
        buf.append( " <element6 xml:space=\"preserve\">  preserve space  </element6>\n" );
        buf.append( "</root>\n" );

        return buf.toString();
    }

    private static String selfClosingTagSource()
    {
        StringBuilder buf = new StringBuilder();
        buf.append( "<root>\n" );
        buf.append( "  <el4></el4>\n" );
        buf.append( "  <el5></el5>\n" );
        buf.append( "</root>" );
        return StringUtils.unifyLineSeparators( buf.toString() );
    }

    private static String expectedSelfClosingTag()
    {
        return StringUtils.unifyLineSeparators( xmlDeclaration + selfClosingTagSource() );
    }

}
