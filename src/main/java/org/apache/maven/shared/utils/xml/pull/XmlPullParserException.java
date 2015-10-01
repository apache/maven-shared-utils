package org.apache.maven.shared.utils.xml.pull;

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

import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * 
 */
public class XmlPullParserException
    extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = 117075811816936575L;

    /**
     * @param e IOException.
     */
    public XmlPullParserException( IOException e )
    {
        super( e );
    }

    /**
     * @param e The exception.
     */
    public XmlPullParserException( SAXException e )
    {
        super( e );
    }

    /**
     * @param message The message.
     */
    public XmlPullParserException( String message )
    {
        super( message );
    }
}
