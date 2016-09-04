package org.apache.maven.shared.utils.logging;

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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class AnsiMessageBuilderTest
{

    private AnsiMessageBuilder ansiMessageBuilder;

    @Before
    public void initializeAnsiMessageBuffer()
    {
        this.ansiMessageBuilder = new AnsiMessageBuilder();
    }

    @Test
    public void should_color_debug()
    {
        ansiMessageBuilder.debug( "DEBUG" );

        assertThat( ansiMessageBuilder.toString(), equalTo( "\u001B[1;36mDEBUG\u001B[m" ) );
    }

    @Test
    public void should_color_info()
    {
        ansiMessageBuilder.info( "INFO" );

        assertThat( ansiMessageBuilder.toString(), equalTo( "\u001B[1;34mINFO\u001B[m" ) );
    }

    @Test
    public void should_color_warning_and_reset()
    {
        ansiMessageBuilder.warning( "WARNING" );

        assertThat( ansiMessageBuilder.toString(), equalTo( "\u001B[1;33mWARNING\u001B[m" ) );
    }

    @Test
    public void should_color_error()
    {
        ansiMessageBuilder.error( "ERROR" );

        assertThat( ansiMessageBuilder.toString(), equalTo( "\u001B[1;31mERROR\u001B[m" ) );
    }

    @Test
    public void should_color_success_with_message()
    {
        ansiMessageBuilder.success( "a success message" );

        assertThat( ansiMessageBuilder.toString(), equalTo( "\u001B[1;32ma success message\u001B[m" ) );
    }

    @Test
    public void should_color_failure_and_reset()
    {
        ansiMessageBuilder.failure( "a failure message" );

        assertThat( ansiMessageBuilder.toString(), equalTo( "\u001B[1;31ma failure message\u001B[m" ) );
    }

    @Test
    public void should_color_strong_and_reset()
    {
        ansiMessageBuilder.strong( "a strong message" );

        assertThat( ansiMessageBuilder.toString(), equalTo( "\u001B[1ma strong message\u001B[m" ) );
    }

    @Test
    public void should_color_mojo_and_reset()
    {
        ansiMessageBuilder.mojo( "a mojo" );

        assertThat( ansiMessageBuilder.toString(), equalTo( "\u001B[32ma mojo\u001B[m" ) );
    }

    @Test
    public void should_color_project_and_reset()
    {
        ansiMessageBuilder.project( "a project" );

        assertThat( ansiMessageBuilder.toString(), equalTo( "\u001B[36ma project\u001B[m" ) );
    }

}
