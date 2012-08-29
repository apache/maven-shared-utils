package org.apache.maven.shared.utils.io;

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


import org.apache.maven.shared.utils.testhelpers.FileTestHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class DirectoryScannerTest
{
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();



    private void createTestData() throws IOException
    {
        File rootDir = tempFolder.getRoot();
        File folder1 = new File( rootDir, "folder1" );
        folder1.mkdirs();

        FileTestHelper.generateTestFile( new File( rootDir, "file1.txt" ), 11 );
        FileTestHelper.generateTestFile( new File( rootDir, "file2.txt" ), 12 );
        FileTestHelper.generateTestFile( new File( rootDir, "file3.dat" ), 13 );

        FileTestHelper.generateTestFile( new File( folder1, "file4.txt" ), 14 );
        FileTestHelper.generateTestFile( new File( folder1, "file5.dat" ), 15 );
    }

    @Test
    public void testSimpleScan() throws Exception
    {
        createTestData();

        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( tempFolder.getRoot() );
        ds.setCaseSensitive( true );
        ds.scan();

        String[] files = ds.getIncludedFiles();
        Assert.assertNotNull( files );
        Assert.assertEquals( files.length, 5 );

        Arrays.sort(files);
        Assert.assertEquals( "file1.txt", files[0] );
        Assert.assertEquals( "file2.txt", files[1] );
        Assert.assertEquals( "file3.dat", files[2] );
        Assert.assertEquals( "folder1/file4.txt", files[3].replace( "\\", "/" ) );
        Assert.assertEquals( "folder1/file5.dat", files[4].replace( "\\", "/" ) );
    }

    @Test
    public void testFilteredScan() throws Exception
    {
        createTestData();

        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( tempFolder.getRoot() );
        ds.setCaseSensitive( true );
        ds.setIncludes( new String[] {"**/*.dat", "*.somethingelse"} );
        ds.scan();

        String[] files = ds.getIncludedFiles();
        Assert.assertNotNull( files );
        Assert.assertEquals( files.length, 2 );

        Arrays.sort(files);

        Assert.assertEquals( "file3.dat", files[0] );
        Assert.assertEquals( "folder1/file5.dat", files[1].replace( "\\", "/" ) );
    }
}
