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

import org.apache.maven.shared.utils.cli.StreamConsumer;

/**
 * Specifies the minimum parameters used to control a {@link JavaTool} invocation.
 *
 * @author <a href="mailto:chemit@codelutin.com">Tony Chemit</a>
 * @since 0.5
 */
public interface JavaToolRequest
{

    /**
     * <p>Gets the value of the {@code systemOutStreamConsumer} field.</p>
     * <p>This option field if filled is used by the commandline tool to consume system ouput stream of the jarsigner
     * command.</p>
     *
     * @return the value of the {@code systemOutStreamConsumer} field.
     */
    StreamConsumer getSystemOutStreamConsumer();

    /**
     * <p>Gets the value of the {@code systemErrorStreamConsumer} field.</p>
     * <p>This option field if filled is used by the commandline tool to consume system error stream of the jarsigner
     * command.</p>
     *
     * @return the value of the {@code systemErrorStreamConsumer} field.
     */
    StreamConsumer getSystemErrorStreamConsumer();

    /**
     * Sets the new given value to the field {@code systemOutStreamConsumer} of the request.
     *
     * @param systemOutStreamConsumer the new value of the field {@code systemOutStreamConsumer}.
     */
    void setSystemOutStreamConsumer( StreamConsumer systemOutStreamConsumer );

    /**
     * Sets the new given value to the field {@code systemErrorStreamConsumer} of the request.
     *
     * @param systemErrorStreamConsumer the new value of the field {@code systemErrorStreamConsumer}.
     */
    void setSystemErrorStreamConsumer( StreamConsumer systemErrorStreamConsumer );
}
