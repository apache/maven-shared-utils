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

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert;
import org.junit.rules.TemporaryFolder;


import java.io.File;

import static org.hamcrest.CoreMatchers.is;


/**
 * Test the {@link PathTool} class.
 *
 * We don't need to test this
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class PathToolTest extends Assert
{

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testCalculateLink()
    {
        assertThat( PathTool.calculateLink( "/index.html", "../.." )
                  , is( "../../index.html" ) );

        assertThat( PathTool.calculateLink( "http://plexus.codehaus.org/plexus-utils/index.html", "../.." )
                  , is( "http://plexus.codehaus.org/plexus-utils/index.html" ) );

        assertThat( PathTool.calculateLink( "/usr/local/java/bin/java.sh", "../.." )
                  , is( "../../usr/local/java/bin/java.sh" ) );

        assertThat( PathTool.calculateLink( "../index.html", "/usr/local/java/bin" )
                  , is( "/usr/local/java/bin/../index.html" ) );

        assertThat( PathTool.calculateLink( "../index.html", "http://plexus.codehaus.org/plexus-utils" )
                  , is( "http://plexus.codehaus.org/plexus-utils/../index.html" ) );
    }

    @Test
    public void testGetDirectoryComponent()
    {
        assertThat( PathTool.getDirectoryComponent( null )
                  , is( "" ) );

        assertThat( PathTool.getDirectoryComponent( "/usr/local/java/bin" )
                  , is( "/usr/local/java" ) );

        assertThat( PathTool.getDirectoryComponent( "/usr/local/java/bin/" )
                  , is( "/usr/local/java/bin" ) );

        assertThat( PathTool.getDirectoryComponent( "/usr/local/java/bin/java.sh" )
                  , is( "/usr/local/java/bin" ) );
    }

    @Test
    public void testGetRelativeFilePath()
    {
        assertThat( PathTool.getRelativeFilePath( null, null )
                  , is( "" ) );

        assertThat( PathTool.getRelativeFilePath( null, "/usr/local/java/bin" )
                  , is( "" ) );

        assertThat( PathTool.getRelativeFilePath( "/usr/local", null )
                  , is( "" ) );

        assertThat( PathTool.getRelativeFilePath( "/usr/local", "/usr/local/java/bin" )
                  , is( "java/bin" ) );

        assertThat( PathTool.getRelativeFilePath( "/usr/local", "/usr/local/java/bin/" )
                  , is( "java/bin/" ) );

        assertThat( PathTool.getRelativeFilePath( "/usr/local/java/bin", "/usr/local/" )
                  , is( "../../" ) );

        assertThat( PathTool.getRelativeFilePath( "/usr/local/", "/usr/local/java/bin/java.sh" )
                  , is( "java/bin/java.sh" ) );

        assertThat( PathTool.getRelativeFilePath( "/usr/local/java/bin/java.sh", "/usr/local/" )
                  , is( "../../../" ) );

        assertThat( PathTool.getRelativeFilePath( "/usr/local/", "/bin" )
                  , is( "../../bin" ) );

        assertThat( PathTool.getRelativeFilePath( "/bin", "/usr/local/" )
                  , is( "../usr/local/" ) );
    }

    @Test
    public void testGetRelativePath_2parm()
    {
        assertThat( PathTool.getRelativePath( null, null )
                  , is( "" ) );

        assertThat( PathTool.getRelativePath( null, "/usr/local/java/bin" )
                  , is( "" ) );

        assertThat( PathTool.getRelativePath( "/usr/local/", null )
                  , is( "" ) );

        assertThat( PathTool.getRelativePath( "/usr/local/", "/usr/local/java/bin" )
                  , is( ".." ) );

        assertThat( PathTool.getRelativePath( "/usr/local/", "/usr/local/java/bin/java.sh" )
                  , is( "../.." ) );

        assertThat( PathTool.getRelativePath( "/usr/local/java/bin/java.sh", "/usr/local/" )
                  , is( "" ) );
    }

    @Test
    public void testGetRelativePath_1parm()
    {
        assertThat( PathTool.getRelativePath( null )
                  , is( "" ) );

        File baseFolder = tempFolder.newFolder( "pathtooltest" );

        String folderName = "anotherFolders";
        File newDir = new File( baseFolder, folderName );
        newDir.mkdirs();


        assertThat( PathTool.getRelativePath( folderName )
                  , is( "." ) );
    }

    @Test
    public void testGetRelativeWebPath()
    {
        assertThat( PathTool.getRelativeWebPath( null, null )
                  , is( "" ) );

        assertThat( PathTool.getRelativeWebPath( null, "http://plexus.codehaus.org/" )
                  , is( "" ) );

        assertThat( PathTool.getRelativeWebPath( "http://plexus.codehaus.org/", null )
                  , is( "" ) );

        assertThat( PathTool.getRelativeWebPath( "http://plexus.codehaus.org/"
                                               , "http://plexus.codehaus.org/plexus-utils/index.html" )
                  , is( "plexus-utils/index.html" ) );

        assertThat( PathTool.getRelativeWebPath( "http://plexus.codehaus.org/plexus-utils/index.html"
                                               , "http://plexus.codehaus.org/" )
                  , is( "../../" ) );
    }

    @Test
    public void testUppercaseDrive()
    {
        assertThat( PathTool.uppercaseDrive( null )
                , CoreMatchers.<Object>nullValue() );

        assertThat( PathTool.uppercaseDrive( "d:" )
                , is( "D:" ) );

        assertThat( PathTool.uppercaseDrive( "D:" )
                , is( "D:" ) );

        assertThat( PathTool.uppercaseDrive( "/notadrive" )
                , is( "/notadrive" ) );
    }

}
