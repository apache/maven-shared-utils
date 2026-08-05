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

import java.io.File;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CmdShellTest {

    @Test
    void quotesEveryCommandItemAndPrefixesTheCommand() {
        CmdShell shell = new CmdShell();
        String executable = String.join(File.separator, "C:", "work", "two(1)words", "test.cmd");
        shell.setExecutable(executable);

        assertEquals(
                Arrays.asList("cmd.exe", "/X", "/C", "@\"" + executable + "\" \"-B\" \"first\" \"second argument\""),
                shell.getShellCommandLine("-B", "first", "second argument"));
    }

    @Test
    void caretEscapesEmbeddedDoubleQuotesInArguments() {
        CmdShell shell = new CmdShell();
        shell.setExecutable("test.cmd");

        assertEquals(
                "@\"test.cmd\" \"say ^\"hello^\"\"",
                shell.getShellCommandLine("say \"hello\"").get(3));
    }

    @Test
    void quotesCmdMetacharacters() {
        CmdShell shell = new CmdShell();
        shell.setExecutable("test&(1)^.cmd");

        assertEquals(
                "@\"test&(1)^.cmd\" \"&\" \"|\" \"<\" \">\" \"(\" \")\" \"^\"",
                shell.getShellCommandLine("&", "|", "<", ">", "(", ")", "^").get(3));
    }
}
