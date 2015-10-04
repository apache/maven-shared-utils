package org.apache.maven.shared.utils.cli.shell;

import java.util.List;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.shared.utils.StringUtils;
import org.apache.maven.shared.utils.cli.Commandline;

import junit.framework.TestCase;

public class BourneShellTest
    extends TestCase
{

    Shell newShell()
    {
        return new BourneShell();
    }

    public void testQuoteWorkingDirectoryAndExecutable()
    {
        Shell sh = newShell();

        sh.setWorkingDirectory( "/usr/local/bin" );
        sh.setExecutable( "chmod" );

        String executable = StringUtils.join( sh.getShellCommandLine( new String[]{} ).iterator(), " " );

        assertEquals( "/bin/sh -c cd /usr/local/bin && chmod", executable );
    }

    public void testQuoteWorkingDirectoryAndExecutable_WDPathWithSingleQuotes()
    {
        Shell sh = newShell();

        sh.setWorkingDirectory( "/usr/local/'something else'" );
        sh.setExecutable( "chmod" );

        String executable = StringUtils.join( sh.getShellCommandLine( new String[]{} ).iterator(), " " );

        assertEquals( "/bin/sh -c cd \"/usr/local/\'something else\'\" && chmod", executable );
    }

    public void testQuoteWorkingDirectoryAndExecutable_WDPathWithSingleQuotes_BackslashFileSep()
    {
        Shell sh = newShell();

        sh.setWorkingDirectory( "\\usr\\local\\'something else'" );
        sh.setExecutable( "chmod" );

        String executable = StringUtils.join( sh.getShellCommandLine( new String[]{} ).iterator(), " " );

        assertEquals( "/bin/sh -c cd \"\\usr\\local\\\'something else\'\" && chmod", executable );
    }

    public void testPreserveSingleQuotesOnArgument()
    {
        Shell sh = newShell();

        sh.setWorkingDirectory( "/usr/bin" );
        sh.setExecutable( "chmod" );

        String[] args = { "\'some arg with spaces\'" };

        List<String> shellCommandLine = sh.getShellCommandLine( args );

        String cli = StringUtils.join( shellCommandLine.iterator(), " " );
        System.out.println( cli );
        assertTrue( cli.endsWith( args[0] ) );
    }

    public void testAddSingleQuotesOnArgumentWithSpaces()
    {
        Shell sh = newShell();

        sh.setWorkingDirectory( "/usr/bin" );
        sh.setExecutable( "chmod" );

        String[] args = { "some arg with spaces" };

        List<String> shellCommandLine = sh.getShellCommandLine( args );

        String cli = StringUtils.join( shellCommandLine.iterator(), " " );
        System.out.println( cli );
        assertTrue( cli.endsWith( "\'" + args[0] + "\'" ) );
    }

    public void testArgumentsWithsemicolon()
    {

        System.out.println( "---- semi colon tests ----" );

        Shell sh = newShell();

        sh.setWorkingDirectory( "/usr/bin" );
        sh.setExecutable( "chmod" );

        String[] args = { ";some&argwithunix$chars" };

        List<String> shellCommandLine = sh.getShellCommandLine( args );

        String cli = StringUtils.join( shellCommandLine.iterator(), " " );
        System.out.println( cli );
        assertTrue( cli.endsWith( "\'" + args[0] + "\'" ) );

        Commandline commandline = new Commandline( newShell() );
        commandline.setExecutable( "chmod" );
        commandline.getShell().setQuotedArgumentsEnabled( true );
        commandline.createArg().setValue( "--password" );
        commandline.createArg().setValue( ";password" );

        List<String> lines = commandline.getShell().getShellCommandLine( commandline.getArguments() );
        System.out.println( lines  );

        assertEquals( "/bin/sh", lines.get( 0 ) );
        assertEquals( "-c", lines.get( 1 ) );
        assertEquals( "chmod --password ';password'", lines.get( 2 ) );

        commandline = new Commandline( newShell() );
        commandline.setExecutable( "chmod" );
        commandline.getShell().setQuotedArgumentsEnabled( true );
        commandline.createArg().setValue( "--password" );
        commandline.createArg().setValue( ";password" );
        lines = commandline.getShell().getShellCommandLine( commandline.getArguments() );
        System.out.println( lines );

        assertEquals( "/bin/sh", lines.get( 0) );
        assertEquals( "-c", lines.get( 1 ) );
        assertEquals( "chmod --password ';password'", lines.get( 2 ) );

        commandline = new Commandline( new CmdShell() );
        commandline.getShell().setQuotedArgumentsEnabled( true );
        commandline.createArg().setValue( "--password" );
        commandline.createArg().setValue( ";password" );
        lines = commandline.getShell().getShellCommandLine( commandline.getArguments() );
        System.out.println( lines );

        assertEquals( "cmd.exe", lines.get( 0 ) );
        assertEquals( "/X", lines.get( 1 ) );
        assertEquals( "/C", lines.get( 2 ) );
        assertEquals( "\"--password ;password\"", lines.get( 3 ) );
    }

    public void testBourneShellQuotingCharacters()
        throws Exception
    {
        // { ' ', '$', ';', '&', '|', '<', '>', '*', '?', '(', ')' };
        // test with values http://steve-parker.org/sh/bourne.shtml Appendix B - Meta-characters and Reserved Words
        Commandline commandline = new Commandline( newShell() );
        commandline.setExecutable( "chmod" );
        commandline.getShell().setQuotedArgumentsEnabled( true );
        commandline.createArg().setValue( " " );
        commandline.createArg().setValue( "|" );
        commandline.createArg().setValue( "&&" );
        commandline.createArg().setValue( "||" );
        commandline.createArg().setValue( ";" );
        commandline.createArg().setValue( ";;" );
        commandline.createArg().setValue( "&" );
        commandline.createArg().setValue( "()" );
        commandline.createArg().setValue( "<" );
        commandline.createArg().setValue( "<<" );
        commandline.createArg().setValue( ">" );
        commandline.createArg().setValue( ">>" );
        commandline.createArg().setValue( "*" );
        commandline.createArg().setValue( "?" );
        commandline.createArg().setValue( "[" );
        commandline.createArg().setValue( "]" );
        commandline.createArg().setValue( "{" );
        commandline.createArg().setValue( "}" );
        commandline.createArg().setValue( "`" );

        List<String> lines = commandline.getShell().getShellCommandLine( commandline.getArguments() );
        System.out.println( lines  );

        assertEquals( "/bin/sh", lines.get( 0 ) );
        assertEquals( "-c", lines.get( 1 ) );
        assertEquals( "chmod ' ' '|' '&&' '||' ';' ';;' '&' '()' '<' '<<' '>' '>>' '*' '?' '[' ']' '{' '}' '`'",
                      lines.get( 2 ) );
    }

}
