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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

/**
 * 
 */
public class XmlStreamReader
        extends Reader
{
    private final org.apache.commons.io.input.XmlStreamReader reader;

    private static String staticDefaultEncoding = null;

    /**
     * @param encoding define the default encoding.
     */
    public static void setDefaultEncoding( String encoding )
    {
        staticDefaultEncoding = encoding;
    }

    /**
     * @return the default encoding.
     */
    public static String getDefaultEncoding()
    {
        return staticDefaultEncoding;
    }

    /**
     * @param file The file to create it from.
     * @throws IOException in case of an error.
     */
    public XmlStreamReader( File file )
            throws IOException
    {
        this( new FileInputStream( file ) );
    }

    /**
     * @param is {@link InputStream}
     * @throws IOException in case of an error.
     */
    public XmlStreamReader( InputStream is )
            throws IOException
    {
        this( is, true );
    }

    /**
     * @param is {@link InputStream}
     * @param lenient yes/no
     * @throws IOException in case of an error.
     * @throws XmlStreamReaderException in case of an error.
     */
    public XmlStreamReader( InputStream is, boolean lenient )
            throws IOException, XmlStreamReaderException
    {
        reader = new org.apache.commons.io.input.XmlStreamReader( is, lenient, staticDefaultEncoding );
    }

    /**
     * @param url {@link URL}
     * @throws IOException in case of error.
     */
    public XmlStreamReader( URL url )
            throws IOException
    {
        this( url.openConnection() );
    }

    /**
     * @param conn The URL connection {@link URLConnection}.
     * @throws IOException in case of error.
     */
    public XmlStreamReader( URLConnection conn )
            throws IOException
    {
        reader = new org.apache.commons.io.input.XmlStreamReader( conn, staticDefaultEncoding );
    }

    /**
     * @param is {@link InputStream}
     * @param httpContentType content type.
     * @throws IOException in case of error.
     */
    public XmlStreamReader( InputStream is, String httpContentType )
            throws IOException
    {
        this( is, httpContentType, true );
    }

    /**
     * @param is {@link InputStream}
     * @param httpContentType content type.
     * @param lenient yes/no.
     * @param defaultEncoding The default encoding.
     * @throws IOException in case of error.
     * @throws XmlStreamReaderException in case of error.
     */
    public XmlStreamReader( InputStream is, String httpContentType, boolean lenient, String defaultEncoding )
            throws IOException, XmlStreamReaderException
    {
        reader = new org.apache.commons.io.input.XmlStreamReader( is, httpContentType, lenient,
                ( defaultEncoding == null )
                        ? staticDefaultEncoding
                        : defaultEncoding );
    }

    /**
     * @param is {@link InputStream}
     * @param httpContentType content type.
     * @param lenient yes/no.
     * @throws IOException in case of error.
     * @throws XmlStreamReaderException in case of error.
     */
    public XmlStreamReader( InputStream is, String httpContentType, boolean lenient )
            throws IOException, XmlStreamReaderException
    {
        this( is, httpContentType, lenient, null );
    }

    /**
     * @return The current encoding.
     */
    public String getEncoding()
    {
        return reader.getEncoding();
    }

    /** {@inheritDoc} */
    public int read( char[] buf, int offset, int len )
            throws IOException
    {
        return reader.read( buf, offset, len );
    }

    /** {@inheritDoc} */
    public void close()
            throws IOException
    {
        reader.close();
    }

    static final Pattern ENCODING_PATTERN =
            Pattern.compile( "<\\?xml.*encoding[\\s]*=[\\s]*((?:\".[^\"]*\")|(?:'.[^']*'))", Pattern.MULTILINE );
}
