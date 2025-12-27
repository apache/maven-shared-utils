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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test the {@link PathTool} class.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
@SuppressWarnings("deprecation")
public class PathToolTest {

    @Test
    // Keep in sync with testGetRelativeFilePathWindows()
    @DisabledOnOs(OS.WINDOWS)
    public void testGetRelativeFilePathNonWindows() {
        assertEquals("", PathTool.getRelativeFilePath(null, null));

        assertEquals("", PathTool.getRelativeFilePath(null, "/usr/local/java/bin"));

        assertEquals("", PathTool.getRelativeFilePath("/usr/local", null));

        assertEquals("java/bin", PathTool.getRelativeFilePath("/usr/local", "/usr/local/java/bin"));

        assertEquals("java/bin/", PathTool.getRelativeFilePath("/usr/local", "/usr/local/java/bin/"));

        assertEquals("../../", PathTool.getRelativeFilePath("/usr/local/java/bin", "/usr/local/"));

        assertEquals("java/bin/java.sh", PathTool.getRelativeFilePath("/usr/local/", "/usr/local/java/bin/java.sh"));

        assertEquals("../../../", PathTool.getRelativeFilePath("/usr/local/java/bin/java.sh", "/usr/local/"));

        assertEquals("../../bin", PathTool.getRelativeFilePath("/usr/local/", "/bin"));

        assertEquals("../usr/local/", PathTool.getRelativeFilePath("/bin", "/usr/local/"));
    }

    @Test
    // Keep in sync with testGetRelativeFilePathNonWindows()
    @EnabledOnOs(OS.WINDOWS)
    public void testGetRelativeFilePathWindows() {
        assertEquals("", PathTool.getRelativeFilePath(null, null));

        assertEquals("", PathTool.getRelativeFilePath(null, "c:\\usr\\local\\java\\bin"));

        assertEquals("", PathTool.getRelativeFilePath("c:\\usr\\local", null));

        assertEquals("java\\bin", PathTool.getRelativeFilePath("c:\\usr\\local", "c:\\usr\\local\\java\\bin"));

        assertEquals("java\\bin\\", PathTool.getRelativeFilePath("c:\\usr\\local", "c:\\usr\\local\\java\\bin\\"));

        assertEquals("..\\..\\", PathTool.getRelativeFilePath("c:\\usr\\local\\java\\bin", "c:\\usr\\local\\"));

        assertEquals(
                "java\\bin\\java.sh",
                PathTool.getRelativeFilePath("c:\\usr\\local\\", "c:\\usr\\local\\java\\bin\\java.sh"));

        assertEquals(
                "..\\..\\..\\", PathTool.getRelativeFilePath("c:\\usr\\local\\java\\bin\\java.sh", "c:\\usr\\local\\"));

        assertEquals("..\\..\\bin", PathTool.getRelativeFilePath("c:\\usr\\local\\", "c:\\bin"));

        assertEquals("..\\usr\\local\\", PathTool.getRelativeFilePath("c:\\bin", "c:\\usr\\local\\"));
    }

    @Test
    public void testGetRelativePath2Parm() {
        assertEquals("", PathTool.getRelativePath(null, null));

        assertEquals("", PathTool.getRelativePath(null, "/usr/local/java/bin"));

        assertEquals("", PathTool.getRelativePath("/usr/local/", null));

        assertEquals("..", PathTool.getRelativePath("/usr/local/", "/usr/local/java/bin"));

        assertEquals("../..", PathTool.getRelativePath("/usr/local/", "/usr/local/java/bin/java.sh"));

        assertEquals("", PathTool.getRelativePath("/usr/local/java/bin/java.sh", "/usr/local/"));
    }

    @Test
    public void testUppercaseDrive() {
        assertNull(PathTool.uppercaseDrive(null));

        assertEquals("D:", PathTool.uppercaseDrive("d:"));

        assertEquals("D:", PathTool.uppercaseDrive("D:"));

        assertEquals("/notadrive", PathTool.uppercaseDrive("/notadrive"));
    }
}
