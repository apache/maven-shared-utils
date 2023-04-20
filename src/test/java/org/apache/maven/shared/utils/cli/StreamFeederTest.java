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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StreamFeederTest {
    static class BlockingInputStream extends ByteArrayInputStream {
        boolean endStream = false;
        final Object lock = new Object();

        public BlockingInputStream(byte[] buf) {
            super(buf);
        }

        @Override
        public synchronized int read() {
            int data = super.read();
            if (data >= 0) {
                return data;
            }

            // end test data ... block
            endStream = true;

            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return -1;
        }

        public synchronized void waitForEndStream() throws InterruptedException {
            while (!endStream) {
                wait(100);
            }
        }
    }

    @Test
    public void waitUntilFeederDone() throws InterruptedException {

        BlockingInputStream inputStream = new BlockingInputStream("TestData".getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        StreamFeeder streamFeeder = new StreamFeeder(inputStream, outputStream);

        streamFeeder.start();

        // wait until input stream will be in block mode
        inputStream.waitForEndStream();

        streamFeeder.waitUntilDone(); // wait until process finish

        assertEquals("TestData", outputStream.toString());
    }
}
