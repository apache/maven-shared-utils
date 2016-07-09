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

public class AnsiMessageBufferTest
{

    private AnsiMessageBuffer ansiMessageBuffer;

    @Before
    public void initializeAnsiMessageBuffer()
    {
        this.ansiMessageBuffer = new AnsiMessageBuffer();
    }

    @Test
    public void should_color_debug()
    {
        ansiMessageBuffer.debug();

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[1;36m" ) );
    }

    @Test
    public void should_color_info()
    {
        ansiMessageBuffer.info();

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[1;34m" ) );
    }

    @Test
    public void should_color_warning()
    {
        ansiMessageBuffer.warning();

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[1;33m" ) );
    }

    @Test
    public void should_color_warning_and_reset()
    {
        ansiMessageBuffer.warning( "a warning message" );

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[1;33ma warning message\u001B[m" ) );
    }

    @Test
    public void should_color_error()
    {
        ansiMessageBuffer.error();

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[1;31m" ) );
    }

    @Test
    public void should_color_success()
    {
        ansiMessageBuffer.success();

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[1;32m" ) );
    }

    @Test
    public void should_color_success_with_message()
    {
        ansiMessageBuffer.success( "a success message" );

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[1;32ma success message\u001B[m" ) );
    }

    @Test
    public void should_color_failure()
    {
        ansiMessageBuffer.failure();

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[1;31m" ) );
    }

    @Test
    public void should_color_failure_and_reset()
    {
        ansiMessageBuffer.failure( "a failure message" );

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[1;31ma failure message\u001B[m" ) );
    }

    @Test
    public void should_color_strong()
    {
        ansiMessageBuffer.strong();

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[1m" ) );
    }

    @Test
    public void should_color_strong_and_reset()
    {
        ansiMessageBuffer.strong( "a strong message" );

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[1ma strong message\u001B[m" ) );
    }

    @Test
    public void should_color_mojo()
    {
        ansiMessageBuffer.mojo();

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[32m" ) );
    }

    @Test
    public void should_color_mojo_and_reset()
    {
        ansiMessageBuffer.mojo( "a mojo" );

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[32ma mojo\u001B[m" ) );
    }

    @Test
    public void should_color_project()
    {
        ansiMessageBuffer.project();

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[36m" ) );
    }

    @Test
    public void should_color_project_and_reset()
    {
        ansiMessageBuffer.project( "a project" );

        assertThat( ansiMessageBuffer.toString(), equalTo( "\u001B[36ma project\u001B[m" ) );
    }

}
