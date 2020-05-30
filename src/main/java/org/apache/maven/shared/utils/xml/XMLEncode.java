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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Collection of XML encoding/decoding helpers. <br>
 * This is all about the special characters &amp; and &lt;, and for attributes
 * &quot; and &apos;. These must be encoded/decoded from/to XML.
 */
final class XMLEncode
{

    private static final int CDATA_BLOCK_THRESHOLD_LENGTH = 12;

    private static final char DEFAULT_QUOTE_CHAR = '"';

    /**
     * Checks if this text purely consists of the white space characters
     * ' ',  TAB, NEWLINE.
     */
    static boolean isWhiteSpace( String text )
    {
        for ( int i = 0; i < text.length(); i++ )
        {
            char c = text.charAt( i );
            if ( !Character.isWhitespace( c ) )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Makes any text fit into XML attributes.
     */
    static String xmlEncodeTextForAttribute( String text, char quoteChar ) throws IOException
    {
        if ( text == null )
        {
            return null;
        }
        return xmlEncodeTextAsPCDATA( text, true, quoteChar );
    }

    /**
     * Encodes text as XML in the most suitable way, either CDATA block or PCDATA.
     */
    static String xmlEncodeText( String text ) throws IOException
    {
        if ( text == null )
        {
            return null;
        }
        StringWriter writer = new StringWriter( text.length() * 2 );
        xmlEncodeText( text, writer );
        return writer.toString();
    }

    static void xmlEncodeText( String text, Writer writer ) throws IOException
    {
        if ( text == null )
        {
            return;
        }
        if ( !needsEncoding( text ) )
        {
            writer.write( text );
            return;
        }
        else
        {
            // only encode as cdata if is is longer than CDATA block overhead:
            if ( text.length() > CDATA_BLOCK_THRESHOLD_LENGTH )
            {
                String cdata = xmlEncodeTextAsCDATABlock( text );
                if ( cdata != null )
                {
                    writer.write( cdata );
                    return;
                }
            }
        }
        // if every thing else fails, do it the save way...
        xmlEncodeTextAsPCDATA( text, false, DEFAULT_QUOTE_CHAR, writer );
    }

    /**
     * Encodes any text as PCDATA.
     * @throws IOException 
     */
    static String xmlEncodeTextAsPCDATA( String text ) throws IOException
    {
        if ( text == null )
        {
            return null;
        }
        return xmlEncodeTextAsPCDATA( text, false );
    }

    /**
     * Encodes any text as PCDATA.
     *
     * @param forAttribute if you want
     *                     quotes and apostrophes specially treated for attributes
     */
    static String xmlEncodeTextAsPCDATA( String text, boolean forAttribute ) throws IOException
    {
        return xmlEncodeTextAsPCDATA( text, forAttribute, DEFAULT_QUOTE_CHAR );
    }

    /**
     * Encodes any text as PCDATA.
     *
     * @param forAttribute if you want
     *                     quotes and apostrophes specially treated for attributes
     * @param quoteChar    if this is for attributes this <code>char</code> is used to quote the attribute value
     * @throws IOException 
     */
    static String xmlEncodeTextAsPCDATA( String text, boolean forAttribute, char quoteChar )
                    throws IOException
    {
        if ( text == null )
        {
            return null;
        }
        StringWriter writer = new StringWriter( text.length() * 2 );
        xmlEncodeTextAsPCDATA( text, forAttribute, quoteChar, writer );
        return writer.toString();
    }

    static void xmlEncodeTextAsPCDATA( String text, boolean forAttribute, char quoteChar, Writer n )
                    throws IOException
    {
        if ( text == null )
        {
            return;
        }
        int length = text.length();
        if ( forAttribute )
        {
            n.append( quoteChar );
        }

        for ( int i = 0; i < length; i++ )
        {
            char c = text.charAt( i );
            switch ( c )
            {
                case '&':
                    n.append( "&amp;" );
                    break;
                case '<':
                    n.append( "&lt;" );
                    break;
                case '>': // FIX for sourceforge bug #802520 ("]]>" needs encoding)
                    n.append( "&gt;" );
                    break;
                case '"':
                    if ( forAttribute )
                    {
                        n.append( "&quot;" );
                    }
                    else
                    {
                        n.append( c );
                    }
                    break;
                case '\'':
                    if ( forAttribute )
                    {
                        n.append( "&apos;" );
                    }
                    else
                    {
                        n.append( c );
                    }
                    break;
                case '\r':
                    if ( forAttribute )
                    {
                        if ( i == ( length - 1 ) || text.charAt( i + 1 ) != '\n' )
                        {
                            n.append( "&#13;" );
                        }
                    }
                    else
                    {
                        n.append( c );
                    }
                    // but skip the \r in \r\n

                    break;
                case '\n':
                    if ( forAttribute )
                    {
                        n.append( "&#10;" );
                    }
                    break;

                default:
                    n.append( c );
                    break;
            }
        }

        if ( forAttribute )
        {
            n.append( quoteChar );
        }
    }

    /**
     * Returns string as CDATA block if possible, otherwise null.
     */
    static String xmlEncodeTextAsCDATABlock( String text )
    {
        if ( text == null )
        {
            return null;
        }
        if ( isCompatibleWithCDATABlock( text ) )
        {
            return "<![CDATA[" + text + "]]>";
        }
        else
        {
            return null;
        }
    }

    /**
     * Checks if this text needs encoding in order to be represented in XML.
     */
    static boolean needsEncoding( String text )
    {
        return needsEncoding( text, false );
    }

    /**
     * Checks if this text needs encoding in order to be represented in XML.
     * <p/>
     * Set <code>checkForAttr</code> if you want to check for storability in
     * an attribute.
     */
    static boolean needsEncoding( String data, boolean checkForAttr )
    {
        if ( data == null )
        {
            return false;
        }
        char c;
        for ( int i = 0; i < data.length(); i++ )
        {
            c = data.charAt( i );
            if ( c == '&' || c == '<' || ( checkForAttr && ( c == '"' || c == '\'' ) ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Can this text be stored in a CDATA block?
     */
    static boolean isCompatibleWithCDATABlock( String text )
    {
        return text != null && ( !text.contains( "]]>" ) );
    }

    /**
     * Make CDATA out of possibly encoded PCDATA. <br>
     * E.g. make '&amp;' out of '&amp;amp;'
     */
    static String xmlDecodeTextToCDATA( String pcdata )
    {
        if ( pcdata == null )
        {
            return null;
        }
        char c, c1, c2, c3, c4, c5;
        StringBuilder n = new StringBuilder( pcdata.length() );
        for ( int i = 0; i < pcdata.length(); i++ )
        {
            c = pcdata.charAt( i );
            if ( c == '&' )
            {
                c1 = lookAhead( 1, i, pcdata );
                c2 = lookAhead( 2, i, pcdata );
                c3 = lookAhead( 3, i, pcdata );
                c4 = lookAhead( 4, i, pcdata );
                c5 = lookAhead( 5, i, pcdata );

                if ( c1 == 'a' && c2 == 'm' && c3 == 'p' && c4 == ';' )
                {
                    n.append( "&" );
                    i += 4;
                }
                else if ( c1 == 'l' && c2 == 't' && c3 == ';' )
                {
                    n.append( "<" );
                    i += 3;
                }
                else if ( c1 == 'g' && c2 == 't' && c3 == ';' )
                {
                    n.append( ">" );
                    i += 3;
                }
                else if ( c1 == 'q' && c2 == 'u' && c3 == 'o' && c4 == 't' && c5 == ';' )
                {
                    n.append( "\"" );
                    i += 5;
                }
                else if ( c1 == 'a' && c2 == 'p' && c3 == 'o' && c4 == 's' && c5 == ';' )
                {
                    n.append( "'" );
                    i += 5;
                }
                else
                {
                    n.append( "&" );
                }
            }
            else
            {
                n.append( c );
            }
        }
        return n.toString();
    }

    private static char lookAhead( int la, int offset, String data )
    {
        try
        {
            return data.charAt( offset + la );
        }
        catch ( StringIndexOutOfBoundsException e )
        {
            return 0x0;
        }
    }

}
