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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Poll InputStream for available data and write the output to an OutputStream.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @deprecated Use {@code org.codehaus.plexus.util.cli.StreamPollFeeder} instead.
 */
@Deprecated
class StreamPollFeeder extends Thread {

    public static final int BUF_LEN = 80;

    private final InputStream input;
    private final OutputStream output;

    private Throwable exception;

    private boolean done;
    private final Object lock = new Object();

    /**
     * Create a new StreamPollFeeder
     *
     * @param input  Stream to read from
     * @param output Stream to write to
     */
    StreamPollFeeder(InputStream input, OutputStream output) {
        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);
        this.done = false;
    }

    @Override
    public void run() {

        byte[] buf = new byte[BUF_LEN];

        try {
            while (!done) {
                if (input.available() > 0) {
                    int i = input.read(buf);
                    if (i > 0) {
                        output.write(buf, 0, i);
                        output.flush();
                    } else {
                        done = true;
                    }
                } else {
                    synchronized (lock) {
                        if (!done) {
                            lock.wait(100);
                        }
                    }
                }
            }
        } catch (IOException e) {
            exception = e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            close();
        }
    }

    private void close() {
        try {
            output.close();
        } catch (IOException e) {
            if (exception == null) {
                exception = e;
            }
        }
    }

    /**
     * @since 3.2.0
     */
    public Throwable getException() {
        return this.exception;
    }

    public void waitUntilDone() {

        synchronized (lock) {
            done = true;
            lock.notifyAll();
        }

        try {
            join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
