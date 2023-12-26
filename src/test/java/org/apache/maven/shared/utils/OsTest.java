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

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the 'Os' class which evaluates operation system specific settings.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
class OsTest {
    private String origOsName;
    private String origOsArch;
    private String origOsVersion;

    @BeforeEach
    void setUp() {
        origOsName = System.getProperty("os.name");
        origOsArch = System.getProperty("os.arch");
        origOsVersion = System.getProperty("os.version");

        // and now set some special settings ;)
        System.setProperty("os.name", "os/2");
        System.setProperty("os.arch", "i386");
        System.setProperty("os.version", "2.1.32");
    }

    @AfterEach
    void tearDown() {
        // set the original OS settings again
        System.setProperty("os.name", origOsName);
        System.setProperty("os.arch", origOsArch);
        System.setProperty("os.version", origOsVersion);
    }

    @Test
    void constructor() {
        Os os = new Os();
        os.eval();

        assertTrue(Os.isName(Os.FAMILY_OS2));

        assertFalse(Os.isName(Os.FAMILY_DOS));
        assertFalse(Os.isName(Os.FAMILY_MAC));
        assertFalse(Os.isName(Os.FAMILY_NETWARE));
        assertFalse(Os.isName(Os.FAMILY_OPENVMS));
        assertFalse(Os.isName(Os.FAMILY_OS400));
        assertFalse(Os.isName(Os.FAMILY_TANDEM));
        assertFalse(Os.isName(Os.FAMILY_UNIX));
        assertFalse(Os.isName(Os.FAMILY_WIN9X));
        assertFalse(Os.isName(Os.FAMILY_WINDOWS));
        assertFalse(Os.isName(Os.FAMILY_ZOS));
    }

    @Test
    void familyNames() {
        assertEquals(Os.FAMILY_DOS, "dos");
        assertEquals(Os.FAMILY_MAC, "mac");
        assertEquals(Os.FAMILY_NETWARE, "netware");
        assertEquals(Os.FAMILY_OPENVMS, "openvms");
        assertEquals(Os.FAMILY_OS2, "os/2");
        assertEquals(Os.FAMILY_OS400, "os/400");
        assertEquals(Os.FAMILY_TANDEM, "tandem");
        assertEquals(Os.FAMILY_UNIX, "unix");
        assertEquals(Os.FAMILY_WIN9X, "win9x");
        assertEquals(Os.FAMILY_WINDOWS, "windows");
        assertEquals(Os.FAMILY_ZOS, "z/os");
    }

    @Test
    void getValidFamilies() {
        Set<String> osFamilies = Os.getValidFamilies();

        assertTrue(osFamilies.size() >= 11, "OsFamilies Set size");

        assertTrue(osFamilies.contains(Os.FAMILY_DOS));
        assertTrue(osFamilies.contains(Os.FAMILY_MAC));
        assertTrue(osFamilies.contains(Os.FAMILY_NETWARE));
        assertTrue(osFamilies.contains(Os.FAMILY_OPENVMS));
        assertTrue(osFamilies.contains(Os.FAMILY_OS2));
        assertTrue(osFamilies.contains(Os.FAMILY_OS400));
        assertTrue(osFamilies.contains(Os.FAMILY_TANDEM));
        assertTrue(osFamilies.contains(Os.FAMILY_UNIX));
        assertTrue(osFamilies.contains(Os.FAMILY_WIN9X));
        assertTrue(osFamilies.contains(Os.FAMILY_WINDOWS));
        assertTrue(osFamilies.contains(Os.FAMILY_ZOS));
    }

    @Test
    void isArch() {
        assertThat(Os.isArch("i386")).as("Arch is i386").isEqualTo(true);

        assertThat(Os.isArch("x86_64")).as("Os is not Mac").isEqualTo(false);
    }

    @Test
    void isFamily() {
        assertThat(Os.isFamily(Os.FAMILY_OS2)).as("Family is os/2").isEqualTo(true);

        assertThat(Os.isFamily(Os.FAMILY_MAC)).as("Family is not mac").isEqualTo(false);
    }

    @Test
    void isName() {
        assertThat(Os.isName("os/2")).as("Name is os/2").isEqualTo(true);

        assertThat(Os.isName("Mac OS X")).as("Name is not Mac OS X").isEqualTo(false);
    }

    @Test
    void isValidFamily() {
        assertThat(Os.isValidFamily(Os.FAMILY_OS2)).as("os/2 isValidFamily").isEqualTo(true);

        assertThat(Os.isValidFamily("iPhone")).as("iPone != isValidFamily").isEqualTo(false);
    }

    @Test
    void isVersion() {
        assertThat(Os.isVersion("2.1.32")).as("isVersion").isEqualTo(true);

        assertThat(Os.isVersion("2.1")).as("isVersion").isEqualTo(false);

        assertThat(Os.isVersion("4.5")).as("isVersion").isEqualTo(false);
    }
}
