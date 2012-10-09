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
 * @deprecated use XmlStreamReader
 */
public class XmlReader
    extends Reader
{
    private final org.apache.commons.io.input.XmlStreamReader reader;

    private static String _staticDefaultEncoding = null;

    public static void setDefaultEncoding( String encoding )
    {
        _staticDefaultEncoding = encoding;
    }

    public static String getDefaultEncoding()
    {
        return _staticDefaultEncoding;
    }

    XmlReader( File file )
        throws IOException
    {
        this( new FileInputStream( file ) );
    }

    XmlReader( InputStream is )
        throws IOException
    {
        this( is, true );
    }

    XmlReader( InputStream is, boolean lenient )
        throws IOException, XmlStreamReaderException
    {
        reader = new org.apache.commons.io.input.XmlStreamReader( is, lenient, _staticDefaultEncoding );
    }

    XmlReader( URL url )
        throws IOException
    {
        this( url.openConnection() );
    }

    XmlReader( URLConnection conn )
        throws IOException
    {
        reader = new org.apache.commons.io.input.XmlStreamReader( conn, _staticDefaultEncoding );
    }

    XmlReader( InputStream is, String httpContentType )
        throws IOException
    {
        this( is, httpContentType, true );
    }

    XmlReader( InputStream is, String httpContentType, boolean lenient, String defaultEncoding )
        throws IOException, XmlStreamReaderException
    {
        reader = new org.apache.commons.io.input.XmlStreamReader( is, httpContentType, lenient,
                                                                  ( defaultEncoding == null )
                                                                      ? _staticDefaultEncoding
                                                                      : defaultEncoding );
    }

    XmlReader( InputStream is, String httpContentType, boolean lenient )
        throws IOException, XmlStreamReaderException
    {
        this( is, httpContentType, lenient, null );
    }

    public String getEncoding()
    {
        return reader.getEncoding();
    }

    public int read( char[] buf, int offset, int len )
        throws IOException
    {
        return reader.read( buf, offset, len );
    }

    public void close()
        throws IOException
    {
        reader.close();
    }

    static final Pattern ENCODING_PATTERN =
        Pattern.compile( "<\\?xml.*encoding[\\s]*=[\\s]*((?:\".[^\"]*\")|(?:'.[^']*'))", Pattern.MULTILINE );
}
