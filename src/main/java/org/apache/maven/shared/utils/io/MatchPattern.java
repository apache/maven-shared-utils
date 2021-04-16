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
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Describes a match target for SelectorUtils.
 * <p/>
 * Significantly more efficient than using strings, since re-evaluation and re-tokenizing is avoided.
 *
 * @author Kristian Rosenvold
 * @deprecated use {@code java.nio.filejava.nio.file.DirectoryStream.Filter<T>} and related classes
 */
@Deprecated
public class MatchPattern
{
    private final String source;

    private final String regexPattern;

    private final Pattern regexPatternRegex;

    private final String separator;

    private final String[] tokenized;

    private MatchPattern( @Nonnull String source, @Nonnull String separator )
    {
        regexPattern = SelectorUtils.isRegexPrefixedPattern( source ) ? source.substring(
            SelectorUtils.REGEX_HANDLER_PREFIX.length(),
            source.length() - SelectorUtils.PATTERN_HANDLER_SUFFIX.length() ) : null;
        regexPatternRegex = regexPattern != null ? Pattern.compile( regexPattern ) : null;
        this.source = SelectorUtils.isAntPrefixedPattern( source ) ? source.substring(
            SelectorUtils.ANT_HANDLER_PREFIX.length(),
            source.length() - SelectorUtils.PATTERN_HANDLER_SUFFIX.length() ) : source;
        this.separator = separator;
        tokenized = tokenizePathToString( this.source, separator );
    }


    /**
     * @param str The string to match for.
     * @param isCaseSensitive case sensitive true false otherwise.
     * @return true if matches false otherwise.
     */
    public boolean matchPath( String str, boolean isCaseSensitive )
    {
        if ( regexPattern != null )
        {
            return regexPatternRegex.matcher( str ).matches();
        }
        else
        {
            return SelectorUtils.matchAntPathPattern( this, str, separator, isCaseSensitive );
        }
    }

    boolean matchPath( String str, String[] strDirs, boolean isCaseSensitive )
    {
        if ( regexPattern != null )
        {
            return regexPatternRegex.matcher( str ).matches();
        }
        else
        {
            return SelectorUtils.matchAntPathPattern( getTokenizedPathString(), strDirs, isCaseSensitive );
        }
    }

    /**
     * @param str The string to check.
     * @param isCaseSensitive Check case sensitive or not.
     * @return true in case of matching pattern.
     */
    public boolean matchPatternStart( @Nonnull String str, boolean isCaseSensitive )
    {
        if ( regexPattern != null )
        {
            // FIXME: ICK! But we can't do partial matches for regex, so we have to reserve judgment until we have
            // a file to deal with, or we can definitely say this is an exclusion...
            return true;
        }
        else
        {
            String altStr = source.replace( '\\', '/' );

            return SelectorUtils.matchAntPathPatternStart( this, str, File.separator, isCaseSensitive )
                || SelectorUtils.matchAntPathPatternStart( this, altStr, "/", isCaseSensitive );
        }
    }

    /**
     * @return Tokenized string.
     */
    public String[] getTokenizedPathString()
    {
        return tokenized;
    }


    /**
     * @param string The part which will be checked to start with.
     * @return true in case of starting with the string false otherwise.
     */
    public boolean startsWith( String string )
    {
        return source.startsWith( string );
    }


    static String[] tokenizePathToString( @Nonnull String path, @Nonnull String separator )
    {
        List<String> ret = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer( path, separator );
        while ( st.hasMoreTokens() )
        {
            ret.add( st.nextToken() );
        }
        return ret.toArray( new String[ret.size()] );
    }

    /**
     * @param source The source.
     * @return The match pattern.
     */
    public static MatchPattern fromString( String source )
    {
        return new MatchPattern( source, File.separator );
    }

}
