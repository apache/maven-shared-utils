package org.apache.maven.shared.utils.io;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.channels.Channel;

/**
 * <p>General IO Stream manipulation.</p>
 * <p>
 * This class provides static utility methods for input/output operations, particularly buffered
 * copying between sources (<code>InputStream</code>, <code>Reader</code>, <code>String</code> and
 * <code>byte[]</code>) and destinations (<code>OutputStream</code>, <code>Writer</code>,
 * <code>String</code> and <code>byte[]</code>).</p>
 * 
 * <p>Unless otherwise noted, these <code>copy</code> methods do <em>not</em> flush or close the
 * streams. Often, doing so would require making non-portable assumptions about the streams' origin
 * and further use. This means that both streams' <code>close()</code> methods must be called after
 * copying. if one omits this step, then the stream resources (sockets, file descriptors) are
 * released when the associated Stream is garbage-collected. It is not a good idea to rely on this
 * mechanism.</p>
 * 
 * <p>For each <code>copy</code> method, a variant is provided that allows the caller to specify the
 * buffer size (the default is 4k). As the buffer size can have a fairly large impact on speed, this
 * may be worth tweaking. Often "large buffer -&gt; faster" does not hold, even for large data
 * transfers.</p>
 * <p>For byte-to-char methods, a <code>copy</code> variant allows the encoding to be selected
 * (otherwise the platform default is used).</p>
 * <p>The <code>copy</code> methods use an internal buffer when copying. It is therefore advisable
 * <em>not</em> to deliberately wrap the stream arguments to the <code>copy</code> methods in
 * <code>Buffered*</code> streams. For example, don't do the
 * following:</p>
 * <p>
 * <code>copy( new BufferedInputStream( in ), new BufferedOutputStream( out ) );</code>
 * </p>
 * <p>The rationale is as follows:</p>
 * 
 * <p>Imagine that an InputStream's read() is a very expensive operation, which would usually suggest
 * wrapping in a BufferedInputStream. The BufferedInputStream works by issuing infrequent
 * {@link java.io.InputStream#read(byte[] b, int off, int len)} requests on the underlying InputStream, to
 * fill an internal buffer, from which further <code>read</code> requests can inexpensively get
 * their data (until the buffer runs out).</p>
 * <p>However, the <code>copy</code> methods do the same thing, keeping an internal buffer,
 * populated by {@link InputStream#read(byte[] b, int off, int len)} requests. Having two buffers
 * (or three if the destination stream is also buffered) is pointless, and the unnecessary buffer
 * management hurts performance slightly (about 3%, according to some simple experiments).</p>
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @version CVS $Revision$ $Date$
 *
 */
public final class IOUtil
/*
 * Behold, intrepid explorers; a map of this class:
 *
 *       Method      Input               Output          Dependency
 *       ------      -----               ------          -------
 * 1     copy        InputStream         OutputStream    (primitive)
 * 2     copy        Reader              Writer          (primitive)
 *
 * 3     copy        InputStream         Writer          2
 * 4     toString    InputStream         String          3
 * 5     toByteArray InputStream         byte[]          1
 *
 * 6     copy        Reader              OutputStream    2
 * 7     toString    Reader              String          2
 * 8     toByteArray Reader              byte[]          6
 *
 * 9     copy        String              OutputStream    2
 * 10    copy        String              Writer          (trivial)
 * 11    toByteArray String              byte[]          9
 *
 * 12    copy        byte[]              Writer          3
 * 13    toString    byte[]              String          12
 * 14    copy        byte[]              OutputStream    (trivial)
 *
 *
 * Note that only the first two methods shuffle bytes; the rest use these two, or (if possible) copy
 * using native Java copy methods. As there are method variants to specify buffer size and encoding,
 * each row may correspond to up to 4 methods.
 *
 */
{
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Private constructor to prevent instantiation.
     */
    private IOUtil()
    {
    }

    ///////////////////////////////////////////////////////////////
    // Core copy methods
    ///////////////////////////////////////////////////////////////

    /**
     * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
     * 
     * @param input the stream to read from
     * @param output the stream to write to
     * @throws IOException in case of an error
     * @deprecated use {@code org.apache.commons.io.IOUtils.copy()} or in
     *         Java 9 and later {@code InputStream.transferTo()}.
     */
    @Deprecated
    public static void copy( @Nonnull final InputStream input, @Nonnull final OutputStream output )
        throws IOException
    {
        copy( input, output, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
     *
     * In Java 9 and later this is replaced by {@code InputStream.transferTo()}.
     * 
     * @param input the stream to read from
     * @param output the stream to write to
     * @param bufferSize size of internal buffer
     * @throws IOException in case of an error
     * @deprecated use {@code org.apache.commons.io.IOUtils.copy()} or in
     *         Java 9 and later {@code InputStream.transferTo()}.
     */
    @Deprecated
    public static void copy( @Nonnull final InputStream input, @Nonnull final OutputStream output,
                             final int bufferSize )
        throws IOException
    {
        final byte[] buffer = new byte[bufferSize];
        int n;
        while ( -1 != ( n = input.read( buffer ) ) )
        {
            output.write( buffer, 0, n );
        }
    }

    /**
     * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
     * 
     * @param input the reader to read from
     * @param output the writer to write to
     * @throws IOException in case of failure     * @deprecated use {@code org.apache.commons.io.IOUtils.copy()}.
     */
    @Deprecated
    public static void copy( @Nonnull final Reader input, @Nonnull final Writer output )
        throws IOException
    {
        copy( input, output, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
     *
     * @param input the reader to read from
     * @param output the writer to write to
     * @param bufferSize size of internal buffer
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.copy()}.
     */
    @Deprecated
    public static void copy( @Nonnull final Reader input, @Nonnull final Writer output, final int bufferSize )
        throws IOException
    {
        final char[] buffer = new char[bufferSize];
        int n;
        while ( -1 != ( n = input.read( buffer ) ) )
        {
            output.write( buffer, 0, n );
        }
        output.flush();
    }

    ///////////////////////////////////////////////////////////////
    // Derived copy methods
    // InputStream -> *
    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    // InputStream -> Writer

    /**
     * Copy and convert bytes from an <code>InputStream</code> to chars on a
     * <code>Writer</code>.
     * 
     * The platform's default encoding is used for the byte-to-char conversion.
     * 
     * @param input the reader to read from
     * @param output the writer to write to
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.copy()}.
     */
    @Deprecated
    public static void copy( @Nonnull final InputStream input, @Nonnull final Writer output )
        throws IOException
    {
        copy( input, output, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Copy and convert bytes from an <code>InputStream</code> to chars on a
     * <code>Writer</code>.
     * The platform's default encoding is used for the byte-to-char conversion.
     *
     * @param input the input stream to read from
     * @param output the writer to write to
     * @param bufferSize size of internal buffer
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.copy()}.
     */
    @Deprecated
    public static void copy( @Nonnull final InputStream input, @Nonnull final Writer output, final int bufferSize )
        throws IOException
    {
        final InputStreamReader in = new InputStreamReader( input );
        copy( in, output, bufferSize );
    }

    /**
     * Copy and convert bytes from an <code>InputStream</code> to chars on a
     * <code>Writer</code>, using the specified encoding.
     *
     * @param input the input stream to read from
     * @param output the writer to write to
     * @param encoding the name of a supported character encoding. See the
     *                 <a href="https://www.iana.org/assignments/character-sets">IANA
     *                 Charset Registry</a> for a list of valid encoding types.
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.copy()}.
     */
    @Deprecated
    public static void copy( @Nonnull final InputStream input, @Nonnull final Writer output,
                             @Nonnull final String encoding )
        throws IOException
    {
        final InputStreamReader in = new InputStreamReader( input, encoding );
        copy( in, output );
    }

    /**
     * Copy and convert bytes from an <code>InputStream</code> to chars on a
     * <code>Writer</code>, using the specified encoding.
     *
     * @param encoding   the name of a supported character encoding. See the
     *                   <a href="https://www.iana.org/assignments/character-sets">IANA
     *                   Charset Registry</a> for a list of valid encoding types.
     * @param input the input stream to read from
     * @param output the writer to write to
     * @param bufferSize size of internal buffer
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.copy()}.
     */
    @Deprecated
    public static void copy( @Nonnull final InputStream input, @Nonnull final Writer output,
                             @Nonnull final String encoding, final int bufferSize )
        throws IOException
    {
        final InputStreamReader in = new InputStreamReader( input, encoding );
        copy( in, output, bufferSize );
    }

    ///////////////////////////////////////////////////////////////
    // InputStream -> String

    /**
     * Get the contents of an <code>InputStream</code> as a String.
     * The platform's default encoding is used for the byte-to-char conversion.
     * 
     * @param input the InputStream to read from
     * @return the resulting string
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    @Nonnull public static String toString( @Nonnull final InputStream input )
        throws IOException
    {
        return toString( input, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Get the contents of an <code>InputStream</code> as a String.
     * The platform's default encoding is used for the byte-to-char conversion.
     *
     * @param input the InputStream to read from
     * @param bufferSize size of internal buffer
     * @return the resulting string
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    @Nonnull public static String toString( @Nonnull final InputStream input, final int bufferSize )
        throws IOException
    {
        final StringWriter sw = new StringWriter();
        copy( input, sw, bufferSize );
        return sw.toString();
    }

    /**
     * Get the contents of an <code>InputStream</code> as a String.
     *
     * @param input the InputStream to read from
     * @param encoding the name of a supported character encoding. See the
     *                 <a href="https://www.iana.org/assignments/character-sets">IANA
     *                 Charset Registry</a> for a list of valid encoding types.
     * @return the converted string
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.toString()}.
     */
    @Deprecated
    @Nonnull public static String toString( @Nonnull final InputStream input, @Nonnull final String encoding )
        throws IOException
    {
        return toString( input, encoding, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Get the contents of an <code>InputStream</code> as a String.
     *
     * @param input the InputStream to read from
     * @param encoding   the name of a supported character encoding. See the
     *                   <a href="https://www.iana.org/assignments/character-sets">IANA
     *                   Charset Registry</a> for a list of valid encoding types.
     * @param bufferSize size of internal buffer
     * @return The converted string.
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.toString()}.
     */
    @Deprecated
    @Nonnull public static String toString( @Nonnull final InputStream input, @Nonnull final String encoding,
                                            final int bufferSize )
        throws IOException
    {
        final StringWriter sw = new StringWriter();
        copy( input, sw, encoding, bufferSize );
        return sw.toString();
    }

    ///////////////////////////////////////////////////////////////
    // InputStream -> byte[]

    /**
     * Get the contents of an <code>InputStream</code> as a <code>byte[]</code>.
     * 
     * @param input the InputStream to read from
     * @return the resulting byte array.
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.readFully()}.
     */
    @Deprecated
    @Nonnull public static byte[] toByteArray( @Nonnull final InputStream input )
        throws IOException
    {
        return toByteArray( input, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Get the contents of an <code>InputStream</code> as a <code>byte[]</code>.
     *
     * @param input the InputStream to read from
     * @param bufferSize size of internal buffer
     * @return the resulting byte array.
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.readFully()}.
     */
    @Deprecated
    @Nonnull public static byte[] toByteArray( @Nonnull final InputStream input, final int bufferSize )
        throws IOException
    {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy( input, output, bufferSize );
        return output.toByteArray();
    }

    ///////////////////////////////////////////////////////////////
    // Derived copy methods
    // Reader -> *
    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    // Reader -> OutputStream

    /**
     * Serialize chars from a <code>Reader</code> to bytes on an <code>OutputStream</code>, and
     * flush the <code>OutputStream</code>.
     * 
     * @param input the InputStream to read from
     * @param output the output stream to write to
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    public static void copy( @Nonnull final Reader input, @Nonnull final OutputStream output )
        throws IOException
    {
        copy( input, output, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Serialize chars from a <code>Reader</code> to bytes on an <code>OutputStream</code>, and
     * flush the <code>OutputStream</code>.
     *
     * @param input the InputStream to read from
     * @param output the output to write to
     * @param bufferSize size of internal buffer
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    public static void copy( @Nonnull final Reader input, @Nonnull final OutputStream output, final int bufferSize )
        throws IOException
    {
        final OutputStreamWriter out = new OutputStreamWriter( output );
        copy( input, out, bufferSize );
        // NOTE: Unless anyone is planning on rewriting OutputStreamWriter, we have to flush
        // here.
        out.flush();
    }

    ///////////////////////////////////////////////////////////////
    // Reader -> String

    /**
     * Get the contents of a <code>Reader</code> as a String.
     * @param input the InputStream to read from
     * @return The converted string.
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.toString()}.
     */
    @Deprecated
    @Nonnull public static String toString( @Nonnull final Reader input )
        throws IOException
    {
        return toString( input, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Get the contents of a <code>Reader</code> as a String.
     *
     * @param input the reader to read from
     * @param bufferSize size of internal buffer
     * @return the resulting byte array.
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.toString()}.
     */
    @Deprecated
    @Nonnull public static String toString( @Nonnull final Reader input, final int bufferSize )
        throws IOException
    {
        final StringWriter sw = new StringWriter();
        copy( input, sw, bufferSize );
        return sw.toString();
    }

    ///////////////////////////////////////////////////////////////
    // Reader -> byte[]

    /**
     * Get the contents of a <code>Reader</code> as a <code>byte[]</code>.
     * 
     * @param input the InputStream to read from
     * @return the resulting byte array.
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    @Nonnull public static byte[] toByteArray( @Nonnull final Reader input )
        throws IOException
    {
        return toByteArray( input, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Get the contents of a <code>Reader</code> as a <code>byte[]</code>.
     *
     * @param input the InputStream to read from
     * @param bufferSize size of internal buffer
     * @return the resulting byte array.
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    @Nonnull public static byte[] toByteArray( @Nonnull final Reader input, final int bufferSize )
        throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy( input, output, bufferSize );
        return output.toByteArray();
    }

    ///////////////////////////////////////////////////////////////
    // Derived copy methods
    // String -> *
    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    // String -> OutputStream

    /**
     * Serialize chars from a <code>String</code> to bytes on an <code>OutputStream</code>, and
     * flush the <code>OutputStream</code>.
     * @param input the InputStream to read from
     * @param output the output to write to
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    public static void copy( @Nonnull final String input, @Nonnull final OutputStream output )
        throws IOException
    {
        copy( input, output, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Serialize chars from a <code>String</code> to bytes on an <code>OutputStream</code>, and
     * flush the <code>OutputStream</code>.
     *
     * @param input the InputStream to read from
     * @param output the output to write to
     * @param bufferSize size of internal buffer
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    public static void copy( @Nonnull final String input, @Nonnull final OutputStream output, final int bufferSize )
        throws IOException
    {
        final StringReader in = new StringReader( input );
        final OutputStreamWriter out = new OutputStreamWriter( output );
        copy( in, out, bufferSize );
        // NOTE: Unless anyone is planning on rewriting OutputStreamWriter, we have to flush
        // here.
        out.flush();
    }

    ///////////////////////////////////////////////////////////////
    // String -> Writer

    /**
     * Copy chars from a <code>String</code> to a <code>Writer</code>.
     * 
     * @param input the string to write
     * @param output resulting output {@link Writer}
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.write()}.
     */
    @Deprecated
    public static void copy( @Nonnull final String input, @Nonnull final Writer output )
        throws IOException
    {
        output.write( input );
    }

    ///////////////////////////////////////////////////////////////
    // String -> byte[]

    /**
     * Get the contents of a <code>String</code> as a <code>byte[]</code>.
     * 
     * @param input the String to read from
     * @return the resulting byte array
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    @Nonnull public static byte[] toByteArray( @Nonnull final String input )
        throws IOException
    {
        return toByteArray( input, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Get the contents of a <code>String</code> as a <code>byte[]</code>.
     *
     * @param input the InputStream to read from
     * @param bufferSize size of internal buffer
     * @return the resulting byte array
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    @Nonnull public static byte[] toByteArray( @Nonnull final String input, final int bufferSize )
        throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy( input, output, bufferSize );
        return output.toByteArray();
    }

    ///////////////////////////////////////////////////////////////
    // Derived copy methods
    // byte[] -> *
    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    // byte[] -> Writer

    /**
     * Copy and convert bytes from a <code>byte[]</code> to chars on a
     * <code>Writer</code>.
     * The platform's default encoding is used for the byte-to-char conversion.
     * 
     * @param input the InputStream to read from
     * @param output the output to write to
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    public static void copy( @Nonnull final byte[] input, @Nonnull final Writer output )
        throws IOException
    {
        copy( input, output, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Copy and convert bytes from a <code>byte[]</code> to chars on a
     * <code>Writer</code>.
     * The platform's default encoding is used for the byte-to-char conversion.
     *
     * @param input the InputStream to read from
     * @param output the output to write to
     * @param bufferSize size of internal buffer
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    public static void copy( @Nonnull final byte[] input, @Nonnull final Writer output, final int bufferSize )
        throws IOException
    {
        final ByteArrayInputStream in = new ByteArrayInputStream( input );
        copy( in, output, bufferSize );
    }

    /**
     * Copy and convert bytes from a <code>byte[]</code> to chars on a
     * <code>Writer</code>, using the specified encoding.
     *
     * @param encoding the name of a supported character encoding. See the
     *                 <a href="https://www.iana.org/assignments/character-sets">IANA
     *                 Charset Registry</a> for a list of valid encoding types.
     * @param input the data to write
     * @param output the writer to write to
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.write()}.
     */
    @Deprecated
    public static void copy( @Nonnull final byte[] input, @Nonnull final Writer output, final String encoding )
        throws IOException
    {
        final ByteArrayInputStream in = new ByteArrayInputStream( input );
        copy( in, output, encoding );
    }

    /**
     * Copy and convert bytes from a <code>byte[]</code> to chars on a
     * <code>Writer</code>, using the specified encoding.
     *
     * @param encoding   the name of a supported character encoding. See the
     *                   <a href="https://www.iana.org/assignments/character-sets">IANA
     *                   Charset Registry</a> for a list of valid encoding types.
     * @param input the input bytes
     * @param output The output buffer {@link Writer}
     * @param bufferSize size of internal buffer
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.write()}.
     */
    @Deprecated
    public static void copy( @Nonnull final byte[] input, @Nonnull final Writer output, @Nonnull final String encoding,
                             final int bufferSize )
        throws IOException
    {
        final ByteArrayInputStream in = new ByteArrayInputStream( input );
        copy( in, output, encoding, bufferSize );
    }

    ///////////////////////////////////////////////////////////////
    // byte[] -> String

    /**
     * Get the contents of a <code>byte[]</code> as a String.
     * The platform's default encoding is used for the byte-to-char conversion.
     * @param input the input bytes
     * @return the resulting string
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    @Nonnull public static String toString( @Nonnull final byte[] input )
        throws IOException
    {
        return toString( input, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Get the contents of a <code>byte[]</code> as a String.
     * The platform's default encoding is used for the byte-to-char conversion.
     *
     * @param bufferSize size of internal buffer
     * @param input the input bytes
     * @return the created string
     * @throws IOException in case of failure
     * @deprecated always specify a character encoding
     */
    @Deprecated
    @Nonnull public static String toString( @Nonnull final byte[] input, final int bufferSize )
        throws IOException
    {
        final StringWriter sw = new StringWriter();
        copy( input, sw, bufferSize );
        return sw.toString();
    }

    /**
     * Get the contents of a <code>byte[]</code> as a String.
     *
     * @param encoding the name of a supported character encoding. See the
     *                 <a href="https://www.iana.org/assignments/character-sets">IANA
     *                 Charset Registry</a> for a list of valid encoding types.
     * @param input the input bytes
     * @return the resulting string
     * @throws IOException in case of failure
     * @deprecated use {@code new String(input, encoding)}
     */
    @Deprecated
    @Nonnull public static String toString( @Nonnull final byte[] input, @Nonnull final String encoding )
        throws IOException
    {
        return toString( input, encoding, DEFAULT_BUFFER_SIZE );
    }

    /**
     * Get the contents of a <code>byte[]</code> as a String.
     *
     * @param encoding   the name of a supported character encoding. See the
     *                   <a href="https://www.iana.org/assignments/character-sets">IANA
     *                   Charset Registry</a> for a list of valid encoding types.
     * @param bufferSize size of internal buffer
     * @param input input bytes
     * @return the resulting string
     * @throws IOException in case of failure
     * @deprecated use {@code new String(input, encoding)}
     */
    @Deprecated
    @Nonnull public static String toString( @Nonnull final byte[] input, @Nonnull final String encoding,
                                            final int bufferSize )
        throws IOException
    {
        final StringWriter sw = new StringWriter();
        copy( input, sw, encoding, bufferSize );
        return sw.toString();
    }

    ///////////////////////////////////////////////////////////////
    // byte[] -> OutputStream

    /**
     * Copy bytes from a <code>byte[]</code> to an <code>OutputStream</code>.
     * 
     * @param input Input byte array.
     * @param output output stream {@link OutputStream}
     * @throws IOException in case of failure
     * @deprecated inline this method
     */
    @Deprecated
    public static void copy( @Nonnull final byte[] input, @Nonnull final OutputStream output )
        throws IOException
    {
        output.write( input );
    }

    /**
     * Compare the contents of two streams to determine if they are equal or not.
     *
     * @param input1 the first stream
     * @param input2 the second stream
     * @return true if the content of the streams are equal or they both don't exist, false otherwise
     * @throws IOException in case of failure
     * @deprecated use {@code org.apache.commons.io.IOUtils.contentEquals()}
     */
    @Deprecated
    public static boolean contentEquals( @Nonnull final InputStream input1, @Nonnull final InputStream input2 )
        throws IOException
    {
        final InputStream bufferedInput1 = new BufferedInputStream( input1 );
        final InputStream bufferedInput2 = new BufferedInputStream( input2 );

        int ch = bufferedInput1.read();
        while ( -1 != ch )
        {
            final int ch2 = bufferedInput2.read();
            if ( ch != ch2 )
            {
                return false;
            }
            ch = bufferedInput1.read();
        }

        final int ch2 = bufferedInput2.read();
        return -1 == ch2;
    }

    // ----------------------------------------------------------------------
    // closeXXX()
    // ----------------------------------------------------------------------

    /**
     * <p>Closes a {@code Channel} suppressing any {@code IOException}.</p>
     * <p>
     * <b>Note:</b> The use case justifying this method is a shortcoming of the Java language up to but not including
     * Java 7. For any code targeting Java 7 or later use of this method is highly discouraged and the
     * {@code try-with-resources} statement should be used instead. Care must be taken to not use this method in a way
     * {@code IOException}s get suppressed incorrectly.
     * <strong>You must close all resources in use inside the {@code try} block to not suppress exceptions in the
     * {@code finally} block incorrectly by using this method.</strong>
     * </p>
     * <p>
     * <b>Example:</b>
     * </p>
     * <pre>
     * // Introduce variables for the resources and initialize them to null. This cannot throw an exception.
     * Closeable resource1 = null;
     * Closeable resource2 = null;
     * try
     * {
     *     // Obtain a resource object and assign it to variable resource1. This may throw an exception.
     *     // If successful, resource1 != null.
     *     resource1 = ...
     *
     *     // Obtain a resource object and assign it to variable resource2. This may throw an exception.
     *     // If successful, resource2 != null. Not reached if an exception has been thrown above.
     *     resource2 = ...
     *
     *     // Perform operations on the resources. This may throw an exception. Not reached if an exception has been
     *     // thrown above. Note: Treat the variables resource1 and resource2 the same way as if they would have been
     *     // declared with the final modifier - that is - do NOT write anyting like resource1 = something else or
     *     // resource2 = something else here.
     *     resource1 ...
     *     resource2 ...
     *
     *     // Finally, close the resources and set the variables to null indicating successful completion.
     *     // This may throw an exception. Not reached if an exception has been thrown above.
     *     resource1.close();
     *     resource1 = null;
     *     // Not reached if an exception has been thrown above.
     *     resource2.close();
     *     resource2 = null;
     *
     *     // All resources are closed at this point and all operations (up to here) completed successfully without
     *     // throwing an exception we would need to handle (by letting it propagate or by catching and handling it).
     * }
     * finally
     * {
     *     // Cleanup any resource not closed in the try block due to an exception having been thrown and suppress any
     *     // exception this may produce to not stop the exception from the try block to be propagated. If the try
     *     // block completed successfully, all variables will have been set to null there and this will not do
     *     // anything. This is just to cleanup properly in case of an exception.
     *
     *     IOUtil.close( resource1 );
     *     IOUtil.close( resource2 );
     *
     *     // Without that utility method you would need to write the following:
     *     //
     *     // try
     *     // {
     *     //     if ( resource1 != null )
     *     //     {
     *     //         resource1.close();
     *     //     }
     *     // }
     *     // catch( IOException e )
     *     // {
     *     //     Suppressed. If resource1 != null, an exception has already been thrown in the try block we need to
     *     //     propagate instead of this one.
     *     // }
     *     // finally
     *     // {
     *     //     try
     *     //     {
     *     //         if ( resource2 != null )
     *     //         {
     *     //             resource2.close();
     *     //         }
     *     //     }
     *     //     catch ( IOException e )
     *     //     {
     *     //         Suppressed. If resource2 != null, an exception has already been thrown in the try block we need to
     *     //         propagate instead of this one.
     *     //     }
     *     // }
     * }
     * </pre>
     *
     * @param channel The channel to close or {@code null}.
     * @deprecated use try-with-resources
     */
    @Deprecated
    public static void close( @Nullable Channel channel )
    {
        try
        {
            if ( channel != null )
            {
                channel.close();
            }
        }
        catch ( IOException ex )
        {
            // Suppressed
        }
    }

    /**
     * <p>Closes an {@code InputStream} suppressing any {@code IOException}.</p>
     * <p>
     * <b>Note:</b> The use case justifying this method is a shortcoming of the Java language up to but not including
     * Java 7. For any code targeting Java 7 or later use of this method is highly discouraged and the
     * {@code try-with-resources} statement should be used instead. Care must be taken to not use this method in a way
     * {@code IOException}s get suppressed incorrectly.
     * <strong>You must close all resources in use inside the {@code try} block to not suppress exceptions in the
     * {@code finally} block incorrectly by using this method.</strong>
     * </p>
     * <p>
     * <b>Example:</b>
     * </p>
     * <pre>
     * // Introduce variables for the resources and initialize them to null. This cannot throw an exception.
     * Closeable resource1 = null;
     * Closeable resource2 = null;
     * try
     * {
     *     // Obtain a resource object and assign it to variable resource1. This may throw an exception.
     *     // If successful, resource1 != null.
     *     resource1 = ...
     *
     *     // Obtain a resource object and assign it to variable resource2. This may throw an exception.
     *     // If successful, resource2 != null. Not reached if an exception has been thrown above.
     *     resource2 = ...
     *
     *     // Perform operations on the resources. This may throw an exception. Not reached if an exception has been
     *     // thrown above. Note: Treat the variables resource1 and resource2 the same way as if they would have been
     *     // declared with the final modifier - that is - do NOT write anyting like resource1 = something else or
     *     // resource2 = something else here.
     *     resource1 ...
     *     resource2 ...
     *
     *     // Finally, close the resources and set the variables to null indicating successful completion.
     *     // This may throw an exception. Not reached if an exception has been thrown above.
     *     resource1.close();
     *     resource1 = null;
     *     // This may throw an exception. Not reached if an exception has been thrown above.
     *     resource2.close();
     *     resource2 = null;
     *
     *     // All resources are closed at this point and all operations (up to here) completed successfully without
     *     // throwing an exception we would need to handle (by letting it propagate or by catching and handling it).
     * }
     * finally
     * {
     *     // Cleanup any resource not closed in the try block due to an exception having been thrown and suppress any
     *     // exception this may produce to not stop the exception from the try block to be propagated. If the try
     *     // block completed successfully, all variables will have been set to null there and this will not do
     *     // anything. This is just to cleanup properly in case of an exception.
     *
     *     IOUtil.close( resource1 );
     *     IOUtil.close( resource2 );
     *
     *     // Without that utility method you would need to write the following:
     *     //
     *     // try
     *     // {
     *     //     if ( resource1 != null )
     *     //     {
     *     //         resource1.close();
     *     //     }
     *     // }
     *     // catch( IOException e )
     *     // {
     *     //     Suppressed. If resource1 != null, an exception has already been thrown in the try block we need to
     *     //     propagate instead of this one.
     *     // }
     *     // finally
     *     // {
     *     //     try
     *     //     {
     *     //         if ( resource2 != null )
     *     //         {
     *     //             resource2.close();
     *     //         }
     *     //     }
     *     //     catch ( IOException e )
     *     //     {
     *     //         Suppressed. If resource2 != null, an exception has already been thrown in the try block we need to
     *     //         propagate instead of this one.
     *     //     }
     *     // }
     * }
     * </pre>
     *
     * @param inputStream The stream to close or {@code null}.
     * @deprecated use try-with-resources
     */
    @Deprecated
    public static void close( @Nullable InputStream inputStream )
    {
        try
        {
            if ( inputStream != null )
            {
                inputStream.close();
            }
        }
        catch ( IOException ex )
        {
            // Suppressed
        }
    }

    /**
     * <p>Closes an {@code OutputStream} suppressing any {@code IOException}.</p>
     * <p>
     * <b>Note:</b> The use case justifying this method is a shortcoming of the Java language up to but not including
     * Java 7. For any code targeting Java 7 or later use of this method is highly discouraged and the
     * {@code try-with-resources} statement should be used instead. Care must be taken to not use this method in a way
     * {@code IOException}s get suppressed incorrectly.
     * <strong>You must close all resources in use inside the {@code try} block to not suppress exceptions in the
     * {@code finally} block incorrectly by using this method.</strong>
     * </p>
     * <p>
     * <b>Example:</b>
     * </p>
     * <pre>
     * // Introduce variables for the resources and initialize them to null. This cannot throw an exception.
     * Closeable resource1 = null;
     * Closeable resource2 = null;
     * try
     * {
     *     // Obtain a resource object and assign it to variable resource1. This may throw an exception.
     *     // If successful, resource1 != null.
     *     resource1 = ...
     *
     *     // Obtain a resource object and assign it to variable resource2. This may throw an exception.
     *     // If successful, resource2 != null. Not reached if an exception has been thrown above.
     *     resource2 = ...
     *
     *     // Perform operations on the resources. This may throw an exception. Not reached if an exception has been
     *     // thrown above. Note: Treat the variables resource1 and resource2 the same way as if they would have been
     *     // declared with the final modifier - that is - do NOT write anyting like resource1 = something else or
     *     // resource2 = something else here.
     *     resource1 ...
     *     resource2 ...
     *
     *     // Finally, close the resources and set the variables to null indicating successful completion.
     *     // This may throw an exception. Not reached if an exception has been thrown above.
     *     resource1.close();
     *     resource1 = null;
     *     // This may throw an exception. Not reached if an exception has been thrown above.
     *     resource2.close();
     *     resource2 = null;
     *
     *     // All resources are closed at this point and all operations (up to here) completed successfully without
     *     // throwing an exception we would need to handle (by letting it propagate or by catching and handling it).
     * }
     * finally
     * {
     *     // Cleanup any resource not closed in the try block due to an exception having been thrown and suppress any
     *     // exception this may produce to not stop the exception from the try block to be propagated. If the try
     *     // block completed successfully, all variables will have been set to null there and this will not do
     *     // anything. This is just to cleanup properly in case of an exception.
     *
     *     IOUtil.close( resource1 );
     *     IOUtil.close( resource2 );
     *
     *     // Without that utility method you would need to write the following:
     *     //
     *     // try
     *     // {
     *     //     if ( resource1 != null )
     *     //     {
     *     //         resource1.close();
     *     //     }
     *     // }
     *     // catch( IOException e )
     *     // {
     *     //     Suppressed. If resource1 != null, an exception has already been thrown in the try block we need to
     *     //     propagate instead of this one.
     *     // }
     *     // finally
     *     // {
     *     //     try
     *     //     {
     *     //         if ( resource2 != null )
     *     //         {
     *     //             resource2.close();
     *     //         }
     *     //     }
     *     //     catch ( IOException e )
     *     //     {
     *     //         Suppressed. If resource2 != null, an exception has already been thrown in the try block we need to
     *     //         propagate instead of this one.
     *     //     }
     *     // }
     * }
     * </pre>
     *
     * @param outputStream The stream to close or {@code null}.
     * @deprecated use try-with-resources
     */
    @Deprecated
    public static void close( @Nullable OutputStream outputStream )
    {
        try
        {
            if ( outputStream != null )
            {
                outputStream.close();
            }
        }
        catch ( IOException ex )
        {
            // Suppressed
        }
    }

    /**
     * <p>Closes a {@code Reader} suppressing any {@code IOException}.</p>
     * <p>
     * <b>Note:</b> The use case justifying this method is a shortcoming of the Java language up to but not including
     * Java 7. For any code targeting Java 7 or later use of this method is highly discouraged and the
     * {@code try-with-resources} statement should be used instead. Care must be taken to not use this method in a way
     * {@code IOException}s get suppressed incorrectly.
     * <strong>You must close all resources in use inside the {@code try} block to not suppress exceptions in the
     * {@code finally} block incorrectly by using this method.</strong>
     * </p>
     * <p>
     * <b>Example:</b>
     * </p>
     * <pre>
     * // Introduce variables for the resources and initialize them to null. This cannot throw an exception.
     * Closeable resource1 = null;
     * Closeable resource2 = null;
     * try
     * {
     *     // Obtain a resource object and assign it to variable resource1. This may throw an exception.
     *     // If successful, resource1 != null.
     *     resource1 = ...
     *
     *     // Obtain a resource object and assign it to variable resource2. This may throw an exception.
     *     // If successful, resource2 != null. Not reached if an exception has been thrown above.
     *     resource2 = ...
     *
     *     // Perform operations on the resources. This may throw an exception. Not reached if an exception has been
     *     // thrown above. Note: Treat the variables resource1 and resource2 the same way as if they would have been
     *     // declared with the final modifier - that is - do NOT write anyting like resource1 = something else or
     *     // resource2 = something else here.
     *     resource1 ...
     *     resource2 ...
     *
     *     // Finally, close the resources and set the variables to null indicating successful completion.
     *     // This may throw an exception. Not reached if an exception has been thrown above.
     *     resource1.close();
     *     resource1 = null;
     *     // This may throw an exception. Not reached if an exception has been thrown above.
     *     resource2.close();
     *     resource2 = null;
     *
     *     // All resources are closed at this point and all operations (up to here) completed successfully without
     *     // throwing an exception we would need to handle (by letting it propagate or by catching and handling it).
     * }
     * finally
     * {
     *     // Cleanup any resource not closed in the try block due to an exception having been thrown and suppress any
     *     // exception this may produce to not stop the exception from the try block to be propagated. If the try
     *     // block completed successfully, all variables will have been set to null there and this will not do
     *     // anything. This is just to cleanup properly in case of an exception.
     *
     *     IOUtil.close( resource1 );
     *     IOUtil.close( resource2 );
     *
     *     // Without that utility method you would need to write the following:
     *     //
     *     // try
     *     // {
     *     //     if ( resource1 != null )
     *     //     {
     *     //         resource1.close();
     *     //     }
     *     // }
     *     // catch( IOException e )
     *     // {
     *     //     Suppressed. If resource1 != null, an exception has already been thrown in the try block we need to
     *     //     propagate instead of this one.
     *     // }
     *     // finally
     *     // {
     *     //     try
     *     //     {
     *     //         if ( resource2 != null )
     *     //         {
     *     //             resource2.close();
     *     //         }
     *     //     }
     *     //     catch ( IOException e )
     *     //     {
     *     //         Suppressed. If resource2 != null, an exception has already been thrown in the try block we need to
     *     //         propagate instead of this one.
     *     //     }
     *     // }
     * }
     * </pre>
     *
     * @param reader The reader to close or {@code null}.
     * @deprecated use try-with-resources
     */
    @Deprecated
    public static void close( @Nullable Reader reader )
    {
        try
        {
            if ( reader != null )
            {
                reader.close();
            }
        }
        catch ( IOException ex )
        {
            // Suppressed
        }
    }

    /**
     * <p>Closes a {@code Writer} suppressing any {@code IOException}.</p>
     * <p>
     * <b>Note:</b> The use case justifying this method is a shortcoming of the Java language up to but not including
     * Java 7. For any code targeting Java 7 or later use of this method is highly discouraged and the
     * {@code try-with-resources} statement should be used instead. Care must be taken to not use this method in a way
     * {@code IOException}s get suppressed incorrectly.
     * <strong>You must close all resources in use inside the {@code try} block to not suppress exceptions in the
     * {@code finally} block incorrectly by using this method.</strong>
     * </p>
     * <p>
     * <b>Example:</b>
     * </p>
     * <pre>
     * // Introduce variables for the resources and initialize them to null. This cannot throw an exception.
     * Closeable resource1 = null;
     * Closeable resource2 = null;
     * try
     * {
     *     // Obtain a resource object and assign it to variable resource1. This may throw an exception.
     *     // If successful, resource1 != null.
     *     resource1 = ...
     *
     *     // Obtain a resource object and assign it to variable resource2. This may throw an exception.
     *     // If successful, resource2 != null. Not reached if an exception has been thrown above.
     *     resource2 = ...
     *
     *     // Perform operations on the resources. This may throw an exception. Not reached if an exception has been
     *     // thrown above. Note: Treat the variables resource1 and resource2 the same way as if they would have been
     *     // declared with the final modifier - that is - do NOT write anyting like resource1 = something else or
     *     // resource2 = something else here.
     *     resource1 ...
     *     resource2 ...
     *
     *     // Finally, close the resources and set the variables to null indicating successful completion.
     *     // This may throw an exception. Not reached if an exception has been thrown above.
     *     resource1.close();
     *     resource1 = null;
     *     // This may throw an exception. Not reached if an exception has been thrown above.
     *     resource2.close();
     *     resource2 = null;
     *
     *     // All resources are closed at this point and all operations (up to here) completed successfully without
     *     // throwing an exception we would need to handle (by letting it propagate or by catching and handling it).
     * }
     * finally
     * {
     *     // Cleanup any resource not closed in the try block due to an exception having been thrown and suppress any
     *     // exception this may produce to not stop the exception from the try block to be propagated. If the try
     *     // block completed successfully, all variables will have been set to null there and this will not do
     *     // anything. This is just to cleanup properly in case of an exception.
     *
     *     IOUtil.close( resource1 );
     *     IOUtil.close( resource2 );
     *
     *     // Without that utility method you would need to write the following:
     *     //
     *     // try
     *     // {
     *     //     if ( resource1 != null )
     *     //     {
     *     //         resource1.close();
     *     //     }
     *     // }
     *     // catch( IOException e )
     *     // {
     *     //     Suppressed. If resource1 != null, an exception has already been thrown in the try block we need to
     *     //     propagate instead of this one.
     *     // }
     *     // finally
     *     // {
     *     //     try
     *     //     {
     *     //         if ( resource2 != null )
     *     //         {
     *     //             resource2.close();
     *     //         }
     *     //     }
     *     //     catch ( IOException e )
     *     //     {
     *     //         Suppressed. If resource2 != null, an exception has already been thrown in the try block we need to
     *     //         propagate instead of this one.
     *     //     }
     *     // }
     * }
     * </pre>
     *
     * @param writer The writer to close or {@code null}.
     * @deprecated use try-with-resources
     */
    @Deprecated
    public static void close( @Nullable Writer writer )
    {
        try
        {
            if ( writer != null )
            {
                writer.close();
            }
        }
        catch ( IOException ex )
        {
            // Suppressed
        }
    }
}
