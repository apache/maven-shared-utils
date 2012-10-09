package org.apache.maven.shared.utils.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WalkCollector
    implements DirectoryWalkListener
{
    final List<File> steps;

    File startingDir;

    int startCount;

    int finishCount;

    int percentageLow;

    int percentageHigh;

    public WalkCollector()
    {
        steps = new ArrayList<File>();
        startCount = 0;
        finishCount = 0;
        percentageLow = 0;
        percentageHigh = 0;
    }

    public void debug( String message )
    {
        // can be used to set some message
    }

    public void directoryWalkStarting( File basedir )
    {
        startingDir = basedir;
        startCount++;
    }

    public void directoryWalkStep( int percentage, File file )
    {
        steps.add( file );
        percentageLow = Math.min( percentageLow, percentage );
        percentageHigh = Math.max( percentageHigh, percentage );
    }

    public void directoryWalkFinished()
    {
        finishCount++;
    }
}
