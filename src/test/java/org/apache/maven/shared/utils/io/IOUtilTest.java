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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IOUtilTest
{

    private static final long INFINITE_LOOP_TIMEOUT = 500;

    @Test
    public void closeReaderWithNull()
        throws Exception
    {
        IOUtil.close( (Reader) null );
    }

    @Test
    public void closeWriterWithNull()
        throws Exception
    {
        IOUtil.close( (Writer) null );
    }

    @Test
    public void closeInputStreamWithNull()
        throws Exception
    {
        IOUtil.close( nullInputStream() );
    }

    @Test
    public void closeOutputStreamWithNull()
        throws Exception
    {
        IOUtil.close( nullOutputStream() );
    }

    @Test
    public void closeReaderWithIOE()
        throws Exception
    {
        IOUtil.close( new BufferedReader( new StringReader( emptyString() ) )
        {
            @Override
            public void close()
                throws IOException
            {
                super.close();
                throw new IOException( "don't bomb out" );
            }
        } );
    }

    @Test
    public void closeWriterWithIOE()
        throws Exception
    {
        IOUtil.close( new BufferedWriter( new StringWriter() )
        {
            @Override
            public void close()
                throws IOException
            {
                super.close();
                throw new IOException( "don't bomb out" );
            }
        } );
    }

    @Test
    public void closeInputStreamWithIOE()
        throws Exception
    {
        IOUtil.close( new BufferedInputStream( emptyInputStream() )
        {
            @Override
            public void close()
                throws IOException
            {
                super.close();
                throw new IOException( "don't bomb out" );
            }
        } );
    }

    @Test
    public void closeOutputStreamWithIOE()
        throws Exception
    {
        IOUtil.close( new BufferedOutputStream( new ByteArrayOutputStream() )
        {
            @Override
            public void close()
                throws IOException
            {
                super.close();
                throw new IOException( "don't bomb out" );
            }
        } );
    }

    @Test
    public void closeReaderCloses()
        throws Exception
    {
        final AtomicBoolean closed = new AtomicBoolean( false );
        IOUtil.close( new BufferedReader( new StringReader( emptyString() ) )
        {
            @Override
            public void close()
                throws IOException
            {
                closed.set( true );
                super.close();
            }
        } );
        assertThat( closed.get(), is( true ) );
    }

    @Test
    public void closeWriterCloses()
        throws Exception
    {
        final AtomicBoolean closed = new AtomicBoolean( false );
        IOUtil.close( new BufferedWriter( new StringWriter() )
        {
            @Override
            public void close()
                throws IOException
            {
                closed.set( true );
                super.close();
            }
        } );
        assertThat( closed.get(), is( true ) );
    }

    @Test
    public void closeInputStreamCloses()
        throws Exception
    {
        final AtomicBoolean closed = new AtomicBoolean( false );
        IOUtil.close( new BufferedInputStream( emptyInputStream() )
        {
            @Override
            public void close()
                throws IOException
            {
                closed.set( true );
                super.close();
            }
        } );
        assertThat( closed.get(), is( true ) );
    }

    @Test
    public void closeOutputStreamCloses()
        throws Exception
    {
        final AtomicBoolean closed = new AtomicBoolean( false );
        IOUtil.close( new BufferedOutputStream( new ByteArrayOutputStream() )
        {
            @Override
            public void close()
                throws IOException
            {
                closed.set( true );
                super.close();
            }
        } );
        assertThat( closed.get(), is( true ) );
    }

    @Test
    public void toByteArrayFromString()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toByteArray( probe ), is( probe.getBytes() ) );
    }

    @Test
    public void toByteArrayFromReader()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toByteArray( new StringReader( probe ) ), is( probe.getBytes() ) );
    }

    @Test
    public void toByteArrayFromInputStream()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toByteArray( new DontCloseByteArrayInputStream( IOUtil.toByteArray( probe ) ) ),
                    is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toByteArrayNullString()
        throws Exception
    {
        IOUtil.toByteArray( (String) null );
    }

    @Test( expected = NullPointerException.class )
    public void toByteArrayNullReader()
        throws Exception
    {
        IOUtil.toByteArray( (Reader) null );
    }

    @Test( expected = NullPointerException.class )
    public void toByteArrayNullInputStream()
        throws Exception
    {
        IOUtil.toByteArray( nullInputStream() );
    }

    @Test( expected = IOException.class )
    public void contentEqualNullNull()
        throws Exception
    {
        IOUtil.contentEquals( null, null );
    }

    @Test( expected = IOException.class )
    public void contentEqualNonNullNull()
        throws Exception
    {
        IOUtil.contentEquals( new DontCloseByteArrayInputStream( emptyByteArray() ), null );
    }

    @Test( expected = IOException.class )
    public void contentEqualNullNonNull()
        throws Exception
    {
        IOUtil.contentEquals( new DontCloseByteArrayInputStream( emptyByteArray() ), null );
    }

    @Test
    public void contentEqualEmptyEmpty()
        throws Exception
    {
        assertThat( IOUtil.contentEquals( new DontCloseByteArrayInputStream( emptyByteArray() ),
                                          new DontCloseByteArrayInputStream( emptyByteArray() ) ), is( true ) );
    }

    @Test
    public void contentEqualNonEmptyEmpty()
        throws Exception
    {
        assertThat( IOUtil.contentEquals( new DontCloseByteArrayInputStream( new byte[1] ),
                                          new DontCloseByteArrayInputStream( emptyByteArray() ) ), is( false ) );
    }

    @Test
    public void contentEqualEmptyNonEmpty()
        throws Exception
    {
        assertThat( IOUtil.contentEquals( new DontCloseByteArrayInputStream( emptyByteArray() ),
                                          new DontCloseByteArrayInputStream( new byte[1] ) ), is( false ) );
    }

    @Test
    public void contentEqualNonEmptyNonEmpty()
        throws Exception
    {
        assertThat( IOUtil.contentEquals( new DontCloseByteArrayInputStream( new byte[1] ),
                                          new DontCloseByteArrayInputStream( new byte[1] ) ), is( true ) );
    }

    @Test
    public void contentEqualMostlySame()
        throws Exception
    {
        assertThat( IOUtil.contentEquals( new DontCloseByteArrayInputStream( new byte[]{ 1, 2, 3, 4, 5, 6 } ),
                                          new DontCloseByteArrayInputStream( new byte[]{ 1, 2, 3, 4, 5, 7 } ) ),
                    is( false ) );
    }

    @Test
    public void contentEqualLargeSame()
        throws Exception
    {
        assertThat( IOUtil.contentEquals( new DontCloseByteArrayInputStream( new byte[8192] ),
                                          new DontCloseByteArrayInputStream( new byte[8192] ) ), is( true ) );
    }

    @Test
    public void contentEqualLargeDifferent()
        throws Exception
    {
        byte[] buf = new byte[8192];
        buf[8191] = 1;
        assertThat( IOUtil.contentEquals( new DontCloseByteArrayInputStream( new byte[8192] ),
                                          new DontCloseByteArrayInputStream( buf ) ), is( false ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullByteArray()
        throws Exception
    {
        IOUtil.toString( nullByteArray() );
    }

    @Test
    public void toStringEmptyByteArray()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyByteArray() ), is( emptyString() ) );
    }

    @Test
    public void toStringByteArray()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( probe.getBytes() ).getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullByteArrayNegBufSz()
        throws Exception
    {
        IOUtil.toString( nullByteArray(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void toStringEmptyByteArrayNegBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyByteArray(), -1 ), is( emptyString() ) );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void toStringByteArrayNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( probe.getBytes(), -1 ), is( probe ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringNullByteArrayZeroBufSz()
        throws Exception
    {
        IOUtil.toString( nullByteArray(), 0 );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullByteArrayPosBufSz()
        throws Exception
    {
        IOUtil.toString( nullByteArray(), 1 );
    }

    @Test
    public void toStringEmptyByteArrayPosBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyByteArray(), 1 ), is( emptyString() ) );
    }

    @Test
    public void toStringByteArrayPosBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( probe.getBytes(), 1 ).getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullByteArrayNullEncoding()
        throws Exception
    {
        IOUtil.toString( nullByteArray(), null );
    }

    @Test( expected = NullPointerException.class )
    public void toStringEmptyByteArrayNullEncoding()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyByteArray(), null ), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringByteArrayNullEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( probe.getBytes(), null ).getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullByteArrayJunkEncoding()
        throws Exception
    {
        IOUtil.toString( nullByteArray(), "junk" );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void toStringEmptyByteArrayJunkEncoding()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyByteArray(), "junk" ), is( emptyString() ) );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void toStringByteArrayJunkEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( probe.getBytes(), "junk" ).getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullByteArrayValidEncoding()
        throws Exception
    {
        IOUtil.toString( nullByteArray(), "utf-16" );
    }

    @Test
    public void toStringEmptyByteArrayValidEncoding()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyByteArray(), "utf-16" ), is( emptyString() ) );
    }

    @Test
    public void toStringByteArrayValidEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( probe.getBytes( "utf-16" ), "utf-16" ).getBytes( "utf-8" ),
                    is( probe.getBytes( "utf-8" ) ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullByteArrayNullEncodingNegBufSz()
        throws Exception
    {
        IOUtil.toString( nullByteArray(), null, -1 );
    }

    @Test( expected = NullPointerException.class )
    public void toStringEmptyByteArrayNullEncodingNegBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyByteArray(), null, -1 ), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringByteArrayNullEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( probe.getBytes(), null, -1 ).getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullByteArrayJunkEncodingNegBufSz()
        throws Exception
    {
        IOUtil.toString( nullByteArray(), "junk", -1 );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void toStringEmptyByteArrayJunkEncodingNegBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyByteArray(), "junk", -1 ), is( emptyString() ) );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void toStringByteArrayJunkEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( probe.getBytes(), "junk", -1 ).getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullByteArrayValidEncodingNegBufSz()
        throws Exception
    {
        IOUtil.toString( nullByteArray(), "utf-16", -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void toStringEmptyByteArrayValidEncodingNegBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyByteArray(), "utf-16", -1 ), is( emptyString() ) );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void toStringByteArrayValidEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( probe.getBytes( "utf-16" ), "utf-16", -1 ).getBytes( "utf-8" ),
                    is( probe.getBytes( "utf-8" ) ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringNullByteArrayNullEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.toString( nullByteArray(), null, 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringEmptyByteArrayNullEncodingZeroBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyByteArray(), null, 0 ), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringByteArrayNullEncodingZeroBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( probe.getBytes(), null, 0 ).getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringNullByteArrayJunkEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.toString( nullByteArray(), "junk", 0 );
    }

    @Test( expected = UnsupportedEncodingException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringEmptyByteArrayJunkEncodingZeroBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyByteArray(), "junk", 0 ), is( emptyString() ) );
    }

    @Test( expected = UnsupportedEncodingException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringByteArrayJunkEncodingZeroBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( probe.getBytes(), "junk", 0 ).getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringNullByteArrayValidEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.toString( nullByteArray(), "utf-16", 0 );
    }

    /*
     * copy(byte[],OutputStream)
     */

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayNullOutputStream()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullOutputStream() );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayValidOutputStream()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseByteArrayOutputStream() );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyByteArrayNullOutputStream()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullOutputStream() );
    }

    @Test
    public void copyEmptyByteArrayValidOutputStream()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), new DontCloseByteArrayOutputStream() );
    }

    @Test
    public void copyByteArrayValidOutputStream()
        throws Exception
    {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = { 1, 2, 3, 4, 5, 6 };
        IOUtil.copy( input, outputStream );
        assertThat( outputStream.toByteArray(), is( input ) );
    }

    /*
     * copy(byte[],OutputStream,int)
     */

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayNullOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullOutputStream());
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayValidOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyByteArrayNullOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullOutputStream());
    }

    @Test
    public void copyEmptyByteArrayValidOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test
    public void copyByteArrayValidOutputStreamNegBufSz()
        throws Exception
    {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = { 1, 2, 3, 4, 5, 6 };
        IOUtil.copy( input, outputStream);
        assertThat( outputStream.toByteArray(), is( input ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullByteArrayNullOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullOutputStream());
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullByteArrayValidOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyByteArrayNullOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullOutputStream());
    }

    @Test( timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyByteArrayValidOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test( timeout = INFINITE_LOOP_TIMEOUT )
    public void copyByteArrayValidOutputStreamZeroBufSz()
        throws Exception
    {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = { 1, 2, 3, 4, 5, 6 };
        IOUtil.copy( input, outputStream);
        assertThat( outputStream.toByteArray(), is( input ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullByteArrayNullOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullOutputStream());
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullByteArrayValidOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyByteArrayNullOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullOutputStream());
    }

    @Test( timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyByteArrayValidOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test( timeout = INFINITE_LOOP_TIMEOUT )
    public void copyByteArrayValidOutputStreamPosBufSz()
        throws Exception
    {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = { 1, 2, 3, 4, 5, 6 };
        IOUtil.copy( input, outputStream);
        assertThat( outputStream.toByteArray(), is( input ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamNullOutputStream()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullOutputStream() );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamValidOutputStream()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new DontCloseByteArrayOutputStream() );
    }

    @Test
    public void copyEmptyInputStreamNullOutputStream()
        throws Exception
    {
        IOUtil.copy( new DontCloseByteArrayInputStream( emptyByteArray() ), nullOutputStream() );
    }

    @Test
    public void copyEmptyInputStreamValidOutputStream()
        throws Exception
    {
        IOUtil.copy( new DontCloseByteArrayInputStream( emptyByteArray() ), new DontCloseByteArrayOutputStream() );
    }

    @Test
    public void copyInputStreamValidOutputStream()
        throws Exception
    {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = { 1, 2, 3, 4, 5, 6 };
        IOUtil.copy( new DontCloseByteArrayInputStream( input ), outputStream );
        assertThat( outputStream.toByteArray(), is( input ) );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyNullInputStreamNullOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullOutputStream(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyNullInputStreamValidOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new DontCloseByteArrayOutputStream(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyEmptyInputStreamNullOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( new DontCloseByteArrayInputStream( emptyByteArray() ), nullOutputStream(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyEmptyInputStreamValidOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( new DontCloseByteArrayInputStream( emptyByteArray() ), new DontCloseByteArrayOutputStream(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyInputStreamValidOutputStreamNegBufSz()
        throws Exception
    {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = { 1, 2, 3, 4, 5, 6 };
        IOUtil.copy( new DontCloseByteArrayInputStream( input ), outputStream, -1 );
        assertThat( outputStream.toByteArray(), is( input ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullInputStreamNullOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullOutputStream(), 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullInputStreamValidOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new ByteArrayOutputStream(), 0 );
    }

    @Test( timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyInputStreamNullOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( new DontCloseByteArrayInputStream( emptyByteArray() ), nullOutputStream(), 0 );
    }

    @Test( timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyInputStreamValidOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( new DontCloseByteArrayInputStream( emptyByteArray() ), new DontCloseByteArrayOutputStream(), 0 );
    }
    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullInputStreamNullOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullOutputStream(), 1 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullInputStreamValidOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new ByteArrayOutputStream(), 1 );
    }

    @Test( timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyInputStreamNullOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( new DontCloseByteArrayInputStream( emptyByteArray() ), nullOutputStream(), 1 );
    }

    @Test( timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyInputStreamValidOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( new DontCloseByteArrayInputStream( emptyByteArray() ), new DontCloseByteArrayOutputStream(), 1 );
    }

    @Test( timeout = INFINITE_LOOP_TIMEOUT )
    public void copyInputStreamValidOutputStreamPosBufSz()
        throws Exception
    {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = { 1, 2, 3, 4, 5, 6 };
        IOUtil.copy( new DontCloseByteArrayInputStream( input ), outputStream, 1 );
        assertThat( outputStream.toByteArray(), is( input ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullInputStream()
        throws Exception
    {
        IOUtil.toString( nullInputStream() );
    }

    @Test
    public void toStringEmptyInputStream()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyInputStream() ), is( emptyString() ) );
    }

    @Test
    public void toStringInputStream()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( new ByteArrayInputStream( probe.getBytes() ) ).getBytes(),
                    is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullInputStreamNegBufSz()
        throws Exception
    {
        IOUtil.toString( nullInputStream(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void toStringEmptyInputStreamNegBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyInputStream(), -1 ), is( emptyString() ) );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void toStringInputStreamNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( new ByteArrayInputStream( probe.getBytes() ), -1 ), is( probe ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringNullInputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.toString( nullInputStream(), 0 );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullInputStreamPosBufSz()
        throws Exception
    {
        IOUtil.toString( nullInputStream(), 1 );
    }

    @Test
    public void toStringEmptyInputStreamPosBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyInputStream(), 1 ), is( emptyString() ) );
    }

    @Test
    public void toStringInputStreamPosBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( new ByteArrayInputStream( probe.getBytes() ), 1 ).getBytes(),
                    is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullInputStreamNullEncoding()
        throws Exception
    {
        IOUtil.toString( nullInputStream(), null );
    }

    @Test( expected = NullPointerException.class )
    public void toStringEmptyInputStreamNullEncoding()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyInputStream(), null ), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringInputStreamNullEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( new ByteArrayInputStream( probe.getBytes() ), null ).getBytes(),
                    is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullInputStreamJunkEncoding()
        throws Exception
    {
        IOUtil.toString( nullInputStream(), "junk" );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void toStringEmptyInputStreamJunkEncoding()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyInputStream(), "junk" ), is( emptyString() ) );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void toStringInputStreamJunkEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( new ByteArrayInputStream( probe.getBytes() ), "junk" ).getBytes(),
                    is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullInputStreamValidEncoding()
        throws Exception
    {
        IOUtil.toString( nullInputStream(), "utf-16" );
    }

    @Test
    public void toStringEmptyInputStreamValidEncoding()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyInputStream(), "utf-16" ), is( emptyString() ) );
    }

    @Test
    public void toStringInputStreamValidEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat(
            IOUtil.toString( new ByteArrayInputStream( probe.getBytes( "utf-16" ) ), "utf-16" ).getBytes( "utf-8" ),
            is( probe.getBytes( "utf-8" ) ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullInputStreamNullEncodingNegBufSz()
        throws Exception
    {
        IOUtil.toString( nullInputStream(), null, -1 );
    }

    @Test( expected = NullPointerException.class )
    public void toStringEmptyInputStreamNullEncodingNegBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyInputStream(), null, -1 ), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringInputStreamNullEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( new ByteArrayInputStream( probe.getBytes() ), null, -1 ).getBytes(),
                    is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullInputStreamJunkEncodingNegBufSz()
        throws Exception
    {
        IOUtil.toString( nullInputStream(), "junk", -1 );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void toStringEmptyInputStreamJunkEncodingNegBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyInputStream(), "junk", -1 ), is( emptyString() ) );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void toStringInputStreamJunkEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( new ByteArrayInputStream( probe.getBytes() ), "junk", -1 ).getBytes(),
                    is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullInputStreamValidEncodingNegBufSz()
        throws Exception
    {
        IOUtil.toString( nullInputStream(), "utf-16", -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void toStringEmptyInputStreamValidEncodingNegBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyInputStream(), "utf-16", -1 ), is( emptyString() ) );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void toStringInputStreamValidEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat(
            IOUtil.toString( new ByteArrayInputStream( probe.getBytes( "utf-16" ) ), "utf-16", -1 ).getBytes( "utf-8" ),
            is( probe.getBytes( "utf-8" ) ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringNullInputStreamNullEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.toString( nullInputStream(), null, 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringEmptyInputStreamNullEncodingZeroBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyInputStream(), null, 0 ), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringInputStreamNullEncodingZeroBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( new ByteArrayInputStream( probe.getBytes() ), null, 0 ).getBytes(),
                    is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringNullInputStreamJunkEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.toString( nullInputStream(), "junk", 0 );
    }

    @Test( expected = UnsupportedEncodingException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringEmptyInputStreamJunkEncodingZeroBufSz()
        throws Exception
    {
        assertThat( IOUtil.toString( emptyInputStream(), "junk", 0 ), is( emptyString() ) );
    }

    @Test( expected = UnsupportedEncodingException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringInputStreamJunkEncodingZeroBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( new ByteArrayInputStream( probe.getBytes() ), "junk", 0 ).getBytes(),
                    is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void toStringNullInputStreamValidEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.toString( nullInputStream(), "utf-16", 0 );
    }

    /*
     * copy(InputStream,Writer)
     */

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamNullWriter()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullWriter() );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyInputStreamNullWriter()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), nullWriter() );
    }

    @Test
    public void copyEmptyInputStreamValidWriter()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyInputStream(), writer );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyInputStreamNullWriter()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), nullWriter() );
    }

    @Test
    public void copyInputStreamValidWriter()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), writer );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    /*
     * copy(InputStream,Writer,int)
     */

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamNullWriterNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyEmptyInputStreamNullWriterNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), nullWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyEmptyInputStreamValidWriterNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), new DontCloseStringWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyInputStreamNullWriterNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), nullWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyInputStreamValidWriterNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), writer, -1 );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullInputStreamNullWriterZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullWriter(), 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullInputStreamValidWriterZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new DontCloseStringWriter(), 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyInputStreamNullWriterZeroBufSz()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), nullWriter(), 0 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamNullWriterPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullWriter(), 1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamValidWriterPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new DontCloseStringWriter(), 1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyInputStreamNullWriterPosBufSz()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), nullWriter(), 1 );
    }

    @Test
    public void copyEmptyInputStreamValidWriterPosBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyInputStream(), writer, 1 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test
    public void copyInputStreamValidWriterPosBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), writer, 1 );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    /*
     * copy(InputStream,Writer,String)
     */

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamNullWriterNullEncoding()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullWriter(), null );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamValidWriterNullEncoding()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new DontCloseStringWriter(), null );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyInputStreamNullWriterNullEncoding()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), nullWriter(), null );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyInputStreamValidWriterNullEncoding()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), new DontCloseStringWriter(), null );
    }

    @Test( expected = NullPointerException.class )
    public void copyInputStreamNullEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), writer, null );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamNullWriterJunkEncoding()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullWriter(), "junk" );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamValidWriterJunkEncoding()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new DontCloseStringWriter(), "junk" );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyEmptyInputStreamNullWriterJunkEncoding()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), nullWriter(), "junk" );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyEmptyInputStreamValidWriterJunkEncoding()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), new DontCloseStringWriter(), "junk" );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyInputStreamNullWriterJunkEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), nullWriter(), "junk" );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyInputStreamValidWriterJunkEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), writer, "junk" );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamNullWriterValidEncoding()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullWriter(), "utf-16" );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyInputStreamNullWriterValidEncoding()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), nullWriter(), "utf-16" );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamValidWriterValidEncoding()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new DontCloseStringWriter(), "utf-16" );
    }

    @Test
    public void copyEmptyInputStreamValidWriterValidEncoding()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyInputStream(), writer, "utf-16" );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyInputStreamNullWriterValidEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes( "utf-16" ) ), nullWriter(), "utf-16" );
    }

    @Test
    public void copyInputStreamValidWriterValidEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes( "utf-16" ) ), writer, "utf-16" );
        assertThat( writer.toString().getBytes( "utf-8" ), is( probe.getBytes( "utf-8" ) ) );
    }

    /*
     * copy(InputStream,Writer,String,int)
     */

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamNullWriterNullEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullWriter(), null, -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamValidWriterNullEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new DontCloseStringWriter(), null, -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyInputStreamNullWriterNullEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), nullWriter(), null, -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyInputStreamValidWriterNullEncodingNegBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyInputStream(), writer, null, -1 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyInputStreamNullWriterNullEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), nullWriter(), null, -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyInputStreamValidWriterNullEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), writer, null, -1 );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamNullWriterJunkEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullWriter(), "junk", -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamValidWriterJunkEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new DontCloseStringWriter(), "junk", -1 );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyEmptyInputStreamNullWriterJunkEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), nullWriter(), "junk", -1 );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyEmptyInputStreamJunkEncodingNegBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyInputStream(), writer, "junk", -1 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyInputStreamNullWriterJunkEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), nullWriter(), "junk", -1 );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyInputStreamValidWriterJunkEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), writer, "junk", -1 );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamNullWriterValidEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullWriter(), "utf-16", -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullInputStreamValidWriterValidEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new DontCloseStringWriter(), "utf-16", -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyEmptyInputStreamNullWriterValidEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), nullWriter(), "utf-16", -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyEmptyInputStreamValidWriterValidEncodingNegBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyInputStream(), writer, "utf-16", -1 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyInputStreamNullWriterValidEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes( "utf-16" ) ), nullWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyInputStreamValidEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes( "utf-16" ) ), writer, "utf-16", -1 );
        assertThat( writer.toString().getBytes( "utf-8" ), is( probe.getBytes( "utf-8" ) ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullInputStreamNullWriterNullEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullWriter(), null, 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullInputStreamValidWriterNullEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new DontCloseStringWriter(), null, 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyInputStreamNullWriterNullEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), nullWriter(), null, 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyInputStreamValidWriterNullEncodingZeroBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyInputStream(), writer, null, 0 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyInputStreamNullWriterNullEncodingZeroBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), nullWriter(), null, 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyInputStreamValidWriterNullEncodingZeroBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), writer, null, 0 );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullInputStreamNullWriterJunkEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullWriter(), "junk", 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullInputStreamValidWriterJunkEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new DontCloseStringWriter(), "junk", 0 );
    }

    @Test( expected = UnsupportedEncodingException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyInputStreamNullWriterJunkEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( emptyInputStream(), nullWriter(), "junk", 0 );
    }

    @Test( expected = UnsupportedEncodingException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyInputStreamValidWriterJunkEncodingZeroBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyInputStream(), writer, "junk", 0 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = UnsupportedEncodingException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyInputStreamNullWriterJunkEncodingZeroBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), nullWriter(), "junk", 0 );
    }

    @Test( expected = UnsupportedEncodingException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyInputStreamValidWriterJunkEncodingZeroBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new ByteArrayInputStream( probe.getBytes() ), writer, "junk", 0 );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullInputStreamNullWriterValidEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), nullWriter(), "utf-16", 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullInputStreamValidWriterValidEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullInputStream(), new DontCloseStringWriter(), "utf-16", 0 );
    }

    /*
     * copy(String,Writer)
     */

    @Test( expected = NullPointerException.class )
    public void copyNullStringNullWriter()
        throws Exception
    {
        IOUtil.copy( nullString(), nullWriter() );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyStringNullWriter()
        throws Exception
    {
        IOUtil.copy( emptyString(), nullWriter() );
    }

    @Test
    public void copyNullStringValidWriter()
        throws Exception
    {
        IOUtil.copy( nullString(), new DontCloseStringWriter() );
    }

    @Test
    public void copyEmptyStringValidWriter()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyString(), writer );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyStringNullWriter()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( probe, nullWriter() );
    }

    @Test
    public void copyStringValidWriter()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( probe, writer );
        assertThat( writer.toString(), is( probe ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullStringNullOutputStream()
        throws Exception
    {
        IOUtil.copy( nullString(), nullOutputStream() );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyStringNullOutputStream()
        throws Exception
    {
        IOUtil.copy( emptyString(), nullOutputStream() );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullStringValidOutputStream()
        throws Exception
    {
        IOUtil.copy( nullString(), new DontCloseByteArrayOutputStream() );
    }

    @Test
    public void copyEmptyStringValidOutputStream()
        throws Exception
    {
        ByteArrayOutputStream OutputStream = new DontCloseByteArrayOutputStream();
        IOUtil.copy( emptyString(), OutputStream );
        assertThat( OutputStream.toByteArray(), is( emptyString().getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyStringNullOutputStream()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( probe, nullOutputStream() );
    }

    @Test
    public void copyStringValidOutputStream()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        ByteArrayOutputStream OutputStream = new DontCloseByteArrayOutputStream();
        IOUtil.copy( probe, OutputStream );
        assertThat( OutputStream.toByteArray(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullStringNullOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullString(), nullOutputStream(), -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyStringNullOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyString(), nullOutputStream(), -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullStringValidOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullString(), new DontCloseByteArrayOutputStream(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyEmptyStringValidOutputStreamNegBufSz()
        throws Exception
    {
        ByteArrayOutputStream OutputStream = new DontCloseByteArrayOutputStream();
        IOUtil.copy( emptyString(), OutputStream, -1 );
        assertThat( OutputStream.toByteArray(), is( emptyString().getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyStringNullOutputStreamNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( probe, nullOutputStream(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyStringValidOutputStreamNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        ByteArrayOutputStream OutputStream = new DontCloseByteArrayOutputStream();
        IOUtil.copy( probe, OutputStream, -1 );
        assertThat( OutputStream.toByteArray(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullStringNullOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullString(), nullOutputStream(), 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyStringNullOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( emptyString(), nullOutputStream(), 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullStringValidOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullString(), new DontCloseByteArrayOutputStream(), 0 );
    }
    @Test( expected = NullPointerException.class )
    public void copyNullStringNullOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullString(), nullOutputStream(), 1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyStringNullOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( emptyString(), nullOutputStream(), 1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullStringValidOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullString(), new DontCloseByteArrayOutputStream(), 1 );
    }

    @Test
    public void copyEmptyStringValidOutputStreamPosBufSz()
        throws Exception
    {
        ByteArrayOutputStream OutputStream = new DontCloseByteArrayOutputStream();
        IOUtil.copy( emptyString(), OutputStream, 1 );
        assertThat( OutputStream.toByteArray(), is( emptyString().getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyStringNullOutputStreamPosBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( probe, nullOutputStream(), 1 );
    }

    @Test
    public void copyStringValidOutputStreamPosBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        ByteArrayOutputStream OutputStream = new DontCloseByteArrayOutputStream();
        IOUtil.copy( probe, OutputStream, 1 );
        assertThat( OutputStream.toByteArray(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullReaderNullWriter()
        throws Exception
    {
        IOUtil.copy( nullReader(), nullWriter() );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyReaderNullWriter()
        throws Exception
    {
        IOUtil.copy( emptyReader(), nullWriter() );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullReaderValidWriter()
        throws Exception
    {
        IOUtil.copy( nullReader(), new DontCloseStringWriter() );
    }

    @Test
    public void copyEmptyReaderValidWriter()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyReader(), writer );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyReaderNullWriter()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new StringReader( probe ), nullWriter() );
    }

    @Test
    public void copyReaderValidWriter()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new StringReader( probe ), writer );
        assertThat( writer.toString(), is( probe ) );
    }

    /*
     * copy(Reader,Writer,int)
     */

    @Test( expected = NegativeArraySizeException.class )
    public void copyNullReaderNullWriterNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullReader(), nullWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyEmptyReaderNullWriterNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyReader(), nullWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyNullReaderValidWriterNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullReader(), new DontCloseStringWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyEmptyReaderValidWriterNegBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyReader(), writer, -1 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyReaderNullWriterNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new StringReader( probe ), nullWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyReaderValidWriterNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new StringReader( probe ), writer, -1 );
        assertThat( writer.toString(), is( probe ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullReaderNullWriterZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullReader(), nullWriter(), 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyReaderNullWriterZeroBufSz()
        throws Exception
    {
        IOUtil.copy( emptyReader(), nullWriter(), 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullReaderValidWriterZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullReader(), new DontCloseStringWriter(), 0 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullReaderNullWriterPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullReader(), nullWriter(), 1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyReaderNullWriterPosBufSz()
        throws Exception
    {
        IOUtil.copy( emptyReader(), nullWriter(), 1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullReaderValidWriterPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullReader(), new DontCloseStringWriter(), 1 );
    }

    @Test
    public void copyEmptyReaderValidWriterPosBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyReader(), writer, 1 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyReaderNullWriterPosBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new StringReader( probe ), nullWriter(), 1 );
    }

    @Test
    public void copyReaderValidWriterPosBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( new StringReader( probe ), writer, 1 );
        assertThat( writer.toString(), is( probe ) );
    }

    /*
     * toByteArray(InputStream,int)
     */

    @Test( expected = NegativeArraySizeException.class )
    public void toByteArrayFromInputStreamNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toByteArray( new DontCloseByteArrayInputStream( IOUtil.toByteArray( probe ) ), -1 ),
                    is( probe.getBytes() ) );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void toByteArrayNullInputStreamNegBufSz()
        throws Exception
    {
        IOUtil.toByteArray( nullInputStream(), -1 );
    }

    @Test
    public void toByteArrayFromInputStreamPosBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toByteArray( new DontCloseByteArrayInputStream( IOUtil.toByteArray( probe ) ), +1 ),
                    is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toByteArrayNullInputStreamPosBufSz()
        throws Exception
    {
        IOUtil.toByteArray( nullInputStream(), +1 );
    }

    /*
     * toByteArray(Reader,int)
     */

    @Test( expected = NegativeArraySizeException.class )
    public void toByteArrayFromReaderNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toByteArray( new DontCloseStringReader( probe ), -1 ),
                    is( probe.getBytes() ) );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void toByteArrayNullReaderNegBufSz()
        throws Exception
    {
        IOUtil.toByteArray( nullReader(), -1 );
    }

    @Test
    public void toByteArrayFromReaderPosBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toByteArray( new DontCloseStringReader( probe ), +1 ),
                    is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toByteArrayNullReaderPosBufSz()
        throws Exception
    {
        IOUtil.toByteArray( nullReader(), +1 );
    }

    /*
     * toByteArray(String,int)
     */

    @Test( expected = NegativeArraySizeException.class )
    public void toByteArrayFromStringNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toByteArray( probe, -1 ), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toByteArrayNullStringNegBufSz()
        throws Exception
    {
        IOUtil.toByteArray( nullString(), -1 );
    }

    @Test
    public void toByteArrayFromStringPosBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toByteArray( probe, +1 ), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void toByteArrayNullStringPosBufSz()
        throws Exception
    {
        IOUtil.toByteArray( nullString(), +1 );
    }

    /*
     * toString(Reader,int)
     */

    @Test( expected = NegativeArraySizeException.class )
    public void toStringFromReaderNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( new DontCloseStringReader( probe ), -1 ),
                    is( probe) );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void toStringNullReaderNegBufSz()
        throws Exception
    {
        IOUtil.toString( nullReader(), -1 );
    }

    @Test
    public void toStringFromReaderPosBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        assertThat( IOUtil.toString( new DontCloseStringReader( probe ), +1 ),
                    is( probe) );
    }

    @Test( expected = NullPointerException.class )
    public void toStringNullReaderPosBufSz()
        throws Exception
    {
        IOUtil.toString( nullReader(), +1 );
    }

    /*
     * copy(Reader,OutputStream)
     */

    @Test( expected = NullPointerException.class )
    public void copyNullReaderNullOutputStream()
        throws Exception
    {
        IOUtil.copy( nullReader(), nullOutputStream() );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullReaderValidOutputStream()
        throws Exception
    {
        IOUtil.copy( nullReader(), new DontCloseByteArrayOutputStream() );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyReaderNullOutputStream()
        throws Exception
    {
        IOUtil.copy( emptyReader(), nullOutputStream() );
    }

    @Test
    public void copyEmptyReaderValidOutputStream()
        throws Exception
    {
        IOUtil.copy( emptyReader(), new DontCloseByteArrayOutputStream() );
    }

    @Test
    public void copyReaderValidOutputStream()
        throws Exception
    {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new DontCloseStringReader( probe ), outputStream );
        assertThat( outputStream.toByteArray(), is( probe.getBytes()) );
    }

    /*
     * copy(Reader,OutputStream,int)
     */

    @Test( expected = NullPointerException.class )
    public void copyNullReaderNullOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullReader(), nullOutputStream(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyNullReaderValidOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullReader(), new DontCloseByteArrayOutputStream(), -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyReaderNullOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyReader(), nullOutputStream(), -1 );
    }

    @Test(expected = NegativeArraySizeException.class)
    public void copyEmptyReaderValidOutputStreamNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyReader(), new DontCloseByteArrayOutputStream(), -1 );
    }

    @Test(expected = NegativeArraySizeException.class)
    public void copyReaderValidOutputStreamNegBufSz()
        throws Exception
    {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new DontCloseStringReader( probe ), outputStream, -1 );
        assertThat( outputStream.toByteArray(), is( probe.getBytes()) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullReaderNullOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullReader(), nullOutputStream(), 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullReaderValidOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullReader(), new DontCloseByteArrayOutputStream(), 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyReaderNullOutputStreamZeroBufSz()
        throws Exception
    {
        IOUtil.copy( emptyReader(), nullOutputStream(), 0 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullReaderNullOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullReader(), nullOutputStream(), 1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullReaderValidOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullReader(), new DontCloseByteArrayOutputStream(), 1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyReaderNullOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( emptyReader(), nullOutputStream(), 1 );
    }

    @Test
    public void copyEmptyReaderValidOutputStreamPosBufSz()
        throws Exception
    {
        IOUtil.copy( emptyReader(), new DontCloseByteArrayOutputStream(), 1 );
    }

    @Test
    public void copyReaderValidOutputStreamPosBufSz()
        throws Exception
    {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( new DontCloseStringReader( probe ), outputStream, 1 );
        assertThat( outputStream.toByteArray(), is( probe.getBytes()) );
    }

    /*
     * copy(byte[],Writer)
     */

    /*
     * copy(byte[],Writer,int)
     */

    /*
     * copy(byte[],Writer,String)
     */

    /*
     * copy(byte[],Writer,String,int)
     */
    /*
     * copy(byte[],Writer)
     */

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayNullWriter()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullWriter() );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyByteArrayNullWriter()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullWriter() );
    }

    @Test
    public void copyEmptyByteArrayValidWriter()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyByteArray(), writer );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyByteArrayNullWriter()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( probe.getBytes(), nullWriter() );
    }

    @Test
    public void copyByteArrayValidWriter()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( probe.getBytes(), writer );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    /*
     * copy(byte[],Writer,int)
     */

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayNullWriterNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyEmptyByteArrayNullWriterNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyEmptyByteArrayValidWriterNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), new DontCloseStringWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyByteArrayNullWriterNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( probe.getBytes(), nullWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyByteArrayValidWriterNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( probe.getBytes(), writer, -1 );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullByteArrayNullWriterZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullWriter(), 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullByteArrayValidWriterZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseStringWriter(), 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyByteArrayNullWriterZeroBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullWriter(), 0 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayNullWriterPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullWriter(), 1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayValidWriterPosBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseStringWriter(), 1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyByteArrayNullWriterPosBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullWriter(), 1 );
    }

    @Test
    public void copyEmptyByteArrayValidWriterPosBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyByteArray(), writer, 1 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test
    public void copyByteArrayValidWriterPosBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( probe.getBytes(), writer, 1 );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    /*
     * copy(byte[],Writer,String)
     */

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayNullWriterNullEncoding()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullWriter(), null );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayValidWriterNullEncoding()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseStringWriter(), null );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyByteArrayNullWriterNullEncoding()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullWriter(), null );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyByteArrayValidWriterNullEncoding()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), new DontCloseStringWriter(), null );
    }

    @Test( expected = NullPointerException.class )
    public void copyByteArrayNullEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( probe.getBytes(), writer, null );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayNullWriterJunkEncoding()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullWriter(), "junk" );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayValidWriterJunkEncoding()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseStringWriter(), "junk" );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyEmptyByteArrayNullWriterJunkEncoding()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullWriter(), "junk" );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyEmptyByteArrayValidWriterJunkEncoding()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), new DontCloseStringWriter(), "junk" );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyByteArrayNullWriterJunkEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( probe.getBytes(), nullWriter(), "junk" );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyByteArrayValidWriterJunkEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( probe.getBytes(), writer, "junk" );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayNullWriterValidEncoding()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullWriter(), "utf-16" );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyByteArrayNullWriterValidEncoding()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullWriter(), "utf-16" );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayValidWriterValidEncoding()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseStringWriter(), "utf-16" );
    }

    @Test
    public void copyEmptyByteArrayValidWriterValidEncoding()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyByteArray(), writer, "utf-16" );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyByteArrayNullWriterValidEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( probe.getBytes( "utf-16" ), nullWriter(), "utf-16" );
    }

    @Test
    public void copyByteArrayValidWriterValidEncoding()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( probe.getBytes( "utf-16" ), writer, "utf-16" );
        assertThat( writer.toString().getBytes( "utf-8" ), is( probe.getBytes( "utf-8" ) ) );
    }

    /*
     * copy(byte[],Writer,String,int)
     */

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayNullWriterNullEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullWriter(), null, -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayValidWriterNullEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseStringWriter(), null, -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyByteArrayNullWriterNullEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullWriter(), null, -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyEmptyByteArrayValidWriterNullEncodingNegBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyByteArray(), writer, null, -1 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyByteArrayNullWriterNullEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( probe.getBytes(), nullWriter(), null, -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyByteArrayValidWriterNullEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( probe.getBytes(), writer, null, -1 );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayNullWriterJunkEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullWriter(), "junk", -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayValidWriterJunkEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseStringWriter(), "junk", -1 );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyEmptyByteArrayNullWriterJunkEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullWriter(), "junk", -1 );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyEmptyByteArrayJunkEncodingNegBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyByteArray(), writer, "junk", -1 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyByteArrayNullWriterJunkEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( probe.getBytes(), nullWriter(), "junk", -1 );
    }

    @Test( expected = UnsupportedEncodingException.class )
    public void copyByteArrayValidWriterJunkEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( probe.getBytes(), writer, "junk", -1 );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayNullWriterValidEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullWriter(), "utf-16", -1 );
    }

    @Test( expected = NullPointerException.class )
    public void copyNullByteArrayValidWriterValidEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseStringWriter(), "utf-16", -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyEmptyByteArrayNullWriterValidEncodingNegBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullWriter(), "utf-16", -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyEmptyByteArrayValidWriterValidEncodingNegBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyByteArray(), writer, "utf-16", -1 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyByteArrayNullWriterValidEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( probe.getBytes( "utf-16" ), nullWriter(), -1 );
    }

    @Test( expected = NegativeArraySizeException.class )
    public void copyByteArrayValidEncodingNegBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( probe.getBytes( "utf-16" ), writer, "utf-16", -1 );
        assertThat( writer.toString().getBytes( "utf-8" ), is( probe.getBytes( "utf-8" ) ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullByteArrayNullWriterNullEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullWriter(), null, 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullByteArrayValidWriterNullEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseStringWriter(), null, 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyByteArrayNullWriterNullEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullWriter(), null, 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyByteArrayValidWriterNullEncodingZeroBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyByteArray(), writer, null, 0 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyByteArrayNullWriterNullEncodingZeroBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( probe.getBytes(), nullWriter(), null, 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyByteArrayValidWriterNullEncodingZeroBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( probe.getBytes(), writer, null, 0 );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullByteArrayNullWriterJunkEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullWriter(), "junk", 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullByteArrayValidWriterJunkEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseStringWriter(), "junk", 0 );
    }

    @Test( expected = UnsupportedEncodingException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyByteArrayNullWriterJunkEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( emptyByteArray(), nullWriter(), "junk", 0 );
    }

    @Test( expected = UnsupportedEncodingException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyEmptyByteArrayValidWriterJunkEncodingZeroBufSz()
        throws Exception
    {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( emptyByteArray(), writer, "junk", 0 );
        assertThat( writer.toString(), is( emptyString() ) );
    }

    @Test( expected = UnsupportedEncodingException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyByteArrayNullWriterJunkEncodingZeroBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        IOUtil.copy( probe.getBytes(), nullWriter(), "junk", 0 );
    }

    @Test( expected = UnsupportedEncodingException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyByteArrayValidWriterJunkEncodingZeroBufSz()
        throws Exception
    {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy( probe.getBytes(), writer, "junk", 0 );
        assertThat( writer.toString().getBytes(), is( probe.getBytes() ) );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullByteArrayNullWriterValidEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), nullWriter(), "utf-16", 0 );
    }

    @Test( expected = NullPointerException.class, timeout = INFINITE_LOOP_TIMEOUT )
    public void copyNullByteArrayValidWriterValidEncodingZeroBufSz()
        throws Exception
    {
        IOUtil.copy( nullByteArray(), new DontCloseStringWriter(), "utf-16", 0 );
    }

    /*
     * Utility methods
     */
    private static byte[] nullByteArray()
    {
        return null;
    }

    private static String nullString()
    {
        return null;
    }

    private static OutputStream nullOutputStream()
    {
        return null;
    }

    private static InputStream nullInputStream()
    {
        return null;
    }

    private static Writer nullWriter()
    {
        return null;
    }

    private static Reader nullReader()
    {
        return null;
    }

    private static ByteArrayInputStream emptyInputStream()
    {
        return new ByteArrayInputStream( emptyByteArray() );
    }

    private static Reader emptyReader()
    {
        return new StringReader( emptyString() );
    }

    private static String emptyString()
    {
        return "";
    }

    private static byte[] emptyByteArray()
    {
        return new byte[0];
    }

    private static class DontCloseStringWriter
        extends StringWriter
    {
        @Override
        public void close()
            throws IOException
        {
            throw new UnsupportedOperationException( "should not be called" );
        }
    }

    private static class DontCloseStringReader
        extends StringReader
    {

        public DontCloseStringReader( String s )
        {
            super( s );
        }

        @Override
        public void close()
        {
            throw new UnsupportedOperationException( "should not be called" );
        }
    }

    private static class DontCloseByteArrayInputStream
        extends ByteArrayInputStream
    {
        public DontCloseByteArrayInputStream( byte[] input )
        {
            super( input );
        }

        @Override
        public void close()
            throws IOException
        {
            throw new UnsupportedOperationException( "should not be called" );
        }
    }

    private static class DontCloseByteArrayOutputStream
        extends ByteArrayOutputStream
    {
        @Override
        public void close()
            throws IOException
        {
            throw new UnsupportedOperationException( "should not be called" );
        }
    }
}
