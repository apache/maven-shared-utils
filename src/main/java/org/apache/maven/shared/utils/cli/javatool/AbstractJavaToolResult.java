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
 * Abstract implementation of a {@link JavaToolResult}.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 0.5
 */
public abstract class AbstractJavaToolResult
    implements JavaToolResult
{
    /**
     * The exception that prevented to execute the command line, will be <code>null</code> if jarSigner could be
     * successfully started.
     */
    private CommandLineException executionException;

    /**
     * The exit code reported by the Maven invocation.
     */
    private int exitCode = Integer.MIN_VALUE;

    /**
     * The command line used to obtain this result.
     */
    private Commandline commandline;

    /**
     * {@inheritDoc}
     */
    public int getExitCode()
    {
        return exitCode;
    }

    /**
     * {@inheritDoc}
     */
    public Commandline getCommandline()
    {
        return commandline;
    }

    /**
     * {@inheritDoc}
     */
    public CommandLineException getExecutionException()
    {
        return executionException;
    }

    /**
     * {@inheritDoc}
     */
    public void setExitCode( int exitCode )
    {
        this.exitCode = exitCode;
    }

    /**
     * {@inheritDoc}
     */
    public void setExecutionException( CommandLineException executionException )
    {
        this.executionException = executionException;
    }

    /**
     * {@inheritDoc}
     */
    public void setCommandline( Commandline commandline )
    {
        this.commandline = commandline;
    }
}
