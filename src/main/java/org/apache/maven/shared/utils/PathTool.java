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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.StringTokenizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * <p>Path tool contains static methods to assist in determining path-related
 * information such as relative paths.</p>
 * <p>
 * This class originally got developed at Apache Anakia and later maintained
 * in maven-utils of Apache Maven-1.
 * Some external fixes by Apache Committers have been applied later.
 * </p>
 */
public class PathTool
{
    
    /**
     * The constructor.
     *
     * @deprecated This is a utility class with only static methods. Don't create instances of it.
     */
    @Deprecated
    public PathTool()
    {
    }    
    
    /**
     * Determines the relative path of a filename from a base directory.
     * This method is useful in building relative links within pages of
     * a web site.  It provides similar functionality to Anakia's
     * <code>$relativePath</code> context variable.  The arguments to
     * this method may contain either forward or backward slashes as
     * file separators.  The relative path returned is formed using
     * forward slashes as it is expected this path is to be used as a
     * link in a web page (again mimicking Anakia's behavior).
     * <p>
     * This method is thread-safe.
     * </p>
     * <pre>
     * PathTool.getRelativePath( null, null )                                   = ""
     * PathTool.getRelativePath( null, "/usr/local/java/bin" )                  = ""
     * PathTool.getRelativePath( "/usr/local/", null )                          = ""
     * PathTool.getRelativePath( "/usr/local/", "/usr/local/java/bin" )         = ".."
     * PathTool.getRelativePath( "/usr/local/", "/usr/local/java/bin/java.sh" ) = "../.."
     * PathTool.getRelativePath( "/usr/local/java/bin/java.sh", "/usr/local/" ) = ""
     * </pre>
     *
     * @param basedir  The base directory.
     * @param filename The filename that is relative to the base
     *                 directory.
     * @return The relative path of the filename from the base
     *         directory.  This value is not terminated with a forward slash.
     *         A zero-length string is returned if: the filename is not relative to
     *         the base directory, <code>basedir</code> is null or zero-length,
     *         or <code>filename</code> is null or zero-length.
     */
    public static String getRelativePath( @Nullable String basedir, @Nullable String filename )
    {
        basedir = uppercaseDrive( basedir );
        filename = uppercaseDrive( filename );

        /*
         * Verify the arguments and make sure the filename is relative
         * to the base directory.
         */
        if ( basedir == null || basedir.length() == 0 || filename == null || filename.length() == 0
            || !filename.startsWith( basedir ) )
        {
            return "";
        }

        /*
         * Normalize the arguments.  First, determine the file separator
         * that is being used, then strip that off the end of both the
         * base directory and filename.
         */
        String separator = determineSeparator( filename );
        basedir = StringUtils.chompLast( basedir, separator );
        filename = StringUtils.chompLast( filename, separator );

        /*
         * Remove the base directory from the filename to end up with a
         * relative filename (relative to the base directory).  This
         * filename is then used to determine the relative path.
         */
        String relativeFilename = filename.substring( basedir.length() );

        return determineRelativePath( relativeFilename, separator );
    }

    /**
     * <p>This method can calculate the relative path between two paths on a file system.</p>
     * <pre>
     * PathTool.getRelativeFilePath( null, null )                                   = ""
     * PathTool.getRelativeFilePath( null, "/usr/local/java/bin" )                  = ""
     * PathTool.getRelativeFilePath( "/usr/local", null )                           = ""
     * PathTool.getRelativeFilePath( "/usr/local", "/usr/local/java/bin" )          = "java/bin"
     * PathTool.getRelativeFilePath( "/usr/local", "/usr/local/java/bin/" )         = "java/bin"
     * PathTool.getRelativeFilePath( "/usr/local/java/bin", "/usr/local/" )         = "../.."
     * PathTool.getRelativeFilePath( "/usr/local/", "/usr/local/java/bin/java.sh" ) = "java/bin/java.sh"
     * PathTool.getRelativeFilePath( "/usr/local/java/bin/java.sh", "/usr/local/" ) = "../../.."
     * PathTool.getRelativeFilePath( "/usr/local/", "/bin" )                        = "../../bin"
     * PathTool.getRelativeFilePath( "/bin", "/usr/local/" )                        = "../usr/local"
     * </pre>
     * Note: On Windows based system, the <code>/</code> character should be replaced by <code>\</code> character.
     *
     * @param oldPath old path
     * @param newPath new path
     * @return a relative file path from <code>oldPath</code>.
     */
    public static String getRelativeFilePath( final String oldPath, final String newPath )
    {
        if ( StringUtils.isEmpty( oldPath ) || StringUtils.isEmpty( newPath ) )
        {
            return "";
        }

        // normalise the path delimiters
        String fromPath = new File( oldPath ).getPath();
        String toPath = new File( newPath ).getPath();

        // strip any leading slashes if its a windows path
        if ( toPath.matches( "^\\[a-zA-Z]:" ) )
        {
            toPath = toPath.substring( 1 );
        }
        if ( fromPath.matches( "^\\[a-zA-Z]:" ) )
        {
            fromPath = fromPath.substring( 1 );
        }

        // lowercase windows drive letters.
        if ( fromPath.startsWith( ":", 1 ) )
        {
            fromPath = Character.toLowerCase( fromPath.charAt( 0 ) ) + fromPath.substring( 1 );
        }
        if ( toPath.startsWith( ":", 1 ) )
        {
            toPath = Character.toLowerCase( toPath.charAt( 0 ) ) + toPath.substring( 1 );
        }

        // check for the presence of windows drives. No relative way of
        // traversing from one to the other.
        if ( ( toPath.startsWith( ":", 1 ) && fromPath.startsWith( ":", 1 ) )
            && ( !toPath.substring( 0, 1 ).equals( fromPath.substring( 0, 1 ) ) ) )
        {
            // they both have drive path element but they dont match, no
            // relative path
            return null;
        }

        if ( ( toPath.startsWith( ":", 1 ) && !fromPath.startsWith( ":", 1 ) )
            || ( !toPath.startsWith( ":", 1 ) && fromPath.startsWith( ":", 1 ) ) )
        {
            // one has a drive path element and the other doesnt, no relative
            // path.
            return null;
        }

        String resultPath = buildRelativePath( toPath, fromPath, File.separatorChar );

        if ( newPath.endsWith( File.separator ) && !resultPath.endsWith( File.separator ) )
        {
            return resultPath + File.separator;
        }

        return resultPath;
    }

    // ----------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------

    /**
     * Determines the relative path of a filename.  For each separator
     * within the filename (except the leading if present), append the
     * "../" string to the return value.
     *
     * @param filename  The filename to parse.
     * @param separator The separator used within the filename.
     * @return The relative path of the filename.  This value is not
     *         terminated with a forward slash.  A zero-length string is
     *         returned if: the filename is zero-length.
     */
    @Nonnull private static String determineRelativePath( @Nonnull String filename, @Nonnull String separator )
    {
        if ( filename.length() == 0 )
        {
            return "";
        }

        /*
        * Count the slashes in the relative filename, but exclude the
        * leading slash.  If the path has no slashes, then the filename
        * is relative to the current directory.
        */
        int slashCount = StringUtils.countMatches( filename, separator ) - 1;
        if ( slashCount <= 0 )
        {
            return ".";
        }

        /*
         * The relative filename contains one or more slashes indicating
         * that the file is within one or more directories.  Thus, each
         * slash represents a "../" in the relative path.
         */
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < slashCount; i++ )
        {
            sb.append( "../" );
        }

        /*
         * Finally, return the relative path but strip the trailing
         * slash to mimic Anakia's behavior.
         */
        return StringUtils.chop( sb.toString() );
    }

    /**
     * Helper method to determine the file separator (forward or
     * backward slash) used in a filename.  The slash that occurs more
     * often is returned as the separator.
     *
     * @param filename The filename parsed to determine the file
     *                 separator.
     * @return The file separator used within <code>filename</code>.
     *         This value is either a forward or backward slash.
     */
    private static String determineSeparator( String filename )
    {
        int forwardCount = StringUtils.countMatches( filename, "/" );
        int backwardCount = StringUtils.countMatches( filename, "\\" );

        return forwardCount >= backwardCount ? "/" : "\\";
    }

    /**
     * Cygwin prefers lowercase drive letters, but other parts of maven use uppercase
     *
     * @param path old path
     * @return String
     */
    static String uppercaseDrive( @Nullable String path )
    {
        if ( path == null )
        {
            return null;
        }
        if ( path.length() >= 2 && path.charAt( 1 ) == ':' )
        {
            path = Character.toUpperCase( path.charAt( 0 ) ) + path.substring( 1 );
        }
        return path;
    }

    @Nonnull private static String buildRelativePath( @Nonnull String toPath, @Nonnull String fromPath,
                                                      final char separatorChar )
    {
        // use tokeniser to traverse paths and for lazy checking
        StringTokenizer toTokeniser = new StringTokenizer( toPath, String.valueOf( separatorChar ) );
        StringTokenizer fromTokeniser = new StringTokenizer( fromPath, String.valueOf( separatorChar ) );

        int count = 0;

        // walk along the to path looking for divergence from the from path
        while ( toTokeniser.hasMoreTokens() && fromTokeniser.hasMoreTokens() )
        {
            if ( separatorChar == '\\' )
            {
                if ( !fromTokeniser.nextToken().equalsIgnoreCase( toTokeniser.nextToken() ) )
                {
                    break;
                }
            }
            else
            {
                if ( !fromTokeniser.nextToken().equals( toTokeniser.nextToken() ) )
                {
                    break;
                }
            }

            count++;
        }

        // reinitialise the tokenisers to count positions to retrieve the
        // gobbled token

        toTokeniser = new StringTokenizer( toPath, String.valueOf( separatorChar ) );
        fromTokeniser = new StringTokenizer( fromPath, String.valueOf( separatorChar ) );

        while ( count-- > 0 )
        {
            fromTokeniser.nextToken();
            toTokeniser.nextToken();
        }

        StringBuilder relativePath = new StringBuilder();

        // add back refs for the rest of from location.
        while ( fromTokeniser.hasMoreTokens() )
        {
            fromTokeniser.nextToken();

            relativePath.append( ".." );

            if ( fromTokeniser.hasMoreTokens() )
            {
                relativePath.append( separatorChar );
            }
        }

        if ( relativePath.length() != 0 && toTokeniser.hasMoreTokens() )
        {
            relativePath.append( separatorChar );
        }

        // add fwd fills for whatevers left of newPath.
        while ( toTokeniser.hasMoreTokens() )
        {
            relativePath.append( toTokeniser.nextToken() );

            if ( toTokeniser.hasMoreTokens() )
            {
                relativePath.append( separatorChar );
            }
        }
        return relativePath.toString();
    }
}
