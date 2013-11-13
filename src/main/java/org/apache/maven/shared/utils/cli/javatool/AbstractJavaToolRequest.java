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
 * Abstract implementation of a {@link JavaToolRequest}.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 0.5
 */
public class AbstractJavaToolRequest
    implements JavaToolRequest
{

    /**
     * Optional system out stream consumer used by the commandline execution.
     */
    private StreamConsumer systemOutStreamConsumer;

    /**
     * Optional system error stream consumer used by the commandline execution.
     */
    private StreamConsumer systemErrorStreamConsumer;

    /**
     * {@inheritDoc}
     */
    public StreamConsumer getSystemOutStreamConsumer()
    {
        return systemOutStreamConsumer;
    }

    /**
     * {@inheritDoc}
     */
    public StreamConsumer getSystemErrorStreamConsumer()
    {
        return systemErrorStreamConsumer;
    }

    /**
     * {@inheritDoc}
     */
    public void setSystemOutStreamConsumer( StreamConsumer systemOutStreamConsumer )
    {
        this.systemOutStreamConsumer = systemOutStreamConsumer;
    }

    /**
     * {@inheritDoc}
     */
    public void setSystemErrorStreamConsumer( StreamConsumer systemErrorStreamConsumer )
    {
        this.systemErrorStreamConsumer = systemErrorStreamConsumer;
    }
}
