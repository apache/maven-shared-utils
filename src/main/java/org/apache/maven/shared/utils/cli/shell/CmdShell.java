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

import java.util.Collections;
import java.util.List;

import org.apache.maven.shared.utils.StringUtils;

/**
 * Implementation to call the CMD Shell present on Windows NT, 2000, XP, 7, 8, and 10.
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 */
public class CmdShell extends Shell {
    /**
     * Create an instance of CmdShell.
     */
    public CmdShell() {
        setShellCommand("cmd.exe");
        setShellArgs(new String[] {"/X", "/C"});
    }

    /**
     * <p>
     * Specific implementation that quotes every command-line item.
     * </p>
     * <p>
     * Workaround for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6468220
     * </p>
     * <p>
     * From cmd.exe /? output:
     * </p>
     * <pre>
     *      If /C or /K is specified, then the remainder of the command line after
     *      the switch is processed as a command line, where the following logic is
     *      used to process quote (&quot;) characters:
     *
     *      1.  If all of the following conditions are met, then quote characters
     *      on the command line are preserved:
     *
     *      - no /S switch
     *      - exactly two quote characters
     *      - no special characters between the two quote characters,
     *      where special is one of: &amp;&lt;&gt;()@&circ;|
     *      - there are one or more whitespace characters between the
     *      the two quote characters
     *      - the string between the two quote characters is the name
     *      of an executable file.
     *
     *      2.  Otherwise, old behavior is to see if the first character is
     *      a quote character and if so, strip the leading character and
     *      remove the last quote character on the command line, preserving
     *      any text after the last quote character.
     * </pre>
     * <p>
     * Every item is quoted independently, and embedded double quotes are escaped with
     * the CMD escape character ({@code ^}). Prefixing the command with {@code @} keeps
     * the opening quote of the executable from being treated as the first character of
     * the command string and stripped by CMD. CMD consumes {@code @} as the prefix that
     * disables echoing for one command.
     * </p>
     *
     * @param executable the executable
     * @param arguments the arguments for the executable
     * @return the resulting command line
     */
    @Override
    public List<String> getCommandLine(String executable, String... arguments) {
        String commandLine = super.getCommandLine(executable, arguments).get(0);
        return Collections.singletonList('@' + commandLine);
    }

    @Override
    protected String quoteOneItem(String inputString, boolean isExecutable) {
        return StringUtils.quoteAndEscape(inputString, '"', new char[] {'"'}, new char[0], '^', true);
    }
}
