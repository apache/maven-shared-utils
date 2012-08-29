package org.apache.maven.shared.utils.testhelpers;

import org.apache.maven.shared.utils.io.FileUtils;
import org.apache.maven.shared.utils.io.IOUtil;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * A few utility methods for file based tests
 */
public final class FileTestHelper
{

    private FileTestHelper()
    {
        // utility function doesn't need a public ct
    }

    public static byte[] generateTestData( long size )
    {
        try
        {
            ByteArrayOutputStream baout = new ByteArrayOutputStream();
            generateTestData( baout, size );
            return baout.toByteArray();
        }
        catch ( IOException ioe )
        {
            throw new RuntimeException( "This should never happen: " + ioe.getMessage() );
        }
    }

    public static void generateTestData( OutputStream out, long size )
            throws IOException
    {
        for ( int i = 0; i < size; i++ )
        {
            // nice varied byte pattern compatible with Readers and Writers
            out.write( (byte) ( ( i % 127 ) + 1 ) );
        }
    }

    public static void generateTestFile( File testfile, int size ) throws IOException
    {
        if ( testfile.exists() )
        {
            testfile.delete();
        }

        OutputStream os = new FileOutputStream( testfile );
        generateTestData( os, size );
        os.flush();
        os.close();
    }



    public static void createLineBasedFile( File file, String[] data )
            throws IOException
    {
        if ( file.getParentFile() != null && !file.getParentFile().exists() )
        {
            throw new IOException( "Cannot create file " + file + " as the parent directory does not exist" );
        }
        PrintWriter output = new PrintWriter( new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" ) );
        try
        {
            for ( int i = 0; i < data.length; i++ )
            {
                output.println( data[i] );
            }
        }
        finally
        {
            IOUtil.close( output );
        }
    }

    /**
     * Check if the given file exists in the given folder and remove it.
     *
     * @return the File object for a new file
     * @throws IOException
     */
    public static File newFile( TemporaryFolder folder, String filename )
            throws IOException
    {
        File destination = folder.newFile( filename );

        if ( destination.exists() )
        {
            FileUtils.forceDelete( destination );
        }
        return destination;
    }
}
