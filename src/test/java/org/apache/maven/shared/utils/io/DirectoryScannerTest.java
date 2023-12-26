/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.shared.utils.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.shared.utils.Os;
import org.apache.maven.shared.utils.testhelpers.FileTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@SuppressWarnings("deprecation")
public class DirectoryScannerTest {
    private static final String[] NONE = new String[0];

    @TempDir
    public File tempFolder;

    private void createTestData() throws IOException {
        File rootDir = tempFolder;
        File folder1 = new File(rootDir, "folder1");
        if (!folder1.mkdirs()) {
            Assertions.fail();
        }

        FileTestHelper.generateTestFile(new File(rootDir, "file1.txt"), 11);
        FileTestHelper.generateTestFile(new File(rootDir, "file2.txt"), 12);
        FileTestHelper.generateTestFile(new File(rootDir, "file3.dat"), 13);

        FileTestHelper.generateTestFile(new File(folder1, "file4.txt"), 14);
        FileTestHelper.generateTestFile(new File(folder1, "file5.dat"), 15);

        File folder2 = new File(folder1, "ignorefolder");
        if (!folder2.mkdirs()) {
            Assertions.fail();
        }
        FileTestHelper.generateTestFile(new File(folder2, "file7.txt"), 17);
    }

    @Test
    void simpleScan() throws Exception {
        createTestData();

        fitScanTest(
                true,
                true,
                true,
                /* includes */ null,
                /* excludes */ null,
                /* expInclFiles */
                new String[] {"file1.txt", "file2.txt", "file3.dat", "folder1/file4.txt", "folder1/file5.dat"},
                /* expInclDirs */ new String[] {"", "folder1"},
                /* expNotInclFiles */ NONE,
                /* expNotInclDirs  */ NONE,
                /* expNotExclFiles */ NONE,
                /* expNotExclDirs  */ NONE);

        // same without followSymlinks
        fitScanTest(
                true,
                false,
                true,
                /* includes */ null,
                /* excludes */ null,
                /* expInclFiles */
                new String[] {"file1.txt", "file2.txt", "file3.dat", "folder1/file4.txt", "folder1/file5.dat"},
                /* expInclDirs */ new String[] {"", "folder1"},
                /* expNotInclFiles */ NONE,
                /* expNotInclDirs  */ NONE,
                /* expNotExclFiles */ NONE,
                /* expNotExclDirs  */ NONE);
    }

    @Test
    void simpleIncludes() throws Exception {
        createTestData();

        fitScanTest(
                true,
                true,
                true,
                /* includes        */ new String[] {"**/*.dat", "*.somethingelse"},
                /* excludes        */ null,
                /* expInclFiles    */ new String[] {"file3.dat", "folder1/file5.dat"},
                /* expInclDirs     */ NONE,
                /* expNotInclFiles */ new String[] {"file1.txt", "file2.txt", "folder1/file4.txt"},
                /* expNotInclDirs  */ new String[] {"", "folder1"},
                /* expExclFiles    */ NONE,
                /* expExclDirs     */ NONE);

        // same without followSymlinks
        fitScanTest(
                true,
                false,
                true,
                /* includes        */ new String[] {"**/*.dat", "*.somethingelse"},
                /* excludes        */ null,
                /* expInclFiles    */ new String[] {"file3.dat", "folder1/file5.dat"},
                /* expInclDirs     */ NONE,
                /* expNotInclFiles */ new String[] {"file1.txt", "file2.txt", "folder1/file4.txt"},
                /* expNotInclDirs  */ new String[] {"", "folder1"},
                /* expExclFiles    */ NONE,
                /* expExclDirs     */ NONE);
    }

    @Test
    void includesWithNull() throws Exception {
        testXcludesWithNull(new String[] {null}, null, "includes");
    }

    @Test
    void excludesWithNull() throws Exception {
        testXcludesWithNull(null, new String[] {null}, "excludes");
    }

    private void testXcludesWithNull(String[] includes, String[] excludes, String listName) throws Exception {
        Throwable exception = assertThrows(NullPointerException.class, () -> {
            createTestData();

            fitScanTest(
                    true,
                    true,
                    true,
                    /* includes        */ includes,
                    /* excludes        */ excludes,
                    /* expInclFiles    */ new String[] {"file3.dat", "folder1/file5.dat"},
                    /* expInclDirs     */ NONE,
                    /* expNotInclFiles */ new String[] {"file1.txt", "file2.txt", "folder1/file4.txt"},
                    /* expNotInclDirs  */ new String[] {"", "folder1"},
                    /* expExclFiles    */ NONE,
                    /* expExclDirs     */ NONE);
        });
        assertTrue(exception
                .getMessage()
                .contains("If a non-null " + listName + " list is given, all elements must be non-null"));
    }

    @Test
    void checkSymlinkBehaviour() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File("src/test/resources/symlinks/src"));
        ds.setFollowSymlinks(false);
        ds.scan();

        String[] includedDirectories = ds.getIncludedDirectories();
        // FIXME 3 (Windows) and 5 (Linux) are both wrong. The correct answer is 4.
        // This method is broken in different ways on different operating systems.
        assertTrue(includedDirectories.length == 3 || includedDirectories.length == 5);

        String[] files = ds.getIncludedFiles();
        assertAlwaysIncluded(Arrays.asList(files));

        // FIXME getIncludedFiles is broken on Windows; correct answer is 9
        assertTrue(files.length == 9 || files.length == 11, "files.length is " + files.length);
    }

    @Test
    void followSymlinksFalse() throws IOException {
        assumeFalse(Os.isFamily(Os.FAMILY_WINDOWS));

        File testDir = SymlinkTestSetup.createStandardSymlinkTestDir(new File("target/test/symlinkTestCase"));

        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(testDir);
        ds.setFollowSymlinks(false);
        ds.scan();
        List<String> included = Arrays.asList(ds.getIncludedFiles());
        assertAlwaysIncluded(included);
        assertEquals(9, included.size());
        List<String> includedDirs = Arrays.asList(ds.getIncludedDirectories());
        assertTrue(includedDirs.contains(""));
        assertTrue(includedDirs.contains("aRegularDir"));
        assertTrue(includedDirs.contains("symDir"));
        assertTrue(includedDirs.contains("symLinkToDirOnTheOutside"));
        assertTrue(includedDirs.contains("targetDir"));
        assertEquals(5, includedDirs.size());
    }

    private void assertAlwaysIncluded(List<String> included) {
        assertTrue(included.contains("aRegularDir" + File.separator + "aRegularFile.txt"));
        assertTrue(included.contains("targetDir" + File.separator + "targetFile.txt"));
        assertTrue(included.contains("fileR.txt"));
        assertTrue(included.contains("fileW.txt"));
        assertTrue(included.contains("fileX.txt"));
        assertTrue(included.contains("symR"));
        assertTrue(included.contains("symW"));
        assertTrue(included.contains("symX"));
        assertTrue(included.contains("symLinkToFileOnTheOutside"));
    }

    @Test
    void followSymlinks() throws IOException {
        assumeFalse(Os.isFamily(Os.FAMILY_WINDOWS));

        DirectoryScanner ds = new DirectoryScanner();
        File testDir = SymlinkTestSetup.createStandardSymlinkTestDir(new File("target/test/symlinkTestCase"));

        ds.setBasedir(testDir);
        ds.setFollowSymlinks(true);
        ds.scan();
        List<String> included = Arrays.asList(ds.getIncludedFiles());
        assertAlwaysIncluded(included);
        assertTrue(included.contains("symDir/targetFile.txt"));
        assertTrue(included.contains("symLinkToDirOnTheOutside/FileInDirOnTheOutside.txt"));
        assertEquals(11, included.size());

        List<String> includedDirs = Arrays.asList(ds.getIncludedDirectories());
        assertTrue(includedDirs.contains("")); // w00t !
        assertTrue(includedDirs.contains("aRegularDir"));
        assertTrue(includedDirs.contains("symDir"));
        assertTrue(includedDirs.contains("symLinkToDirOnTheOutside"));
        assertTrue(includedDirs.contains("targetDir"));
        assertEquals(5, includedDirs.size());
    }

    /*
       Creates a standard directory layout with symlinks and files.
    */
    @Test
    void simpleExcludes() throws Exception {
        createTestData();

        fitScanTest(
                true,
                true,
                true,
                /* includes        */ null,
                /* excludes        */ new String[] {"**/*.dat", "*.somethingelse"},
                /* expInclFiles    */ new String[] {"file1.txt", "file2.txt", "folder1/file4.txt"},
                /* expInclDirs     */ new String[] {"", "folder1"},
                /* expNotInclFiles */ NONE,
                /* expNotInclDirs  */ NONE,
                /* expExclFiles    */ new String[] {"file3.dat", "folder1/file5.dat"},
                /* expExclDirs     */ NONE);

        // same without followSymlinks
        fitScanTest(
                true,
                false,
                true,
                /* includes        */ null,
                /* excludes        */ new String[] {"**/*.dat", "*.somethingelse"},
                /* expInclFiles    */ new String[] {"file1.txt", "file2.txt", "folder1/file4.txt"},
                /* expInclDirs     */ new String[] {"", "folder1"},
                /* expNotInclFiles */ NONE,
                /* expNotInclDirs  */ NONE,
                /* expExclFiles    */ new String[] {"file3.dat", "folder1/file5.dat"},
                /* expExclDirs     */ NONE);
    }

    /**
     * Performs a scan and test for the given parameters if not null.
     */
    private void fitScanTest(
            boolean caseSensitive,
            boolean followSymLinks,
            boolean addDefaultExcludes,
            String[] includes,
            String[] excludes,
            String[] expectedIncludedFiles,
            String[] expectedIncludedDirectories,
            String[] expectedNotIncludedFiles,
            String[] expectedNotIncludedDirectories,
            String[] expectedExcludedFiles,
            String[] expectedExcludedDirectories) {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(tempFolder);

        ds.setCaseSensitive(caseSensitive);
        ds.setFollowSymlinks(followSymLinks);

        if (addDefaultExcludes) {
            ds.addDefaultExcludes();
        }
        if (includes != null) {
            ds.setIncludes(includes);
        }
        if (excludes != null) {
            ds.setExcludes(excludes);
        }

        TestScanConductor scanConductor = new TestScanConductor();

        ds.setScanConductor(scanConductor);

        ds.scan();

        checkFiles("expectedIncludedFiles", expectedIncludedFiles, ds.getIncludedFiles());
        checkFiles("expectedIncludedDirectories", expectedIncludedDirectories, ds.getIncludedDirectories());
        checkFiles("expectedNotIncludedFiles", expectedNotIncludedFiles, ds.getNotIncludedFiles());
        checkFiles("expectedNotIncludedDirectories", expectedNotIncludedDirectories, ds.getNotIncludedDirectories());
        checkFiles("expectedExcludedFiles", expectedExcludedFiles, ds.getExcludedFiles());
        checkFiles("expectedExcludedDirectories", expectedExcludedDirectories, ds.getExcludedDirectories());

        checkFiles("visitedFiles", expectedIncludedFiles, scanConductor.visitedFiles.toArray(new String[0]));
    }

    /**
     * Check if the resolved files match the rules of the expected files.
     *
     * @param expectedFiles
     * @param resolvedFiles
     */
    private void checkFiles(String category, String[] expectedFiles, String[] resolvedFiles) {
        if (expectedFiles != null) {
            String msg = category + " expected: " + Arrays.toString(expectedFiles) + " but got: "
                    + Arrays.toString(resolvedFiles);
            assertNotNull(resolvedFiles, msg);
            assertEquals(expectedFiles.length, resolvedFiles.length, msg);

            Arrays.sort(expectedFiles);
            Arrays.sort(resolvedFiles);

            for (int i = 0; i < resolvedFiles.length; i++) {
                assertEquals(expectedFiles[i], resolvedFiles[i].replace("\\", "/"), msg);
            }
        }
    }

    private static class TestScanConductor implements ScanConductor {
        final List<String> visitedFiles = new ArrayList<>();

        public ScanConductor.ScanAction visitDirectory(String name, File directory) {
            assertTrue(directory.isDirectory());

            if (directory.getName().equals("ignorefolder")) {
                return ScanAction.NO_RECURSE;
            }

            return ScanAction.CONTINUE;
        }

        public ScanConductor.ScanAction visitFile(String name, File file) {
            assertTrue(file.isFile());
            visitedFiles.add(name);
            return ScanAction.CONTINUE;
        }
    }

    private void removeAndAddSomeFiles() throws IOException {
        File rootDir = tempFolder;
        File file2 = new File(rootDir, "file2.txt");
        file2.delete();

        FileTestHelper.generateTestFile(new File(rootDir, "folder1/file9.txt"), 15);

        File folder2 = new File(rootDir, "folder1/ignorefolder");
        FileUtils.deleteDirectory(folder2);
    }

    @Test
    void scanDiff() throws Exception {
        createTestData();

        DirectoryScanner dss = new DirectoryScanner();
        dss.setBasedir(tempFolder);
        assertNotNull(dss);

        // we take the initial snapshot
        dss.scan();
        String[] oldFiles = dss.getIncludedFiles();

        // now we change 3 files. add one and remove
        removeAndAddSomeFiles();

        dss.scan();

        DirectoryScanResult dsr = dss.diffIncludedFiles(oldFiles);

        String[] addedFiles = dsr.getFilesAdded();
        String[] removedFiles = dsr.getFilesRemoved();
        assertNotNull(addedFiles);
        assertNotNull(removedFiles);
        assertEquals(1, addedFiles.length);
        assertEquals(2, removedFiles.length);
    }

    @Disabled("Enable this test to run performance checks")
    @Test
    void performanceTest() throws Exception {

        File rootFolder = tempFolder;

        // do some warmup
        for (int i = 1; i < 200; i++) {
            createTestData();
            removeAndAddSomeFiles();
            FileUtils.deleteDirectory(rootFolder);
        }

        int cycles = 2000;

        // and now we take the time _without_
        long startTime = System.nanoTime();
        for (int i = 1; i < cycles; i++) {
            createTestData();
            removeAndAddSomeFiles();
            FileUtils.deleteDirectory(rootFolder);
            rootFolder.mkdir();
        }
        long endTime = System.nanoTime();

        long durationEmptyRun = endTime - startTime;
        System.out.println("durationEmptyRun            [ns]: " + durationEmptyRun);

        startTime = System.nanoTime();
        for (int i = 1; i < cycles; i++) {
            createTestData();
            DirectoryScanner directoryScanner = new DirectoryScanner();
            directoryScanner.setBasedir(rootFolder);
            directoryScanner.scan();
            String[] oldFiles = directoryScanner.getIncludedFiles();

            removeAndAddSomeFiles();

            directoryScanner.scan();

            DirectoryScanResult directoryScanResult = directoryScanner.diffIncludedFiles(oldFiles);
            assertNotNull(directoryScanResult);

            FileUtils.deleteDirectory(rootFolder);
            rootFolder.mkdir();
        }
        endTime = System.nanoTime();

        long durationWithSnapshotScanner = endTime - startTime;
        System.out.println("durationWithSnapshotScanner [ns]: " + durationWithSnapshotScanner);

        long dirScannerOverhead = durationWithSnapshotScanner - durationEmptyRun;

        System.out.println("Overhead for n cycles [ns]: " + dirScannerOverhead);
    }
}
