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

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;

/**
 * This class will test the 'Os' class which evaluates
 * operation system specific settings.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class OsTest
    extends Assert
{
    private String origOsName;
    private String origOsFamily;
    private String origOsArch;
    private String origOsVersion;


    @Before
    public void setUp()
        throws Exception
    {
        origOsName = System.getProperty( "os.name" );
        origOsArch = System.getProperty( "os.arch" );
        origOsVersion = System.getProperty( "os.version" );
        origOsFamily = Os.OS_FAMILY;

        // and now set some special settings ;)
        System.setProperty( "os.name"   , "os/2" );
        System.setProperty( "os.arch"   , "i386" );
        System.setProperty( "os.version", "2.1.32" );

        // blow away the originally loaded values
        setStaticOsField( "OS_NAME", "os/2" );
        setStaticOsField( "OS_FAMILY", "os/2" );
        setStaticOsField( "OS_ARCH", "i386" );
        setStaticOsField( "OS_VERSION", "2.1.32" );
    }

    @After
    public void tearDown()
        throws Exception
    {
        // set the original OS settings again
        System.setProperty( "os.name"   , origOsName );
        System.setProperty( "os.arch"   , origOsArch );
        System.setProperty( "os.version", origOsVersion );

        // restore the originally loaded values
        setStaticOsField( "OS_NAME", origOsName );
        setStaticOsField( "OS_ARCH", origOsArch );
        setStaticOsField( "OS_VERSION", origOsVersion );
        setStaticOsField( "OS_FAMILY", origOsFamily );
    }

    private void setStaticOsField( String variableName, Object value )
        throws NoSuchFieldException, IllegalAccessException
    {
        Field field = Os.class.getField( variableName );

        Field modifiersField = Field.class.getDeclaredField( "modifiers" );
        modifiersField.setAccessible( true );
        modifiersField.setInt( field, field.getModifiers() & ~Modifier.FINAL );

        field.setAccessible( true );
        field.set( null, value );
    }


    @Test
    public void testConstructor()
        throws Exception
    {
        Os os  = new Os();
        os.eval();

        assertTrue( Os.isName( Os.FAMILY_OS2 ) );

        assertFalse( Os.isName( Os.FAMILY_DOS ) );
        assertFalse( Os.isName( Os.FAMILY_MAC ) );
        assertFalse( Os.isName( Os.FAMILY_NETWARE ) );
        assertFalse( Os.isName( Os.FAMILY_OPENVMS ) );
        assertFalse( Os.isName( Os.FAMILY_OS400 ) );
        assertFalse( Os.isName( Os.FAMILY_TANDEM ) );
        assertFalse( Os.isName( Os.FAMILY_UNIX ) );
        assertFalse( Os.isName( Os.FAMILY_WIN9X ) );
        assertFalse( Os.isName( Os.FAMILY_WINDOWS ) );
        assertFalse( Os.isName( Os.FAMILY_ZOS ) );
    }

    @Test
    public void testFamilyNames()
    {
        assertEquals( Os.FAMILY_DOS, "dos" );
        assertEquals( Os.FAMILY_MAC, "mac" );
        assertEquals( Os.FAMILY_NETWARE, "netware" );
        assertEquals( Os.FAMILY_OPENVMS, "openvms" );
        assertEquals( Os.FAMILY_OS2, "os/2" );
        assertEquals( Os.FAMILY_OS400, "os/400" );
        assertEquals( Os.FAMILY_TANDEM, "tandem" );
        assertEquals( Os.FAMILY_UNIX, "unix" );
        assertEquals( Os.FAMILY_WIN9X, "win9x" );
        assertEquals( Os.FAMILY_WINDOWS, "windows" );
        assertEquals( Os.FAMILY_ZOS, "z/os" );
    }

    @Test
    public void testGetValidFamilies()
    {
        Set<String> osFamilies = Os.getValidFamilies();

        assertTrue( "OsFamilies Set size"
                  , osFamilies.size() >= 11 );
        
        assert( osFamilies.contains( Os.FAMILY_DOS ) );
        assert( osFamilies.contains( Os.FAMILY_MAC ) );
        assert( osFamilies.contains( Os.FAMILY_NETWARE ) );
        assert( osFamilies.contains( Os.FAMILY_OPENVMS ) );
        assert( osFamilies.contains( Os.FAMILY_OS2 ) );
        assert( osFamilies.contains( Os.FAMILY_OS400 ) );
        assert( osFamilies.contains( Os.FAMILY_TANDEM ) );
        assert( osFamilies.contains( Os.FAMILY_UNIX ) );
        assert( osFamilies.contains( Os.FAMILY_WIN9X ) );
        assert( osFamilies.contains( Os.FAMILY_WINDOWS ) );
        assert( osFamilies.contains( Os.FAMILY_ZOS ) );
        
    }


    @Test
    public void testIsArch()
    {
        assertThat( "Arch is i386"
                  , Os.isArch( "i386" )
                  , is( true ) );

        assertThat( "Os is not Mac"
                  , Os.isArch( "x86_64" )
                  , is( false ) );
    }

    @Test
    public void testIsFamily()
    {
        assertThat( "Family is os/2"
                  , Os.isFamily( Os.FAMILY_OS2 )
                  , is( true ) );

        assertThat( "Family is not mac"
                  , Os.isFamily( Os.FAMILY_MAC )
                  , is( false ) );
    }

    @Test
    public void testIsName()
    {
        assertThat( "Name is os/2"
                  , Os.isName( "os/2" )
                  , is( true ) );

        assertThat( "Name is not Mac OS X"
                  , Os.isName( "Mac OS X" )
                  , is( false ) );
    }

    @Test
    public void testIsValidFamily()
    {
        assertThat( "os/2 isValidFamily"
                  , Os.isValidFamily( Os.FAMILY_OS2 )
                  , is( true ) );

        assertThat( "iPone != isValidFamily"
                  , Os.isValidFamily( "iPhone" )
                  , is( false ) );
    }

    @Test
    public void testIsVersion()
    {
        assertThat( "isVersion"
                  , Os.isVersion( "2.1.32" )
                  , is( true ) );

        assertThat( "isVersion"
                  , Os.isVersion( "2.1" )
                  , is( false ) );

        assertThat( "isVersion"
                  , Os.isVersion( "4.5" )
                  , is( false ) );

    }
}
