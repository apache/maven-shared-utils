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
package org.apache.maven.shared.utils.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnsiMessageBuilderTest {

    private AnsiMessageBuilder ansiMessageBuilder;

    @BeforeEach
    public void initializeAnsiMessageBuffer() {
        this.ansiMessageBuilder = new AnsiMessageBuilder();
    }

    @Test
    public void shouldColorDebug() {
        ansiMessageBuilder.debug("DEBUG");

        assertThat(ansiMessageBuilder.toString(), equalTo("\u001B[1;36mDEBUG\u001B[m"));
    }

    @Test
    public void shouldColorInfo() {
        ansiMessageBuilder.info("INFO");

        assertThat(ansiMessageBuilder.toString(), equalTo("\u001B[1;34mINFO\u001B[m"));
    }

    @Test
    public void shouldColorWarningAndReset() {
        ansiMessageBuilder.warning("WARNING");

        assertThat(ansiMessageBuilder.toString(), equalTo("\u001B[1;33mWARNING\u001B[m"));
    }

    @Test
    public void shouldColorError() {
        ansiMessageBuilder.error("ERROR");

        assertThat(ansiMessageBuilder.toString(), equalTo("\u001B[1;31mERROR\u001B[m"));
    }

    @Test
    public void shouldColorSuccessWithMessage() {
        ansiMessageBuilder.success("a success message");

        assertThat(ansiMessageBuilder.toString(), equalTo("\u001B[1;32ma success message\u001B[m"));
    }

    @Test
    public void shouldColorFailureAndReset() {
        ansiMessageBuilder.failure("a failure message");

        assertThat(ansiMessageBuilder.toString(), equalTo("\u001B[1;31ma failure message\u001B[m"));
    }

    @Test
    public void shouldColorStrongAndReset() {
        ansiMessageBuilder.strong("a strong message");

        assertThat(ansiMessageBuilder.toString(), equalTo("\u001B[1ma strong message\u001B[m"));
    }

    @Test
    public void shouldColorMojoAndReset() {
        ansiMessageBuilder.mojo("a mojo");

        assertThat(ansiMessageBuilder.toString(), equalTo("\u001B[32ma mojo\u001B[m"));
    }

    @Test
    public void shouldColorProjectAndReset() {
        ansiMessageBuilder.project("a project");

        assertThat(ansiMessageBuilder.toString(), equalTo("\u001B[36ma project\u001B[m"));
    }
}
