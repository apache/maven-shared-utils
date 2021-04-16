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

import java.util.Locale;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;

/**
 * Test case for character case changes, to precisely point the situations when character case comparison doesn't
 * give intuitive result, or why one should avoid {@link String#toUpperCase()} and {@link String#toLowerCase()}
 * (platform locale dependent, with sometimes unexpected results)
 * but <b>prefer {@link String#equalsIgnoreCase(String)} when possible</b>.
 * 
 * @author Hervé Boutemy
 * @see <a href="http://sim.ivi.co/2011/07/trap-of-case-insensitive-string.html">Simple Smiles - Xuelei Fan's Blog</a>
 */
public class CaseTest
    extends Assert
{
    private final static Locale LOCALE_TURKISH = new Locale( "tr" );

    /** common ASCII 'i' */
    private final static char DOTTED_i = '\u0069';

    /** common ASCII 'I' */
    private final static char DOTLESS_I = '\u0049';

    /** turkish dotless i = ı */
    private final static char DOTLESS_i = '\u0131';

    /** turkish dotted I = İ */
    private final static char DOTTED_I = '\u0130';

    /** http://en.wikipedia.org/wiki/Dot_(diacritic) */
    private final static char COMBINING_DOT_ABOVE = '\u0307';

    private final static Locale SAVED_DEFAULT_LOCALE = Locale.getDefault();

    @AfterClass
    public static void restoreDefaultLocale()
    {
        Locale.setDefault( SAVED_DEFAULT_LOCALE );
    }

    /**
     * test the known case of upper I which doesn't give commonly expected i in Turkish locale, but ı (dotless i).
     * @see <a href="http://mattryall.net/blog/2009/02/the-infamous-turkish-locale-bug">The infamous Turkish locale bug</a>
     */
    @Test
    public void testTurkishI()
    {
        // check common i and I
        assertEquals( "common lowercase i should have a dot", 'i', DOTTED_i );
        assertEquals( "common uppercase I should not have a dot", 'I', DOTLESS_I );

        final String iIıİ = "iIıİ";

        // check source encoding doesn't wreck havoc */
        assertUnicodeEquals( "misc i directly in (UTF-8) source", iIıİ, "" + DOTTED_i + DOTLESS_I + DOTLESS_i
            + DOTTED_I );

        // check toUpperCase and toLowerCase difference with turkish and english locales
        assertUnicodeEquals( "'iIıİ'.toUpperCase('tr')=='İIIİ'", "" + DOTTED_I + DOTLESS_I + DOTLESS_I + DOTTED_I,
                             iIıİ.toUpperCase( LOCALE_TURKISH ) );
        assertUnicodeEquals( "'iIıİ'.toLowerCase('tr')=='iııi'", "" + DOTTED_i + DOTLESS_i + DOTLESS_i + DOTTED_i,
                             iIıİ.toLowerCase( LOCALE_TURKISH ) );
        assertUnicodeEquals( "'iIıİ'.toUpperCase('en')=='IIIİ'", "" + DOTLESS_I + DOTLESS_I + DOTLESS_I + DOTTED_I,
                             iIıİ.toUpperCase( Locale.ENGLISH ) );
        String lower = iIıİ.toLowerCase( Locale.ENGLISH ); // on some platforms, ends with extra COMBINED DOT ABOVE
        assertUnicodeEquals( "'iIıİ'.toLowerCase('en')=='iiıi'", "" + DOTTED_i + DOTTED_i + DOTLESS_i + DOTTED_i
            + ( lower.length() > 4 ? COMBINING_DOT_ABOVE : "" ), lower );

        // check equalsIgnoreCase() , which has no locale
        for ( int i = 0; i < iIıİ.length(); i++ )
        {
            char currentI = iIıİ.charAt( i );

            StringBuilder sb = new StringBuilder( iIıİ.length() );
            for ( int j = 0; j < iIıİ.length(); j++ )
            {
                sb.append( currentI );
            }
            String current = sb.toString();

            assertTrue( "'" + current + "'.equalsIgnoreCase('" + iIıİ + "')", current.equalsIgnoreCase( iIıİ ) );
        }
    }

    /**
     * Assert equals, and in case the result isn't as expected, display content unicode-escaped.
     * @param message
     * @param expected
     * @param actual
     */
    private void assertUnicodeEquals( String message, String expected, String actual )
    {
        if ( expected.equals( actual ) )
        {
            return;
        }

        throw new ComparisonFailure( message, StringEscapeUtils.escapeJava( expected ),
                                     StringEscapeUtils.escapeJava( actual ) );
    }

    /**
     * Test case change on all ascii characters with every available locale, to check that turkish i is the only
     * exception on these characters.
     */
    @Test
    public void testAsciiAvailableLocales()
    {
        final String lower = "abcdefghijklmnopqrstuvwxyz";
        final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for ( Locale locale : Locale.getAvailableLocales() )
        {
            // check that toUpper() == toUpper(default locale) and toLower() = toLower(default locale)
            Locale.setDefault( locale );
            assertEquals( lower.toUpperCase(), lower.toUpperCase( locale ) );
            assertEquals( upper.toLowerCase(), upper.toLowerCase( locale ) );

            // check result
            String expectedToUpperCase = upper;
            String expectedToLowerCase = lower;
            if ( LOCALE_TURKISH.getLanguage().equals( locale.getLanguage() ) ||
                 new Locale( "az" ).getLanguage().equals( locale.getLanguage() )           )
            {
                expectedToUpperCase = upper.replace( DOTLESS_I, DOTTED_I );
                expectedToLowerCase = lower.replace( DOTTED_i, DOTLESS_i );
            }

            assertEquals( "'" + lower + "'.toUpperCase('" + locale.toString() + "')", expectedToUpperCase,
                          lower.toUpperCase( locale ) );
            assertEquals( "'" + upper + "'.toLowerCase('" + locale.toString() + "')", expectedToLowerCase,
                          upper.toLowerCase( locale ) );

            // check that toLowerCase on lower and toUpperCase on upper don't cause harm
            assertEquals( "'" + lower + "'.toLowerCase('" + locale.toString() + "')", lower, lower.toLowerCase( locale ) );
            assertEquals( "'" + upper + "'.toUpperCase('" + locale.toString() + "')", upper, upper.toUpperCase( locale ) );

            // check equalsIgnoreCase
            assertTrue( "'" + upper + "'.equalsIgnoreCase('" + lower + "')", upper.equalsIgnoreCase( lower ) );
            assertTrue( "'" + upper + "'.equalsIgnoreCase('" + expectedToLowerCase + "')",
                        upper.equalsIgnoreCase( expectedToLowerCase ) );
            assertTrue( "'" + expectedToUpperCase + "'.equalsIgnoreCase('" + lower + "')",
                        expectedToUpperCase.equalsIgnoreCase( lower ) );
        }
    }
}
