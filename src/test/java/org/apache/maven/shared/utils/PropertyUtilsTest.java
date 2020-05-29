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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class PropertyUtilsTest
{

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    @interface NeedsTemporaryFolder
    {
    }

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    @SuppressWarnings( "deprecation" )
    // @ReproducesPlexusBug( "Should return null on error like url and file do" )
    public void loadNullInputStream()
        throws Exception
    {
        assertThat( PropertyUtils.loadProperties( (InputStream) null ), is( new Properties() ) );
    }

    @Test
    public void loadOptionalNullInputStream()
        throws Exception
    {
        assertThat( PropertyUtils.loadOptionalProperties( (InputStream) null ), is( new Properties() ) );
    }
    

    @Test
    public void loadOptionalProperties_ioException()
        throws Exception
    {
        URL url = new URL( "https://nonesuch12344.foo.bar.com" );
        assertThat( PropertyUtils.loadOptionalProperties( url ), is( new Properties() ) );
    }

    @Test
    @SuppressWarnings( "deprecation" )
    public void loadNullURL()
        throws Exception
    {
        assertThat( PropertyUtils.loadProperties( (URL) null ), nullValue( Properties.class ) );
    }

    @Test
    public void loadOptionalNullURL()
        throws Exception
    {
        assertThat( PropertyUtils.loadOptionalProperties( (URL) null ), is( new Properties() ) );
    }

    @Test
    @SuppressWarnings( "deprecation" )
    public void loadNullFile()
        throws Exception
    {
        assertThat( PropertyUtils.loadProperties( (File) null ), nullValue( Properties.class ) );
    }

    @Test
    public void loadOptionalNullFile()
        throws Exception
    {
        assertThat( PropertyUtils.loadOptionalProperties( (File) null ), is( new Properties() ) );
    }

    @Test
    @SuppressWarnings( "deprecation" )
    public void loadEmptyInputStream()
        throws Exception
    {
        assertThat( PropertyUtils.loadProperties( new ByteArrayInputStream( new byte[ 0 ] ) ),
                    is( new Properties() ) );

        assertThat( PropertyUtils.loadOptionalProperties( new ByteArrayInputStream( new byte[ 0 ] ) ),
                    is( new Properties() ) );

    }

    @Test
    @NeedsTemporaryFolder
    @SuppressWarnings( "deprecation" )
    public void loadEmptyFile()
        throws Exception
    {
        assertThat( PropertyUtils.loadProperties( tempFolder.newFile( "empty" ) ), is( new Properties() ) );
        assertThat( PropertyUtils.loadOptionalProperties( tempFolder.newFile( "optional" ) ), is( new Properties() ) );
    }

    @Test
    @NeedsTemporaryFolder
    @SuppressWarnings( "deprecation" )
    public void loadEmptyURL()
        throws Exception
    {
        assertThat( PropertyUtils.loadProperties( tempFolder.newFile( "empty" ).toURI().toURL() ),
                    is( new Properties() ) );

        assertThat( PropertyUtils.loadOptionalProperties( tempFolder.newFile( "optional" ).toURI().toURL() ),
                    is( new Properties() ) );

    }

    @Test
    @SuppressWarnings( "deprecation" )
    public void loadValidInputStream() throws UnsupportedEncodingException
    {
        Properties value = new Properties();
        value.setProperty( "a", "b" );

        assertThat( PropertyUtils.loadProperties( new ByteArrayInputStream( "a=b".getBytes( "ISO-8859-1" ) ) ),
                    is( value ) );

        assertThat( PropertyUtils.loadOptionalProperties( new ByteArrayInputStream( "a=b".getBytes( "ISO-8859-1" ) ) ),
                    is( value ) );

    }

    @Test
    @NeedsTemporaryFolder
    @SuppressWarnings( "deprecation" )
    public void loadValidFile() throws IOException
    {
        File valid = tempFolder.newFile( "valid" );
        Properties value = new Properties();
        value.setProperty( "a", "b" );
        try ( OutputStream out = new FileOutputStream( valid ) )
        {
            value.store( out, "a test" );
            assertThat( PropertyUtils.loadProperties( valid ), is( value ) );
            assertThat( PropertyUtils.loadOptionalProperties( valid ), is( value ) );
        }
    }

    @Test
    @NeedsTemporaryFolder
    @SuppressWarnings( "deprecation" )
    public void loadValidURL() throws IOException
    {
        File valid = tempFolder.newFile( "valid" );
        Properties value = new Properties();
        value.setProperty( "a", "b" );
        try ( OutputStream out = new FileOutputStream( valid ) )
        {
          value.store( out, "a test" );
          assertThat( PropertyUtils.loadProperties( valid.toURI().toURL() ), is( value ) );
          assertThat( PropertyUtils.loadOptionalProperties( valid.toURI().toURL() ), is( value ) );
        }
    }

}
