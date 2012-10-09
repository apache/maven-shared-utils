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


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.maven.shared.utils.io.IOUtil;

class PropertyUtils
{

    public PropertyUtils()
    {
        // should throw new IllegalAccessError( "Utility class" );
    }

    public static java.util.Properties loadProperties( java.net.URL url )
    {
        try
        {
            return loadProperties( url.openStream() );
        }
        catch ( Exception e )
        {
            // ignore
        }
        return null;
    }

    public static java.util.Properties loadProperties( java.io.File file )
    {
        try
        {
            return loadProperties( new FileInputStream( file ) );
        }
        catch ( Exception e )
        {
            // ignore
        }
        return null;
    }

    public static java.util.Properties loadProperties( java.io.InputStream is )
    {
        try
        {
            // to make this the same behaviour as the others we should really return null on any error
            Properties result = new Properties();
            if ( is != null )
            {
                try
                {
                    result.load( is );
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
            return result;
        }
        catch ( Exception e )
        {
            // ignore
        }
        finally
        {
            IOUtil.close( is );
        }
        return null;
    }

}
