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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test the {@link StringUtils} class.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class StringUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testAbbreviateNPE() {
        assertThat(StringUtils.abbreviate(null, 10), nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviateMinLength() {
        assertThat(StringUtils.abbreviate("This is a longtext", 3), is("T"));
    }

    @Test
    public void testAbbreviate() {
        assertThat(StringUtils.abbreviate("This is a longtext", 10), is("This is..."));

        assertThat(StringUtils.abbreviate("This is a longtext", 50), is("This is a longtext"));
    }

    @Test(expected = NullPointerException.class)
    public void testAbbreviateOffsetNPE() {
        assertThat(StringUtils.abbreviate(null, 10, 20), nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviateOffsetMinLength() {
        assertThat(StringUtils.abbreviate("This is a longtext", 10, 3), is("T"));
    }

    @Test
    public void testAbbreviateOffset() {
        assertThat(StringUtils.abbreviate("This is a longtext", 5, 10), is("...is a..."));

        assertThat(StringUtils.abbreviate("This is a longtext", 10, 20), is("This is a longtext"));

        assertThat(StringUtils.abbreviate("This is a longtext", 50, 20), is("This is a longtext"));
    }

    @Test(expected = NullPointerException.class)
    public void testAddAndDeHumpNPE() {
        StringUtils.addAndDeHump(null);
    }

    @Test
    public void testAddAndDeHump() {
        assertThat(StringUtils.addAndDeHump("lalala"), is("lalala"));

        assertThat(StringUtils.addAndDeHump("LaLaLa"), is("la-la-la"));

        assertThat(StringUtils.addAndDeHump("ALLUPPER"), is("a-l-l-u-p-p-e-r"));
    }

    @Test
    public void testCapitalise() {
        assertThat(StringUtils.capitalise(null), nullValue());

        assertThat(StringUtils.capitalise("startBig"), is("StartBig"));
    }

    @Test
    public void testCapitaliseAllWords() {
        assertThat(StringUtils.capitaliseAllWords(null), nullValue());

        assertThat(StringUtils.capitaliseAllWords("start all big"), is("Start All Big"));
    }

    @Test(expected = NullPointerException.class)
    public void testCapitalizeFirstLetterNPE() {
        assertThat(StringUtils.capitalizeFirstLetter(null), nullValue());
    }

    @Test
    public void testCapitalizeFirstLetter() {
        assertThat(StringUtils.capitalizeFirstLetter("Dings"), is("Dings"));

        assertThat(StringUtils.capitalizeFirstLetter("  dings"), is("  dings"));

        assertThat(StringUtils.capitalizeFirstLetter("start all big"), is("Start all big"));
    }

    @Test(expected = NullPointerException.class)
    public void testCenterNPE() {
        StringUtils.center(null, 20);
    }

    @Test
    public void testCenter() {
        assertThat(StringUtils.center("centerMe", 20), is("      centerMe      "));

        assertThat(StringUtils.center("centerMe", 4), is("centerMe"));

        assertThat(StringUtils.center("        centerMe", 20), is("          centerMe  "));
    }

    @Test(expected = NullPointerException.class)
    public void testCenterDelimNPE() {
        StringUtils.center(null, 20, "*");
    }

    @Test
    public void testCenterDelim() {
        assertThat(StringUtils.center("centerMe", 20, "*"), is("******centerMe******"));

        assertThat(StringUtils.center("centerMe", 4, "*"), is("centerMe"));

        assertThat(StringUtils.center("        centerMe", 20, "*"), is("**        centerMe**"));
    }

    @Test(expected = NullPointerException.class)
    public void testChompNPE() {
        StringUtils.chomp(null);
    }

    @Test
    public void testChomp() {
        assertThat(StringUtils.chomp("dings"), is("dings"));

        assertThat(StringUtils.chomp("dings\n"), is("dings"));

        assertThat(StringUtils.chomp("dings\nbums"), is("dings"));

        assertThat(StringUtils.chomp("dings\nbums\ndongs"), is("dings\nbums"));
    }

    @Test(expected = NullPointerException.class)
    public void testChompDelimNPE() {
        StringUtils.chomp(null, "+");
    }

    @Test
    public void testChompDelim() {
        assertThat(StringUtils.chomp("dings", "+"), is("dings"));

        assertThat(StringUtils.chomp("dings+", "+"), is("dings"));

        assertThat(StringUtils.chomp("dings+bums", "+"), is("dings"));

        assertThat(StringUtils.chomp("dings+bums+dongs", "+"), is("dings+bums"));
    }

    @Test(expected = NullPointerException.class)
    public void testChompLastNPE() {
        StringUtils.chompLast(null);
    }

    @Test
    public void testChompLast() {
        assertThat(StringUtils.chompLast("dings"), is("dings"));

        assertThat(StringUtils.chompLast("\n"), is(""));

        assertThat(StringUtils.chompLast("dings\n"), is("dings"));

        assertThat(StringUtils.chompLast("dings\nbums"), is("dings\nbums"));

        assertThat(StringUtils.chompLast("dings\nbums\ndongs\n"), is("dings\nbums\ndongs"));
    }

    @Test(expected = NullPointerException.class)
    public void testChompLastDelimNPE() {
        StringUtils.chompLast(null, "+");
    }

    @Test
    public void testChompLastDelim() {
        assertThat(StringUtils.chompLast("dings", "+"), is("dings"));

        assertThat(StringUtils.chompLast("+", "+"), is(""));

        assertThat(StringUtils.chompLast("dings+", "+"), is("dings"));

        assertThat(StringUtils.chompLast("dings+bums", "+"), is("dings+bums"));

        assertThat(StringUtils.chompLast("dings+bums+dongs+", "+"), is("dings+bums+dongs"));
    }

    @Test(expected = NullPointerException.class)
    public void testChopNPE() {
        StringUtils.chop(null);
    }

    @Test
    public void testChop() {
        assertThat(StringUtils.chop("dings"), is("ding"));

        assertThat(StringUtils.chop("x"), is(""));

        assertThat(StringUtils.chop("dings\n"), is("dings"));

        assertThat(StringUtils.chop("dings\r\n"), is("dings"));

        assertThat(StringUtils.chop("dings\n\r"), is("dings\n"));
    }

    @Test(expected = NullPointerException.class)
    public void testChopNewlineNPE() {
        StringUtils.chopNewline(null);
    }

    @Test
    public void testChopNewline() {
        assertThat(StringUtils.chopNewline("dings"), is("dings"));

        assertThat(StringUtils.chopNewline("x"), is("x"));

        assertThat(StringUtils.chopNewline("dings\n"), is("dings"));

        assertThat(StringUtils.chopNewline("dings\r\n"), is("dings"));

        assertThat(StringUtils.chopNewline("dings\n\r"), is("dings\n\r"));
    }

    @Test
    public void testClean() {
        assertThat(StringUtils.clean(null), is(""));

        assertThat(StringUtils.clean("   "), is(""));

        assertThat(StringUtils.clean("  c "), is("c"));

        assertThat(StringUtils.clean("  dings \n  "), is("dings"));
    }

    @Test(expected = NullPointerException.class)
    public void testConcatenateNPE() {
        StringUtils.concatenate(null);
    }

    @Test
    public void testConcatenate() {
        assertThat(StringUtils.concatenate(new String[0]), is(""));

        assertThat(StringUtils.concatenate(new String[] {"x"}), is("x"));

        assertThat(StringUtils.concatenate(new String[] {"x", "y", "z"}), is("xyz"));
    }

    @Test
    public void testContainsString() {
        assertThat(StringUtils.contains(null, null), is(false));

        assertThat(StringUtils.contains(null, "string"), is(false));

        assertThat(StringUtils.contains("string", null), is(false));

        assertThat(StringUtils.contains("string", ""), is(true));

        assertThat(StringUtils.contains("string", "in"), is(true));

        assertThat(StringUtils.contains("string", "IN"), is(false));
    }

    @Test
    public void testContainsChar() {
        assertThat(StringUtils.contains(null, 'c'), is(false));

        assertThat(StringUtils.contains("string", "c"), is(false));

        assertThat(StringUtils.contains("string", ""), is(true));

        assertThat(StringUtils.contains("string", "r"), is(true));

        assertThat(StringUtils.contains("string", "R"), is(false));
    }

    @Test(expected = NullPointerException.class)
    public void testCountMatchesNPE() {
        StringUtils.countMatches(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testCountMatchesNPE2() {
        StringUtils.countMatches("this is it", null);
    }

    @Test
    public void testCountMatches() {
        assertThat(StringUtils.countMatches(null, "is"), is(0));

        assertThat(StringUtils.countMatches("this is it", "is"), is(2));

        assertThat(StringUtils.countMatches("this is it", "notincluded"), is(0));
    }

    @Test
    public void testDefaultString() {
        assertThat(StringUtils.defaultString(null), is(""));

        assertThat(StringUtils.defaultString("dings"), is("dings"));
    }

    @Test
    public void testDefaultStringDefaultValue() {
        assertThat(StringUtils.defaultString(null, "defaultValue"), is("defaultValue"));

        assertThat(StringUtils.defaultString("dings", "defaultValue"), is("dings"));
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteWhitespaceNPE() {
        StringUtils.deleteWhitespace(null);
    }

    @Test
    public void testDeleteWhitespace() {
        assertThat(StringUtils.deleteWhitespace(" \t  \n"), is(""));

        assertThat(StringUtils.deleteWhitespace(" \t  \b \n"), is("\b"));

        assertThat(StringUtils.deleteWhitespace("dings"), is("dings"));

        assertThat(StringUtils.deleteWhitespace("\n  dings \t "), is("dings"));
    }

    @Test(expected = NullPointerException.class)
    public void testDifferenceNPE() {
        StringUtils.difference(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testDifferenceNPE2() {
        StringUtils.difference(null, "another");
    }

    @Test(expected = NullPointerException.class)
    public void testDifferenceNPE3() {
        StringUtils.difference("this", null);
    }

    @Test
    public void testDifference() {
        assertThat(StringUtils.difference("this", "another"), is("another"));

        assertThat(StringUtils.difference("I am human", "I am a robot"), is("a robot"));

        assertThat(StringUtils.difference("I am human", "I AM a robot"), is("AM a robot"));
    }

    @Test(expected = NullPointerException.class)
    public void testDifferenceAtNPE() {
        StringUtils.differenceAt(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testDifferenceAtNPE2() {
        StringUtils.differenceAt("test", null);
    }

    @Test(expected = NullPointerException.class)
    public void testDifferenceAtNPE3() {
        StringUtils.differenceAt(null, "test");
    }

    @Test
    public void testDifferenceAt() {
        assertThat(StringUtils.differenceAt("this", "another"), is(0));

        assertThat(StringUtils.differenceAt("I am human", "I am a robot"), is(5));

        assertThat(StringUtils.differenceAt("I am human", "I AM a robot"), is(2));
    }

    @Test
    public void testEndsWithIgnoreCase() {
        assertThat(StringUtils.endsWithIgnoreCase(null, null), is(false));

        assertThat(StringUtils.endsWithIgnoreCase(null, "string"), is(false));

        assertThat(StringUtils.endsWithIgnoreCase("string", null), is(false));

        assertThat(StringUtils.endsWithIgnoreCase("string", "ing"), is(true));

        assertThat(StringUtils.endsWithIgnoreCase("string", "a string"), is(false));

        assertThat(StringUtils.endsWithIgnoreCase("string", "str"), is(false));
    }

    @Test
    public void testEquals() {
        assertThat(StringUtils.equals(null, null), is(true));

        assertThat(StringUtils.equals("x", null), is(false));

        assertThat(StringUtils.equals(null, "x"), is(false));

        assertThat(StringUtils.equals("X", "x"), is(false));

        assertThat(StringUtils.equals("dings", "dings"), is(true));
    }

    @Test
    public void testEqualsIgnoreCase() {
        assertThat(StringUtils.equalsIgnoreCase(null, null), is(true));

        assertThat(StringUtils.equalsIgnoreCase("x", null), is(false));

        assertThat(StringUtils.equalsIgnoreCase(null, "x"), is(false));

        assertThat(StringUtils.equalsIgnoreCase("X", "x"), is(true));

        assertThat(StringUtils.equalsIgnoreCase("dings", "dings"), is(true));

        assertThat(StringUtils.equalsIgnoreCase("dings", "diNGs"), is(true));
    }

    @Test(expected = NullPointerException.class)
    public void testEscapeNPE() {
        StringUtils.escape(null);
    }

    @Test
    public void testEscape() {
        assertThat(StringUtils.escape("dings"), is("dings"));

        assertThat(StringUtils.escape("dings\tbums"), is("dings\\tbums"));

        assertThat(StringUtils.escape("dings\nbums"), is("dings\\nbums"));
    }

    @Test
    public void testEscape2() {
        assertThat(StringUtils.escape(null, null, '#'), nullValue());

        assertThat(StringUtils.escape("dings", new char[] {'\t', '\b'}, '+'), is("dings"));

        assertThat(StringUtils.escape("dings\tbums", new char[] {'\t', '\b'}, '+'), is("dings+\tbums"));

        assertThat(StringUtils.escape("dings\nbums", new char[] {'\t', '\b'}, '+'), is("dings\nbums"));
        assertThat(StringUtils.escape("dings\bbums", new char[] {'\t', '\b'}, '+'), is("dings+\bbums"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetChompNPE1() {
        StringUtils.getChomp(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testGetChompNPE2() {
        StringUtils.getChomp("dings", null);
    }

    @Test(expected = NullPointerException.class)
    public void testGetChompNPE3() {
        StringUtils.getChomp(null, "dings");
    }

    @Test
    public void testGetChomp() {
        assertThat(StringUtils.getChomp("dings-bums", "-"), is("-bums"));

        assertThat(StringUtils.getChomp("dings-", "-"), is("-"));

        assertThat(StringUtils.getChomp("dingsbums", "-"), is(""));
    }

    @Test(expected = NullPointerException.class)
    public void testGetNestedStringNPE() {
        assertThat(StringUtils.getNestedString("  +dings+ ", null), nullValue());
    }

    @Test
    public void testGetNestedString() {
        assertThat(StringUtils.getNestedString(null, null), nullValue());

        assertThat(StringUtils.getNestedString("  +dings+ ", "+"), is("dings"));

        assertThat(StringUtils.getNestedString("  +dings+ ", "not"), nullValue());
    }

    @Test(expected = NullPointerException.class)
    public void testGetNestedString2NPE1() {
        assertThat(StringUtils.getNestedString("  +dings+ ", null, null), nullValue());
    }

    @Test(expected = NullPointerException.class)
    public void testGetNestedString2NPE2() {
        assertThat(StringUtils.getNestedString("  +dings+ ", null, "neither"), nullValue());
    }

    @Test
    public void testGetNestedString2() {
        assertThat(StringUtils.getNestedString(null, null, null), nullValue());

        assertThat(StringUtils.getNestedString("  +dings+ ", "not", null), nullValue());

        assertThat(StringUtils.getNestedString("  +dings- ", "+", "-"), is("dings"));

        assertThat(StringUtils.getNestedString("  +dings+ ", "not", "neither"), nullValue());
    }

    @Test(expected = NullPointerException.class)
    public void testGetPrechompNPE1() {
        StringUtils.getPrechomp(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testGetPrechompNPE2() {
        StringUtils.getPrechomp(null, "bums");
    }

    @Test
    public void testGetPrechomp() {
        assertThat(StringUtils.getPrechomp("dings bums dongs", "bums"), is("dings bums"));

        assertThat(StringUtils.getPrechomp("dings bums dongs", "non"), is(""));
    }

    @Test
    public void testIndexOfAny() {
        assertThat(StringUtils.indexOfAny(null, null), is(-1));

        assertThat(StringUtils.indexOfAny("dings", null), is(-1));

        assertThat(StringUtils.indexOfAny(null, new String[] {}), is(-1));

        assertThat(StringUtils.indexOfAny("dings bums dongs", new String[] {"knuff", "bums"}), is(6));
    }

    @Test(expected = NullPointerException.class)
    public void testInterpolateNPE() {
        StringUtils.interpolate(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testInterpolateNPE2() {
        StringUtils.interpolate("This ${text} will get replaced", null);
    }

    @Test
    public void testInterpolate() {
        Map<String, String> variables = new HashMap<>();
        assertThat(
                StringUtils.interpolate("This ${text} will get replaced", variables),
                is("This ${text} will get replaced"));

        variables.put("text", "with a special content");

        assertThat(
                StringUtils.interpolate("This ${text} will get replaced", variables),
                is("This with a special content will get replaced"));
    }

    @Test
    public void testIsAlpha() {
        assertThat(StringUtils.isAlpha(null), is(false));

        assertThat(StringUtils.isAlpha("2"), is(false));

        assertThat(StringUtils.isAlpha("asvsdfSDF"), is(true));

        assertThat(StringUtils.isAlpha("asvsdfSDF \t "), is(false));

        assertThat(StringUtils.isAlpha("435afsafd3!"), is(false));
    }

    @Test
    public void testIsAlphaSpace() {
        assertThat(StringUtils.isAlphaSpace(null), is(false));

        assertThat(StringUtils.isAlphaSpace("2"), is(false));

        assertThat(StringUtils.isAlphaSpace("asvsdfSDF"), is(true));

        assertThat(StringUtils.isAlphaSpace("asvsdfSDF  "), is(true));

        assertThat(StringUtils.isAlphaSpace("asvsdfSDF \t "), is(false));

        assertThat(StringUtils.isAlphaSpace("435afsafd3!"), is(false));
    }

    @Test
    public void testIsAlphanumeric() {
        assertThat(StringUtils.isAlphanumeric(null), is(false));

        assertThat(StringUtils.isAlphanumeric("2"), is(true));

        assertThat(StringUtils.isAlphanumeric("asvsdfSDF"), is(true));

        assertThat(StringUtils.isAlphanumeric("asvsdfSDF  "), is(false));

        assertThat(StringUtils.isAlphanumeric("asvsdfSDF \t "), is(false));

        assertThat(StringUtils.isAlphanumeric("435afsafd3!"), is(false));

        assertThat(StringUtils.isAlphanumeric("435afsafd3"), is(true));

        assertThat(StringUtils.isAlphanumeric("435 "), is(false));

        assertThat(StringUtils.isAlphanumeric("435"), is(true));
    }

    @Test
    public void testIsAlphanumericSpace() {
        assertThat(StringUtils.isAlphanumericSpace(null), is(false));

        assertThat(StringUtils.isAlphanumericSpace("2"), is(true));

        assertThat(StringUtils.isAlphanumericSpace("asvsdfSDF"), is(true));

        assertThat(StringUtils.isAlphanumericSpace("asvsdfSDF  "), is(true));

        assertThat(StringUtils.isAlphanumericSpace("asvsdfSDF \t "), is(false));

        assertThat(StringUtils.isAlphanumericSpace("435afsafd3!"), is(false));

        assertThat(StringUtils.isAlphanumericSpace("435afsafd3"), is(true));

        assertThat(StringUtils.isAlphanumericSpace("435 "), is(true));

        assertThat(StringUtils.isAlphanumericSpace("435"), is(true));
    }

    @Test
    public void testIsBlank() {
        assertThat(StringUtils.isBlank(null), is(true));

        assertThat(StringUtils.isBlank("xx"), is(false));

        assertThat(StringUtils.isBlank("xx "), is(false));

        assertThat(StringUtils.isBlank("  "), is(true));

        assertThat(StringUtils.isBlank("  \t "), is(true));

        assertThat(StringUtils.isBlank("  \n "), is(true));
    }

    @Test
    public void testEmpty() {
        assertThat(StringUtils.isEmpty(null), is(true));

        assertThat(StringUtils.isEmpty("xx"), is(false));

        assertThat(StringUtils.isEmpty("xx "), is(false));

        assertThat(StringUtils.isEmpty("  "), is(true));

        assertThat(StringUtils.isEmpty("  \t "), is(true));

        assertThat(StringUtils.isEmpty("  \n "), is(true));
    }

    @Test
    public void testNotBlank() {
        assertThat(StringUtils.isNotBlank(null), is(false));

        assertThat(StringUtils.isNotBlank("xx"), is(true));

        assertThat(StringUtils.isNotBlank("xx "), is(true));

        assertThat(StringUtils.isNotBlank("  "), is(false));

        assertThat(StringUtils.isNotBlank("  \t "), is(false));

        assertThat(StringUtils.isNotBlank("  \n "), is(false));
    }

    @Test
    public void testNotEmpty() {
        assertThat(StringUtils.isNotEmpty(null), is(false));

        assertThat(StringUtils.isNotEmpty("xx"), is(true));

        assertThat(StringUtils.isNotEmpty("xx "), is(true));

        assertThat(StringUtils.isNotEmpty("  "), is(true));

        assertThat(StringUtils.isNotEmpty(""), is(false));

        assertThat(StringUtils.isNotEmpty("  \t "), is(true));

        assertThat(StringUtils.isNotEmpty("  \n "), is(true));
    }

    @Test
    public void testIsNumeric() {
        assertThat(StringUtils.isNumeric(null), is(false));

        assertThat(StringUtils.isNumeric("2"), is(true));

        assertThat(StringUtils.isNumeric("asvsdfSDF"), is(false));

        assertThat(StringUtils.isNumeric("asvsdfSDF  "), is(false));

        assertThat(StringUtils.isNumeric("asvsdfSDF \t "), is(false));

        assertThat(StringUtils.isNumeric("435afsafd3!"), is(false));

        assertThat(StringUtils.isNumeric("435afsafd3"), is(false));

        assertThat(StringUtils.isNumeric("435 "), is(false));

        assertThat(StringUtils.isNumeric("435"), is(true));
    }

    @Test
    public void testIsWhitespace() {
        assertThat(StringUtils.isWhitespace(null), is(false));

        assertThat(StringUtils.isWhitespace("xx"), is(false));

        assertThat(StringUtils.isWhitespace("xx "), is(false));

        assertThat(StringUtils.isWhitespace("  "), is(true));

        assertThat(StringUtils.isWhitespace(""), is(true));

        assertThat(StringUtils.isWhitespace("  \t "), is(true));

        assertThat(StringUtils.isWhitespace("  \n "), is(true));
    }

    @Test(expected = NullPointerException.class)
    public void testJoinArrayNPE() {
        StringUtils.join((Object[]) null, null);
    }

    @Test
    public void testJoinArray() {
        assertThat(StringUtils.join(new Object[0], null), is(""));

        assertThat(StringUtils.join(new Object[] {"a", "b", "c"}, null), is("abc"));

        assertThat(StringUtils.join(new Object[] {"a", "b", "c"}, "__"), is("a__b__c"));
    }

    @Test(expected = NullPointerException.class)
    public void testJoinIteratorNPE() {
        StringUtils.join((Iterator<?>) null, null);
    }

    @Test
    public void testJoinIterator() {
        ArrayList<String> list = new ArrayList<>();

        assertThat(StringUtils.join(list.iterator(), null), is(""));

        list.add("a");
        list.add("b");
        list.add("c");

        assertThat(StringUtils.join(list.iterator(), null), is("abc"));

        assertThat(StringUtils.join(list.iterator(), "__"), is("a__b__c"));
    }

    @Test
    public void testLastIndexOfAny() {
        assertThat(StringUtils.lastIndexOfAny(null, null), is(-1));

        assertThat(StringUtils.lastIndexOfAny("dings", null), is(-1));

        assertThat(StringUtils.lastIndexOfAny("dings bums boms", new String[] {"ms", " b"}), is(13));

        assertThat(StringUtils.lastIndexOfAny("dings bums boms", new String[] {"nix", "da"}), is(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLeftIAE() {
        StringUtils.left(null, -1);
    }

    @Test
    public void testLeft() {
        assertThat(StringUtils.left(null, 4), nullValue());

        assertThat(StringUtils.left("dingsbums", 4), is("ding"));

        assertThat(StringUtils.left("dingsbums", 40), is("dingsbums"));

        assertThat(StringUtils.left("dingsbums", 0), is(""));
    }

    @Test(expected = NullPointerException.class)
    public void testLeftPad1NPE() {
        StringUtils.leftPad(null, 0);
    }

    @Test
    public void testLeftPad1() {
        assertThat(StringUtils.leftPad("dings", 0), is("dings"));

        assertThat(StringUtils.leftPad("dings", 2), is("dings"));

        assertThat(StringUtils.leftPad("dings", 10), is("     dings"));
    }

    @Test(expected = NullPointerException.class)
    public void testLeftPad2NPE1() {
        StringUtils.leftPad(null, 0, null);
    }

    @Test(expected = NullPointerException.class)
    public void testLeftPad2NPE2() {
        StringUtils.leftPad("dings", 0, null);
    }

    @Test(expected = NullPointerException.class)
    public void testLeftPad2NPE3() {
        StringUtils.leftPad(null, 0, "*");
    }

    @Test
    public void testLeftPad2() {
        assertThat(StringUtils.leftPad("dings", 0, "*"), is("dings"));

        assertThat(StringUtils.leftPad("dings", 2, "*"), is("dings"));

        assertThat(StringUtils.leftPad("dings", 10, "*"), is("*****dings"));
    }

    @Test
    public void testLowerCase() {
        assertThat(StringUtils.lowerCase(null), nullValue());

        assertThat(StringUtils.lowerCase("dinGSbuMS"), is("dingsbums"));

        assertThat(StringUtils.lowerCase(""), is(""));
    }

    @Test(expected = NullPointerException.class)
    public void testLowerCaseFirstLetterNPE() {
        StringUtils.lowercaseFirstLetter(null);
    }

    @Test
    public void testLowerCaseFirstLetter() {
        assertThat(StringUtils.lowercaseFirstLetter("Dings Bums"), is("dings Bums"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMidNegativeLen() {
        StringUtils.mid(null, 0, -2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testMidWrongPos() {
        StringUtils.mid(null, -2, 3);
    }

    @Test
    public void testMid() {
        assertThat(StringUtils.mid(null, 0, 0), nullValue());

        assertThat(StringUtils.mid("dings bums", 0, 0), is(""));

        assertThat(StringUtils.mid("dings bums", 3, 4), is("gs b"));
    }

    @Test(expected = NullPointerException.class)
    public void testOverlayStringNPE1() {
        StringUtils.overlayString(null, null, 0, 0);
    }

    @Test(expected = NullPointerException.class)
    public void testOverlayStringNPE2() {
        StringUtils.overlayString("dings", null, 0, 0);
    }

    @Test(expected = NullPointerException.class)
    public void testOverlayStringNPE3() {
        StringUtils.overlayString(null, "bums", 0, 0);
    }

    @Test
    public void testOverlayString() {
        assertThat(StringUtils.overlayString("dings", "bums", 0, 0), is("bumsdings"));

        assertThat(StringUtils.overlayString("dings", "bums", 2, 4), is("dibumss"));
    }

    @Test(expected = NullPointerException.class)
    public void testPrechompNPE1() {
        StringUtils.prechomp(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testPrechompNPE2() {
        StringUtils.prechomp("dings", null);
    }

    @Test(expected = NullPointerException.class)
    public void testPrechompNPE3() {
        StringUtils.prechomp(null, "bums");
    }

    @Test
    public void testPrechomp() {
        assertThat(StringUtils.prechomp("dings bums", " "), is("bums"));

        assertThat(StringUtils.prechomp("dings bums", "nix"), is("dings bums"));
    }

    @Test
    public void testQuoteAndEscape1() {
        assertThat(StringUtils.quoteAndEscape(null, '+'), nullValue());

        assertThat(StringUtils.quoteAndEscape("", '+'), is(""));

        assertThat(StringUtils.quoteAndEscape("abc", '"'), is("abc"));

        assertThat(StringUtils.quoteAndEscape("a\"bc", '"'), is("\"a\\\"bc\""));

        assertThat(StringUtils.quoteAndEscape("a\'bc", '\''), is("\'a\\'bc\'"));

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\''), is("a\"bc"));
    }

    @Test
    public void testQuoteAndEscape2() {
        assertThat(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}), nullValue());

        assertThat(StringUtils.quoteAndEscape("", '+', new char[] {'"'}), is(""));

        assertThat(StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}), is("abc"));

        assertThat(StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}), is("\"a\\\"bc\""));

        assertThat(StringUtils.quoteAndEscape("a\'bc", '\'', new char[] {'"'}), is("\'a\\'bc\'"));

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}), is("a\"bc"));

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\'', '"'}), is("\'a\"bc\'"));
    }

    @Test
    public void testQuoteAndEscape3() {
        assertThat(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}, '\\', false), nullValue());

        assertThat(StringUtils.quoteAndEscape("", '+', new char[] {'"'}, '\\', false), is(""));

        assertThat(StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}, '\\', false), is("abc"));

        assertThat(StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}, '\\', false), is("\"a\\\"bc\""));

        assertThat(StringUtils.quoteAndEscape("a\'bc", '\'', new char[] {'"'}, '\\', false), is("a\'bc"));

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}, '\\', false), is("a\"bc"));

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\'', '"'}, '\\', false), is("\'a\\\"bc\'"));

        // with force flag
        assertThat(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}, '\\', true), nullValue());

        assertThat(StringUtils.quoteAndEscape("", '+', new char[] {'"'}, '\\', true), is("++"));

        assertThat(StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}, '\\', true), is("\"abc\""));

        assertThat(StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}, '\\', true), is("\"a\\\"bc\""));

        assertThat(StringUtils.quoteAndEscape("a\'bc", '\'', new char[] {'"'}, '\\', true), is("\'a\'bc\'"));

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}, '\\', true), is("\'a\"bc\'"));

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\'', '"'}, '\\', true), is("\'a\\\"bc\'"));
    }

    @Test
    public void testQuoteAndEscape4() {
        assertThat(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}, new char[] {'"'}, '\\', false), nullValue());

        assertThat(StringUtils.quoteAndEscape("", '+', new char[] {'"'}, new char[] {'"'}, '\\', false), is(""));

        assertThat(StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}, new char[] {'"'}, '\\', false), is("abc"));

        assertThat(
                StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}, new char[] {'"'}, '\\', false),
                is("\"a\\\"bc\""));

        assertThat(
                StringUtils.quoteAndEscape("a\'bc", '\'', new char[] {'"'}, new char[] {'"'}, '\\', false),
                is("a\'bc"));

        assertThat(
                StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}, new char[] {'"'}, '\\', false),
                is("\'a\"bc\'"));

        assertThat(
                StringUtils.quoteAndEscape("\'a\"bc\'", '\'', new char[] {'\'', '"'}, new char[] {'"'}, '\\', false),
                is("\'a\"bc\'"));

        // with force flag
        assertThat(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}, new char[] {'"'}, '\\', true), nullValue());

        assertThat(StringUtils.quoteAndEscape("", '+', new char[] {'"'}, new char[] {'"'}, '\\', true), is("++"));

        assertThat(
                StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}, new char[] {'"'}, '\\', true), is("\"abc\""));

        assertThat(
                StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}, new char[] {'"'}, '\\', true),
                is("\"a\\\"bc\""));

        assertThat(
                StringUtils.quoteAndEscape("a\'bc", '\'', new char[] {'"'}, new char[] {'"'}, '\\', true),
                is("\'a\'bc\'"));

        assertThat(
                StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}, new char[] {'"'}, '\\', true),
                is("\'a\"bc\'"));

        assertThat(
                StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\'', '"'}, new char[] {'"'}, '\\', true),
                is("\'a\\\"bc\'"));
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveAndHumpNPE1() {
        StringUtils.removeAndHump(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveAndHumpNPE2() {
        StringUtils.removeAndHump("dings", null);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveAndHumpNPE3() {
        StringUtils.removeAndHump(null, "bums");
    }

    @Test
    public void testRemoveAndHump() {
        assertThat(StringUtils.removeAndHump("dings", "bums"), is("Ding"));

        assertThat(StringUtils.removeAndHump("this-is-it", "-"), is("ThisIsIt"));

        assertThat(StringUtils.removeAndHump("THIS-IS-IT", "-"), is("THISISIT"));
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveDuplicateWhitespaceNPE() {
        StringUtils.removeDuplicateWhitespace(null);
    }

    @Test
    public void testRemoveDuplicateWhitespace() {
        assertThat(StringUtils.removeDuplicateWhitespace("dings"), is("dings"));

        assertThat(StringUtils.removeDuplicateWhitespace("dings bums"), is("dings bums"));

        assertThat(StringUtils.removeDuplicateWhitespace("dings  bums"), is("dings bums"));

        assertThat(StringUtils.removeDuplicateWhitespace("dings \t bums"), is("dings bums"));
    }

    @Test(expected = NullPointerException.class)
    public void testRepeatNPE() {
        StringUtils.repeat(null, 0);
    }

    @Test(expected = NegativeArraySizeException.class)
    public void testRepeatNegativeAmount() {
        StringUtils.repeat("dings", -1);
    }

    @Test
    public void testRepeat() {
        assertThat(StringUtils.repeat("dings", 0), is(""));

        assertThat(StringUtils.repeat("dings", 1), is("dings"));

        assertThat(StringUtils.repeat("dings", 3), is("dingsdingsdings"));
    }

    @Test
    public void testReplaceChar() {
        assertThat(StringUtils.replace(null, 'i', 'o'), nullValue());

        assertThat(StringUtils.replace("dings", 'i', 'o'), is("dongs"));

        assertThat(StringUtils.replace("dingsbims", 'i', 'o'), is("dongsboms"));

        assertThat(StringUtils.replace("dings", 'x', 'o'), is("dings"));
    }

    @Test
    public void testReplace2CharMax() {
        assertThat(StringUtils.replace(null, 'i', 'o', 0), nullValue());

        assertThat(StringUtils.replace("dingsibumsi", 'i', 'o', 3), is("dongsobumso"));

        assertThat(StringUtils.replace("dingsibumsi", 'i', 'o', 2), is("dongsobumsi"));

        assertThat(StringUtils.replace("dingsibumsi", 'i', 'o', 0), is("dongsobumso"));

        assertThat(StringUtils.replace("dingsibumsi", 'i', 'o', -2), is("dongsobumso"));

        assertThat(StringUtils.replace("dings", 'x', 'o', 2), is("dings"));
    }

    @Test
    public void testReplaceString() {
        assertThat(StringUtils.replace(null, "in", "ox"), nullValue());

        assertThat(StringUtils.replace("dings", "in", "ox"), is("doxgs"));

        assertThat(StringUtils.replace("dingsbins", "in", "ox"), is("doxgsboxs"));

        assertThat(StringUtils.replace("dings", "nin", "ox"), is("dings"));
    }

    @Test
    public void testReplace2StringMax() {
        assertThat(StringUtils.replace(null, "in", "ox", 0), nullValue());

        assertThat(StringUtils.replace("dingsibumsi", "si", "xo", 3), is("dingxobumxo"));

        assertThat(StringUtils.replace("dingsibumsi", "si", "xo", 2), is("dingxobumxo"));

        assertThat(StringUtils.replace("dingsibumsi", "si", "xo", 1), is("dingxobumsi"));

        assertThat(StringUtils.replace("dingsibumsi", "si", "xo", 0), is("dingxobumxo"));

        assertThat(StringUtils.replace("dingsibumsi", "si", "xo", -2), is("dingxobumxo"));

        assertThat(StringUtils.replace("dings", "si", "xo", 2), is("dings"));
    }

    @Test
    public void testReplaceOnceChar() {
        assertThat(StringUtils.replaceOnce(null, 'i', 'o'), nullValue());

        assertThat(StringUtils.replaceOnce("dingsibumsi", 'i', 'o'), is("dongsibumsi"));

        assertThat(StringUtils.replaceOnce("dings", 'x', 'o'), is("dings"));
    }

    @Test
    public void testReplaceOnceString() {
        assertThat(StringUtils.replaceOnce(null, "in", "ox"), nullValue());

        assertThat(StringUtils.replaceOnce("dingsibumsi", "si", "xo"), is("dingxobumsi"));

        assertThat(StringUtils.replaceOnce("dings", "si", "xo"), is("dings"));
    }

    @Test
    public void testReverse() {
        assertThat(StringUtils.reverse(null), nullValue());

        assertThat(StringUtils.reverse(""), is(""));

        assertThat(StringUtils.reverse("dings"), is("sgnid"));

        assertThat(StringUtils.reverse("  dings "), is(" sgnid  "));
    }

    @Test(expected = NullPointerException.class)
    public void testReverseDelimitedStringNPE1() {
        StringUtils.reverseDelimitedString(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testReverseDelimitedStringNPE2() {
        StringUtils.reverseDelimitedString(null, " ");
    }

    @Test
    public void testReverseDelimitedString() {
        assertThat(StringUtils.reverseDelimitedString("dings", null), is("dings"));

        assertThat(StringUtils.reverseDelimitedString("", " "), is(""));

        assertThat(StringUtils.reverseDelimitedString("dings", " "), is("dings"));

        assertThat(StringUtils.reverseDelimitedString("  dings ", " "), is("dings"));

        assertThat(StringUtils.reverseDelimitedString("dings bums", " "), is("bums dings"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRightIAE1() {
        StringUtils.right(null, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRightIAE2() {
        StringUtils.right("dings", -1);
    }

    @Test
    public void testRight() {
        assertThat(StringUtils.right(null, 0), nullValue());

        assertThat(StringUtils.right("dings", 0), is(""));

        assertThat(StringUtils.right("dings", 3), is("ngs"));

        assertThat(StringUtils.right("dings ", 3), is("gs "));
    }

    @Test(expected = NullPointerException.class)
    public void testRightPad1NPE() {
        StringUtils.rightPad(null, 0);
    }

    @Test
    public void testRightPad1() {
        assertThat(StringUtils.rightPad("dings", 0), is("dings"));

        assertThat(StringUtils.rightPad("dings", 3), is("dings"));

        assertThat(StringUtils.rightPad("dings", 10), is("dings     "));
    }

    @Test(expected = NullPointerException.class)
    public void testRightPad2NPE1() {
        StringUtils.rightPad(null, 0, null);
    }

    @Test(expected = NullPointerException.class)
    public void testRightPad2NPE2() {
        StringUtils.rightPad("dings", 0, null);
    }

    @Test(expected = NullPointerException.class)
    public void testRightPad2NPE23() {
        StringUtils.rightPad(null, 0, "+");
    }

    @Test
    public void testRightPad2() {
        assertThat(StringUtils.rightPad("dings", 0, "+"), is("dings"));

        assertThat(StringUtils.rightPad("dings", 3, "+"), is("dings"));

        assertThat(StringUtils.rightPad("dings", 10, "+"), is("dings+++++"));
    }

    @Test(expected = NullPointerException.class)
    public void testSplit1NPE() {
        StringUtils.split(null);
    }

    @Test
    public void testSplit1() {
        assertThat(StringUtils.split("dings"), is(new String[] {"dings"}));

        assertThat(StringUtils.split("dings bums"), is(new String[] {"dings", "bums"}));
    }

    @Test(expected = NullPointerException.class)
    public void testSplit2NPE1() {
        StringUtils.split(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testSplit2NPE2() {
        StringUtils.split(null, " ");
    }

    @Test
    public void testSplit2() {
        assertThat(StringUtils.split("dings", null), is(new String[] {"dings"}));

        assertThat(StringUtils.split("dings bums", null), is(new String[] {"dings", "bums"}));

        assertThat(StringUtils.split("dings", "+"), is(new String[] {"dings"}));

        assertThat(StringUtils.split("dings+bums", "+"), is(new String[] {"dings", "bums"}));
    }

    @Test(expected = NullPointerException.class)
    public void testSplit3NPE1() {
        StringUtils.split(null, null, 1);
    }

    @Test(expected = NullPointerException.class)
    public void testSplit3NPE2() {
        StringUtils.split(null, " ", 1);
    }

    @Test
    public void testSplit3() {
        assertThat(StringUtils.split("dings", null, 3), is(new String[] {"dings"}));

        assertThat(StringUtils.split("dings bums", null, 3), is(new String[] {"dings", "bums"}));

        assertThat(StringUtils.split("dings", "+", 3), is(new String[] {"dings"}));

        assertThat(StringUtils.split("dings+bums", "+", 3), is(new String[] {"dings", "bums"}));

        assertThat(StringUtils.split("dings+bums", "+", 1), is(new String[] {"dings+bums"}));

        assertThat(StringUtils.split("dings+bums", "+", 0), is(new String[] {"dings", "bums"}));

        assertThat(StringUtils.split("dings+bums", "+", -5), is(new String[] {"dings", "bums"}));
    }

    @Test
    public void testStrip1() {
        assertThat(StringUtils.strip(null), nullValue());

        assertThat(StringUtils.strip("dings"), is("dings"));

        assertThat(StringUtils.strip("  dings \t "), is("dings"));
    }

    @Test
    public void testStrip2() {
        assertThat(StringUtils.strip(null, " "), nullValue());

        assertThat(StringUtils.strip(null, null), nullValue());

        assertThat(StringUtils.strip("dings", " "), is("dings"));

        assertThat(StringUtils.strip("  dings \t ", " "), is("dings \t"));
    }

    @Test
    public void testStripAll1() {
        assertThat(StringUtils.stripAll(null), nullValue());

        assertThat(StringUtils.stripAll(new String[] {}), is(new String[] {}));

        assertThat(StringUtils.stripAll(new String[] {"dings"}), is(new String[] {"dings"}));

        assertThat(StringUtils.stripAll(new String[] {" dings ", "  bums \t  "}), is(new String[] {"dings", "bums"}));
    }

    @Test
    public void testStripAll2() {
        assertThat(StringUtils.stripAll(null, " "), nullValue());

        assertThat(StringUtils.stripAll(new String[] {}, " "), is(new String[] {}));

        assertThat(StringUtils.stripAll(new String[] {"dings"}, " "), is(new String[] {"dings"}));

        assertThat(
                StringUtils.stripAll(new String[] {" dings ", "  bums \t  "}, " "),
                is(new String[] {"dings", "bums \t"}));
    }

    @Test
    public void testStripEnd() {
        assertThat(StringUtils.stripEnd(null, null), nullValue());

        assertThat(StringUtils.stripEnd("dings", null), is("dings"));

        assertThat(StringUtils.stripEnd("  dings \t ", null), is("  dings"));

        assertThat(StringUtils.stripEnd(null, " "), nullValue());

        assertThat(StringUtils.stripEnd("dings", " "), is("dings"));

        assertThat(StringUtils.stripEnd("  dings \t ", " "), is("  dings \t"));
    }

    @Test
    public void testStripStart() {
        assertThat(StringUtils.stripStart(null, null), nullValue());

        assertThat(StringUtils.stripStart("dings", null), is("dings"));

        assertThat(StringUtils.stripStart("  dings \t ", null), is("dings \t "));

        assertThat(StringUtils.stripStart(null, " "), nullValue());

        assertThat(StringUtils.stripStart("dings", " "), is("dings"));

        assertThat(StringUtils.stripStart("  \t dings \t ", " "), is("\t dings \t "));
    }

    @Test
    public void testSubstring1() {
        assertThat(StringUtils.substring(null, 0), nullValue());
        assertThat(StringUtils.substring(null, -3), nullValue());

        assertThat(StringUtils.substring("dings", 2), is("ngs"));

        assertThat(StringUtils.substring("dings", -2), is("gs"));

        assertThat(StringUtils.substring("dings", 20), is(""));
    }

    @Test
    public void testSubstring2() {
        assertThat(StringUtils.substring(null, 0, 2), nullValue());

        assertThat(StringUtils.substring(null, -3, 0), nullValue());

        assertThat(StringUtils.substring("dings", 2, 4), is("ng"));

        assertThat(StringUtils.substring("dings", -2, 4), is("g"));

        assertThat(StringUtils.substring("dings", 20, 23), is(""));

        assertThat(StringUtils.substring("dings", 4, 2), is(""));
    }

    @Test
    public void testSwapCase() {
        assertThat(StringUtils.swapCase(null), nullValue());

        assertThat(StringUtils.swapCase("dings"), is("DINGS"));

        assertThat(StringUtils.swapCase("DinGs"), is("dINgS"));
    }

    @Test
    public void testTrim() {
        assertThat(StringUtils.trim(null), nullValue());

        assertThat(StringUtils.trim("   "), is(""));

        assertThat(StringUtils.trim("  c "), is("c"));

        assertThat(StringUtils.trim("  dings \n  "), is("dings"));
    }

    @Test
    public void testUncapitalise() {
        assertThat(StringUtils.uncapitalise(null), nullValue());

        assertThat(StringUtils.uncapitalise("   "), is("   "));

        assertThat(StringUtils.uncapitalise("dings"), is("dings"));

        assertThat(StringUtils.uncapitalise("Dings"), is("dings"));

        assertThat(StringUtils.uncapitalise("DINGS"), is("dINGS"));
    }

    @Test
    public void testUncapitaliseAllWords() {
        assertThat(StringUtils.uncapitaliseAllWords(null), nullValue());

        assertThat(StringUtils.uncapitaliseAllWords("   "), is("   "));

        assertThat(StringUtils.uncapitaliseAllWords("dings bums"), is("dings bums"));

        assertThat(StringUtils.uncapitaliseAllWords("Dings Bums"), is("dings bums"));

        assertThat(StringUtils.uncapitaliseAllWords("DINGS Bums"), is("dINGS bums"));
    }

    @Test
    public void testUnifyLineSeparators1() {
        String sls = System.lineSeparator();

        assertThat(StringUtils.unifyLineSeparators(null), nullValue());

        assertThat(StringUtils.unifyLineSeparators("   "), is("   "));

        assertThat(StringUtils.unifyLineSeparators("dings\nbums\r\ndongs"), is("dings" + sls + "bums" + sls + "dongs"));
    }

    @Test
    public void testUnifyLineSeparators2() {
        assertThat(StringUtils.unifyLineSeparators(null, "\n"), nullValue());

        assertThat(StringUtils.unifyLineSeparators("   ", "\n"), is("   "));

        assertThat(
                StringUtils.unifyLineSeparators("   ", null) // takes the sytem line separator
                ,
                is("   "));

        assertThat(StringUtils.unifyLineSeparators("dings\nbums\r\ndongs", "\n"), is("dings\nbums\ndongs"));
    }

    @Test
    public void testUppercase() {
        assertThat(StringUtils.upperCase(null), nullValue());

        assertThat(StringUtils.upperCase("   "), is("   "));

        assertThat(StringUtils.upperCase(""), is(""));

        assertThat(StringUtils.upperCase("dings"), is("DINGS"));
    }
}
