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

/**
 * <p>Works in concert with the StreamPumper class to
 * allow implementations to gain access to the lines being
 * "Pumped".</p>
 * <p>Please note that implementations of this interface can be expected to be
 * called from arbitrary threads and must therefore be threadsafe.</p>
 *
 * @author <a href="mailto:fvancea@maxiq.com">Florin Vancea</a>
 * @author <a href="mailto:pj@thoughtworks.com">Paul Julius</a>
 */
public interface StreamConsumer {
    /**
     * Called when the StreamPumper pumps a line from the Stream.
     * @param line The line to be consumed.
     * @throws IOException if consuming {@code line} fails.
     */
    void consumeLine(String line) throws IOException;
}
