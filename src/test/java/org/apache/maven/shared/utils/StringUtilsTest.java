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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the {@link StringUtils} class.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
@SuppressWarnings({"deprecation", "DataFlowIssue"})
class StringUtilsTest {

    @Test
    void abbreviateNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.abbreviate(null, 10));
    }

    @Test
    void abbreviateMinLength() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.abbreviate("This is a longtext", 3));
    }

    @Test
    void abbreviate() {
        assertEquals("This is...", StringUtils.abbreviate("This is a longtext", 10));

        assertEquals("This is a longtext", StringUtils.abbreviate("This is a longtext", 50));
    }

    @Test
    void abbreviateOffsetNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.abbreviate(null, 10, 20));
    }

    @Test
    void abbreviateOffsetMinLength() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.abbreviate("This is a longtext", 10, 3));
    }

    @Test
    void abbreviateOffset() {
        assertEquals("...is a...", StringUtils.abbreviate("This is a longtext", 5, 10));

        assertEquals("This is a longtext", StringUtils.abbreviate("This is a longtext", 10, 20));

        assertEquals("This is a longtext", StringUtils.abbreviate("This is a longtext", 50, 20));
    }

    @Test
    void addAndDeHumpNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.addAndDeHump(null));
    }

    @Test
    void addAndDeHump() {
        assertEquals("lalala", StringUtils.addAndDeHump("lalala"));

        assertEquals("la-la-la", StringUtils.addAndDeHump("LaLaLa"));

        assertEquals("a-l-l-u-p-p-e-r", StringUtils.addAndDeHump("ALLUPPER"));
    }

    @Test
    void capitalise() {
        assertNull(StringUtils.capitalise(null));

        assertEquals("StartBig", StringUtils.capitalise("startBig"));
    }

    @Test
    void capitaliseAllWords() {
        assertNull(StringUtils.capitaliseAllWords(null));

        assertEquals("Start All Big", StringUtils.capitaliseAllWords("start all big"));
    }

    @Test
    void capitalizeFirstLetterNPE() {
        assertThrows(NullPointerException.class, () -> assertNull(StringUtils.capitalizeFirstLetter(null)));
    }

    @Test
    void capitalizeFirstLetter() {
        assertEquals("Dings", StringUtils.capitalizeFirstLetter("Dings"));

        assertEquals("  dings", StringUtils.capitalizeFirstLetter("  dings"));

        assertEquals("Start all big", StringUtils.capitalizeFirstLetter("start all big"));
    }

    @Test
    void centerNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.center(null, 20));
    }

    @Test
    void center() {
        assertEquals("      centerMe      ", StringUtils.center("centerMe", 20));

        assertEquals("centerMe", StringUtils.center("centerMe", 4));

        assertEquals("          centerMe  ", StringUtils.center("        centerMe", 20));
    }

    @Test
    void centerDelimNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.center(null, 20, "*"));
    }

    @Test
    void centerDelim() {
        assertEquals("******centerMe******", StringUtils.center("centerMe", 20, "*"));

        assertEquals("centerMe", StringUtils.center("centerMe", 4, "*"));

        assertEquals("**        centerMe**", StringUtils.center("        centerMe", 20, "*"));
    }

    @Test
    void chompNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chomp(null));
    }

    @Test
    void chomp() {
        assertEquals("dings", StringUtils.chomp("dings"));

        assertEquals("dings", StringUtils.chomp("dings\n"));

        assertEquals("dings", StringUtils.chomp("dings\nbums"));

        assertEquals("dings\nbums", StringUtils.chomp("dings\nbums\ndongs"));
    }

    @Test
    void chompDelimNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chomp(null, "+"));
    }

    @Test
    void chompDelim() {
        assertEquals("dings", StringUtils.chomp("dings", "+"));

        assertEquals("dings", StringUtils.chomp("dings+", "+"));

        assertEquals("dings", StringUtils.chomp("dings+bums", "+"));

        assertEquals("dings+bums", StringUtils.chomp("dings+bums+dongs", "+"));
    }

    @Test
    void chompLastNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chompLast(null));
    }

    @Test
    void chompLast() {
        assertEquals("dings", StringUtils.chompLast("dings"));

        assertEquals("", StringUtils.chompLast("\n"));

        assertEquals("dings", StringUtils.chompLast("dings\n"));

        assertEquals("dings\nbums", StringUtils.chompLast("dings\nbums"));

        assertEquals("dings\nbums\ndongs", StringUtils.chompLast("dings\nbums\ndongs\n"));
    }

    @Test
    void chompLastDelimNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chompLast(null, "+"));
    }

    @Test
    void chompLastDelim() {
        assertEquals("dings", StringUtils.chompLast("dings", "+"));

        assertEquals("", StringUtils.chompLast("+", "+"));

        assertEquals("dings", StringUtils.chompLast("dings+", "+"));

        assertEquals("dings+bums", StringUtils.chompLast("dings+bums", "+"));

        assertEquals("dings+bums+dongs", StringUtils.chompLast("dings+bums+dongs+", "+"));
    }

    @Test
    void chompLastSepLongerThanStr() {
        assertEquals("x", StringUtils.chompLast("x", "ab"));
    }

    @Test
    void chopNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chop(null));
    }

    @Test
    void chop() {
        assertEquals("ding", StringUtils.chop("dings"));

        assertEquals("", StringUtils.chop("x"));

        assertEquals("dings", StringUtils.chop("dings\n"));

        assertEquals("dings", StringUtils.chop("dings\r\n"));

        assertEquals("dings\n", StringUtils.chop("dings\n\r"));
    }

    @Test
    void chopNewlineNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chopNewline(null));
    }

    @Test
    void chopNewline() {
        assertEquals("dings", StringUtils.chopNewline("dings"));

        assertEquals("x", StringUtils.chopNewline("x"));

        assertEquals("dings", StringUtils.chopNewline("dings\n"));

        assertEquals("dings", StringUtils.chopNewline("dings\r\n"));

        assertEquals("dings\n\r", StringUtils.chopNewline("dings\n\r"));
    }

    @Test
    void chopNewlineEmptyString() {
        assertEquals("", StringUtils.chopNewline(""));
    }

    @Test
    void clean() {
        assertEquals("", StringUtils.clean(null));

        assertEquals("", StringUtils.clean("   "));

        assertEquals("c", StringUtils.clean("  c "));

        assertEquals("dings", StringUtils.clean("  dings \n  "));
    }

    @Test
    void concatenateNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.concatenate((Object) null));
    }

    @Test
    void concatenate() {
        assertEquals("", StringUtils.concatenate());

        assertEquals("x", StringUtils.concatenate("x"));

        assertEquals("xyz", StringUtils.concatenate("x", "y", "z"));
    }

    @SuppressWarnings("ConstantValue")
    @Test
    void containsString() {
        assertFalse(StringUtils.contains(null, null));

        assertFalse(StringUtils.contains(null, "string"));

        assertFalse(StringUtils.contains("string", null));

        assertTrue(StringUtils.contains("string", ""));

        assertTrue(StringUtils.contains("string", "in"));

        assertFalse(StringUtils.contains("string", "IN"));
    }

    @Test
    void containsChar() {
        assertFalse(StringUtils.contains(null, 'c'));

        assertFalse(StringUtils.contains("string", "c"));

        assertTrue(StringUtils.contains("string", ""));

        assertTrue(StringUtils.contains("string", "r"));

        assertFalse(StringUtils.contains("string", "R"));
    }

    @Test
    void countMatchesNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.countMatches(null, null));
    }

    @Test
    void countMatchesNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.countMatches("this is it", null));
    }

    @Test
    void countMatches() {
        assertEquals(0, StringUtils.countMatches(null, "is"));

        assertEquals(2, StringUtils.countMatches("this is it", "is"));

        assertEquals(0, StringUtils.countMatches("this is it", "notincluded"));
    }

    @Test
    void defaultString() {
        assertEquals("", StringUtils.defaultString(null));

        assertEquals("dings", StringUtils.defaultString("dings"));
    }

    @Test
    void defaultStringDefaultValue() {
        assertEquals("defaultValue", StringUtils.defaultString(null, "defaultValue"));

        assertEquals("dings", StringUtils.defaultString("dings", "defaultValue"));
    }

    @Test
    void deleteWhitespaceNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.deleteWhitespace(null));
    }

    @Test
    void deleteWhitespace() {
        assertEquals("", StringUtils.deleteWhitespace(" \t  \n"));

        assertEquals("\b", StringUtils.deleteWhitespace(" \t  \b \n"));

        assertEquals("dings", StringUtils.deleteWhitespace("dings"));

        assertEquals("dings", StringUtils.deleteWhitespace("\n  dings \t "));
    }

    @Test
    void differenceNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.difference(null, null));
    }

    @Test
    void differenceNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.difference(null, "another"));
    }

    @Test
    void differenceNPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.difference("this", null));
    }

    @Test
    void difference() {
        assertEquals("another", StringUtils.difference("this", "another"));

        assertEquals("a robot", StringUtils.difference("I am human", "I am a robot"));

        assertEquals("AM a robot", StringUtils.difference("I am human", "I AM a robot"));
    }

    @Test
    void differenceAtNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.differenceAt(null, null));
    }

    @Test
    void differenceAtNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.differenceAt("test", null));
    }

    @Test
    void differenceAtNPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.differenceAt(null, "test"));
    }

    @Test
    void differenceAt() {
        assertEquals(0, StringUtils.differenceAt("this", "another"));

        assertEquals(5, StringUtils.differenceAt("I am human", "I am a robot"));

        assertEquals(2, StringUtils.differenceAt("I am human", "I AM a robot"));
    }

    @Test
    void endsWithIgnoreCase() {
        assertFalse(StringUtils.endsWithIgnoreCase(null, null));

        assertFalse(StringUtils.endsWithIgnoreCase(null, "string"));

        assertFalse(StringUtils.endsWithIgnoreCase("string", null));

        assertTrue(StringUtils.endsWithIgnoreCase("string", "ing"));

        assertFalse(StringUtils.endsWithIgnoreCase("string", "a string"));

        assertFalse(StringUtils.endsWithIgnoreCase("string", "str"));
    }

    @SuppressWarnings("ConstantValue")
    @Test
    void equals() {
        assertTrue(StringUtils.equals(null, null));

        assertFalse(StringUtils.equals("x", null));

        assertFalse(StringUtils.equals(null, "x"));

        assertFalse(StringUtils.equals("X", "x"));

        assertTrue(StringUtils.equals("dings", "dings"));
    }

    @SuppressWarnings("ConstantValue")
    @Test
    void equalsIgnoreCase() {
        assertTrue(StringUtils.equalsIgnoreCase(null, null));

        assertFalse(StringUtils.equalsIgnoreCase("x", null));

        assertFalse(StringUtils.equalsIgnoreCase(null, "x"));

        assertTrue(StringUtils.equalsIgnoreCase("X", "x"));

        assertTrue(StringUtils.equalsIgnoreCase("dings", "dings"));

        assertTrue(StringUtils.equalsIgnoreCase("dings", "diNGs"));
    }

    @Test
    void escapeNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.escape(null));
    }

    @Test
    void escape() {
        assertEquals("dings", StringUtils.escape("dings"));

        assertEquals("dings\\tbums", StringUtils.escape("dings\tbums"));

        assertEquals("dings\\nbums", StringUtils.escape("dings\nbums"));
    }

    @Test
    void escape2() {
        assertNull(StringUtils.escape(null, null, '#'));

        assertEquals("dings", StringUtils.escape("dings", new char[] {'\t', '\b'}, '+'));

        assertEquals("dings+\tbums", StringUtils.escape("dings\tbums", new char[] {'\t', '\b'}, '+'));

        assertEquals("dings\nbums", StringUtils.escape("dings\nbums", new char[] {'\t', '\b'}, '+'));
        assertEquals("dings+\bbums", StringUtils.escape("dings\bbums", new char[] {'\t', '\b'}, '+'));
    }

    @Test
    void getChompNPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.getChomp(null, null));
    }

    @Test
    void getChompNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.getChomp("dings", null));
    }

    @Test
    void getChompNPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.getChomp(null, "dings"));
    }

    @Test
    void getChomp() {
        assertEquals("-bums", StringUtils.getChomp("dings-bums", "-"));

        assertEquals("-", StringUtils.getChomp("dings-", "-"));

        assertEquals("", StringUtils.getChomp("dingsbums", "-"));
    }

    @Test
    void getNestedStringNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.getNestedString("  +dings+ ", null));
    }

    @Test
    void getNestedString() {
        assertNull(StringUtils.getNestedString(null, null));

        assertEquals("dings", StringUtils.getNestedString("  +dings+ ", "+"));

        assertNull(StringUtils.getNestedString("  +dings+ ", "not"));
    }

    @Test
    void getNestedString2NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.getNestedString("  +dings+ ", null, null));
    }

    @Test
    void getNestedString2NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.getNestedString("  +dings+ ", null, "neither"));
    }

    @Test
    void getNestedString2() {
        assertNull(StringUtils.getNestedString(null, null, null));

        assertNull(StringUtils.getNestedString("  +dings+ ", "not", null));

        assertEquals("dings", StringUtils.getNestedString("  +dings- ", "+", "-"));

        assertNull(StringUtils.getNestedString("  +dings+ ", "not", "neither"));
    }

    @Test
    void getPrechompNPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.getPrechomp(null, null));
    }

    @Test
    void getPrechompNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.getPrechomp(null, "bums"));
    }

    @Test
    void getPrechomp() {
        assertEquals("dings bums", StringUtils.getPrechomp("dings bums dongs", "bums"));

        assertEquals("", StringUtils.getPrechomp("dings bums dongs", "non"));
    }

    @SuppressWarnings("RedundantArrayCreation")
    @Test
    void indexOfAny() {
        assertEquals(-1, StringUtils.indexOfAny(null, (String[]) null));

        assertEquals(-1, StringUtils.indexOfAny("dings", (String[]) null));

        assertEquals(-1, StringUtils.indexOfAny(null, new String[] {}));

        assertEquals(6, StringUtils.indexOfAny("dings bums dongs", new String[] {"knuff", "bums"}));
    }

    @Test
    void interpolateNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.interpolate(null, null));
    }

    @Test
    void interpolateNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.interpolate("This ${text} will get replaced", null));
    }

    @Test
    void interpolate() {
        Map<String, String> variables = new HashMap<>();
        assertEquals(
                "This ${text} will get replaced", StringUtils.interpolate("This ${text} will get replaced", variables));

        variables.put("text", "with a special content");

        assertEquals(
                "This with a special content will get replaced",
                StringUtils.interpolate("This ${text} will get replaced", variables));
    }

    @Test
    void isAlpha() {
        assertFalse(StringUtils.isAlpha(null));

        assertFalse(StringUtils.isAlpha("2"));

        assertTrue(StringUtils.isAlpha("asvsdfSDF"));

        assertFalse(StringUtils.isAlpha("asvsdfSDF \t "));

        assertFalse(StringUtils.isAlpha("435afsafd3!"));
    }

    @Test
    void isAlphaSpace() {
        assertFalse(StringUtils.isAlphaSpace(null));

        assertFalse(StringUtils.isAlphaSpace("2"));

        assertTrue(StringUtils.isAlphaSpace("asvsdfSDF"));

        assertTrue(StringUtils.isAlphaSpace("asvsdfSDF  "));

        assertFalse(StringUtils.isAlphaSpace("asvsdfSDF \t "));

        assertFalse(StringUtils.isAlphaSpace("435afsafd3!"));
    }

    @Test
    void isAlphanumeric() {
        assertFalse(StringUtils.isAlphanumeric(null));

        assertTrue(StringUtils.isAlphanumeric("2"));

        assertTrue(StringUtils.isAlphanumeric("asvsdfSDF"));

        assertFalse(StringUtils.isAlphanumeric("asvsdfSDF  "));

        assertFalse(StringUtils.isAlphanumeric("asvsdfSDF \t "));

        assertFalse(StringUtils.isAlphanumeric("435afsafd3!"));

        assertTrue(StringUtils.isAlphanumeric("435afsafd3"));

        assertFalse(StringUtils.isAlphanumeric("435 "));

        assertTrue(StringUtils.isAlphanumeric("435"));
    }

    @Test
    void isAlphanumericSpace() {
        assertFalse(StringUtils.isAlphanumericSpace(null));

        assertTrue(StringUtils.isAlphanumericSpace("2"));

        assertTrue(StringUtils.isAlphanumericSpace("asvsdfSDF"));

        assertTrue(StringUtils.isAlphanumericSpace("asvsdfSDF  "));

        assertFalse(StringUtils.isAlphanumericSpace("asvsdfSDF \t "));

        assertFalse(StringUtils.isAlphanumericSpace("435afsafd3!"));

        assertTrue(StringUtils.isAlphanumericSpace("435afsafd3"));

        assertTrue(StringUtils.isAlphanumericSpace("435 "));

        assertTrue(StringUtils.isAlphanumericSpace("435"));
    }

    @Test
    void isBlank() {
        assertTrue(StringUtils.isBlank(null));

        assertFalse(StringUtils.isBlank("xx"));

        assertFalse(StringUtils.isBlank("xx "));

        assertTrue(StringUtils.isBlank("  "));

        assertTrue(StringUtils.isBlank("  \t "));

        assertTrue(StringUtils.isBlank("  \n "));
    }

    @Test
    void empty() {
        assertTrue(StringUtils.isEmpty(null));

        assertFalse(StringUtils.isEmpty("xx"));

        assertFalse(StringUtils.isEmpty("xx "));

        assertTrue(StringUtils.isEmpty("  "));

        assertTrue(StringUtils.isEmpty("  \t "));

        assertTrue(StringUtils.isEmpty("  \n "));
    }

    @Test
    void notBlank() {
        assertFalse(StringUtils.isNotBlank(null));

        assertTrue(StringUtils.isNotBlank("xx"));

        assertTrue(StringUtils.isNotBlank("xx "));

        assertFalse(StringUtils.isNotBlank("  "));

        assertFalse(StringUtils.isNotBlank("  \t "));

        assertFalse(StringUtils.isNotBlank("  \n "));
    }

    @SuppressWarnings("ConstantValue")
    @Test
    void notEmpty() {
        assertFalse(StringUtils.isNotEmpty(null));

        assertTrue(StringUtils.isNotEmpty("xx"));

        assertTrue(StringUtils.isNotEmpty("xx "));

        assertTrue(StringUtils.isNotEmpty("  "));

        assertFalse(StringUtils.isNotEmpty(""));

        assertTrue(StringUtils.isNotEmpty("  \t "));

        assertTrue(StringUtils.isNotEmpty("  \n "));
    }

    @Test
    void isNumeric() {
        assertFalse(StringUtils.isNumeric(null));

        assertTrue(StringUtils.isNumeric("2"));

        assertFalse(StringUtils.isNumeric("asvsdfSDF"));

        assertFalse(StringUtils.isNumeric("asvsdfSDF  "));

        assertFalse(StringUtils.isNumeric("asvsdfSDF \t "));

        assertFalse(StringUtils.isNumeric("435afsafd3!"));

        assertFalse(StringUtils.isNumeric("435afsafd3"));

        assertFalse(StringUtils.isNumeric("435 "));

        assertTrue(StringUtils.isNumeric("435"));
    }

    @Test
    void isWhitespace() {
        assertFalse(StringUtils.isWhitespace(null));

        assertFalse(StringUtils.isWhitespace("xx"));

        assertFalse(StringUtils.isWhitespace("xx "));

        assertTrue(StringUtils.isWhitespace("  "));

        assertTrue(StringUtils.isWhitespace(""));

        assertTrue(StringUtils.isWhitespace("  \t "));

        assertTrue(StringUtils.isWhitespace("  \n "));
    }

    @Test
    void joinArrayNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.join((Object[]) null, null));
    }

    @Test
    void joinArray() {
        assertEquals("", StringUtils.join(new Object[0], null));

        assertEquals("abc", StringUtils.join(new Object[] {"a", "b", "c"}, null));

        assertEquals("a__b__c", StringUtils.join(new Object[] {"a", "b", "c"}, "__"));
    }

    @Test
    void joinIteratorNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.join((Iterator<?>) null, null));
    }

    @Test
    void joinIterator() {
        ArrayList<String> list = new ArrayList<>();

        //noinspection RedundantOperationOnEmptyContainer
        assertEquals("", StringUtils.join(list.iterator(), null));

        list.add("a");
        list.add("b");
        list.add("c");

        assertEquals("abc", StringUtils.join(list.iterator(), null));

        assertEquals("a__b__c", StringUtils.join(list.iterator(), "__"));
    }

    @Test
    void lastIndexOfAny() {
        assertEquals(-1, StringUtils.lastIndexOfAny(null, (String[]) null));

        assertEquals(-1, StringUtils.lastIndexOfAny("dings", (String[]) null));

        assertEquals(13, StringUtils.lastIndexOfAny("dings bums boms", new String[] {"ms", " b"}));

        assertEquals(-1, StringUtils.lastIndexOfAny("dings bums boms", new String[] {"nix", "da"}));
    }

    @Test
    void leftIAE() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.left(null, -1));
    }

    @Test
    void left() {
        assertNull(StringUtils.left(null, 4));

        assertEquals("ding", StringUtils.left("dingsbums", 4));

        assertEquals("dingsbums", StringUtils.left("dingsbums", 40));

        assertEquals("", StringUtils.left("dingsbums", 0));
    }

    @Test
    void leftPad1NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.leftPad(null, 0));
    }

    @Test
    void leftPad1() {
        assertEquals("dings", StringUtils.leftPad("dings", 0));

        assertEquals("dings", StringUtils.leftPad("dings", 2));

        assertEquals("     dings", StringUtils.leftPad("dings", 10));
    }

    @Test
    void leftPad2NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.leftPad(null, 0, null));
    }

    @Test
    void leftPad2NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.leftPad("dings", 0, null));
    }

    @Test
    void leftPad2NPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.leftPad(null, 0, "*"));
    }

    @Test
    void leftPad2() {
        assertEquals("dings", StringUtils.leftPad("dings", 0, "*"));

        assertEquals("dings", StringUtils.leftPad("dings", 2, "*"));

        assertEquals("*****dings", StringUtils.leftPad("dings", 10, "*"));
    }

    @Test
    void leftPadEmptyDelim() {
        assertEquals("dings", StringUtils.leftPad("dings", 10, ""));
    }

    @SuppressWarnings("ConstantValue")
    @Test
    void lowerCase() {
        assertNull(StringUtils.lowerCase(null));

        assertEquals("dingsbums", StringUtils.lowerCase("dinGSbuMS"));

        assertEquals("", StringUtils.lowerCase(""));
    }

    @Test
    void lowerCaseFirstLetterNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.lowercaseFirstLetter(null));
    }

    @Test
    void lowerCaseFirstLetter() {
        assertEquals("dings Bums", StringUtils.lowercaseFirstLetter("Dings Bums"));
    }

    @Test
    void midNegativeLen() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.mid(null, 0, -2));
    }

    @Test
    void midWrongPos() {
        assertThrows(IndexOutOfBoundsException.class, () -> StringUtils.mid(null, -2, 3));
    }

    @Test
    void mid() {
        assertNull(StringUtils.mid(null, 0, 0));

        assertEquals("", StringUtils.mid("dings bums", 0, 0));

        assertEquals("gs b", StringUtils.mid("dings bums", 3, 4));
    }

    @Test
    void overlayStringNPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.overlayString(null, null, 0, 0));
    }

    @Test
    void overlayStringNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.overlayString("dings", null, 0, 0));
    }

    @Test
    void overlayStringNPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.overlayString(null, "bums", 0, 0));
    }

    @Test
    void overlayString() {
        assertEquals("bumsdings", StringUtils.overlayString("dings", "bums", 0, 0));

        assertEquals("dibumss", StringUtils.overlayString("dings", "bums", 2, 4));
    }

    @Test
    void prechompNPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.prechomp(null, null));
    }

    @Test
    void prechompNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.prechomp("dings", null));
    }

    @Test
    void prechompNPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.prechomp(null, "bums"));
    }

    @Test
    void prechomp() {
        assertEquals("bums", StringUtils.prechomp("dings bums", " "));

        assertEquals("dings bums", StringUtils.prechomp("dings bums", "nix"));
    }

    @Test
    void quoteAndEscape1() {
        assertNull(StringUtils.quoteAndEscape(null, '+'));

        assertEquals("", StringUtils.quoteAndEscape("", '+'));

        assertEquals("abc", StringUtils.quoteAndEscape("abc", '"'));

        assertEquals("\"a\\\"bc\"", StringUtils.quoteAndEscape("a\"bc", '"'));

        assertEquals("'a\\'bc'", StringUtils.quoteAndEscape("a'bc", '\''));

        assertEquals("a\"bc", StringUtils.quoteAndEscape("a\"bc", '\''));
    }

    @Test
    void quoteAndEscape2() {
        assertNull(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}));

        assertEquals("", StringUtils.quoteAndEscape("", '+', new char[] {'"'}));

        assertEquals("abc", StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}));

        assertEquals("\"a\\\"bc\"", StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}));

        assertEquals("'a\\'bc'", StringUtils.quoteAndEscape("a'bc", '\'', new char[] {'"'}));

        assertEquals("a\"bc", StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}));

        assertEquals("'a\"bc'", StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\'', '"'}));
    }

    @Test
    void quoteAndEscape3() {
        assertNull(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}, '\\', false));

        assertEquals("", StringUtils.quoteAndEscape("", '+', new char[] {'"'}, '\\', false));

        assertEquals("abc", StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}, '\\', false));

        assertEquals("\"a\\\"bc\"", StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}, '\\', false));

        assertEquals("a'bc", StringUtils.quoteAndEscape("a'bc", '\'', new char[] {'"'}, '\\', false));

        assertEquals("a\"bc", StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}, '\\', false));

        assertEquals("'a\\\"bc'", StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\'', '"'}, '\\', false));

        // with force flag
        assertNull(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}, '\\', true));

        assertEquals("++", StringUtils.quoteAndEscape("", '+', new char[] {'"'}, '\\', true));

        assertEquals("\"abc\"", StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}, '\\', true));

        assertEquals("\"a\\\"bc\"", StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}, '\\', true));

        assertEquals("'a'bc'", StringUtils.quoteAndEscape("a'bc", '\'', new char[] {'"'}, '\\', true));

        assertEquals("'a\"bc'", StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}, '\\', true));

        assertEquals("'a\\\"bc'", StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\'', '"'}, '\\', true));
    }

    @Test
    void quoteAndEscape4() {
        assertNull(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}, new char[] {'"'}, '\\', false));

        assertEquals("", StringUtils.quoteAndEscape("", '+', new char[] {'"'}, new char[] {'"'}, '\\', false));

        assertEquals("abc", StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}, new char[] {'"'}, '\\', false));

        assertEquals(
                "\"a\\\"bc\"",
                StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}, new char[] {'"'}, '\\', false));

        assertEquals("a'bc", StringUtils.quoteAndEscape("a'bc", '\'', new char[] {'"'}, new char[] {'"'}, '\\', false));

        assertEquals(
                "'a\"bc'", StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}, new char[] {'"'}, '\\', false));

        assertEquals(
                "'a\"bc'",
                StringUtils.quoteAndEscape("'a\"bc'", '\'', new char[] {'\'', '"'}, new char[] {'"'}, '\\', false));

        // with force flag
        assertNull(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}, new char[] {'"'}, '\\', true));

        assertEquals("++", StringUtils.quoteAndEscape("", '+', new char[] {'"'}, new char[] {'"'}, '\\', true));

        assertEquals("\"abc\"", StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}, new char[] {'"'}, '\\', true));

        assertEquals(
                "\"a\\\"bc\"",
                StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}, new char[] {'"'}, '\\', true));

        assertEquals(
                "'a'bc'", StringUtils.quoteAndEscape("a'bc", '\'', new char[] {'"'}, new char[] {'"'}, '\\', true));

        assertEquals(
                "'a\"bc'", StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}, new char[] {'"'}, '\\', true));

        assertEquals(
                "'a\\\"bc'",
                StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\'', '"'}, new char[] {'"'}, '\\', true));
    }

    @Test
    void removeAndHumpNPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.removeAndHump(null, null));
    }

    @Test
    void removeAndHumpNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.removeAndHump("dings", null));
    }

    @Test
    void removeAndHumpNPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.removeAndHump(null, "bums"));
    }

    @Test
    void removeAndHump() {
        assertEquals("Ding", StringUtils.removeAndHump("dings", "bums"));

        assertEquals("ThisIsIt", StringUtils.removeAndHump("this-is-it", "-"));

        assertEquals("THISISIT", StringUtils.removeAndHump("THIS-IS-IT", "-"));
    }

    @Test
    void removeDuplicateWhitespaceNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.removeDuplicateWhitespace(null));
    }

    @Test
    void removeDuplicateWhitespace() {
        assertEquals("dings", StringUtils.removeDuplicateWhitespace("dings"));

        assertEquals("dings bums", StringUtils.removeDuplicateWhitespace("dings bums"));

        assertEquals("dings bums", StringUtils.removeDuplicateWhitespace("dings  bums"));

        assertEquals("dings bums", StringUtils.removeDuplicateWhitespace("dings \t bums"));
    }

    @Test
    void repeatNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.repeat(null, 0));
    }

    @Test
    void repeatNegativeAmount() {
        assertThrows(NegativeArraySizeException.class, () -> StringUtils.repeat("dings", -1));
    }

    @Test
    void repeat() {
        assertEquals("", StringUtils.repeat("dings", 0));

        assertEquals("dings", StringUtils.repeat("dings", 1));

        assertEquals("dingsdingsdings", StringUtils.repeat("dings", 3));
    }

    @Test
    void replaceChar() {
        assertNull(StringUtils.replace(null, 'i', 'o'));

        assertEquals("dongs", StringUtils.replace("dings", 'i', 'o'));

        assertEquals("dongsboms", StringUtils.replace("dingsbims", 'i', 'o'));

        assertEquals("dings", StringUtils.replace("dings", 'x', 'o'));
    }

    @Test
    void replace2CharMax() {
        assertNull(StringUtils.replace(null, 'i', 'o', 0));

        assertEquals("dongsobumso", StringUtils.replace("dingsibumsi", 'i', 'o', 3));

        assertEquals("dongsobumsi", StringUtils.replace("dingsibumsi", 'i', 'o', 2));

        assertEquals("dongsobumso", StringUtils.replace("dingsibumsi", 'i', 'o', 0));

        assertEquals("dongsobumso", StringUtils.replace("dingsibumsi", 'i', 'o', -2));

        assertEquals("dings", StringUtils.replace("dings", 'x', 'o', 2));
    }

    @Test
    void replaceString() {
        assertNull(StringUtils.replace(null, "in", "ox"));

        assertEquals("doxgs", StringUtils.replace("dings", "in", "ox"));

        assertEquals("doxgsboxs", StringUtils.replace("dingsbins", "in", "ox"));

        assertEquals("dings", StringUtils.replace("dings", "nin", "ox"));
    }

    @Test
    void replace2StringMax() {
        assertNull(StringUtils.replace(null, "in", "ox", 0));

        assertEquals("dingxobumxo", StringUtils.replace("dingsibumsi", "si", "xo", 3));

        assertEquals("dingxobumxo", StringUtils.replace("dingsibumsi", "si", "xo", 2));

        assertEquals("dingxobumsi", StringUtils.replace("dingsibumsi", "si", "xo", 1));

        assertEquals("dingxobumxo", StringUtils.replace("dingsibumsi", "si", "xo", 0));

        assertEquals("dingxobumxo", StringUtils.replace("dingsibumsi", "si", "xo", -2));

        assertEquals("dings", StringUtils.replace("dings", "si", "xo", 2));
    }

    @Test
    void replaceOnceChar() {
        assertNull(StringUtils.replaceOnce(null, 'i', 'o'));

        assertEquals("dongsibumsi", StringUtils.replaceOnce("dingsibumsi", 'i', 'o'));

        assertEquals("dings", StringUtils.replaceOnce("dings", 'x', 'o'));
    }

    @Test
    void replaceOnceString() {
        assertNull(StringUtils.replaceOnce(null, "in", "ox"));

        assertEquals("dingxobumsi", StringUtils.replaceOnce("dingsibumsi", "si", "xo"));

        assertEquals("dings", StringUtils.replaceOnce("dings", "si", "xo"));
    }

    @Test
    void reverse() {
        assertNull(StringUtils.reverse(null));

        assertEquals("", StringUtils.reverse(""));

        assertEquals("sgnid", StringUtils.reverse("dings"));

        assertEquals(" sgnid  ", StringUtils.reverse("  dings "));
    }

    @Test
    void reverseDelimitedStringNPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.reverseDelimitedString(null, null));
    }

    @Test
    void reverseDelimitedStringNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.reverseDelimitedString(null, " "));
    }

    @Test
    void reverseDelimitedString() {
        assertEquals("dings", StringUtils.reverseDelimitedString("dings", null));

        assertEquals("", StringUtils.reverseDelimitedString("", " "));

        assertEquals("dings", StringUtils.reverseDelimitedString("dings", " "));

        assertEquals("dings", StringUtils.reverseDelimitedString("  dings ", " "));

        assertEquals("bums dings", StringUtils.reverseDelimitedString("dings bums", " "));
    }

    @Test
    void rightIAE1() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.right(null, -1));
    }

    @Test
    void rightIAE2() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.right("dings", -1));
    }

    @Test
    void right() {
        assertNull(StringUtils.right(null, 0));

        assertEquals("", StringUtils.right("dings", 0));

        assertEquals("ngs", StringUtils.right("dings", 3));

        assertEquals("gs ", StringUtils.right("dings ", 3));
    }

    @Test
    void rightPad1NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.rightPad(null, 0));
    }

    @Test
    void rightPad1() {
        assertEquals("dings", StringUtils.rightPad("dings", 0));

        assertEquals("dings", StringUtils.rightPad("dings", 3));

        assertEquals("dings     ", StringUtils.rightPad("dings", 10));
    }

    @Test
    void rightPad2NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.rightPad(null, 0, null));
    }

    @Test
    void rightPad2NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.rightPad("dings", 0, null));
    }

    @Test
    void rightPad2NPE23() {
        assertThrows(NullPointerException.class, () -> StringUtils.rightPad(null, 0, "+"));
    }

    @Test
    void rightPad2() {
        assertEquals("dings", StringUtils.rightPad("dings", 0, "+"));

        assertEquals("dings", StringUtils.rightPad("dings", 3, "+"));

        assertEquals("dings+++++", StringUtils.rightPad("dings", 10, "+"));
    }

    @Test
    void rightPadEmptyDelim() {
        assertEquals("dings", StringUtils.rightPad("dings", 10, ""));
    }

    @Test
    void split1NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null));
    }

    @Test
    void split1() {
        assertArrayEquals(new String[] {"dings"}, StringUtils.split("dings"));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings bums"));
    }

    @Test
    void split2NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null, null));
    }

    @Test
    void split2NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null, " "));
    }

    @Test
    void split2() {
        assertArrayEquals(new String[] {"dings"}, StringUtils.split("dings", null));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings bums", null));

        assertArrayEquals(new String[] {"dings"}, StringUtils.split("dings", "+"));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings+bums", "+"));
    }

    @Test
    void split3NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null, null, 1));
    }

    @Test
    void split3NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null, " ", 1));
    }

    @Test
    void split3() {
        assertArrayEquals(new String[] {"dings"}, StringUtils.split("dings", null, 3));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings bums", null, 3));

        assertArrayEquals(new String[] {"dings"}, StringUtils.split("dings", "+", 3));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings+bums", "+", 3));

        assertArrayEquals(new String[] {"dings+bums"}, StringUtils.split("dings+bums", "+", 1));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings+bums", "+", 0));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings+bums", "+", -5));
    }

    @Test
    void strip1() {
        assertNull(StringUtils.strip(null));

        assertEquals("dings", StringUtils.strip("dings"));

        assertEquals("dings", StringUtils.strip("  dings \t "));
    }

    @Test
    void strip2() {
        assertNull(StringUtils.strip(null, " "));

        assertNull(StringUtils.strip(null, null));

        assertEquals("dings", StringUtils.strip("dings", " "));

        assertEquals("dings \t", StringUtils.strip("  dings \t ", " "));
    }

    @Test
    void stripAll1() {
        assertNull(StringUtils.stripAll((String[]) null));

        assertArrayEquals(new String[] {}, StringUtils.stripAll());

        assertArrayEquals(new String[] {"dings"}, StringUtils.stripAll(new String[] {"dings"}));

        assertArrayEquals(
                new String[] {"dings", "bums"}, StringUtils.stripAll(new String[] {" dings ", "  bums \t  "}));
    }

    @Test
    void stripAll2() {
        assertNull(StringUtils.stripAll(null, " "));

        assertArrayEquals(new String[] {}, StringUtils.stripAll(new String[] {}, " "));

        assertArrayEquals(new String[] {"dings"}, StringUtils.stripAll(new String[] {"dings"}, " "));

        assertArrayEquals(
                new String[] {"dings", "bums \t"}, StringUtils.stripAll(new String[] {" dings ", "  bums \t  "}, " "));
    }

    @Test
    void stripEnd() {
        assertNull(StringUtils.stripEnd(null, null));

        assertEquals("dings", StringUtils.stripEnd("dings", null));

        assertEquals("  dings", StringUtils.stripEnd("  dings \t ", null));

        assertNull(StringUtils.stripEnd(null, " "));

        assertEquals("dings", StringUtils.stripEnd("dings", " "));

        assertEquals("  dings \t", StringUtils.stripEnd("  dings \t ", " "));
    }

    @Test
    void stripStart() {
        assertNull(StringUtils.stripStart(null, null));

        assertEquals("dings", StringUtils.stripStart("dings", null));

        assertEquals("dings \t ", StringUtils.stripStart("  dings \t ", null));

        assertNull(StringUtils.stripStart(null, " "));

        assertEquals("dings", StringUtils.stripStart("dings", " "));

        assertEquals("\t dings \t ", StringUtils.stripStart("  \t dings \t ", " "));
    }

    @Test
    void substring1() {
        assertNull(StringUtils.substring(null, 0));
        assertNull(StringUtils.substring(null, -3));

        assertEquals("ngs", StringUtils.substring("dings", 2));

        assertEquals("gs", StringUtils.substring("dings", -2));

        assertEquals("", StringUtils.substring("dings", 20));
    }

    @Test
    void substring2() {
        assertNull(StringUtils.substring(null, 0, 2));

        assertNull(StringUtils.substring(null, -3, 0));

        assertEquals("ng", StringUtils.substring("dings", 2, 4));

        assertEquals("g", StringUtils.substring("dings", -2, 4));

        assertEquals("", StringUtils.substring("dings", 20, 23));

        assertEquals("", StringUtils.substring("dings", 4, 2));
    }

    @Test
    void swapCase() {
        assertNull(StringUtils.swapCase(null));

        assertEquals("DINGS", StringUtils.swapCase("dings"));

        assertEquals("dINgS", StringUtils.swapCase("DinGs"));
    }

    @SuppressWarnings("ConstantValue")
    @Test
    void trim() {
        assertNull(StringUtils.trim(null));

        assertEquals("", StringUtils.trim("   "));

        assertEquals("c", StringUtils.trim("  c "));

        assertEquals("dings", StringUtils.trim("  dings \n  "));
    }

    @Test
    void uncapitalise() {
        assertNull(StringUtils.uncapitalise(null));

        assertEquals("   ", StringUtils.uncapitalise("   "));

        assertEquals("dings", StringUtils.uncapitalise("dings"));

        assertEquals("dings", StringUtils.uncapitalise("Dings"));

        assertEquals("dINGS", StringUtils.uncapitalise("DINGS"));
    }

    @Test
    void uncapitaliseAllWords() {
        assertNull(StringUtils.uncapitaliseAllWords(null));

        assertEquals("   ", StringUtils.uncapitaliseAllWords("   "));

        assertEquals("dings bums", StringUtils.uncapitaliseAllWords("dings bums"));

        assertEquals("dings bums", StringUtils.uncapitaliseAllWords("Dings Bums"));

        assertEquals("dINGS bums", StringUtils.uncapitaliseAllWords("DINGS Bums"));
    }

    @Test
    void unifyLineSeparators1() {
        String sls = System.lineSeparator();

        assertNull(StringUtils.unifyLineSeparators(null));

        assertEquals("   ", StringUtils.unifyLineSeparators("   "));

        assertEquals("dings" + sls + "bums" + sls + "dongs", StringUtils.unifyLineSeparators("dings\nbums\r\ndongs"));
    }

    @Test
    void unifyLineSeparators2() {
        assertNull(StringUtils.unifyLineSeparators(null, "\n"));

        assertEquals("   ", StringUtils.unifyLineSeparators("   ", "\n"));

        assertEquals(
                "   " // takes the sytem line separator
                ,
                StringUtils.unifyLineSeparators("   ", null));

        assertEquals("dings\nbums\ndongs", StringUtils.unifyLineSeparators("dings\nbums\r\ndongs", "\n"));
    }

    @SuppressWarnings("ConstantValue")
    @Test
    void uppercase() {
        assertNull(StringUtils.upperCase(null));

        assertEquals("   ", StringUtils.upperCase("   "));

        assertEquals("", StringUtils.upperCase(""));

        assertEquals("DINGS", StringUtils.upperCase("dings"));
    }
}
