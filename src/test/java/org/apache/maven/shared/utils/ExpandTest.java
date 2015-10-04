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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.maven.shared.utils.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.*;

/**
 * This will test the plexus utility class {@link Expand}.
 *
 * Most of this stuff will be obsolete because java-1.4.2
 * introduced a java.util.zip package which works like a charm.
 *
 * We of course need to implement this class due to compatibility
 * reasons.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class ExpandTest
    extends Assert
{

    private static final String TEST_ZIP_LOCATION = "/expand/expand_test.zip";
    private static final String TEST_ZIP_TARGET_FOLDER = "expand_test_target/";

    private static final String TEST_UNZIPPED_FILE = "expand_test/test_file.txt";
    private static final String TEST_UNZIPPED_CONTENT = "TestContent";


    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File getSourceFile()
    {
        URL zipFileUrl = getClass().getResource( TEST_ZIP_LOCATION );

        assertNotNull( zipFileUrl );

        return new File( zipFileUrl.getFile() );
    }

    /**
     * Create a clean target directory for unzipping.
     * If it did exist, then clean it first.
     *
     * @return The target folder.
     */
    private File getTestTargetDir()
        throws Exception
    {
        return tempFolder.newFolder( TEST_ZIP_TARGET_FOLDER );
    }

    @Test
    public void testSetDest_No_NPE()
    {
        Expand expand = new Expand();
        expand.setDest( null );
    }

    @Test
    public void testSetSrc_No_NPE()
    {
        Expand expand = new Expand();
        expand.setSrc( null );
    }

    @Test
    public void testExecute()
        throws Exception
    {
        Expand expand = new Expand();

        File source = getSourceFile();
        expand.setSrc( source );

        File targetDir = getTestTargetDir();
        expand.setDest( targetDir );

        expand.execute();

        verifyExpandedFileAndContent( targetDir, TEST_UNZIPPED_CONTENT );
    }

    @Test
    public void testExecuteIntoNonexistingDirectory()
        throws Exception
    {
        Expand expand = new Expand();

        File source = getSourceFile();
        expand.setSrc( source );

        File nonexisingDir = new File( getTestTargetDir(), "nonexisting_dir" );

        if ( nonexisingDir.exists() )
        {
            FileUtils.deleteDirectory( nonexisingDir );
        }

        expand.setDest( nonexisingDir );

        expand.execute();

        verifyExpandedFileAndContent( nonexisingDir, TEST_UNZIPPED_CONTENT );
    }

    @Test
    public void testExecuteNonexistingSource()
        throws Exception
    {
        Expand expand = new Expand();

        File nonexistingSource = new File( "target/expand_test_target/nonexisting_source_file.nixda" );
        expand.setSrc( nonexistingSource );

        File targetDir = getTestTargetDir();
        expand.setDest( targetDir );

        try
        {
            expand.execute();
            fail( "expand with notexiting source must throw Exception!" );
        }
        catch ( Exception e )
        {
            Throwable cause = ExceptionUtils.getRootCause( e );
            if ( cause == null )
            {
                cause = e;
            }

            assertTrue( "cause must be a FileNotFoundException", cause instanceof FileNotFoundException );
        }

    }

    @Test( expected = NullPointerException.class )
    public void testExecute_NullSource()
        throws Exception
    {
        Expand expand = new Expand();
        expand.setSrc( null );

        File targetDir = getTestTargetDir();
        expand.setDest( targetDir );

        expand.execute();
    }

    @Test
    public void testExecute_NullDest()
        throws Exception
    {
        Expand expand = new Expand();
        expand.setSrc( getSourceFile() );

        // execute without a dest directory seems to
        // expand all the archive into the current working directory
        expand.setDest( null );

        String oldWorkingDirectory = System.getProperty( "user.dir" );

        try
        {
            File targetDir = getTestTargetDir();
            System.setProperty( "user.dir", targetDir.getAbsolutePath() );

            expand.execute();

            verifyExpandedFileAndContent( targetDir, TEST_UNZIPPED_CONTENT );
        }
        finally
        {
            System.setProperty( "user.dir", oldWorkingDirectory );
        }
    }

    @Test
    public void testExecute_Overwrite()
        throws Exception
    {
        File targetDir = getTestTargetDir();
        File expandedFile = null;

        {
            // part1: expand

            Expand expand = new Expand();

            File source = getSourceFile();
            expand.setSrc( source );

            expand.setDest( targetDir );

            expand.execute();

            expandedFile = verifyExpandedFile( targetDir );
        }

        // turn the clock back 10 seconds
        long time = System.currentTimeMillis() - 10000L;

        // round down to 1s;
        time = time - time % 1000L;

        expandedFile.setLastModified( time );
        assertEquals( time, expandedFile.lastModified() );

        {
            // part2: expand in non-overwrite mode

            Expand expand = new Expand();

            File source = getSourceFile();
            expand.setSrc( source );
            expand.setDest( targetDir );

            expand.setOverwrite( false );

            expand.execute();

            expandedFile = verifyExpandedFile( targetDir );

            assertEquals( "file must still have the old lastModified timestamp"
                        , time, expandedFile.lastModified() );

        }

        {
            // part3: expand in overwrite mode but local file is still newer than the one in the archive

            Expand expand = new Expand();

            File source = getSourceFile();
            expand.setSrc( source );
            expand.setDest( targetDir );

            expand.setOverwrite( true );

            expand.execute();

            expandedFile = verifyExpandedFile( targetDir );

            // obviously the file will be overwritten anyway
            assertTrue( "file must now have the original old lastModified timestamp, but was: time=" + time
                        + " expandedFile.lastModified()= " + expandedFile.lastModified()
                        , time > expandedFile.lastModified() );
        }

        // turn the clock back a loooong time!
        time = 100000000L;

        expandedFile.setLastModified( time );
        assertEquals( time, expandedFile.lastModified() );

        {
            // part3: expand in overwrite mode but local file is now older than the one in the archive

            Expand expand = new Expand();

            File source = getSourceFile();
            expand.setSrc( source );
            expand.setDest( targetDir );

            expand.setOverwrite( true );

            expand.execute();

            expandedFile = verifyExpandedFile( targetDir );

            assertTrue( "file must now have newer lastModified timestamp, but was: time=" + time
                        + " expandedFile.lastModified()= " + expandedFile.lastModified()
                        , time < expandedFile.lastModified() );

        }
    }


    private File verifyExpandedFile( File targetDir )
    {
        assertThat( "target directory must exist"
                  , targetDir.exists()
                  , is( true ) );

        File expandedFile = new File( targetDir, TEST_UNZIPPED_FILE );

        assertThat( "expanded file must exist: " + expandedFile.getAbsolutePath()
                  , expandedFile.exists()
                  , is( true ) );

        return expandedFile;
    }

    private void verifyExpandedFileAndContent( File targetDir, String expectedContent )
            throws FileNotFoundException
    {
        File expandedFile = verifyExpandedFile( targetDir );

        assertNotNull( expandedFile );

        java.util.Scanner scanner = new java.util.Scanner( expandedFile ).useDelimiter( "\n" );
        String text = scanner.next();

        assertThat( "expanded file content must match"
                  , text
                  , is( expectedContent ) );
    }
}
