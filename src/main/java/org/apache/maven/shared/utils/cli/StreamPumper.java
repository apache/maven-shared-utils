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
import java.io.PrintWriter;
import org.apache.maven.shared.utils.io.IOUtil;

/**
 * Class to pump the error stream during Process's runtime. Copied from the Ant
 * built-in task.
 *
 * @author <a href="mailto:fvancea@maxiq.com">Florin Vancea </a>
 * @author <a href="mailto:pj@thoughtworks.com">Paul Julius </a>
 * 
 */
public class StreamPumper
    extends AbstractStreamHandler
{
    private final BufferedReader in;

    private final StreamConsumer consumer;

    private final PrintWriter out;

    private volatile Exception exception = null;

    private static final int SIZE = 1024;

    public StreamPumper( InputStream in, StreamConsumer consumer )
    {
        this( in, null, consumer );
    }

    private StreamPumper( InputStream in, PrintWriter writer, StreamConsumer consumer )
    {
        this.in = new BufferedReader( new InputStreamReader( in ), SIZE );
        this.out = writer;
        this.consumer = consumer;
    }

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

                if ( out != null )
                {
                    out.println( line );

                    out.flush();
                }

            }
        }
        catch ( IOException e )
        {
            exception = e;
        }
        finally
        {
            IOUtil.close( in );

            synchronized ( this )
            {
                setDone();

                this.notifyAll();
            }
        }
    }

    public void flush()
    {
        if ( out != null )
        {
            out.flush();
        }
    }

    public void close()
    {
        IOUtil.close( out );
    }

    public Exception getException()
    {
        return exception;
    }

    private void consumeLine( String line )
    {
        if ( consumer != null && !isDisabled() )
        {
            consumer.consumeLine( line );
        }
    }
}
