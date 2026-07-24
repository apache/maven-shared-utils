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
package org.apache.maven.shared.utils.cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class StreamPollFeederTest {

    @Test
    public void waitUntilFeederDoneWithContinuousData() {
        // Simulates a stream with continuous data where the feeder thread
        // never enters the synchronized(lock) wait block, so the done
        // flag must be visible without relying on lock's happens-before.
        InputStream continuousInput = new InputStream() {
            @Override
            public int available() {
                return 1;
            }

            @Override
            public int read() throws IOException {
                return 0;
            }
        };

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            StreamPollFeeder feeder = new StreamPollFeeder(continuousInput, new java.io.OutputStream() {
                @Override
                public void write(int b) {
                    // discard
                }
            });
            feeder.setDaemon(true);
            feeder.start();
            feeder.waitUntilDone();
            assertNull(feeder.getException());
        });
    }

    @Test
    public void waitUntilFeederDoneOnInputStream() throws Exception {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamPollFeeder streamPollFeeder = new StreamPollFeeder(System.in, outputStream);

        // start thread
        streamPollFeeder.start();

        // wait a moment
        Thread.sleep(100);

        // wait until process finish
        streamPollFeeder.waitUntilDone();
        assertNull(streamPollFeeder.getException());
    }

    @Test
    public void dataShouldBeCopied() throws InterruptedException {

        StringBuilder testData = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            testData.append("TestData");
        }

        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(testData.toString().getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        StreamPollFeeder streamPollFeeder = new StreamPollFeeder(inputStream, outputStream);

        streamPollFeeder.start();

        //  wait until all data from steam will be read
        while (outputStream.size() < testData.length()) {
            Thread.sleep(100);
        }

        // wait until process finish
        streamPollFeeder.waitUntilDone();
        assertNull(streamPollFeeder.getException());

        assertEquals(testData.toString(), outputStream.toString());
    }
}
