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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Static utility methods for loading properties.
 */
public class PropertyUtils
{

    /**
     * The constructor.
     *
     * @deprecated This is a utility class with only static methods. Don't create instances of it.
     */
    @Deprecated
    public PropertyUtils()
    {
    }

    /**
     * @param url the URL which should be used to load the properties
     * @return the loaded properties
     * @deprecated use {@link #loadOptionalProperties(java.net.URL)} instead. This method should not
     *             be used as it suppresses exceptions silently when loading properties fails and returns {@code null}
     *             instead of an empty {@code Properties} instance when the given {@code URL} is {@code null}.
     */
    @Deprecated
    public static java.util.Properties loadProperties( @Nonnull URL url )
    {
        try ( InputStream in = url.openStream() )
        {
            return loadProperties( in );
        }
        catch ( Exception e )
        {
            // ignore
        }
        return null;
    }

    /**
     * @param file the file from which the properties will be loaded
     * @return the loaded properties
     * @deprecated use {@link #loadOptionalProperties(java.io.File)} instead. This method should not
     *             be used as it suppresses exceptions silently when loading properties fails and returns {@code null}
     *             instead of an empty {@code Properties} instance when the given {@code File} is {@code null}.
     */
    @Deprecated
    public static Properties loadProperties( @Nonnull File file )
    {
        try  ( InputStream in = new FileInputStream( file ) )
        {
            return loadProperties( in );
        }
        catch ( Exception e )
        {
            // ignore
        }
        return null;
    }

    /**
     * Loads {@code Properties} from an {@code InputStream} and closes the stream.
     * In a future release, this will no longer close the stream, so callers
     * should close the stream themselves.  
     * 
     * @param is {@link InputStream}
     * @return the loaded properties
     * @deprecated use {@link #loadOptionalProperties(java.io.InputStream)} instead. This method
     *             should not be used as it suppresses exceptions silently when loading properties fails.
     */
    @Deprecated
    public static Properties loadProperties( @Nullable InputStream is )
    {
        try
        {
            Properties result = new Properties();
            if ( is != null )
            {
                try ( InputStream in = is )
                {
                    result.load( in );
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
        return null;
    }

    /**
     * Loads {@code Properties} from a given {@code URL}.
     * <p>
     * If the given {@code URL} is {@code null} or the properties can't be read, an empty properties object is returned.
     * </p>
     *
     * @param url the {@code URL} of the properties resource to load or {@code null}
     * @return the loaded properties or an empty {@code Properties} instance if properties fail to load
     * @since 3.1.0
     */
    @Nonnull
    public static Properties loadOptionalProperties( final @Nullable URL url )
    {

        Properties properties = new Properties();
        if ( url != null )
        {
            try ( InputStream in = url.openStream() )
            {
                properties.load( in );
            }
            catch ( IllegalArgumentException | IOException ex )
            {
                // ignore and return empty properties
            }
        }
        return properties;
    }

    /**
     * Loads {@code Properties} from a {@code File}.
     * <p>
     * If the given {@code File} is {@code null} or the properties file can't be read, an empty properties object is
     * returned.
     * </p>
     *
     * @param file the {@code File} of the properties resource to load or {@code null}
     * @return the loaded properties or an empty {@code Properties} instance if properties fail to load
     * @since 3.1.0
     */
    @Nonnull
    public static Properties loadOptionalProperties( final @Nullable File file )
    {
        Properties properties = new Properties();
        if ( file != null )
        {
            try ( InputStream in = new FileInputStream( file ) )
            {
                properties.load( in );
            }
            catch ( IllegalArgumentException | IOException ex )
            {
                // ignore and return empty properties
            }
        }

        return properties;

    }

    /**
     * Loads {@code Properties} from an {@code InputStream} and closes the stream.
     * If the given {@code InputStream} is {@code null} or the properties can't be read, an empty properties object is
     * returned. In a future release, this will no longer close the stream, so callers
     * should close the stream themselves.  
     *
     * @param inputStream the properties resource to load or {@code null}
     * @return the loaded properties or an empty {@code Properties} instance if properties fail to load
     * @since 3.1.0
     */
    @Nonnull
    public static Properties loadOptionalProperties( final @Nullable InputStream inputStream )
    {

        Properties properties = new Properties();

        if ( inputStream != null )
        {
            try ( InputStream in = inputStream ) // reassign inputStream to autoclose
            {
                properties.load( in );
            }
            catch ( IllegalArgumentException | IOException ex )
            {
                // ignore and return empty properties
            }
        }

        return properties;

    }

}
