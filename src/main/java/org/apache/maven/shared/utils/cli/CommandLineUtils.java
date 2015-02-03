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
 * @version $Id$
 */
public abstract class CommandLineUtils
{

    @SuppressWarnings( "UnusedDeclaration" )
    public static class StringStreamConsumer
        implements StreamConsumer
    {
        private final StringBuffer string = new StringBuffer();

        private static final String LS = System.getProperty( "line.separator" );

        public void consumeLine( String line )
        {
            string.append( line ).append( LS );
        }

        public String getOutput()
        {
            return string.toString();
        }
    }

    private static class ProcessHook
        extends Thread
    {
        private final Process process;

        private ProcessHook( Process process )
        {
            super( "CommandlineUtils process shutdown hook" );
            this.process = process;
            this.setContextClassLoader( null );
        }

        public void run()
        {
            process.destroy();
        }
    }


    @SuppressWarnings( "UnusedDeclaration" )
    public static int executeCommandLine( @Nonnull Commandline cl, StreamConsumer systemOut, StreamConsumer systemErr )
        throws CommandLineException
    {
        return executeCommandLine( cl, null, systemOut, systemErr, 0 );
    }

    @SuppressWarnings( "UnusedDeclaration" )
    public static int executeCommandLine( @Nonnull Commandline cl, StreamConsumer systemOut, StreamConsumer systemErr,
                                          int timeoutInSeconds )
        throws CommandLineException
    {
        return executeCommandLine( cl, null, systemOut, systemErr, timeoutInSeconds );
    }

    @SuppressWarnings( "UnusedDeclaration" )
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
     * @noinspection ThrowableResultOfMethodCallIgnored
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
     * @noinspection ThrowableResultOfMethodCallIgnored
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
     * @noinspection ThrowableResultOfMethodCallIgnored
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
     * @noinspection ThrowableResultOfMethodCallIgnored
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
     * @noinspection ThrowableResultOfMethodCallIgnored
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

        final StreamFeeder inputFeeder = systemIn != null ? new StreamFeeder( systemIn, p.getOutputStream() ) : null;

        final StreamPumper outputPumper = new StreamPumper( p.getInputStream(), systemOut );

        final StreamPumper errorPumper = new StreamPumper( p.getErrorStream(), systemErr );

        if ( inputFeeder != null )
        {
            inputFeeder.start();
        }

        outputPumper.start();

        errorPumper.start();

        final ProcessHook processHook = new ProcessHook( p );

        ShutdownHookUtils.addShutDownHook( processHook );

        return new CommandLineCallable()
        {
            public Integer call()
                throws CommandLineException
            {
                try
                {
                    int returnValue;
                    if ( timeoutInSeconds <= 0 )
                    {
                        returnValue = p.waitFor();
                    }
                    else
                    {
                        long now = System.currentTimeMillis();
                        long timeoutInMillis = 1000L * timeoutInSeconds;
                        long finish = now + timeoutInMillis;
                        while ( isAlive( p ) && ( System.currentTimeMillis() < finish ) )
                        {
                            Thread.sleep( 10 );
                        }
                        if ( isAlive( p ) )
                        {
                            throw new InterruptedException(
                                "Process timeout out after " + timeoutInSeconds + " seconds" );
                        }

                        returnValue = p.exitValue();
                    }

                    if ( runAfterProcessTermination != null )
                    {
                        runAfterProcessTermination.run();
                    }

                    waitForAllPumpers( inputFeeder, outputPumper, errorPumper );

                    if ( outputPumper.getException() != null )
                    {
                        throw new CommandLineException( "Error inside systemOut parser", outputPumper.getException() );
                    }

                    if ( errorPumper.getException() != null )
                    {
                        throw new CommandLineException( "Error inside systemErr parser", errorPumper.getException() );
                    }

                    return returnValue;
                }
                catch ( InterruptedException ex )
                {
                    if ( inputFeeder != null )
                    {
                        inputFeeder.disable();
                    }

                    outputPumper.disable();
                    errorPumper.disable();
                    throw new CommandLineTimeOutException( "Error while executing external command, process killed.",
                                                           ex );
                }
                finally
                {
                    ShutdownHookUtils.removeShutdownHook( processHook );

                    processHook.run();

                    if ( inputFeeder != null )
                    {
                        inputFeeder.close();
                    }

                    outputPumper.close();

                    errorPumper.close();
                }
            }
        };
    }

    private static void waitForAllPumpers( @Nullable StreamFeeder inputFeeder, StreamPumper outputPumper,
                                           StreamPumper errorPumper )
        throws InterruptedException
    {
        if ( inputFeeder != null )
        {
            inputFeeder.waitUntilDone();
        }

        outputPumper.waitUntilDone();
        errorPumper.waitUntilDone();
    }

    /**
     * Gets the shell environment variables for this process. Note that the returned mapping from variable names to
     * values will always be case-sensitive regardless of the platform, i.e. <code>getSystemEnvVars().get("path")</code>
     * and <code>getSystemEnvVars().get("PATH")</code> will in general return different values. However, on platforms
     * with case-insensitive environment variables like Windows, all variable names will be normalized to upper case.
     *
     * @return The shell environment variables, can be empty but never <code>null</code>.
     * @see System#getenv() System.getenv() API, new in JDK 5.0, to get the same result
     *      <b>since 2.0.2 System#getenv() will be used if available in the current running jvm.</b>
     */
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
     * @see System#getenv() System.getenv() API, new in JDK 5.0, to get the same result
     *      <b>since 2.0.2 System#getenv() will be used if available in the current running jvm.</b>
     */
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

    public static String[] translateCommandline( String toProcess )
        throws Exception
    {
        if ( ( toProcess == null ) || ( toProcess.length() == 0 ) )
        {
            return new String[0];
        }

        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        StringTokenizer tok = new StringTokenizer( toProcess, "\"\' ", true );
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
                        state = normal;
                    }
                    else
                    {
                        current.append( nextTok );
                    }
                    break;
                case inDoubleQuote:
                    if ( "\"".equals( nextTok ) )
                    {
                        state = normal;
                    }
                    else
                    {
                        current.append( nextTok );
                    }
                    break;
                default:
                    if ( "\'".equals( nextTok ) )
                    {
                        state = inQuote;
                    }
                    else if ( "\"".equals( nextTok ) )
                    {
                        state = inDoubleQuote;
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

    public static String toString( String... line )
    {
        // empty path return empty string
        if ( ( line == null ) || ( line.length == 0 ) )
        {
            return "";
        }

        // path containing one or more elements
        final StringBuilder result = new StringBuilder();
        for ( int i = 0; i < line.length; i++ )
        {
            if ( i > 0 )
            {
                result.append( ' ' );
            }
            try
            {
                result.append( StringUtils.quoteAndEscape( line[i], '\"' ) );
            }
            catch ( Exception e )
            {
                System.err.println( "Error quoting argument: " + e.getMessage() );
            }
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
