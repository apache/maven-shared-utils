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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** 
 * @deprecated use {@code java.nio.file.FileVisitor} and related classes
 */
@Deprecated
public class WalkCollector
    implements DirectoryWalkListener
{
    final List<File> steps;

    File startingDir;

    int startCount;

    int finishCount;

    int percentageLow;

    int percentageHigh;

    /**
     * Create an instance.
     */
    public WalkCollector()
    {
        steps = new ArrayList<File>();
        startCount = 0;
        finishCount = 0;
        percentageLow = 0;
        percentageHigh = 0;
    }

    /** {@inheritDoc} */
    public void debug( String message )
    {
        // can be used to set some message
    }

    /** {@inheritDoc} */
    public void directoryWalkStarting( File basedir )
    {
        startingDir = basedir;
        startCount++;
    }

    /** {@inheritDoc} */
    public void directoryWalkStep( int percentage, File file )
    {
        steps.add( file );
        percentageLow = Math.min( percentageLow, percentage );
        percentageHigh = Math.max( percentageHigh, percentage );
    }

    /** {@inheritDoc} */
    public void directoryWalkFinished()
    {
        finishCount++;
    }
}
