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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractStreamHandlerTest {
    @Test
    void setDoneNotifiesWaitUntilDone() throws InterruptedException {
        AbstractStreamHandler handler = new AbstractStreamHandler() {};
        CountDownLatch waiting = new CountDownLatch(1);

        Thread waiter = new Thread(() -> {
            try {
                waiting.countDown();
                handler.waitUntilDone();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        waiter.start();

        assertTrue(waiting.await(1, TimeUnit.SECONDS), "waiter thread did not start");

        Thread.sleep(50);
        assertTrue(waiter.isAlive());

        handler.setDone();

        waiter.join(500);
        assertFalse(waiter.isAlive());
    }
}
