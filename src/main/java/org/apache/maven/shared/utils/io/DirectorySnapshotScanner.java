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

import org.apache.maven.shared.utils.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Scan for files in a directory at a given time and reports removed and added files
 * between captures.
 */
public class DirectorySnapshotScanner
{
    private final static String[] NO_FILES = new String[0];

    private File rootDirectory;
    private String[] scannedOldFiles;
    private String[] scannedNewFiles = NO_FILES;
    private String[] filesAdded;
    private String[] filesRemoved;

    public DirectorySnapshotScanner( File rootDirectory )
    {
        this.rootDirectory = rootDirectory;
    }

    public void capture()
    {
        DirectoryScanner ds = new DirectoryScanner();
        capture( ds );
    }

    public void capture( DirectoryScanner ds )
    {
        ds.setBasedir( rootDirectory );
        ds.scan();

        scannedNewFiles = ds.getIncludedFiles();

        if ( scannedOldFiles != null )
        {
            calculateDiff( scannedOldFiles, scannedNewFiles );

        }
        else
        {
            filesAdded = NO_FILES;
            filesRemoved = NO_FILES;
        }

        // this attempts new files is the baseline for the next scan
        scannedOldFiles = scannedNewFiles;
    }

    /**
     * @return all files which got scanned during the last capture.
     */
    public String[] getScannedFiles()
    {
        return scannedNewFiles;
    }

    /**
     * @return all files which got detected as being added between 2 capture calls
     */
    public String[] getFilesAdded()
    {
        return filesAdded;
    }

    /**
     * @return all files which got detected as being removed between 2 capture calls
     */
    public String[] getFilesRemoved()
    {
        return filesRemoved;
    }

    /**
     * Determine the file differences between the oldFiles and newFiles.
     * This method will not look for a changed in content but sole in the
     * list of files given.
     *
     * The result of the diff can be queried by the methods
     * {@link #getFilesAdded()} and {@link #getFilesRemoved()}
     *
     * @param oldFiles
     * @param newFiles
     */
    public void calculateDiff( String[] oldFiles, String[] newFiles )
    {
        Set<String> oldFileSet = CollectionUtils.arrayAsHashSet( oldFiles );
        Set<String> newFileSet = CollectionUtils.arrayAsHashSet( newFiles );

        List<String> added = new ArrayList<String>();
        List<String> removed = new ArrayList<String>();

        for ( String oldFile : oldFileSet )
        {
            if ( !newFileSet.contains( oldFile ) )
            {
                removed.add( oldFile );
            }
        }

        for ( String newFile : newFileSet )
        {
            if ( !oldFileSet.contains( newFile ) )
            {
                added.add( newFile );
            }
        }

        filesAdded = added.toArray( new String[ added.size() ] );
        filesRemoved = removed.toArray( new String[ removed.size() ] );
    }

}
