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
package org.apache.maven.shared.utils.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiColors;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiMode;
import org.fusesource.jansi.AnsiPrintStream;
import org.fusesource.jansi.AnsiType;
import org.fusesource.jansi.io.AnsiOutputStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageUtilsTest {
    @Test
    public void testSystem() {
        PrintStream currentOut = System.out;
        try {
            MessageUtils.systemInstall();
            assertNotSame(System.out, currentOut);
        } catch (LinkageError e) {
            //            assumeNoException("JAnsi not supported for this platform", e);
        } finally {
            try {
                // uninstall is always necessary due to https://github.com/fusesource/jansi/issues/242
                // but might throw exceptions
                MessageUtils.systemUninstall();
            } catch (Throwable t) {
                // ignore any thrown exception like NPE here
            }
        }
        assertSame(System.out, currentOut);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testTerminalWidth() {
        AnsiOutputStream.WidthSupplier width = () -> 33;
        AnsiOutputStream aos = new AnsiOutputStream(
                new ByteArrayOutputStream(),
                width,
                AnsiMode.Default,
                null,
                AnsiType.Emulation,
                AnsiColors.Colors256,
                StandardCharsets.UTF_8,
                null,
                null,
                false);
        try {
            AnsiConsole.systemInstall();
            AnsiConsole.out = new AnsiPrintStream(aos, true);
            assertEquals(33, MessageUtils.getTerminalWidth());
        } catch (LinkageError e) {
            //            assumeNoException("JAnsi not supported for this platform", e);
        } finally {
            try {
                // uninstall is always necessary due to https://github.com/fusesource/jansi/issues/242
                // but might throw exceptions
                AnsiConsole.systemUninstall();
            } catch (Throwable t) {
                // ignore any thrown exception like NPE here
            }
        }
    }

    @Test
    public void testSameOutputAsJansi() {
        assertTrue(MessageUtils.isColorEnabled());
        assertEquals(
                Ansi.ansi()
                        .bold()
                        .a("a")
                        .reset()
                        .a("b")
                        .fg(Ansi.Color.GREEN)
                        .a("c")
                        .reset()
                        .newline()
                        .format("%d", 1)
                        .bold()
                        .fg(Ansi.Color.RED)
                        .a("d")
                        .reset()
                        .toString(),
                MessageUtils.buffer()
                        .strong("a")
                        .a("b")
                        .mojo("c")
                        .newline()
                        .format("%d", 1)
                        .failure("d")
                        .build());
    }

    @Test
    public void testFollowsJansiEnabled() {
        boolean enabled = Ansi.isEnabled();
        try {
            Ansi.setEnabled(false);
            assertFalse(MessageUtils.isColorEnabled());
            assertEquals(
                    "a b", MessageUtils.buffer().strong("a").a(' ').mojo("b").build());
        } finally {
            Ansi.setEnabled(enabled);
        }
    }

    @Test
    public void testWithoutJansi() throws Exception {
        URL classes = MessageUtils.class.getProtectionDomain().getCodeSource().getLocation();
        try (URLClassLoader loader = new URLClassLoader(new URL[] {classes}, null)) {
            Class<?> utils = loader.loadClass(MessageUtils.class.getName());
            assertFalse((Boolean) utils.getMethod("isColorEnabled").invoke(null));
            assertEquals(-1, utils.getMethod("getTerminalWidth").invoke(null));
            Object buffer = utils.getMethod("buffer").invoke(null);
            Method strong = buffer.getClass().getMethod("strong", Object.class);
            strong.setAccessible(true);
            assertEquals("a", strong.invoke(buffer, "a").toString());
        }
    }
}
