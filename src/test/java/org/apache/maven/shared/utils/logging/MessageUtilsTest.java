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
import java.nio.charset.StandardCharsets;

import org.fusesource.jansi.AnsiColors;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiMode;
import org.fusesource.jansi.AnsiPrintStream;
import org.fusesource.jansi.AnsiType;
import org.fusesource.jansi.io.AnsiOutputStream;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageUtilsTest {
    @Test
    public void testSystem() {
        PrintStream currentOut = System.out;
        try {
            MessageUtils.systemInstall();
            assertThat(System.out, not(sameInstance(currentOut)));
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
        assertThat(System.out, sameInstance(currentOut));
    }

    @Test
    public void testTerminalWidth() {
        AnsiOutputStream.WidthSupplier width = new AnsiOutputStream.WidthSupplier() {
            @Override
            public int getTerminalWidth() {
                return 33;
            }
        };
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
}
