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
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import org.apache.maven.shared.utils.Os;

/**
 * XMLWriter with nice indentation
 */
/**
 * @author kama
 *
 */
public class PrettyPrintXMLWriter
    implements XMLWriter
{
    private static final char[] CLOSE_1 = "/>".toCharArray();

    private static final char[] CLOSE_2 = "</".toCharArray();

    private static final char[] DEFAULT_LINE_INDENT = new char[]{ ' ', ' ' };

    private PrintWriter writer;

    private ArrayList<String> elementStack = new ArrayList<String>();

    private boolean processingElement = false;

    private boolean documentStarted = false;

    private boolean endOnSameLine = false;

    private int depth = 0;

    private char[] lineIndent;

    private char[] lineSeparator;

    private String encoding;

    private String docType;

    /**
     * @param writer not null
     * @param lineIndent could be null, but the normal way is some spaces.
     */
    public PrettyPrintXMLWriter( PrintWriter writer, String lineIndent )
    {
        this( writer, lineIndent, null, null );
    }

    /**
     * @param writer not null
     * @param lineIndent could be null, but the normal way is some spaces.
     */
    public PrettyPrintXMLWriter( Writer writer, String lineIndent )
    {
        this( new PrintWriter( writer ), lineIndent );
    }

    /**
     * @param writer not null
     */
    public PrettyPrintXMLWriter( PrintWriter writer )
    {
        this( writer, null, null );
    }

    /**
     * @param writer not null
     */
    public PrettyPrintXMLWriter( Writer writer )
    {
        this( new PrintWriter( writer ) );
    }

    /**
     * @param writer not null
     * @param lineIndent could be null, but the normal way is some spaces.
     * @param encoding could be null or invalid.
     * @param doctype could be null.
     */
    public PrettyPrintXMLWriter( PrintWriter writer, String lineIndent, String encoding, String doctype )
    {
        this( writer, lineIndent.toCharArray(), Os.LINE_SEP.toCharArray(), encoding, doctype );
    }

    /**
     * @param writer not null
     * @param lineIndent could be null, but the normal way is some spaces.
     * @param encoding could be null or invalid.
     * @param doctype could be null.
     */
    public PrettyPrintXMLWriter( Writer writer, String lineIndent, String encoding, String doctype )
    {
        this( new PrintWriter( writer ), lineIndent, encoding, doctype );
    }

    /**
     * @param writer not null
     * @param encoding could be null or invalid.
     * @param doctype could be null.
     */
    public PrettyPrintXMLWriter( PrintWriter writer, String encoding, String doctype )
    {
        this( writer, DEFAULT_LINE_INDENT, Os.LINE_SEP.toCharArray(), encoding, doctype );
    }

    /**
     * @param writer not null
     * @param encoding could be null or invalid.
     * @param doctype could be null.
     */
    public PrettyPrintXMLWriter( Writer writer, String encoding, String doctype )
    {
        this( new PrintWriter( writer ), encoding, doctype );
    }

    /**
     * @param writer not null
     * @param lineIndent could be null, but the normal way is some spaces.
     * @param lineSeparator could be null, but the normal way is valid line separator
     * @param encoding could be null or the encoding to use.
     * @param doctype could be null.
     */
    public PrettyPrintXMLWriter( PrintWriter writer, String lineIndent, String lineSeparator, String encoding,
                                 String doctype )
    {
        this( writer, lineIndent.toCharArray(), lineSeparator.toCharArray(), encoding, doctype );
    }

    /**
     * @param writer        not null
     * @param lineIndent    could be null, but the normal way is some spaces.
     * @param lineSeparator could be null, but the normal way is valid line separator
     * @param encoding      could be null or the encoding to use.
     * @param doctype       could be null.
     */
    private PrettyPrintXMLWriter( PrintWriter writer, char[] lineIndent, char[] lineSeparator, String encoding,
                                  String doctype )
    {
        super();
        this.writer = writer;
        this.lineIndent = lineIndent;
        this.lineSeparator = lineSeparator;
        this.encoding = encoding;
        this.docType = doctype;

        depth = 0;

        // Fail early with assertions enabled. Issue is in the calling code not having checked for any errors.
        assert !writer.checkError() : "Unexpected error state PrintWriter passed to PrettyPrintXMLWriter.";
    }

    /** {@inheritDoc} */
    public void addAttribute( String key, String value ) throws IOException
    {
        if ( !processingElement )
        {
            throw new IllegalStateException( "currently processing no element" );
        }

        writer.write( ' ' );
        writer.write( key );
        writer.write( '=' );
        XMLEncode.xmlEncodeTextAsPCDATA( value, true, '"', writer );
        if ( writer.checkError() )
        {
            throw new IOException( "Failure adding attribute '" + key + "' with value '" + value + "'" );
        }
    }

    /** {@inheritDoc} */
    public void setEncoding( String encoding )
    {
        if ( documentStarted )
        {
            throw new IllegalStateException( "Document headers already written!" );
        }

        this.encoding = encoding;
    }

    /** {@inheritDoc} */
    public void setDocType( String docType )
    {
        if ( documentStarted )
        {
            throw new IllegalStateException( "Document headers already written!" );
        }

        this.docType = docType;
    }

    /**
     * @param lineSeparator The line separator to be used.
     */
    public void setLineSeparator( String lineSeparator )
    {
        if ( documentStarted )
        {
            throw new IllegalStateException( "Document headers already written!" );
        }

        this.lineSeparator = lineSeparator.toCharArray();
    }

    /**
     * @param lineIndentParameter The line indent parameter.
     */
    public void setLineIndenter( String lineIndentParameter )
    {
        if ( documentStarted )
        {
            throw new IllegalStateException( "Document headers already written!" );
        }

        this.lineIndent = lineIndentParameter.toCharArray();
    }

    /** {@inheritDoc} */
    public void startElement( String elementName ) throws IOException
    {
        boolean firstLine = ensureDocumentStarted();

        completePreviouslyOpenedElement();

        if ( !firstLine )
        {
            newLine();
        }

        writer.write( '<' );
        writer.write( elementName );
        if ( writer.checkError() )
        {
            throw new IOException( "Failure starting element '" + elementName + "'." );
        }

        processingElement = true;

        elementStack.add( depth++, elementName );
    }

    /** {@inheritDoc} */
    public void writeText( String text ) throws IOException
    {
        ensureDocumentStarted();

        completePreviouslyOpenedElement();

        XMLEncode.xmlEncodeText( text, writer );

        endOnSameLine = true;
        
        if ( writer.checkError() )
        {
            throw new IOException( "Failure writing text." );
        }
    }

    /** {@inheritDoc} */
    public void writeMarkup( String markup ) throws IOException
    {
        ensureDocumentStarted();

        completePreviouslyOpenedElement();

        writer.write( markup );

        if ( writer.checkError() )
        {
            throw new IOException( "Failure writing markup." );
        }
    }

    /** {@inheritDoc} */
    public void endElement() throws IOException
    {
        String chars = elementStack.get( --depth );
        if ( processingElement )
        {
            // this means we don't have any content yet so we just add a />
            writer.write( CLOSE_1 );

            processingElement = false;
        }
        else
        {
            if ( !endOnSameLine )
            {
                newLine();
            }

            // otherwise we need a full closing tag for that element
            writer.write( CLOSE_2 );
            writer.write( chars );
            writer.write( '>' );
        }

        endOnSameLine = false;

        if ( writer.checkError() )
        {
            throw new IOException( "Failure ending element." );
        }
    }

    /**
     * Write the documents if not already done.
     *
     * @return <code>true</code> if the document headers have freshly been written.
     */
    private boolean ensureDocumentStarted()
    {
        if ( !documentStarted )
        {
            if ( docType != null || encoding != null )
            {
                writeDocumentHeader();
            }

            documentStarted = true;

            return true;
        }

        return false;
    }

    private void writeDocumentHeader()
    {
        writer.write( "<?xml version=\"1.0\"" );

        if ( encoding != null )
        {
            writer.write( " encoding=\"" );
            writer.write( encoding );
            writer.write( '\"' );
        }

        writer.write( "?>" );

        newLine();

        if ( docType != null )
        {
            newLine();
            writer.write( "<!DOCTYPE " );
            writer.write( docType );
            writer.write( '>' );
        }
    }

    private void newLine()
    {
        writer.write( lineSeparator );

        for ( int i = 0; i < depth; i++ )
        {
            writer.write( lineIndent );
        }
    }

    private void completePreviouslyOpenedElement()
    {
        if ( processingElement )
        {
            writer.write( '>' );
            processingElement = false;
        }
    }

}
