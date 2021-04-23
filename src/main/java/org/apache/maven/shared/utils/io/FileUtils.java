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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.io.IOUtils;
import org.apache.maven.shared.utils.Os;
import org.apache.maven.shared.utils.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * This class provides basic facilities for manipulating files and file paths.
 *
 * <h3>Path-related methods</h3>
 * 
 * Methods exist to retrieve the components of a typical file path. For example
 * <code>/www/hosted/mysite/index.html</code>, can be broken into:
 * <ul>
 * <li><code>/www/hosted/mysite/index</code> -- retrievable through {@link #removeExtension}</li>
 * <li><code>html</code> -- retrievable through {@link #getExtension}</li>
 * </ul>
 *
 * <h3>File-related methods</h3>
 * 
 * <p>There are methods to create a {@link #toFile File from a URL}, copy a
 * {@link #copyFile File to another File},
 * copy a {@link #copyURLToFile URL's contents to a File},
 * as well as methods to {@link #deleteDirectory(File) delete} and {@link #cleanDirectory(File)
 * clean} a directory.
 * </p>
 * <p>Common {@link java.io.File} manipulation routines.</p>
 * <p>
 * Taken from the commons-utils repo.
 * Also code from Alexandria's FileUtils.
 * And from Avalon Excalibur's IO.
 * And from Ant.
 * </p>
 *
 * @author <a href="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph.Reck</a>
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 */
public class FileUtils
{
    /**
     * protected constructor.
     */
    protected FileUtils()
    {
        // This is a utility class.  Normally don't instantiate
    }

    /**
     * The number of bytes in a kilobyte.
     */
    private static final int ONE_KB = 1024;

    /**
     * The number of bytes in a megabyte.
     */
    private static final int ONE_MB = ONE_KB * ONE_KB;

    /**
     * The file copy buffer size (30 MB)
     */
    private static final int FILE_COPY_BUFFER_SIZE = ONE_MB * 30;

    /**
     * The vm line separator
     */
    private static final String FS = System.getProperty( "file.separator" );

    /**
     * Non-valid Characters for naming files, folders under Windows: <code>":", "*", "?", "\"", "<", ">", "|"</code>
     *
     * @see <a href="http://support.microsoft.com/?scid=kb%3Ben-us%3B177506&x=12&y=13">
     * http://support.microsoft.com/?scid=kb%3Ben-us%3B177506&x=12&y=13</a>
     */
    private static final String[] INVALID_CHARACTERS_FOR_WINDOWS_FILE_NAME = { ":", "*", "?", "\"", "<", ">", "|" };

    /**
     * @return the default excludes pattern
     * @see DirectoryScanner#DEFAULTEXCLUDES
     */
    @Nonnull public static String[] getDefaultExcludes()
    {
        return DirectoryScanner.DEFAULTEXCLUDES;
    }

    /**
     * @return the default excludes pattern as list
     * @see #getDefaultExcludes()
     */
    @Nonnull public static List<String> getDefaultExcludesAsList()
    {
        return Arrays.asList( getDefaultExcludes() );
    }

    /**
     * @return the default excludes pattern as comma separated string.
     * @see DirectoryScanner#DEFAULTEXCLUDES
     * @see StringUtils#join(Object[], String)
     */
    @Nonnull public static String getDefaultExcludesAsString()
    {
        return StringUtils.join( DirectoryScanner.DEFAULTEXCLUDES, "," );
    }

    /**
     * Returns the directory path portion of a file specification string.
     * Matches the equally named unix command.
     *
     * @param path the file path
     * @return the directory portion excluding the ending file separator
     * @deprecated use {@code Paths.get(path).getParent().getName()}
     */
    @Deprecated
    @Nonnull public static String dirname( @Nonnull String path )
    {
        int i = path.lastIndexOf( File.separator );
        return ( i >= 0 ? path.substring( 0, i ) : "" );
    }

    /**
     * Returns the filename portion of a path.
     *
     * @param path the file path
     * @return the filename string with extension
     * @deprecated use {@code Paths.get(path).getName()}
     */
    @Deprecated
    @Nonnull public static String filename( @Nonnull String path )
    {
        int i = path.lastIndexOf( File.separator );
        return ( i >= 0 ? path.substring( i + 1 ) : path );
    }

    /**
     * Returns the extension portion of a file path.
     * This is everything after the last dot '.' in the path (NOT including the dot).
     *
     * @param path the file path
     * @return the extension of the file
     * @deprecated use {@code org.apache.commons.io.FilenameUtils.getExtension}
     */
    @Deprecated
    @Nonnull public static String extension( @Nonnull String path )
    {
        // Ensure the last dot is after the last file separator
        int lastSep = path.lastIndexOf( File.separatorChar );
        int lastDot;
        if ( lastSep < 0 )
        {
            lastDot = path.lastIndexOf( '.' );
        }
        else
        {
            lastDot = path.substring( lastSep + 1 ).lastIndexOf( '.' );
            if ( lastDot >= 0 )
            {
                lastDot += lastSep + 1;
            }
        }

        if ( lastDot >= 0 && lastDot > lastSep )
        {
            return path.substring( lastDot + 1 );
        }

        return "";
    }

    /**
     * Check if a file exists.
     *
     * @param fileName the file path
     * @return true if file exists
     * @deprecated use {@code java.io.File.exists()}
     */
    @Deprecated
    public static boolean fileExists( @Nonnull String fileName )
    {
        File file = new File( fileName );
        return file.exists();
    }

    /**
     * Note: the file content is read with platform encoding.
     *
     * @param file the file path
     * @return the file content using the platform encoding
     * @throws IOException if any
     * @deprecated use {@code new String(java.nio.files.Files.readAllBytes(file))}
     */
    @Deprecated
    @Nonnull public static String fileRead( @Nonnull String file )
        throws IOException
    {
        return fileRead( file, null );
    }

    /**
     * @param file     the file path
     * @param encoding the wanted encoding
     * @return the file content using the specified encoding
     * @throws IOException if any
     * @deprecated use {@code new String(java.nio.files.Files.readAllBytes(Paths.get(file)), encoding)}
     */
    @Deprecated
    @Nonnull private static String fileRead( @Nonnull String file, @Nullable String encoding )
        throws IOException
    {
        return fileRead( new File( file ), encoding );
    }

    /**
     * Note: the file content is read with platform encoding.
     *
     * @param file the file path
     * @return the file content using the platform encoding
     * @throws IOException if any
     * @deprecated use {@code new String(java.nio.files.Files.readAllBytes(file.toPath()))}
     */
    @Deprecated
    @Nonnull public static String fileRead( @Nonnull File file )
        throws IOException
    {
        return fileRead( file, null );
    }

    /**
     * @param file     the file path
     * @param encoding the wanted encoding
     * @return the file content using the specified encoding
     * @throws IOException if any
     * @deprecated use {@code new String(java.nio.files.Files.readAllBytes(file.toPath()), encoding)}
     */
    @Deprecated
    @Nonnull public static String fileRead( @Nonnull File file, @Nullable String encoding )
        throws IOException
    {
        Charset charset = charset( encoding );

        StringBuilder buf = new StringBuilder();


        try ( Reader reader = Files.newBufferedReader( file.toPath(), charset ) )
        {
            int count;
            char[] b = new char[512];
            while ( ( count = reader.read( b ) ) >= 0 )  // blocking read
            {
                buf.append( b, 0, count );
            }
        }

        return buf.toString();
    }

    /**
     * @param file the file path
     * @return the file content lines as String[] using the system default encoding.
     * An empty List if the file doesn't exist.
     * @throws IOException in case of failure
     * @deprecated use {@code java.nio.files.Files.readAllLines()}
     */
    @Deprecated
    @Nonnull public static String[] fileReadArray( @Nonnull File file )
        throws IOException
    {
        List<String> lines = loadFile( file );

        return lines.toArray( new String[lines.size()] );
    }

    /**
     * Appends data to a file. The file is created if it does not exist.
     * Note: the data is written with platform encoding.
     *
     * @param fileName the path of the file to write
     * @param data     the content to write to the file
     * @throws IOException if any
     * @deprecated use {@code java.nio.files.Files.write(filename, data.getBytes(),
     *     StandardOpenOption.APPEND, StandardOpenOption.CREATE)}
     */
    @Deprecated
    public static void fileAppend( @Nonnull String fileName, @Nonnull String data )
        throws IOException
    {
        fileAppend( fileName, null, data );
    }

    /**
     * Appends data to a file. The file will be created if it does not exist.
     *
     * @param fileName the path of the file to write
     * @param encoding the encoding of the file
     * @param data     the content to write to the file
     * @throws IOException if any
     * @deprecated use {@code java.nio.files.Files.write(filename, data.getBytes(encoding),
     *     StandardOpenOption.APPEND, StandardOpenOption.CREATE)}
     */
    @Deprecated
    public static void fileAppend( @Nonnull String fileName, @Nullable String encoding, @Nonnull String data )
        throws IOException
    {
        Charset charset = charset( encoding );

        try ( OutputStream out = new FileOutputStream( fileName, true ) )
        {
          out.write( data.getBytes( charset ) );
        }
    }

    /**
     * Writes data to a file. The file will be created if it does not exist.
     * Note: the data is written with platform encoding
     *
     * @param fileName the path of the file to write
     * @param data     the content to write to the file
     * @throws IOException if any
     * @deprecated use {@code java.nio.files.Files.write(filename,
     *     data.getBytes(), StandardOpenOption.CREATE)}
     */
    @Deprecated
    public static void fileWrite( @Nonnull String fileName, @Nonnull String data )
        throws IOException
    {
        fileWrite( fileName, null, data );
    }

    /**
     * Writes data to a file. The file will be created if it does not exist.
     *
     * @param fileName the path of the file to write
     * @param encoding the encoding of the file
     * @param data     the content to write to the file
     * @throws IOException if any
     * @deprecated use {@code java.nio.files.Files.write(Paths.get(filename),
     *     data.getBytes(encoding), StandardOpenOption.CREATE)}
     */
    @Deprecated
    public static void fileWrite( @Nonnull String fileName, @Nullable String encoding, @Nonnull String data )
        throws IOException
    {
        File file = new File( fileName );
        fileWrite( file, encoding, data );
    }

    /**
     * Writes data to a file. The file will be created if it does not exist.
     *
     * @param file     the path of the file to write
     * @param encoding the encoding of the file
     * @param data     the content to write to the file
     * @throws IOException if any
     * @deprecated use {@code java.nio.files.Files.write(file.toPath(),
     *     data.getBytes(encoding), StandardOpenOption.CREATE)}
     */
    @Deprecated
    public static void fileWrite( @Nonnull File file, @Nullable String encoding, @Nonnull String data )
        throws IOException
    {
        Charset charset = charset( encoding );

        try ( Writer writer = Files.newBufferedWriter( file.toPath(), charset ) )
        {
            writer.write( data );
        }
    }

    /**
     * Writes String array data to a file in the systems default encoding.
     * The file will be created if it does not exist.
     *
     * @param file the path of the file to write
     * @param data the content to write to the file
     * @throws IOException if any
     * @deprecated use {@code java.nio.files.Files.write(file.toPath(),
     *     data.getBytes(encoding), StandardOpenOption.CREATE)}
     */
    @Deprecated
    public static void fileWriteArray( @Nonnull File file, @Nullable String... data )
        throws IOException
    {
        fileWriteArray( file, null, data );
    }

    /**
     * Writes String array data to a file. The file is created if it does not exist.
     *
     * @param file the path of the file to write
     * @param encoding the encoding of the file
     * @param data the content to write to the file
     * @throws IOException if any
     * @deprecated use {@code java.nio.files.Files.write(file.toPath(),
     *     data.getBytes(encoding), StandardOpenOption.CREATE)}
     */
    @Deprecated
    public static void fileWriteArray( @Nonnull File file, @Nullable String encoding, @Nullable String... data )
        throws IOException
    {
        Charset charset = charset( encoding );

        try ( Writer writer = Files.newBufferedWriter( file.toPath(), charset ) )
        {
            for ( int i = 0; data != null && i < data.length; i++ )
            {
                writer.write( data[i] );
                if ( i < data.length )
                {
                    writer.write( "\n" );
                }
            }
        }
    }

    /**
     * Deletes a file.
     *
     * @param fileName the path of the file to delete
     * @deprecated use {@code Files.delete(Paths.get(fileName))}
     */
    @Deprecated
    public static void fileDelete( @Nonnull String fileName )
    {
        File file = new File( fileName );
        //noinspection ResultOfMethodCallIgnored
        deleteLegacyStyle( file );
    }

    /**
     * Given a directory and an array of extensions return an array of compliant files.
     *
     * <p>The given extensions should be like "java" and not like ".java".</p>
     *
     * @param directory  the path of the directory
     * @param extensions an array of expected extensions
     * @return an array of files for the wanted extensions
     */
    public static String[] getFilesFromExtension( @Nonnull String directory, @Nonnull String... extensions )
    {
        List<String> files = new ArrayList<String>();

        File currentDir = new File( directory );

        String[] unknownFiles = currentDir.list();

        if ( unknownFiles == null )
        {
            return new String[0];
        }

        for ( String unknownFile : unknownFiles )
        {
            String currentFileName = directory + System.getProperty( "file.separator" ) + unknownFile;
            File currentFile = new File( currentFileName );

            if ( currentFile.isDirectory() )
            {
                // ignore all CVS directories...
                if ( currentFile.getName().equals( "CVS" ) )
                {
                    continue;
                }

                // ok... traverse into this directory and get all the files... then combine
                // them with the current list.

                String[] fetchFiles = getFilesFromExtension( currentFileName, extensions );
                files = blendFilesToList( files, fetchFiles );
            }
            else
            {
                // ok... add the file

                String add = currentFile.getAbsolutePath();
                if ( isValidFile( add, extensions ) )
                {
                    files.add( add );
                }
            }
        }

        // ok... move the Vector into the files list...
        String[] foundFiles = new String[files.size()];
        files.toArray( foundFiles );

        return foundFiles;
    }

    /**
     * Private helper method for getFilesFromExtension()
     */
    @Nonnull private static List<String> blendFilesToList( @Nonnull List<String> v, @Nonnull String... files )
    {
        Collections.addAll( v, files );

        return v;
    }

    /**
     * Checks to see if a file is of a particular type(s).
     * Note that if the file does not have an extension, an empty string
     * (&quot;&quot;) is matched for.
     */
    private static boolean isValidFile( @Nonnull String file, @Nonnull String... extensions )
    {
        String extension = extension( file );

        // ok.. now that we have the "extension" go through the current know
        // excepted extensions and determine if this one is OK.

        for ( String extension1 : extensions )
        {
            if ( extension1.equals( extension ) )
            {
                return true;
            }
        }

        return false;

    }

    /**
     * Simple way to make a directory.
     *
     * @param dir the directory to create
     * @throws IllegalArgumentException if the dir contains illegal Windows characters under Windows OS
     * @see #INVALID_CHARACTERS_FOR_WINDOWS_FILE_NAME
     * @deprecated use {@code java.nio.file.Files.createDirectories(Paths.get(dir))}
     */
    @Deprecated
    public static void mkdir( @Nonnull String dir )
    {
        File file = new File( dir );

        if ( Os.isFamily( Os.FAMILY_WINDOWS ) && !isValidWindowsFileName( file ) )
        {
            throw new IllegalArgumentException(
                "The file (" + dir + ") cannot contain any of the following characters: \n" + StringUtils.join(
                    INVALID_CHARACTERS_FOR_WINDOWS_FILE_NAME, " " ) );
        }

        if ( !file.exists() )
        {
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
        }
    }

    /**
     * Compare the contents of two files to determine if they are equal or not.
     *
     * @param file1 the first file
     * @param file2 the second file
     * @return true if the content of the files are equal or they both don't exist, false otherwise
     * @throws IOException if any
     */
    public static boolean contentEquals( @Nonnull final File file1, @Nonnull final File file2 )
        throws IOException
    {
        final boolean file1Exists = file1.exists();
        if ( file1Exists != file2.exists() )
        {
            return false;
        }

        if ( !file1Exists )
        {
            // two not existing files are equal
            return true;
        }

        if ( file1.isDirectory() || file2.isDirectory() )
        {
            // don't want to compare directory contents
            return false;
        }

        try ( InputStream input1 = new FileInputStream( file1 );
            InputStream  input2 = new FileInputStream( file2 ) )
        { 
            return IOUtils.contentEquals( input1, input2 );
        }
    }

    /**
     * Convert from a <code>URL</code> to a <code>File</code>.
     *
     * @param url file URL
     * @return the equivalent <code>File</code> object, or <code>null</code> if the URL's protocol
     *     is not <code>file</code>
     */
    @Nullable public static File toFile( @Nullable final URL url )
    {
        if ( url == null || !url.getProtocol().equalsIgnoreCase( "file" ) )
        {
            return null;
        }

        String filename = url.getFile().replace( '/', File.separatorChar );
        int pos = -1;
        while ( ( pos = filename.indexOf( '%', pos + 1 ) ) >= 0 )
        {
            if ( pos + 2 < filename.length() )
            {
                String hexStr = filename.substring( pos + 1, pos + 3 );
                char ch = (char) Integer.parseInt( hexStr, 16 );
                filename = filename.substring( 0, pos ) + ch + filename.substring( pos + 3 );
            }
        }
        return new File( filename );
    }

    /**
     * Convert the array of Files into a list of URLs.
     *
     * @param files the array of files
     * @return the array of URLs
     * @throws IOException if an error occurs
     */
    @Nonnull public static URL[] toURLs( @Nonnull final File... files )
        throws IOException
    {
        final URL[] urls = new URL[files.length];

        for ( int i = 0; i < urls.length; i++ )
        {
            urls[i] = files[i].toURI().toURL();
        }

        return urls;
    }

    /**
     * Remove extension from a path. E.g.
     * <pre>
     * foo.txt    &rarr; foo
     * a\b\c.jpg  &rarr; a\b\c
     * a\b\c      &rarr; a\b\c
     * </pre>
     *
     * @param filename the path of the file
     * @return the filename minus extension
     * @deprecated use {@code org.apache.commons.io.FilenameUtils.removeExtension()}
     */
    @Deprecated
    @Nonnull public static String removeExtension( @Nonnull final String filename )
    {
        String ext = extension( filename );

        if ( "".equals( ext ) )
        {
            return filename;
        }

        final int index = filename.lastIndexOf( ext ) - 1;
        return filename.substring( 0, index );
    }

    /**
     * Get extension from a path. E.g.
     *
     * <pre>
     * foo.txt    &rarr; "txt"
     * a\b\c.jpg  &rarr; "jpg"
     * a\b\c      &rarr; ""
     * </pre>
     *
     * @param filename the path of the file
     * @return the extension of filename or "" if none
     * @deprecated use {@code org.apache.commons.io.FilenameUtils.getExtension()}
     */
    @Deprecated
    @Nonnull public static String getExtension( @Nonnull final String filename )
    {
        return extension( filename );
    }

    /**
     * Copy file from source to destination. If <code>destinationDirectory</code> does not exist, it
     * (and any parent directories) will be created. If a file <code>source</code> in
     * <code>destinationDirectory</code> exists, it will be overwritten.
     *
     * @param source               an existing <code>File</code> to copy
     * @param destinationDirectory a directory to copy <code>source</code> into
     * @throws java.io.FileNotFoundException if <code>source</code> isn't a normal file
     * @throws IllegalArgumentException      if <code>destinationDirectory</code> isn't a directory
     * @throws IOException                   if <code>source</code> does not exist, the file in
     *                                       <code>destinationDirectory</code> cannot be written to, or an IO error
     *                                       occurs during copying
     * @deprecated use {@code org.apache.commons.io.FileUtils.copyFileToDirectory()}
     */
    @Deprecated
    public static void copyFileToDirectory( @Nonnull final File source, @Nonnull final File destinationDirectory )
        throws IOException
    {
        if ( destinationDirectory.exists() && !destinationDirectory.isDirectory() )
        {
            throw new IOException( "Destination is not a directory" );
        }

        copyFile( source, new File( destinationDirectory, source.getName() ) );
    }

    /**
     * Copy file from source to destination only if source is newer than the target file.
     * If <code>destinationDirectory</code> does not exist, it
     * (and any parent directories) is created. If a file <code>source</code> in
     * <code>destinationDirectory</code> exists, it is overwritten.
     *
     * @param source               an existing <code>File</code> to copy
     * @param destinationDirectory a directory to copy <code>source</code> into
     * @throws java.io.FileNotFoundException if <code>source</code> isn't a normal file
     * @throws IllegalArgumentException      if <code>destinationDirectory</code> isn't a directory
     * @throws IOException                   if <code>source</code> does not exist, the file in
     *                                       <code>destinationDirectory</code> cannot be written to, or an IO error
     *                                       occurs during copying
     */
    private static void copyFileToDirectoryIfModified( @Nonnull final File source,
                                                       @Nonnull final File destinationDirectory )
        throws IOException
    {
        if ( destinationDirectory.exists() && !destinationDirectory.isDirectory() )
        {
            throw new IllegalArgumentException( "Destination is not a directory" );
        }

        copyFileIfModified( source, new File( destinationDirectory, source.getName() ) );
    }


    /**
     * Copy file from source to destination. The directories up to <code>destination</code> will be
     * created if they don't already exist. <code>destination</code> will be overwritten if it
     * already exists.
     *
     * @param source      an existing non-directory <code>File</code> to copy bytes from
     * @param destination a non-directory <code>File</code> to write bytes to (possibly
     *                    overwriting)
     * @throws IOException if <code>source</code> does not exist, <code>destination</code> cannot be
     *                     written to, or an IO error occurs during copying
     * @throws java.io.FileNotFoundException if <code>destination</code> is a directory
     * @deprecated use {@code java.nio.Files.copy(source.toPath(), destination.toPath(), LinkOption.NOFOLLOW_LINKS,
     *     StandardCopyOption.REPLACE_EXISTING)}
     */
    @Deprecated
    public static void copyFile( @Nonnull final File source, @Nonnull final File destination )
        throws IOException
    {
        //check source exists
        if ( !source.exists() )
        {
            final String message = "File " + source + " does not exist";
            throw new IOException( message );
        }
        if ( Files.isSymbolicLink( source.toPath() ) )
        {
            File target = Files.readSymbolicLink( source.toPath() ).toFile();
            createSymbolicLink( destination, target );
            return;
        }

        //check source != destination, see PLXUTILS-10
        if ( source.getCanonicalPath().equals( destination.getCanonicalPath() ) )
        {
            //if they are equal, we can exit the method without doing any work
            return;
        }

        mkdirsFor( destination );

        doCopyFile( source, destination );

        if ( source.length() != destination.length() )
        {
            final String message = "Failed to copy full contents from " + source + " to " + destination;
            throw new IOException( message );
        }
    }

    private static void mkdirsFor( @Nonnull File destination )
    {
        //does destination directory exist ?
        if ( destination.getParentFile() != null && !destination.getParentFile().exists() )
        {
            //noinspection ResultOfMethodCallIgnored
            destination.getParentFile().mkdirs();
        }
    }

    private static void doCopyFile( @Nonnull File source, @Nonnull File destination )
        throws IOException
    {

        try ( FileInputStream fis = new FileInputStream( source );
              FileOutputStream fos = new FileOutputStream( destination );
              FileChannel input = fis.getChannel();
              FileChannel output = fos.getChannel() )
        {
            
            long size = input.size();
            long pos = 0;
            long count;
            while ( pos < size )
            {
                count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
                pos += output.transferFrom( input, pos, count );
            }
        }

        copyFilePermissions( source, destination );
    }

    /**
     * Copy file from source to destination only if source timestamp is later than the destination timestamp.
     * The directories up to <code>destination</code> will be created if they don't already exist.
     * <code>destination</code> will be overwritten if it already exists.
     *
     * @param source      An existing non-directory <code>File</code> to copy bytes from.
     * @param destination A non-directory <code>File</code> to write bytes to (possibly
     *                    overwriting).
     * @return true if no problem occurred
     * @throws IOException if <code>source</code> does not exist, <code>destination</code> cannot be
     *                     written to, or an IO error occurs during copying.
     */
    private static boolean copyFileIfModified( @Nonnull final File source, @Nonnull final File destination )
        throws IOException
    {
        if ( destination.lastModified() < source.lastModified() )
        {
            copyFile( source, destination );

            return true;
        }

        return false;
    }

    /**
     * Copies bytes from the URL <code>source</code> to a file <code>destination</code>.
     * The directories up to <code>destination</code> will be created if they don't already exist.
     * <code>destination</code> will be overwritten if it already exists.
     *
     * @param source      a <code>URL</code> to copy bytes from
     * @param destination a non-directory <code>File</code> to write bytes to (possibly
     *                    overwriting)
     * @throws IOException if
     *                     <ul>
     *                     <li><code>source</code> URL cannot be opened</li>
     *                     <li><code>destination</code> cannot be written to</li>
     *                     <li>an IO error occurs during copying</li>
     *                     </ul>
     * @deprecated use {@code java.nio.Files.copy(source.openStream(), destination.toPath(),
     *     StandardCopyOption.REPLACE_EXISTING)}
     */
    public static void copyURLToFile( @Nonnull final URL source, @Nonnull final File destination )
        throws IOException
    {
        copyStreamToFile( source.openStream(), destination );
    }

    /**
     * Copies bytes from the {@link InputStream} <code>source</code> to a file <code>destination</code>.
     * The directories up to <code>destination</code> will be created if they don't already exist.
     * <code>destination</code> will be overwritten if it already exists.
     *
     * @param source      an {@link InputStream} to copy bytes from. This stream is
     *                    guaranteed to be closed.
     * @param destination a non-directory <code>File</code> to write bytes to (possibly
     *                    overwriting)
     * @throws IOException if
     *                     <ul>
     *                     <li><code>source</code> cannot be opened</li>
     *                     <li><code>destination</code> cannot be written to</li>
     *                     <li>an I/O error occurs during copying</li>
     *                     </ul>
     * @deprecated use {@code java.nio.Files.copy(source, destination.toPath(),
     *     StandardCopyOption.REPLACE_EXISTING)}
     */
    @Deprecated
    private static void copyStreamToFile( @Nonnull @WillClose final InputStream source,
                                          @Nonnull final File destination )
        throws IOException
    {
        // does destination directory exist ?
        if ( destination.getParentFile() != null && !destination.getParentFile().exists() )
        {
            // noinspection ResultOfMethodCallIgnored
            destination.getParentFile().mkdirs();
        }

        // make sure we can write to destination
        if ( destination.exists() && !destination.canWrite() )
        {
            final String message = "Unable to open file " + destination + " for writing.";
            throw new IOException( message );
        }

        try ( OutputStream out = new FileOutputStream( destination ); InputStream in = source )
        {
            IOUtil.copy( in, out );
        }
    }

    /**
     * Normalize a path.
     * Eliminates "/../" and "/./" in a string. Returns <code>null</code> if the ..'s went past the
     * root.
     * Eg:
     * <pre>
     * /foo//               &rarr;     /foo/
     * /foo/./              &rarr;     /foo/
     * /foo/../bar          &rarr;     /bar
     * /foo/../bar/         &rarr;     /bar/
     * /foo/../bar/../baz   &rarr;     /baz
     * //foo//./bar         &rarr;     /foo/bar
     * /../                 &rarr;     null
     * </pre>
     *
     * @param path the path to normalize
     * @return the normalized String, or <code>null</code> if too many ..'s
     * @deprecated use {@code org.apache.commons.io.FileNameUtils.normalize()}
     */
    @Deprecated
    @Nonnull public static String normalize( @Nonnull final String path )
    {
        String normalized = path;
        // Resolve occurrences of "//" in the normalized path
        while ( true )
        {
            int index = normalized.indexOf( "//" );
            if ( index < 0 )
            {
                break;
            }
            normalized = normalized.substring( 0, index ) + normalized.substring( index + 1 );
        }

        // Resolve occurrences of "/./" in the normalized path
        while ( true )
        {
            int index = normalized.indexOf( "/./" );
            if ( index < 0 )
            {
                break;
            }
            normalized = normalized.substring( 0, index ) + normalized.substring( index + 2 );
        }

        // Resolve occurrences of "/../" in the normalized path
        while ( true )
        {
            int index = normalized.indexOf( "/../" );
            if ( index < 0 )
            {
                break;
            }
            if ( index == 0 )
            {
                return null;  // Trying to go outside our context
            }
            int index2 = normalized.lastIndexOf( '/', index - 1 );
            normalized = normalized.substring( 0, index2 ) + normalized.substring( index + 3 );
        }

        // Return the normalized path that we have completed
        return normalized;
    }

    /**
     * Resolve a file <code>filename</code> to its canonical form. If <code>filename</code> is
     * relative (doesn't start with <code>/</code>), it is resolved relative to
     * <code>baseFile</code>. Otherwise it is treated as a normal root-relative path.
     *
     * @param baseFile where to resolve <code>filename</code> from, if <code>filename</code> is relative
     * @param filename absolute or relative file path to resolve
     * @return the canonical <code>File</code> of <code>filename</code>
     */
    @Nonnull public static File resolveFile( final File baseFile, @Nonnull String filename )
    {
        String filenm = filename;
        if ( '/' != File.separatorChar )
        {
            filenm = filename.replace( '/', File.separatorChar );
        }

        if ( '\\' != File.separatorChar )
        {
            filenm = filename.replace( '\\', File.separatorChar );
        }

        // deal with absolute files
        if ( filenm.startsWith( File.separator ) || ( Os.isFamily( Os.FAMILY_WINDOWS ) && filenm.indexOf( ":" ) > 0 ) )
        {
            File file = new File( filenm );

            try
            {
                file = file.getCanonicalFile();
            }
            catch ( final IOException ioe )
            {
                // nop
            }

            return file;
        }
        // FIXME: I'm almost certain this // removal is unnecessary, as getAbsoluteFile() strips
        // them. However, I'm not sure about this UNC stuff. (JT)
        final char[] chars = filename.toCharArray();
        final StringBuilder sb = new StringBuilder();

        //remove duplicate file separators in succession - except
        //on win32 at start of filename as UNC filenames can
        //be \\AComputer\AShare\myfile.txt
        int start = 0;
        if ( '\\' == File.separatorChar )
        {
            sb.append( filenm.charAt( 0 ) );
            start++;
        }

        for ( int i = start; i < chars.length; i++ )
        {
            final boolean doubleSeparator = File.separatorChar == chars[i] && File.separatorChar == chars[i - 1];

            if ( !doubleSeparator )
            {
                sb.append( chars[i] );
            }
        }

        filenm = sb.toString();

        //must be relative
        File file = ( new File( baseFile, filenm ) ).getAbsoluteFile();

        try
        {
            file = file.getCanonicalFile();
        }
        catch ( final IOException ioe )
        {
            // nop
        }

        return file;
    }

    /**
     * Delete a file. If file is directory, delete it and all sub-directories.
     *
     * @param file the file path
     * @throws IOException if any
     * @deprecated use {@code org.apache.commons.io.FileUtils.deleteQuietly()}
     */
    @Deprecated
    public static void forceDelete( @Nonnull final String file )
        throws IOException
    {
        forceDelete( new File( file ) );
    }

    /**
     * Delete a file. If file is directory, delete it and all sub-directories.
     *
     * @param file a file
     * @throws IOException if any
     * @deprecated use {@code org.apache.commons.io.FileUtils.deleteQuietly()}
     */
    @Deprecated
    public static void forceDelete( @Nonnull final File file )
        throws IOException
    {
        if ( file.isDirectory() )
        {
            deleteDirectory( file );
        }
        else
        {
            /*
             * NOTE: Always try to delete the file even if it appears to be non-existent. This will ensure that a
             * symlink whose target does not exist is deleted, too.
             */
            boolean filePresent = file.getCanonicalFile().exists();
            if ( !deleteFile( file ) && filePresent )
            {
                final String message = "File " + file + " unable to be deleted.";
                throw new IOException( message );
            }
        }
    }

    /**
     * Deletes a file.
     *
     * @param file the file to delete
     * @throws IOException if the file cannot be deleted
     * @deprecated use {@code java.nio.files.Files.delete(file.toPath())}
     */
    @Deprecated
    public static void delete( @Nonnull File file )
        throws IOException
    {
        Files.delete( file.toPath() );
    }

    /**
     * @param file the file
     * @return true / false
     * @deprecated use {@code java.nio.files.Files.delete(file.toPath())}
     */
    @Deprecated
    public static boolean deleteLegacyStyle( @Nonnull File file )
    {
        try
        {
            Files.delete( file.toPath() );
            return true;
        }
        catch ( IOException e )
        {
            return false;
        }
    }

    /**
     * Accommodate Windows bug encountered in both Sun and IBM JDKs.
     * Others possible. If the delete does not work, call System.gc(),
     * wait a little and try again.
     *
     * @param file a file
     * @throws IOException if any
     */
    private static boolean deleteFile( @Nonnull File file )
        throws IOException
    {
        if ( file.isDirectory() )
        {
            throw new IOException( "File " + file + " isn't a file." );
        }

        if ( !deleteLegacyStyle( file ) )
        {
            if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
            {
                file = file.getCanonicalFile();
            }

            try
            {
                Thread.sleep( 10 );
                return deleteLegacyStyle( file );
            }
            catch ( InterruptedException ex )
            {
                return deleteLegacyStyle( file );
            }
        }

        return true;
    }


    /**
     * Make a directory.
     *
     * @param file not null
     * @throws IOException if a file already exists with the specified name or the directory is unable to be created
     * @throws IllegalArgumentException if the file contains illegal Windows characters under Windows OS.
     * @see #INVALID_CHARACTERS_FOR_WINDOWS_FILE_NAME
     */
    public static void forceMkdir( @Nonnull final File file )
        throws IOException
    {
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) && !isValidWindowsFileName( file ) )
        {
            throw new IllegalArgumentException(
                "The file (" + file.getAbsolutePath() + ") cannot contain any of the following characters: \n"
                    + StringUtils.join( INVALID_CHARACTERS_FOR_WINDOWS_FILE_NAME, " " ) );
        }

        if ( file.exists() )
        {
            if ( file.isFile() )
            {
                final String message =
                    "File " + file + " exists and is " + "not a directory. Unable to create directory.";
                throw new IOException( message );
            }
        }
        else
        {
            if ( !file.mkdirs() )
            {
                final String message = "Unable to create directory " + file;
                throw new IOException( message );
            }
        }
    }

    /**
     * Recursively delete a directory.
     *
     * @param directory a directory
     * @throws IOException if any
     * @deprecated use {@code org.apache.commons.io.FileUtils.deleteDirectory()}
     */
    @Deprecated
    public static void deleteDirectory( @Nonnull final String directory )
        throws IOException
    {
        deleteDirectory( new File( directory ) );
    }

    /**
     * Recursively delete a directory.
     *
     * @param directory a directory
     * @throws IOException if any
     * @deprecated use {@code org.apache.commons.io.FileUtils.deleteDirectory()}
     */
    @Deprecated
    public static void deleteDirectory( @Nonnull final File directory )
        throws IOException
    {
        if ( !directory.exists() )
        {
            return;
        }

        /* try delete the directory before its contents, which will take
         * care of any directories that are really symbolic links.
         */
        if ( deleteLegacyStyle( directory ) )
        {
            return;
        }

        cleanDirectory( directory );
        if ( !deleteLegacyStyle( directory ) )
        {
            final String message = "Directory " + directory + " unable to be deleted.";
            throw new IOException( message );
        }
    }

    /**
     * Remove all files from a directory without deleting it.
     *
     * @param directory a directory
     * @throws IOException if any. This can leave cleaning in a half-finished state where
     *         some but not all files have been deleted.
     * @deprecated use {@code org.apache.commons.io.FileUtils.cleanDirectory()}
     */
    @Deprecated
    public static void cleanDirectory( @Nonnull final File directory )
        throws IOException
    {
        if ( !directory.exists() )
        {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException( message );
        }

        if ( !directory.isDirectory() )
        {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException( message );
        }

        IOException exception = null;

        final File[] files = directory.listFiles();

        if ( files == null )
        {
            return;
        }

        for ( final File file : files )
        {
            try
            {
                forceDelete( file );
            }
            catch ( final IOException ioe )
            {
                exception = ioe;
            }
        }

        if ( null != exception )
        {
            throw exception;
        }
    }

    /**
     * Recursively count size of a directory.
     *
     * @param directory a directory
     * @return size of directory in bytes
     * @deprecated use {@code org.apache.commons.io.FileUtils.sizeOf()}
     */
    @Deprecated
    public static long sizeOfDirectory( @Nonnull final String directory )
    {
        return sizeOfDirectory( new File( directory ) );
    }

    /**
     * Recursively count size of a directory.
     *
     * @param directory a directory
     * @return size of directory in bytes
     * @deprecated use {@code org.apache.commons.io.FileUtils.sizeOf()}
     */
    @Deprecated
    public static long sizeOfDirectory( @Nonnull final File directory )
    {
        if ( !directory.exists() )
        {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException( message );
        }

        if ( !directory.isDirectory() )
        {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException( message );
        }

        long size = 0;

        final File[] files = directory.listFiles();
        if ( files == null )
        {
            throw new IllegalArgumentException( "Problems reading directory" );
        }

        for ( final File file : files )
        {
            if ( file.isDirectory() )
            {
                size += sizeOfDirectory( file );
            }
            else
            {
                size += file.length();
            }
        }

        return size;
    }

    /**
     * Return the files contained in the directory, using inclusion and exclusion Ant patterns,
     * including the directory name in each of the files
     *
     * @param directory the directory to scan
     * @param includes  the Ant includes pattern, comma separated
     * @param excludes  the Ant excludes pattern, comma separated
     * @return a list of File objects
     * @throws IOException in case of failure.
     * @see #getFileNames(File, String, String, boolean)
     */
    @Nonnull
    public static List<File> getFiles( @Nonnull File directory, @Nullable String includes, @Nullable String excludes )
        throws IOException
    {
        return getFiles( directory, includes, excludes, true );
    }

    /**
     * Return the files contained in the directory, using inclusion and exclusion Ant patterns
     *
     * @param directory      the directory to scan
     * @param includes       the includes pattern, comma separated
     * @param excludes       the excludes pattern, comma separated
     * @param includeBasedir true to include the base dir in each file
     * @return a list of File objects
     * @throws IOException in case of failure.
     * @see #getFileNames(File, String, String, boolean)
     */
    @Nonnull
    public static List<File> getFiles( @Nonnull File directory, @Nullable String includes, @Nullable String excludes,
                                       boolean includeBasedir )
        throws IOException
    {
        List<String> fileNames = getFileNames( directory, includes, excludes, includeBasedir );

        List<File> files = new ArrayList<File>();

        for ( String filename : fileNames )
        {
            files.add( new File( filename ) );
        }

        return files;
    }

    /**
     * Return a list of files as String depending options.
     * This method use case sensitive file name.
     *
     * @param directory      the directory to scan
     * @param includes       the Ant includes pattern, comma separated
     * @param excludes       the Ant excludes pattern, comma separated
     * @param includeBasedir true to include the base directory in each String of file
     * @return a list of file names
     * @throws IOException in case of failure
     */
    @Nonnull public static List<String> getFileNames( @Nonnull File directory, @Nullable String includes,
                                                      @Nullable String excludes, boolean includeBasedir )
        throws IOException
    {
        return getFileNames( directory, includes, excludes, includeBasedir, true );
    }

    /**
     * Return a list of files as String depending options.
     *
     * @param directory       the directory to scan
     * @param includes        the Ant includes pattern, comma separated
     * @param excludes        the Ant excludes pattern, comma separated
     * @param includeBasedir  true to include the base dir in each String of file
     * @param isCaseSensitive true if case sensitive
     * @return a list of files as String
     * @throws IOException
     */
    @Nonnull private static List<String> getFileNames( @Nonnull File directory, @Nullable String includes,
                                                       @Nullable String excludes, boolean includeBasedir,
                                                       boolean isCaseSensitive )
        throws IOException
    {
        return getFileAndDirectoryNames( directory, includes, excludes, includeBasedir, isCaseSensitive, true, false );
    }

    /**
     * Return a list of directories as String depending options.
     * This method use case sensitive file name.
     *
     * @param directory      the directory to scan
     * @param includes       the Ant includes pattern, comma separated
     * @param excludes       the Ant excludes pattern, comma separated
     * @param includeBasedir true to include the base dir in each String of file
     * @return a list of directories as String
     * @throws IOException in case of failure.
     */
    @Nonnull public static List<String> getDirectoryNames( @Nonnull File directory, @Nullable String includes,
                                                           @Nullable String excludes, boolean includeBasedir )
        throws IOException
    {
        return getDirectoryNames( directory, includes, excludes, includeBasedir, true );
    }

    /**
     * Return a list of directories as Strings.
     *
     * @param directory       the directory to scan
     * @param includes        the Ant includes pattern, comma separated
     * @param excludes        the Ant excludes pattern, comma separated
     * @param includeBasedir  true to include the base directory in each String of file
     * @param isCaseSensitive true if case sensitive
     * @return a list of directories as String
     * @throws IOException in case of failure
     */
    @Nonnull public static List<String> getDirectoryNames( @Nonnull File directory, @Nullable String includes,
                                                           @Nullable String excludes, boolean includeBasedir,
                                                           boolean isCaseSensitive )
        throws IOException
    {
        return getFileAndDirectoryNames( directory, includes, excludes, includeBasedir, isCaseSensitive, false, true );
    }

    /**
     * Return a list of file names as Strings.
     *
     * @param directory       the directory to scan
     * @param includes        the Ant includes pattern, comma separated
     * @param excludes        the Ant excludes pattern, comma separated
     * @param includeBasedir  true to include the base directory in each String of file
     * @param isCaseSensitive true if case sensitive
     * @param getFiles        true to include regular files
     * @param getDirectories  true to include directories
     * @return a list of file names
     */
    @Nonnull public static List<String> getFileAndDirectoryNames( File directory, @Nullable String includes,
                                                                  @Nullable String excludes, boolean includeBasedir,
                                                                  boolean isCaseSensitive, boolean getFiles,
                                                                  boolean getDirectories )
    {
        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir( directory );

        if ( includes != null )
        {
            scanner.setIncludes( StringUtils.split( includes, "," ) );
        }

        if ( excludes != null )
        {
            scanner.setExcludes( StringUtils.split( excludes, "," ) );
        }

        scanner.setCaseSensitive( isCaseSensitive );

        scanner.scan();

        List<String> list = new ArrayList<String>();

        if ( getFiles )
        {
            String[] files = scanner.getIncludedFiles();

            for ( String file : files )
            {
                if ( includeBasedir )
                {
                    list.add( directory + FileUtils.FS + file );
                }
                else
                {
                    list.add( file );
                }
            }
        }

        if ( getDirectories )
        {
            String[] directories = scanner.getIncludedDirectories();

            for ( String directory1 : directories )
            {
                if ( includeBasedir )
                {
                    list.add( directory + FileUtils.FS + directory1 );
                }
                else
                {
                    list.add( directory1 );
                }
            }
        }

        return list;
    }

    /**
     * Copy the contents of a directory into another one.
     *
     * @param sourceDirectory the source directory. If the source does not exist,
     *     the method simply returns.
     * @param destinationDirectory the target directory; will be created if it doesn't exist
     * @throws IOException if any
     * @deprecated use {@code org.apache.commons.io.FileUtils.copyDirectory()}
     */
    @Deprecated
    public static void copyDirectory( @Nonnull File sourceDirectory, @Nonnull File destinationDirectory )
        throws IOException
    {
        Objects.requireNonNull( sourceDirectory );
        Objects.requireNonNull( destinationDirectory );
        if ( destinationDirectory.equals( sourceDirectory ) )
        {
            throw new IOException( "Can't copy directory " + sourceDirectory + " to itself." );
        }
        else if ( !destinationDirectory.exists() )
        {
            if ( !destinationDirectory.mkdirs() )
            {
                throw new IOException( "Can't create directory " + destinationDirectory );
            }
        }
        copyDirectoryStructure( sourceDirectory, destinationDirectory );
    }

    /**
     * Copy the contents of a directory into another one.
     *
     * @param sourceDirectory      the source directory
     * @param destinationDirectory the target directory
     * @param includes             Ant include pattern
     * @param excludes             Ant exclude pattern
     * @throws IOException if the source is a file or cannot be copied
     * @see #getFiles(File, String, String)
     * @deprecated use {@code org.apache.commons.io.FileUtils.copyDirectory()}
     */
    @Deprecated
    public static void copyDirectory( @Nonnull File sourceDirectory, @Nonnull File destinationDirectory,
                                      @Nullable String includes, @Nullable String excludes )
        throws IOException
    {
        if ( !sourceDirectory.exists() )
        {
            return;
        }
        else if ( !sourceDirectory.isDirectory() )
        {
            throw new IOException( sourceDirectory + " is not a directory." );
        }

        List<File> files = getFiles( sourceDirectory, includes, excludes );

        for ( File file : files )
        {
            copyFileToDirectory( file, destinationDirectory );
        }
    }

    /**
     * Copies an entire directory structure.
     * <p>Note:</p>
     * <ul>
     * <li>It will include empty directories.
     * <li>The <code>sourceDirectory</code> must exist.
     * </ul>
     *
     * @param sourceDirectory the existing directory to be copied
     * @param destinationDirectory the new directory to be created
     * @throws IOException if any
     * @deprecated use {@code org.apache.commons.io.FileUtils.copyDirectory()}
     */
    @Deprecated
    public static void copyDirectoryStructure( @Nonnull File sourceDirectory, @Nonnull File destinationDirectory )
        throws IOException
    {
        copyDirectoryStructure( sourceDirectory, destinationDirectory, destinationDirectory, false );
    }

    private static void copyDirectoryStructure( @Nonnull File sourceDirectory, @Nonnull File destinationDirectory,
                                                File rootDestinationDirectory, boolean onlyModifiedFiles )
        throws IOException
    {
        //noinspection ConstantConditions
        if ( sourceDirectory == null )
        {
            throw new IOException( "source directory can't be null." );
        }

        //noinspection ConstantConditions
        if ( destinationDirectory == null )
        {
            throw new IOException( "destination directory can't be null." );
        }

        if ( sourceDirectory.equals( destinationDirectory ) )
        {
            throw new IOException( "source and destination are the same directory." );
        }

        if ( !sourceDirectory.exists() )
        {
            throw new IOException( "Source directory doesn't exist (" + sourceDirectory.getAbsolutePath() + ")." );
        }

        File[] files = sourceDirectory.listFiles();

        if ( files == null )
        {
            return;
        }

        String sourcePath = sourceDirectory.getAbsolutePath();

        for ( File file : files )
        {
            if ( file.equals( rootDestinationDirectory ) )
            {
                // We don't copy the destination directory in itself
                continue;
            }

            String dest = file.getAbsolutePath();

            dest = dest.substring( sourcePath.length() + 1 );

            File destination = new File( destinationDirectory, dest );

            if ( file.isFile() )
            {
                destination = destination.getParentFile();

                if ( onlyModifiedFiles )
                {
                    copyFileToDirectoryIfModified( file, destination );
                }
                else
                {
                    copyFileToDirectory( file, destination );
                }
            }
            else if ( file.isDirectory() )
            {
                if ( !destination.exists() && !destination.mkdirs() )
                {
                    throw new IOException(
                        "Could not create destination directory '" + destination.getAbsolutePath() + "'." );
                }

                copyDirectoryStructure( file, destination, rootDestinationDirectory, onlyModifiedFiles );
            }
            else
            {
                throw new IOException( "Unknown file type: " + file.getAbsolutePath() );
            }
        }
    }

    /**
     * Renames a file, even if that involves crossing file system boundaries.
     * <p>This will remove <code>to</code> (if it exists), ensure that
     * <code>to</code>'s parent directory exists and move
     * <code>from</code>, which involves deleting <code>from</code> as
     * well.</p>
     *
     * @param from the file to move
     * @param to   the new file name
     * @throws IOException if anything bad happens during this process.
     *                     Note that <code>to</code> may have been deleted already when this happens.
     * @deprecated use {@code java.nio.Files.move()}
     */
    @Deprecated
    public static void rename( @Nonnull File from, @Nonnull File to )
        throws IOException
    {
        if ( to.exists() && !deleteLegacyStyle( to ) )
        {
            throw new IOException( "Failed to delete " + to + " while trying to rename " + from );
        }

        File parent = to.getParentFile();
        if ( parent != null && !parent.exists() && !parent.mkdirs() )
        {
            throw new IOException( "Failed to create directory " + parent + " while trying to rename " + from );
        }

        if ( !from.renameTo( to ) )
        {
            copyFile( from, to );
            if ( !deleteLegacyStyle( from ) )
            {
                throw new IOException( "Failed to delete " + from + " while trying to rename it." );
            }
        }
    }

    /**
     * <p>Create a temporary file in a given directory.</p>
     * <p>The file denoted by the returned abstract pathname did not
     * exist before this method was invoked, any subsequent invocation
     * of this method will yield a different file name.</p>
     * <p>
     * The filename is prefixNNNNNsuffix where NNNN is a random number
     * </p>
     * <p>This method is different to {@link File#createTempFile(String, String, File)}
     * as it doesn't create the file itself.
     * It uses the location pointed to by java.io.tmpdir
     * when the parentDir attribute is null.</p>
     * <p>To automatically delete the file created by this method, use the
     * {@link File#deleteOnExit()} method.</p>
     *
     * @param prefix    prefix before the random number
     * @param suffix    file extension; include the '.'
     * @param parentDir directory to create the temporary file in <code>-java.io.tmpdir</code>
     *                  used if not specified
     * @return a File reference to the new temporary file.
     * @deprecated use {@code java.nio.Files.createTempFile()}
     */
    @Deprecated
    public static File createTempFile( @Nonnull String prefix, @Nonnull String suffix, @Nullable File parentDir )
    {
        File result;
        String parent = System.getProperty( "java.io.tmpdir" );
        if ( parentDir != null )
        {
            parent = parentDir.getPath();
        }
        DecimalFormat fmt = new DecimalFormat( "#####" );
        SecureRandom secureRandom = new SecureRandom();
        long secureInitializer = secureRandom.nextLong();
        Random rand = new Random( secureInitializer + Runtime.getRuntime().freeMemory() );
        do
        {
            result = new File( parent, prefix + fmt.format( positiveRandom( rand ) ) + suffix );
        }
        while ( result.exists() );

        return result;
    }

    private static int positiveRandom( Random rand )
    {
        int a = rand.nextInt();
        while ( a == Integer.MIN_VALUE )
        {
            a = rand.nextInt();
        }
        return Math.abs( a );
    }

    /**
     * <b>If wrappers is null or empty, the file will be copied only if to.lastModified() &lt; from.lastModified()</b>
     *
     * @param from     the file to copy
     * @param to       the destination file
     * @param encoding the file output encoding (only if wrappers is not empty)
     * @param wrappers array of {@link FilterWrapper}
     * @throws IOException if an IO error occurs during copying or filtering
     */
    public static void copyFile( @Nonnull File from, @Nonnull File to, @Nullable String encoding,
                                 @Nullable FilterWrapper... wrappers )
        throws IOException
    {
        copyFile( from, to, encoding, wrappers, false );
    }

    /**
     * Wrapper class for Filter.
     */
    public abstract static class FilterWrapper
    {
        /**
         * @param fileReader {@link Reader}
         * @return the Reader instance
         */
        public abstract Reader getReader( Reader fileReader );
    }

    /**
     * <b>If wrappers is null or empty, the file will be copy only if to.lastModified() &lt; from.lastModified() or if
     * overwrite is true</b>
     *
     * @param from the file to copy
     * @param to the destination file
     * @param encoding the file output encoding (only if wrappers is not empty)
     * @param wrappers array of {@link FilterWrapper}
     * @param overwrite if true and wrappers is null or empty, the file will be copied even if
     *         to.lastModified() &lt; from.lastModified()
     * @throws IOException if an IO error occurs during copying or filtering
     */
    public static void copyFile( @Nonnull File from, @Nonnull File to, @Nullable String encoding,
                                 @Nullable FilterWrapper[] wrappers, boolean overwrite )
        throws IOException
    {
        if ( wrappers == null || wrappers.length == 0 )
        {
            if ( overwrite || to.lastModified() < from.lastModified() )
            {
                copyFile( from, to );
            }
        }
        else
        {
            Charset charset = charset( encoding );

            // buffer so it isn't reading a byte at a time!
            try ( Reader fileReader = Files.newBufferedReader( from.toPath(), charset ) )
            {
                Reader wrapped = fileReader;
                for ( FilterWrapper wrapper : wrappers )
                {
                    wrapped = wrapper.getReader( wrapped );
                }

                if ( overwrite || !to.exists() )
                {
                    try ( Writer fileWriter = Files.newBufferedWriter( to.toPath(), charset ) )
                    {
                        IOUtil.copy( wrapped, fileWriter );
                    }
                }
                else
                {
                    CharsetEncoder encoder = charset.newEncoder();

                    int totalBufferSize = FILE_COPY_BUFFER_SIZE;

                    int charBufferSize = ( int ) Math.floor( totalBufferSize / ( 2 + 2 * encoder.maxBytesPerChar() ) );
                    int byteBufferSize = ( int ) Math.ceil( charBufferSize * encoder.maxBytesPerChar() );

                    CharBuffer newChars = CharBuffer.allocate( charBufferSize );
                    ByteBuffer newBytes = ByteBuffer.allocate( byteBufferSize );
                    ByteBuffer existingBytes = ByteBuffer.allocate( byteBufferSize );

                    CoderResult coderResult;
                    int existingRead;
                    boolean writing = false;

                    try ( final RandomAccessFile existing = new RandomAccessFile( to, "rw" ) )
                    {
                        int n;
                        while ( -1 != ( n = wrapped.read( newChars ) ) )
                        {
                            ( ( Buffer ) newChars ).flip();

                            coderResult = encoder.encode( newChars, newBytes, n != 0 );
                            if ( coderResult.isError() )
                            {
                                coderResult.throwException();
                            }

                            ( ( Buffer ) newBytes ).flip();

                            if ( !writing )
                            {
                                existingRead = existing.read( existingBytes.array(), 0, newBytes.remaining() );
                                ( ( Buffer ) existingBytes ).position( existingRead );
                                ( ( Buffer ) existingBytes ).flip();

                                if ( newBytes.compareTo( existingBytes ) != 0 )
                                {
                                    writing = true;
                                    if ( existingRead > 0 )
                                    {
                                        existing.seek( existing.getFilePointer() - existingRead );
                                    }
                                }
                            }

                            if ( writing )
                            {
                                existing.write( newBytes.array(), 0, newBytes.remaining() );
                            }

                            ( ( Buffer ) newChars ).clear();
                            ( ( Buffer ) newBytes ).clear();
                            ( ( Buffer ) existingBytes ).clear();
                        }

                        if ( existing.length() > existing.getFilePointer() )
                        {
                            existing.setLength( existing.getFilePointer() );
                        }
                    }
                }
            }
        }

        copyFilePermissions( from, to );
    }

    /**
     * Attempts to copy file permissions from the source to the destination file.
     * Initially attempts to copy posix file permissions, assuming that the files are both on posix filesystems.
     * If the initial attempts fail then a second attempt using less precise permissions model.
     * Note that permissions are copied on a best-efforts basis,
     * failure to copy permissions will not result in an exception.
     *
     * @param source the file to copy permissions from.
     * @param destination the file to copy permissions to.
     */
    private static void copyFilePermissions( @Nonnull File source, @Nonnull File destination )
        throws IOException
    {
        try
        {
            // attempt to copy posix file permissions
            Files.setPosixFilePermissions(
                destination.toPath(),
                Files.getPosixFilePermissions( source.toPath() )
            );
        }
        catch ( UnsupportedOperationException e )
        {
            // fallback to setting partial permissions
            destination.setExecutable( source.canExecute() );
            destination.setReadable( source.canRead() );
            destination.setWritable( source.canWrite() );
        }
    }

    /**
     * Note: the file content is read with platform encoding.
     *
     * @param file the file
     * @return a List containing every every line not starting with # and not empty
     * @throws IOException if any
     * @deprecated assumes the platform default character set
     */
    @Deprecated
    @Nonnull public static List<String> loadFile( @Nonnull File file )
        throws IOException
    {
        List<String> lines = new ArrayList<String>();

        if ( file.exists() )
        {
            try ( BufferedReader reader = Files.newBufferedReader( file.toPath(), Charset.defaultCharset() ) )
            {
                for ( String line = reader.readLine(); line != null; line = reader.readLine() )
                {
                    line = line.trim();
                    if ( !line.startsWith( "#" ) && line.length() != 0 )
                    {
                        lines.add( line );
                    }
                }
            }
        }

        return lines;

    }

    /**
     * Returns the named charset or the default charset.
     * @param encoding the name or alias of the charset, null or empty
     * @return A charset object for the named or default charset.
     */
    private static Charset charset( String encoding )
    {
        if ( encoding == null || encoding.isEmpty() )
        {
            return Charset.defaultCharset();
        }
        else
        {
            return Charset.forName( encoding );
        }
    }

    /**
     * For Windows OS, check if the file name contains any of the following characters:
     * <code>":", "*", "?", "\"", "<", ">", "|"</code>
     *
     * @param f not null file
     * @return <code>false</code> if the file path contains any of forbidden Windows characters,
     * <code>true</code> if the Os is not Windows or if the file path respect the Windows constraints.
     * @see #INVALID_CHARACTERS_FOR_WINDOWS_FILE_NAME
     */
    private static boolean isValidWindowsFileName( @Nonnull File f )
    {
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            if ( StringUtils.indexOfAny( f.getName(), INVALID_CHARACTERS_FOR_WINDOWS_FILE_NAME ) != -1 )
            {
                return false;
            }

            if ( f.getParentFile() != null )
            {
                return isValidWindowsFileName( f.getParentFile() );
            }
        }

        return true;
    }

    /**
     * Checks whether a given file is a symbolic link.
     *
     * @param file the file to check
     * @throws IOException in case of failure.
     * @return true if symbolic link false otherwise.
     * @deprecated use {@code java.nio.file.Files.isSymbolicLink(file.toPath())}
     */
    @Deprecated
    public static boolean isSymbolicLink( @Nonnull final File file )
        throws IOException
    {
        return Files.isSymbolicLink( file.toPath() );
    }

    /**
     * Checks whether a given file is a symbolic link.
     *
     * @param file the file to check
     * @return true if and only if we reliably can say this is a symlink
     *
     * @throws IOException in case of failure
     * @deprecated use {@code java.nio.file.Files.isSymbolicLink(file.toPath())}
     */
    @Deprecated
    public static boolean isSymbolicLinkForSure( @Nonnull final File file )
        throws IOException
    {
        return Files.isSymbolicLink( file.toPath() );
    }

    /**
     * Create a new symbolic link, possibly replacing an existing symbolic link.
     * 
     * @param symlink the link name
     * @param target the target
     * @return the linked file
     * @throws IOException in case of an error
     * @see Files#createSymbolicLink(Path, Path, FileAttribute[]) which creates a new symbolic link but does
     * not replace existing symbolic links
     */
    @Nonnull public static File createSymbolicLink( @Nonnull File symlink,  @Nonnull File target )
            throws IOException
    {
        final Path symlinkPath = symlink.toPath();

        if ( Files.exists( symlinkPath ) )
        {
            if ( target.equals( Files.readSymbolicLink( symlinkPath ).toFile() ) )
            {
                return symlink;
            }

            Files.delete( symlinkPath );
        }

        return Files.createSymbolicLink( symlinkPath, target.toPath() ).toFile();
    }
}
