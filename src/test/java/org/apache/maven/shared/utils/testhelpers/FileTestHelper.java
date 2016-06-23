package org.apache.maven.shared.utils.testhelpers;

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

import org.apache.maven.shared.utils.io.FileUtils;
import org.apache.maven.shared.utils.io.IOUtil;
import org.junit.rules.TemporaryFolder;

import java.io.*;

/**
 * A few utility methods for file based tests
 */
public final class FileTestHelper
{

    private FileTestHelper()
    {
        // utility function doesn't need a public ct
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

    public static void generateTestFile( File testfile, int size )
        throws IOException
    {
        if ( testfile.exists() )
        {
            //noinspection ResultOfMethodCallIgnored
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

        PrintWriter out = null;
        try
        {
            out = new PrintWriter( new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" ) );
            for ( String aData : data )
            {
                out.println( aData );
            }
            out.close();
            out = null;
        }
        finally
        {
            IOUtil.close( out );
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
