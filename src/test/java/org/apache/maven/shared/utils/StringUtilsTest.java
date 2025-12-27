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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the {@link StringUtils} class.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
@SuppressWarnings("deprecation")
public class StringUtilsTest {

    @Test
    public void testAbbreviateNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.abbreviate(null, 10));
    }

    @Test
    public void testAbbreviateMinLength() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.abbreviate("This is a longtext", 3));
    }

    @Test
    public void testAbbreviate() {
        assertEquals("This is...", StringUtils.abbreviate("This is a longtext", 10));

        assertEquals("This is a longtext", StringUtils.abbreviate("This is a longtext", 50));
    }

    @Test
    public void testAbbreviateOffsetNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.abbreviate(null, 10, 20));
    }

    @Test
    public void testAbbreviateOffsetMinLength() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.abbreviate("This is a longtext", 10, 3));
    }

    @Test
    public void testAbbreviateOffset() {
        assertEquals("...is a...", StringUtils.abbreviate("This is a longtext", 5, 10));

        assertEquals("This is a longtext", StringUtils.abbreviate("This is a longtext", 10, 20));

        assertEquals("This is a longtext", StringUtils.abbreviate("This is a longtext", 50, 20));
    }

    @Test
    public void testAddAndDeHumpNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.addAndDeHump(null));
    }

    @Test
    public void testAddAndDeHump() {
        assertEquals("lalala", StringUtils.addAndDeHump("lalala"));

        assertEquals("la-la-la", StringUtils.addAndDeHump("LaLaLa"));

        assertEquals("a-l-l-u-p-p-e-r", StringUtils.addAndDeHump("ALLUPPER"));
    }

    @Test
    public void testCapitalise() {
        assertNull(StringUtils.capitalise(null));

        assertEquals("StartBig", StringUtils.capitalise("startBig"));
    }

    @Test
    public void testCapitaliseAllWords() {
        assertNull(StringUtils.capitaliseAllWords(null));

        assertEquals("Start All Big", StringUtils.capitaliseAllWords("start all big"));
    }

    @Test
    public void testCapitalizeFirstLetterNPE() {
        assertThrows(NullPointerException.class, () -> assertNull(StringUtils.capitalizeFirstLetter(null)));
    }

    @Test
    public void testCapitalizeFirstLetter() {
        assertEquals("Dings", StringUtils.capitalizeFirstLetter("Dings"));

        assertEquals("  dings", StringUtils.capitalizeFirstLetter("  dings"));

        assertEquals("Start all big", StringUtils.capitalizeFirstLetter("start all big"));
    }

    @Test
    public void testCenterNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.center(null, 20));
    }

    @Test
    public void testCenter() {
        assertEquals("      centerMe      ", StringUtils.center("centerMe", 20));

        assertEquals("centerMe", StringUtils.center("centerMe", 4));

        assertEquals("          centerMe  ", StringUtils.center("        centerMe", 20));
    }

    @Test
    public void testCenterDelimNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.center(null, 20, "*"));
    }

    @Test
    public void testCenterDelim() {
        assertEquals("******centerMe******", StringUtils.center("centerMe", 20, "*"));

        assertEquals("centerMe", StringUtils.center("centerMe", 4, "*"));

        assertEquals("**        centerMe**", StringUtils.center("        centerMe", 20, "*"));
    }

    @Test
    public void testChompNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chomp(null));
    }

    @Test
    public void testChomp() {
        assertEquals("dings", StringUtils.chomp("dings"));

        assertEquals("dings", StringUtils.chomp("dings\n"));

        assertEquals("dings", StringUtils.chomp("dings\nbums"));

        assertEquals("dings\nbums", StringUtils.chomp("dings\nbums\ndongs"));
    }

    @Test
    public void testChompDelimNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chomp(null, "+"));
    }

    @Test
    public void testChompDelim() {
        assertEquals("dings", StringUtils.chomp("dings", "+"));

        assertEquals("dings", StringUtils.chomp("dings+", "+"));

        assertEquals("dings", StringUtils.chomp("dings+bums", "+"));

        assertEquals("dings+bums", StringUtils.chomp("dings+bums+dongs", "+"));
    }

    @Test
    public void testChompLastNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chompLast(null));
    }

    @Test
    public void testChompLast() {
        assertEquals("dings", StringUtils.chompLast("dings"));

        assertEquals("", StringUtils.chompLast("\n"));

        assertEquals("dings", StringUtils.chompLast("dings\n"));

        assertEquals("dings\nbums", StringUtils.chompLast("dings\nbums"));

        assertEquals("dings\nbums\ndongs", StringUtils.chompLast("dings\nbums\ndongs\n"));
    }

    @Test
    public void testChompLastDelimNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chompLast(null, "+"));
    }

    @Test
    public void testChompLastDelim() {
        assertEquals("dings", StringUtils.chompLast("dings", "+"));

        assertEquals("", StringUtils.chompLast("+", "+"));

        assertEquals("dings", StringUtils.chompLast("dings+", "+"));

        assertEquals("dings+bums", StringUtils.chompLast("dings+bums", "+"));

        assertEquals("dings+bums+dongs", StringUtils.chompLast("dings+bums+dongs+", "+"));
    }

    @Test
    public void testChopNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chop(null));
    }

    @Test
    public void testChop() {
        assertEquals("ding", StringUtils.chop("dings"));

        assertEquals("", StringUtils.chop("x"));

        assertEquals("dings", StringUtils.chop("dings\n"));

        assertEquals("dings", StringUtils.chop("dings\r\n"));

        assertEquals("dings\n", StringUtils.chop("dings\n\r"));
    }

    @Test
    public void testChopNewlineNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chopNewline(null));
    }

    @Test
    public void testChopNewline() {
        assertEquals("dings", StringUtils.chopNewline("dings"));

        assertEquals("x", StringUtils.chopNewline("x"));

        assertEquals("dings", StringUtils.chopNewline("dings\n"));

        assertEquals("dings", StringUtils.chopNewline("dings\r\n"));

        assertEquals("dings\n\r", StringUtils.chopNewline("dings\n\r"));
    }

    @Test
    public void testClean() {
        assertEquals("", StringUtils.clean(null));

        assertEquals("", StringUtils.clean("   "));

        assertEquals("c", StringUtils.clean("  c "));

        assertEquals("dings", StringUtils.clean("  dings \n  "));
    }

    @Test
    public void testConcatenateNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.concatenate((Object) null));
    }

    @Test
    public void testConcatenate() {
        assertEquals("", StringUtils.concatenate());

        assertEquals("x", StringUtils.concatenate("x"));

        assertEquals("xyz", StringUtils.concatenate("x", "y", "z"));
    }

    @SuppressWarnings("ConstantValue")
    @Test
    public void testContainsString() {
        assertFalse(StringUtils.contains(null, null));

        assertFalse(StringUtils.contains(null, "string"));

        assertFalse(StringUtils.contains("string", null));

        assertTrue(StringUtils.contains("string", ""));

        assertTrue(StringUtils.contains("string", "in"));

        assertFalse(StringUtils.contains("string", "IN"));
    }

    @Test
    public void testContainsChar() {
        assertFalse(StringUtils.contains(null, 'c'));

        assertFalse(StringUtils.contains("string", "c"));

        assertTrue(StringUtils.contains("string", ""));

        assertTrue(StringUtils.contains("string", "r"));

        assertFalse(StringUtils.contains("string", "R"));
    }

    @Test
    public void testCountMatchesNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.countMatches(null, null));
    }

    @Test
    public void testCountMatchesNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.countMatches("this is it", null));
    }

    @Test
    public void testCountMatches() {
        assertEquals(0, StringUtils.countMatches(null, "is"));

        assertEquals(2, StringUtils.countMatches("this is it", "is"));

        assertEquals(0, StringUtils.countMatches("this is it", "notincluded"));
    }

    @Test
    public void testDefaultString() {
        assertEquals("", StringUtils.defaultString(null));

        assertEquals("dings", StringUtils.defaultString("dings"));
    }

    @Test
    public void testDefaultStringDefaultValue() {
        assertEquals("defaultValue", StringUtils.defaultString(null, "defaultValue"));

        assertEquals("dings", StringUtils.defaultString("dings", "defaultValue"));
    }

    @Test
    public void testDeleteWhitespaceNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.deleteWhitespace(null));
    }

    @Test
    public void testDeleteWhitespace() {
        assertEquals("", StringUtils.deleteWhitespace(" \t  \n"));

        assertEquals("\b", StringUtils.deleteWhitespace(" \t  \b \n"));

        assertEquals("dings", StringUtils.deleteWhitespace("dings"));

        assertEquals("dings", StringUtils.deleteWhitespace("\n  dings \t "));
    }

    @Test
    public void testDifferenceNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.difference(null, null));
    }

    @Test
    public void testDifferenceNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.difference(null, "another"));
    }

    @Test
    public void testDifferenceNPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.difference("this", null));
    }

    @Test
    public void testDifference() {
        assertEquals("another", StringUtils.difference("this", "another"));

        assertEquals("a robot", StringUtils.difference("I am human", "I am a robot"));

        assertEquals("AM a robot", StringUtils.difference("I am human", "I AM a robot"));
    }

    @Test
    public void testDifferenceAtNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.differenceAt(null, null));
    }

    @Test
    public void testDifferenceAtNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.differenceAt("test", null));
    }

    @Test
    public void testDifferenceAtNPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.differenceAt(null, "test"));
    }

    @Test
    public void testDifferenceAt() {
        assertEquals(0, StringUtils.differenceAt("this", "another"));

        assertEquals(5, StringUtils.differenceAt("I am human", "I am a robot"));

        assertEquals(2, StringUtils.differenceAt("I am human", "I AM a robot"));
    }

    @Test
    public void testEndsWithIgnoreCase() {
        assertFalse(StringUtils.endsWithIgnoreCase(null, null));

        assertFalse(StringUtils.endsWithIgnoreCase(null, "string"));

        assertFalse(StringUtils.endsWithIgnoreCase("string", null));

        assertTrue(StringUtils.endsWithIgnoreCase("string", "ing"));

        assertFalse(StringUtils.endsWithIgnoreCase("string", "a string"));

        assertFalse(StringUtils.endsWithIgnoreCase("string", "str"));
    }

    @Test
    public void testEquals() {
        assertTrue(StringUtils.equals(null, null));

        assertFalse(StringUtils.equals("x", null));

        assertFalse(StringUtils.equals(null, "x"));

        assertFalse(StringUtils.equals("X", "x"));

        assertTrue(StringUtils.equals("dings", "dings"));
    }

    @Test
    public void testEqualsIgnoreCase() {
        assertTrue(StringUtils.equalsIgnoreCase(null, null));

        assertFalse(StringUtils.equalsIgnoreCase("x", null));

        assertFalse(StringUtils.equalsIgnoreCase(null, "x"));

        assertTrue(StringUtils.equalsIgnoreCase("X", "x"));

        assertTrue(StringUtils.equalsIgnoreCase("dings", "dings"));

        assertTrue(StringUtils.equalsIgnoreCase("dings", "diNGs"));
    }

    @Test
    public void testEscapeNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.escape(null));
    }

    @Test
    public void testEscape() {
        assertEquals("dings", StringUtils.escape("dings"));

        assertEquals("dings\\tbums", StringUtils.escape("dings\tbums"));

        assertEquals("dings\\nbums", StringUtils.escape("dings\nbums"));
    }

    @Test
    public void testEscape2() {
        assertNull(StringUtils.escape(null, null, '#'));

        assertEquals("dings", StringUtils.escape("dings", new char[] {'\t', '\b'}, '+'));

        assertEquals("dings+\tbums", StringUtils.escape("dings\tbums", new char[] {'\t', '\b'}, '+'));

        assertEquals("dings\nbums", StringUtils.escape("dings\nbums", new char[] {'\t', '\b'}, '+'));
        assertEquals("dings+\bbums", StringUtils.escape("dings\bbums", new char[] {'\t', '\b'}, '+'));
    }

    @Test
    public void testGetChompNPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.getChomp(null, null));
    }

    @Test
    public void testGetChompNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.getChomp("dings", null));
    }

    @Test
    public void testGetChompNPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.getChomp(null, "dings"));
    }

    @Test
    public void testGetChomp() {
        assertEquals("-bums", StringUtils.getChomp("dings-bums", "-"));

        assertEquals("-", StringUtils.getChomp("dings-", "-"));

        assertEquals("", StringUtils.getChomp("dingsbums", "-"));
    }

    @Test
    public void testGetNestedStringNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.getNestedString("  +dings+ ", null));
    }

    @Test
    public void testGetNestedString() {
        assertNull(StringUtils.getNestedString(null, null));

        assertEquals("dings", StringUtils.getNestedString("  +dings+ ", "+"));

        assertNull(StringUtils.getNestedString("  +dings+ ", "not"));
    }

    @Test
    public void testGetNestedString2NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.getNestedString("  +dings+ ", null, null));
    }

    @Test
    public void testGetNestedString2NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.getNestedString("  +dings+ ", null, "neither"));
    }

    @Test
    public void testGetNestedString2() {
        assertNull(StringUtils.getNestedString(null, null, null));

        assertNull(StringUtils.getNestedString("  +dings+ ", "not", null));

        assertEquals("dings", StringUtils.getNestedString("  +dings- ", "+", "-"));

        assertNull(StringUtils.getNestedString("  +dings+ ", "not", "neither"));
    }

    @Test
    public void testGetPrechompNPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.getPrechomp(null, null));
    }

    @Test
    public void testGetPrechompNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.getPrechomp(null, "bums"));
    }

    @Test
    public void testGetPrechomp() {
        assertEquals("dings bums", StringUtils.getPrechomp("dings bums dongs", "bums"));

        assertEquals("", StringUtils.getPrechomp("dings bums dongs", "non"));
    }

    @Test
    public void testIndexOfAny() {
        assertEquals(-1, StringUtils.indexOfAny(null, null));

        assertEquals(-1, StringUtils.indexOfAny("dings", null));

        assertEquals(-1, StringUtils.indexOfAny(null, new String[] {}));

        assertEquals(6, StringUtils.indexOfAny("dings bums dongs", new String[] {"knuff", "bums"}));
    }

    @Test
    public void testInterpolateNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.interpolate(null, null));
    }

    @Test
    public void testInterpolateNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.interpolate("This ${text} will get replaced", null));
    }

    @Test
    public void testInterpolate() {
        Map<String, String> variables = new HashMap<>();
        assertEquals(
                "This ${text} will get replaced", StringUtils.interpolate("This ${text} will get replaced", variables));

        variables.put("text", "with a special content");

        assertEquals(
                "This with a special content will get replaced",
                StringUtils.interpolate("This ${text} will get replaced", variables));
    }

    @Test
    public void testIsAlpha() {
        assertFalse(StringUtils.isAlpha(null));

        assertFalse(StringUtils.isAlpha("2"));

        assertTrue(StringUtils.isAlpha("asvsdfSDF"));

        assertFalse(StringUtils.isAlpha("asvsdfSDF \t "));

        assertFalse(StringUtils.isAlpha("435afsafd3!"));
    }

    @Test
    public void testIsAlphaSpace() {
        assertFalse(StringUtils.isAlphaSpace(null));

        assertFalse(StringUtils.isAlphaSpace("2"));

        assertTrue(StringUtils.isAlphaSpace("asvsdfSDF"));

        assertTrue(StringUtils.isAlphaSpace("asvsdfSDF  "));

        assertFalse(StringUtils.isAlphaSpace("asvsdfSDF \t "));

        assertFalse(StringUtils.isAlphaSpace("435afsafd3!"));
    }

    @Test
    public void testIsAlphanumeric() {
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
    public void testIsAlphanumericSpace() {
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
    public void testIsBlank() {
        assertTrue(StringUtils.isBlank(null));

        assertFalse(StringUtils.isBlank("xx"));

        assertFalse(StringUtils.isBlank("xx "));

        assertTrue(StringUtils.isBlank("  "));

        assertTrue(StringUtils.isBlank("  \t "));

        assertTrue(StringUtils.isBlank("  \n "));
    }

    @Test
    public void testEmpty() {
        assertTrue(StringUtils.isEmpty(null));

        assertFalse(StringUtils.isEmpty("xx"));

        assertFalse(StringUtils.isEmpty("xx "));

        assertTrue(StringUtils.isEmpty("  "));

        assertTrue(StringUtils.isEmpty("  \t "));

        assertTrue(StringUtils.isEmpty("  \n "));
    }

    @Test
    public void testNotBlank() {
        assertFalse(StringUtils.isNotBlank(null));

        assertTrue(StringUtils.isNotBlank("xx"));

        assertTrue(StringUtils.isNotBlank("xx "));

        assertFalse(StringUtils.isNotBlank("  "));

        assertFalse(StringUtils.isNotBlank("  \t "));

        assertFalse(StringUtils.isNotBlank("  \n "));
    }

    @Test
    public void testNotEmpty() {
        assertFalse(StringUtils.isNotEmpty(null));

        assertTrue(StringUtils.isNotEmpty("xx"));

        assertTrue(StringUtils.isNotEmpty("xx "));

        assertTrue(StringUtils.isNotEmpty("  "));

        assertFalse(StringUtils.isNotEmpty(""));

        assertTrue(StringUtils.isNotEmpty("  \t "));

        assertTrue(StringUtils.isNotEmpty("  \n "));
    }

    @Test
    public void testIsNumeric() {
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
    public void testIsWhitespace() {
        assertFalse(StringUtils.isWhitespace(null));

        assertFalse(StringUtils.isWhitespace("xx"));

        assertFalse(StringUtils.isWhitespace("xx "));

        assertTrue(StringUtils.isWhitespace("  "));

        assertTrue(StringUtils.isWhitespace(""));

        assertTrue(StringUtils.isWhitespace("  \t "));

        assertTrue(StringUtils.isWhitespace("  \n "));
    }

    @Test
    public void testJoinArrayNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.join((Object[]) null, null));
    }

    @Test
    public void testJoinArray() {
        assertEquals("", StringUtils.join(new Object[0], null));

        assertEquals("abc", StringUtils.join(new Object[] {"a", "b", "c"}, null));

        assertEquals("a__b__c", StringUtils.join(new Object[] {"a", "b", "c"}, "__"));
    }

    @Test
    public void testJoinIteratorNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.join((Iterator<?>) null, null));
    }

    @Test
    public void testJoinIterator() {
        ArrayList<String> list = new ArrayList<>();

        assertEquals(StringUtils.join(list.iterator(), null), "");

        list.add("a");
        list.add("b");
        list.add("c");

        assertEquals("abc", StringUtils.join(list.iterator(), null));

        assertEquals(StringUtils.join(list.iterator(), "__"), "a__b__c");
    }

    @Test
    public void testLastIndexOfAny() {
        assertEquals(-1, StringUtils.lastIndexOfAny(null, null));

        assertEquals(-1, StringUtils.lastIndexOfAny("dings", null));

        assertEquals(13, StringUtils.lastIndexOfAny("dings bums boms", "ms", " b"));

        assertEquals(-1, StringUtils.lastIndexOfAny("dings bums boms", "nix", "da"));
    }

    @Test
    public void testLeftIAE() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.left(null, -1));
    }

    @Test
    public void testLeft() {
        assertNull(StringUtils.left(null, 4));

        assertEquals("ding", StringUtils.left("dingsbums", 4));

        assertEquals("dingsbums", StringUtils.left("dingsbums", 40));

        assertEquals("", StringUtils.left("dingsbums", 0));
    }

    @Test
    public void testLeftPad1NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.leftPad(null, 0));
    }

    @Test
    public void testLeftPad1() {
        assertEquals("dings", StringUtils.leftPad("dings", 0));

        assertEquals("dings", StringUtils.leftPad("dings", 2));

        assertEquals("     dings", StringUtils.leftPad("dings", 10));
    }

    @Test
    public void testLeftPad2NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.leftPad(null, 0, null));
    }

    @Test
    public void testLeftPad2NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.leftPad("dings", 0, null));
    }

    @Test
    public void testLeftPad2NPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.leftPad(null, 0, "*"));
    }

    @Test
    public void testLeftPad2() {
        assertEquals("dings", StringUtils.leftPad("dings", 0, "*"));

        assertEquals("dings", StringUtils.leftPad("dings", 2, "*"));

        assertEquals("*****dings", StringUtils.leftPad("dings", 10, "*"));
    }

    @Test
    public void testLowerCase() {
        assertNull(StringUtils.lowerCase(null));

        assertEquals("dingsbums", StringUtils.lowerCase("dinGSbuMS"));

        assertEquals("", StringUtils.lowerCase(""));
    }

    @Test
    public void testLowerCaseFirstLetterNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.lowercaseFirstLetter(null));
    }

    @Test
    public void testLowerCaseFirstLetter() {
        assertEquals("dings Bums", StringUtils.lowercaseFirstLetter("Dings Bums"));
    }

    @Test
    public void testMidNegativeLen() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.mid(null, 0, -2));
    }

    @Test
    public void testMidWrongPos() {
        assertThrows(IndexOutOfBoundsException.class, () -> StringUtils.mid(null, -2, 3));
    }

    @Test
    public void testMid() {
        assertNull(StringUtils.mid(null, 0, 0));

        assertEquals("", StringUtils.mid("dings bums", 0, 0));

        assertEquals("gs b", StringUtils.mid("dings bums", 3, 4));
    }

    @Test
    public void testOverlayStringNPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.overlayString(null, null, 0, 0));
    }

    @Test
    public void testOverlayStringNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.overlayString("dings", null, 0, 0));
    }

    @Test
    public void testOverlayStringNPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.overlayString(null, "bums", 0, 0));
    }

    @Test
    public void testOverlayString() {
        assertEquals("bumsdings", StringUtils.overlayString("dings", "bums", 0, 0));

        assertEquals("dibumss", StringUtils.overlayString("dings", "bums", 2, 4));
    }

    @Test
    public void testPrechompNPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.prechomp(null, null));
    }

    @Test
    public void testPrechompNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.prechomp("dings", null));
    }

    @Test
    public void testPrechompNPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.prechomp(null, "bums"));
    }

    @Test
    public void testPrechomp() {
        assertEquals("bums", StringUtils.prechomp("dings bums", " "));

        assertEquals("dings bums", StringUtils.prechomp("dings bums", "nix"));
    }

    @Test
    public void testQuoteAndEscape1() {
        assertNull(StringUtils.quoteAndEscape(null, '+'));

        assertEquals("", StringUtils.quoteAndEscape("", '+'));

        assertEquals("abc", StringUtils.quoteAndEscape("abc", '"'));

        assertEquals("\"a\\\"bc\"", StringUtils.quoteAndEscape("a\"bc", '"'));

        assertEquals("'a\\'bc'", StringUtils.quoteAndEscape("a'bc", '\''));

        assertEquals("a\"bc", StringUtils.quoteAndEscape("a\"bc", '\''));
    }

    @Test
    public void testQuoteAndEscape2() {
        assertNull(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}));

        assertEquals("", StringUtils.quoteAndEscape("", '+', new char[] {'"'}));

        assertEquals("abc", StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}));

        assertEquals("\"a\\\"bc\"", StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}));

        assertEquals("'a\\'bc'", StringUtils.quoteAndEscape("a'bc", '\'', new char[] {'"'}));

        assertEquals("a\"bc", StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}));

        assertEquals("'a\"bc'", StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\'', '"'}));
    }

    @Test
    public void testQuoteAndEscape3() {
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
    public void testQuoteAndEscape4() {
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
    public void testRemoveAndHumpNPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.removeAndHump(null, null));
    }

    @Test
    public void testRemoveAndHumpNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.removeAndHump("dings", null));
    }

    @Test
    public void testRemoveAndHumpNPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.removeAndHump(null, "bums"));
    }

    @Test
    public void testRemoveAndHump() {
        assertEquals("Ding", StringUtils.removeAndHump("dings", "bums"));

        assertEquals("ThisIsIt", StringUtils.removeAndHump("this-is-it", "-"));

        assertEquals("THISISIT", StringUtils.removeAndHump("THIS-IS-IT", "-"));
    }

    @Test
    public void testRemoveDuplicateWhitespaceNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.removeDuplicateWhitespace(null));
    }

    @Test
    public void testRemoveDuplicateWhitespace() {
        assertEquals("dings", StringUtils.removeDuplicateWhitespace("dings"));

        assertEquals("dings bums", StringUtils.removeDuplicateWhitespace("dings bums"));

        assertEquals("dings bums", StringUtils.removeDuplicateWhitespace("dings  bums"));

        assertEquals("dings bums", StringUtils.removeDuplicateWhitespace("dings \t bums"));
    }

    @Test
    public void testRepeatNPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.repeat(null, 0));
    }

    @Test
    public void testRepeatNegativeAmount() {
        assertThrows(NegativeArraySizeException.class, () -> StringUtils.repeat("dings", -1));
    }

    @Test
    public void testRepeat() {
        assertEquals("", StringUtils.repeat("dings", 0));

        assertEquals("dings", StringUtils.repeat("dings", 1));

        assertEquals("dingsdingsdings", StringUtils.repeat("dings", 3));
    }

    @Test
    public void testReplaceChar() {
        assertNull(StringUtils.replace(null, 'i', 'o'));

        assertEquals("dongs", StringUtils.replace("dings", 'i', 'o'));

        assertEquals("dongsboms", StringUtils.replace("dingsbims", 'i', 'o'));

        assertEquals("dings", StringUtils.replace("dings", 'x', 'o'));
    }

    @Test
    public void testReplace2CharMax() {
        assertNull(StringUtils.replace(null, 'i', 'o', 0));

        assertEquals("dongsobumso", StringUtils.replace("dingsibumsi", 'i', 'o', 3));

        assertEquals("dongsobumsi", StringUtils.replace("dingsibumsi", 'i', 'o', 2));

        assertEquals("dongsobumso", StringUtils.replace("dingsibumsi", 'i', 'o', 0));

        assertEquals("dongsobumso", StringUtils.replace("dingsibumsi", 'i', 'o', -2));

        assertEquals("dings", StringUtils.replace("dings", 'x', 'o', 2));
    }

    @Test
    public void testReplaceString() {
        assertNull(StringUtils.replace(null, "in", "ox"));

        assertEquals("doxgs", StringUtils.replace("dings", "in", "ox"));

        assertEquals("doxgsboxs", StringUtils.replace("dingsbins", "in", "ox"));

        assertEquals("dings", StringUtils.replace("dings", "nin", "ox"));
    }

    @Test
    public void testReplace2StringMax() {
        assertNull(StringUtils.replace(null, "in", "ox", 0));

        assertEquals("dingxobumxo", StringUtils.replace("dingsibumsi", "si", "xo", 3));

        assertEquals("dingxobumxo", StringUtils.replace("dingsibumsi", "si", "xo", 2));

        assertEquals("dingxobumsi", StringUtils.replace("dingsibumsi", "si", "xo", 1));

        assertEquals("dingxobumxo", StringUtils.replace("dingsibumsi", "si", "xo", 0));

        assertEquals("dingxobumxo", StringUtils.replace("dingsibumsi", "si", "xo", -2));

        assertEquals("dings", StringUtils.replace("dings", "si", "xo", 2));
    }

    @Test
    public void testReplaceOnceChar() {
        assertNull(StringUtils.replaceOnce(null, 'i', 'o'));

        assertEquals("dongsibumsi", StringUtils.replaceOnce("dingsibumsi", 'i', 'o'));

        assertEquals("dings", StringUtils.replaceOnce("dings", 'x', 'o'));
    }

    @Test
    public void testReplaceOnceString() {
        assertNull(StringUtils.replaceOnce(null, "in", "ox"));

        assertEquals("dingxobumsi", StringUtils.replaceOnce("dingsibumsi", "si", "xo"));

        assertEquals("dings", StringUtils.replaceOnce("dings", "si", "xo"));
    }

    @Test
    public void testReverse() {
        assertNull(StringUtils.reverse(null));

        assertEquals("", StringUtils.reverse(""));

        assertEquals("sgnid", StringUtils.reverse("dings"));

        assertEquals(" sgnid  ", StringUtils.reverse("  dings "));
    }

    @Test
    public void testReverseDelimitedStringNPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.reverseDelimitedString(null, null));
    }

    @Test
    public void testReverseDelimitedStringNPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.reverseDelimitedString(null, " "));
    }

    @Test
    public void testReverseDelimitedString() {
        assertEquals("dings", StringUtils.reverseDelimitedString("dings", null));

        assertEquals("", StringUtils.reverseDelimitedString("", " "));

        assertEquals("dings", StringUtils.reverseDelimitedString("dings", " "));

        assertEquals("dings", StringUtils.reverseDelimitedString("  dings ", " "));

        assertEquals("bums dings", StringUtils.reverseDelimitedString("dings bums", " "));
    }

    @Test
    public void testRightIAE1() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.right(null, -1));
    }

    @Test
    public void testRightIAE2() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.right("dings", -1));
    }

    @Test
    public void testRight() {
        assertNull(StringUtils.right(null, 0));

        assertEquals("", StringUtils.right("dings", 0));

        assertEquals("ngs", StringUtils.right("dings", 3));

        assertEquals("gs ", StringUtils.right("dings ", 3));
    }

    @Test
    public void testRightPad1NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.rightPad(null, 0));
    }

    @Test
    public void testRightPad1() {
        assertEquals("dings", StringUtils.rightPad("dings", 0));

        assertEquals("dings", StringUtils.rightPad("dings", 3));

        assertEquals("dings     ", StringUtils.rightPad("dings", 10));
    }

    @Test
    public void testRightPad2NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.rightPad(null, 0, null));
    }

    @Test
    public void testRightPad2NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.rightPad("dings", 0, null));
    }

    @Test
    public void testRightPad2NPE23() {
        assertThrows(NullPointerException.class, () -> StringUtils.rightPad(null, 0, "+"));
    }

    @Test
    public void testRightPad2() {
        assertEquals("dings", StringUtils.rightPad("dings", 0, "+"));

        assertEquals("dings", StringUtils.rightPad("dings", 3, "+"));

        assertEquals("dings+++++", StringUtils.rightPad("dings", 10, "+"));
    }

    @Test
    public void testSplit1NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null));
    }

    @Test
    public void testSplit1() {
        assertArrayEquals(new String[] {"dings"}, StringUtils.split("dings"));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings bums"));
    }

    @Test
    public void testSplit2NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null, null));
    }

    @Test
    public void testSplit2NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null, " "));
    }

    @Test
    public void testSplit2() {
        assertArrayEquals(new String[] {"dings"}, StringUtils.split("dings", null));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings bums", null));

        assertArrayEquals(new String[] {"dings"}, StringUtils.split("dings", "+"));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings+bums", "+"));
    }

    @Test
    public void testSplit3NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null, null, 1));
    }

    @Test
    public void testSplit3NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null, " ", 1));
    }

    @Test
    public void testSplit3() {
        assertArrayEquals(new String[] {"dings"}, StringUtils.split("dings", null, 3));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings bums", null, 3));

        assertArrayEquals(new String[] {"dings"}, StringUtils.split("dings", "+", 3));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings+bums", "+", 3));

        assertArrayEquals(new String[] {"dings+bums"}, StringUtils.split("dings+bums", "+", 1));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings+bums", "+", 0));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.split("dings+bums", "+", -5));
    }

    @Test
    public void testStrip1() {
        assertNull(StringUtils.strip(null));

        assertEquals("dings", StringUtils.strip("dings"));

        assertEquals("dings", StringUtils.strip("  dings \t "));
    }

    @Test
    public void testStrip2() {
        assertNull(StringUtils.strip(null, " "));

        assertNull(StringUtils.strip(null, null));

        assertEquals("dings", StringUtils.strip("dings", " "));

        assertEquals("dings \t", StringUtils.strip("  dings \t ", " "));
    }

    @Test
    public void testStripAll1() {
        assertNull(StringUtils.stripAll(null));

        assertArrayEquals(new String[] {}, StringUtils.stripAll());

        assertArrayEquals(new String[] {"dings"}, StringUtils.stripAll("dings"));

        assertArrayEquals(new String[] {"dings", "bums"}, StringUtils.stripAll(" dings ", "  bums \t  "));
    }

    @Test
    public void testStripAll2() {
        assertNull(StringUtils.stripAll(null, " "));

        assertArrayEquals(new String[] {}, StringUtils.stripAll(new String[] {}, " "));

        assertArrayEquals(new String[] {"dings"}, StringUtils.stripAll(new String[] {"dings"}, " "));

        assertArrayEquals(
                new String[] {"dings", "bums \t"}, StringUtils.stripAll(new String[] {" dings ", "  bums \t  "}, " "));
    }

    @Test
    public void testStripEnd() {
        assertNull(StringUtils.stripEnd(null, null));

        assertEquals("dings", StringUtils.stripEnd("dings", null));

        assertEquals("  dings", StringUtils.stripEnd("  dings \t ", null));

        assertNull(StringUtils.stripEnd(null, " "));

        assertEquals("dings", StringUtils.stripEnd("dings", " "));

        assertEquals("  dings \t", StringUtils.stripEnd("  dings \t ", " "));
    }

    @Test
    public void testStripStart() {
        assertNull(StringUtils.stripStart(null, null));

        assertEquals("dings", StringUtils.stripStart("dings", null));

        assertEquals("dings \t ", StringUtils.stripStart("  dings \t ", null));

        assertNull(StringUtils.stripStart(null, " "));

        assertEquals("dings", StringUtils.stripStart("dings", " "));

        assertEquals("\t dings \t ", StringUtils.stripStart("  \t dings \t ", " "));
    }

    @Test
    public void testSubstring1() {
        assertNull(StringUtils.substring(null, 0));
        assertNull(StringUtils.substring(null, -3));

        assertEquals("ngs", StringUtils.substring("dings", 2));

        assertEquals("gs", StringUtils.substring("dings", -2));

        assertEquals("", StringUtils.substring("dings", 20));
    }

    @Test
    public void testSubstring2() {
        assertNull(StringUtils.substring(null, 0, 2));

        assertNull(StringUtils.substring(null, -3, 0));

        assertEquals("ng", StringUtils.substring("dings", 2, 4));

        assertEquals("g", StringUtils.substring("dings", -2, 4));

        assertEquals("", StringUtils.substring("dings", 20, 23));

        assertEquals("", StringUtils.substring("dings", 4, 2));
    }

    @Test
    public void testSwapCase() {
        assertNull(StringUtils.swapCase(null));

        assertEquals("DINGS", StringUtils.swapCase("dings"));

        assertEquals("dINgS", StringUtils.swapCase("DinGs"));
    }

    @Test
    public void testTrim() {
        assertNull(StringUtils.trim(null));

        assertEquals("", StringUtils.trim("   "));

        assertEquals("c", StringUtils.trim("  c "));

        assertEquals("dings", StringUtils.trim("  dings \n  "));
    }

    @Test
    public void testUncapitalise() {
        assertNull(StringUtils.uncapitalise(null));

        assertEquals("   ", StringUtils.uncapitalise("   "));

        assertEquals("dings", StringUtils.uncapitalise("dings"));

        assertEquals("dings", StringUtils.uncapitalise("Dings"));

        assertEquals("dINGS", StringUtils.uncapitalise("DINGS"));
    }

    @Test
    public void testUncapitaliseAllWords() {
        assertNull(StringUtils.uncapitaliseAllWords(null));

        assertEquals("   ", StringUtils.uncapitaliseAllWords("   "));

        assertEquals("dings bums", StringUtils.uncapitaliseAllWords("dings bums"));

        assertEquals("dings bums", StringUtils.uncapitaliseAllWords("Dings Bums"));

        assertEquals("dINGS bums", StringUtils.uncapitaliseAllWords("DINGS Bums"));
    }

    @Test
    public void testUnifyLineSeparators1() {
        String sls = System.lineSeparator();

        assertNull(StringUtils.unifyLineSeparators(null));

        assertEquals("   ", StringUtils.unifyLineSeparators("   "));

        assertEquals("dings" + sls + "bums" + sls + "dongs", StringUtils.unifyLineSeparators("dings\nbums\r\ndongs"));
    }

    @Test
    public void testUnifyLineSeparators2() {
        assertNull(StringUtils.unifyLineSeparators(null, "\n"));

        assertEquals("   ", StringUtils.unifyLineSeparators("   ", "\n"));

        assertEquals(
                "   " // takes the sytem line separator
                ,
                StringUtils.unifyLineSeparators("   ", null));

        assertEquals("dings\nbums\ndongs", StringUtils.unifyLineSeparators("dings\nbums\r\ndongs", "\n"));
    }

    @Test
    public void testUppercase() {
        assertNull(StringUtils.upperCase(null));

        assertEquals("   ", StringUtils.upperCase("   "));

        assertEquals("", StringUtils.upperCase(""));

        assertEquals("DINGS", StringUtils.upperCase("dings"));
    }
}
