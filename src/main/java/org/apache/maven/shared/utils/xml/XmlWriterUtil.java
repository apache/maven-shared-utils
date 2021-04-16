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
import org.apache.maven.shared.utils.StringUtils;

/**
 * Utility class for the <code>XmlWriter</code> class.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 *
 */
public class XmlWriterUtil
{
    /** The vm line separator */
    public static final String LS = System.getProperty( "line.separator" );

    /** The default line indenter size i.e. 2. */
    public static final int DEFAULT_INDENTATION_SIZE = 2;

    /** The default column before line wrapping i.e. 80. */
    public static final int DEFAULT_COLUMN_LINE = 80;

    /**
     * Convenience method to write one <code>CRLF</code>.
     *
     * @param writer not null writer
     * @throws IOException if writing fails.
     */
    public static void writeLineBreak( XMLWriter writer ) throws IOException
    {
        writeLineBreak( writer, 1 );
    }

    /**
     * Convenience method to repeat <code>CRLF</code>.
     *
     * @param writer not null
     * @param repeat positive number
     * @throws IOException if writing fails.
     */
    public static void writeLineBreak( XMLWriter writer, int repeat ) throws IOException
    {
        for ( int i = 0; i < repeat; i++ )
        {
            writer.writeMarkup( LS );
        }
    }

    /**
     * Convenience method to repeat <code>CRLF</code> and to indent the writer by <code>2</code>.
     *
     * @param writer not null
     * @param repeat The number of repetitions of the indent
     * @param indent positive number
     * @see #DEFAULT_INDENTATION_SIZE
     * @see #writeLineBreak(XMLWriter, int, int, int)
     * @throws IOException if writing fails.
     */
    public static void writeLineBreak( XMLWriter writer, int repeat, int indent ) throws IOException
    {
        writeLineBreak( writer, repeat, indent, DEFAULT_INDENTATION_SIZE );
    }

    /**
     * Convenience method to repeat <code>CRLF</code> and to indent the writer by <code>indentSize</code>.
     *
     * @param writer not null
     * @param repeat The number of repetitions of the indent
     * @param indent positive number
     * @param indentSize positive number
     * @throws IOException if writing fails.
     */
    public static void writeLineBreak( XMLWriter writer, int repeat, int indent, int indentSize ) throws IOException
    {
        writeLineBreak( writer, repeat );

        if ( indent < 0 )
        {
            indent = 0;
        }

        if ( indentSize < 0 )
        {
            indentSize = 0;
        }

        writer.writeText( StringUtils.repeat( " ", indent * indentSize ) );
    }

    /**
     * Convenience method to write XML comment line break. Its size is <code>80</code>.
     *
     * @param writer not null
     * @see #DEFAULT_COLUMN_LINE
     * @see #writeCommentLineBreak(XMLWriter, int)
     * @throws IOException if writing fails.
     */
    public static void writeCommentLineBreak( XMLWriter writer ) throws IOException
    {
        writeCommentLineBreak( writer, DEFAULT_COLUMN_LINE );
    }

    /**
     * Convenience method to write XML comment line break with <code>columnSize</code> as length.
     *
     * @param writer not null
     * @param columnSize positive number
     * @throws IOException if writing fails.
     */
    public static void writeCommentLineBreak( XMLWriter writer, int columnSize ) throws IOException
    {
        if ( columnSize < 10 )
        {
            columnSize = DEFAULT_COLUMN_LINE;
        }

        writer.writeMarkup( "<!-- " + StringUtils.repeat( "=", columnSize - 10 ) + " -->" + LS );
    }

    /**
     * Convenience method to write XML comment line. The <code>comment</code> is splitted to have a size of
     * <code>80</code>.
     *
     * @param writer not null
     * @param comment The comment to write
     * @see #DEFAULT_INDENTATION_SIZE
     * @see #writeComment(XMLWriter, String, int, int)
     * @throws IOException if writing fails.
     */
    public static void writeComment( XMLWriter writer, String comment ) throws IOException
    {
        writeComment( writer, comment, 0, DEFAULT_INDENTATION_SIZE );
    }

    /**
     * Convenience method to write XML comment line. The <code>comment</code> is split to have a size of
     * <code>80</code> and is indented by <code>indent</code> using <code>2</code> as indentation size.
     *
     * @param writer not null
     * @param comment The comment to write
     * @param indent positive number
     * @see #DEFAULT_INDENTATION_SIZE
     * @see #writeComment(XMLWriter, String, int, int)
     * @throws IOException if writing fails.
     */
    public static void writeComment( XMLWriter writer, String comment, int indent ) throws IOException
    {
        writeComment( writer, comment, indent, DEFAULT_INDENTATION_SIZE );
    }

    /**
     * Convenience method to write XML comment line. The <code>comment</code> is split to have a size of <code>80</code>
     * and is indented by <code>indent</code> using <code>indentSize</code>.
     *
     * @param writer not null
     * @param comment The comment to write
     * @param indent positive number
     * @param indentSize positive number
     * @see #DEFAULT_COLUMN_LINE
     * @see #writeComment(XMLWriter, String, int, int, int)
     * @throws IOException if writing fails.
     */
    public static void writeComment( XMLWriter writer, String comment, int indent, int indentSize ) throws IOException
    {
        writeComment( writer, comment, indent, indentSize, DEFAULT_COLUMN_LINE );
    }
    
    /**
     * Convenience method to write XML comment line. The <code>comment</code> is split to have a size of
     * <code>columnSize</code> and is indented by <code>indent</code> using <code>indentSize</code>.
     *
     * @param writer not null
     * @param comment The comment to write
     * @param indent positive number
     * @param indentSize positive number
     * @param columnSize positive number
     * @throws IOException if writing fails.
     */
    public static void writeComment( XMLWriter writer, String comment, int indent, int indentSize, int columnSize )
        throws IOException
    {
        if ( comment == null )
        {
            comment = "null";
        }

        if ( indent < 0 )
        {
            indent = 0;
        }

        if ( indentSize < 0 )
        {
            indentSize = 0;
        }

        if ( columnSize < 0 )
        {
            columnSize = DEFAULT_COLUMN_LINE;
        }

        String indentation = StringUtils.repeat( " ", indent * indentSize );
        int magicNumber = indentation.length() + columnSize - "-->".length() - 1;
        String[] sentences = StringUtils.split( comment, LS );

        StringBuffer line = new StringBuffer( indentation + "<!-- " );
        for ( String sentence : sentences )
        {
            String[] words = StringUtils.split( sentence, " " );
            for ( String word : words )
            {
                StringBuilder sentenceTmp = new StringBuilder( line.toString() );
                sentenceTmp.append( word ).append( ' ' );
                if ( sentenceTmp.length() > magicNumber )
                {
                    if ( line.length() != indentation.length() + "<!-- ".length() )
                    {
                        if ( magicNumber - line.length() > 0 )
                        {
                            line.append( StringUtils.repeat( " ", magicNumber - line.length() ) );
                        }

                        line.append( "-->" ).append( LS );
                        writer.writeMarkup( line.toString() );
                    }
                    line = new StringBuffer( indentation + "<!-- " );
                    line.append( word ).append( ' ' );
                }
                else
                {
                    line.append( word ).append( ' ' );
                }
            }

            if ( magicNumber - line.length() > 0 )
            {
                line.append( StringUtils.repeat( " ", magicNumber - line.length() ) );
            }
        }

        if ( line.length() <= magicNumber )
        {
            line.append( StringUtils.repeat( " ", magicNumber - line.length() ) );
        }

        line.append( "-->" ).append( LS );

        writer.writeMarkup( line.toString() );
    }

    /**
     * Convenience method to write XML comments between two comments line break.
     * The XML comment block is not indented.
     *
     * @param writer not null
     * @param comment The comment to write
     * @see #DEFAULT_INDENTATION_SIZE
     * @see #writeCommentText(XMLWriter, String, int, int)
     * @throws IOException if writing fails.
     */
    public static void writeCommentText( XMLWriter writer, String comment ) throws IOException
    {
        writeCommentText( writer, comment, 0, DEFAULT_INDENTATION_SIZE );
    }

    /**
     * Convenience method to write XML comments between two comments line break.
     * The XML comment block is also indented by <code>indent</code> using
     * <code>2</code> as indentation size.
     *
     * @param writer not null
     * @param comment The comment to write
     * @param indent positive number
     * @see #DEFAULT_INDENTATION_SIZE
     * @see #writeCommentText(XMLWriter, String, int, int)
     * @throws IOException if writing fails.
     */
    public static void writeCommentText( XMLWriter writer, String comment, int indent ) throws IOException
    {
        writeCommentText( writer, comment, indent, DEFAULT_INDENTATION_SIZE );
    }

    /**
     * Convenience method to write XML comment between two comment line break.
     * The XML comment block is also indented by <code>indent</code> using <code>indentSize</code>.
     *
     * @param writer not null
     * @param comment The comment to write
     * @param indent positive number
     * @param indentSize positive number
     * @see #DEFAULT_COLUMN_LINE
     * @see #writeCommentText(XMLWriter, String, int, int, int)
     * @throws IOException if writing fails.
     */
    public static void writeCommentText( XMLWriter writer, String comment, int indent, int indentSize )
        throws IOException
    {
        writeCommentText( writer, comment, indent, indentSize, DEFAULT_COLUMN_LINE );
    }

    /**
     * Convenience method to write XML comments between two comments line break.
     * The XML comment block is also indented by <code>indent</code> using <code>indentSize</code>.
     * The column size could be also be specified.
     *
     * @param writer not null
     * @param comment The comment to write
     * @param indent positive number
     * @param indentSize positive number
     * @param columnSize positive number
     * @throws IOException if writing fails.
     */
    public static void writeCommentText( XMLWriter writer, String comment, int indent, int indentSize, int columnSize )
        throws IOException
    {
        if ( indent < 0 )
        {
            indent = 0;
        }

        if ( indentSize < 0 )
        {
            indentSize = 0;
        }

        if ( columnSize < 0 )
        {
            columnSize = DEFAULT_COLUMN_LINE;
        }

        writeLineBreak( writer, 1 );

        writer.writeMarkup( StringUtils.repeat( " ", indent * indentSize ) );
        writeCommentLineBreak( writer, columnSize );

        writeComment( writer, comment, indent, indentSize, columnSize );

        writer.writeMarkup( StringUtils.repeat( " ", indent * indentSize ) );
        writeCommentLineBreak( writer, columnSize );

        writeLineBreak( writer, 1, indent, indentSize );
    }
}
