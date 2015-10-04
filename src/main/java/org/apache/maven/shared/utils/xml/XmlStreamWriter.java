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
import java.io.FileNotFoundException;
import java.io.OutputStream;

/**
 * We just wrap the commons StreamWriter to not get into troubles
 * by exposing shaded commons-io packages
 */
public class XmlStreamWriter
    extends org.apache.commons.io.output.XmlStreamWriter
{
    /**
     * @param out {@link OutputStream}
     */
    public XmlStreamWriter( OutputStream out )
    {
        super( out );
    }

    /**
     * @param file The file to use.
     * @throws FileNotFoundException in case of not found file.
     */
    public XmlStreamWriter( File file )
        throws FileNotFoundException
    {
        super( file );
    }
}
