package org.apache.maven.shared.utils.cli.shell;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.shared.utils.StringUtils;

/**
 * <p>
 * Class that abstracts the Shell functionality,
 * with subclases for shells that behave particularly, like
 * <ul>
 * <li><code>command.com</code></li>
 * <li><code>cmd.exe</code></li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * 
 */
public class Shell
    implements Cloneable
{
    private static final char[] DEFAULT_QUOTING_TRIGGER_CHARS = { ' ' };

    private String shellCommand;

    private final List<String> shellArgs = new ArrayList<String>();

    private boolean quotedArgumentsEnabled = true;

    private String executable;

    private String workingDir;

    private boolean quotedExecutableEnabled = true;

    private boolean singleQuotedArgumentEscaped = false;

    private boolean singleQuotedExecutableEscaped = false;

    private char argQuoteDelimiter = '\"';

    private char exeQuoteDelimiter = '\"';

    /**
     * Set the command to execute the shell (eg. COMMAND.COM, /bin/bash,...)
     *
     * @param shellCommand The command
     */
    void setShellCommand( String shellCommand )
    {
        this.shellCommand = shellCommand;
    }

    /**
     * Get the command to execute the shell
     *
     * @return  The command
     */
    String getShellCommand()
    {
        return shellCommand;
    }

    /**
     * Set the shell arguments when calling a command line (not the executable arguments)
     * (eg. /X /C for CMD.EXE)
     *
     * @param shellArgs the arguments to the shell
     */
    void setShellArgs( String[] shellArgs )
    {
        this.shellArgs.clear();
        this.shellArgs.addAll( Arrays.asList( shellArgs ) );
    }

    /**
     * Get the shell arguments
     *
     * @return  The arguments
     */
    String[] getShellArgs()
    {
        if ( ( shellArgs == null ) || shellArgs.isEmpty() )
        {
            return null;
        }
        else
        {
            return shellArgs.toArray( new String[shellArgs.size()] );
        }
    }

    /**
     * Get the command line for the provided executable and arguments in this shell
     *
     * @param executableParameter executable that the shell has to call
     * @param argumentsParameter  arguments for the executable, not the shell
     * @return List with one String object with executable and arguments quoted as needed
     */
    List<String> getCommandLine( String executableParameter, String... argumentsParameter )
    {
        return getRawCommandLine( executableParameter, argumentsParameter );
    }

    /**
     * @param executableParameter Executable.
     * @param argumentsParameter The arguments for the executable.
     * @return The list on command line. 
     */
    List<String> getRawCommandLine( String executableParameter, String... argumentsParameter )
    {
        List<String> commandLine = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();

        if ( executableParameter != null )
        {
            String preamble = getExecutionPreamble();
            if ( preamble != null )
            {
                sb.append( preamble );
            }

            if ( isQuotedExecutableEnabled() )
            {
                char[] escapeChars =
                    getEscapeChars( isSingleQuotedExecutableEscaped(), isDoubleQuotedExecutableEscaped() );

                sb.append( StringUtils.quoteAndEscape( getExecutable(), getExecutableQuoteDelimiter(), escapeChars,
                                                       getQuotingTriggerChars(), '\\', false ) );
            }
            else
            {
                sb.append( getExecutable() );
            }
        }
        for ( String argument : argumentsParameter )
        {
            if ( sb.length() > 0 )
            {
                sb.append( ' ' );
            }

            if ( isQuotedArgumentsEnabled() )
            {
                char[] escapeChars = getEscapeChars( isSingleQuotedArgumentEscaped(), isDoubleQuotedArgumentEscaped() );

                sb.append( StringUtils.quoteAndEscape( argument, getArgumentQuoteDelimiter(), escapeChars,
                                                       getQuotingTriggerChars(), '\\', false ) );
            }
            else
            {
                sb.append( argument );
            }
        }

        commandLine.add( sb.toString() );

        return commandLine;
    }

    char[] getQuotingTriggerChars()
    {
        return DEFAULT_QUOTING_TRIGGER_CHARS;
    }

    String getExecutionPreamble()
    {
        return null;
    }

    char[] getEscapeChars( boolean includeSingleQuote, boolean includeDoubleQuote )
    {
        StringBuilder buf = new StringBuilder( 2 );
        if ( includeSingleQuote )
        {
            buf.append( '\'' );
        }

        if ( includeDoubleQuote )
        {
            buf.append( '\"' );
        }

        char[] result = new char[buf.length()];
        buf.getChars( 0, buf.length(), result, 0 );

        return result;
    }

    /**
     * @return false in all cases. 
     */
    protected boolean isDoubleQuotedArgumentEscaped()
    {
        return false;
    }

    /**
     * @return {@link #singleQuotedArgumentEscaped}
     */
    protected boolean isSingleQuotedArgumentEscaped()
    {
        return singleQuotedArgumentEscaped;
    }

    boolean isDoubleQuotedExecutableEscaped()
    {
        return false;
    }

    boolean isSingleQuotedExecutableEscaped()
    {
        return singleQuotedExecutableEscaped;
    }

    /**
     * @param argQuoteDelimiterParameter {@link #argQuoteDelimiter}
     */
    void setArgumentQuoteDelimiter( char argQuoteDelimiterParameter )
    {
        this.argQuoteDelimiter = argQuoteDelimiterParameter;
    }

    char getArgumentQuoteDelimiter()
    {
        return argQuoteDelimiter;
    }

    /**
     * @param exeQuoteDelimiterParameter {@link #exeQuoteDelimiter}
     */
    void setExecutableQuoteDelimiter( char exeQuoteDelimiterParameter )
    {
        this.exeQuoteDelimiter = exeQuoteDelimiterParameter;
    }

    char getExecutableQuoteDelimiter()
    {
        return exeQuoteDelimiter;
    }

    /**
     * Get the full command line to execute, including shell command, shell arguments,
     * executable and executable arguments
     *
     * @param arguments arguments for the executable, not the shell
     * @return List of String objects, whose array version is suitable to be used as argument
     *         of Runtime.getRuntime().exec()
     */
    public List<String> getShellCommandLine( String... arguments )
    {

        List<String> commandLine = new ArrayList<String>();

        if ( getShellCommand() != null )
        {
            commandLine.add( getShellCommand() );
        }

        if ( getShellArgs() != null )
        {
            commandLine.addAll( getShellArgsList() );
        }

        commandLine.addAll( getCommandLine( getExecutable(), arguments ) );

        return commandLine;

    }

    List<String> getShellArgsList()
    {
        return shellArgs;
    }

    /**
     * @param quotedArgumentsEnabled {@link #quotedArgumentsEnabled}
     */
    public void setQuotedArgumentsEnabled( boolean quotedArgumentsEnabled )
    {
        this.quotedArgumentsEnabled = quotedArgumentsEnabled;
    }

    boolean isQuotedArgumentsEnabled()
    {
        return quotedArgumentsEnabled;
    }

    void setQuotedExecutableEnabled( boolean quotedExecutableEnabled )
    {
        this.quotedExecutableEnabled = quotedExecutableEnabled;
    }

    boolean isQuotedExecutableEnabled()
    {
        return quotedExecutableEnabled;
    }

    /**
     * Sets the executable to run.
     * @param executable The executable.
     */
    public void setExecutable( String executable )
    {
        if ( ( executable == null ) || ( executable.length() == 0 ) )
        {
            return;
        }
        this.executable = executable.replace( '/', File.separatorChar ).replace( '\\', File.separatorChar );
    }

    /**
     * @return The executable.
     */
    public String getExecutable()
    {
        return executable;
    }

    /**
     * Sets execution directory.
     * @param path The path which should be used as working directory.
     */
    public void setWorkingDirectory( String path )
    {
        if ( path != null )
        {
            this.workingDir = path;
        }
    }

    /**
     * Sets execution directory.
     * @param workingDirectory The working directory.
     */
    public void setWorkingDirectory( File workingDirectory )
    {
        if ( workingDirectory != null )
        {
            this.workingDir = workingDirectory.getAbsolutePath();
        }
    }

    /**
     * @return The working directory.
     */
    public File getWorkingDirectory()
    {
        return workingDir == null ? null : new File( workingDir );
    }

    String getWorkingDirectoryAsString()
    {
        return workingDir;
    }

    /** {@inheritDoc} */
    public Object clone()
    {
        throw new RuntimeException( "Do we ever clone this?" );
/*        Shell shell = new Shell();
        shell.setExecutable( getExecutable() );
        shell.setWorkingDirectory( getWorkingDirectory() );
        shell.setShellArgs( getShellArgs() );
        return shell;*/
    }

    void setSingleQuotedArgumentEscaped( boolean singleQuotedArgumentEscaped )
    {
        this.singleQuotedArgumentEscaped = singleQuotedArgumentEscaped;
    }

    void setSingleQuotedExecutableEscaped( boolean singleQuotedExecutableEscaped )
    {
        this.singleQuotedExecutableEscaped = singleQuotedExecutableEscaped;
    }

}
