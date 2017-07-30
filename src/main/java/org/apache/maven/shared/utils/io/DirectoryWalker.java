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
import java.util.Stack;

/**
 * DirectoryWalker
 * 
 * @version $Id$
 */
class DirectoryWalker
{
    /**
     * DirStackEntry is an Item on the {@link DirectoryWalker#dirStack}
     */
    static class DirStackEntry
    {
        /**
         * Count of files in the directory.
         */
        private final int count;

        /**
         * Current Directory.
         */
        private final File dir;

        /**
         * Index (or offset) within the directory count.
         */
        private int index;

        /**
         * Offset for percentage calculations. Based on parent DirStackEntry.
         */
        private double percentageOffset;

        /**
         * Size of percentage space to work with.
         */
        private double percentageSize;

        /**
         * Create a DirStackEntry.
         * 
         * @param d the directory to track
         * @param length the length of entries in the directory.
         */
        DirStackEntry( File d, int length )
        {
            dir = d;
            count = length;
        }

        /**
         * Calculate the next percentage offset. Used by the next DirStackEntry.
         * 
         * @return the value for the next percentage offset.
         */
        public double getNextPercentageOffset()
        {
            return percentageOffset + ( index * ( percentageSize / count ) );
        }

        /**
         * Calculate the next percentage size. Used by the next DirStackEntry.
         * 
         * @return the value for the next percentage size.
         */
        public double getNextPercentageSize()
        {
            return ( percentageSize / count );
        }

        /**
         * The percentage of the DirStackEntry right now. Based on count, index, percentageOffset, and percentageSize.
         * 
         * @return the percentage right now.
         */
        public int getPercentage()
        {
            double percentageWithinDir = (double) index / (double) count;
            return (int) Math.floor( percentageOffset + ( percentageWithinDir * percentageSize ) );
        }

        public String toString()
        {
            return "DirStackEntry[" + "dir=" + dir.getAbsolutePath() + ",count=" + count + ",index=" + index
                + ",percentageOffset=" + percentageOffset + ",percentageSize=" + percentageSize + ",percentage()="
                + getPercentage() + ",getNextPercentageOffset()=" + getNextPercentageOffset()
                + ",getNextPercentageSize()=" + getNextPercentageSize() + "]";
        }
    }

    private File baseDir;

    private int baseDirOffset;

    private Stack<DirStackEntry> dirStack;

    private final List<String> excludes;

    private final List<String> includes;

    private final List<DirectoryWalkListener> listeners;

    DirectoryWalker()
    {
        this.includes = new ArrayList<String>();
        this.excludes = new ArrayList<String>();
        this.listeners = new ArrayList<DirectoryWalkListener>();
    }

    public void addDirectoryWalkListener( DirectoryWalkListener listener )
    {
        this.listeners.add( listener );
    }

    void addExclude( String exclude )
    {
        this.excludes.add( fixPattern( exclude ) );
    }

    void addInclude( String include )
    {
        this.includes.add( fixPattern( include ) );
    }

    /**
     * Add's to the Exclude List the default list of SCM excludes.
     */
    public void addSCMExcludes()
    {
        String scmexcludes[] = DirectoryScanner.DEFAULTEXCLUDES;
        for ( String scmexclude : scmexcludes )
        {
            addExclude( scmexclude );
        }
    }

    private void fireStep( File file )
    {
        DirStackEntry dsEntry = dirStack.peek();
        int percentage = dsEntry.getPercentage();
        for ( DirectoryWalkListener listener : this.listeners )
        {
            listener.directoryWalkStep( percentage, file );
        }
    }

    private void fireWalkFinished()
    {
        for ( Object listener1 : this.listeners )
        {
            DirectoryWalkListener listener = (DirectoryWalkListener) listener1;
            listener.directoryWalkFinished();
        }
    }

    private void fireWalkStarting()
    {
        for ( Object listener1 : this.listeners )
        {
            DirectoryWalkListener listener = (DirectoryWalkListener) listener1;
            listener.directoryWalkStarting( this.baseDir );
        }
    }

    private void fireDebugMessage( String message )
    {
        for ( Object listener1 : this.listeners )
        {
            DirectoryWalkListener listener = (DirectoryWalkListener) listener1;
            listener.debug( message );
        }
    }

    private String fixPattern( String pattern )
    {
        String cleanPattern = pattern;

        if ( File.separatorChar != '/' )
        {
            cleanPattern = cleanPattern.replace( '/', File.separatorChar );
        }

        if ( File.separatorChar != '\\' )
        {
            cleanPattern = cleanPattern.replace( '\\', File.separatorChar );
        }

        return cleanPattern;
    }

    private boolean isExcluded( String name )
    {
        return isMatch( this.excludes, name );
    }

    private boolean isIncluded( String name )
    {
        return isMatch( this.includes, name );
    }

    private boolean isMatch( List<String> patterns, String name )
    {
        for ( String pattern : patterns )
        {
            boolean caseSensitive = true;
            if ( SelectorUtils.matchPath( pattern, name, caseSensitive ) )
            {
                return true;
            }
        }

        return false;
    }

    private String relativeToBaseDir( File file )
    {
        return file.getAbsolutePath().substring( baseDirOffset + 1 );
    }

    /**
     * Performs a Scan against the provided {@link #setBaseDir(File)}
     */
    public void scan()
    {
        if ( baseDir == null )
        {
            throw new IllegalStateException( "Scan Failure.  BaseDir not specified." );
        }

        if ( !baseDir.exists() )
        {
            throw new IllegalStateException( "Scan Failure.  BaseDir does not exist." );
        }

        if ( !baseDir.isDirectory() )
        {
            throw new IllegalStateException( "Scan Failure.  BaseDir is not a directory." );
        }

        if ( this.includes.isEmpty() )
        {
            // default to include all.
            addInclude( "**" );
        }

        fireWalkStarting();
        dirStack = new Stack<DirStackEntry>();
        scanDir( this.baseDir );
        fireWalkFinished();
    }

    private void scanDir( File dir )
    {
        File files[] = dir.listFiles();

        if ( files == null )
        {
            return;
        }

        DirStackEntry curStackEntry = new DirStackEntry( dir, files.length );
        if ( dirStack.isEmpty() )
        {
            curStackEntry.percentageOffset = 0;
            curStackEntry.percentageSize = 100;
        }
        else
        {
            DirStackEntry previousStackEntry = dirStack.peek();
            curStackEntry.percentageOffset = previousStackEntry.getNextPercentageOffset();
            curStackEntry.percentageSize = previousStackEntry.getNextPercentageSize();
        }

        dirStack.push( curStackEntry );

        for ( int idx = 0; idx < files.length; idx++ )
        {
            curStackEntry.index = idx;
            String name = relativeToBaseDir( files[idx] );

            if ( isExcluded( name ) )
            {
                fireDebugMessage( name + " is excluded." );
                continue;
            }

            if ( files[idx].isDirectory() )
            {
                scanDir( files[idx] );
            }
            else
            {
                if ( isIncluded( name ) )
                {
                    fireStep( files[idx] );
                }
            }
        }

        dirStack.pop();
    }

    /**
     * @param baseDir The baseDir to set.
     */
    public void setBaseDir( File baseDir )
    {
        this.baseDir = baseDir;
        this.baseDirOffset = baseDir.getAbsolutePath().length();
    }

}
