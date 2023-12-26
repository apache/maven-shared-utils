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
package org.apache.maven.shared.utils;

import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for character case changes, to precisely point the situations when character case comparison doesn't
 * give intuitive result, or why one should avoid {@link String#toUpperCase()} and {@link String#toLowerCase()}
 * (platform locale dependent, with sometimes unexpected results)
 * but <b>prefer {@link String#equalsIgnoreCase(String)} when possible</b>.
 *
 * @author Hervé Boutemy
 * @see <a href="http://sim.ivi.co/2011/07/trap-of-case-insensitive-string.html">Simple Smiles - Xuelei Fan's Blog</a>
 */
class CaseTest { // } extends Assert {
    private static final Locale LOCALE_TURKISH = new Locale("tr");

    /** common ASCII 'i' */
    private static final char DOTTED_i = '\u0069';

    /** common ASCII 'I' */
    private static final char DOTLESS_I = '\u0049';

    /** turkish dotless i = ı */
    private static final char DOTLESS_i = '\u0131';

    /** turkish dotted I = İ */
    private static final char DOTTED_I = '\u0130';

    /** http://en.wikipedia.org/wiki/Dot_(diacritic) */
    private static final char COMBINING_DOT_ABOVE = '\u0307';

    private static final Locale SAVED_DEFAULT_LOCALE = Locale.getDefault();

    @AfterAll
    static void restoreDefaultLocale() {
        Locale.setDefault(SAVED_DEFAULT_LOCALE);
    }

    /**
     * test the known case of upper I which doesn't give commonly expected i in Turkish locale, but ı (dotless i).
     * @see <a href="http://mattryall.net/blog/2009/02/the-infamous-turkish-locale-bug">The infamous Turkish locale bug</a>
     */
    @Test
    void turkishI() {
        // check common i and I
        assertEquals('i', DOTTED_i, "common lowercase i should have a dot");
        assertEquals('I', DOTLESS_I, "common uppercase I should not have a dot");

        final String iIıİ = "iIıİ";

        // check source encoding doesn't wreck havoc */
        assertEquals("" + DOTTED_i + DOTLESS_I + DOTLESS_i + DOTTED_I, "misc i directly in (UTF-8) source", iIıİ);

        // check toUpperCase and toLowerCase difference with turkish and english locales
        assertEquals(
                iIıİ.toUpperCase(LOCALE_TURKISH),
                "'iIıİ'.toUpperCase('tr')=='İIIİ'",
                "" + DOTTED_I + DOTLESS_I + DOTLESS_I + DOTTED_I);
        assertEquals(
                iIıİ.toLowerCase(LOCALE_TURKISH),
                "'iIıİ'.toLowerCase('tr')=='iııi'",
                "" + DOTTED_i + DOTLESS_i + DOTLESS_i + DOTTED_i);
        assertEquals(
                iIıİ.toUpperCase(Locale.ENGLISH),
                "'iIıİ'.toUpperCase('en')=='IIIİ'",
                "" + DOTLESS_I + DOTLESS_I + DOTLESS_I + DOTTED_I);
        String lower = iIıİ.toLowerCase(Locale.ENGLISH); // on some platforms, ends with extra COMBINED DOT ABOVE
        String message =
                "" + DOTTED_i + DOTTED_i + DOTLESS_i + DOTTED_i + (lower.length() > 4 ? COMBINING_DOT_ABOVE : "");
        assertEquals(lower, "'iIıİ'.toLowerCase('en')=='iiıi'", message);

        // check equalsIgnoreCase() , which has no locale
        for (int i = 0; i < iIıİ.length(); i++) {
            char currentI = iIıİ.charAt(i);

            StringBuilder sb = new StringBuilder(iIıİ.length());
            for (int j = 0; j < iIıİ.length(); j++) {
                sb.append(currentI);
            }
            String current = sb.toString();

            assertTrue(current.equalsIgnoreCase(iIıİ), "'" + current + "'.equalsIgnoreCase('" + iIıİ + "')");
        }
    }

    /**
     * Test case change on all ascii characters with every available locale, to check that turkish i is the only
     * exception on these characters.
     */
    @Test
    void asciiAvailableLocales() {
        final String lower = "abcdefghijklmnopqrstuvwxyz";
        final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (Locale locale : Locale.getAvailableLocales()) {
            // check that toUpper() == toUpper(default locale) and toLower() = toLower(default locale)
            Locale.setDefault(locale);
            assertEquals(lower.toUpperCase(), lower.toUpperCase(locale));
            assertEquals(upper.toLowerCase(), upper.toLowerCase(locale));

            // check result
            String expectedToUpperCase = upper;
            String expectedToLowerCase = lower;
            if (LOCALE_TURKISH.getLanguage().equals(locale.getLanguage())
                    || new Locale("az").getLanguage().equals(locale.getLanguage())) {
                expectedToUpperCase = upper.replace(DOTLESS_I, DOTTED_I);
                expectedToLowerCase = lower.replace(DOTTED_i, DOTLESS_i);
            }

            assertEquals(
                    expectedToUpperCase,
                    lower.toUpperCase(locale),
                    "'" + lower + "'.toUpperCase('" + locale.toString() + "')");
            assertEquals(
                    expectedToLowerCase,
                    upper.toLowerCase(locale),
                    "'" + upper + "'.toLowerCase('" + locale.toString() + "')");

            // check that toLowerCase on lower and toUpperCase on upper don't cause harm
            assertEquals(lower, lower.toLowerCase(locale), "'" + lower + "'.toLowerCase('" + locale.toString() + "')");
            assertEquals(upper, upper.toUpperCase(locale), "'" + upper + "'.toUpperCase('" + locale.toString() + "')");

            // check equalsIgnoreCase
            assertTrue(upper.equalsIgnoreCase(lower), "'" + upper + "'.equalsIgnoreCase('" + lower + "')");
            assertTrue(
                    upper.equalsIgnoreCase(expectedToLowerCase),
                    "'" + upper + "'.equalsIgnoreCase('" + expectedToLowerCase + "')");
            assertTrue(
                    expectedToUpperCase.equalsIgnoreCase(lower),
                    "'" + expectedToUpperCase + "'.equalsIgnoreCase('" + lower + "')");
        }
    }
}
