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

import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests the 'Os' class which evaluates operation system specific settings.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class OsTest
{
    private String origOsName;
    private String origOsArch;
    private String origOsVersion;


    @Before
    public void setUp()
    {
        origOsName = System.getProperty( "os.name" );
        origOsArch = System.getProperty( "os.arch" );
        origOsVersion = System.getProperty( "os.version" );

        // and now set some special settings ;)
        System.setProperty( "os.name"   , "os/2" );
        System.setProperty( "os.arch"   , "i386" );
        System.setProperty( "os.version", "2.1.32" );
    }

    @After
    public void tearDown()
    {
        // set the original OS settings again
        System.setProperty( "os.name"   , origOsName );
        System.setProperty( "os.arch"   , origOsArch );
        System.setProperty( "os.version", origOsVersion );
    }

    @Test
    public void testConstructor()
    {
        Os os  = new Os();
        os.eval();

        Assert.assertTrue( Os.isName( Os.FAMILY_OS2 ) );

        Assert.assertFalse( Os.isName( Os.FAMILY_DOS ) );
        Assert.assertFalse( Os.isName( Os.FAMILY_MAC ) );
        Assert.assertFalse( Os.isName( Os.FAMILY_NETWARE ) );
        Assert.assertFalse( Os.isName( Os.FAMILY_OPENVMS ) );
        Assert.assertFalse( Os.isName( Os.FAMILY_OS400 ) );
        Assert.assertFalse( Os.isName( Os.FAMILY_TANDEM ) );
        Assert.assertFalse( Os.isName( Os.FAMILY_UNIX ) );
        Assert.assertFalse( Os.isName( Os.FAMILY_WIN9X ) );
        Assert.assertFalse( Os.isName( Os.FAMILY_WINDOWS ) );
        Assert.assertFalse( Os.isName( Os.FAMILY_ZOS ) );
    }

    @Test
    public void testFamilyNames()
    {
        Assert.assertEquals( Os.FAMILY_DOS, "dos" );
        Assert.assertEquals( Os.FAMILY_MAC, "mac" );
        Assert.assertEquals( Os.FAMILY_NETWARE, "netware" );
        Assert.assertEquals( Os.FAMILY_OPENVMS, "openvms" );
        Assert.assertEquals( Os.FAMILY_OS2, "os/2" );
        Assert.assertEquals( Os.FAMILY_OS400, "os/400" );
        Assert.assertEquals( Os.FAMILY_TANDEM, "tandem" );
        Assert.assertEquals( Os.FAMILY_UNIX, "unix" );
        Assert.assertEquals( Os.FAMILY_WIN9X, "win9x" );
        Assert.assertEquals( Os.FAMILY_WINDOWS, "windows" );
        Assert.assertEquals( Os.FAMILY_ZOS, "z/os" );
    }

    @Test
    public void testGetValidFamilies()
    {
        Set<String> osFamilies = Os.getValidFamilies();

        Assert.assertTrue( "OsFamilies Set size"
                  , osFamilies.size() >= 11 );
        
        Assert.assertTrue( osFamilies.contains( Os.FAMILY_DOS ) );
        Assert.assertTrue( osFamilies.contains( Os.FAMILY_MAC ) );
        Assert.assertTrue( osFamilies.contains( Os.FAMILY_NETWARE ) );
        Assert.assertTrue( osFamilies.contains( Os.FAMILY_OPENVMS ) );
        Assert.assertTrue( osFamilies.contains( Os.FAMILY_OS2 ) );
        Assert.assertTrue( osFamilies.contains( Os.FAMILY_OS400 ) );
        Assert.assertTrue( osFamilies.contains( Os.FAMILY_TANDEM ) );
        Assert.assertTrue( osFamilies.contains( Os.FAMILY_UNIX ) );
        Assert.assertTrue( osFamilies.contains( Os.FAMILY_WIN9X ) );
        Assert.assertTrue( osFamilies.contains( Os.FAMILY_WINDOWS ) );
        Assert.assertTrue( osFamilies.contains( Os.FAMILY_ZOS ) );
        
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
