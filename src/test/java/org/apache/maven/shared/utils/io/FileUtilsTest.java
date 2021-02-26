package org.apache.maven.shared.utils.io;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeThat;

import org.apache.commons.io.IOUtils;
import org.apache.maven.shared.utils.Os;
import org.apache.maven.shared.utils.testhelpers.FileTestHelper;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import javax.annotation.Nonnull;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

/**
 * This is used to test FileUtils for correctness.
 *
 * @author Peter Donald
 * @author Matthew Hawthorne
 * @author Stephen Colebourne
 * @author Jim Harrington
 * @version $Id: FileUtilsTestCase.java 1081025 2011-03-13 00:45:10Z niallp $
 * @see FileUtils
 */
@SuppressWarnings( "deprecation" )
public class FileUtilsTest
{

    // Test data

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public TestName name = new TestName();

    /**
     * Size of test directory.
     */
    private static final int TEST_DIRECTORY_SIZE = 0;

    private File testFile1;

    private File testFile2;

    private long testFile1Size;

    private long testFile2Size;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp()
        throws Exception
    {
        testFile1 = tempFolder.newFile( "file1-test.txt" );
        testFile2 = tempFolder.newFile( "file1a-test.txt" );

        testFile1Size = (int) testFile1.length();
        testFile2Size = (int) testFile2.length();

        tempFolder.getRoot().mkdirs();
        createFile( testFile1, testFile1Size );
        createFile( testFile2, testFile2Size );
        FileUtils.deleteDirectory( tempFolder.getRoot() );
        tempFolder.getRoot().mkdirs();
        createFile( testFile1, testFile1Size );
        createFile( testFile2, testFile2Size );
    }

    private static void createFile( File file, long size )
        throws IOException
    {
        if ( !file.getParentFile().exists() )
        {
            throw new IOException( "Cannot create file " + file + " as the parent directory does not exist" );
        }
        
        try (OutputStream out = new BufferedOutputStream( new FileOutputStream( file ) ) )
        {
            FileTestHelper.generateTestData( out, size );
        }
    }


    /**
     * Assert that the content of a file is equal to that in a byte[].
     */
    private void assertEqualContent( byte[] b0, File file )
        throws IOException
    {
        int count = 0, numRead = 0;
        byte[] b1 = new byte[b0.length];
        try ( InputStream is = new FileInputStream( file ) )
        {
            while ( count < b0.length && numRead >= 0 )
            {
                numRead = is.read( b1, count, b0.length );
                count += numRead;
            }
            assertThat( "Different number of bytes: ", count, is( b0.length ) );
            for ( int i = 0; i < count; i++ )
            {
                assertEquals( "byte " + i + " differs", b1[i], b0[i] );
            }
        }
    }

    private void deleteFile( File file )
    {
        if ( file.exists() )
        {
            assertTrue( "Couldn't delete file: " + file, file.delete() );
        }
    }


    //-----------------------------------------------------------------------
    @Test
    public void toFile1()
        throws Exception
    {
        URL url = new URL( "file", null, "a/b/c/file.txt" );
        File file = FileUtils.toFile( url );
        assertThat( file.toString(), containsString( "file.txt" ) );
    }

    @Test
    public void toFile2()
        throws Exception
    {
        URL url = new URL( "file", null, "a/b/c/file%20n%61me%2520.tx%74" );
        File file = FileUtils.toFile( url );
        assertThat( file.toString(), containsString( "file name%20.txt" ) );
    }

    @Test
    public void toFile3()
        throws Exception
    {
        assertThat( FileUtils.toFile( null ), CoreMatchers.nullValue() );
        assertThat( FileUtils.toFile( new URL( "http://jakarta.apache.org" ) ), CoreMatchers.nullValue() );
    }

    @Test( expected = NumberFormatException.class )
    public void toFile4()
        throws Exception
    {
        URL url = new URL( "file", null, "a/b/c/file%%20%me.txt%" );
        File file = FileUtils.toFile( url );
        assertThat( file.toString(), containsString( "file% %me.txt%" ) );
    }

    /**
     * IO-252
     */
    @Test
    public void toFile5()
        throws Exception
    {
        URL url = new URL( "file", null, "both%20are%20100%20%25%20true" );
        File file = FileUtils.toFile( url );
        assertThat( file.toString(), is( "both are 100 % true" ) );
    }

    @Test
    public void toFileUtf8()
        throws Exception
    {
        URL url = new URL( "file", null, "/home/%C3%A4%C3%B6%C3%BC%C3%9F" );
        File file = FileUtils.toFile( url );
        assertThat( file.toString(), not( containsString( "\u00E4\u00F6\u00FC\u00DF" ) ) );
    }

    // toURLs

    @Test
    public void toURLs1()
        throws Exception
    {
        File[] files = new File[]{ new File( tempFolder.getRoot(), "file1.txt" ), new File( tempFolder.getRoot(), "file2.txt" ),
            new File( tempFolder.getRoot(), "test file.txt" ), };
        URL[] urls = FileUtils.toURLs( files );

        assertThat( urls.length, is( files.length ) );
        assertThat( urls[0].toExternalForm().startsWith( "file:" ), is( true ) );
        assertThat( urls[0].toExternalForm().contains( "file1.txt" ), is( true ) );
        assertThat( urls[1].toExternalForm().startsWith( "file:" ), is( true ) );
        assertThat( urls[1].toExternalForm(), containsString( "file2.txt" ) );

        // Test escaped char
        assertThat( urls[2].toExternalForm().startsWith( "file:" ), is( true ) );
        assertThat( urls[2].toExternalForm(), containsString( "test%20file.txt" ) );
    }

    // contentEquals

    @Test
    public void contentEquals()
        throws Exception
    {
        // Non-existent files
        File file = new File( tempFolder.getRoot(), name.getMethodName() );
        File file2 = new File( tempFolder.getRoot(), name.getMethodName() + "2" );
        // both don't  exist
        assertThat( FileUtils.contentEquals( file, file ), is( true ) );
        assertThat( FileUtils.contentEquals( file, file2 ), is( true ) );
        assertThat( FileUtils.contentEquals( file2, file2 ), is( true ) );
        assertThat( FileUtils.contentEquals( file2, file ), is( true ) );

        // Directories
        FileUtils.contentEquals( tempFolder.getRoot(), tempFolder.getRoot() );

        // Different files
        File objFile1 = new File( tempFolder.getRoot(), name.getMethodName() + ".object" );
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile( getClass().getResource( "/java/lang/Object.class" ), objFile1 );

        File objFile1b = new File( tempFolder.getRoot(), name.getMethodName() + ".object2" );
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile( getClass().getResource( "/java/lang/Object.class" ), objFile1b );

        File objFile2 = new File( tempFolder.getRoot(), name.getMethodName() + ".collection" );
        objFile2.deleteOnExit();
        FileUtils.copyURLToFile( getClass().getResource( "/java/util/Collection.class" ), objFile2 );

        assertThat( FileUtils.contentEquals( objFile1, objFile2 ), is( false ) );
        assertThat( FileUtils.contentEquals( objFile1b, objFile2 ), is( false ) );
        assertThat( FileUtils.contentEquals( objFile1, objFile1b ), is( true ) );

        assertThat( FileUtils.contentEquals( objFile1, objFile1 ), is( true ) );
        assertThat( FileUtils.contentEquals( objFile1b, objFile1b ), is( true ) );
        assertThat( FileUtils.contentEquals( objFile2, objFile2 ), is( true ) );

        // Equal files
        file.createNewFile();
        file2.createNewFile();
        assertThat( FileUtils.contentEquals( file, file ), is( true ) );
        assertThat( FileUtils.contentEquals( file, file2 ), is( true ) );
    }

    // copyURLToFile

    @Test
    public void copyURLToFile()
        throws Exception
    {
        // Creates file
        File file = new File( tempFolder.getRoot(), name.getMethodName() );
        file.deleteOnExit();

        // Loads resource
        String resourceName = "/java/lang/Object.class";
        FileUtils.copyURLToFile( getClass().getResource( resourceName ), file );

        // Tests that resource was copied correctly
        try ( FileInputStream fis = new FileInputStream( file ) )
        {
            assertThat( "Content is not equal.",
                        IOUtil.contentEquals( getClass().getResourceAsStream( resourceName ), fis ), is( true ) );
        }
        //TODO Maybe test copy to itself like for copyFile()
    }

    // forceMkdir

    @Test
    public void forceMkdir()
        throws Exception
    {
        // Tests with existing directory
        FileUtils.forceMkdir( tempFolder.getRoot() );

        // Creates test file
        File testFile = new File( tempFolder.getRoot(), name.getMethodName() );
        testFile.deleteOnExit();
        testFile.createNewFile();
        assertThat( "Test file does not exist.", testFile.exists(), is( true ) );

        // Tests with existing file
        try
        {
            FileUtils.forceMkdir( testFile );
            fail( "Exception expected." );
        }
        catch ( IOException ex )
        {
        }

        testFile.delete();

        // Tests with non-existent directory
        FileUtils.forceMkdir( testFile );
        assertThat( "Directory was not created.", testFile.exists(), is( true ) );
    }

    // sizeOfDirectory

    @Test
    public void sizeOfDirectory()
        throws Exception
    {
        File file = new File( tempFolder.getRoot(), name.getMethodName() );

        // Non-existent file
        try
        {
            FileUtils.sizeOfDirectory( file );
            fail( "Exception expected." );
        }
        catch ( IllegalArgumentException ex )
        {
        }

        // Creates file
        file.createNewFile();
        file.deleteOnExit();

        // Existing file
        try
        {
            FileUtils.sizeOfDirectory( file );
            fail( "Exception expected." );
        }
        catch ( IllegalArgumentException ex )
        {
        }

        // Existing directory
        file.delete();
        file.mkdir();

        assertThat( "Unexpected directory size", FileUtils.sizeOfDirectory( file ), is( (long) TEST_DIRECTORY_SIZE ) );
    }

    // copyFile

    @Test
    public void copyFile1()
        throws Exception
    {
        File destination = new File( tempFolder.getRoot(), "copy1.txt" );

        //Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if
        //the lastModified date is not ok

        FileUtils.copyFile( testFile1, destination );
        assertThat( "Check Exist", destination.exists(), is( true ) );
        assertThat( "Check Full copy", destination.length(), is( testFile1Size ) );
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/
    }

    /** A time today, rounded down to the previous minute */
    private static long MODIFIED_TODAY = (System.currentTimeMillis() / TimeUnit.MINUTES.toMillis( 1 )) * TimeUnit.MINUTES.toMillis( 1 );

    /** A time yesterday, rounded down to the previous minute */
    private static long MODIFIED_YESTERDAY = MODIFIED_TODAY - TimeUnit.DAYS.toMillis( 1 );

    /** A time last week, rounded down to the previous minute */
    private static long MODIFIED_LAST_WEEK = MODIFIED_TODAY - TimeUnit.DAYS.toMillis( 7 );

    @Test
    public void copyFileWithNoFiltersAndNoDestination()
        throws Exception
    {
        File from = write(
            "from.txt",
            MODIFIED_YESTERDAY,
            "Hello World!"
        );
        File to = new File(
            tempFolder.getRoot(),
            "to.txt"
        );

        FileUtils.copyFile( from, to, null, ( FileUtils.FilterWrapper[] ) null);

        assertTrue(
            "to.txt did not exist so should have been written",
            to.lastModified() >= MODIFIED_TODAY
        );
        assertFileContent( to, "Hello World!" );
    }

    @Test
    public void copyFileWithNoFiltersAndOutdatedDestination()
            throws Exception
    {
        File from = write(
            "from.txt",
            MODIFIED_YESTERDAY,
            "Hello World!"
        );
        File to = write(
            "to.txt",
            MODIFIED_LAST_WEEK,
            "Older content"
        );

        FileUtils.copyFile( from, to, null, ( FileUtils.FilterWrapper[] ) null);

        assertTrue(
            "to.txt was outdated so should have been overwritten",
            to.lastModified() >= MODIFIED_TODAY
        );
        assertFileContent( to, "Hello World!" );
    }

    @Test
    public void copyFileWithNoFiltersAndNewerDestination()
            throws Exception
    {
        File from = write(
            "from.txt",
            MODIFIED_LAST_WEEK,
            "Hello World!"
        );
        File to = write(
            "to.txt",
            MODIFIED_YESTERDAY,
            "Older content"
        );

        FileUtils.copyFile( from, to, null, ( FileUtils.FilterWrapper[] ) null);

        assertTrue(
            "to.txt was newer so should have been left alone",
            to.lastModified() < MODIFIED_TODAY
        );
        assertFileContent( to, "Older content" );
    }

    @Test
    public void copyFileWithNoFiltersAndNewerDestinationButForcedOverwrite()
            throws Exception
    {
        File from = write(
            "from.txt",
            MODIFIED_LAST_WEEK,
            "Hello World!"
        );
        File to = write(
            "to.txt",
            MODIFIED_YESTERDAY,
            "Older content"
        );

        FileUtils.copyFile( from, to, null, null, true);

        assertTrue(
            "to.txt was newer but the overwrite should have been forced",
            to.lastModified() >= MODIFIED_TODAY
        );
        assertFileContent( to, "Hello World!" );
    }

    @Test
    public void copyFileWithFilteringButNoFilters()
            throws Exception
    {
        File from = write(
            "from.txt",
            MODIFIED_YESTERDAY,
            "Hello ${name}!"
        );
        File to = write(
            "to.txt",
            MODIFIED_LAST_WEEK,
            "Older content"
        );

        FileUtils.copyFile( from, to, null );

        assertTrue(
            "to.txt was outdated so should have been overwritten",
            to.lastModified() >= MODIFIED_TODAY
        );
        assertFileContent( to, "Hello ${name}!" );
    }

    @Test
    public void copyFileWithFilteringAndNoDestination()
            throws Exception
    {
        File from = write(
            "from.txt",
            MODIFIED_YESTERDAY,
            "Hello ${name}!"
        );
        File to = new File(
            tempFolder.getRoot(),
            "to.txt"
        );

        FileUtils.copyFile( from, to, null, wrappers( "name", "Bob" ) );

        assertTrue(
            "to.txt did not exist so should have been written",
            to.lastModified() >= MODIFIED_TODAY
        );
        assertFileContent( to, "Hello Bob!" );
    }

    @Test
    public void copyFileWithFilteringAndOutdatedDestination()
            throws Exception
    {
        File from = write(
            "from.txt",
            MODIFIED_YESTERDAY,
            "Hello ${name}!"
        );
        File to = write(
            "to.txt",
            MODIFIED_LAST_WEEK,
            "Older content"
        );

        FileUtils.copyFile( from, to, null, wrappers( "name", "Bob" ) );

        assertTrue(
            "to.txt was outdated so should have been overwritten",
            to.lastModified() >= MODIFIED_TODAY
        );
        assertFileContent( to, "Hello Bob!" );
    }

    @Test
    public void copyFileWithFilteringAndNewerDestinationButForcedOverwrite()
            throws Exception
    {
        File from = write(
            "from.txt",
            MODIFIED_LAST_WEEK,
            "Hello ${name}!"
        );
        File to = write(
            "to.txt",
            MODIFIED_YESTERDAY,
            "Older content"
        );

        FileUtils.copyFile( from, to, null, wrappers( "name", "Bob" ), true );

        assertTrue(
            "to.txt was newer but the overwrite should have been forced",
            to.lastModified() >= MODIFIED_TODAY
        );
        assertFileContent( to, "Hello Bob!" );
    }

    @Test
    public void copyFileWithFilteringAndNewerDestinationButModifiedContent()
            throws Exception
    {
        File from = write(
            "from.txt",
            MODIFIED_LAST_WEEK,
            "Hello ${name}!"
        );
        File to = write(
            "to.txt",
            MODIFIED_YESTERDAY,
            "Hello Charlie!"
        );

        FileUtils.copyFile( from, to, null, wrappers( "name", "Bob" ) );

        assertTrue(
            "to.txt was outdated so should have been overwritten",
            to.lastModified() >= MODIFIED_TODAY
        );
        assertFileContent( to, "Hello Bob!" );
    }

    @Test
    public void copyFileWithFilteringAndNewerDestinationAndMatchingContent()
            throws Exception
    {
        File from = write(
            "from.txt",
            MODIFIED_LAST_WEEK,
            "Hello ${name}!"
        );
        File to = write(
            "to.txt",
            MODIFIED_YESTERDAY,
            "Hello Bob!"
        );

        FileUtils.copyFile( from, to, null, wrappers( "name", "Bob" ) );

        assertFileContent( to, "Hello Bob!" );
        assertTrue(
            "to.txt content should be unchanged and have been left alone",
            to.lastModified() < MODIFIED_TODAY
        );
    }

    private static FileUtils.FilterWrapper[] wrappers( String key, String value )
    {
        final Map<String, Object> map = new HashMap<>();
        map.put( key, value );
        return new FileUtils.FilterWrapper[]
            {
                new FileUtils.FilterWrapper()
                {
                    @Override
                    public Reader getReader( Reader reader )
                    {
                        return new InterpolationFilterReader( reader, map );
                    }
                }
            };
    }

    private File write( @Nonnull String name, long lastModified, @Nonnull String text) throws IOException
    {
        final File file = new File( tempFolder.getRoot(), name );
        try ( final Writer writer = new FileWriter( file ) ) {
            writer.write(text);
        }
        assertTrue( file.setLastModified( lastModified ) );
        assertEquals( "Failed to set lastModified date on " + file.getPath(), lastModified, file.lastModified() );
        return file;
    }

    private static void assertFileContent( @Nonnull File file, @Nonnull String expected ) throws IOException
    {
        try ( Reader in = new FileReader( file ))
        {
            assertEquals(
                "Expected " + file.getPath() + " to contain: " + expected,
                expected,
                IOUtils.toString( in )
            );
        }
    }

    @Test
    public void copyFileThatIsSymlink()
            throws Exception
    {
        assumeFalse( Os.isFamily( Os.FAMILY_WINDOWS ) );

        File destination = new File( tempFolder.getRoot(), "symCopy.txt" );

        File testDir = SymlinkTestSetup.createStandardSymlinkTestDir( new File( "target/test/symlinkCopy" ) );

        FileUtils.copyFile( new File( testDir, "symR" ), destination );

        assertTrue( Files.isSymbolicLink( destination.toPath() ) );
    }


    @Test
    public void deleteFile()
        throws Exception
    {
        File destination = new File( tempFolder.getRoot(), "copy1.txt" );
        FileUtils.copyFile( testFile1, destination );
        FileUtils.delete( destination );
        assertThat( "Check Exist", destination.exists(), is( false ) );
    }

    @Test(expected = IOException.class)
    public void deleteFileNofile()
        throws Exception
    {
        File destination = new File( "abc/cde" );
        FileUtils.delete( destination );
    }

    @Test
    public void deleteFileLegacy()
        throws Exception
    {
        File destination = new File( tempFolder.getRoot(), "copy1.txt" );
        FileUtils.copyFile( testFile1, destination );
        assertTrue( FileUtils.deleteLegacyStyle( destination ) );
    }

    @Test
    public void deleteFileLegacyNofile()
        throws Exception
    {
        File destination = new File( "abc/cde" );
        assertFalse( FileUtils.deleteLegacyStyle( destination ) );
    }

    @Test
    public void copyFileWithPermissions()
        throws Exception
    {
        File source = new File( "src/test/resources/executable" );
        source.setExecutable( true );
        assumeThat( "Need an existing file to copy", source.exists(), is( true ) );
        assumeThat( "Need an executable file to copy", source.canExecute(), is( true ) );

        File destination = new File( tempFolder.getRoot(), "executable-copy" );

        FileUtils.copyFile( source, destination );

        assertThat( "destination not exists: " + destination.getAbsolutePath()
                        + ", directory content: " + Arrays.asList( destination.getParentFile().list() ),
                    Files.exists( destination.toPath() ), is( true ) );

        assertThat( "Check copy executable", destination.canExecute(), is( true ) );
    }

    @Test
    public void copyFile2()
        throws Exception
    {
        File destination = new File( tempFolder.getRoot(), "copy2.txt" );

        //Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if
        //the lastModified date is not ok

        FileUtils.copyFile( testFile1, destination );
        assertThat( "Check Exist", destination.exists(), is( true ) );
        assertThat( "Check Full copy", destination.length(), is( testFile2Size ) );
        /* disabled: Thread.sleep doesn't work reliably for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/
    }

    @Test
    public void copyToSelf() throws IOException
    {
        File destination = new File( tempFolder.getRoot(), "copy3.txt" );
        //Prepare a test file
        FileUtils.copyFile( testFile1, destination );

        FileUtils.copyFile( destination, destination );
    }

    @Test
    public void copyDirectoryToNonExistingDest()
        throws Exception
    {
        createFile( testFile1, 1234 );
        createFile( testFile2, 4321 );
        File srcDir = tempFolder.getRoot();
        File subDir = new File( srcDir, "sub" );
        subDir.mkdir();
        File subFile = new File( subDir, "A.txt" );
        FileUtils.fileWrite( subFile, "UTF8", "HELLO WORLD" );
        File destDir = new File( System.getProperty( "java.io.tmpdir" ), "tmp-FileUtilsTestCase" );
        FileUtils.deleteDirectory( destDir );

        FileUtils.copyDirectory( srcDir, destDir );

        assertTrue( destDir.exists() );
        assertEquals( FileUtils.sizeOfDirectory( destDir ), FileUtils.sizeOfDirectory( srcDir ) );
        assertTrue( new File( destDir, "sub/A.txt" ).exists() );
        FileUtils.deleteDirectory( destDir );
    }

    @Test
    public void copyDirectoryToExistingDest() throws IOException
    {
        createFile( testFile1, 1234 );
        createFile( testFile2, 4321 );
        File srcDir = tempFolder.getRoot();
        File subDir = new File( srcDir, "sub" );
        assertTrue( subDir.mkdir() );
        File subFile = new File( subDir, "A.txt" );
        FileUtils.fileWrite( subFile, "UTF8", "HELLO WORLD" );
        File destDir = new File( System.getProperty( "java.io.tmpdir" ), "tmp-FileUtilsTestCase" );
        FileUtils.deleteDirectory( destDir );
        assertTrue ( destDir.mkdirs() );

        FileUtils.copyDirectory( srcDir, destDir );

        assertEquals( FileUtils.sizeOfDirectory( destDir ), FileUtils.sizeOfDirectory( srcDir ) );
        assertTrue( new File( destDir, "sub/A.txt" ).exists() );
    }

    @Test
    public void copyDirectoryErrors_nullDestination() throws IOException {
        try
        {
            FileUtils.copyDirectory( new File( "a" ), null );
            fail();
        }
        catch ( NullPointerException ex )
        {
        }
    }

    @Test
    public void copyDirectoryErrors_copyToSelf() {
        try
        {
            FileUtils.copyDirectory( tempFolder.getRoot(), tempFolder.getRoot() );
            fail();
        }
        catch ( IOException ex )
        {
        }
    }

    @Test
    public void copyDirectoryErrors()
        throws IOException
    {
        try
        {
            FileUtils.copyDirectory( null, null );
            fail();
        }
        catch ( NullPointerException ex )
        {
        }
        try
        {
            FileUtils.copyDirectory( null, new File( "a" ) );
            fail();
        }
        catch ( NullPointerException ex )
        {
        }
        try
        {
            FileUtils.copyDirectory( tempFolder.getRoot(), testFile1 );
            fail();
        }
        catch ( IOException ex )
        {
        }
    }

    // forceDelete

    @Test
    public void forceDeleteAFile1()
        throws Exception
    {
        File destination = new File( tempFolder.getRoot(), "copy1.txt" );
        destination.createNewFile();
        assertTrue( "Copy1.txt doesn't exist to delete", destination.exists() );
        FileUtils.forceDelete( destination );
        assertFalse( destination.exists() );
    }

    @Test
    public void forceDeleteAFile2()
        throws Exception
    {
        File destination = new File( tempFolder.getRoot(), "copy2.txt" );
        destination.createNewFile();
        assertThat( "Copy2.txt doesn't exist to delete", destination.exists(), is( true ) );
        FileUtils.forceDelete( destination );
        assertThat( "Check No Exist", !destination.exists(), is( true ) );
    }

    @Test
    @Ignore( "Commons test case that is failing for plexus" )
    public void forceDeleteAFile3()
        throws Exception
    {
        File destination = new File( tempFolder.getRoot(), "no_such_file" );
        assertThat( "Check No Exist", !destination.exists(), is( true ) );
        try
        {
            FileUtils.forceDelete( destination );
            fail( "Should generate FileNotFoundException" );
        }
        catch ( FileNotFoundException ignored )
        {
        }
    }

    // copyFileToDirectory

    @Test
    @Ignore( "Commons test case that is failing for plexus" )
    public void copyFile1ToDir()
        throws Exception
    {
        File directory = new File( tempFolder.getRoot(), "subdir" );
        if ( !directory.exists() )
        {
            directory.mkdirs();
        }
        File destination = new File( directory, testFile1.getName() );

        //Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if
        //the lastModified date is not ok

        FileUtils.copyFileToDirectory( testFile1, directory );
        assertThat( "Check Exist", destination.exists(), is( true ) );
        assertThat( "Check Full copy", destination.length(), is( testFile1Size ) );
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/

        try
        {
            FileUtils.copyFileToDirectory( destination, directory );
            fail( "Should not be able to copy a file into the same directory as itself" );
        }
        catch ( IOException ioe )
        {
            //we want that, cannot copy to the same directory as the original file
        }
    }

    @Test
    public void copyFile2ToDir()
        throws Exception
    {
        File directory = new File( tempFolder.getRoot(), "subdir" );
        if ( !directory.exists() )
        {
            directory.mkdirs();
        }
        File destination = new File( directory, testFile1.getName() );

        //Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if
        //the lastModified date is not ok

        FileUtils.copyFileToDirectory( testFile1, directory );
        assertThat( "Check Exist", destination.exists(), is( true ) );
        assertThat( "Check Full copy", destination.length(), is( testFile2Size ) );
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/
    }

    // forceDelete

    @Test
    public void forceDeleteDir()
        throws Exception
    {
        File testDirectory = tempFolder.newFolder( name.getMethodName() );
        FileUtils.forceDelete( testDirectory.getParentFile() );
        assertThat( "Check No Exist", !testDirectory.getParentFile().exists(), is( true ) );
    }

    /**
     * Test the FileUtils implementation.
     */
    @Test
    public void fileUtils()
        throws Exception
    {
        // Loads file from classpath
        File file1 = new File( tempFolder.getRoot(), "test.txt" );
        String filename = file1.getAbsolutePath();

        //Create test file on-the-fly (used to be in CVS)
        try ( OutputStream out = new java.io.FileOutputStream( file1 ) )
        {
            out.write( "This is a test".getBytes( "UTF-8" ) );
        }

        File file2 = new File( tempFolder.getRoot(), "test2.txt" );

        FileUtils.fileWrite( file2, "UTF-8", filename );
        assertThat( file2.exists(), is( true ) );
        assertThat( file2.length() > 0, is( true ) );

        String file2contents = FileUtils.fileRead( file2, "UTF-8" );
        assertThat( "Second file's contents correct", filename.equals( file2contents ), is( true ) );

        assertThat( file2.delete(), is( true ) );

        String contents = FileUtils.fileRead( new File( filename ), "UTF-8" );
        assertThat( "FileUtils.fileRead()", contents.equals( "This is a test" ), is( true ) );

    }

    @Test
    public void fileReadWithDefaultEncoding()
        throws Exception
    {
        File file = new File( tempFolder.getRoot(), "read.obj" );
        FileOutputStream out = new FileOutputStream( file );
        byte[] text = "Hello /u1234".getBytes();
        out.write( text );
        out.close();

        String data = FileUtils.fileRead( file );
        assertThat( data, is( "Hello /u1234" ) );
    }

    @Test
    public void fileReadWithEncoding()
        throws Exception
    {
        File file = new File( tempFolder.getRoot(), "read.obj" );
        FileOutputStream out = new FileOutputStream( file );
        byte[] text = "Hello /u1234".getBytes( "UTF8" );
        out.write( text );
        out.close();

        String data = FileUtils.fileRead( file, "UTF8" );
        assertThat( data, is( "Hello /u1234" ) );
    }

    @Test
    @Ignore( "Commons test case that is failing for plexus" )
    public void readLines()
        throws Exception
    {
        File file = FileTestHelper.newFile( tempFolder, "lines.txt" );
        try
        {
            String[] data = new String[]{ "hello", "/u1234", "", "this is", "some text" };
            FileTestHelper.createLineBasedFile( file, data );

            List<String> lines = FileUtils.loadFile( file );
            assertThat( lines, is( Arrays.asList( data ) ) );
        }
        finally
        {
            deleteFile( file );
        }
    }

    @Test
    public void writeStringToFile1()
        throws Exception
    {
        File file = new File( tempFolder.getRoot(), "write.txt" );
        FileUtils.fileWrite( file, "UTF8", "Hello /u1234" );
        byte[] text = "Hello /u1234".getBytes( "UTF8" );
        assertEqualContent( text, file );
    }

    @Test
    public void writeStringToFile2()
        throws Exception
    {
        File file = new File( tempFolder.getRoot(), "write.txt" );
        FileUtils.fileWrite( file, null, "Hello /u1234" );
        byte[] text = "Hello /u1234".getBytes();
        assertEqualContent( text, file );
    }

    @Test
    public void writeCharSequence1()
        throws Exception
    {
        File file = new File( tempFolder.getRoot(), "write.txt" );
        FileUtils.fileWrite( file, "UTF8", "Hello /u1234" );
        byte[] text = "Hello /u1234".getBytes( "UTF8" );
        assertEqualContent( text, file );
    }

    @Test
    public void writeCharSequence2()
        throws Exception
    {
        File file = new File( tempFolder.getRoot(), "write.txt" );
        FileUtils.fileWrite( file, null, "Hello /u1234" );
        byte[] text = "Hello /u1234".getBytes();
        assertEqualContent( text, file );
    }


    @Test
    public void writeStringToFileWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines()
        throws Exception
    {
        File file = FileTestHelper.newFile( tempFolder, "lines.txt" );
        FileUtils.fileWrite( file, null, "This line was there before you..." );

        FileUtils.fileAppend( file.getAbsolutePath(), "this is brand new data" );

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead( file );
        assertThat( actual, is( expected ) );
    }

    @Test
    public void writeStringToFile_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines()
        throws Exception
    {
        File file = FileTestHelper.newFile( tempFolder, "lines.txt" );
        FileUtils.fileWrite( file, null, "This line was there before you..." );

        FileUtils.fileAppend( file.getAbsolutePath(), "this is brand new data" );

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead( file );
        assertThat( actual, is( expected ) );
    }

    @Test
    public void writeStringArrayToFile()
            throws Exception
    {
        File file = new File( tempFolder.getRoot(), "writeArray.txt" );
        FileUtils.fileWriteArray( file, new String[]{"line1", "line2", "line3"} );

        byte[] text = "line1\nline2\nline3".getBytes( "UTF8" );
        assertEqualContent( text, file );
    }

    @Test
    public void writeStringArrayToFileWithEncoding()
            throws Exception
    {
        File file = new File( tempFolder.getRoot(), "writeArray.txt" );
        FileUtils.fileWriteArray( file, "UTF8", new String[]{"line1", "line2", "line3"} );

        byte[] text = "line1\nline2\nline3".getBytes( "UTF8" );
        assertEqualContent( text, file );
    }


    @Test
    public void writeWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines()
        throws Exception
    {
        File file = FileTestHelper.newFile( tempFolder, "lines.txt" );
        FileUtils.fileWrite( file, "UTF-8", "This line was there before you..." );

        FileUtils.fileAppend( file.getAbsolutePath(), "UTF-8", "this is brand new data" );

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead( file );
        assertThat( actual, is( expected ) );
    }

    @Test
    public void write_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines()
        throws Exception
    {
        File file = FileTestHelper.newFile( tempFolder, "lines.txt" );
        FileUtils.fileWrite( file, null, "This line was there before you..." );

        FileUtils.fileAppend( file.getAbsolutePath(), "this is brand new data" );

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead( file );
        assertThat( actual, is( expected ) );
    }

    @Test( expected = NullPointerException.class )
    public void blowUpOnNull()
        throws IOException
    {
        FileUtils.deleteDirectory( (File) null );
    }

    @Test
    public void deleteQuietlyDir()
        throws IOException
    {
        File testDirectory = new File( tempFolder.getRoot(), "testDeleteQuietlyDir" );
        File testFile = new File( testDirectory, "testDeleteQuietlyFile" );
        testDirectory.mkdirs();
        createFile( testFile, 0 );

        assertThat( testDirectory.exists(), is( true ) );
        assertThat( testFile.exists(), is( true ) );
        FileUtils.deleteDirectory( testDirectory );
        assertThat( "Check No Exist", testDirectory.exists(), is( false ) );
        assertThat( "Check No Exist", testFile.exists(), is( false ) );
    }

    @Test
    public void deleteQuietlyFile()
        throws IOException
    {
        File testFile = new File( tempFolder.getRoot(), "testDeleteQuietlyFile" );
        createFile( testFile, 0 );

        assertThat( testFile.exists(), is( true ) );
        FileUtils.deleteDirectory( testFile );
        assertThat( "Check No Exist", testFile.exists(), is( false ) );
    }

    @Test
    public void deleteQuietlyNonExistent()
        throws IOException
    {
        File testFile = new File( tempFolder.getRoot(), "testDeleteQuietlyNonExistent" );
        assertThat( testFile.exists(), is( false ) );

        FileUtils.deleteDirectory( testFile );
    }


    ////  getDefaultExcludes

    @Test
    public void getDefaultExcludes()
        throws Exception
    {
        assertThat( Arrays.asList( FileUtils.getDefaultExcludes() ), hasItems( MINIMUM_DEFAULT_EXCLUDES ) );
    }


    //// getDefaultExcludesAsList

    @Test
    public void getDefaultExcludesAsList()
        throws Exception
    {
        assertThat( FileUtils.getDefaultExcludesAsList(), hasItems( MINIMUM_DEFAULT_EXCLUDES ) );
    }


    //// getDefaultExcludesAsString

    @Test
    public void getDefaultExcludesAsString()
        throws Exception
    {
        assertThat( new HashSet<String>( Arrays.asList( FileUtils.getDefaultExcludesAsString().split( "," ) ) ),
                    hasItems( MINIMUM_DEFAULT_EXCLUDES ) );
    }



    //// dirname(String)

    @SuppressWarnings("ConstantConditions")
    @Test( expected = NullPointerException.class )
    public void nlowUpOnDirnameNull()
        throws Exception
    {
        FileUtils.dirname( null );
    }

    @Test
    public void dirnameEmpty()
        throws Exception
    {
        assertThat( FileUtils.dirname( "" ), is( "" ) );
    }

    @Test
    public void dirnameFilename()
        throws Exception
    {
        assertThat( FileUtils.dirname( "foo.bar.txt" ), is( "" ) );
    }

    @Test
    //X @ReproducesPlexusBug( "assumes that the path is a local path" )
    public void dirnameWindowsRootPathOnUnix()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '/' ) );
        assertThat( FileUtils.dirname( "C:\\foo.bar.txt" ), is( "" ) );
    }

    @Test
    //X @ReproducesPlexusBug( "assumes that the path is a local path" )
    public void dirnameWindowsNonRootPathOnUnix()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '/' ) );
        assertThat( FileUtils.dirname( "C:\\test\\foo.bar.txt" ), is( "" ) );
    }

    @Test
    //X @ReproducesPlexusBug( "assumes that the path is a local path" )
    public void dirnameUnixRootPathOnWindows()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '\\' ) );
        assertThat( FileUtils.dirname( "/foo.bar.txt" ), is( "" ) );
    }

    @Test
    //X @ReproducesPlexusBug( "assumes that the path is a local path" )
    public void dirnameUnixNonRootPathOnWindows()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '\\' ) );
        assertThat( FileUtils.dirname( "/test/foo.bar.txt" ), is( "" ) );
    }

    @Test
    public void dirnameWindowsRootPathOnWindows()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '\\' ) );
        assertThat( FileUtils.dirname( "C:\\foo.bar.txt" ), is( "C:" ) );
    }

    @Test
    public void dirnameWindowsNonRootPathOnWindows()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '\\' ) );
        assertThat( FileUtils.dirname( "C:\\test\\foo.bar.txt" ), is( "C:\\test" ) );
    }

    @Test
    public void dirnameUnixRootPathOnUnix()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '/' ) );
        assertThat( FileUtils.dirname( "/foo.bar.txt" ), is( "" ) );
    }

    @Test
    public void dirnameUnixNonRootPathOnUnix()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '/' ) );
        assertThat( FileUtils.dirname( "/test/foo.bar.txt" ), is( "/test" ) );
    }

    //// filename(String)

    @SuppressWarnings("ConstantConditions")
    @Test( expected = NullPointerException.class )
    public void blowUpOnFilenameNull()
        throws Exception
    {
        FileUtils.filename( null );
    }

    @Test
    public void filenameEmpty()
        throws Exception
    {
        assertThat( FileUtils.filename( "" ), is( "" ) );
    }

    @Test
    public void filenameFilename()
        throws Exception
    {
        assertThat( FileUtils.filename( "foo.bar.txt" ), is( "foo.bar.txt" ) );
    }

    @Test
    //X @ReproducesPlexusBug( "assumes that the path is a local path" )
    public void filenameWindowsRootPathOnUnix()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '/' ) );
        assertThat( FileUtils.filename( "C:\\foo.bar.txt" ), is( "C:\\foo.bar.txt" ) );
    }

    @Test
    //X @ReproducesPlexusBug( "assumes that the path is a local path" )
    public void filenameWindowsNonRootPathOnUnix()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '/' ) );
        assertThat( FileUtils.filename( "C:\\test\\foo.bar.txt" ), is( "C:\\test\\foo.bar.txt" ) );
    }

    @Test
    //X @ReproducesPlexusBug( "assumes that the path is a local path" )
    public void filenameUnixRootPathOnWindows()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '\\' ) );
        assertThat( FileUtils.filename( "/foo.bar.txt" ), is( "/foo.bar.txt" ) );
    }

    @Test
    //X @ReproducesPlexusBug( "assumes that the path is a local path" )
    public void filenameUnixNonRootPathOnWindows()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '\\' ) );
        assertThat( FileUtils.filename( "/test/foo.bar.txt" ), is( "/test/foo.bar.txt" ) );
    }

    @Test
    public void filenameWindowsRootPathOnWindows()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '\\' ) );
        assertThat( FileUtils.filename( "C:\\foo.bar.txt" ), is( "foo.bar.txt" ) );
    }

    @Test
    public void filenameWindowsNonRootPathOnWindows()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '\\' ) );
        assertThat( FileUtils.filename( "C:\\test\\foo.bar.txt" ), is( "foo.bar.txt" ) );
    }

    @Test
    public void filenameUnixRootPathOnUnix()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '/' ) );
        assertThat( FileUtils.filename( "/foo.bar.txt" ), is( "foo.bar.txt" ) );
    }

    @Test
    public void filenameUnixNonRootPathOnUnix()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '/' ) );
        assertThat( FileUtils.filename( "/test/foo.bar.txt" ), is( "foo.bar.txt" ) );
    }

    //// extension(String)

    @SuppressWarnings("ConstantConditions")
    @Test( expected = NullPointerException.class )
    public void blowUpOnNullExtension()
        throws Exception
    {
        FileUtils.extension( null );
    }

    @Test
    public void extensionEmpty()
        throws Exception
    {
        assertThat( FileUtils.extension( "" ), is( "" ) );
    }

    @Test
    public void extensionFileName()
        throws Exception
    {
        assertThat( FileUtils.extension( "foo.bar.txt" ), is( "txt" ) );
    }

    @Test
    public void extensionFileNameNoExtension()
        throws Exception
    {
        assertThat( FileUtils.extension( "foo_bar_txt" ), is( "" ) );
    }

    @Test
    //X @ReproducesPlexusBug( "assumes that the path is a local path" )
    public void extensionWindowsRootPathOnUnix()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '/' ) );
        assertThat( FileUtils.extension( "C:\\foo.bar.txt" ), is( "txt" ) );
    }

    @Test
    //X @ReproducesPlexusBug( "assumes that the path is a local path" )
    public void extensionWindowsNonRootPathOnUnix()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '/' ) );
        assertThat( FileUtils.extension( "C:\\test\\foo.bar.txt" ), is( "txt" ) );
    }

    @Test
    //X @ReproducesPlexusBug( "assumes that the path is a local path" )
    public void extensionUnixRootPathOnWindows()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '\\' ) );
        assertThat( FileUtils.extension( "/foo.bar.txt" ), is( "txt" ) );
    }

    @Test
    //X @ReproducesPlexusBug( "assumes that the path is a local path" )
    public void extensionUnixNonRootPathOnWindows()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '\\' ) );
        assertThat( FileUtils.extension( "/test/foo.bar.txt" ), is( "txt" ) );
    }

    @Test
    public void extensionWindowsRootPathOnWindows()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '\\' ) );
        assertThat( FileUtils.extension( "C:\\foo.bar.txt" ), is( "txt" ) );
    }

    @Test
    public void extensionWindowsNonRootPathOnWindows()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '\\' ) );
        assertThat( FileUtils.extension( "C:\\test\\foo.bar.txt" ), is( "txt" ) );
    }

    @Test
    @Ignore("Wait until we can run with assembly 2.5 which will support symlinks properly")
    public void isASymbolicLink()
        throws IOException
    {
        // This testcase will pass when running under java7 or higher
        assumeFalse( Os.isFamily( Os.FAMILY_WINDOWS ) );

        File file = new File( "src/test/resources/symlinks/src/symDir" );
        assertTrue(FileUtils.isSymbolicLink(file  ));
    }

    @Test
    @Ignore("Wait until we can run with assembly 2.5 which will support symlinks properly")
    public void notASymbolicLink()
        throws IOException
    {
        File file = new File( "src/test/resources/symlinks/src/" );
        assertFalse(FileUtils.isSymbolicLink(file  ));
    }

    @Test
    public void extensionUnixRootPathOnUnix()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '/' ) );
        assertThat( FileUtils.extension( "/foo.bar.txt" ), is( "txt" ) );
    }

    @Test
    public void extensionUnixNonRootPathOnUnix()
        throws Exception
    {
        assumeThat( File.separatorChar, is( '/' ) );
        assertThat( FileUtils.extension( "/test/foo.bar.txt" ), is( "txt" ) );
    }

    @Test
    public void createAndReadSymlink()
        throws Exception
    {
        assumeFalse( Os.isFamily( Os.FAMILY_WINDOWS ) );

        File file = new File( "target/fzz" );
        FileUtils.createSymbolicLink(  file, new File("../target") );

        final File file1 = Files.readSymbolicLink( file.toPath() ).toFile();
        assertEquals( "target", file1.getName() );
        Files.delete( file.toPath() );
    }

    @Test
    public void createSymbolicLinkWithDifferentTargetOverwritesSymlink()
            throws Exception
    {
        assumeFalse( Os.isFamily( Os.FAMILY_WINDOWS ) );

        // Arrange

        final File symlink1 = new File( tempFolder.getRoot(), "symlink" );

        FileUtils.createSymbolicLink( symlink1, testFile1 );

        // Act

        final File symlink2 = FileUtils.createSymbolicLink( symlink1, testFile2 );

        // Assert

        assertThat(
            Files.readSymbolicLink( symlink2.toPath() ).toFile(),
            CoreMatchers.equalTo( testFile2 )
        );
    }

    //// constants for testing

    private static final String[] MINIMUM_DEFAULT_EXCLUDES = {
        // Miscellaneous typical temporary files
        "**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*",

        // CVS
        "**/CVS", "**/CVS/**", "**/.cvsignore",

        // Subversion
        "**/.svn", "**/.svn/**",

        // Arch
        "**/.arch-ids", "**/.arch-ids/**",

        //Bazaar
        "**/.bzr", "**/.bzr/**",

        //SurroundSCM
        "**/.MySCMServerInfo",

        // Mac
        "**/.DS_Store",

        // Serena Dimensions Version 10
        "**/.metadata", "**/.metadata/**",

        // Mercurial
        "**/.hg", "**/.hg/**",

        // git
        "**/.git", "**/.git/**",

        // BitKeeper
        "**/BitKeeper", "**/BitKeeper/**", "**/ChangeSet", "**/ChangeSet/**",

        // darcs
        "**/_darcs", "**/_darcs/**", "**/.darcsrepo", "**/.darcsrepo/**", "**/-darcs-backup*", "**/.darcs-temp-mail" };

}
