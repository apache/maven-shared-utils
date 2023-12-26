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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.maven.shared.utils.testhelpers.FileTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.*;

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
    public File tempDir;

    public String name;

    /**
     * Size of test directory.
     */
    private static final int TEST_DIRECTORY_SIZE = 0;

    private File testFile1;

    private File testFile2;

    private long testFile1Size;

    private long testFile2Size;

    @BeforeEach
    void setUp(TestInfo testInfo) throws Exception {
        Optional<Method> testMethod = testInfo.getTestMethod();
        testMethod.ifPresent(method -> this.name = method.getName());
        testFile1 = File.createTempFile("file1-test.txt", null, tempDir);
        testFile2 = File.createTempFile("file1a-test.txt", null, tempDir);

        testFile1Size = (int) testFile1.length();
        testFile2Size = (int) testFile2.length();

        tempDir.mkdirs();
        createFile(testFile1, testFile1Size);
        createFile(testFile2, testFile2Size);
        FileUtils.deleteDirectory(tempDir);
        tempDir.mkdirs();
        createFile(testFile1, testFile1Size);
        createFile(testFile2, testFile2Size);
    }

    private static void createFile(File file, long size) throws IOException {
        if (!file.getParentFile().exists()) {
            throw new IOException("Cannot create file " + file + " as the parent directory does not exist");
        }

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            FileTestHelper.generateTestData(out, size);
        }
    }

    /**
     * Assert that the content of a file is equal to that in a byte[].
     */
    private void assertEqualContent(byte[] b0, File file) throws IOException {
        int count = 0, numRead = 0;
        byte[] b1 = new byte[b0.length];
        try (InputStream is = new FileInputStream(file)) {
            while (count < b0.length && numRead >= 0) {
                numRead = is.read(b1, count, b0.length);
                count += numRead;
            }
            assertThat(count).as("Different number of bytes: ").isEqualTo(b0.length);
            for (int i = 0; i < count; i++) {
                assertEquals(b1[i], b0[i], "byte " + i + " differs");
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
    void toFile1() throws Exception {
        URL url = new URL("file", null, "a/b/c/file.txt");
        File file = FileUtils.toFile(url);
        assertThat(file.toString()).contains("file.txt");
    }

    @Test
    void toFile2() throws Exception {
        URL url = new URL("file", null, "a/b/c/file%20n%61me%2520.tx%74");
        File file = FileUtils.toFile(url);
        assertThat(file.toString()).contains("file name%20.txt");
    }

    @Test
    void toFile3() throws Exception {
        assertThat(FileUtils.toFile(null)).isNull();
        assertThat(FileUtils.toFile(new URL("http://jakarta.apache.org"))).isNull();
    }

    @Test
    void toFile4() throws Exception {
        assertThrows(NumberFormatException.class, () -> {
            URL url = new URL("file", null, "a/b/c/file%%20%me.txt%");
            File file = FileUtils.toFile(url);
            assertThat(file.toString()).contains("file% %me.txt%");
        });
    }

    /**
     * IO-252
     */
    @Test
    void toFile5() throws Exception {
        URL url = new URL("file", null, "both%20are%20100%20%25%20true");
        File file = FileUtils.toFile(url);
        assertThat(file.toString()).isEqualTo("both are 100 % true");
    }

    @Test
    void toFileUtf8() throws Exception {
        URL url = new URL("file", null, "/home/%C3%A4%C3%B6%C3%BC%C3%9F");
        File file = FileUtils.toFile(url);
        assertThat(file.toString()).doesNotContain("\u00E4\u00F6\u00FC\u00DF");
    }

    // toURLs

    @Test
    void toURLs1() throws Exception {
        File[] files = new File[] {
            new File(tempDir, "file1.txt"), new File(tempDir, "file2.txt"), new File(tempDir, "test file.txt"),
        };
        URL[] urls = FileUtils.toURLs(files);

        assertThat(urls).hasSize(files.length);
        assertThat(urls[0].toExternalForm()).startsWith("file:");
        assertThat(urls[0].toExternalForm()).contains("file1.txt");
        assertThat(urls[1].toExternalForm()).startsWith("file:");
        assertThat(urls[1].toExternalForm()).contains("file2.txt");

        // Test escaped char
        assertThat(urls[2].toExternalForm()).startsWith("file:");
        assertThat(urls[2].toExternalForm()).contains("test%20file.txt");
    }

    // contentEquals

    @Test
    void contentEquals() throws Exception {
        // Non-existent files
        File file = new File(tempDir, name);
        File file2 = new File(tempDir, name + "2");
        // both don't  exist
        assertThat(FileUtils.contentEquals(file, file)).isEqualTo(true);
        assertThat(FileUtils.contentEquals(file, file2)).isEqualTo(true);
        assertThat(FileUtils.contentEquals(file2, file2)).isEqualTo(true);
        assertThat(FileUtils.contentEquals(file2, file)).isEqualTo(true);

        // Directories
        FileUtils.contentEquals(tempDir, tempDir);

        // Different files
        File objFile1 = new File(tempDir, name + ".object");
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile(getClass().getResource("/java/lang/Object.class"), objFile1);

        File objFile1b = new File(tempDir, name + ".object2");
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile(getClass().getResource("/java/lang/Object.class"), objFile1b);

        File objFile2 = new File(tempDir, name + ".collection");
        objFile2.deleteOnExit();
        FileUtils.copyURLToFile(getClass().getResource("/java/util/Collection.class"), objFile2);

        assertThat(FileUtils.contentEquals(objFile1, objFile2)).isEqualTo(false);
        assertThat(FileUtils.contentEquals(objFile1b, objFile2)).isEqualTo(false);
        assertThat(FileUtils.contentEquals(objFile1, objFile1b)).isEqualTo(true);

        assertThat(FileUtils.contentEquals(objFile1, objFile1)).isEqualTo(true);
        assertThat(FileUtils.contentEquals(objFile1b, objFile1b)).isEqualTo(true);
        assertThat(FileUtils.contentEquals(objFile2, objFile2)).isEqualTo(true);

        // Equal files
        file.createNewFile();
        file2.createNewFile();
        assertThat(FileUtils.contentEquals(file, file)).isEqualTo(true);
        assertThat(FileUtils.contentEquals(file, file2)).isEqualTo(true);
    }

    // copyURLToFile

    @Test
    void copyURLToFile() throws Exception {
        // Creates file
        File file = new File(tempDir, name);
        file.deleteOnExit();

        // Loads resource
        String resourceName = "/java/lang/Object.class";
        FileUtils.copyURLToFile(getClass().getResource(resourceName), file);

        // Tests that resource was copied correctly
        try (FileInputStream fis = new FileInputStream(file)) {
            assertTrue(
                    IOUtil.contentEquals(getClass().getResourceAsStream(resourceName), fis), "Content is not equal.");
        }
        // TODO Maybe test copy to itself like for copyFile()
    }

    // forceMkdir

    @Test
    void forceMkdir() throws Exception {
        // Tests with existing directory
        FileUtils.forceMkdir(tempDir);

        // Creates test file
        File testFile = new File(tempDir, name);
        testFile.deleteOnExit();
        testFile.createNewFile();
        assertThat(testFile).as("Test file does not exist.").exists();

        // Tests with existing file
        assertThrows(IOException.class, () -> FileUtils.forceMkdir(testFile));

        testFile.delete();

        // Tests with non-existent directory
        FileUtils.forceMkdir(testFile);
        assertThat(testFile).as("Directory was not created.").exists();
    }

    // sizeOfDirectory

    @Test
    void sizeOfDirectory() throws Exception {
        File file = new File(tempDir, name);

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

        assertThat(FileUtils.sizeOfDirectory(file))
                .as("Unexpected directory size")
                .isEqualTo(TEST_DIRECTORY_SIZE);
    }

    // copyFile

    @Test
    void copyFile1() throws Exception {
        File destination = new File(tempDir, "copy1.txt");

        // Thread.sleep(LAST_MODIFIED_DELAY);
        // This is to slow things down so we can catch if
        // the lastModified date is not ok

        FileUtils.copyFile(testFile1, destination);
        assertThat(destination).as("Check Exist").exists();
        assertThat(destination).as("Check Full copy").hasSize(testFile1Size);
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/
    }

    /** A time today, rounded down to the previous minute */
    private static long MODIFIED_TODAY =
            (System.currentTimeMillis() / TimeUnit.MINUTES.toMillis(1)) * TimeUnit.MINUTES.toMillis(1);

    /** A time yesterday, rounded down to the previous minute */
    private static long MODIFIED_YESTERDAY = MODIFIED_TODAY - TimeUnit.DAYS.toMillis(1);

    /** A time last week, rounded down to the previous minute */
    private static long MODIFIED_LAST_WEEK = MODIFIED_TODAY - TimeUnit.DAYS.toMillis(7);

    @Test
    void copyFileWithNoFiltersAndNoDestination() throws Exception {
        File from = write("from.txt", MODIFIED_YESTERDAY, "Hello World!");
        File to = new File(tempDir, "to.txt");

        FileUtils.copyFile(from, to, null, (FileUtils.FilterWrapper[]) null);

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt did not exist so should have been written");
        assertThat(to).hasContent("Hello World!");
    }

    @Test
    void copyFileWithNoFiltersAndLastModifiedDateOfZeroAndNoDestination() throws Exception {
        File from = write("from.txt", MODIFIED_YESTERDAY, "Hello World!");
        File to = new File(tempDir, "to.txt");

        from.setLastModified(0);
        FileUtils.copyFile(from, to, null, (FileUtils.FilterWrapper[]) null);

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt did not exist so should have been written");
        assertThat(to).hasContent("Hello World!");
    }

    @Test
    void copyFileWithNoFiltersAndOutdatedDestination() throws Exception {
        File from = write("from.txt", MODIFIED_YESTERDAY, "Hello World!");
        File to = write("to.txt", MODIFIED_LAST_WEEK, "Older content");

        FileUtils.copyFile(from, to, null, (FileUtils.FilterWrapper[]) null);

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt was outdated so should have been overwritten");
        assertThat(to).hasContent("Hello World!");
    }

    @Test
    void copyFileWithNoFiltersAndNewerDestination() throws Exception {
        File from = write("from.txt", MODIFIED_LAST_WEEK, "Hello World!");
        File to = write("to.txt", MODIFIED_YESTERDAY, "Older content");

        FileUtils.copyFile(from, to, null, (FileUtils.FilterWrapper[]) null);

        assertTrue(to.lastModified() < MODIFIED_TODAY, "to.txt was newer so should have been left alone");
        assertThat(to).hasContent("Older content");
    }

    @Test
    void copyFileWithNoFiltersAndNewerDestinationButForcedOverwrite() throws Exception {
        File from = write("from.txt", MODIFIED_LAST_WEEK, "Hello World!");
        File to = write("to.txt", MODIFIED_YESTERDAY, "Older content");

        FileUtils.copyFile(from, to, null, null, true);

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt was newer but the overwrite should have been forced");
        assertThat(to).hasContent("Hello World!");
    }

    @Test
    void copyFileWithFilteringButNoFilters() throws Exception {
        File from = write("from.txt", MODIFIED_YESTERDAY, "Hello ${name}!");
        File to = write("to.txt", MODIFIED_LAST_WEEK, "Older content");

        FileUtils.copyFile(from, to, null);

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt was outdated so should have been overwritten");
        assertThat(to).hasContent("Hello ${name}!");
    }

    @Test
    void copyFileWithFilteringAndNoDestination() throws Exception {
        File from = write("from.txt", MODIFIED_YESTERDAY, "Hello ${name}!");
        File to = new File(tempDir, "to.txt");

        FileUtils.copyFile(from, to, null, wrappers());

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt did not exist so should have been written");
        assertThat(to).hasContent("Hello Bob!");
    }

    @Test
    void copyFileWithFilteringAndOutdatedDestination() throws Exception {
        File from = write("from.txt", MODIFIED_YESTERDAY, "Hello ${name}!");
        File to = write("to.txt", MODIFIED_LAST_WEEK, "Older content");

        FileUtils.copyFile(from, to, null, wrappers());

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt was outdated so should have been overwritten");
        assertThat(to).hasContent("Hello Bob!");
    }

    @Test
    void copyFileWithFilteringAndNewerDestinationButForcedOverwrite() throws Exception {
        File from = write("from.txt", MODIFIED_LAST_WEEK, "Hello ${name}!");
        File to = write("to.txt", MODIFIED_YESTERDAY, "Older content");

        FileUtils.copyFile(from, to, null, wrappers(), true);

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt was newer but the overwrite should have been forced");
        assertThat(to).hasContent("Hello Bob!");
    }

    @Test
    void copyFileWithFilteringAndNewerDestinationButModifiedContent() throws Exception {
        File from = write("from.txt", MODIFIED_LAST_WEEK, "Hello ${name}!");
        File to = write("to.txt", MODIFIED_YESTERDAY, "Hello Charlie!");

        FileUtils.copyFile(from, to, null, wrappers());

        assertTrue(to.lastModified() >= MODIFIED_TODAY, "to.txt was outdated so should have been overwritten");
        assertThat(to).hasContent("Hello Bob!");
    }

    @Test
    void copyFileWithFilteringAndNewerDestinationAndMatchingContent() throws Exception {
        File from = write("from.txt", MODIFIED_LAST_WEEK, "Hello ${name}!");
        File to = write("to.txt", MODIFIED_YESTERDAY, "Hello Bob!");

        FileUtils.copyFile(from, to, null, wrappers());

        assertTrue(to.lastModified() < MODIFIED_TODAY, "to.txt content should be unchanged and have been left alone");
        assertThat(to).hasContent("Hello Bob!");
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
        final File file = new File(tempDir, name);
        try (final Writer writer = new FileWriter(file)) {
            writer.write(text);
        }
        assertTrue(file.setLastModified(lastModified));
        assertEquals(lastModified, file.lastModified(), "Failed to set lastModified date on " + file.getPath());
        return file;
    }

    @Test
    @DisabledOnOs(WINDOWS)
    void copyFileThatIsSymlink() throws Exception {
        File destination = new File(tempDir, "symCopy.txt");

        File testDir = SymlinkTestSetup.createStandardSymlinkTestDir(new File("target/test/symlinkCopy"));

        FileUtils.copyFile(new File(testDir, "symR"), destination);

        assertTrue(Files.isSymbolicLink(destination.toPath()));
    }

    @Test
    void deleteFile() throws Exception {
        File destination = new File(tempDir, "copy1.txt");
        FileUtils.copyFile(testFile1, destination);
        FileUtils.delete(destination);
        assertThat(destination).as("Check Exist").doesNotExist();
    }

    @Test
    void deleteFileNofile() throws Exception {
        assertThrows(IOException.class, () -> {
            File destination = new File("abc/cde");
            FileUtils.delete(destination);
        });
    }

    @Test
    void deleteFileLegacy() throws Exception {
        File destination = new File(tempDir, "copy1.txt");
        FileUtils.copyFile(testFile1, destination);
        assertTrue(FileUtils.deleteLegacyStyle(destination));
    }

    @Test
    void deleteFileLegacyNofile() throws Exception {
        File destination = new File("abc/cde");
        assertFalse(FileUtils.deleteLegacyStyle(destination));
    }

    @Test
    void copyFileWithPermissions() throws Exception {
        File source = new File("src/test/resources/executable");
        source.setExecutable(true);
        assertThat(source).as("Need an existing file to copy").exists();
        assertTrue(source.canExecute(), "Need an executable file to copy");

        File destination = new File(tempDir, "executable-copy");

        FileUtils.copyFile(source, destination);

        assertThat(destination)
                .as("destination not exists: " + destination.getAbsolutePath() + ", directory content: "
                        + Arrays.asList(destination.getParentFile().list()))
                .exists();

        assertThat(destination.canExecute()).as("Check copy executable").isEqualTo(true);
    }

    @Test
    void copyFile2() throws Exception {
        File destination = new File(tempDir, "copy2.txt");

        // Thread.sleep(LAST_MODIFIED_DELAY);
        // This is to slow things down so we can catch if
        // the lastModified date is not ok

        FileUtils.copyFile(testFile1, destination);
        assertThat(destination).as("Check Exist").exists();
        assertThat(destination).as("Check Full copy").hasSize(testFile2Size);
        /* disabled: Thread.sleep doesn't work reliably for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/
    }

    @Test
    void copyToSelf() throws IOException {
        File destination = new File(tempDir, "copy3.txt");
        // Prepare a test file
        FileUtils.copyFile(testFile1, destination);

        FileUtils.copyFile(destination, destination);
    }

    @Test
    void copyDirectoryToNonExistingDest() throws Exception {
        createFile(testFile1, 1234);
        createFile(testFile2, 4321);
        File srcDir = tempDir;
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
    void copyDirectoryToExistingDest() throws IOException {
        createFile(testFile1, 1234);
        createFile(testFile2, 4321);
        File srcDir = tempDir;
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
    void copyDirectoryErrors_nullDestination() {
        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(new File("a"), null));
    }

    @Test
    void copyDirectoryErrors_copyToSelf() {
        assertThrows(IOException.class, () -> FileUtils.copyDirectory(tempDir, tempDir));
    }

    @Test
    void copyDirectoryErrors() {
        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(null, null));

        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(null, new File("a")));

        assertThrows(IOException.class, () -> FileUtils.copyDirectory(tempDir, testFile1));
    }

    // forceDelete

    @Test
    void forceDeleteAFile1() throws Exception {
        File destination = new File(tempDir, "copy1.txt");
        destination.createNewFile();
        assertThat(destination).as("Copy1.txt doesn't exist to delete").exists();
        FileUtils.forceDelete(destination);
        assertThat(destination).doesNotExist();
    }

    @Test
    void forceDeleteAFile2() throws Exception {
        File destination = new File(tempDir, "copy2.txt");
        destination.createNewFile();
        assertThat(destination).as("Copy2.txt doesn't exist to delete").exists();
        FileUtils.forceDelete(destination);
        assertThat(destination).as("Check No Exist").doesNotExist();
    }

    @Test
    @Disabled("Commons test case that is failing for plexus")
    void forceDeleteAFile3() throws Exception {
        File destination = new File(tempDir, "no_such_file");
        assertThat(destination).as("Check No Exist").doesNotExist();
        assertThrows(FileNotFoundException.class, () -> FileUtils.forceDelete(destination));
    }

    // copyFileToDirectory

    @Test
    @Disabled("Commons test case that is failing for plexus")
    void copyFile1ToDir() throws Exception {
        File directory = new File(tempDir, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File destination = new File(directory, testFile1.getName());

        // Thread.sleep(LAST_MODIFIED_DELAY);
        // This is to slow things down so we can catch if
        // the lastModified date is not ok

        FileUtils.copyFileToDirectory(testFile1, directory);
        assertThat(destination.exists()).as("Check Exist").isEqualTo(true);
        assertThat(destination.length()).as("Check Full copy").isEqualTo(testFile1Size);
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
    void copyFile2ToDir() throws Exception {
        File directory = new File(tempDir, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File destination = new File(directory, testFile1.getName());

        // Thread.sleep(LAST_MODIFIED_DELAY);
        // This is to slow things down so we can catch if
        // the lastModified date is not ok

        FileUtils.copyFileToDirectory(testFile1, directory);
        assertThat(destination.exists()).as("Check Exist").isEqualTo(true);
        assertThat(destination.length()).as("Check Full copy").isEqualTo(testFile2Size);
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/
    }

    // forceDelete

    @Test
    void forceDeleteDir() throws Exception {
        File testDirectory = newFolder(tempDir, name);
        FileUtils.forceDelete(testDirectory.getParentFile());
        assertThat(!testDirectory.getParentFile().exists()).as("Check No Exist").isEqualTo(true);
    }

    /**
     * Test the FileUtils implementation.
     */
    @Test
    void fileUtils() throws Exception {
        // Loads file from classpath
        File file1 = new File(tempDir, "test.txt");
        String filename = file1.getAbsolutePath();

        // Create test file on-the-fly
        try (OutputStream out = new java.io.FileOutputStream(file1)) {
            out.write("This is a test".getBytes("UTF-8"));
        }

        File file2 = new File(tempDir, "test2.txt");

        FileUtils.fileWrite(file2, "UTF-8", filename);
        assertThat(file2.exists()).isEqualTo(true);
        assertThat(file2.length() > 0).isEqualTo(true);

        String file2contents = FileUtils.fileRead(file2, "UTF-8");
        assertThat(filename.equals(file2contents))
                .as("Second file's contents correct")
                .isEqualTo(true);

        assertThat(file2.delete()).isEqualTo(true);

        String contents = FileUtils.fileRead(new File(filename), "UTF-8");
        assertThat(contents.equals("This is a test")).as("FileUtils.fileRead()").isEqualTo(true);
    }

    @Test
    void fileReadWithDefaultEncoding() throws Exception {
        File file = new File(tempDir, "read.obj");
        FileOutputStream out = new FileOutputStream(file);
        byte[] text = "Hello /u1234".getBytes();
        out.write(text);
        out.close();

        String data = FileUtils.fileRead(file);
        assertThat(data).isEqualTo("Hello /u1234");
    }

    @Test
    void fileReadWithEncoding() throws Exception {
        File file = new File(tempDir, "read.obj");
        FileOutputStream out = new FileOutputStream(file);
        byte[] text = "Hello /u1234".getBytes("UTF8");
        out.write(text);
        out.close();

        String data = FileUtils.fileRead(file, "UTF8");
        assertThat(data).isEqualTo("Hello /u1234");
    }

    @Test
    @Disabled("Commons test case that is failing for plexus")
    void readLines() throws Exception {
        File file = FileTestHelper.newFile(tempDir, "lines.txt");
        try {
            String[] data = new String[] {"hello", "/u1234", "", "this is", "some text"};
            FileTestHelper.createLineBasedFile(file, data);

            List<String> lines = FileUtils.loadFile(file);
            assertThat(lines).isEqualTo(Arrays.asList(data));
        } finally {
            deleteFile(file);
        }
    }

    @Test
    void writeStringToFile1() throws Exception {
        File file = new File(tempDir, "write.txt");
        FileUtils.fileWrite(file, "UTF8", "Hello /u1234");
        byte[] text = "Hello /u1234".getBytes("UTF8");
        assertEqualContent(text, file);
    }

    @Test
    void writeStringToFile2() throws Exception {
        File file = new File(tempDir, "write.txt");
        FileUtils.fileWrite(file, null, "Hello /u1234");
        byte[] text = "Hello /u1234".getBytes();
        assertEqualContent(text, file);
    }

    @Test
    void writeCharSequence1() throws Exception {
        File file = new File(tempDir, "write.txt");
        FileUtils.fileWrite(file, "UTF8", "Hello /u1234");
        byte[] text = "Hello /u1234".getBytes("UTF8");
        assertEqualContent(text, file);
    }

    @Test
    void writeCharSequence2() throws Exception {
        File file = new File(tempDir, "write.txt");
        FileUtils.fileWrite(file, null, "Hello /u1234");
        byte[] text = "Hello /u1234".getBytes();
        assertEqualContent(text, file);
    }

    @Test
    void writeStringToFileWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        File file = FileTestHelper.newFile(tempDir, "lines.txt");
        FileUtils.fileWrite(file, null, "This line was there before you...");

        FileUtils.fileAppend(file.getAbsolutePath(), "this is brand new data");

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void writeStringToFile_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        File file = FileTestHelper.newFile(tempDir, "lines.txt");
        FileUtils.fileWrite(file, null, "This line was there before you...");

        FileUtils.fileAppend(file.getAbsolutePath(), "this is brand new data");

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void writeStringArrayToFile() throws Exception {
        File file = new File(tempDir, "writeArray.txt");
        FileUtils.fileWriteArray(file, new String[] {"line1", "line2", "line3"});

        byte[] text = "line1\nline2\nline3".getBytes("UTF8");
        assertEqualContent(text, file);
    }

    @Test
    void writeStringArrayToFileWithEncoding() throws Exception {
        File file = new File(tempDir, "writeArray.txt");
        FileUtils.fileWriteArray(file, "UTF8", new String[] {"line1", "line2", "line3"});

        byte[] text = "line1\nline2\nline3".getBytes("UTF8");
        assertEqualContent(text, file);
    }

    @Test
    void writeWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        File file = FileTestHelper.newFile(tempDir, "lines.txt");
        FileUtils.fileWrite(file, "UTF-8", "This line was there before you...");

        FileUtils.fileAppend(file.getAbsolutePath(), "UTF-8", "this is brand new data");

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void write_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        File file = FileTestHelper.newFile(tempDir, "lines.txt");
        FileUtils.fileWrite(file, null, "This line was there before you...");

        FileUtils.fileAppend(file.getAbsolutePath(), "this is brand new data");

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void blowUpOnNull() throws IOException {
        assertThrows(NullPointerException.class, () -> FileUtils.deleteDirectory((File) null));
    }

    @Test
    void deleteQuietlyDir() throws IOException {
        File testDirectory = new File(tempDir, "testDeleteQuietlyDir");
        File testFile = new File(testDirectory, "testDeleteQuietlyFile");
        testDirectory.mkdirs();
        createFile(testFile, 0);

        assertThat(testDirectory.exists()).isEqualTo(true);
        assertThat(testFile.exists()).isEqualTo(true);
        FileUtils.deleteDirectory(testDirectory);
        assertThat(testDirectory.exists()).as("Check No Exist").isEqualTo(false);
        assertThat(testFile.exists()).as("Check No Exist").isEqualTo(false);
    }

    @Test
    void deleteQuietlyFile() throws IOException {
        File testFile = new File(tempDir, "testDeleteQuietlyFile");
        createFile(testFile, 0);

        assertThat(testFile.exists()).isEqualTo(true);
        FileUtils.deleteDirectory(testFile);
        assertThat(testFile.exists()).as("Check No Exist").isEqualTo(false);
    }

    @Test
    void deleteQuietlyNonExistent() throws IOException {
        File testFile = new File(tempDir, "testDeleteQuietlyNonExistent");
        assertThat(testFile.exists()).isEqualTo(false);

        FileUtils.deleteDirectory(testFile);
    }

    ////  getDefaultExcludes

    @Test
    void getDefaultExcludes() throws Exception {
        assertThat(Arrays.asList(FileUtils.getDefaultExcludes())).contains(MINIMUM_DEFAULT_EXCLUDES);
    }

    //// getDefaultExcludesAsList

    @Test
    void getDefaultExcludesAsList() throws Exception {
        assertThat(FileUtils.getDefaultExcludesAsList()).contains(MINIMUM_DEFAULT_EXCLUDES);
    }

    //// getDefaultExcludesAsString

    @Test
    void getDefaultExcludesAsString() throws Exception {
        assertThat(new HashSet<>(
                        Arrays.asList(FileUtils.getDefaultExcludesAsString().split(","))))
                .contains(MINIMUM_DEFAULT_EXCLUDES);
    }

    //// dirname(String)

    @Test
    void blowUpOnDirnameNull() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.dirname(null));
    }

    @Test
    void dirnameEmpty() throws Exception {
        assertThat(FileUtils.dirname("")).isEqualTo("");
    }

    @Test
    void dirnameFilename() throws Exception {
        assertThat(FileUtils.dirname("foo.bar.txt")).isEqualTo("");
    }

    // X @ReproducesPlexusBug( "assumes that the path is a local path" )
    @Test
    @DisabledOnOs(WINDOWS)
    void dirnameWindowsRootPathOnUnix() throws Exception {
        assertThat(FileUtils.dirname("C:\\foo.bar.txt")).isEqualTo("");
    }

    // X @ReproducesPlexusBug( "assumes that the path is a local path" )
    @Test
    @DisabledOnOs(WINDOWS)
    void dirnameWindowsNonRootPathOnUnix() throws Exception {
        assertThat(FileUtils.dirname("C:\\test\\foo.bar.txt")).isEqualTo("");
    }

    // X @ReproducesPlexusBug( "assumes that the path is a local path" )
    @Test
    @EnabledOnOs(WINDOWS)
    void dirnameUnixRootPathOnWindows() throws Exception {
        assertThat(FileUtils.dirname("/foo.bar.txt")).isEqualTo("");
    }

    // X @ReproducesPlexusBug( "assumes that the path is a local path" )
    @Test
    @EnabledOnOs(WINDOWS)
    void dirnameUnixNonRootPathOnWindows() throws Exception {
        assertThat(FileUtils.dirname("/test/foo.bar.txt")).isEqualTo("");
    }

    @Test
    @EnabledOnOs(WINDOWS)
    void dirnameWindowsRootPathOnWindows() throws Exception {
        assertThat(FileUtils.dirname("C:\\foo.bar.txt")).isEqualTo("C:");
    }

    @Test
    @EnabledOnOs(WINDOWS)
    void dirnameWindowsNonRootPathOnWindows() throws Exception {
        assertThat(FileUtils.dirname("C:\\test\\foo.bar.txt")).isEqualTo("C:\\test");
    }

    @Test
    @DisabledOnOs(WINDOWS)
    void dirnameUnixRootPathOnUnix() throws Exception {
        assertThat(FileUtils.dirname("/foo.bar.txt")).isEqualTo("");
    }

    @Test
    @DisabledOnOs(WINDOWS)
    void dirnameUnixNonRootPathOnUnix() throws Exception {
        assertThat(FileUtils.dirname("/test/foo.bar.txt")).isEqualTo("/test");
    }

    //// filename(String)

    @Test
    void blowUpOnFilenameNull() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.filename(null));
    }

    @Test
    void filenameEmpty() throws Exception {
        assertThat(FileUtils.filename("")).isEqualTo("");
    }

    @Test
    void filenameFilename() throws Exception {
        assertThat(FileUtils.filename("foo.bar.txt")).isEqualTo("foo.bar.txt");
    }

    // X @ReproducesPlexusBug( "assumes that the path is a local path" )
    @Test
    @DisabledOnOs(WINDOWS)
    void filenameWindowsRootPathOnUnix() throws Exception {
        assertThat(FileUtils.filename("C:\\foo.bar.txt")).isEqualTo("C:\\foo.bar.txt");
    }

    // X @ReproducesPlexusBug( "assumes that the path is a local path" )
    @Test
    @DisabledOnOs(WINDOWS)
    void filenameWindowsNonRootPathOnUnix() throws Exception {
        assertThat(FileUtils.filename("C:\\test\\foo.bar.txt")).isEqualTo("C:\\test\\foo.bar.txt");
    }

    // X @ReproducesPlexusBug( "assumes that the path is a local path" )
    @Test
    @EnabledOnOs(WINDOWS)
    void filenameUnixRootPathOnWindows() throws Exception {
        assertThat(FileUtils.filename("/foo.bar.txt")).isEqualTo("/foo.bar.txt");
    }

    // X @ReproducesPlexusBug( "assumes that the path is a local path" )
    @Test
    @EnabledOnOs(WINDOWS)
    void filenameUnixNonRootPathOnWindows() throws Exception {
        assertThat(FileUtils.filename("/test/foo.bar.txt")).isEqualTo("/test/foo.bar.txt");
    }

    @Test
    @EnabledOnOs(WINDOWS)
    void filenameWindowsRootPathOnWindows() throws Exception {
        assertThat(FileUtils.filename("C:\\foo.bar.txt")).isEqualTo("foo.bar.txt");
    }

    @Test
    @EnabledOnOs(WINDOWS)
    void filenameWindowsNonRootPathOnWindows() throws Exception {
        assertThat(FileUtils.filename("C:\\test\\foo.bar.txt")).isEqualTo("foo.bar.txt");
    }

    @Test
    @DisabledOnOs(WINDOWS)
    void filenameUnixRootPathOnUnix() throws Exception {
        assertThat(FileUtils.filename("/foo.bar.txt")).isEqualTo("foo.bar.txt");
    }

    @Test
    @DisabledOnOs(WINDOWS)
    void filenameUnixNonRootPathOnUnix() throws Exception {
        assertThat(FileUtils.filename("/test/foo.bar.txt")).isEqualTo("foo.bar.txt");
    }

    //// extension(String)

    @Test
    void blowUpOnNullExtension() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.extension(null));
    }

    @Test
    void extensionEmpty() throws Exception {
        assertThat(FileUtils.extension("")).isEqualTo("");
    }

    @Test
    void extensionFileName() throws Exception {
        assertThat(FileUtils.extension("foo.bar.txt")).isEqualTo("txt");
    }

    @Test
    void extensionFileNameNoExtension() throws Exception {
        assertThat(FileUtils.extension("foo_bar_txt")).isEqualTo("");
    }

    // X @ReproducesPlexusBug( "assumes that the path is a local path" )
    @Test
    @DisabledOnOs(WINDOWS)
    void extensionWindowsRootPathOnUnix() throws Exception {
        assertThat(FileUtils.extension("C:\\foo.bar.txt")).isEqualTo("txt");
    }

    // X @ReproducesPlexusBug( "assumes that the path is a local path" )
    @Test
    @DisabledOnOs(WINDOWS)
    void extensionWindowsNonRootPathOnUnix() throws Exception {
        assertThat(FileUtils.extension("C:\\test\\foo.bar.txt")).isEqualTo("txt");
    }

    // X @ReproducesPlexusBug( "assumes that the path is a local path" )
    @Test
    @EnabledOnOs(WINDOWS)
    void extensionUnixRootPathOnWindows() throws Exception {
        assertThat(FileUtils.extension("/foo.bar.txt")).isEqualTo("txt");
    }

    // X @ReproducesPlexusBug( "assumes that the path is a local path" )
    @Test
    @EnabledOnOs(WINDOWS)
    void extensionUnixNonRootPathOnWindows() throws Exception {
        assertThat(FileUtils.extension("/test/foo.bar.txt")).isEqualTo("txt");
    }

    @Test
    @EnabledOnOs(WINDOWS)
    void extensionWindowsRootPathOnWindows() throws Exception {
        assertThat(FileUtils.extension("C:\\foo.bar.txt")).isEqualTo("txt");
    }

    @Test
    @EnabledOnOs(WINDOWS)
    void extensionWindowsNonRootPathOnWindows() throws Exception {
        assertThat(FileUtils.extension("C:\\test\\foo.bar.txt")).isEqualTo("txt");
    }

    @Test
    @Disabled("Wait until we can run with assembly 2.5 which will support symlinks properly")
    @DisabledOnOs(WINDOWS)
    void isASymbolicLink() throws IOException {
        File file = new File("src/test/resources/symlinks/src/symDir");
        assertTrue(FileUtils.isSymbolicLink(file));
    }

    @Test
    @Disabled("Wait until we can run with assembly 2.5 which will support symlinks properly")
    void notASymbolicLink() throws IOException {
        File file = new File("src/test/resources/symlinks/src/");
        assertFalse(FileUtils.isSymbolicLink(file));
    }

    @Test
    @DisabledOnOs(WINDOWS)
    void extensionUnixRootPathOnUnix() throws Exception {
        assertThat(FileUtils.extension("/foo.bar.txt")).isEqualTo("txt");
    }

    @Test
    @DisabledOnOs(WINDOWS)
    void extensionUnixNonRootPathOnUnix() throws Exception {
        assertThat(FileUtils.extension("/test/foo.bar.txt")).isEqualTo("txt");
    }

    @Test
    @DisabledOnOs(WINDOWS)
    void createAndReadSymlink() throws Exception {
        File file = new File("target/fzz");
        FileUtils.createSymbolicLink(file, new File("../target"));

        final File file1 = Files.readSymbolicLink(file.toPath()).toFile();
        assertEquals("target", file1.getName());
        Files.delete(file.toPath());
    }

    @Test
    @DisabledOnOs(WINDOWS)
    void createSymbolicLinkWithDifferentTargetOverwritesSymlink() throws Exception {
        // Arrange
        final File symlink1 = new File(tempDir, "symlink");

        FileUtils.createSymbolicLink(symlink1, testFile1);

        // Act

        final File symlink2 = FileUtils.createSymbolicLink(symlink1, testFile2);

        // Assert

        assertThat(Files.readSymbolicLink(symlink2.toPath()).toFile()).isEqualTo(testFile2);
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

    private static File newFolder(File root, String... subDirs) throws IOException {
        String subFolder = String.join("/", subDirs);
        File result = new File(root, subFolder);
        if (!result.mkdirs()) {
            throw new IOException("Couldn't create folders " + root);
        }
        return result;
    }
}
