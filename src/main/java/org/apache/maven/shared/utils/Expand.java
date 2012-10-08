package org.apache.maven.shared.utils;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.maven.shared.utils.io.FileUtils;

/**
 * Expand will unpack the given zip archive.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class Expand
{
    /**
     * Source file which should get expanded
     */
    private File source;

    /**
     * destination directory
     */
    private File dest;

    /**
     * if the unpackaging should get performed if the destination already exists.
     */
    private boolean overwrite = false;

    private static final int BUFFER_SIZE = 2 ^ 16;


    /**
     * The zip archive which should get expanded.
     *
     * @param sourceArchive
     */
    public void setSrc( File sourceArchive )
    {
        this.source = sourceArchive;
    }

    /**
     * Set the destination directory into which the archive should get expanded.
     * The directory will get created if it doesn't yet exist
     * while executing the expand.
     *
     * @param destinationDirectory
     */
    public void setDest( File destinationDirectory )
    {
        this.dest = destinationDirectory;
    }

    /**
     * If the destination directory should get overwritten if the content
     * already exists. If <code>false</code> we will only overwrite if the local
     * file or directory is older than the one in the archive.
     *
     * @param overwrite
     */
    public void setOverwrite( boolean overwrite )
    {
        this.overwrite = overwrite;
    }

    /**
     * Actually perform the unpacking of the source archive
     * into the destination directory.
     *
     * @throws Exception
     */
    public void execute()
        throws Exception
    {
        expandFile( source, dest );
    }

    /**
     * <p>It is intended to be overwritten when implementing an own unarchiver</p>
     * <p/>
     * <p><b>Note:</b> we kept this protected method for the sake of backward compatibility!</p>
     *
     * @param srcFile
     * @param dest
     * @throws Exception
     */
    protected void expandFile( File srcFile, File dest )
        throws Exception
    {
        if ( source == null )
        {
            throw new NullPointerException( "Source Archive must not be null!" );
        }

        File destDir = dest;
        if ( destDir == null )
        {
            destDir = new File( System.getProperty( "user.dir" ) );
        }

        FileInputStream fileInputStream = new FileInputStream( srcFile );
        try
        {
            ZipInputStream zipInputStream = new ZipInputStream( fileInputStream );

            ZipEntry zipEntry;

            while ( ( zipEntry = zipInputStream.getNextEntry() ) != null )
            {
                String zipEntryName = zipEntry.getName();
                Date zipEntryDate = new Date( zipEntry.getTime() );

                extractFile( source, destDir, zipInputStream, zipEntryName, zipEntryDate, zipEntry.isDirectory() );
            }
        }
        finally
        {
            try
            {
                fileInputStream.close();
            }
            catch ( IOException ioe )
            {
                // no worries, all is ok ...
            }
        }
    }

    /**
     * Extract a single ZipEntry.
     * <p/>
     * <p><b>Note:</b> we kept this protected method for the sake of backward compatibility!</p>
     *
     * @param archive               the archive to unpack
     * @param destDir               the destination dirctory
     * @param compressedInputStream
     * @param entryName
     * @param entryDate
     * @param isDirectory
     * @throws Exception
     */
    protected void extractFile( File archive, File destDir, InputStream compressedInputStream, String entryName,
                                Date entryDate, boolean isDirectory )
        throws Exception
    {
        File targetFile = new File( destDir, entryName );

        // if overwrite is specified and the file type
        // of the existing file does not match, then delete it
        if ( overwrite && targetFile.exists() && targetFile.isDirectory() != isDirectory )
        {
            deleteFileOrDir( targetFile );
        }

        if ( !targetFile.exists() ||
            overwrite ||
            targetFile.lastModified() <= entryDate.getTime() )
        {
            if ( isDirectory )
            {
                targetFile.mkdirs();
            }
            else
            {
                byte[] buffer = new byte[BUFFER_SIZE];
                FileOutputStream fileOutputStream = new FileOutputStream( targetFile );
                try
                {
                    int len;
                    while ( ( len = compressedInputStream.read( buffer ) ) > 0 )
                    {
                        fileOutputStream.write( buffer, 0, len );
                    }
                }
                finally
                {
                    try
                    {
                        fileOutputStream.close();
                    }
                    catch ( IOException ioe )
                    {
                        // no worries, all is ok ...
                    }
                }
                targetFile.setLastModified( entryDate.getTime() );
            }
        }
    }

    /**
     * small helper method who deletes the given directory or file.
     *
     * @param targetFile
     * @throws IOException
     */
    private void deleteFileOrDir( File targetFile )
        throws IOException
    {
        if ( targetFile.isDirectory() )
        {
            FileUtils.deleteDirectory( targetFile );
        }
        else
        {
            targetFile.delete();
        }

    }
}
