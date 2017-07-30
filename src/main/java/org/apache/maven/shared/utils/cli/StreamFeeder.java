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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Read from an InputStream and write the output to an OutputStream.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
class StreamFeeder
    extends AbstractStreamHandler
{

    private final AtomicReference<InputStream> input;

    private final AtomicReference<OutputStream> output;

    private volatile Throwable exception;

    /**
     * Create a new StreamFeeder
     *
     * @param input Stream to read from
     * @param output Stream to write to
     */
    StreamFeeder( InputStream input, OutputStream output )
    {
        super();
        this.input = new AtomicReference<InputStream>( input );
        this.output = new AtomicReference<OutputStream>( output );
    }

    @Override
    public void run()
    {
        try
        {
            feed();
        }
        catch ( Throwable e )
        {
            // Catch everything so the streams will be closed and flagged as done.
            if ( this.exception != null )
            {
                this.exception = e;
            }
        }
        finally
        {
            close();

            synchronized ( this )
            {
                notifyAll();
            }
        }
    }

    public void close()
    {
        setDone();
        final InputStream is = input.getAndSet( null );
        if ( is != null )
        {
            try
            {
                is.close();
            }
            catch ( IOException ex )
            {
                if ( this.exception != null )
                {
                    this.exception = ex;
                }
            }
        }

        final OutputStream os = output.getAndSet( null );
        if ( os != null )
        {
            try
            {
                os.close();
            }
            catch ( IOException ex )
            {
                if ( this.exception != null )
                {
                    this.exception = ex;
                }
            }
        }
    }

    /**
     * @since 3.2.0
     */
    public Throwable getException()
    {
        return this.exception;
    }

    @SuppressWarnings( "checkstyle:innerassignment" )
    private void feed()
        throws IOException
    {
        InputStream is = input.get();
        OutputStream os = output.get();
        boolean flush = false;

        if ( is != null && os != null )
        {
            for ( int data; !isDone() && ( data = is.read() ) != -1; )
            {
                if ( !isDisabled() )
                {
                    os.write( data );
                    flush = true;
                }
            }

            if ( flush )
            {
                os.flush();
            }
        }
    }

}
