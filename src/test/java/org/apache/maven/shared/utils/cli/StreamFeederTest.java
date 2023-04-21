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
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return -1;
        }
    }

    @Test
    public void waitUntilFeederDone() throws InterruptedException {

        String TEST_DATA = "TestData";

        BlockingInputStream inputStream = new BlockingInputStream(TEST_DATA.getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        StreamFeeder streamFeeder = new StreamFeeder(inputStream, outputStream);

        streamFeeder.start();

        // wait until all data from steam will be read
        while (outputStream.size() < TEST_DATA.length()) {
            Thread.sleep(10);
        }

        streamFeeder.waitUntilDone(); // wait until process finish

        assertEquals(TEST_DATA, outputStream.toString());
    }
}
