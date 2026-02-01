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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the 'Os' class which evaluates operation system specific settings.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
@SuppressWarnings("deprecation")
public class OsTest {
    private String origOsName;
    private String origOsArch;
    private String origOsVersion;

    @BeforeEach
    public void setUp() {
        origOsName = System.getProperty("os.name");
        origOsArch = System.getProperty("os.arch");
        origOsVersion = System.getProperty("os.version");

        // and now set some special settings ;)
        System.setProperty("os.name", "os/2");
        System.setProperty("os.arch", "i386");
        System.setProperty("os.version", "2.1.32");
    }

    @AfterEach
    public void tearDown() {
        // set the original OS settings again
        System.setProperty("os.name", origOsName);
        System.setProperty("os.arch", origOsArch);
        System.setProperty("os.version", origOsVersion);
    }

    @Test
    public void testConstructor() {
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
    public void testFamilyNames() {
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
    public void testGetValidFamilies() {
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
    public void testIsArch() {
        assertTrue(Os.isArch("i386"), "Arch is i386");

        assertFalse(Os.isArch("x86_64"), "Os is not Mac");
    }

    @Test
    public void testIsFamily() {
        assertTrue(Os.isFamily(Os.FAMILY_OS2), "Family is os/2");

        assertFalse(Os.isFamily(Os.FAMILY_MAC), "Family is not mac");
    }

    @Test
    public void testIsName() {
        assertTrue(Os.isName("os/2"), "Name is os/2");

        assertFalse(Os.isName("Mac OS X"), "Name is not Mac OS X");
    }

    @Test
    public void testIsValidFamily() {
        assertTrue(Os.isValidFamily(Os.FAMILY_OS2), "os/2 isValidFamily");

        assertFalse(Os.isValidFamily("iPhone"), "iPone != isValidFamily");
    }

    @Test
    public void testIsVersion() {
        assertTrue(Os.isVersion("2.1.32"), "isVersion");

        assertFalse(Os.isVersion("2.1"), "isVersion");

        assertFalse(Os.isVersion("4.5"), "isVersion");
    }
}
