package org.apache.maven.shared.utils.io;


import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.write;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
public class SymlinkTestSetup
{
    /**
     * Creates a standard directory layout with symlinks and files.
     */
    public static File createStandardSymlinkTestDir( File root )
        throws IOException
    {
        File srcDir = new File( root, "/src" );
        srcDir.mkdirs();
        File target = new File( srcDir, "targetDir" );
        target.mkdirs();
        write( new File( target, "targetFile.txt" ), "a regular File payload" );
        File aRegularDir = new File( srcDir, "aRegularDir" );
        aRegularDir.mkdirs();
        write( new File( aRegularDir, "aRegularFile.txt" ), "a regular File payload" );

        File dirOnTheOutside = new File( root, "dirOnTheOutside" );
        dirOnTheOutside.mkdirs();
        write( new File( dirOnTheOutside, "FileInDirOnTheOutside.txt" ), "a file in dir on the outside" );
        write( new File( root, "onTheOutside.txt" ), "A file on the outside" );
        write( new File( srcDir, "fileR.txt" ), "FileR payload" );
        write( new File( srcDir, "fileW.txt" ), "FileW payload" );
        write( new File( srcDir, "fileX.txt" ), "FileX payload" );
        // todo: set file attributes (not used here)

        Java7Support.createSymbolicLink( new File( srcDir, "symDir" ), new File( "targetDir" ) );
        Java7Support.createSymbolicLink( new File( srcDir, "symLinkToDirOnTheOutside" ),
                                         new File( "../dirOnTheOutside" ) );
        Java7Support.createSymbolicLink( new File( srcDir, "symLinkToFileOnTheOutside" ),
                                         new File( "../onTheOutside.txt" ) );
        Java7Support.createSymbolicLink( new File( srcDir, "symR" ), new File( "fileR.txt" ) );
        Java7Support.createSymbolicLink( new File( srcDir, "symW" ), new File( "fileW.txt" ) );
        Java7Support.createSymbolicLink( new File( srcDir, "symX" ), new File( "fileX.txt" ) );
        return srcDir;
    }
}
