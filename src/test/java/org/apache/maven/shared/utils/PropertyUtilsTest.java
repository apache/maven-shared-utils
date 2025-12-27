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
package org.apache.maven.shared.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PropertyUtilsTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface NeedsTemporaryFolder {}

    @TempDir
    private File tempFolder;

    @Test
    @SuppressWarnings("deprecation")
    // @ReproducesPlexusBug( "Should return null on error like url and file do" )
    public void loadNullInputStream() {
        assertEquals(new Properties(), PropertyUtils.loadProperties((InputStream) null));
    }

    @Test
    public void loadOptionalNullInputStream() {
        assertEquals(new Properties(), PropertyUtils.loadOptionalProperties((InputStream) null));
    }

    @Test
    public void loadOptionalPropertiesIoException() throws Exception {
        URL url = new URL("https://nonesuch12344.foo.bar.com");
        assertEquals(new Properties(), PropertyUtils.loadOptionalProperties(url));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void loadNullURL() {
        assertNull(PropertyUtils.loadProperties((URL) null));
    }

    @Test
    public void loadOptionalNullURL() {
        assertEquals(new Properties(), PropertyUtils.loadOptionalProperties((URL) null));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void loadNullFile() {
        assertNull(PropertyUtils.loadProperties((File) null));
    }

    @Test
    public void loadOptionalNullFile() {
        assertEquals(new Properties(), PropertyUtils.loadOptionalProperties((File) null));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void loadEmptyInputStream() {
        assertEquals(new Properties(), PropertyUtils.loadProperties(new ByteArrayInputStream(new byte[0])));

        assertEquals(new Properties(), PropertyUtils.loadOptionalProperties(new ByteArrayInputStream(new byte[0])));
    }

    @Test
    @NeedsTemporaryFolder
    @SuppressWarnings("deprecation")
    public void loadEmptyFile() throws Exception {
        assertEquals(new Properties(), PropertyUtils.loadProperties(newFile(tempFolder, "empty")));
        assertEquals(new Properties(), PropertyUtils.loadOptionalProperties(newFile(tempFolder, "optional")));
    }

    @Test
    @NeedsTemporaryFolder
    @SuppressWarnings("deprecation")
    public void loadEmptyURL() throws Exception {
        assertEquals(
                new Properties(),
                PropertyUtils.loadProperties(
                        newFile(tempFolder, "empty").toURI().toURL()));

        assertEquals(
                new Properties(),
                PropertyUtils.loadOptionalProperties(
                        newFile(tempFolder, "optional").toURI().toURL()));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void loadValidInputStream() throws UnsupportedEncodingException {
        Properties value = new Properties();
        value.setProperty("a", "b");

        assertEquals(value, PropertyUtils.loadProperties(new ByteArrayInputStream("a=b".getBytes("ISO-8859-1"))));

        assertEquals(
                value, PropertyUtils.loadOptionalProperties(new ByteArrayInputStream("a=b".getBytes("ISO-8859-1"))));
    }

    @Test
    @NeedsTemporaryFolder
    @SuppressWarnings("deprecation")
    public void loadValidFile() throws IOException {
        File valid = newFile(tempFolder, "valid");
        Properties value = new Properties();
        value.setProperty("a", "b");
        try (OutputStream out = Files.newOutputStream(valid.toPath())) {
            value.store(out, "a test");
            assertEquals(value, PropertyUtils.loadProperties(valid));
            assertEquals(value, PropertyUtils.loadOptionalProperties(valid));
        }
    }

    @Test
    @NeedsTemporaryFolder
    @SuppressWarnings("deprecation")
    public void loadValidURL() throws IOException {
        File valid = newFile(tempFolder, "valid");
        Properties value = new Properties();
        value.setProperty("a", "b");
        try (OutputStream out = Files.newOutputStream(valid.toPath())) {
            value.store(out, "a test");
            assertEquals(value, PropertyUtils.loadProperties(valid.toURI().toURL()));
            assertEquals(
                    value, PropertyUtils.loadOptionalProperties(valid.toURI().toURL()));
        }
    }

    private static File newFile(File parent, String child) throws IOException {
        File result = new File(parent, child);
        result.createNewFile();
        return result;
    }
}
