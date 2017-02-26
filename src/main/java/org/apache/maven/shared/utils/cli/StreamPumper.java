package org.apache.maven.shared.utils.cli;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import javax.annotation.Nullable;

/**
 * Class to pump the error stream during Process's runtime. Copied from the Ant built-in task.
 *
 * @author <a href="mailto:fvancea@maxiq.com">Florin Vancea </a>
 * @author <a href="mailto:pj@thoughtworks.com">Paul Julius </a>
 */
public class StreamPumper
    extends AbstractStreamHandler
{
    private final BufferedReader in;

    private final StreamConsumer consumer;

    private volatile Exception exception = null;

    private static final int SIZE = 1024;

    /**
     * @param in {@link InputStream}
     * @param consumer {@link StreamConsumer}
     */
    public StreamPumper( InputStream in, StreamConsumer consumer )
    {
        this( new InputStreamReader( in ), consumer );
    }

    /**
     * @param in {@link InputStream}
     * @param consumer {@link StreamConsumer}
     * @param charset {@link Charset}
     */
    public StreamPumper( InputStream in, StreamConsumer consumer, @Nullable Charset charset )
    {
        this( null == charset ? new InputStreamReader( in ) : new InputStreamReader( in, charset ), consumer );
    }

    /**
     * @param in {@link Reader}
     * @param consumer {@link StreamConsumer}
     */
    private StreamPumper( Reader in, StreamConsumer consumer )
    {
        super();
        this.in = new BufferedReader( in, SIZE );
        this.consumer = consumer;
    }

    /** run it. */
    public void run()
    {
        try
        {
            for ( String line = in.readLine(); line != null; line = in.readLine() )
            {
                try
                {
                    if ( exception == null )
                    {
                        consumeLine( line );
                    }
                }
                catch ( Exception t )
                {
                    exception = t;
                }
            }
        }
        catch ( IOException e )
        {
            exception = e;
        }
        finally
        {
            try
            {
                in.close();
            }
            catch ( final IOException e2 )
            {
                if ( this.exception == null )
                {
                    this.exception = e2;
                }
            }

            synchronized ( this )
            {
                setDone();

                this.notifyAll();
            }
        }
    }

    /**
     * flush.
     *
     * @deprecated As of 3.2.0, removed without replacement.
     */
    @Deprecated
    public void flush()
    {
        // Nothing to flush.
    }

    /**
     * Close it.
     *
     * @deprecated As of 3.2.0, removed without replacement.
     */
    @Deprecated
    public void close()
    {
        // Nothing to close.
    }

    /**
     * @return {@link Exception}
     */
    public Exception getException()
    {
        return exception;
    }

    private void consumeLine( String line ) throws IOException
    {
        if ( consumer != null && !isDisabled() )
        {
            consumer.consumeLine( line );
        }
    }
}
