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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.maven.shared.utils.Os;
import org.apache.maven.shared.utils.StringUtils;
import org.apache.maven.shared.utils.cli.shell.BourneShell;
import org.apache.maven.shared.utils.cli.shell.CmdShell;
import org.apache.maven.shared.utils.cli.shell.Shell;

/**
 * <p>
 * Commandline objects help handling command lines specifying processes to
 * execute.
 * </p>
 * <p>
 * The class can be used to define a command line as nested elements or as a
 * helper to define a command line by an application.
 * </p>
 * <code>
 * &lt;someelement&gt;<br>
 * &nbsp;&nbsp;&lt;acommandline executable="/executable/to/run"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;argument value="argument 1" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;argument line="argument_1 argument_2 argument_3" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;argument value="argument 4" /&gt;<br>
 * &nbsp;&nbsp;&lt;/acommandline&gt;<br>
 * &lt;/someelement&gt;<br>
 * </code>
 * <p>
 * The element <code>someelement</code> must provide a method
 * <code>createAcommandline</code> which returns an instance of this class.
 * </p>
 *
 * @author thomas.haas@softwired-inc.com
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 */
public class Commandline
    implements Cloneable
{
    private final List<Arg> arguments = new Vector<Arg>();

    private final Map<String, String> envVars = Collections.synchronizedMap( new LinkedHashMap<String, String>() );

    private Shell shell;

    /**
     * Create a new command line object.
     * Shell is autodetected from operating system.
     * 
     * @param shell the shell instance
     */
    public Commandline( Shell shell )
    {
        this.shell = shell;
    }

    /**
     * Create a new command line object.
     * Shell is autodetected from operating system.
     *
     * @param toProcess the command to process
     * @throws CommandLineException in case of unbalanced quotes.
     */
    public Commandline( String toProcess ) throws CommandLineException
    {
        setDefaultShell();
        String[] tmp = CommandLineUtils.translateCommandline( toProcess );
        if ( ( tmp.length > 0 ) )
        {
            setExecutable( tmp[0] );
            for ( int i = 1; i < tmp.length; i++ )
            {
                createArg().setValue( tmp[i] );
            }
        }
    }

    /**
     * Create a new command line object.
     * Shell is autodetected from operating system.
     */
    public Commandline()
    {
        setDefaultShell();
    }

    /**
     * <p>Sets the shell or command-line interpreter for the detected operating system,
     * and the shell arguments.</p>
     */
    private void setDefaultShell()
    {
        //If this is windows set the shell to command.com or cmd.exe with correct arguments.
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            setShell( new CmdShell() );
        }
        else
        {
            setShell( new BourneShell() );
        }
    }

    /**
     * Creates an empty argument object and inserts it at the end of the argument list.
     *
     * @return the argument object
     */
    public Arg createArg()
    {
        return this.createArg( false );
    }

    /**
     * Creates an argument object and adds it to the list of args.
     *
     * @param insertAtStart if true, the argument is inserted at the
     *                      beginning of the list of args. Otherwise it is appended.
     * @return the argument
     */
    public Arg createArg( boolean insertAtStart )
    {
        Arg argument = new Argument();
        if ( insertAtStart )
        {
            arguments.add( 0, argument );
        }
        else
        {
            arguments.add( argument );
        }
        return argument;
    }

    /**
     * Sets the executable to run.
     * 
     * @param executable the executable
     */
    public void setExecutable( String executable )
    {
        shell.setExecutable( executable );
    }

    /**
     * @return the executable
     */
    public String getExecutable()
    {

        return shell.getExecutable();
    }

    /**
     * @param line the arguments
     */
    public void addArguments( String... line )
    {
        for ( String aLine : line )
        {
            createArg().setValue( aLine );
        }
    }

    /**
     * Add an environment variable.
     * 
     * @param name the name of the environment variable
     * @param value the appropriate value
     */
    public void addEnvironment( String name, String value )
    {
        //envVars.add( name + "=" + value );
        envVars.put( name, value );
    }

    /**
     * Add system environment variables.
     */
    public void addSystemEnvironment()
    {
        Properties systemEnvVars = CommandLineUtils.getSystemEnvVars();

        for ( Object o : systemEnvVars.keySet() )
        {
            String key = (String) o;
            if ( !envVars.containsKey( key ) )
            {
                addEnvironment( key, systemEnvVars.getProperty( key ) );
            }
        }
    }

    /**
     * Return the list of environment variables.
     * 
     * @return an array of all environment variables
     */
    public String[] getEnvironmentVariables()
    {
        addSystemEnvironment();
        List<String> environmentVars = new ArrayList<>();
        for ( String name : envVars.keySet() )
        {
            String value = envVars.get( name );
            if ( value != null )
            {
                environmentVars.add( name + "=" + value );
            }
        }
        return environmentVars.toArray( new String[0] );
    }

    /**
     * Returns the executable and all defined arguments.
     * 
     * @return an array of all arguments including the executable
     */
    public String[] getCommandline()
    {
        final String[] args = getArguments();
        String executable = getExecutable();

        if ( executable == null )
        {
            return args;
        }
        final String[] result = new String[args.length + 1];
        result[0] = executable;
        System.arraycopy( args, 0, result, 1, args.length );
        return result;
    }

    /**
     * @return the shell, executable and all defined arguments without masking any arguments
     */
    private String[] getShellCommandline()
    {
        return getShellCommandline( false ) ;
    }

    /**
     * @param mask flag to mask any arguments (having his {@code mask} field to {@code true})
     * @return the shell, executable and all defined arguments with masking some arguments if
     * {@code mask} parameter is on
     */
    private String[] getShellCommandline( boolean mask )
    {
        List<String> shellCommandLine = getShell().getShellCommandLine( getArguments( mask ) );
        return shellCommandLine.toArray( new String[shellCommandLine.size()] );
    }

    /**
     * Returns all arguments defined by <code>addLine</code>,
     * <code>addValue</code> or the argument object.
     * @return an array of arguments.
     */
    public String[] getArguments()
    {
        return getArguments( false );
    }

    /**
     * Returns all arguments defined by <code>addLine</code>,
     * <code>addValue</code>, or the argument object.
     *
     * @param mask flag to mask any arguments (having his {@code mask} field to {@code true})
     * @return an array of arguments
     */
    public String[] getArguments( boolean mask )
    {
        List<String> result = new ArrayList<String>( arguments.size() * 2 );
        for ( Arg argument : arguments )
        {
            Argument arg = (Argument) argument;
            String[] s = arg.getParts();
            if ( s != null )
            {
                if ( mask && ( arg.isMask() ) )
                {
                    // should be a key-pair argument
                    if ( s.length > 0 )
                    {

                        // use a masked copy
                        String[] copy = new String[s.length];
                        Arrays.fill( copy, "*****" );
                        s = copy;
                    }
                }
                Collections.addAll( result, s );
            }
        }

        return result.toArray( new String[result.size()] );
    }

    /** {@inheritDoc}
     */
    public String toString()
    {
        return StringUtils.join( getShellCommandline( true ), " " );
    }


    /** {@inheritDoc}
     */
    public Object clone()
    {
        throw new RuntimeException( "Do we ever clone a commandline?" );
/*        Commandline c = new Commandline( (Shell) shell.clone() );
       c.addArguments( getArguments() );
        return c;*/
    }

    /**
     * Sets working directory.
     * 
     * @param path the working directory
     */
    public void setWorkingDirectory( String path )
    {
        shell.setWorkingDirectory( path );
    }

    /**
     * Sets working directory.
     * 
     * @param workingDirectory the working directory
     */
    public void setWorkingDirectory( File workingDirectory )
    {
        shell.setWorkingDirectory( workingDirectory );
    }

    /**
     * @return the working directory
     */
    public File getWorkingDirectory()
    {
        return shell.getWorkingDirectory();
    }

    /**
     * Clear out the arguments but leave the executable in place for another operation.
     */
    public void clearArgs()
    {
        arguments.clear();
    }

    /**
     * Execute the command.
     * 
     * @return the process
     * @throws CommandLineException in case of errors
     */
    public Process execute()
        throws CommandLineException
    {
        Process process;

        String[] environment = getEnvironmentVariables();

        File workingDir = shell.getWorkingDirectory();

        try
        {
            if ( workingDir == null )
            {
                process = Runtime.getRuntime().exec( getShellCommandline(), environment );
            }
            else
            {
                if ( !workingDir.exists() )
                {
                    throw new CommandLineException(
                        "Working directory \"" + workingDir.getPath() + "\" does not exist!" );
                }
                else if ( !workingDir.isDirectory() )
                {
                    throw new CommandLineException(
                        "Path \"" + workingDir.getPath() + "\" does not specify a directory." );
                }

                process = Runtime.getRuntime().exec( getShellCommandline(), environment, workingDir );
            }
        }
        catch ( IOException ex )
        {
            throw new CommandLineException( "Error while executing process.", ex );
        }

        return process;
    }

    /**
     * Set the shell to be used for this command line.
     *
     * @param shell the shell
     */
    void setShell( Shell shell )
    {
        this.shell = shell;
    }

    /**
     * Get the shell to be used in this command line.
     * 
     * @return the shell
     */
    public Shell getShell()
    {
        return shell;
    }

    /**
     * A single command line argument
     */
    public static class Argument
        implements Arg
    {
        private String[] parts;

        private boolean mask;

        /**
         * {@inheritDoc}
         */
        public void setValue( String value )
        {
            if ( value != null )
            {
                parts = new String[]{ value };
            }
        }

        /**
         * {@inheritDoc}
         */
        public void setLine( String line ) throws CommandLineException
        {
            if ( line == null )
            {
                return;
            }
            parts = CommandLineUtils.translateCommandline( line );
        }

        /**
         * {@inheritDoc}
         */
        public void setFile( File value )
        {
            parts = new String[]{ value.getAbsolutePath() };
        }

        /**
         * {@inheritDoc}
         */
        public void setMask( boolean mask )
        {
            this.mask = mask;
        }

        /**
         * @return the parts
         */
        private String[] getParts()
        {
            return parts;
        }

        /**
         * @return true/false
         */
        public boolean isMask()
        {
            return mask;
        }
    }
}
