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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.shared.utils.Os;

import org.junit.Test;

public class CommandLineUtilsTest
{

    /**
     * Tests that case-insensitive environment variables are normalized to upper case.
     */
    @Test
    public void testGetSystemEnvVarsCaseInsensitive()
    {
        Properties vars = CommandLineUtils.getSystemEnvVars( false );
        for ( Object o : vars.keySet() )
        {
            String variable = (String) o;
            assertEquals( variable.toUpperCase( Locale.ENGLISH ), variable );
        }
    }

    @Test
    public void testEnsureCaseSensitivity()
        throws Exception
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put( "abz", "cool" );
        assertTrue( CommandLineUtils.ensureCaseSensitivity( data, false ).containsKey( "ABZ" ) );
        assertTrue( CommandLineUtils.ensureCaseSensitivity( data, true ).containsKey( "abz" ) );
    }

    /**
     * Tests that environment variables on Windows are normalized to upper case. Does nothing on Unix platforms.
     */
    @Test
    public void testGetSystemEnvVarsWindows()
        throws Exception
    {
        if ( !Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            return;
        }
        Properties vars = CommandLineUtils.getSystemEnvVars();
        for ( Object o : vars.keySet() )
        {
            String variable = (String) o;
            assertEquals( variable.toUpperCase( Locale.ENGLISH ), variable );
        }
    }

    /**
     * Tests the splitting of a command line into distinct arguments.
     */
    @Test
    public void testTranslateCommandline()
        throws Exception
    {
        assertCmdLineArgs( new String[] {}, null );
        assertCmdLineArgs( new String[] {}, "" );

        assertCmdLineArgs( new String[] { "foo", "bar" }, "foo bar" );
        assertCmdLineArgs( new String[] { "foo", "bar" }, "   foo   bar   " );

        assertCmdLineArgs( new String[] { "foo", " double quotes ", "bar" }, "foo \" double quotes \" bar" );
        assertCmdLineArgs( new String[] { "foo", " single quotes ", "bar" }, "foo ' single quotes ' bar" );

        assertCmdLineArgs( new String[] { "foo", " \" ", "bar" }, "foo ' \" ' bar" );
        assertCmdLineArgs( new String[] { "foo", " ' ", "bar" }, "foo \" ' \" bar" );
    }


    @Test
    public void givenASingleQuoteMarkInArgument_whenExecutingCode_thenNoExceptionIsThrown() throws Exception {
        new Commandline("echo \"let's go\"").execute();
    }

    @Test
    public void givenADoubleQuoteMarkInArgument_whenExecutingCode_thenCommandLineExceptionIsThrown() throws Exception {
        try {
            new Commandline("echo \"let\"s go\"").execute();
        } catch (CommandLineException e) {
            assertTrue(true);
            return;
        }
        fail("Exception was not thrown when given invalid (3 unescaped double quote) input");
    }


    @Test
    public void givenASingleQuoteMarkInArgument_whenExecutingCode_thenExitCode0Returned() throws Exception {
        final Process p = new Commandline("echo \"let's go\"").execute();
        // Note, this sleep should be removed when java version reaches Java 8
        Thread.sleep(1000);
        assertEquals(0, p.exitValue());
    }

    @Test
    public void givenASingleQuoteMarkInArgument_whenTranslatingToCmdLineArgs_thenTheQuotationMarkIsNotEscaped()
        throws Exception
    {
        final String command = "echo \"let's go\"";
        final String[] expected = new String[] { "echo", "let's go" };
        assertCmdLineArgs( expected, command );
    }

    @Test
    public void givenAnEscapedDoubleQuoteMarkInArgument_whenTranslatingToCmdLineArgs_thenTheQuotationMarkRemainsEscaped()
        throws Exception
    {
        final String command = "echo \"let\\\"s go\"";
        final String[] expected = new String[] { "echo", "let\\\"s go" };
        assertCmdLineArgs( expected, command );
    }

    @Test
    public void givenAnEscapedSingleQuoteMarkInArgument_whenTranslatingToCmdLineArgs_thenTheQuotationMarkRemainsEscaped()
        throws Exception
    {
        final String command = "echo \"let\\'s go\"";
        final String[] expected = new String[] { "echo", "let\\'s go"};
        assertCmdLineArgs( expected, command );
    }

    @Test
    public void givenAnEscapedDoubleQuoteMarkInArgument_whenTranslatingToCmdLineArgs_thenNoExceptionIsThrown()
        throws Exception
    {
        new Commandline( "echo \"let\\\"s go\"" ).execute();
    }

    private void assertCmdLineArgs( final String[] expected, final String cmdLine )
        throws Exception
    {
        String[] actual = CommandLineUtils.translateCommandline( cmdLine );
        assertNotNull( actual );
        assertEquals( expected.length, actual.length );
        assertEquals( Arrays.asList( expected ), Arrays.asList( actual ) );
    }

    @Test
    public void environmentVariableWithNullShouldNotBeSet() {

        Commandline commandline = new Commandline();
        commandline.addEnvironment("TEST_NULL_ENV", null);

        String[] environmentVariables = commandline.getEnvironmentVariables();

        assertNotNull( environmentVariables );
        assertThat( environmentVariables, not( hasItemInArray( "TEST_NULL_ENV=null" ) ) );
    }

    @Test
    public void environmentVariableFromSystemIsCopied() {

        Commandline commandline = new Commandline();

        String[] environmentVariables = commandline.getEnvironmentVariables();

        assertNotNull(environmentVariables);
        assertThat(environmentVariables, hasItemInArray( "TEST_SHARED_ENV=TestValue" ) );
    }

    @Test
    public void environmentVariableFromSystemIsRemoved() {

        Commandline commandline = new Commandline();
        commandline.addEnvironment("TEST_SHARED_ENV", null);

        String[] environmentVariables = commandline.getEnvironmentVariables();

        assertNotNull(environmentVariables);
        assertThat(environmentVariables, not ( hasItemInArray( "TEST_SHARED_ENV=TestValue" ) ) );
    }

}
