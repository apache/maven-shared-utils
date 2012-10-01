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

/**
 * A list of patterns to be matched
 *
 * @author Kristian Rosenvold
 */
public class MatchPatterns
{
    private final MatchPattern[] patterns;

    private MatchPatterns( MatchPattern[] patterns )
    {
        this.patterns = patterns;
    }

    /**
     * Checks these MatchPatterns against a specified string.
     * <p/>
     * Uses far less string tokenization than any of the alternatives.
     *
     * @param name            The name to look for
     * @param isCaseSensitive If the comparison is case sensitive
     * @return true if any of the supplied patterns match
     */
    public boolean matches( String name, boolean isCaseSensitive )
    {
        String[] tokenized = MatchPattern.tokenizePathToString( name, File.separator );
        for ( MatchPattern pattern : patterns )
        {
            if ( pattern.matchPath( name, tokenized, isCaseSensitive ) )
            {
                return true;
            }
        }
        return false;
    }

    public boolean matchesPatternStart( String name, boolean isCaseSensitive )
    {
        for ( MatchPattern includesPattern : patterns )
        {
            if ( includesPattern.matchPatternStart( name, isCaseSensitive ) )
            {
                return true;
            }
        }
        return false;
    }

    public static MatchPatterns from( String... sources )
    {
        final int length = sources.length;
        MatchPattern[] result = new MatchPattern[length];
        for ( int i = 0; i < length; i++ )
        {
            result[i] = MatchPattern.fromString( sources[i] );
        }
        return new MatchPatterns( result );
    }

    public static MatchPatterns from( Iterable<String> strings )
    {
        return new MatchPatterns( getMatchPatterns( strings ) );
    }

    private static MatchPattern[] getMatchPatterns( Iterable<String> items )
    {
        List<MatchPattern> result = new ArrayList<MatchPattern>();
        for ( String string : items )
        {
            result.add( MatchPattern.fromString( string ) );
        }
        return result.toArray( new MatchPattern[result.size()] );
    }

}
