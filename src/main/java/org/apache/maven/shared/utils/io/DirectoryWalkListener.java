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
package org.apache.maven.shared.utils.io;

import java.io.File;

/**
 * DirectoryWalkListener.
 *
 * @deprecated use {@code java.nio.file.FileVisitor} and related classes
 */
@Deprecated
public interface DirectoryWalkListener {
    /**
     * The directory walking has begun.
     *
     * @param basedir the basedir that walk started in
     */
    void directoryWalkStarting(File basedir);

    /**
     * The included entry that was encountered.
     *
     * @param percentage rough percentage of the walk completed. (inaccurate)
     * @param file       the file that was included
     */
    void directoryWalkStep(int percentage, File file);

    /**
     * The directory walking has finished.
     */
    void directoryWalkFinished();

    /**
     * @param message the message for the debugging output
     */
    void debug(String message);
}
