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
 * Read from an InputStream and write the output to an OutputStream.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
class StreamFeeder extends Thread {

    private final InputStream input;

    private final OutputStream output;

    private Throwable exception;
    private boolean done;

    private final Object lock = new Object();

    /**
     * Create a new StreamFeeder
     *
     * @param input  Stream to read from
     * @param output Stream to write to
     */
    StreamFeeder(InputStream input, OutputStream output) {
        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);
        this.done = false;
    }

    @Override
    @SuppressWarnings("checkstyle:innerassignment")
    public void run() {
        try {
            for (int data; !isInterrupted() && (data = input.read()) != -1; ) {
                output.write(data);
            }
            output.flush();
        } catch (IOException e) {
            exception = e;
        } finally {
            close();
        }

        synchronized (lock) {
            done = true;
            lock.notifyAll();
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
        this.interrupt();
        synchronized (lock) {
            while (!done) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
