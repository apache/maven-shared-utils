package org.apache.maven.shared.utils.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WalkCollector implements DirectoryWalkListener
{
    protected List steps;
    protected File startingDir;
    protected int startCount;
    protected int finishCount;
    protected int percentageLow;
    protected int percentageHigh;

    public WalkCollector()
    {
        steps = new ArrayList();
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


    public int getFinishCount()
    {
        return finishCount;
    }

    public int getPercentageHigh()
    {
        return percentageHigh;
    }

    public int getPercentageLow()
    {
        return percentageLow;
    }

    public int getStartCount()
    {
        return startCount;
    }

    public File getStartingDir()
    {
        return startingDir;
    }

    public List getSteps()
    {
        return steps;
    }
}
