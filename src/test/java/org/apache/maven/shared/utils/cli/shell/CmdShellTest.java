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
package org.apache.maven.shared.utils.cli.shell;

import java.util.List;

import org.apache.maven.shared.utils.cli.Commandline;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The command line built here is a string, so these tests run on every platform. {@code Commandline} rewrites the
 * path separators of the executable for the current platform, so the expected strings are built from
 * {@code getExecutable()} rather than from the literal path.
 */
public class CmdShellTest {

    private static Commandline commandline(String executable, String... arguments) {
        Commandline commandline = new Commandline(new CmdShell());
        commandline.setExecutable(executable);
        for (String argument : arguments) {
            commandline.createArg().setValue(argument);
        }
        return commandline;
    }

    private static String commandLine(Commandline commandline) {
        List<String> lines = commandline.getShell().getShellCommandLine(commandline.getArguments());
        assertEquals("cmd.exe", lines.get(0));
        assertEquals("/X", lines.get(1));
        assertEquals("/C", lines.get(2));
        assertEquals(4, lines.size());
        return lines.get(3);
    }

    @Test
    public void plainItemsAreNotQuoted() {
        assertEquals("\"mvn.cmd -B compile\"", commandLine(commandline("mvn.cmd", "-B", "compile")));
    }

    @Test
    public void executableWithParenthesesIsQuoted() {
        // MSHARED-832: C:\work\lol(1)\maven\bin\mvn.cmd
        Commandline cl = commandline("C:\\work\\lol(1)\\maven\\bin\\mvn.cmd", "-B", "compile");
        assertEquals("\"\"" + cl.getExecutable() + "\" -B compile\"", commandLine(cl));
    }

    @Test
    public void executableWithSpaceIsQuoted() {
        Commandline cl = commandline("C:\\Program Files\\maven\\bin\\mvn.cmd", "-B");
        assertEquals("\"\"" + cl.getExecutable() + "\" -B\"", commandLine(cl));
    }

    @Test
    public void argumentWithCmdSpecialCharactersIsQuoted() {
        // MSHARED-765: a password containing &
        assertEquals("\"jarsigner -storepass \"a&b\"\"", commandLine(commandline("jarsigner", "-storepass", "a&b")));
        assertEquals(
                "\"x \"a|b\" \"c<d\" \"e>f\" \"g^h\" \"i@j\" \"k l\"\"",
                commandLine(commandline("x", "a|b", "c<d", "e>f", "g^h", "i@j", "k l")));
    }

    @Test
    public void alreadyQuotedItemIsLeftAlone() {
        assertEquals("\"x \"a b\"\"", commandLine(commandline("x", "\"a b\"")));
    }

    @Test
    public void unconditionalQuotingQuotesEveryItem() {
        Commandline cl = commandline("x", "plain");
        cl.getShell().setUnconditionalQuoting(true);
        assertEquals("\"\"x\" \"plain\"\"", commandLine(cl));
    }

    @Test
    public void quotingCanBeDisabled() {
        Commandline cl = commandline("x", "a&b");
        cl.getShell().setQuotedArgumentsEnabled(false);
        assertEquals("\"x a&b\"", commandLine(cl));
    }
}
