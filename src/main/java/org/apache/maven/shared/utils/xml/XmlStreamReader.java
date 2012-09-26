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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

@Deprecated
public class XmlStreamReader
    extends XmlReader
{
    public XmlStreamReader( File file )
        throws IOException
    {
        super( file );
    }

    public XmlStreamReader( InputStream is )
        throws IOException
    {
        super( is );
    }

    public XmlStreamReader( InputStream is, boolean lenient )
        throws IOException, XmlStreamReaderException
    {
        super( is, lenient );
    }

    public XmlStreamReader( URL url )
        throws IOException
    {
        super( url );
    }

    public XmlStreamReader( URLConnection conn )
        throws IOException
    {
        super( conn );
    }

    public XmlStreamReader( InputStream is, String httpContentType )
        throws IOException
    {
        super( is, httpContentType );
    }

    public XmlStreamReader( InputStream is, String httpContentType, boolean lenient, String defaultEncoding )
        throws IOException, XmlStreamReaderException
    {
        super( is, httpContentType, lenient, defaultEncoding );
    }

    public XmlStreamReader( InputStream is, String httpContentType, boolean lenient )
        throws IOException, XmlStreamReaderException
    {
        super( is, httpContentType, lenient );
    }
}
