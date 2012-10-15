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

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

/**
 * Test the {@link SelectorUtils} class.
 *
 */
public class SelectorUtilsTest
{

    @Test( expected = NullPointerException.class )
    public void testMatchPatternStart()
    {
        SelectorUtils.matchPatternStart( null, null );
    }

    @Test
    public void testEmptyStrings()
    {
        assertTrue( SelectorUtils.matchPatternStart( "", "" ) );
    }

    @Test
    public void testRegexPrefix()
        throws Exception
    {
        assertEquals( true,
                      SelectorUtils.matchPatternStart( SelectorUtils.REGEX_HANDLER_PREFIX + File.separator + "aaa"
                          + SelectorUtils.PATTERN_HANDLER_SUFFIX, "" ) );
    }

    @Test
    public void testAntPatternStrings()
    {
        assertAntDoesNotMatch( "/aaa", "" );
        assertAntDoesNotMatch( "\\aaa", "" );
        assertAntMatch( "aaa", "" );
        assertAntMatch( "/aaa/bbb", "/aaa/bbb" );
        assertAntMatch( "/aaa/**", "/aaa/bbb" );
        assertAntDoesNotMatch( "/aaa/**", "/ccc/bbb" );
        assertAntMatch( "/aaa/**", "\\aaa\\bbb" );
        assertAntDoesNotMatch( "/aaa/**", "\\ccc\\bbb" );
        assertAntDoesNotMatch( "/aaa/", "\\aaa\\bbb" );
    }


    private void assertAntDoesNotMatch( String pattern, String target )
    {
        assertEquals( false, SelectorUtils.matchPatternStart( wrapWithAntHandler( pattern ), target ) );
    }

    private void assertAntMatch( String pattern, String target )
    {
        assertEquals( true, SelectorUtils.matchPatternStart( wrapWithAntHandler( pattern ), target ) );
    }

    private String wrapWithAntHandler( String val )
    {
        return SelectorUtils.ANT_HANDLER_PREFIX + val + SelectorUtils.PATTERN_HANDLER_SUFFIX;
    }
}
