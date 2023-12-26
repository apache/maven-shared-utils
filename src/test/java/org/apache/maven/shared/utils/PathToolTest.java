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

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

/**
 * Test the {@link PathTool} class.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class PathToolTest {

    @TempDir
    public File tempFolder;

    // Keep in sync with testGetRelativeFilePath_Windows()
    @Test
    @DisabledOnOs(WINDOWS)
    void getRelativeFilePath_NonWindows() {
        assertThat(PathTool.getRelativeFilePath(null, null)).isEqualTo("");

        assertThat(PathTool.getRelativeFilePath(null, "/usr/local/java/bin")).isEqualTo("");

        assertThat(PathTool.getRelativeFilePath("/usr/local", null)).isEqualTo("");

        assertThat(PathTool.getRelativeFilePath("/usr/local", "/usr/local/java/bin"))
                .isEqualTo("java/bin");

        assertThat(PathTool.getRelativeFilePath("/usr/local", "/usr/local/java/bin/"))
                .isEqualTo("java/bin/");

        assertThat(PathTool.getRelativeFilePath("/usr/local/java/bin", "/usr/local/"))
                .isEqualTo("../../");

        assertThat(PathTool.getRelativeFilePath("/usr/local/", "/usr/local/java/bin/java.sh"))
                .isEqualTo("java/bin/java.sh");

        assertThat(PathTool.getRelativeFilePath("/usr/local/java/bin/java.sh", "/usr/local/"))
                .isEqualTo("../../../");

        assertThat(PathTool.getRelativeFilePath("/usr/local/", "/bin")).isEqualTo("../../bin");

        assertThat(PathTool.getRelativeFilePath("/bin", "/usr/local/")).isEqualTo("../usr/local/");
    }

    // Keep in sync with testGetRelativeFilePath_NonWindows()
    @Test
    @EnabledOnOs(WINDOWS)
    void getRelativeFilePath_Windows() {
        assertThat(PathTool.getRelativeFilePath(null, null)).isEqualTo("");

        assertThat(PathTool.getRelativeFilePath(null, "c:\\usr\\local\\java\\bin"))
                .isEqualTo("");

        assertThat(PathTool.getRelativeFilePath("c:\\usr\\local", null)).isEqualTo("");

        assertThat(PathTool.getRelativeFilePath("c:\\usr\\local", "c:\\usr\\local\\java\\bin"))
                .isEqualTo("java\\bin");

        assertThat(PathTool.getRelativeFilePath("c:\\usr\\local", "c:\\usr\\local\\java\\bin\\"))
                .isEqualTo("java\\bin\\");

        assertThat(PathTool.getRelativeFilePath("c:\\usr\\local\\java\\bin", "c:\\usr\\local\\"))
                .isEqualTo("..\\..\\");

        assertThat(PathTool.getRelativeFilePath("c:\\usr\\local\\", "c:\\usr\\local\\java\\bin\\java.sh"))
                .isEqualTo("java\\bin\\java.sh");

        assertThat(PathTool.getRelativeFilePath("c:\\usr\\local\\java\\bin\\java.sh", "c:\\usr\\local\\"))
                .isEqualTo("..\\..\\..\\");

        assertThat(PathTool.getRelativeFilePath("c:\\usr\\local\\", "c:\\bin")).isEqualTo("..\\..\\bin");

        assertThat(PathTool.getRelativeFilePath("c:\\bin", "c:\\usr\\local\\")).isEqualTo("..\\usr\\local\\");
    }

    @Test
    void getRelativePath_2parm() {
        assertThat(PathTool.getRelativePath(null, null)).isEqualTo("");

        assertThat(PathTool.getRelativePath(null, "/usr/local/java/bin")).isEqualTo("");

        assertThat(PathTool.getRelativePath("/usr/local/", null)).isEqualTo("");

        assertThat(PathTool.getRelativePath("/usr/local/", "/usr/local/java/bin"))
                .isEqualTo("..");

        assertThat(PathTool.getRelativePath("/usr/local/", "/usr/local/java/bin/java.sh"))
                .isEqualTo("../..");

        assertThat(PathTool.getRelativePath("/usr/local/java/bin/java.sh", "/usr/local/"))
                .isEqualTo("");
    }

    @Test
    void uppercaseDrive() {
        assertThat(PathTool.uppercaseDrive(null)).isNull();

        assertThat(PathTool.uppercaseDrive("d:")).isEqualTo("D:");

        assertThat(PathTool.uppercaseDrive("D:")).isEqualTo("D:");

        assertThat(PathTool.uppercaseDrive("/notadrive")).isEqualTo("/notadrive");
    }
}
