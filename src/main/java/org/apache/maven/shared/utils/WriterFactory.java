package org.apache.maven.shared.utils;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import org.apache.maven.shared.utils.xml.XmlStreamWriter;


import javax.annotation.Nonnull;

/**
 * Utility to create Writers, with explicit encoding choice: platform default,
 * XML, or specified.
 *
 * @author Hervé Boutemy
 * @see java.nio.charset.Charset
 * @see <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/intl/encoding.doc.html">Supported encodings</a>
 */
public class WriterFactory
{
    /**
     * ISO Latin Alphabet #1, also known as ISO-LATIN-1.
     * Every implementation of the Java platform is required to support this character encoding.
     *
     * @deprecated use {@code java.nio.charset.StandardCharset.ISO_8859_1}
     */
    @Deprecated
    public static final String ISO_8859_1 = "ISO-8859-1";

    /**
     * Seven-bit ASCII, also known as ISO646-US, also known as the Basic Latin block of the Unicode character set.
     * Every implementation of the Java platform is required to support this character encoding.
     *
     * @deprecated use {@code java.nio.charset.StandardCharset.US_ASCII}
     */
    @Deprecated
    public static final String US_ASCII = "US-ASCII";

    /**
     * Sixteen-bit Unicode Transformation Format, byte order specified by a mandatory initial byte-order mark (either
     * order accepted on input, big-endian used on output).
     * Every implementation of the Java platform is required to support this character encoding.
     *
     * @deprecated use {@code java.nio.charset.StandardCharset.UTF_16}
     */
    @Deprecated
    public static final String UTF_16 = "UTF-16";

    /**
     * Sixteen-bit Unicode Transformation Format, big-endian byte order.
     * Every implementation of the Java platform is required to support this character encoding.
     *
     * @deprecated use {@code java.nio.charset.StandardCharset.UTF_16BE}
     */
    @Deprecated
    public static final String UTF_16BE = "UTF-16BE";

    /**
     * Sixteen-bit Unicode Transformation Format, little-endian byte order.
     * Every implementation of the Java platform is required to support this character encoding.
     *
     * @deprecated use {@code java.nio.charset.StandardCharset.UTF_16LE}
     */
    @Deprecated
    public static final String UTF_16LE = "UTF-16LE";

    /**
     * Eight-bit Unicode Transformation Format.
     * Every implementation of the Java platform is required to support this character encoding.
     *
     * @deprecated use {@code java.nio.charset.StandardCharset.UTF_8}
     */
    @Deprecated
    public static final String UTF_8 = "UTF-8";

    /**
     * The <code>file.encoding</code> System Property.
     */
    public static final String FILE_ENCODING = System.getProperty( "file.encoding" );

    /**
     * Create a new Writer with XML encoding detection rules.
     *
     * @param out not null output stream
     * @return an XML writer instance for the output stream
     * @throws IOException if any
     * @see XmlStreamWriter
     */
    public static XmlStreamWriter newXmlWriter( @Nonnull OutputStream out )
        throws IOException
    {
        return new XmlStreamWriter( out );
    }

    /**
     * Create a new Writer with XML encoding detection rules.
     *
     * @param file not null file
     * @return an XML writer instance for the output file
     * @throws IOException if any
     * @see XmlStreamWriter
     */
    public static XmlStreamWriter newXmlWriter( @Nonnull File file )
        throws IOException
    {
        return new XmlStreamWriter( file );
    }

    /**
     * Create a new Writer with default platform encoding.
     *
     * @param out not null output stream
     * @return a writer instance for the output stream using the default platform charset
     * @deprecated always specify an encoding. Do not depend on the default platform character set.
     */
    @Deprecated
    public static Writer newPlatformWriter( @Nonnull OutputStream out )
    {
        return new OutputStreamWriter( out );
    }

    /**
     * Create a new Writer with default platform encoding.
     *
     * @param file not null file
     * @return a writer instance for the output file using the default platform charset
     * @throws IOException if any
     * @deprecated always specify an encoding. Do not depend on the default platform character set.
     */
    @Deprecated
    public static Writer newPlatformWriter( @Nonnull File file )
        throws IOException
    {
        return new FileWriter( file );
    }

    /**
     * Create a new Writer with specified encoding.
     *
     * @param out not null output stream
     * @param encoding not null supported encoding
     * @return a writer instance for the output stream using the given encoding
     * @throws UnsupportedEncodingException if any
     * @see <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/intl/encoding.doc.html">Supported
     *         encodings</a>
     */
    public static Writer newWriter( @Nonnull OutputStream out, @Nonnull String encoding )
        throws UnsupportedEncodingException
    {
        return new OutputStreamWriter( out, encoding );
    }

    /**
     * Create a new Writer with specified encoding.
     *
     * @param file not null file
     * @param encoding not null supported encoding
     * @return a writer instance for the output file using the given encoding
     * @throws UnsupportedEncodingException if any
     * @throws FileNotFoundException if any
     * @see <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/intl/encoding.doc.html">Supported
     *         encodings</a>
     */
    public static Writer newWriter( @Nonnull File file, @Nonnull String encoding )
        throws UnsupportedEncodingException, FileNotFoundException
    {
        return newWriter( new FileOutputStream( file ), encoding );
    }
}
