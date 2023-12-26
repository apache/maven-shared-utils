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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyUtilsTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface NeedsTemporaryFolder {}

    @TempDir
    public File tempFolder;

    // @ReproducesPlexusBug( "Should return null on error like url and file do" )
    @Test
    @SuppressWarnings("deprecation")
    void loadNullInputStream() throws Exception {
        assertThat(PropertyUtils.loadProperties((InputStream) null)).isEqualTo(new Properties());
    }

    @Test
    void loadOptionalNullInputStream() throws Exception {
        assertThat(PropertyUtils.loadOptionalProperties((InputStream) null)).isEqualTo(new Properties());
    }

    @Test
    void loadOptionalProperties_ioException() throws Exception {
        URL url = new URL("https://nonesuch12344.foo.bar.com");
        assertThat(PropertyUtils.loadOptionalProperties(url)).isEqualTo(new Properties());
    }

    @Test
    @SuppressWarnings("deprecation")
    void loadNullURL() throws Exception {
        assertThat(PropertyUtils.loadProperties((URL) null)).isNull();
    }

    @Test
    void loadOptionalNullURL() throws Exception {
        assertThat(PropertyUtils.loadOptionalProperties((URL) null)).isEqualTo(new Properties());
    }

    @Test
    @SuppressWarnings("deprecation")
    void loadNullFile() throws Exception {
        assertThat(PropertyUtils.loadProperties((File) null)).isNull();
    }

    @Test
    void loadOptionalNullFile() throws Exception {
        assertThat(PropertyUtils.loadOptionalProperties((File) null)).isEqualTo(new Properties());
    }

    @Test
    @SuppressWarnings("deprecation")
    void loadEmptyInputStream() throws Exception {
        assertThat(PropertyUtils.loadProperties(new ByteArrayInputStream(new byte[0])))
                .isEqualTo(new Properties());

        assertThat(PropertyUtils.loadOptionalProperties(new ByteArrayInputStream(new byte[0])))
                .isEqualTo(new Properties());
    }

    @Test
    @NeedsTemporaryFolder
    @SuppressWarnings("deprecation")
    void loadEmptyFile() throws Exception {
        assertThat(PropertyUtils.loadProperties(File.createTempFile("empty", null, tempFolder)))
                .isEqualTo(new Properties());
        assertThat(PropertyUtils.loadOptionalProperties(File.createTempFile("optional", null, tempFolder)))
                .isEqualTo(new Properties());
    }

    @Test
    @NeedsTemporaryFolder
    @SuppressWarnings("deprecation")
    void loadEmptyURL() throws Exception {
        assertThat(PropertyUtils.loadProperties(
                        File.createTempFile("empty", null, tempFolder).toURI().toURL()))
                .isEqualTo(new Properties());

        assertThat(PropertyUtils.loadOptionalProperties(File.createTempFile("optional", null, tempFolder)
                        .toURI()
                        .toURL()))
                .isEqualTo(new Properties());
    }

    @Test
    @SuppressWarnings("deprecation")
    void loadValidInputStream() throws UnsupportedEncodingException {
        Properties value = new Properties();
        value.setProperty("a", "b");

        assertThat(PropertyUtils.loadProperties(new ByteArrayInputStream("a=b".getBytes("ISO-8859-1"))))
                .isEqualTo(value);

        assertThat(PropertyUtils.loadOptionalProperties(new ByteArrayInputStream("a=b".getBytes("ISO-8859-1"))))
                .isEqualTo(value);
    }

    @Test
    @NeedsTemporaryFolder
    @SuppressWarnings("deprecation")
    void loadValidFile() throws IOException {
        File valid = File.createTempFile("valid", null, tempFolder);
        Properties value = new Properties();
        value.setProperty("a", "b");
        try (OutputStream out = new FileOutputStream(valid)) {
            value.store(out, "a test");
            assertThat(PropertyUtils.loadProperties(valid)).isEqualTo(value);
            assertThat(PropertyUtils.loadOptionalProperties(valid)).isEqualTo(value);
        }
    }

    @Test
    @NeedsTemporaryFolder
    @SuppressWarnings("deprecation")
    void loadValidURL() throws IOException {
        File valid = File.createTempFile("valid", null, tempFolder);
        Properties value = new Properties();
        value.setProperty("a", "b");
        try (OutputStream out = new FileOutputStream(valid)) {
            value.store(out, "a test");
            assertThat(PropertyUtils.loadProperties(valid.toURI().toURL())).isEqualTo(value);
            assertThat(PropertyUtils.loadOptionalProperties(valid.toURI().toURL()))
                    .isEqualTo(value);
        }
    }
}
