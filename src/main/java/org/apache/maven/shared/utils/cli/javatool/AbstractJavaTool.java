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

import org.apache.maven.shared.utils.Os;
import org.apache.maven.shared.utils.StringUtils;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
import org.apache.maven.shared.utils.cli.Commandline;
import org.apache.maven.shared.utils.cli.StreamConsumer;
import org.apache.maven.toolchain.Toolchain;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * Abstract implementation of a {@link JavaTool}.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 0.5
 */
public abstract class AbstractJavaTool<Request extends JavaToolRequest>
    extends AbstractLogEnabled
    implements JavaTool<Request>
{

    /**
     * The java tool name to find out in the jdk.
     */
    private final String javaToolName;

    /**
     * The location of the java tool executable file.
     */
    private String javaToolFile;

    /**
     * Optional toolChain used to find java tool executable file.
     */
    private Toolchain toolchain;

    protected AbstractJavaTool( String javaToolName )
    {
        this.javaToolName = javaToolName;
    }

    /**
     * Create the commandline object given the request.
     *
     * @param request      User request on the java tool
     * @param javaToolFile Location of the java tool file to use
     * @return the commandline
     * @throws JavaToolException if could not create the command line from the request
     */
    protected abstract Commandline createCommandLine( Request request, String javaToolFile )
        throws JavaToolException;

    /**
     * {@inheritDoc}
     */
    public String getJavaToolName()
    {
        return javaToolName;
    }

    /**
     * {@inheritDoc}
     */
    public void setToolchain( Toolchain toolchain )
    {
        this.toolchain = toolchain;
    }

    /**
     * {@inheritDoc}
     */
    public JavaToolResult execute( Request request )
        throws JavaToolException
    {

        if ( javaToolFile == null )
        {

            // find the java tool file to use
            try
            {
                javaToolFile = findJavaToolExecutable();
            }
            catch ( Exception e )
            {
                throw new JavaToolException( "Error finding " + javaToolName + " executable. Reason: " + e.getMessage(),
                                             e );
            }
        }

        // creates the command line from the given request
        Commandline cli = createCommandLine( request, javaToolFile );

        // execute it
        JavaToolResult result = executeCommandLine( cli, request );

        // return result
        return result;
    }

    protected InputStream createSystemInputStream()
    {
        InputStream systemIn = new InputStream()
        {

            /**
             * {@inheritDoc}
             */
            public int read()
            {
                return -1;
            }

        };
        return systemIn;
    }

    protected JavaToolResult executeCommandLine( Commandline cli, Request request )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing: " + cli );
        }

        JavaToolResult result = createResult();

        result.setCommandline( cli );

        InputStream systemIn = createSystemInputStream();

        StreamConsumer systemOut = createSystemOutStreamConsumer( request );

        StreamConsumer systemErr = createSystemErrorStreamConsumer( request );

        try
        {
            int resultCode = CommandLineUtils.executeCommandLine( cli, systemIn, systemOut, systemErr );

            result.setExitCode( resultCode );
        }
        catch ( CommandLineException e )
        {
            result.setExecutionException( e );
        }

        return result;
    }

    protected StreamConsumer createSystemErrorStreamConsumer( Request request )
    {
        StreamConsumer systemErr = request.getSystemErrorStreamConsumer();

        if ( systemErr == null )
        {
            systemErr = new StreamConsumer()
            {

                /**
                 * {@inheritDoc}
                 */
                public void consumeLine( final String line )
                {
                    getLogger().warn( line );
                }

            };
        }
        return systemErr;
    }

    protected StreamConsumer createSystemOutStreamConsumer( Request request )
    {
        StreamConsumer systemOut = request.getSystemOutStreamConsumer();

        if ( systemOut == null )
        {

            systemOut = new StreamConsumer()
            {

                /**
                 * {@inheritDoc}
                 */
                public void consumeLine( final String line )
                {
                    getLogger().info( line );

                }

            };
        }
        return systemOut;
    }

    protected JavaToolResult createResult()
    {
        return new JavaToolResult();
    }

    protected String findJavaToolExecutable()
    {
        String command = javaToolName + ( Os.isFamily( Os.FAMILY_WINDOWS ) ? ".exe" : "" );

        String executable = null;

        if ( toolchain != null )
        {
            executable = toolchain.findTool( javaToolName );
        }

        if ( executable == null )
        {
            executable = findExecutable( command, System.getProperty( "java.home" ), "../bin", "bin", "../sh" );
        }

        if ( executable == null )
        {

            Map<String, String> env = System.getenv();

            String[] variables = { "JDK_HOME", "JAVA_HOME" };

            for ( String variable : variables )
            {
                executable = findExecutable( command, env.get( variable ), "bin", "sh" );
                if ( executable != null )
                {
                    break;
                }
            }
        }

        if ( executable == null )
        {
            executable = command;
        }

        return executable;
    }

    /**
     * Finds the specified command in any of the given sub directories of the specified JDK/JRE home directory.
     *
     * @param command The command to find, must not be <code>null</code>.
     * @param homeDir The home directory to search in, may be <code>null</code>.
     * @param subDirs The sub directories of the home directory to search in, must not be <code>null</code>.
     * @return The (absolute) path to the command if found, <code>null</code> otherwise.
     */
    private String findExecutable( String command, String homeDir, String... subDirs )
    {
        String result = null;
        if ( StringUtils.isNotEmpty( homeDir ) )
        {
            for ( String subDir : subDirs )
            {
                File file = new File( new File( homeDir, subDir ), command );

                if ( file.isFile() )
                {
                    result = file.getAbsolutePath();
                    break;
                }
            }
        }

        return result;
    }
}
