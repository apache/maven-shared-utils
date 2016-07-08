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

/**
 * Describes a java tool, means a executable available in the jdk.
 * <p/>
 * The name of the tool ({@link #getJavaToolName()}) reflects the name of the executable that should exists as an
 * executable in the jdk, like {@code jarsigner, keytool, javadoc, ...}.
 * <p/>
 * An abstract implementation of the {@link JavaTool} named {@link AbstractJavaTool} use the command line API to execute
 * any user requests of this tool.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 0.5
 * @param <Request>
 */
public interface JavaTool<Request extends JavaToolRequest>
{

    /**
     * Return the name of the java tool. This is exactly the name (without his extension) of the executable to
     * find in the {@code jdk/bin} directory.
     * <p/>
     * For example: {@code jarsigner, keytool, javadoc, ...}
     *
     * @return the name of the java tool.
     */
    String getJavaToolName();

    /**
     * Set an optional tool chain to find out the java tool executable location.
     *
     * @param toolchain optional tool chain to find out the java tool executable location.
     * To avoid direct dependency on Maven core, this parameter is an Object that will be
     * used as Toolchain through reflection
     */
    void setToolchain( Object toolchain );

    /**
     * Execute the input request and then returns the result of the execution.
     * <p/>
     * If could not create the java tool invocation, a {@link JavaToolException} will be thrown.
     * <p/>
     * If execution fails, then the result will have a none-zero {@link JavaToolResult#getExitCode()} and his
     * {@link JavaToolResult#getExecutionException()} will be filled with the error, otherwise the exist code will be
     * zero.
     *
     * @param request the request to perform
     * @return the result of the tool execution
     * @throws JavaToolException if could not create the java tool invocation
     */
    JavaToolResult execute( Request request )
        throws JavaToolException;
}
