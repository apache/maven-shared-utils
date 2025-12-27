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
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.maven.shared.utils.Os;
import org.apache.maven.shared.utils.testhelpers.FileTestHelper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
@SuppressWarnings("deprecation")
public class FileUtilsTest {

    // Test data

    @TempDir
    private File tempFolder;

    private String name;

    /**
     * Size of test directory.
     */
    private static final int TEST_DIRECTORY_SIZE = 0;

    private File testFile1;

    private File testFile2;

    private long testFile1Size;

    private long testFile2Size;

    @BeforeEach
    public void setUp(TestInfo testInfo) throws Exception {
        testInfo.getTestMethod().ifPresent(method -> this.name = method.getName());
        testFile1 = newFile(tempFolder, "file1-test.txt");
        testFile2 = newFile(tempFolder, "file1a-test.txt");

        testFile1Size = (int) testFile1.length();
        testFile2Size = (int) testFile2.length();

        tempFolder.mkdirs();
        createFile(testFile1, testFile1Size);
        createFile(testFile2, testFile2Size);
        FileUtils.deleteDirectory(tempFolder);
        tempFolder.mkdirs();
        createFile(testFile1, testFile1Size);
        createFile(testFile2, testFile2Size);
    }

    private static void createFile(File file, long size) throws IOException {
        if (!file.getParentFile().exists()) {
            throw new IOException("Cannot create file " + file + " as the parent directory does not exist");
        }

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            FileTestHelper.generateTestData(out, size);
        }
    }

    /**
     * Assert that the content of a file is equal to that in a byte[].
     */
    private void assertEqualContent(byte[] b0, File file) throws IOException {
        int count = 0, numRead = 0;
        byte[] b1 = new byte[b0.length];
        try (InputStream is = Files.newInputStream(file.toPath())) {
            while (count < b0.length && numRead >= 0) {
                numRead = is.read(b1, count, b0.length);
                count += numRead;
            }
            assertEquals(b0.length, count, "Different number of bytes: ");
            for (int i = 0; i < count; i++) {
                assertEquals(b0[i], b1[i], "byte " + i + " differs");
            }
        }
    }

    private void deleteFile(File file) {
        if (file.exists()) {
            assertTrue(file.delete(), "Couldn't delete file: " + file);
        }
    }

    // -----------------------------------------------------------------------
    @Test
    public void toFile1() throws Exception {
        URL url = new URL("file", null, "a/b/c/file.txt");
        File file = FileUtils.toFile(url);
        Assertions.assertNotNull(file);
        assertTrue(file.toString().contains("file.txt"));
    }

    @Test
    public void toFile2() throws Exception {
        URL url = new URL("file", null, "a/b/c/file%20n%61me%2520.tx%74");
        File file = FileUtils.toFile(url);
        Assertions.assertNotNull(file);
        assertTrue(file.toString().contains("file name%20.txt"));
    }

    @Test
    public void toFile3() throws Exception {
        assertNull(FileUtils.toFile(null));
        assertNull(FileUtils.toFile(new URL("https://jakarta.apache.org")));
    }

    @Test
    public void toFile4() {
        assertThrows(NumberFormatException.class, () -> {
            URL url = new URL("file", null, "a/b/c/file%%20%me.txt%");
            File file = FileUtils.toFile(url);
            Assertions.assertNotNull(file);
            assertTrue(file.toString().contains("file% %me.txt%"));
        });
    }

    /**
     * IO-252
     */
    @Test
    public void toFile5() throws Exception {
        URL url = new URL("file", null, "both%20are%20100%20%25%20true");
        File file = FileUtils.toFile(url);
        Assertions.assertNotNull(file);
        assertEquals("both are 100 % true", file.toString());
    }

    @Test
    public void toFileUtf8() throws Exception {
        URL url = new URL("file", null, "/home/%C3%A4%C3%B6%C3%BC%C3%9F");
        File file = FileUtils.toFile(url);
        Assertions.assertNotNull(file);
        assertFalse(file.toString().contains("äöüß"));
    }

    // toURLs

    @Test
    public void toURLs1() throws Exception {
        File[] files = new File[] {
            new File(tempFolder, "file1.txt"), new File(tempFolder, "file2.txt"), new File(tempFolder, "test file.txt"),
        };
        URL[] urls = FileUtils.toURLs(files);

        assertEquals(files.length, urls.length);
        assertTrue(urls[0].toExternalForm().startsWith("file:"));
        assertTrue(urls[0].toExternalForm().contains("file1.txt"));
        assertTrue(urls[1].toExternalForm().startsWith("file:"));
        assertTrue(urls[1].toExternalForm().contains("file2.txt"));

        // Test escaped char
        assertTrue(urls[2].toExternalForm().startsWith("file:"));
        assertTrue(urls[2].toExternalForm().contains("test%20file.txt"));
    }

    // contentEquals

    @Test
    public void contentEquals() throws Exception {
        // Non-existent files
        File file = new File(tempFolder, name);
        File file2 = new File(tempFolder, name + "2");
        // both don't  exist
        assertTrue(FileUtils.contentEquals(file, file));
        assertTrue(FileUtils.contentEquals(file, file2));
        assertTrue(FileUtils.contentEquals(file2, file2));
        assertTrue(FileUtils.contentEquals(file2, file));

        // Directories
        FileUtils.contentEquals(tempFolder, tempFolder);

        // Different files
        File objFile1 = new File(tempFolder, name + ".object");
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile(Objects.requireNonNull(getClass().getResource("/java/lang/Object.class")), objFile1);

        File objFile1b = new File(tempFolder, name + ".object2");
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile(Objects.requireNonNull(getClass().getResource("/java/lang/Object.class")), objFile1b);

        File objFile2 = new File(tempFolder, name + ".collection");
        objFile2.deleteOnExit();
        FileUtils.copyURLToFile(
                Objects.requireNonNull(getClass().getResource("/java/util/Collection.class")), objFile2);

        assertFalse(FileUtils.contentEquals(objFile1, objFile2));
        assertFalse(FileUtils.contentEquals(objFile1b, objFile2));
        assertTrue(FileUtils.contentEquals(objFile1, objFile1b));

        assertTrue(FileUtils.contentEquals(objFile1, objFile1));
        assertTrue(FileUtils.contentEquals(objFile1b, objFile1b));
        assertTrue(FileUtils.contentEquals(objFile2, objFile2));

        // Equal files
        file.createNewFile();
        file2.createNewFile();
        assertTrue(FileUtils.contentEquals(file, file));
        assertTrue(FileUtils.contentEquals(file, file2));
    }

    // copyURLToFile

    @Test
    public void copyURLToFile() throws Exception {
        // Creates file
        File file = new File(tempFolder, name);
        file.deleteOnExit();

        // Loads resource
        String resourceName = "/java/lang/Object.class";
        FileUtils.copyURLToFile(Objects.requireNonNull(getClass().getResource(resourceName)), file);

        // Tests that resource was copied correctly
        try (FileInputStream fis = new FileInputStream(file)) {
            assertTrue(
                    IOUtil.contentEquals(Objects.requireNonNull(getClass().getResourceAsStream(resourceName)), fis),
                    "Content is not equal.");
        }
        // TODO Maybe test copy to itself like for copyFile()
    }

    // forceMkdir

    @Test
    public void forceMkdir() throws Exception {
        // Tests with existing directory
        FileUtils.forceMkdir(tempFolder);

        // Creates test file
        File testFile = new File(tempFolder, name);
        testFile.deleteOnExit();
        testFile.createNewFile();
        assertTrue(testFile.exists(), "Test file does not exist.");

        // Tests with existing file
        assertThrows(IOException.class, () -> FileUtils.forceMkdir(testFile));

        testFile.delete();

        // Tests with non-existent directory
        FileUtils.forceMkdir(testFile);
        assertTrue(testFile.exists(), "Directory was not created.");
    }

    // sizeOfDirectory

    @Test
    public void sizeOfDirectory() throws Exception {
        File file = new File(tempFolder, name);

        // Non-existent file
        assertThrows(IllegalArgumentException.class, () -> FileUtils.sizeOfDirectory(file));

        // Creates file
        file.createNewFile();
        file.deleteOnExit();

        // Existing file
        assertThrows(IllegalArgumentException.class, () -> FileUtils.sizeOfDirectory(file));

        // Existing directory
        file.delete();
        file.mkdir();

        assertEquals(TEST_DIRECTORY_SIZE, FileUtils.sizeOfDirectory(file), "Unexpected directory size");
    }

    // copyFile

    @Test
    public void copyFile1() throws Exception {
        File destination = new File(tempFolder, "copy1.txt");

        // Thread.sleep(LAST_MODIFIED_DELAY);
        // This is to slow things down so we can catch if
        // the lastModified date is not ok

        FileUtils.copyFile(testFile1, destination);
        assertTrue(destination.exists(), "Check Exist");
        assertEquals(testFile1Size, destination.length(), "Check Full copy");
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/
    }

    /** A time today, rounded down to the previous minute */
    private static final long MODIFIED_TODAY =
            (System.currentTimeMillis() / TimeUnit.MINUTES.toMillis(1)) * TimeUnit.MINUTES.toMillis(1);

    /** A time yesterday, rounded down to the previous minute */
    private static final long MODIFIED_YESTERDAY = MODIFIED_TODAY - TimeUnit.DAYS.toMillis(1);

    /** A time last week, rounded down to the previous minute */
    private static final long MODIFIED_LAST_WEEK = MODIFIED_TODAY - TimeUnit.DAYS.toMillis(7);

    @Test
    public void copyFileWithNoFiltersAndNoDestination() throws Exception {
        File from = write("from.txt", MODIFIED_YESTERDAY, "Hello World!");
        File to = new File(tempFolder, "to.txt");

        FileUtils.copyFile(from, to, null, (FileUtils.FilterWrapper[]) null);

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt did not exist so should have been written");
        assertFileContent(to, "Hello World!");
    }

    @Test
    public void copyFileWithNoFiltersAndLastModifiedDateOfZeroAndNoDestination() throws Exception {
        File from = write("from.txt", MODIFIED_YESTERDAY, "Hello World!");
        File to = new File(tempFolder, "to.txt");

        from.setLastModified(0);
        FileUtils.copyFile(from, to, null, (FileUtils.FilterWrapper[]) null);

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt did not exist so should have been written");
        assertFileContent(to, "Hello World!");
    }

    @Test
    public void copyFileWithNoFiltersAndOutdatedDestination() throws Exception {
        File from = write("from.txt", MODIFIED_YESTERDAY, "Hello World!");
        File to = write("to.txt", MODIFIED_LAST_WEEK, "Older content");

        FileUtils.copyFile(from, to, null, (FileUtils.FilterWrapper[]) null);

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt was outdated so should have been overwritten");
        assertFileContent(to, "Hello World!");
    }

    @Test
    public void copyFileWithNoFiltersAndNewerDestination() throws Exception {
        File from = write("from.txt", MODIFIED_LAST_WEEK, "Hello World!");
        File to = write("to.txt", MODIFIED_YESTERDAY, "Older content");

        FileUtils.copyFile(from, to, null, (FileUtils.FilterWrapper[]) null);

        assertTrue(to.lastModified() < MODIFIED_TODAY, "to.txt was newer so should have been left alone");
        assertFileContent(to, "Older content");
    }

    @Test
    public void copyFileWithNoFiltersAndNewerDestinationButForcedOverwrite() throws Exception {
        File from = write("from.txt", MODIFIED_LAST_WEEK, "Hello World!");
        File to = write("to.txt", MODIFIED_YESTERDAY, "Older content");

        FileUtils.copyFile(from, to, null, null, true);

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt was newer but the overwrite should have been forced");
        assertFileContent(to, "Hello World!");
    }

    @Test
    public void copyFileWithFilteringButNoFilters() throws Exception {
        File from = write("from.txt", MODIFIED_YESTERDAY, "Hello ${name}!");
        File to = write("to.txt", MODIFIED_LAST_WEEK, "Older content");

        FileUtils.copyFile(from, to, null);

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt was outdated so should have been overwritten");
        assertFileContent(to, "Hello ${name}!");
    }

    @Test
    public void copyFileWithFilteringAndNoDestination() throws Exception {
        File from = write("from.txt", MODIFIED_YESTERDAY, "Hello ${name}!");
        File to = new File(tempFolder, "to.txt");

        FileUtils.copyFile(from, to, null, wrappers());

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt did not exist so should have been written");
        assertFileContent(to, "Hello Bob!");
    }

    @Test
    public void copyFileWithFilteringAndOutdatedDestination() throws Exception {
        File from = write("from.txt", MODIFIED_YESTERDAY, "Hello ${name}!");
        File to = write("to.txt", MODIFIED_LAST_WEEK, "Older content");

        FileUtils.copyFile(from, to, null, wrappers());

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt was outdated so should have been overwritten");
        assertFileContent(to, "Hello Bob!");
    }

    @Test
    public void copyFileWithFilteringAndNewerDestinationButForcedOverwrite() throws Exception {
        File from = write("from.txt", MODIFIED_LAST_WEEK, "Hello ${name}!");
        File to = write("to.txt", MODIFIED_YESTERDAY, "Older content");

        FileUtils.copyFile(from, to, null, wrappers(), true);

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt was newer but the overwrite should have been forced");
        assertFileContent(to, "Hello Bob!");
    }

    @Test
    public void copyFileWithFilteringAndNewerDestinationButModifiedContent() throws Exception {
        File from = write("from.txt", MODIFIED_LAST_WEEK, "Hello ${name}!");
        File to = write("to.txt", MODIFIED_YESTERDAY, "Hello Charlie!");

        FileUtils.copyFile(from, to, null, wrappers());

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt was outdated so should have been overwritten");
        assertFileContent(to, "Hello Bob!");
    }

    @Test
    public void copyFileWithFilteringAndNewerDestinationAndMatchingContent() throws Exception {
        File from = write("from.txt", MODIFIED_LAST_WEEK, "Hello ${name}!");
        File to = write("to.txt", MODIFIED_YESTERDAY, "Hello Bob!");

        FileUtils.copyFile(from, to, null, wrappers());

        assertFileContent(to, "Hello Bob!");
        assertTrue(to.lastModified() < MODIFIED_TODAY, "to.txt content should be unchanged and have been left alone");
    }

    private static FileUtils.FilterWrapper[] wrappers() {
        return new FileUtils.FilterWrapper[] {
            new FileUtils.FilterWrapper() {
                @Override
                public Reader getReader(Reader reader) {
                    return new StringReader("Hello Bob!");
                }
            }
        };
    }

    private File write(@Nonnull String name, long lastModified, @Nonnull String text) throws IOException {
        final File file = new File(tempFolder, name);
        try (Writer writer = new FileWriter(file)) {
            writer.write(text);
        }
        assertTrue(file.setLastModified(lastModified));
        assertEquals(lastModified, file.lastModified(), "Failed to set lastModified date on " + file.getPath());
        return file;
    }

    private static void assertFileContent(@Nonnull File file, @Nonnull String expected) throws IOException {
        try (Reader in = new FileReader(file)) {
            assertEquals(expected, IOUtils.toString(in), "Expected " + file.getPath() + " to contain: " + expected);
        }
    }

    @Test
    public void copyFileThatIsSymlink() throws Exception {
        assumeFalse(Os.isFamily(Os.FAMILY_WINDOWS));

        File destination = new File(tempFolder, "symCopy.txt");

        File testDir = SymlinkTestSetup.createStandardSymlinkTestDir(new File("target/test/symlinkCopy"));

        FileUtils.copyFile(new File(testDir, "symR"), destination);

        assertTrue(Files.isSymbolicLink(destination.toPath()));
    }

    @Test
    public void deleteFile() throws Exception {
        File destination = new File(tempFolder, "copy1.txt");
        FileUtils.copyFile(testFile1, destination);
        FileUtils.delete(destination);
        assertFalse(destination.exists(), "Check Exist");
    }

    @Test
    public void deleteFileNofile() {
        assertThrows(IOException.class, () -> {
            File destination = new File("abc/cde");
            FileUtils.delete(destination);
        });
    }

    @Test
    public void deleteFileLegacy() throws Exception {
        File destination = new File(tempFolder, "copy1.txt");
        FileUtils.copyFile(testFile1, destination);
        assertTrue(FileUtils.deleteLegacyStyle(destination));
    }

    @Test
    public void deleteFileLegacyNofile() {
        File destination = new File("abc/cde");
        assertFalse(FileUtils.deleteLegacyStyle(destination));
    }

    @Test
    public void copyFileWithPermissions() throws Exception {
        File source = new File("src/test/resources/executable");
        source.setExecutable(true);
        assertTrue(source.exists(), "Need an existing file to copy");
        // On some OS (Windows) the executable bit is not supported
        assumeTrue(source.canExecute(), "Need an executable file to copy");

        File destination = new File(tempFolder, "executable-copy");

        FileUtils.copyFile(source, destination);

        assertTrue(
                Files.exists(destination.toPath()),
                "destination not exists: " + destination.getAbsolutePath() + ", directory content: "
                        + Arrays.asList(Objects.requireNonNull(
                                destination.getParentFile().list())));

        assertTrue(destination.canExecute(), "Check copy executable");
    }

    @Test
    public void copyFile2() throws Exception {
        File destination = new File(tempFolder, "copy2.txt");

        // Thread.sleep(LAST_MODIFIED_DELAY);
        // This is to slow things down so we can catch if
        // the lastModified date is not ok

        FileUtils.copyFile(testFile1, destination);
        assertTrue(destination.exists(), "Check Exist");
        assertEquals(testFile2Size, destination.length(), "Check Full copy");
        /* disabled: Thread.sleep doesn't work reliably for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/
    }

    @Test
    public void copyToSelf() throws IOException {
        File destination = new File(tempFolder, "copy3.txt");
        // Prepare a test file
        FileUtils.copyFile(testFile1, destination);

        FileUtils.copyFile(destination, destination);
    }

    @Test
    public void copyDirectoryToNonExistingDest() throws Exception {
        createFile(testFile1, 1234);
        createFile(testFile2, 4321);
        File srcDir = tempFolder;
        File subDir = new File(srcDir, "sub");
        subDir.mkdir();
        File subFile = new File(subDir, "A.txt");
        FileUtils.fileWrite(subFile, "UTF8", "HELLO WORLD");
        File destDir = new File(System.getProperty("java.io.tmpdir"), "tmp-FileUtilsTestCase");
        FileUtils.deleteDirectory(destDir);

        FileUtils.copyDirectory(srcDir, destDir);

        assertTrue(destDir.exists());
        assertEquals(FileUtils.sizeOfDirectory(destDir), FileUtils.sizeOfDirectory(srcDir));
        assertTrue(new File(destDir, "sub/A.txt").exists());
        FileUtils.deleteDirectory(destDir);
    }

    @Test
    public void copyDirectoryToExistingDest() throws IOException {
        createFile(testFile1, 1234);
        createFile(testFile2, 4321);
        File srcDir = tempFolder;
        File subDir = new File(srcDir, "sub");
        assertTrue(subDir.mkdir());
        File subFile = new File(subDir, "A.txt");
        FileUtils.fileWrite(subFile, "UTF8", "HELLO WORLD");
        File destDir = new File(System.getProperty("java.io.tmpdir"), "tmp-FileUtilsTestCase");
        FileUtils.deleteDirectory(destDir);
        assertTrue(destDir.mkdirs());

        FileUtils.copyDirectory(srcDir, destDir);

        assertEquals(FileUtils.sizeOfDirectory(destDir), FileUtils.sizeOfDirectory(srcDir));
        assertTrue(new File(destDir, "sub/A.txt").exists());
    }

    @Test
    public void copyDirectoryErrorsNullDestination() throws IOException {
        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(new File("a"), null));
    }

    @Test
    public void copyDirectoryErrorsCopyToSelf() {
        assertThrows(IOException.class, () -> FileUtils.copyDirectory(tempFolder, tempFolder));
    }

    @Test
    public void copyDirectoryErrors() throws IOException {
        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(null, null));

        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(null, new File("a")));

        assertThrows(IOException.class, () -> FileUtils.copyDirectory(tempFolder, testFile1));
    }

    // forceDelete

    @Test
    public void forceDeleteAFile1() throws Exception {
        File destination = new File(tempFolder, "copy1.txt");
        destination.createNewFile();
        assertTrue(destination.exists(), "Copy1.txt doesn't exist to delete");
        FileUtils.forceDelete(destination);
        assertFalse(destination.exists());
    }

    @Test
    public void forceDeleteAFile2() throws Exception {
        File destination = new File(tempFolder, "copy2.txt");
        destination.createNewFile();
        assertTrue(destination.exists(), "Copy2.txt doesn't exist to delete");
        FileUtils.forceDelete(destination);
        assertFalse(destination.exists(), "Check No Exist");
    }

    @Test
    @Disabled("Commons test case that is failing for plexus")
    public void forceDeleteAFile3() throws Exception {
        File destination = new File(tempFolder, "no_such_file");
        assertFalse(destination.exists(), "Check No Exist");
        try {
            FileUtils.forceDelete(destination);
            fail("Should generate FileNotFoundException");
        } catch (FileNotFoundException ignored) {
        }
    }

    // copyFileToDirectory

    @Test
    @Disabled("Commons test case that is failing for plexus")
    public void copyFile1ToDir() throws Exception {
        File directory = new File(tempFolder, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File destination = new File(directory, testFile1.getName());

        // Thread.sleep(LAST_MODIFIED_DELAY);
        // This is to slow things down so we can catch if
        // the lastModified date is not ok

        FileUtils.copyFileToDirectory(testFile1, directory);
        assertTrue(destination.exists(), "Check Exist");
        assertEquals(testFile1Size, destination.length(), "Check Full copy");
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/

        try {
            FileUtils.copyFileToDirectory(destination, directory);
            fail("Should not be able to copy a file into the same directory as itself");
        } catch (IOException ioe) {
            // we want that, cannot copy to the same directory as the original file
        }
    }

    @Test
    public void copyFile2ToDir() throws Exception {
        File directory = new File(tempFolder, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File destination = new File(directory, testFile1.getName());

        // Thread.sleep(LAST_MODIFIED_DELAY);
        // This is to slow things down so we can catch if
        // the lastModified date is not ok

        FileUtils.copyFileToDirectory(testFile1, directory);
        assertTrue(destination.exists(), "Check Exist");
        assertEquals(testFile2Size, destination.length(), "Check Full copy");
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/
    }

    // forceDelete

    @Test
    public void forceDeleteDir() throws Exception {
        File testDirectory = newFolder(tempFolder, name);
        FileUtils.forceDelete(testDirectory.getParentFile());
        assertFalse(testDirectory.getParentFile().exists(), "Check No Exist");
    }

    /**
     * Test the FileUtils implementation.
     */
    @Test
    public void fileUtils() throws Exception {
        // Loads file from classpath
        File file1 = new File(tempFolder, "test.txt");
        String filename = file1.getAbsolutePath();

        // Create test file on-the-fly
        try (OutputStream out = Files.newOutputStream(file1.toPath())) {
            out.write("This is a test".getBytes("UTF-8"));
        }

        File file2 = new File(tempFolder, "test2.txt");

        FileUtils.fileWrite(file2, "UTF-8", filename);
        assertTrue(file2.exists());
        assertTrue(file2.length() > 0);

        String file2contents = FileUtils.fileRead(file2, "UTF-8");
        assertEquals(filename, file2contents, "Second file's contents correct");

        assertTrue(file2.delete());

        String contents = FileUtils.fileRead(new File(filename), "UTF-8");
        assertEquals("This is a test", contents, "FileUtils.fileRead()");
    }

    @Test
    public void fileReadWithDefaultEncoding() throws Exception {
        File file = new File(tempFolder, "read.obj");
        FileOutputStream out = new FileOutputStream(file);
        byte[] text = "Hello /u1234".getBytes();
        out.write(text);
        out.close();

        String data = FileUtils.fileRead(file);
        assertEquals("Hello /u1234", data);
    }

    @Test
    public void fileReadWithEncoding() throws Exception {
        File file = new File(tempFolder, "read.obj");
        FileOutputStream out = new FileOutputStream(file);
        byte[] text = "Hello /u1234".getBytes("UTF8");
        out.write(text);
        out.close();

        String data = FileUtils.fileRead(file, "UTF8");
        assertEquals("Hello /u1234", data);
    }

    @Test
    @Disabled("Commons test case that is failing for plexus")
    public void readLines() throws Exception {
        File file = FileTestHelper.newFile(tempFolder, "lines.txt");
        try {
            String[] data = new String[] {"hello", "/u1234", "", "this is", "some text"};
            FileTestHelper.createLineBasedFile(file, data);

            List<String> lines = FileUtils.loadFile(file);
            assertIterableEquals(Arrays.asList(data), lines);
        } finally {
            deleteFile(file);
        }
    }

    @Test
    public void writeStringToFile1() throws Exception {
        File file = new File(tempFolder, "write.txt");
        FileUtils.fileWrite(file, "UTF8", "Hello /u1234");
        byte[] text = "Hello /u1234".getBytes("UTF8");
        assertEqualContent(text, file);
    }

    @Test
    public void writeStringToFile2() throws Exception {
        File file = new File(tempFolder, "write.txt");
        FileUtils.fileWrite(file, null, "Hello /u1234");
        byte[] text = "Hello /u1234".getBytes();
        assertEqualContent(text, file);
    }

    @Test
    public void writeCharSequence1() throws Exception {
        File file = new File(tempFolder, "write.txt");
        FileUtils.fileWrite(file, "UTF8", "Hello /u1234");
        byte[] text = "Hello /u1234".getBytes("UTF8");
        assertEqualContent(text, file);
    }

    @Test
    public void writeCharSequence2() throws Exception {
        File file = new File(tempFolder, "write.txt");
        FileUtils.fileWrite(file, null, "Hello /u1234");
        byte[] text = "Hello /u1234".getBytes();
        assertEqualContent(text, file);
    }

    @Test
    public void writeStringToFileWithEncodingWithAppendOptionTrueShouldNotDeletePreviousFileLines() throws Exception {
        File file = FileTestHelper.newFile(tempFolder, "lines.txt");
        FileUtils.fileWrite(file, null, "This line was there before you...");

        FileUtils.fileAppend(file.getAbsolutePath(), "this is brand new data");

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead(file);
        assertEquals(expected, actual);
    }

    @Test
    public void writeStringToFileWithAppendOptionTrueShouldNotDeletePreviousFileLines() throws Exception {
        File file = FileTestHelper.newFile(tempFolder, "lines.txt");
        FileUtils.fileWrite(file, null, "This line was there before you...");

        FileUtils.fileAppend(file.getAbsolutePath(), "this is brand new data");

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead(file);
        assertEquals(expected, actual);
    }

    @Test
    public void writeStringArrayToFile() throws Exception {
        File file = new File(tempFolder, "writeArray.txt");
        FileUtils.fileWriteArray(file, new String[] {"line1", "line2", "line3"});

        byte[] text = "line1\nline2\nline3".getBytes(StandardCharsets.UTF_8);
        assertEqualContent(text, file);
    }

    @Test
    public void writeStringArrayToFileWithEncoding() throws Exception {
        File file = new File(tempFolder, "writeArray.txt");
        FileUtils.fileWriteArray(file, "UTF8", new String[] {"line1", "line2", "line3"});

        byte[] text = "line1\nline2\nline3".getBytes(StandardCharsets.UTF_8);
        assertEqualContent(text, file);
    }

    @Test
    public void writeWithEncodingWithAppendOptionTrueShouldNotDeletePreviousFileLines() throws Exception {
        File file = FileTestHelper.newFile(tempFolder, "lines.txt");
        FileUtils.fileWrite(file, "UTF-8", "This line was there before you...");

        FileUtils.fileAppend(file.getAbsolutePath(), "UTF-8", "this is brand new data");

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead(file);
        assertEquals(expected, actual);
    }

    @Test
    public void writeWithAppendOptionTrueShouldNotDeletePreviousFileLines() throws Exception {
        File file = FileTestHelper.newFile(tempFolder, "lines.txt");
        FileUtils.fileWrite(file, null, "This line was there before you...");

        FileUtils.fileAppend(file.getAbsolutePath(), "this is brand new data");

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead(file);
        assertEquals(expected, actual);
    }

    @Test
    public void blowUpOnNull() {
        //noinspection DataFlowIssue
        assertThrows(NullPointerException.class, () -> FileUtils.deleteDirectory((File) null));
    }

    @Test
    public void deleteQuietlyDir() throws IOException {
        File testDirectory = new File(tempFolder, "testDeleteQuietlyDir");
        File testFile = new File(testDirectory, "testDeleteQuietlyFile");
        testDirectory.mkdirs();
        createFile(testFile, 0);

        assertTrue(testDirectory.exists());
        assertTrue(testFile.exists());
        FileUtils.deleteDirectory(testDirectory);
        assertFalse(testDirectory.exists(), "Check No Exist");
        assertFalse(testFile.exists(), "Check No Exist");
    }

    @Test
    public void deleteQuietlyFile() throws IOException {
        File testFile = new File(tempFolder, "testDeleteQuietlyFile");
        createFile(testFile, 0);

        assertTrue(testFile.exists());
        FileUtils.deleteDirectory(testFile);
        assertFalse(testFile.exists(), "Check No Exist");
    }

    @Test
    public void deleteQuietlyNonExistent() throws IOException {
        File testFile = new File(tempFolder, "testDeleteQuietlyNonExistent");
        assertFalse(testFile.exists());

        FileUtils.deleteDirectory(testFile);
    }

    ////  getDefaultExcludes

    @Test
    public void getDefaultExcludes() {
        List<String> excludes = Arrays.asList(FileUtils.getDefaultExcludes());
        assertAll(
                "All minimum default excludes should be present",
                Arrays.stream(MINIMUM_DEFAULT_EXCLUDES)
                        .map(exclude -> () -> assertTrue(excludes.contains(exclude), "Missing exclude: " + exclude)));
    }

    //// getDefaultExcludesAsList

    @Test
    public void getDefaultExcludesAsList() {
        List<String> excludes = FileUtils.getDefaultExcludesAsList();
        assertAll(
                "All minimum default excludes should be present",
                Arrays.stream(MINIMUM_DEFAULT_EXCLUDES)
                        .map(exclude -> () -> assertTrue(excludes.contains(exclude), "Missing exclude: " + exclude)));
    }

    //// getDefaultExcludesAsString

    @Test
    public void getDefaultExcludesAsString() {
        HashSet<String> excludes = new HashSet<>(
                Arrays.asList(FileUtils.getDefaultExcludesAsString().split(",")));
        assertAll(
                "All minimum default excludes should be present",
                Arrays.stream(MINIMUM_DEFAULT_EXCLUDES)
                        .map(exclude -> () -> assertTrue(excludes.contains(exclude), "Missing exclude: " + exclude)));
    }

    //// dirname(String)

    @Test
    public void blowUpOnDirnameNull() {
        //noinspection DataFlowIssue
        assertThrows(NullPointerException.class, () -> FileUtils.dirname(null));
    }

    @Test
    public void dirnameEmpty() {
        assertEquals("", FileUtils.dirname(""));
    }

    @Test
    public void dirnameFilename() {
        assertEquals("", FileUtils.dirname("foo.bar.txt"));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void dirnameWindowsRootPathOnUnix() {
        assertEquals("", FileUtils.dirname("C:\\foo.bar.txt"));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void dirnameWindowsNonRootPathOnUnix() {
        assertEquals("", FileUtils.dirname("C:\\test\\foo.bar.txt"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void dirnameUnixRootPathOnWindows() {
        assertEquals("", FileUtils.dirname("/foo.bar.txt"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void dirnameUnixNonRootPathOnWindows() {
        assertEquals("", FileUtils.dirname("/test/foo.bar.txt"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void dirnameWindowsRootPathOnWindows() {
        assertEquals("C:", FileUtils.dirname("C:\\foo.bar.txt"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void dirnameWindowsNonRootPathOnWindows() {
        assertEquals("C:\\test", FileUtils.dirname("C:\\test\\foo.bar.txt"));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void dirnameUnixRootPathOnUnix() {
        assertEquals("", FileUtils.dirname("/foo.bar.txt"));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void dirnameUnixNonRootPathOnUnix() {
        assertEquals("/test", FileUtils.dirname("/test/foo.bar.txt"));
    }

    //// filename(String)

    @Test
    public void blowUpOnFilenameNull() {
        //noinspection DataFlowIssue
        assertThrows(NullPointerException.class, () -> FileUtils.filename(null));
    }

    @Test
    public void filenameEmpty() {
        assertEquals("", FileUtils.filename(""));
    }

    @Test
    public void filenameFilename() {
        assertEquals("foo.bar.txt", FileUtils.filename("foo.bar.txt"));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void filenameWindowsRootPathOnUnix() {
        assertEquals("C:\\foo.bar.txt", FileUtils.filename("C:\\foo.bar.txt"));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void filenameWindowsNonRootPathOnUnix() {
        assertEquals("C:\\test\\foo.bar.txt", FileUtils.filename("C:\\test\\foo.bar.txt"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void filenameUnixRootPathOnWindows() {
        assertEquals("/foo.bar.txt", FileUtils.filename("/foo.bar.txt"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void filenameUnixNonRootPathOnWindows() {
        assertEquals("/test/foo.bar.txt", FileUtils.filename("/test/foo.bar.txt"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void filenameWindowsRootPathOnWindows() {
        assertEquals("foo.bar.txt", FileUtils.filename("C:\\foo.bar.txt"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void filenameWindowsNonRootPathOnWindows() {
        assertEquals("foo.bar.txt", FileUtils.filename("C:\\test\\foo.bar.txt"));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void filenameUnixRootPathOnUnix() {
        assertEquals("foo.bar.txt", FileUtils.filename("/foo.bar.txt"));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void filenameUnixNonRootPathOnUnix() {
        assertEquals("foo.bar.txt", FileUtils.filename("/test/foo.bar.txt"));
    }

    //// extension(String)

    @Test
    public void blowUpOnNullExtension() {
        //noinspection DataFlowIssue
        assertThrows(NullPointerException.class, () -> FileUtils.extension(null));
    }

    @Test
    public void extensionEmpty() {
        assertEquals("", FileUtils.extension(""));
    }

    @Test
    public void extensionFileName() {
        assertEquals("txt", FileUtils.extension("foo.bar.txt"));
    }

    @Test
    public void extensionFileNameNoExtension() {
        assertEquals("", FileUtils.extension("foo_bar_txt"));
    }

    @Test
    // X @ReproducesPlexusBug( "assumes that the path is a local path" )
    @DisabledOnOs(OS.WINDOWS)
    public void extensionWindowsRootPathOnUnix() {
        assertEquals("txt", FileUtils.extension("C:\\foo.bar.txt"));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void extensionWindowsNonRootPathOnUnix() {
        assertEquals("txt", FileUtils.extension("C:\\test\\foo.bar.txt"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void extensionUnixRootPathOnWindows() {
        assertEquals("txt", FileUtils.extension("/foo.bar.txt"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void extensionUnixNonRootPathOnWindows() {
        assertEquals("txt", FileUtils.extension("/test/foo.bar.txt"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void extensionWindowsRootPathOnWindows() {
        assertEquals("txt", FileUtils.extension("C:\\foo.bar.txt"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void extensionWindowsNonRootPathOnWindows() {
        assertEquals("txt", FileUtils.extension("C:\\test\\foo.bar.txt"));
    }

    @Test
    @Disabled("Wait until we can run with assembly 2.5 which will support symlinks properly")
    @DisabledOnOs(OS.WINDOWS)
    public void isASymbolicLink() throws IOException {
        // This testcase will pass when running under java7 or higher
        File file = new File("src/test/resources/symlinks/src/symDir");
        assertTrue(FileUtils.isSymbolicLink(file));
    }

    @Test
    @Disabled("Wait until we can run with assembly 2.5 which will support symlinks properly")
    public void notASymbolicLink() throws IOException {
        File file = new File("src/test/resources/symlinks/src/");
        assertFalse(FileUtils.isSymbolicLink(file));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void extensionUnixRootPathOnUnix() {
        assertEquals("txt", FileUtils.extension("/foo.bar.txt"));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void extensionUnixNonRootPathOnUnix() {
        assertEquals("txt", FileUtils.extension("/test/foo.bar.txt"));
    }

    @Test
    public void createAndReadSymlink() throws Exception {
        assumeFalse(Os.isFamily(Os.FAMILY_WINDOWS));

        File file = new File("target/fzz");
        FileUtils.createSymbolicLink(file, new File("../target"));

        final File file1 = Files.readSymbolicLink(file.toPath()).toFile();
        assertEquals("target", file1.getName());
        Files.delete(file.toPath());
    }

    @Test
    public void createSymbolicLinkWithDifferentTargetOverwritesSymlink() throws Exception {
        assumeFalse(Os.isFamily(Os.FAMILY_WINDOWS));

        // Arrange

        final File symlink1 = new File(tempFolder, "symlink");

        FileUtils.createSymbolicLink(symlink1, testFile1);

        // Act

        final File symlink2 = FileUtils.createSymbolicLink(symlink1, testFile2);

        // Assert

        assertEquals(testFile2, Files.readSymbolicLink(symlink2.toPath()).toFile());
    }

    //// constants for testing

    private static final String[] MINIMUM_DEFAULT_EXCLUDES = {
        // Miscellaneous typical temporary files
        "**/*~",
        "**/#*#",
        "**/.#*",
        "**/%*%",
        "**/._*",

        // CVS
        "**/CVS",
        "**/CVS/**",
        "**/.cvsignore",

        // Subversion
        "**/.svn",
        "**/.svn/**",

        // Arch
        "**/.arch-ids",
        "**/.arch-ids/**",

        // Bazaar
        "**/.bzr",
        "**/.bzr/**",

        // SurroundSCM
        "**/.MySCMServerInfo",

        // Mac
        "**/.DS_Store",

        // Serena Dimensions Version 10
        "**/.metadata",
        "**/.metadata/**",

        // Mercurial
        "**/.hg",
        "**/.hg/**",

        // git
        "**/.git",
        "**/.git/**",

        // BitKeeper
        "**/BitKeeper",
        "**/BitKeeper/**",
        "**/ChangeSet",
        "**/ChangeSet/**",

        // darcs
        "**/_darcs",
        "**/_darcs/**",
        "**/.darcsrepo",
        "**/.darcsrepo/**",
        "**/-darcs-backup*",
        "**/.darcs-temp-mail"
    };

    private static File newFile(File parent, String child) throws IOException {
        File result = new File(parent, child);
        result.createNewFile();
        return result;
    }

    private static File newFolder(File root, String... subDirs) throws IOException {
        String subFolder = String.join("/", subDirs);
        File result = new File(root, subFolder);
        if (!result.mkdirs()) {
            throw new IOException("Couldn't create folders " + root);
        }
        return result;
    }
}
