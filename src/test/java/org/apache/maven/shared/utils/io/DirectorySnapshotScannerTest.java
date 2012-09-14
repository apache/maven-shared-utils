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
import org.junit.Test;
import org.junit.Rule;
import org.junit.Ignore;
import org.junit.Assert;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class DirectorySnapshotScannerTest
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

        File folder2 = new File( folder1, "ignorefolder" );
        folder2.mkdirs();
        FileTestHelper.generateTestFile( new File( folder2, "file7.txt" ), 17 );

    }

    private void removeAndAddSomeFiles() throws IOException
    {
        File rootDir = tempFolder.getRoot();
        File file2 = new File( rootDir, "file2.txt" );
        file2.delete();

        FileTestHelper.generateTestFile( new File( rootDir, "folder1/file9.txt" ), 15 );

        File folder2 = new File( rootDir, "folder1/ignorefolder" );
        FileUtils.deleteDirectory( folder2 );
    }

    @Test
    public void testInitialScan() throws Exception
    {
        createTestData();

        DirectorySnapshotScanner dss = new DirectorySnapshotScanner( tempFolder.getRoot() );
        Assert.assertNotNull( dss );

        // we take the initial snapshot which should result in an empty diff
        dss.capture();

        //X TODO define result of the initial scan. Full directory tree or just an empty array?
        String[] addedFiles = dss.getFilesAdded();
        String[] removedFiles = dss.getFilesRemoved();
        Assert.assertNotNull( addedFiles );
        Assert.assertNotNull( removedFiles );
        Assert.assertEquals( 0, addedFiles.length );
        Assert.assertEquals(0, removedFiles.length );

        // now we change 3 files. add one and remove
        removeAndAddSomeFiles();

        dss.capture();

        addedFiles = dss.getFilesAdded();
        removedFiles = dss.getFilesRemoved();
        Assert.assertNotNull( addedFiles );
        Assert.assertNotNull( removedFiles );
        Assert.assertEquals( 1, addedFiles.length );
        Assert.assertEquals(2, removedFiles.length );

        String[] allFiles = dss.getScannedFiles();
        Assert.assertNotNull( allFiles );
        Assert.assertEquals(5, allFiles.length );

    }



    @Ignore("Enable this test to run performance checks")
    @Test
    public void performanceTest() throws Exception {

        File rootFolder = tempFolder.getRoot();

        // do some warmup
        for ( int i = 1; i < 200; i++ )
        {
            createTestData();
            removeAndAddSomeFiles();
            FileUtils.deleteDirectory( rootFolder );
        }

        int cycles = 2000;

        // and now we take the time _without_
        long startTime = System.nanoTime();
        for ( int i = 1; i < cycles; i++ )
        {
            createTestData();
            removeAndAddSomeFiles();
            FileUtils.deleteDirectory( rootFolder );
            rootFolder.mkdir();
        }
        long endTime = System.nanoTime();

        long durationEmptyRun = endTime - startTime;
        System.out.println( "durationEmptyRun            [ns]: " + durationEmptyRun);

        startTime = System.nanoTime();
        for ( int i = 1; i < cycles; i++ )
        {
            createTestData();
            DirectorySnapshotScanner dss = new DirectorySnapshotScanner( rootFolder );
            dss.capture();
            removeAndAddSomeFiles();
            dss.capture();

            FileUtils.deleteDirectory( rootFolder );
            rootFolder.mkdir();
        }
        endTime = System.nanoTime();

        long durationWithSnapshotScanner = endTime - startTime;
        System.out.println( "durationWithSnapshotScanner [ns]: " + durationWithSnapshotScanner);

        long dirScannerOverhead = durationWithSnapshotScanner - durationEmptyRun;

        System.out.println( "Overhead for n cycles [ns]: " + dirScannerOverhead);
    }

}
