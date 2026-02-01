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
import java.io.IOException;
import java.nio.file.Files;

import org.jspecify.annotations.NonNull;

/**
 * Java7 feature detection.
 *
 * @author Kristian Rosenvold
 * @deprecated no longer needed, prefer to use {@link java.nio.file.Files} methods directly.
 */
@Deprecated
public class Java7Support {
    /**
     * @param file the file to check for being a symbolic link
     * @return true if the file is a symlink false otherwise
     */
    public static boolean isSymLink(@NonNull File file) {
        return Files.isSymbolicLink(file.toPath());
    }

    /**
     * @param symlink the sym link
     * @return the file
     * @throws IOException in case of error
     */
    @NonNull
    public static File readSymbolicLink(@NonNull File symlink) throws IOException {
        return Files.readSymbolicLink(symlink.toPath()).toFile();
    }

    /**
     * @param file the file to check
     * @return true if exist false otherwise
     * @throws IOException in case of failure
     */
    public static boolean exists(@NonNull File file) throws IOException {
        return Files.exists(file.toPath());
    }

    /**
     * @param symlink the link name
     * @param target the target
     * @return the linked file
     * @throws IOException in case of an error
     */
    @NonNull
    public static File createSymbolicLink(@NonNull File symlink, @NonNull File target) throws IOException {
        return FileUtils.createSymbolicLink(symlink, target);
    }

    /**
     * Performs a nio delete.
     *
     * @param file the file to delete
     * @throws IOException in case of error
     */
    public static void delete(@NonNull File file) throws IOException {
        Files.delete(file.toPath());
    }

    /**
     * @return true in case of Java 7
     */
    public static boolean isJava7() {
        return true;
    }

    /**
     * @return true in case of Java7 or greater
     */
    public static boolean isAtLeastJava7() {
        return true;
    }
}
