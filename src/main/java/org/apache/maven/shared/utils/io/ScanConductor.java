package org.apache.maven.shared.utils.io;

import java.io.File;

/**
 * <p>Visitor pattern for the DirectoryScanner. A ScanConductor controls the scanning process.</p>
 *
 * <p>Create an instance and pass it to
 * {@link org.apache.maven.shared.utils.io.DirectoryScanner#setScanConductor(ScanConductor)}.
 * You will get notified about every visited directory and file. You can use the {@link ScanAction}
 * to control what should happen next.</p>
 *
 * <p>A ScanConductor might also store own information but users must make sure that the state gets
 * cleaned between two scan() invocations.</p>
 *
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public interface ScanConductor
{
    public enum ScanAction
    {
        /**
         * Abort the whole scanning process. The current file will not
         * be added anymore.
         */
        ABORT,

        /**
         * Continue the scanning with the next item in the list.
         */
        CONTINUE,

        /**
         * This response is only valid for {@link ScanConductor#visitDirectory(String, java.io.File)}.
         * Do not recurse into the current directory. The current directory will not be added
         * and the processing will be continued with the next item in the list.
         */
        NO_RECURSE,

        /**
         * Abort processing the current directory.
         * The current file will not be added.
         * The processing will continue it's scan in the parent directory if any.
         */
        ABORT_DIRECTORY
    }

    /**
     * This method will get invoked for every detected directory.
     *
     * @param name the directory name (contains parent folders up to the pwd)
     * @param directory
     * @return the ScanAction to control how to proceed with the scanning
     */
    ScanAction visitDirectory( String name, File directory );

    /**
     * This method will get invoked for every detected file.
     *
     * @param name the file name (contains parent folders up to the pwd)
     * @param file
     * @return the ScanAction to control how to proceed with the scanning
     */
    ScanAction visitFile( String name, File file );
}
