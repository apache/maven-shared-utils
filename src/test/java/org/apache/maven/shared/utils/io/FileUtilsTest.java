package org.apache.maven.shared.utils.io;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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

import org.apache.maven.shared.utils.Os;
import org.apache.maven.shared.utils.testhelpers.FileTestHelper;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

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

    /**
     * Delay in milliseconds to make sure test for "last modified date" are accurate
     */
    //private static final int LAST_MODIFIED_DELAY = 600;

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

    void createFile( File file, long size )
        throws IOException
    {
        if ( !file.getParentFile().exists() )
        {
            throw new IOException( "Cannot create file " + file + " as the parent directory does not exist" );
        }

        OutputStream out = null;
        try
        {
            out = new BufferedOutputStream( new FileOutputStream( file ) );
            FileTestHelper.generateTestData( out, size );
            out.close();
            out = null;
        }
        finally
        {
            IOUtil.close( out );
        }
    }


    /**
     * Assert that the content of a file is equal to that in a byte[].
     */
    void assertEqualContent( byte[] b0, File file )
        throws IOException
    {
        InputStream is = new java.io.FileInputStream( file );
        int count = 0, numRead = 0;
        byte[] b1 = new byte[b0.length];
        try
        {
            while ( count < b0.length && numRead >= 0 )
            {
                numRead = is.read( b1, count, b0.length );
                count += numRead;
            }
            assertThat( "Different number of bytes: ", count, is( b0.length ) );
            for ( int i = 0; i < count; i++ )
            {
                assertThat( "byte " + i + " differs", b1[i], is( b0[i] ) );
            }
        }
        finally
        {
            is.close();
        }
    }

    void deleteFile( File file )
    {
        if ( file.exists() )
        {
            assertThat( "Couldn't delete file: " + file, file.delete(), is( true ) );
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

//    @Test public void toURLs2() throws Exception {
//        File[] files = new File[] {
//            new File(getTestDirectory(), "file1.txt"),
//            null,
//        };
//        URL[] urls = FileUtils.toURLs(files);
//
//        assertEquals(files.length, urls.length);
//        assertEquals(true, urls[0].toExternalForm().startsWith("file:"));
//        assertEquals(true, urls[0].toExternalForm().indexOf("file1.txt") > 0);
//        assertEquals(null, urls[1]);
//    }
//
//    @Test public void toURLs3() throws Exception {
//        File[] files = null;
//        URL[] urls = FileUtils.toURLs(files);
//
//        assertEquals(0, urls.length);
//    }

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

        // Tests that resuorce was copied correctly
        FileInputStream fis = new FileInputStream( file );
        try
        {
            assertThat( "Content is not equal.",
                        IOUtil.contentEquals( getClass().getResourceAsStream( resourceName ), fis ), is( true ) );
        }
        finally
        {
            fis.close();
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

    @Test
    public void copyFileThatIsSymlink()
        throws Exception
    {
        assumeTrue( Java7Support.isAtLeastJava7() );
        assumeFalse( Os.isFamily( Os.FAMILY_WINDOWS ) );

        File destination = new File( tempFolder.getRoot(), "symCopy.txt" );

        File testDir = SymlinkTestSetup.createStandardSymlinkTestDir( new File( "target/test/symlinkCopy" ) );

        FileUtils.copyFile( new File( testDir, "symR" ), destination );
        assertTrue( Java7Support.isSymLink(  destination ));
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
    @Ignore( "Commons test case that is failing for plexus" )
    public void copyToSelf()
        throws Exception
    {
        File destination = new File( tempFolder.getRoot(), "copy3.txt" );
        //Prepare a test file
        FileUtils.copyFile( testFile1, destination );

        FileUtils.copyFile( destination, destination );
    }

    @Test
    @Ignore( "Commons test case that is failing for plexus" )
    public void copyDirectoryToDirectory_NonExistingDest()
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
        File actualDestDir = new File( destDir, srcDir.getName() );

        FileUtils.copyDirectory( srcDir, destDir );

        assertThat( "Check exists", destDir.exists(), is( true ) );
        assertThat( "Check exists", actualDestDir.exists(), is( true ) );
        assertThat( "Check size", FileUtils.sizeOfDirectory( actualDestDir ),
                    is( FileUtils.sizeOfDirectory( srcDir ) ) );
        assertThat( new File( actualDestDir, "sub/A.txt" ).exists(), is( true ) );
        FileUtils.deleteDirectory( destDir );
    }

    @Test
    @Ignore( "Commons test case that is failing for plexus" )
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

        assertThat( "Check exists", destDir.exists(), is( true ) );
        assertThat( "Check size", FileUtils.sizeOfDirectory( destDir ), is( FileUtils.sizeOfDirectory( srcDir ) ) );
        assertThat( new File( destDir, "sub/A.txt" ).exists(), is( true ) );
        FileUtils.deleteDirectory( destDir );
    }

    @Test
    @Ignore( "Commons test case that is failing for plexus" )
    public void copyDirectoryToExistingDest()
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
        destDir.mkdirs();

        FileUtils.copyDirectory( srcDir, destDir );

        assertThat( FileUtils.sizeOfDirectory( destDir ), is( FileUtils.sizeOfDirectory( srcDir ) ) );
        assertThat( new File( destDir, "sub/A.txt" ).exists(), is( true ) );
    }

    /**
     * Test for IO-141
     */
    @Test
    @Ignore( "Commons test case that is failing for plexus" )
    public void copyDirectoryToChild()
        throws Exception
    {
        File grandParentDir = new File( tempFolder.getRoot(), "grandparent" );
        File parentDir = new File( grandParentDir, "parent" );
        File childDir = new File( parentDir, "child" );
        createFilesForTestCopyDirectory( grandParentDir, parentDir, childDir );

        long expectedCount =
            FileUtils.getFileAndDirectoryNames( grandParentDir, null, null, true, true, true, true ).size()
                + FileUtils.getFileAndDirectoryNames( parentDir, null, null, true, true, true, true ).size();
        long expectedSize = FileUtils.sizeOfDirectory( grandParentDir ) + FileUtils.sizeOfDirectory( parentDir );
        FileUtils.copyDirectory( parentDir, childDir );
        assertThat(
            1L * FileUtils.getFileAndDirectoryNames( grandParentDir, null, null, true, true, true, true ).size(),
            is( expectedCount ) );
        assertThat( FileUtils.sizeOfDirectory( grandParentDir ), is( expectedSize ) );
    }

    /**
     * Test for IO-141
     */
    @Test
    @Ignore( "Commons test case that is failing for plexus" )
    public void copyDirectoryToGrandChild()
        throws Exception
    {
        File grandParentDir = new File( tempFolder.getRoot(), "grandparent" );
        File parentDir = new File( grandParentDir, "parent" );
        File childDir = new File( parentDir, "child" );
        createFilesForTestCopyDirectory( grandParentDir, parentDir, childDir );

        long expectedCount =
            ( FileUtils.getFileAndDirectoryNames( grandParentDir, null, null, true, true, true, true ).size() * 2 );
        long expectedSize = ( FileUtils.sizeOfDirectory( grandParentDir ) * 2 );
        FileUtils.copyDirectory( grandParentDir, childDir );
        assertThat(
            1L * FileUtils.getFileAndDirectoryNames( grandParentDir, null, null, true, true, true, true ).size(),
            is( expectedCount ) );
        assertThat( FileUtils.sizeOfDirectory( grandParentDir ), is( expectedSize ) );
    }

    /**
     * Test for IO-217 FileUtils.copyDirectoryToDirectory makes infinite loops
     */
    @Test
    public void copyDirectoryToItself()
        throws Exception
    {
        File dir = new File( tempFolder.getRoot(), "itself" );
        dir.mkdirs();
        FileUtils.copyDirectory( dir, dir );
        assertThat( FileUtils.getFileAndDirectoryNames( dir, null, null, true, true, true, true ).size(), is( 1 ) );
    }

    private void createFilesForTestCopyDirectory( File grandParentDir, File parentDir, File childDir )
        throws Exception
    {
        File childDir2 = new File( parentDir, "child2" );
        File grandChildDir = new File( childDir, "grandChild" );
        File grandChild2Dir = new File( childDir2, "grandChild2" );
        File file1 = new File( grandParentDir, "file1.txt" );
        File file2 = new File( parentDir, "file2.txt" );
        File file3 = new File( childDir, "file3.txt" );
        File file4 = new File( childDir2, "file4.txt" );
        File file5 = new File( grandChildDir, "file5.txt" );
        File file6 = new File( grandChild2Dir, "file6.txt" );
        FileUtils.deleteDirectory( grandParentDir );
        grandChildDir.mkdirs();
        grandChild2Dir.mkdirs();
        FileUtils.fileWrite( file1, "UTF8", "File 1 in grandparent" );
        FileUtils.fileWrite( file2, "UTF8", "File 2 in parent" );
        FileUtils.fileWrite( file3, "UTF8", "File 3 in child" );
        FileUtils.fileWrite( file4, "UTF8", "File 4 in child2" );
        FileUtils.fileWrite( file5, "UTF8", "File 5 in grandChild" );
        FileUtils.fileWrite( file6, "UTF8", "File 6 in grandChild2" );
    }

    @Test
    @Ignore( "Commons test case that is failing for plexus" )
    public void copyDirectoryErrors()
        throws Exception
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
            FileUtils.copyDirectory( new File( "a" ), null );
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
            FileUtils.copyDirectory( new File( "doesnt-exist" ), new File( "a" ) );
            fail();
        }
        catch ( IOException ex )
        {
        }
        try
        {
            FileUtils.copyDirectory( testFile1, new File( "a" ) );
            fail();
        }
        catch ( IOException ex )
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
        try
        {
            FileUtils.copyDirectory( tempFolder.getRoot(), tempFolder.getRoot() );
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
        assertThat( "Copy1.txt doesn't exist to delete", destination.exists(), is( true ) );
        FileUtils.forceDelete( destination );
        assertThat( "Check No Exist", !destination.exists(), is( true ) );
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
        OutputStream out = new java.io.FileOutputStream( file1 );
        try
        {
            out.write( "This is a test".getBytes( "UTF-8" ) );
        }
        finally
        {
            out.close();
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
    @SuppressWarnings( "unchecked" )
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
        assumeThat( Os.isFamily(Os.FAMILY_WINDOWS), is(false) );

        File file = new File( "src/test/resources/symlinks/src/symDir" );
        assertTrue(FileUtils.isSymbolicLink(file  ));
        assertTrue(FileUtils.isSymbolicLinkLegacy(file  ));
    }

    @Test
    @Ignore("Wait until we can run with assembly 2.5 which will support symlinks properly")
    public void notASymbolicLink()
        throws IOException
    {
        File file = new File( "src/test/resources/symlinks/src/" );
        assertFalse(FileUtils.isSymbolicLink(file  ));
        assertFalse(FileUtils.isSymbolicLinkLegacy(file  ));
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
