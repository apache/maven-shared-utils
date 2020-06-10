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

import org.apache.maven.shared.utils.io.conductor.EnforceExcludesOverIncludes;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;

/**
 * Class for scanning a directory for files/directories which match certain criteria.
 * <p/>
 * These criteria consist of selectors and patterns which have been specified. With the selectors you can select which
 * files you want to have included. Files which are not selected are excluded. With patterns you can include or exclude
 * files based on their filename.
 * <p/>
 * The idea is simple. A given directory is recursively scanned for all files and directories. Each file/directory is
 * matched against a set of selectors, including special support for matching against filenames with include and and
 * exclude patterns. Only files/directories which match at least one pattern of the include pattern list or other file
 * selector, and don't match any pattern of the exclude pattern list or fail to match against a required selector will
 * be placed in the list of files/directories found.
 * <p/>
 * When no list of include patterns is supplied, "**" will be used, which means that everything will be matched. When no
 * list of exclude patterns is supplied, an empty list is used, such that nothing will be excluded. When no selectors
 * are supplied, none are applied.
 * <p/>
 * The filename pattern matching is done as follows: The name to be matched is split up in path segments. A path segment
 * is the name of a directory or file, which is bounded by <code>File.separator</code> ('/' under UNIX, '\' under
 * Windows). For example, "abc/def/ghi/xyz.java" is split up in the segments "abc", "def","ghi" and "xyz.java". The same
 * is done for the pattern against which should be matched.
 * <p/>
 * The segments of the name and the pattern are then matched against each other. When '**' is used for a path segment in
 * the pattern, it matches zero or more path segments of the name.
 * <p/>
 * There is a special case regarding the use of <code>File.separator</code>s at the beginning of the pattern and the
 * string to match:<br>
 * When a pattern starts with a <code>File.separator</code>, the string to match must also start with a
 * <code>File.separator</code>. When a pattern does not start with a <code>File.separator</code>, the string to match
 * may not start with a <code>File.separator</code>. When one of these rules is not obeyed, the string will not match.
 * <p/>
 * When a name path segment is matched against a pattern path segment, the following special characters can be used:<br>
 * '*' matches zero or more characters<br>
 * '?' matches one character.
 * <p/>
 * Examples:
 * <p/>
 * "**\*.class" matches all .class files/dirs in a directory tree.
 * <p/>
 * "test\a??.java" matches all files/dirs which start with an 'a', then two more characters and then ".java", in a
 * directory called test.
 * <p/>
 * "**" matches everything in a directory tree.
 * <p/>
 * "**\test\**\XYZ*" matches all files/dirs which start with "XYZ" and where there is a parent directory called test
 * (e.g. "abc\test\def\ghi\XYZ123").
 * <p/>
 * Case sensitivity may be turned off if necessary. By default, it is turned on.
 * <p/>
 * Example of usage:
 * <p/>
 * <pre>
 * String[] includes = { &quot;**\\*.class&quot; };
 * String[] excludes = { &quot;modules\\*\\**&quot; };
 * ds.setIncludes( includes );
 * ds.setExcludes( excludes );
 * ds.setBasedir( new File( &quot;test&quot; ) );
 * ds.setCaseSensitive( true );
 * ds.scan();
 *
 * System.out.println( &quot;FILES:&quot; );
 * String[] files = ds.getIncludedFiles();
 * for ( int i = 0; i &lt; files.length; i++ )
 * {
 *     System.out.println( files[i] );
 * }
 * </pre>
 * <p/>
 * This will scan a directory called test for .class files, but excludes all files in all proper subdirectories of a
 * directory called "modules"
 * <p/>
 * This class must not be used from multiple Threads concurrently!
 *
 * @author Arnout J. Kuiper <a href="mailto:ajkuiper@wxs.nl">ajkuiper@wxs.nl</a>
 * @author Magesh Umasankar
 * @author <a href="mailto:bruce@callenish.com">Bruce Atherton</a>
 * @author <a href="mailto:levylambert@tiscali-dsl.de">Antoine Levy-Lambert</a>
 */
public class DirectoryScanner
{
    /**
     * Patterns which should be excluded by default.
     *
     * @see #addDefaultExcludes()
     */
    public static final String[] DEFAULTEXCLUDES = {
        // Miscellaneous typical temporary files
        "**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*",

        // CVS
        "**/CVS", "**/CVS/**", "**/.cvsignore",

        // Subversion
        "**/.svn", "**/.svn/**",

        // Arch
        "**/.arch-ids", "**/.arch-ids/**",

        // Bazaar
        "**/.bzr", "**/.bzr/**",

        // SurroundSCM
        "**/.MySCMServerInfo",

        // Mac
        "**/.DS_Store",

        // Serena Dimensions Version 10
        "**/.metadata", "**/.metadata/**",

        // Mercurial
        "**/.hg", "**/.hg/**",

        // git
        "**/.git", "**/.git/**",

        // BitKeeper
        "**/BitKeeper", "**/BitKeeper/**", "**/ChangeSet", "**/ChangeSet/**",

        // darcs
        "**/_darcs", "**/_darcs/**", "**/.darcsrepo", "**/.darcsrepo/**", "**/-darcs-backup*", "**/.darcs-temp-mail" };

    /**
     * The base directory to be scanned.
     */
    private Path basedir;

    /**
     * The patterns for the files to be included.
     */
    private String[] includes;

    /**
     * The patterns for the files to be excluded.
     */
    private String[] excludes;

    private MatchPatterns excludesPatterns;

    private MatchPatterns includesPatterns;


    /**
     * The files which matched at least one include and no excludes and were selected.
     */
    private List<String> filesIncluded;

    /**
     * The files which did not match any includes or selectors.
     */
    private List<String> filesNotIncluded;

    /**
     * The files which matched at least one include and at least one exclude.
     */
    private List<String> filesExcluded;

    /**
     * The directories which matched at least one include and no excludes and were selected.
     */
    private List<String> dirsIncluded;

    /**
     * The directories which were found and did not match any includes.
     */
    private List<String> dirsNotIncluded;

    /**
     * The directories which matched at least one include and at least one exclude.
     */
    private List<String> dirsExcluded;

    /**
     * Whether or not our results were built by a slow scan.
     */
    private boolean haveSlowResults = false;

    /**
     * Whether or not the file system should be treated as a case sensitive one.
     */
    private boolean isCaseSensitive = true;

    /**
     * Whether or not symbolic links should be followed.
     */
    private boolean followSymlinks = true;


    /**
     * A {@link ScanConductor} an control the scanning process.
     */
    private ScanConductor scanConductor = null;

    /**
     * Sole constructor.
     */
    public DirectoryScanner()
    {
    }

    /**
     * Sets the base directory to be scanned. This is the directory which is scanned recursively. All '/' and '\'
     * characters are replaced by <code>File.separatorChar</code>, so the separator used need not match
     * <code>File.separatorChar</code>.
     *
     * @param basedir The base directory to scan. Must not be <code>null</code>.
     */
    public void setBasedir( final String basedir )
    {
        setBasedir( new File( basedir.replace( '/', File.separatorChar ).replace( '\\', File.separatorChar ) ) );
    }

    /**
     * Sets the base directory to be scanned. This is the directory which is scanned recursively.
     *
     * @param basedir The base directory for scanning. Should not be <code>null</code>.
     */
    public void setBasedir( @Nonnull final File basedir )
    {
        setBasedir( basedir.toPath() );
    }

    /**
     * Sets the base directory to be scanned. This is the directory which is scanned recursively.
     *
     * @param basedir The base directory for scanning. Should not be <code>null</code>.
     */
    public void setBasedir( @Nonnull final Path basedir )
    {
        this.basedir = basedir;
    }

    /**
     * Returns the base directory to be scanned. This is the directory which is scanned recursively.
     *
     * @return the base directory to be scanned
     */
    public File getBasedir()
    {
        return basedir.toFile();
    }

    /**
     * Sets whether or not the file system should be regarded as case sensitive.
     *
     * @param isCaseSensitiveParameter whether or not the file system should be regarded as a case sensitive one
     */
    public void setCaseSensitive( final boolean isCaseSensitiveParameter )
    {
        this.isCaseSensitive = isCaseSensitiveParameter;
    }

    /**
     * Sets whether or not symbolic links should be followed.
     *
     * @param followSymlinks whether or not symbolic links should be followed
     */
    public void setFollowSymlinks( final boolean followSymlinks )
    {
        this.followSymlinks = followSymlinks;
    }

    /**
     * Sets the list of include patterns to use. All '/' and '\' characters are replaced by
     * <code>File.separatorChar</code>, so the separator used need not match <code>File.separatorChar</code>.
     * <p/>
     * When a pattern ends with a '/' or '\', "**" is appended.
     *
     * @param includes A list of include patterns. May be <code>null</code>, indicating that all files should be
     *                 included. If a non-<code>null</code> list is given, all elements must be non-<code>null</code>.
     */
    public void setIncludes( final String... includes )
    {
        if ( includes == null )
        {
            this.includes = null;
        }
        else
        {
            this.includes = new String[includes.length];
            for ( int i = 0; i < includes.length; i++ )
            {
                String pattern;
                pattern = includes[i].trim().replace( '/', File.separatorChar ).replace( '\\', File.separatorChar );
                if ( pattern.endsWith( File.separator ) )
                {
                    pattern += "**";
                }
                this.includes[i] = pattern;
            }
        }
    }

    /**
     * Sets the list of exclude patterns to use. All '/' and '\' characters are replaced by
     * <code>File.separatorChar</code>, so the separator used need not match <code>File.separatorChar</code>.
     * <p/>
     * When a pattern ends with a '/' or '\', "**" is appended.
     *
     * @param excludes A list of exclude patterns. May be <code>null</code>, indicating that no files should be
     *                 excluded. If a non-<code>null</code> list is given, all elements must be non-<code>null</code>.
     */
    public void setExcludes( final String... excludes )
    {
        if ( excludes == null )
        {
            this.excludes = null;
        }
        else
        {
            this.excludes = new String[excludes.length];
            for ( int i = 0; i < excludes.length; i++ )
            {
                String pattern;
                pattern = excludes[i].trim().replace( '/', File.separatorChar ).replace( '\\', File.separatorChar );
                if ( pattern.endsWith( File.separator ) )
                {
                    pattern += "**";
                }
                this.excludes[i] = pattern;
            }
        }
    }

    /**
     * @param scanConductor {@link #scanConductor}
     */
    public void setScanConductor( final ScanConductor scanConductor )
    {
        this.scanConductor = scanConductor;
    }

    /**
     * Set {@link EnforceExcludesOverIncludes} scan conductor for a faster scanning
     * and generally no functional side effect.
     */
    public void setEnforceExcludesOverIncludes()
    {
        setScanConductor( new EnforceExcludesOverIncludes() );
    }

    /**
     * Scans the base directory for files which match at least one include pattern and don't match any exclude patterns.
     * If there are selectors then the files must pass muster there, as well.
     *
     * @throws IllegalStateException if the base directory was set incorrectly (i.e. if it is <code>null</code>,
     *                               doesn't exist, or isn't a directory).
     */
    public void scan()
        throws IllegalStateException
    {
        if ( basedir == null )
        {
            throw new IllegalStateException( "No basedir set" );
        }
        if ( !Files.exists( basedir ) )
        {
            throw new IllegalStateException( "basedir " + basedir + " does not exist" );
        }
        if ( !Files.isDirectory( basedir ) )
        {
            throw new IllegalStateException( "basedir " + basedir + " is not a directory" );
        }

        setupDefaultFilters();
        setupMatchPatterns();

        if ( scanConductor instanceof ScannerAware ) // after the init
        {
            ( ( ScannerAware ) scanConductor ).setDirectoryScanner( this );
        }

        filesIncluded = new ArrayList<>();
        filesNotIncluded = new ArrayList<>();
        filesExcluded = new ArrayList<>();
        dirsIncluded = new ArrayList<>();
        dirsNotIncluded = new ArrayList<>();
        dirsExcluded = new ArrayList<>();

        doScan( basedir, followSymlinks ? EnumSet.of( FOLLOW_LINKS ) : EnumSet.noneOf( FileVisitOption.class ), true );
    }

    private void doScan( final Path root, final Set<FileVisitOption> options, final boolean fast )
    {
        try
        {
            Files.walkFileTree( root, options, Integer.MAX_VALUE, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult preVisitDirectory( final Path dir, final BasicFileAttributes attrs )
                        throws IOException
                {
                    final String name = root.relativize( dir ).toString();
                    if ( isIncluded( name ) )
                    {
                        if ( !isExcluded( name ) )
                        {
                            if ( scanConductor != null )
                            {
                                final ScanConductor.ScanAction scanAction = scanConductor.visitDirectory(
                                        name, dir.toFile() );
                                if ( ScanConductor.ScanAction.ABORT.equals( scanAction )
                                        || ScanConductor.ScanAction.ABORT_DIRECTORY.equals( scanAction ) )
                                {
                                    return FileVisitResult.SKIP_SIBLINGS;
                                }
                                if ( ScanConductor.ScanAction.NO_RECURSE.equals( scanAction ) )
                                {
                                    return FileVisitResult.SKIP_SUBTREE;
                                }
                            }
                            dirsIncluded.add( name );
                        }
                        else
                        {
                            dirsExcluded.add( name );
                            if ( fast && !couldHoldIncluded( name ) )
                            {
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                            if ( scanConductor != null )
                            {
                                final FileVisitResult result = toVisitResult( dir, name );
                                if ( result != null )
                                {
                                    return result;
                                }
                            }
                            // else continue to visit
                        }
                    }
                    else
                    {
                        if ( fast && couldHoldIncluded( name ) )
                        {
                            if ( scanConductor != null )
                            {
                                final FileVisitResult result = toVisitResult( dir, name );
                                if ( result != null )
                                {
                                    return result;
                                }
                            }
                            dirsNotIncluded.add( name );
                        }
                        else if ( !fast )
                        {
                            final FileVisitResult result = toVisitResult( dir, name );
                            if ( result != null )
                            {
                                return result;
                            }
                        }
                    }
                    return super.preVisitDirectory( dir, attrs );
                }

                @Override
                public FileVisitResult visitFile( final Path file, final BasicFileAttributes attrs ) throws IOException
                {
                    final String name = root.relativize( file ).toString();
                    if ( !followSymlinks && Files.isSymbolicLink( file ) )
                    {
                        final Path resolved = file.toRealPath( );
                        if ( Files.isDirectory( resolved ) )
                        {
                            dirsIncluded.add( name );
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }
                    if ( isIncluded( name ) )
                    {
                        if ( !isExcluded( name ) )
                        {
                            final ScanConductor.ScanAction scanAction;
                            if ( scanConductor != null )
                            {
                                scanAction = scanConductor.visitFile( name, file.toFile() );
                            }
                            else
                            {
                                scanAction = null;
                            }

                            if ( ScanConductor.ScanAction.ABORT.equals( scanAction )
                                    || ScanConductor.ScanAction.ABORT_DIRECTORY.equals( scanAction ) )
                            {
                                return FileVisitResult.SKIP_SIBLINGS;
                            }

                            filesIncluded.add( name );
                        }
                        else
                        {
                            filesExcluded.add( name );
                        }
                    }
                    else
                    {
                        filesNotIncluded.add( name );
                    }
                    return super.visitFile( file, attrs );
                }
            } );
        }
        catch ( final IOException e )
        {
            throw new IllegalStateException( e );
        }
    }

    private FileVisitResult toVisitResult( final Path dir, final String name )
    {
        final ScanConductor.ScanAction scanAction = scanConductor.visitDirectory(
                name, dir.toFile() );
        if ( ScanConductor.ScanAction.ABORT.equals( scanAction )
                || ScanConductor.ScanAction.ABORT_DIRECTORY.equals( scanAction ) )
        {
            return FileVisitResult.SKIP_SIBLINGS;
        }
        if ( ScanConductor.ScanAction.NO_RECURSE.equals( scanAction ) )
        {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return null;
    }

    /**
     * Determine the file differences between the currently included files and
     * a previously captured list of files.
     * This method will not look for a changed in content but sole in the
     * list of files given.
     * <p/>
     * The method will compare the given array of file Strings with the result
     * of the last directory scan. It will execute a {@link #scan()} if no
     * result of a previous scan could be found.
     * <p/>
     * The result of the diff can be queried by the methods
     * {@link DirectoryScanResult#getFilesAdded()} and {@link DirectoryScanResult#getFilesRemoved()}
     *
     * @param oldFiles the list of previously captured files names.
     * @return the result of the directory scan.
     */
    public DirectoryScanResult diffIncludedFiles( String... oldFiles )
    {
        if ( filesIncluded == null )
        {
            // perform a scan if the directory didn't got scanned yet
            scan();
        }

        return diffFiles( oldFiles, filesIncluded.toArray( new String[filesIncluded.size()] ) );
    }

    /**
     * @param oldFiles array of old files
     * @param newFiles array of new files
     * @return calculated difference
     */
    public static DirectoryScanResult diffFiles( @Nullable String[] oldFiles, @Nullable  String[] newFiles )
    {
        Set<String> oldFileSet = arrayAsHashSet( oldFiles );
        Set<String> newFileSet = arrayAsHashSet( newFiles );

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

        String[] filesAdded = added.toArray( new String[added.size()] );
        String[] filesRemoved = removed.toArray( new String[removed.size()] );

        return new DirectoryScanResult( filesAdded, filesRemoved );
    }


    /**
     * Take an array of type T and convert it into a HashSet of type T.
     * If <code>null</code> or an empty array gets passed, an empty Set will be returned.
     *
     * @param array  The array
     * @return the filled HashSet of type T
     */
    private static <T> Set<T> arrayAsHashSet( @Nullable T[] array )
    {
        if ( array == null || array.length == 0 )
        {
            return Collections.emptySet();
        }

        Set<T> set = new HashSet<T>( array.length );
        Collections.addAll( set, array );

        return set;
    }

    /**
     * Top level invocation for a slow scan. A slow scan builds up a full list of excluded/included files/directories,
     * whereas a fast scan will only have full results for included files, as it ignores directories which can't
     * possibly hold any included files/directories.
     * <p/>
     * Returns immediately if a slow scan has already been completed.
     */
    void slowScan()
    {
        if ( haveSlowResults )
        {
            return;
        }

        final String[] excl = dirsExcluded.toArray( new String[ 0 ] );

        final String[] notIncl = dirsNotIncluded.toArray( new String[ 0 ] );

        final EnumSet<FileVisitOption> opts = followSymlinks
                ? EnumSet.of( FOLLOW_LINKS ) : EnumSet.noneOf( FileVisitOption.class );

        for ( String anExcl : excl )
        {
            if ( !couldHoldIncluded( anExcl ) )
            {
                doScan( basedir.resolve( anExcl ), opts, false );
            }
        }

        for ( String aNotIncl : notIncl )
        {
            if ( !couldHoldIncluded( aNotIncl ) )
            {
                doScan( basedir.resolve( aNotIncl ), opts, false );
            }
        }

        haveSlowResults = true;
    }

    /**
     * Tests whether or not a name matches against at least one include pattern.
     *
     * @param name The name to match. Must not be <code>null</code>.
     * @return <code>true</code> when the name matches against at least one include pattern, or <code>false</code>
     *         otherwise.
     */
    boolean isIncluded( final String name )
    {
        return includesPatterns.matches( name, isCaseSensitive );
    }

    /**
     * Tests whether or not a name matches the start of at least one include pattern.
     *
     * @param name The name to match. Must not be <code>null</code>.
     * @return <code>true</code> when the name matches against the start of at least one include pattern, or
     *         <code>false</code> otherwise.
     */
    boolean couldHoldIncluded( @Nonnull final String name )
    {
        return includesPatterns.matchesPatternStart( name, isCaseSensitive );
    }

    /**
     * Tests whether or not a name matches against at least one exclude pattern.
     *
     * @param name The name to match. Must not be <code>null</code>.
     * @return <code>true</code> when the name matches against at least one exclude pattern, or <code>false</code>
     *         otherwise.
     */
    boolean isExcluded( @Nonnull final String name )
    {
        return excludesPatterns.matches( name, isCaseSensitive );
    }

    /**
     * Returns the names of the files which matched at least one of the include patterns and none of the exclude
     * patterns. The names are relative to the base directory.
     *
     * @return the names of the files which matched at least one of the include patterns and none of the exclude
     *         patterns. May also contain symbolic links to files.
     */
    public String[] getIncludedFiles()
    {
        if ( filesIncluded == null )
        {
            return new String[0];
        }
        return filesIncluded.toArray( new String[0] );
    }

    /**
     * Returns the names of the files which matched none of the include patterns. The names are relative to the base
     * directory. This involves performing a slow scan if one has not already been completed.
     *
     * @return the names of the files which matched none of the include patterns.
     * @see #slowScan
     */
    public String[] getNotIncludedFiles()
    {
        slowScan();
        return filesNotIncluded.toArray( new String[filesNotIncluded.size()] );
    }

    /**
     * Returns the names of the files which matched at least one of the include patterns and at least one of the exclude
     * patterns. The names are relative to the base directory. This involves performing a slow scan if one has not
     * already been completed.
     *
     * @return the names of the files which matched at least one of the include patterns and at at least one of the
     *         exclude patterns.
     * @see #slowScan
     */
    public String[] getExcludedFiles()
    {
        slowScan();
        return filesExcluded.toArray( new String[filesExcluded.size()] );
    }

    /**
     * Returns the names of the directories which matched at least one of the include patterns and none of the exclude
     * patterns. The names are relative to the base directory.
     *
     * @deprecated this method is buggy. Do not depend on it. 
     * @return the names of the directories which matched at least one of the include patterns and none of the exclude
     *         patterns. May also contain symbolic links to directories.
     */
    @Deprecated
    public String[] getIncludedDirectories()
    {
        return dirsIncluded.toArray( new String[dirsIncluded.size()] );
    }

    /**
     * Returns the names of the directories which matched none of the include patterns. The names are relative to the
     * base directory. This involves performing a slow scan if one has not already been completed.
     *
     * @return the names of the directories which matched none of the include patterns.
     * @see #slowScan
     */
    public String[] getNotIncludedDirectories()
    {
        slowScan();
        return dirsNotIncluded.toArray( new String[dirsNotIncluded.size()] );
    }

    /**
     * Returns the names of the directories which matched at least one of the include patterns and at least one of the
     * exclude patterns. The names are relative to the base directory. This involves performing a slow scan if one has
     * not already been completed.
     *
     * @return the names of the directories which matched at least one of the include patterns and at least one of the
     *         exclude patterns.
     * @see #slowScan
     */
    public String[] getExcludedDirectories()
    {
        slowScan();
        return dirsExcluded.toArray( new String[dirsExcluded.size()] );
    }

    /**
     * Adds default exclusions to the current exclusions set.
     */
    public void addDefaultExcludes()
    {
        final int excludesLength = excludes == null ? 0 : excludes.length;
        String[] newExcludes;
        newExcludes = new String[excludesLength + DEFAULTEXCLUDES.length];
        if ( excludesLength > 0 )
        {
            System.arraycopy( excludes, 0, newExcludes, 0, excludesLength );
        }
        for ( int i = 0; i < DEFAULTEXCLUDES.length; i++ )
        {
            newExcludes[i + excludesLength] =
                DEFAULTEXCLUDES[i].replace( '/', File.separatorChar ).replace( '\\', File.separatorChar );
        }
        excludes = newExcludes;
    }

    public MatchPatterns getExcludesPatterns()
    {
        setupDefaultFilters();
        return excludesPatterns;
    }

    public MatchPatterns getIncludesPatterns()
    {
        setupDefaultFilters();
        return includesPatterns;
    }

    private void setupDefaultFilters()
    {
        if ( includes == null )
        {
            // No includes supplied, so set it to 'matches all'
            includes = new String[1];
            includes[0] = "**";
        }
        if ( excludes == null )
        {
            excludes = new String[0];
        }
    }


    private void setupMatchPatterns()
    {
        includesPatterns = MatchPatterns.from( includes );
        excludesPatterns = MatchPatterns.from( excludes );
    }

}
