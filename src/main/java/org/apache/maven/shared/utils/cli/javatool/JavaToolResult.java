package org.apache.maven.shared.utils.cli.javatool;

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

import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.Commandline;

/**
 * Describes the result of a {@link JavaTool} invocation.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 0.5
 */
public interface JavaToolResult
{
    /**
     * Gets the command line used.
     *
     * @return The command line used
     */
    Commandline getCommandline();

    /**
     * Gets the exception that possibly occurred during the execution of the command line.
     *
     * @return The exception that prevented to invoke tool or <code>null</code> if the command line was successfully
     *         processed by the operating system.
     */
    CommandLineException getExecutionException();

    /**
     * Gets the exit code from the tool invocation. A non-zero value indicates a build failure. <strong>Note:</strong>
     * This value is undefined if {@link #getExecutionException()} reports an exception.
     *
     * @return The exit code from the tool invocation.
     */
    int getExitCode();

    /**
     * Sets the exit code reported by the tool invocation.
     *
     * @param exitCode The exit code reported by the tool invocation.
     */
    void setExitCode( int exitCode );

    /**
     * Sets the exception that prevented to execute the command line.
     *
     * @param executionException The exception that prevented to execute the command line, may be <code>null</code>.
     */
    void setExecutionException( CommandLineException executionException );

    /**
     * Set the commandline used to obtain this result.
     *
     * @param commandline the commandline used to obtain this result
     */
    void setCommandline( Commandline commandline );
}
