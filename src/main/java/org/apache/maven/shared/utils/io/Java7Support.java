package org.apache.maven.shared.utils.io;

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

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Java7 feature detection
 *
 * @author Kristian Rosenvold
 * @deprecated Java 7 is the minimum, so no need to have this class
 */
@Deprecated
public class Java7Support
{
    /**
     * @param file The file to check for being a symbolic link.
     * @return true if the file is a symlink false otherwise.
     * 
     * @deprecated Instead use {@link Files#isSymbolicLink(java.nio.file.Path)}
     */
    public static boolean isSymLink( @Nonnull File file )
    {
        return Files.isSymbolicLink( file.toPath() );
    }


    /**
     * @param symlink The sym link.
     * @return The file.
     * @throws IOException in case of error.
     * @deprecated Instead use {@link Files#readSymbolicLink(java.nio.file.Path)}
     */
    @Nonnull public static File readSymbolicLink( @Nonnull File symlink )
        throws IOException
    {
        return Files.readSymbolicLink( symlink.toPath() ).toFile();
    }


    /**
     * @param file The file to check.
     * @return true if exist false otherwise.
     * @throws IOException in case of failure.
     * @deprecated Instead use {@link Files#exists(java.nio.file.Path, java.nio.file.LinkOption...)}
     */
    public static boolean exists( @Nonnull File file )
        throws IOException
    {
        return Files.exists( file.toPath() );
    }

    // CHECKSTYLE_OFF: LineLength
    /**
     * @param symlink The link name.
     * @param target The target.
     * @return The linked file.
     * @throws IOException in case of an error.
     * @deprecated Instead use
     *             {@link Files#createSymbolicLink(java.nio.file.Path, java.nio.file.Path, java.nio.file.attribute.FileAttribute...)}
     */
    // CHECKSTYLE_ON: LineLength
    @Nonnull public static File createSymbolicLink( @Nonnull File symlink,  @Nonnull File target )
        throws IOException
    {
        if ( !exists( symlink ) )
        {
            return Files.createSymbolicLink( symlink.toPath(), target.toPath() ).toFile();
        }
        return symlink;
    }

    /**
     * Performs a nio delete
     * @param file the file to delete
     * @throws IOException in case of error.
     * @deprecated Instead use {@link Files#delete(java.nio.file.Path)}
     */
    public static void delete( @Nonnull File file )
        throws IOException
    {
        Files.delete( file.toPath() );
    }

    /**
     * @return true
     */
    public static boolean isJava7()
    {
        return true;
    }

    /**
     * @return true
     */
    public static boolean isAtLeastJava7()
    {
        return true;
    }

}
