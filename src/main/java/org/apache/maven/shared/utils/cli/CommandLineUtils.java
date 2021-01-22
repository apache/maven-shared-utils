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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.maven.shared.utils.Os;
import org.apache.maven.shared.utils.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 */
public abstract class CommandLineUtils
{

    /**
     * A {@code StreamConsumer} providing consumed lines as a {@code String}.
     *
     * @see #getOutput()
     */
    public static class StringStreamConsumer
        implements StreamConsumer
    {

        private final StringBuffer string = new StringBuffer();

        private static final String LS = System.getProperty( "line.separator", "\n" );

        /**
         * {@inheritDoc}
         */
        @Override
        public void consumeLine( String line )
        {
            string.append( line ).append( LS );
        }

        /**
         * @return The output.
         */
        public String getOutput()
        {
            return string.toString();
        }

    }

    /**
     * Number of milliseconds per second.
     */
    private static final long MILLIS_PER_SECOND = 1000L;

    /**
     * Number of nanoseconds per second.
     */
    private static final long NANOS_PER_SECOND = 1000000000L;

    /**
     * @param cl The command line {@link Commandline}
     * @param systemOut {@link StreamConsumer}
     * @param systemErr {@link StreamConsumer}
     * @return return code.
     * @throws CommandLineException in case of a problem.
     */
    public static int executeCommandLine( @Nonnull Commandline cl, StreamConsumer systemOut, StreamConsumer systemErr )
        throws CommandLineException
    {
        return executeCommandLine( cl, null, systemOut, systemErr, 0 );
    }

    /**
     * @param cl The command line {@link Commandline}
     * @param systemOut {@link StreamConsumer}
     * @param systemErr {@link StreamConsumer}
     * @param timeoutInSeconds The timeout.
     * @return return code.
     * @throws CommandLineException in case of a problem.
     */
    public static int executeCommandLine( @Nonnull Commandline cl, StreamConsumer systemOut, StreamConsumer systemErr,
                                          int timeoutInSeconds )
        throws CommandLineException
    {
        return executeCommandLine( cl, null, systemOut, systemErr, timeoutInSeconds );
    }

    /**
     * @param cl The command line {@link Commandline}
     * @param systemIn {@link StreamConsumer}
     * @param systemOut {@link StreamConsumer}
     * @param systemErr {@link StreamConsumer}
     * @return return code.
     * @throws CommandLineException in case of a problem.
     */
    public static int executeCommandLine( @Nonnull Commandline cl, InputStream systemIn, StreamConsumer systemOut,
                                          StreamConsumer systemErr )
        throws CommandLineException
    {
        return executeCommandLine( cl, systemIn, systemOut, systemErr, 0 );
    }

    /**
     * @param cl               The command line to execute
     * @param systemIn         The input to read from, must be thread safe
     * @param systemOut        A consumer that receives output, must be thread safe
     * @param systemErr        A consumer that receives system error stream output, must be thread safe
     * @param timeoutInSeconds Positive integer to specify timeout, zero and negative integers for no timeout.
     * @return A return value, see {@link Process#exitValue()}
     * @throws CommandLineException or CommandLineTimeOutException if time out occurs
     */
    public static int executeCommandLine( @Nonnull Commandline cl, InputStream systemIn, StreamConsumer systemOut,
                                          StreamConsumer systemErr, int timeoutInSeconds )
        throws CommandLineException
    {
        return executeCommandLine( cl, systemIn, systemOut, systemErr, timeoutInSeconds, null );
    }

    /**
     * @param cl               The command line to execute
     * @param systemIn         The input to read from, must be thread safe
     * @param systemOut        A consumer that receives output, must be thread safe
     * @param systemErr        A consumer that receives system error stream output, must be thread safe
     * @param timeoutInSeconds Positive integer to specify timeout, zero and negative integers for no timeout.
     * @param runAfterProcessTermination Optional callback to run after the process terminated or the the timeout was
     *  exceeded, but before waiting on the stream feeder and pumpers to finish.
     * @return A return value, see {@link Process#exitValue()}
     * @throws CommandLineException or CommandLineTimeOutException if time out occurs
     */
    public static int executeCommandLine( @Nonnull Commandline cl, InputStream systemIn, StreamConsumer systemOut,
                                          StreamConsumer systemErr, int timeoutInSeconds,
                                          @Nullable Runnable runAfterProcessTermination )
        throws CommandLineException
    {
        return executeCommandLine( cl, systemIn, systemOut, systemErr, timeoutInSeconds, runAfterProcessTermination,
                                   null );
    }

    /**
     * @param cl               The command line to execute
     * @param systemIn         The input to read from, must be thread safe
     * @param systemOut        A consumer that receives output, must be thread safe
     * @param systemErr        A consumer that receives system error stream output, must be thread safe
     * @param timeoutInSeconds Positive integer to specify timeout, zero and negative integers for no timeout.
     * @param runAfterProcessTermination Optional callback to run after the process terminated or the the timeout was
     *  exceeded, but before waiting on the stream feeder and pumpers to finish.
     * @param streamCharset    Charset to use for reading streams
     * @return A return value, see {@link Process#exitValue()}
     * @throws CommandLineException or CommandLineTimeOutException if time out occurs
     */
    public static int executeCommandLine( @Nonnull Commandline cl, InputStream systemIn, StreamConsumer systemOut,
                                          StreamConsumer systemErr, int timeoutInSeconds,
                                          @Nullable Runnable runAfterProcessTermination,
                                          @Nullable final Charset streamCharset )
        throws CommandLineException
    {
        final CommandLineCallable future =
            executeCommandLineAsCallable( cl, systemIn, systemOut, systemErr, timeoutInSeconds,
                                          runAfterProcessTermination, streamCharset );
        return future.call();
    }

    /**
     * Immediately forks a process, returns a callable that will block until process is complete.
     *
     * @param cl               The command line to execute
     * @param systemIn         The input to read from, must be thread safe
     * @param systemOut        A consumer that receives output, must be thread safe
     * @param systemErr        A consumer that receives system error stream output, must be thread safe
     * @param timeoutInSeconds Positive integer to specify timeout, zero and negative integers for no timeout.
     * @param runAfterProcessTermination Optional callback to run after the process terminated or the the timeout was
     * @return A CommandLineCallable that provides the process return value, see {@link Process#exitValue()}. "call"
     *         must be called on this to be sure the forked process has terminated, no guarantees is made about
     *         any internal state before after the completion of the call statements
     * @throws CommandLineException or CommandLineTimeOutException if time out occurs
     */
    public static CommandLineCallable executeCommandLineAsCallable( @Nonnull final Commandline cl,
                                                                    @Nullable final InputStream systemIn,
                                                                    final StreamConsumer systemOut,
                                                                    final StreamConsumer systemErr,
                                                                    final int timeoutInSeconds,
                                                                  @Nullable final Runnable runAfterProcessTermination )
        throws CommandLineException
    {
        return executeCommandLineAsCallable( cl, systemIn, systemOut, systemErr, timeoutInSeconds,
                                             runAfterProcessTermination, null );
    }

    /**
     * Immediately forks a process, returns a callable that will block until process is complete.
     *
     * @param cl               The command line to execute
     * @param systemIn         The input to read from, must be thread safe
     * @param systemOut        A consumer that receives output, must be thread safe
     * @param systemErr        A consumer that receives system error stream output, must be thread safe
     * @param timeoutInSeconds Positive integer to specify timeout, zero and negative integers for no timeout.
     * @param runAfterProcessTermination Optional callback to run after the process terminated or the the timeout was
     * @param streamCharset    Charset to use for reading streams
     * @return A CommandLineCallable that provides the process return value, see {@link Process#exitValue()}. "call"
     *         must be called on this to be sure the forked process has terminated, no guarantees is made about
     *         any internal state before after the completion of the call statements
     * @throws CommandLineException or CommandLineTimeOutException if time out occurs
     */
    public static CommandLineCallable executeCommandLineAsCallable( @Nonnull final Commandline cl,
                                                                    @Nullable final InputStream systemIn,
                                                                    final StreamConsumer systemOut,
                                                                    final StreamConsumer systemErr,
                                                                    final int timeoutInSeconds,
                                                                    @Nullable final Runnable runAfterProcessTermination,
                                                                    @Nullable final Charset streamCharset )
        throws CommandLineException
    {
        //noinspection ConstantConditions
        if ( cl == null )
        {
            throw new IllegalArgumentException( "cl cannot be null." );
        }

        final Process p = cl.execute();

        final Thread processHook = new Thread()
        {

            {
                this.setName( "CommandLineUtils process shutdown hook" );
                this.setContextClassLoader( null );
            }

            @Override
            public void run()
            {
                p.destroy();
            }

        };

        ShutdownHookUtils.addShutDownHook( processHook );

        return new CommandLineCallable()
        {

            @Override
            public Integer call()
                throws CommandLineException
            {
                StreamFeeder inputFeeder = null;
                StreamPumper outputPumper = null;
                StreamPumper errorPumper = null;
                try
                {
                    if ( systemIn != null )
                    {
                        inputFeeder = new StreamFeeder( systemIn, p.getOutputStream() );
                        inputFeeder.setName( "StreamFeeder-systemIn" );
                        inputFeeder.start();
                    }

                    outputPumper = new StreamPumper( p.getInputStream(), systemOut );
                    outputPumper.setName( "StreamPumper-systemOut" );
                    outputPumper.start();

                    errorPumper = new StreamPumper( p.getErrorStream(), systemErr );
                    errorPumper.setName( "StreamPumper-systemErr" );
                    errorPumper.start();

                    int returnValue;
                    if ( timeoutInSeconds <= 0 )
                    {
                        returnValue = p.waitFor();
                    }
                    else
                    {
                        final long now = System.nanoTime();
                        final long timeout = now + NANOS_PER_SECOND * timeoutInSeconds;
                        while ( isAlive( p ) && ( System.nanoTime() < timeout ) )
                        {
                            // The timeout is specified in seconds. Therefore we must not sleep longer than one second
                            // but we should sleep as long as possible to reduce the number of iterations performed.
                            Thread.sleep( MILLIS_PER_SECOND - 1L );
                        }

                        if ( isAlive( p ) )
                        {
                            throw new InterruptedException( String.format( "Process timed out after %d seconds.",
                                                                           timeoutInSeconds ) );

                        }

                        returnValue = p.exitValue();
                    }

// TODO Find out if waitUntilDone needs to be called using a try-finally construct. The method may throw an
//      InterruptedException so that calls to waitUntilDone may be skipped.
//                    try
//                    {
//                        if ( inputFeeder != null )
//                        {
//                            inputFeeder.waitUntilDone();
//                        }
//                    }
//                    finally
//                    {
//                        try
//                        {
//                            outputPumper.waitUntilDone();
//                        }
//                        finally
//                        {
//                            errorPumper.waitUntilDone();
//                        }
//                    }
                    if ( inputFeeder != null )
                    {
                        inputFeeder.waitUntilDone();
                    }

                    outputPumper.waitUntilDone();
                    errorPumper.waitUntilDone();

                    if ( inputFeeder != null )
                    {
                        inputFeeder.close();

                        if ( inputFeeder.getException() != null )
                        {
                            throw new CommandLineException( "Failure processing stdin.", inputFeeder.getException() );
                        }
                    }

                    if ( outputPumper.getException() != null )
                    {
                        throw new CommandLineException( "Failure processing stdout.", outputPumper.getException() );
                    }

                    if ( errorPumper.getException() != null )
                    {
                        throw new CommandLineException( "Failure processing stderr.", errorPumper.getException() );
                    }

                    return returnValue;
                }
                catch ( InterruptedException ex )
                {
                    throw new CommandLineTimeOutException( "Error while executing external command, process killed.",
                                                           ex );

                }
                finally
                {
                    if ( inputFeeder != null )
                    {
                        inputFeeder.disable();
                    }
                    if ( outputPumper != null )
                    {
                        outputPumper.disable();
                    }
                    if ( errorPumper != null )
                    {
                        errorPumper.disable();
                    }

                    try
                    {
                        if ( runAfterProcessTermination != null )
                        {
                            runAfterProcessTermination.run();
                        }
                    }
                    finally
                    {
                        ShutdownHookUtils.removeShutdownHook( processHook );

                        try
                        {
                            processHook.run();
                        }
                        finally
                        {
                            if ( inputFeeder != null )
                            {
                                inputFeeder.close();
                            }
                        }
                    }
                }
            }

        };
    }

    /**
     * Gets the shell environment variables for this process. Note that the returned mapping from variable names to
     * values will always be case-sensitive regardless of the platform, i.e. <code>getSystemEnvVars().get("path")</code>
     * and <code>getSystemEnvVars().get("PATH")</code> will in general return different values. However, on platforms
     * with case-insensitive environment variables like Windows, all variable names will be normalized to upper case.
     *
     * @return The shell environment variables, can be empty but never <code>null</code>.
     * @deprecated use System#getenv()
     */
    @Deprecated
    public static Properties getSystemEnvVars()
    {
        return getSystemEnvVars( !Os.isFamily( Os.FAMILY_WINDOWS ) );
    }

    /**
     * Return the shell environment variables. If <code>caseSensitive == true</code>, then envar
     * keys will all be upper-case.
     *
     * @param caseSensitive Whether environment variable keys should be treated case-sensitively.
     * @return Properties object of (possibly modified) envar keys mapped to their values.
     * @deprecated use System#getenv()
     */
    @Deprecated
    public static Properties getSystemEnvVars( boolean caseSensitive )
    {
        Map<String, String> envs = System.getenv();
        return ensureCaseSensitivity( envs, caseSensitive );
    }

    private static boolean isAlive( Process p )
    {
        if ( p == null )
        {
            return false;
        }

        try
        {
            p.exitValue();
            return false;
        }
        catch ( IllegalThreadStateException e )
        {
            return true;
        }
    }

    /**
     * @param toProcess The command line to translate.
     * @return The array of translated parts.
     * @throws CommandLineException in case of unbalanced quotes.
     */
    public static String[] translateCommandline( String toProcess ) throws CommandLineException
    {
        if ( ( toProcess == null ) || ( toProcess.length() == 0 ) )
        {
            return new String[0];
        }

        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        boolean inEscape = false;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer( toProcess, "\"\' \\", true );
        List<String> tokens = new ArrayList<String>();
        StringBuilder current = new StringBuilder();

        while ( tok.hasMoreTokens() )
        {
            String nextTok = tok.nextToken();
            switch ( state )
            {
                case inQuote:
                    if ( "\'".equals( nextTok ) )
                    {
                        if ( inEscape )
                        {
                            current.append( nextTok );
                            inEscape = false;
                        }
                        else
                        {
                            state = normal;
                        }
                    }
                    else
                    {
                        current.append( nextTok );
                        inEscape = "\\".equals( nextTok );
                    }
                    break;
                case inDoubleQuote:
                    if ( "\"".equals( nextTok ) )
                    {
                        if ( inEscape )
                        {
                            current.append( nextTok );
                            inEscape = false;
                        }
                        else
                        {
                            state = normal;
                        }
                    }
                    else
                    {
                        current.append( nextTok );
                        inEscape = "\\".equals( nextTok );
                    }
                    break;
                default:
                    if ( "\'".equals( nextTok ) )
                    {
                        if ( inEscape )
                        {
                            inEscape = false;
                            current.append( nextTok );
                        }
                        else
                        {
                            state = inQuote;
                        }
                    }
                    else if ( "\"".equals( nextTok ) )
                    {
                        if ( inEscape )
                        {
                            inEscape = false;
                            current.append( nextTok );
                        }
                        else
                            {
                            state = inDoubleQuote;
                        }
                    }
                    else if ( " ".equals( nextTok ) )
                    {
                        if ( current.length() != 0 )
                        {
                            tokens.add( current.toString() );
                            current.setLength( 0 );
                        }
                    }
                    else
                    {
                        current.append( nextTok );
                        inEscape = "\\".equals( nextTok );
                    }
                    break;
            }
        }

        if ( current.length() != 0 )
        {
            tokens.add( current.toString() );
        }

        if ( ( state == inQuote ) || ( state == inDoubleQuote ) )
        {
            throw new CommandLineException( "unbalanced quotes in " + toProcess );
        }

        return tokens.toArray( new String[tokens.size()] );
    }

    /**
     * @param line the lines
     * @return the concatenated lines, quoted and escaped, separated by spaces
     */
    public static String toString( String... line )
    {
        // empty path return empty string
        if ( ( line == null ) || ( line.length == 0 ) )
        {
            return "";
        }

        final StringBuilder result = new StringBuilder();
        for ( int i = 0; i < line.length; i++ )
        {
            if ( i > 0 )
            {
                result.append( ' ' );
            }
            result.append( StringUtils.quoteAndEscape( line[i], '\"' ) );
        }
        return result.toString();
    }

    static Properties ensureCaseSensitivity( Map<String, String> envs, boolean preserveKeyCase )
    {
        Properties envVars = new Properties();
        for ( Map.Entry<String, String> entry : envs.entrySet() )
        {
            envVars.put( !preserveKeyCase ? entry.getKey().toUpperCase( Locale.ENGLISH ) : entry.getKey(),
                         entry.getValue() );
        }
        return envVars;
    }
}
