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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test the {@link StringUtils} class.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
class StringUtilsTest {

    @Test
    void abbreviate_NPE() {
        assertThrows(NullPointerException.class, () -> assertThat(StringUtils.abbreviate(null, 10))
                .isNull());
    }

    @Test
    void abbreviate_MinLength() {
        assertThrows(IllegalArgumentException.class, () -> assertThat(StringUtils.abbreviate("This is a longtext", 3))
                .isEqualTo("T"));
    }

    @Test
    void abbreviate() {
        assertThat(StringUtils.abbreviate("This is a longtext", 10)).isEqualTo("This is...");

        assertThat(StringUtils.abbreviate("This is a longtext", 50)).isEqualTo("This is a longtext");
    }

    @Test
    void abbreviate_Offset_NPE() {
        assertThrows(NullPointerException.class, () -> assertThat(StringUtils.abbreviate(null, 10, 20))
                .isNull());
    }

    @Test
    void abbreviate_Offset_MinLength() {
        assertThrows(
                IllegalArgumentException.class, () -> assertThat(StringUtils.abbreviate("This is a longtext", 10, 3))
                        .isEqualTo("T"));
    }

    @Test
    void abbreviate_Offset() {
        assertThat(StringUtils.abbreviate("This is a longtext", 5, 10)).isEqualTo("...is a...");

        assertThat(StringUtils.abbreviate("This is a longtext", 10, 20)).isEqualTo("This is a longtext");

        assertThat(StringUtils.abbreviate("This is a longtext", 50, 20)).isEqualTo("This is a longtext");
    }

    @Test
    void addAndDeHump_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.addAndDeHump(null));
    }

    @Test
    void addAndDeHump() {
        assertThat(StringUtils.addAndDeHump("lalala")).isEqualTo("lalala");

        assertThat(StringUtils.addAndDeHump("LaLaLa")).isEqualTo("la-la-la");

        assertThat(StringUtils.addAndDeHump("ALLUPPER")).isEqualTo("a-l-l-u-p-p-e-r");
    }

    @Test
    void capitalise() {
        assertThat(StringUtils.capitalise(null)).isNull();

        assertThat(StringUtils.capitalise("startBig")).isEqualTo("StartBig");
    }

    @Test
    void capitaliseAllWords() {
        assertThat(StringUtils.capitaliseAllWords(null)).isNull();

        assertThat(StringUtils.capitaliseAllWords("start all big")).isEqualTo("Start All Big");
    }

    @Test
    void capitalizeFirstLetter_NPE() {
        assertThrows(NullPointerException.class, () -> assertThat(StringUtils.capitalizeFirstLetter(null))
                .isNull());
    }

    @Test
    void capitalizeFirstLetter() {
        assertThat(StringUtils.capitalizeFirstLetter("Dings")).isEqualTo("Dings");

        assertThat(StringUtils.capitalizeFirstLetter("  dings")).isEqualTo("  dings");

        assertThat(StringUtils.capitalizeFirstLetter("start all big")).isEqualTo("Start all big");
    }

    @Test
    void center_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.center(null, 20));
    }

    @Test
    void center() {
        assertThat(StringUtils.center("centerMe", 20)).isEqualTo("      centerMe      ");

        assertThat(StringUtils.center("centerMe", 4)).isEqualTo("centerMe");

        assertThat(StringUtils.center("        centerMe", 20)).isEqualTo("          centerMe  ");
    }

    @Test
    void center_Delim_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.center(null, 20, "*"));
    }

    @Test
    void center_Delim() {
        assertThat(StringUtils.center("centerMe", 20, "*")).isEqualTo("******centerMe******");

        assertThat(StringUtils.center("centerMe", 4, "*")).isEqualTo("centerMe");

        assertThat(StringUtils.center("        centerMe", 20, "*")).isEqualTo("**        centerMe**");
    }

    @Test
    void chomp_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chomp(null));
    }

    @Test
    void chomp() {
        assertThat(StringUtils.chomp("dings")).isEqualTo("dings");

        assertThat(StringUtils.chomp("dings\n")).isEqualTo("dings");

        assertThat(StringUtils.chomp("dings\nbums")).isEqualTo("dings");

        assertThat(StringUtils.chomp("dings\nbums\ndongs")).isEqualTo("dings\nbums");
    }

    @Test
    void chomp_Delim_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chomp(null, "+"));
    }

    @Test
    void chomp_Delim() {
        assertThat(StringUtils.chomp("dings", "+")).isEqualTo("dings");

        assertThat(StringUtils.chomp("dings+", "+")).isEqualTo("dings");

        assertThat(StringUtils.chomp("dings+bums", "+")).isEqualTo("dings");

        assertThat(StringUtils.chomp("dings+bums+dongs", "+")).isEqualTo("dings+bums");
    }

    @Test
    void chompLast_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chompLast(null));
    }

    @Test
    void chompLast() {
        assertThat(StringUtils.chompLast("dings")).isEqualTo("dings");

        assertThat(StringUtils.chompLast("\n")).isEqualTo("");

        assertThat(StringUtils.chompLast("dings\n")).isEqualTo("dings");

        assertThat(StringUtils.chompLast("dings\nbums")).isEqualTo("dings\nbums");

        assertThat(StringUtils.chompLast("dings\nbums\ndongs\n")).isEqualTo("dings\nbums\ndongs");
    }

    @Test
    void chompLast_Delim_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chompLast(null, "+"));
    }

    @Test
    void chompLast_Delim() {
        assertThat(StringUtils.chompLast("dings", "+")).isEqualTo("dings");

        assertThat(StringUtils.chompLast("+", "+")).isEqualTo("");

        assertThat(StringUtils.chompLast("dings+", "+")).isEqualTo("dings");

        assertThat(StringUtils.chompLast("dings+bums", "+")).isEqualTo("dings+bums");

        assertThat(StringUtils.chompLast("dings+bums+dongs+", "+")).isEqualTo("dings+bums+dongs");
    }

    @Test
    void chop_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chop(null));
    }

    @Test
    void chop() {
        assertThat(StringUtils.chop("dings")).isEqualTo("ding");

        assertThat(StringUtils.chop("x")).isEqualTo("");

        assertThat(StringUtils.chop("dings\n")).isEqualTo("dings");

        assertThat(StringUtils.chop("dings\r\n")).isEqualTo("dings");

        assertThat(StringUtils.chop("dings\n\r")).isEqualTo("dings\n");
    }

    @Test
    void chopNewline_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.chopNewline(null));
    }

    @Test
    void chopNewline() {
        assertThat(StringUtils.chopNewline("dings")).isEqualTo("dings");

        assertThat(StringUtils.chopNewline("x")).isEqualTo("x");

        assertThat(StringUtils.chopNewline("dings\n")).isEqualTo("dings");

        assertThat(StringUtils.chopNewline("dings\r\n")).isEqualTo("dings");

        assertThat(StringUtils.chopNewline("dings\n\r")).isEqualTo("dings\n\r");
    }

    @Test
    void clean() {
        assertThat(StringUtils.clean(null)).isEqualTo("");

        assertThat(StringUtils.clean("   ")).isEqualTo("");

        assertThat(StringUtils.clean("  c ")).isEqualTo("c");

        assertThat(StringUtils.clean("  dings \n  ")).isEqualTo("dings");
    }

    @Test
    void concatenate_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.concatenate(null));
    }

    @Test
    void concatenate() {
        assertThat(StringUtils.concatenate(new String[0])).isEqualTo("");

        assertThat(StringUtils.concatenate(new String[] {"x"})).isEqualTo("x");

        assertThat(StringUtils.concatenate(new String[] {"x", "y", "z"})).isEqualTo("xyz");
    }

    @Test
    void contains_String() {
        assertThat(StringUtils.contains(null, null)).isEqualTo(false);

        assertThat(StringUtils.contains(null, "string")).isEqualTo(false);

        assertThat(StringUtils.contains("string", null)).isEqualTo(false);

        assertThat(StringUtils.contains("string", "")).isEqualTo(true);

        assertThat(StringUtils.contains("string", "in")).isEqualTo(true);

        assertThat(StringUtils.contains("string", "IN")).isEqualTo(false);
    }

    @Test
    void contains_Char() {
        assertThat(StringUtils.contains(null, 'c')).isEqualTo(false);

        assertThat(StringUtils.contains("string", "c")).isEqualTo(false);

        assertThat(StringUtils.contains("string", "")).isEqualTo(true);

        assertThat(StringUtils.contains("string", "r")).isEqualTo(true);

        assertThat(StringUtils.contains("string", "R")).isEqualTo(false);
    }

    @Test
    void countMatches_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.countMatches(null, null));
    }

    @Test
    void countMatches_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.countMatches("this is it", null));
    }

    @Test
    void countMatches() {
        assertThat(StringUtils.countMatches(null, "is")).isEqualTo(0);

        assertThat(StringUtils.countMatches("this is it", "is")).isEqualTo(2);

        assertThat(StringUtils.countMatches("this is it", "notincluded")).isEqualTo(0);
    }

    @Test
    void defaultString() {
        assertThat(StringUtils.defaultString(null)).isEqualTo("");

        assertThat(StringUtils.defaultString("dings")).isEqualTo("dings");
    }

    @Test
    void defaultString_defaultValue() {
        assertThat(StringUtils.defaultString(null, "defaultValue")).isEqualTo("defaultValue");

        assertThat(StringUtils.defaultString("dings", "defaultValue")).isEqualTo("dings");
    }

    @Test
    void deleteWhitespace_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.deleteWhitespace(null));
    }

    @Test
    void deleteWhitespace() {
        assertThat(StringUtils.deleteWhitespace(" \t  \n")).isEqualTo("");

        assertThat(StringUtils.deleteWhitespace(" \t  \b \n")).isEqualTo("\b");

        assertThat(StringUtils.deleteWhitespace("dings")).isEqualTo("dings");

        assertThat(StringUtils.deleteWhitespace("\n  dings \t ")).isEqualTo("dings");
    }

    @Test
    void difference_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.difference(null, null));
    }

    @Test
    void difference_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.difference(null, "another"));
    }

    @Test
    void difference_NPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.difference("this", null));
    }

    @Test
    void difference() {
        assertThat(StringUtils.difference("this", "another")).isEqualTo("another");

        assertThat(StringUtils.difference("I am human", "I am a robot")).isEqualTo("a robot");

        assertThat(StringUtils.difference("I am human", "I AM a robot")).isEqualTo("AM a robot");
    }

    @Test
    void differenceAt_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.differenceAt(null, null));
    }

    @Test
    void differenceAt_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.differenceAt("test", null));
    }

    @Test
    void differenceAt_NPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.differenceAt(null, "test"));
    }

    @Test
    void differenceAt() {
        assertThat(StringUtils.differenceAt("this", "another")).isEqualTo(0);

        assertThat(StringUtils.differenceAt("I am human", "I am a robot")).isEqualTo(5);

        assertThat(StringUtils.differenceAt("I am human", "I AM a robot")).isEqualTo(2);
    }

    @Test
    void endsWithIgnoreCase() {
        assertThat(StringUtils.endsWithIgnoreCase(null, null)).isEqualTo(false);

        assertThat(StringUtils.endsWithIgnoreCase(null, "string")).isEqualTo(false);

        assertThat(StringUtils.endsWithIgnoreCase("string", null)).isEqualTo(false);

        assertThat(StringUtils.endsWithIgnoreCase("string", "ing")).isEqualTo(true);

        assertThat(StringUtils.endsWithIgnoreCase("string", "a string")).isEqualTo(false);

        assertThat(StringUtils.endsWithIgnoreCase("string", "str")).isEqualTo(false);
    }

    @Test
    void equals() {
        assertThat(StringUtils.equals(null, null)).isEqualTo(true);

        assertThat(StringUtils.equals("x", null)).isEqualTo(false);

        assertThat(StringUtils.equals(null, "x")).isEqualTo(false);

        assertThat(StringUtils.equals("X", "x")).isEqualTo(false);

        assertThat(StringUtils.equals("dings", "dings")).isEqualTo(true);
    }

    @Test
    void equalsIgnoreCase() {
        assertThat(StringUtils.equalsIgnoreCase(null, null)).isEqualTo(true);

        assertThat(StringUtils.equalsIgnoreCase("x", null)).isEqualTo(false);

        assertThat(StringUtils.equalsIgnoreCase(null, "x")).isEqualTo(false);

        assertThat(StringUtils.equalsIgnoreCase("X", "x")).isEqualTo(true);

        assertThat(StringUtils.equalsIgnoreCase("dings", "dings")).isEqualTo(true);

        assertThat(StringUtils.equalsIgnoreCase("dings", "diNGs")).isEqualTo(true);
    }

    @Test
    void escape_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.escape(null));
    }

    @Test
    void escape() {
        assertThat(StringUtils.escape("dings")).isEqualTo("dings");

        assertThat(StringUtils.escape("dings\tbums")).isEqualTo("dings\\tbums");

        assertThat(StringUtils.escape("dings\nbums")).isEqualTo("dings\\nbums");
    }

    @Test
    void escape2() {
        assertThat(StringUtils.escape(null, null, '#')).isNull();

        assertThat(StringUtils.escape("dings", new char[] {'\t', '\b'}, '+')).isEqualTo("dings");

        assertThat(StringUtils.escape("dings\tbums", new char[] {'\t', '\b'}, '+'))
                .isEqualTo("dings+\tbums");

        assertThat(StringUtils.escape("dings\nbums", new char[] {'\t', '\b'}, '+'))
                .isEqualTo("dings\nbums");
        assertThat(StringUtils.escape("dings\bbums", new char[] {'\t', '\b'}, '+'))
                .isEqualTo("dings+\bbums");
    }

    @Test
    void getChomp_NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.getChomp(null, null));
    }

    @Test
    void getChomp_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.getChomp("dings", null));
    }

    @Test
    void getChomp_NPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.getChomp(null, "dings"));
    }

    @Test
    void getChomp() {
        assertThat(StringUtils.getChomp("dings-bums", "-")).isEqualTo("-bums");

        assertThat(StringUtils.getChomp("dings-", "-")).isEqualTo("-");

        assertThat(StringUtils.getChomp("dingsbums", "-")).isEqualTo("");
    }

    @Test
    void getNestedString_NPE() {
        assertThrows(NullPointerException.class, () -> assertThat(StringUtils.getNestedString("  +dings+ ", null))
                .isNull());
    }

    @Test
    void getNestedString() {
        assertThat(StringUtils.getNestedString(null, null)).isNull();

        assertThat(StringUtils.getNestedString("  +dings+ ", "+")).isEqualTo("dings");

        assertThat(StringUtils.getNestedString("  +dings+ ", "not")).isNull();
    }

    @Test
    void getNestedString2_NPE1() {
        assertThrows(NullPointerException.class, () -> assertThat(StringUtils.getNestedString("  +dings+ ", null, null))
                .isNull());
    }

    @Test
    void getNestedString2_NPE2() {
        assertThrows(
                NullPointerException.class, () -> assertThat(StringUtils.getNestedString("  +dings+ ", null, "neither"))
                        .isNull());
    }

    @Test
    void getNestedString2() {
        assertThat(StringUtils.getNestedString(null, null, null)).isNull();

        assertThat(StringUtils.getNestedString("  +dings+ ", "not", null)).isNull();

        assertThat(StringUtils.getNestedString("  +dings- ", "+", "-")).isEqualTo("dings");

        assertThat(StringUtils.getNestedString("  +dings+ ", "not", "neither")).isNull();
    }

    @Test
    void getPrechomp_NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.getPrechomp(null, null));
    }

    @Test
    void getPrechomp_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.getPrechomp(null, "bums"));
    }

    @Test
    void getPrechomp() {
        assertThat(StringUtils.getPrechomp("dings bums dongs", "bums")).isEqualTo("dings bums");

        assertThat(StringUtils.getPrechomp("dings bums dongs", "non")).isEqualTo("");
    }

    @Test
    void indexOfAny() {
        assertThat(StringUtils.indexOfAny(null, null)).isEqualTo(-1);

        assertThat(StringUtils.indexOfAny("dings", null)).isEqualTo(-1);

        assertThat(StringUtils.indexOfAny(null, new String[] {})).isEqualTo(-1);

        assertThat(StringUtils.indexOfAny("dings bums dongs", "knuff", "bums")).isEqualTo(6);
    }

    @Test
    void interpolate_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.interpolate(null, null));
    }

    @Test
    void interpolate_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.interpolate("This ${text} will get replaced", null));
    }

    @Test
    void interpolate() {
        Map<String, String> variables = new HashMap<>();
        assertThat(StringUtils.interpolate("This ${text} will get replaced", variables))
                .isEqualTo("This ${text} will get replaced");

        variables.put("text", "with a special content");

        assertThat(StringUtils.interpolate("This ${text} will get replaced", variables))
                .isEqualTo("This with a special content will get replaced");
    }

    @Test
    void isAlpha() {
        assertThat(StringUtils.isAlpha(null)).isEqualTo(false);

        assertThat(StringUtils.isAlpha("2")).isEqualTo(false);

        assertThat(StringUtils.isAlpha("asvsdfSDF")).isEqualTo(true);

        assertThat(StringUtils.isAlpha("asvsdfSDF \t ")).isEqualTo(false);

        assertThat(StringUtils.isAlpha("435afsafd3!")).isEqualTo(false);
    }

    @Test
    void isAlphaSpace() {
        assertThat(StringUtils.isAlphaSpace(null)).isEqualTo(false);

        assertThat(StringUtils.isAlphaSpace("2")).isEqualTo(false);

        assertThat(StringUtils.isAlphaSpace("asvsdfSDF")).isEqualTo(true);

        assertThat(StringUtils.isAlphaSpace("asvsdfSDF  ")).isEqualTo(true);

        assertThat(StringUtils.isAlphaSpace("asvsdfSDF \t ")).isEqualTo(false);

        assertThat(StringUtils.isAlphaSpace("435afsafd3!")).isEqualTo(false);
    }

    @Test
    void isAlphanumeric() {
        assertThat(StringUtils.isAlphanumeric(null)).isEqualTo(false);

        assertThat(StringUtils.isAlphanumeric("2")).isEqualTo(true);

        assertThat(StringUtils.isAlphanumeric("asvsdfSDF")).isEqualTo(true);

        assertThat(StringUtils.isAlphanumeric("asvsdfSDF  ")).isEqualTo(false);

        assertThat(StringUtils.isAlphanumeric("asvsdfSDF \t ")).isEqualTo(false);

        assertThat(StringUtils.isAlphanumeric("435afsafd3!")).isEqualTo(false);

        assertThat(StringUtils.isAlphanumeric("435afsafd3")).isEqualTo(true);

        assertThat(StringUtils.isAlphanumeric("435 ")).isEqualTo(false);

        assertThat(StringUtils.isAlphanumeric("435")).isEqualTo(true);
    }

    @Test
    void isAlphanumericSpace() {
        assertThat(StringUtils.isAlphanumericSpace(null)).isEqualTo(false);

        assertThat(StringUtils.isAlphanumericSpace("2")).isEqualTo(true);

        assertThat(StringUtils.isAlphanumericSpace("asvsdfSDF")).isEqualTo(true);

        assertThat(StringUtils.isAlphanumericSpace("asvsdfSDF  ")).isEqualTo(true);

        assertThat(StringUtils.isAlphanumericSpace("asvsdfSDF \t ")).isEqualTo(false);

        assertThat(StringUtils.isAlphanumericSpace("435afsafd3!")).isEqualTo(false);

        assertThat(StringUtils.isAlphanumericSpace("435afsafd3")).isEqualTo(true);

        assertThat(StringUtils.isAlphanumericSpace("435 ")).isEqualTo(true);

        assertThat(StringUtils.isAlphanumericSpace("435")).isEqualTo(true);
    }

    @Test
    void isBlank() {
        assertThat(StringUtils.isBlank(null)).isEqualTo(true);

        assertThat(StringUtils.isBlank("xx")).isEqualTo(false);

        assertThat(StringUtils.isBlank("xx ")).isEqualTo(false);

        assertThat(StringUtils.isBlank("  ")).isEqualTo(true);

        assertThat(StringUtils.isBlank("  \t ")).isEqualTo(true);

        assertThat(StringUtils.isBlank("  \n ")).isEqualTo(true);
    }

    @Test
    void empty() {
        assertThat(StringUtils.isEmpty(null)).isEqualTo(true);

        assertThat(StringUtils.isEmpty("xx")).isEqualTo(false);

        assertThat(StringUtils.isEmpty("xx ")).isEqualTo(false);

        assertThat(StringUtils.isEmpty("  ")).isEqualTo(true);

        assertThat(StringUtils.isEmpty("  \t ")).isEqualTo(true);

        assertThat(StringUtils.isEmpty("  \n ")).isEqualTo(true);
    }

    @Test
    void notBlank() {
        assertThat(StringUtils.isNotBlank(null)).isEqualTo(false);

        assertThat(StringUtils.isNotBlank("xx")).isEqualTo(true);

        assertThat(StringUtils.isNotBlank("xx ")).isEqualTo(true);

        assertThat(StringUtils.isNotBlank("  ")).isEqualTo(false);

        assertThat(StringUtils.isNotBlank("  \t ")).isEqualTo(false);

        assertThat(StringUtils.isNotBlank("  \n ")).isEqualTo(false);
    }

    @Test
    void notEmpty() {
        assertThat(StringUtils.isNotEmpty(null)).isEqualTo(false);

        assertThat(StringUtils.isNotEmpty("xx")).isEqualTo(true);

        assertThat(StringUtils.isNotEmpty("xx ")).isEqualTo(true);

        assertThat(StringUtils.isNotEmpty("  ")).isEqualTo(true);

        assertThat(StringUtils.isNotEmpty("")).isEqualTo(false);

        assertThat(StringUtils.isNotEmpty("  \t ")).isEqualTo(true);

        assertThat(StringUtils.isNotEmpty("  \n ")).isEqualTo(true);
    }

    @Test
    void isNumeric() {
        assertThat(StringUtils.isNumeric(null)).isEqualTo(false);

        assertThat(StringUtils.isNumeric("2")).isEqualTo(true);

        assertThat(StringUtils.isNumeric("asvsdfSDF")).isEqualTo(false);

        assertThat(StringUtils.isNumeric("asvsdfSDF  ")).isEqualTo(false);

        assertThat(StringUtils.isNumeric("asvsdfSDF \t ")).isEqualTo(false);

        assertThat(StringUtils.isNumeric("435afsafd3!")).isEqualTo(false);

        assertThat(StringUtils.isNumeric("435afsafd3")).isEqualTo(false);

        assertThat(StringUtils.isNumeric("435 ")).isEqualTo(false);

        assertThat(StringUtils.isNumeric("435")).isEqualTo(true);
    }

    @Test
    void isWhitespace() {
        assertThat(StringUtils.isWhitespace(null)).isEqualTo(false);

        assertThat(StringUtils.isWhitespace("xx")).isEqualTo(false);

        assertThat(StringUtils.isWhitespace("xx ")).isEqualTo(false);

        assertThat(StringUtils.isWhitespace("  ")).isEqualTo(true);

        assertThat(StringUtils.isWhitespace("")).isEqualTo(true);

        assertThat(StringUtils.isWhitespace("  \t ")).isEqualTo(true);

        assertThat(StringUtils.isWhitespace("  \n ")).isEqualTo(true);
    }

    @Test
    void join_Array_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.join((Object[]) null, null));
    }

    @Test
    void join_Array() {
        assertThat(StringUtils.join(new Object[0], null)).isEqualTo("");

        assertThat(StringUtils.join(new Object[] {"a", "b", "c"}, null)).isEqualTo("abc");

        assertThat(StringUtils.join(new Object[] {"a", "b", "c"}, "__")).isEqualTo("a__b__c");
    }

    @Test
    void join_Iterator_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.join((Iterator<?>) null, null));
    }

    @Test
    void join_Iterator() {
        ArrayList<String> list = new ArrayList<>();

        assertThat(StringUtils.join(list.iterator(), null)).isEqualTo("");

        list.add("a");
        list.add("b");
        list.add("c");

        assertThat(StringUtils.join(list.iterator(), null)).isEqualTo("abc");

        assertThat(StringUtils.join(list.iterator(), "__")).isEqualTo("a__b__c");
    }

    @Test
    void lastIndexOfAny() {
        assertThat(StringUtils.lastIndexOfAny(null, null)).isEqualTo(-1);

        assertThat(StringUtils.lastIndexOfAny("dings", null)).isEqualTo(-1);

        assertThat(StringUtils.lastIndexOfAny("dings bums boms", "ms", " b")).isEqualTo(13);

        assertThat(StringUtils.lastIndexOfAny("dings bums boms", "nix", "da")).isEqualTo(-1);
    }

    @Test
    void left_IAE() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.left(null, -1));
    }

    @Test
    void left() {
        assertThat(StringUtils.left(null, 4)).isNull();

        assertThat(StringUtils.left("dingsbums", 4)).isEqualTo("ding");

        assertThat(StringUtils.left("dingsbums", 40)).isEqualTo("dingsbums");

        assertThat(StringUtils.left("dingsbums", 0)).isEqualTo("");
    }

    @Test
    void leftPad1_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.leftPad(null, 0));
    }

    @Test
    void leftPad1() {
        assertThat(StringUtils.leftPad("dings", 0)).isEqualTo("dings");

        assertThat(StringUtils.leftPad("dings", 2)).isEqualTo("dings");

        assertThat(StringUtils.leftPad("dings", 10)).isEqualTo("     dings");
    }

    @Test
    void leftPad2_NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.leftPad(null, 0, null));
    }

    @Test
    void leftPad2_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.leftPad("dings", 0, null));
    }

    @Test
    void leftPad2_NPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.leftPad(null, 0, "*"));
    }

    @Test
    void leftPad2() {
        assertThat(StringUtils.leftPad("dings", 0, "*")).isEqualTo("dings");

        assertThat(StringUtils.leftPad("dings", 2, "*")).isEqualTo("dings");

        assertThat(StringUtils.leftPad("dings", 10, "*")).isEqualTo("*****dings");
    }

    @Test
    void lowerCase() {
        assertThat(StringUtils.lowerCase(null)).isNull();

        assertThat(StringUtils.lowerCase("dinGSbuMS")).isEqualTo("dingsbums");

        assertThat(StringUtils.lowerCase("")).isEqualTo("");
    }

    @Test
    void lowerCaseFirstLetter_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.lowercaseFirstLetter(null));
    }

    @Test
    void lowerCaseFirstLetter() {
        assertThat(StringUtils.lowercaseFirstLetter("Dings Bums")).isEqualTo("dings Bums");
    }

    @Test
    void mid_NegativeLen() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.mid(null, 0, -2));
    }

    @Test
    void mid_WrongPos() {
        assertThrows(IndexOutOfBoundsException.class, () -> StringUtils.mid(null, -2, 3));
    }

    @Test
    void mid() {
        assertThat(StringUtils.mid(null, 0, 0)).isNull();

        assertThat(StringUtils.mid("dings bums", 0, 0)).isEqualTo("");

        assertThat(StringUtils.mid("dings bums", 3, 4)).isEqualTo("gs b");
    }

    @Test
    void overlayString_NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.overlayString(null, null, 0, 0));
    }

    @Test
    void overlayString_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.overlayString("dings", null, 0, 0));
    }

    @Test
    void overlayString_NPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.overlayString(null, "bums", 0, 0));
    }

    @Test
    void overlayString() {
        assertThat(StringUtils.overlayString("dings", "bums", 0, 0)).isEqualTo("bumsdings");

        assertThat(StringUtils.overlayString("dings", "bums", 2, 4)).isEqualTo("dibumss");
    }

    @Test
    void prechomp_NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.prechomp(null, null));
    }

    @Test
    void prechomp_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.prechomp("dings", null));
    }

    @Test
    void prechomp_NPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.prechomp(null, "bums"));
    }

    @Test
    void prechomp() {
        assertThat(StringUtils.prechomp("dings bums", " ")).isEqualTo("bums");

        assertThat(StringUtils.prechomp("dings bums", "nix")).isEqualTo("dings bums");
    }

    @Test
    void quoteAndEscape1() {
        assertThat(StringUtils.quoteAndEscape(null, '+')).isNull();

        assertThat(StringUtils.quoteAndEscape("", '+')).isEqualTo("");

        assertThat(StringUtils.quoteAndEscape("abc", '"')).isEqualTo("abc");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '"')).isEqualTo("\"a\\\"bc\"");

        assertThat(StringUtils.quoteAndEscape("a'bc", '\'')).isEqualTo("'a\\'bc'");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'')).isEqualTo("a\"bc");
    }

    @Test
    void quoteAndEscape2() {
        assertThat(StringUtils.quoteAndEscape(null, '+', new char[] {'"'})).isNull();

        assertThat(StringUtils.quoteAndEscape("", '+', new char[] {'"'})).isEqualTo("");

        assertThat(StringUtils.quoteAndEscape("abc", '"', new char[] {'"'})).isEqualTo("abc");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'})).isEqualTo("\"a\\\"bc\"");

        assertThat(StringUtils.quoteAndEscape("a'bc", '\'', new char[] {'"'})).isEqualTo("'a\\'bc'");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''})).isEqualTo("a\"bc");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\'', '"'}))
                .isEqualTo("'a\"bc'");
    }

    @Test
    void quoteAndEscape3() {
        assertThat(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}, '\\', false))
                .isNull();

        assertThat(StringUtils.quoteAndEscape("", '+', new char[] {'"'}, '\\', false))
                .isEqualTo("");

        assertThat(StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}, '\\', false))
                .isEqualTo("abc");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}, '\\', false))
                .isEqualTo("\"a\\\"bc\"");

        assertThat(StringUtils.quoteAndEscape("a'bc", '\'', new char[] {'"'}, '\\', false))
                .isEqualTo("a'bc");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}, '\\', false))
                .isEqualTo("a\"bc");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\'', '"'}, '\\', false))
                .isEqualTo("'a\\\"bc'");

        // with force flag
        assertThat(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}, '\\', true))
                .isNull();

        assertThat(StringUtils.quoteAndEscape("", '+', new char[] {'"'}, '\\', true))
                .isEqualTo("++");

        assertThat(StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}, '\\', true))
                .isEqualTo("\"abc\"");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}, '\\', true))
                .isEqualTo("\"a\\\"bc\"");

        assertThat(StringUtils.quoteAndEscape("a'bc", '\'', new char[] {'"'}, '\\', true))
                .isEqualTo("'a'bc'");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}, '\\', true))
                .isEqualTo("'a\"bc'");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\'', '"'}, '\\', true))
                .isEqualTo("'a\\\"bc'");
    }

    @Test
    void quoteAndEscape4() {
        assertThat(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}, new char[] {'"'}, '\\', false))
                .isNull();

        assertThat(StringUtils.quoteAndEscape("", '+', new char[] {'"'}, new char[] {'"'}, '\\', false))
                .isEqualTo("");

        assertThat(StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}, new char[] {'"'}, '\\', false))
                .isEqualTo("abc");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}, new char[] {'"'}, '\\', false))
                .isEqualTo("\"a\\\"bc\"");

        assertThat(StringUtils.quoteAndEscape("a'bc", '\'', new char[] {'"'}, new char[] {'"'}, '\\', false))
                .isEqualTo("a'bc");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}, new char[] {'"'}, '\\', false))
                .isEqualTo("'a\"bc'");

        assertThat(StringUtils.quoteAndEscape("'a\"bc'", '\'', new char[] {'\'', '"'}, new char[] {'"'}, '\\', false))
                .isEqualTo("'a\"bc'");

        // with force flag
        assertThat(StringUtils.quoteAndEscape(null, '+', new char[] {'"'}, new char[] {'"'}, '\\', true))
                .isNull();

        assertThat(StringUtils.quoteAndEscape("", '+', new char[] {'"'}, new char[] {'"'}, '\\', true))
                .isEqualTo("++");

        assertThat(StringUtils.quoteAndEscape("abc", '"', new char[] {'"'}, new char[] {'"'}, '\\', true))
                .isEqualTo("\"abc\"");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '"', new char[] {'"'}, new char[] {'"'}, '\\', true))
                .isEqualTo("\"a\\\"bc\"");

        assertThat(StringUtils.quoteAndEscape("a'bc", '\'', new char[] {'"'}, new char[] {'"'}, '\\', true))
                .isEqualTo("'a'bc'");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\''}, new char[] {'"'}, '\\', true))
                .isEqualTo("'a\"bc'");

        assertThat(StringUtils.quoteAndEscape("a\"bc", '\'', new char[] {'\'', '"'}, new char[] {'"'}, '\\', true))
                .isEqualTo("'a\\\"bc'");
    }

    @Test
    void removeAndHump_NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.removeAndHump(null, null));
    }

    @Test
    void removeAndHump_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.removeAndHump("dings", null));
    }

    @Test
    void removeAndHump_NPE3() {
        assertThrows(NullPointerException.class, () -> StringUtils.removeAndHump(null, "bums"));
    }

    @Test
    void removeAndHump() {
        assertThat(StringUtils.removeAndHump("dings", "bums")).isEqualTo("Ding");

        assertThat(StringUtils.removeAndHump("this-is-it", "-")).isEqualTo("ThisIsIt");

        assertThat(StringUtils.removeAndHump("THIS-IS-IT", "-")).isEqualTo("THISISIT");
    }

    @Test
    void removeDuplicateWhitespace_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.removeDuplicateWhitespace(null));
    }

    @Test
    void removeDuplicateWhitespace() {
        assertThat(StringUtils.removeDuplicateWhitespace("dings")).isEqualTo("dings");

        assertThat(StringUtils.removeDuplicateWhitespace("dings bums")).isEqualTo("dings bums");

        assertThat(StringUtils.removeDuplicateWhitespace("dings  bums")).isEqualTo("dings bums");

        assertThat(StringUtils.removeDuplicateWhitespace("dings \t bums")).isEqualTo("dings bums");
    }

    @Test
    void repeat_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.repeat(null, 0));
    }

    @Test
    void repeat_NegativeAmount() {
        assertThrows(NegativeArraySizeException.class, () -> StringUtils.repeat("dings", -1));
    }

    @Test
    void repeat() {
        assertThat(StringUtils.repeat("dings", 0)).isEqualTo("");

        assertThat(StringUtils.repeat("dings", 1)).isEqualTo("dings");

        assertThat(StringUtils.repeat("dings", 3)).isEqualTo("dingsdingsdings");
    }

    @Test
    void replace_char() {
        assertThat(StringUtils.replace(null, 'i', 'o')).isNull();

        assertThat(StringUtils.replace("dings", 'i', 'o')).isEqualTo("dongs");

        assertThat(StringUtils.replace("dingsbims", 'i', 'o')).isEqualTo("dongsboms");

        assertThat(StringUtils.replace("dings", 'x', 'o')).isEqualTo("dings");
    }

    @Test
    void replace2_char_max() {
        assertThat(StringUtils.replace(null, 'i', 'o', 0)).isNull();

        assertThat(StringUtils.replace("dingsibumsi", 'i', 'o', 3)).isEqualTo("dongsobumso");

        assertThat(StringUtils.replace("dingsibumsi", 'i', 'o', 2)).isEqualTo("dongsobumsi");

        assertThat(StringUtils.replace("dingsibumsi", 'i', 'o', 0)).isEqualTo("dongsobumso");

        assertThat(StringUtils.replace("dingsibumsi", 'i', 'o', -2)).isEqualTo("dongsobumso");

        assertThat(StringUtils.replace("dings", 'x', 'o', 2)).isEqualTo("dings");
    }

    @Test
    void replace_string() {
        assertThat(StringUtils.replace(null, "in", "ox")).isNull();

        assertThat(StringUtils.replace("dings", "in", "ox")).isEqualTo("doxgs");

        assertThat(StringUtils.replace("dingsbins", "in", "ox")).isEqualTo("doxgsboxs");

        assertThat(StringUtils.replace("dings", "nin", "ox")).isEqualTo("dings");
    }

    @Test
    void replace2_string_max() {
        assertThat(StringUtils.replace(null, "in", "ox", 0)).isNull();

        assertThat(StringUtils.replace("dingsibumsi", "si", "xo", 3)).isEqualTo("dingxobumxo");

        assertThat(StringUtils.replace("dingsibumsi", "si", "xo", 2)).isEqualTo("dingxobumxo");

        assertThat(StringUtils.replace("dingsibumsi", "si", "xo", 1)).isEqualTo("dingxobumsi");

        assertThat(StringUtils.replace("dingsibumsi", "si", "xo", 0)).isEqualTo("dingxobumxo");

        assertThat(StringUtils.replace("dingsibumsi", "si", "xo", -2)).isEqualTo("dingxobumxo");

        assertThat(StringUtils.replace("dings", "si", "xo", 2)).isEqualTo("dings");
    }

    @Test
    void replaceOnce_char() {
        assertThat(StringUtils.replaceOnce(null, 'i', 'o')).isNull();

        assertThat(StringUtils.replaceOnce("dingsibumsi", 'i', 'o')).isEqualTo("dongsibumsi");

        assertThat(StringUtils.replaceOnce("dings", 'x', 'o')).isEqualTo("dings");
    }

    @Test
    void replaceOnce_string() {
        assertThat(StringUtils.replaceOnce(null, "in", "ox")).isNull();

        assertThat(StringUtils.replaceOnce("dingsibumsi", "si", "xo")).isEqualTo("dingxobumsi");

        assertThat(StringUtils.replaceOnce("dings", "si", "xo")).isEqualTo("dings");
    }

    @Test
    void reverse() {
        assertThat(StringUtils.reverse(null)).isNull();

        assertThat(StringUtils.reverse("")).isEqualTo("");

        assertThat(StringUtils.reverse("dings")).isEqualTo("sgnid");

        assertThat(StringUtils.reverse("  dings ")).isEqualTo(" sgnid  ");
    }

    @Test
    void reverseDelimitedString_NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.reverseDelimitedString(null, null));
    }

    @Test
    void reverseDelimitedString_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.reverseDelimitedString(null, " "));
    }

    @Test
    void reverseDelimitedString() {
        assertThat(StringUtils.reverseDelimitedString("dings", null)).isEqualTo("dings");

        assertThat(StringUtils.reverseDelimitedString("", " ")).isEqualTo("");

        assertThat(StringUtils.reverseDelimitedString("dings", " ")).isEqualTo("dings");

        assertThat(StringUtils.reverseDelimitedString("  dings ", " ")).isEqualTo("dings");

        assertThat(StringUtils.reverseDelimitedString("dings bums", " ")).isEqualTo("bums dings");
    }

    @Test
    void right_IAE1() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.right(null, -1));
    }

    @Test
    void right_IAE2() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.right("dings", -1));
    }

    @Test
    void right() {
        assertThat(StringUtils.right(null, 0)).isNull();

        assertThat(StringUtils.right("dings", 0)).isEqualTo("");

        assertThat(StringUtils.right("dings", 3)).isEqualTo("ngs");

        assertThat(StringUtils.right("dings ", 3)).isEqualTo("gs ");
    }

    @Test
    void rightPad1_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.rightPad(null, 0));
    }

    @Test
    void rightPad1() {
        assertThat(StringUtils.rightPad("dings", 0)).isEqualTo("dings");

        assertThat(StringUtils.rightPad("dings", 3)).isEqualTo("dings");

        assertThat(StringUtils.rightPad("dings", 10)).isEqualTo("dings     ");
    }

    @Test
    void rightPad2_NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.rightPad(null, 0, null));
    }

    @Test
    void rightPad2_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.rightPad("dings", 0, null));
    }

    @Test
    void rightPad2_NPE23() {
        assertThrows(NullPointerException.class, () -> StringUtils.rightPad(null, 0, "+"));
    }

    @Test
    void rightPad2() {
        assertThat(StringUtils.rightPad("dings", 0, "+")).isEqualTo("dings");

        assertThat(StringUtils.rightPad("dings", 3, "+")).isEqualTo("dings");

        assertThat(StringUtils.rightPad("dings", 10, "+")).isEqualTo("dings+++++");
    }

    @Test
    void split1_NPE() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null));
    }

    @Test
    void split1() {
        assertThat(StringUtils.split("dings")).isEqualTo(new String[] {"dings"});

        assertThat(StringUtils.split("dings bums")).isEqualTo(new String[] {"dings", "bums"});
    }

    @Test
    void split2_NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null, null));
    }

    @Test
    void split2_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null, " "));
    }

    @Test
    void split2() {
        assertThat(StringUtils.split("dings", null)).isEqualTo(new String[] {"dings"});

        assertThat(StringUtils.split("dings bums", null)).isEqualTo(new String[] {"dings", "bums"});

        assertThat(StringUtils.split("dings", "+")).isEqualTo(new String[] {"dings"});

        assertThat(StringUtils.split("dings+bums", "+")).isEqualTo(new String[] {"dings", "bums"});
    }

    @Test
    void split3_NPE1() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null, null, 1));
    }

    @Test
    void split3_NPE2() {
        assertThrows(NullPointerException.class, () -> StringUtils.split(null, " ", 1));
    }

    @Test
    void split3() {
        assertThat(StringUtils.split("dings", null, 3)).isEqualTo(new String[] {"dings"});

        assertThat(StringUtils.split("dings bums", null, 3)).isEqualTo(new String[] {"dings", "bums"});

        assertThat(StringUtils.split("dings", "+", 3)).isEqualTo(new String[] {"dings"});

        assertThat(StringUtils.split("dings+bums", "+", 3)).isEqualTo(new String[] {"dings", "bums"});

        assertThat(StringUtils.split("dings+bums", "+", 1)).isEqualTo(new String[] {"dings+bums"});

        assertThat(StringUtils.split("dings+bums", "+", 0)).isEqualTo(new String[] {"dings", "bums"});

        assertThat(StringUtils.split("dings+bums", "+", -5)).isEqualTo(new String[] {"dings", "bums"});
    }

    @Test
    void strip1() {
        assertThat(StringUtils.strip(null)).isNull();

        assertThat(StringUtils.strip("dings")).isEqualTo("dings");

        assertThat(StringUtils.strip("  dings \t ")).isEqualTo("dings");
    }

    @Test
    void strip2() {
        assertThat(StringUtils.strip(null, " ")).isNull();

        assertThat(StringUtils.strip(null, null)).isNull();

        assertThat(StringUtils.strip("dings", " ")).isEqualTo("dings");

        assertThat(StringUtils.strip("  dings \t ", " ")).isEqualTo("dings \t");
    }

    @Test
    void stripAll1() {
        assertThat(StringUtils.stripAll(null)).isNull();

        assertThat(StringUtils.stripAll()).isEqualTo(new String[] {});

        assertThat(StringUtils.stripAll("dings")).isEqualTo(new String[] {"dings"});

        assertThat(StringUtils.stripAll(" dings ", "  bums \t  ")).isEqualTo(new String[] {"dings", "bums"});
    }

    @Test
    void stripAll2() {
        assertThat(StringUtils.stripAll(null, " ")).isNull();

        assertThat(StringUtils.stripAll(new String[] {}, " ")).isEqualTo(new String[] {});

        assertThat(StringUtils.stripAll(new String[] {"dings"}, " ")).isEqualTo(new String[] {"dings"});

        assertThat(StringUtils.stripAll(new String[] {" dings ", "  bums \t  "}, " "))
                .isEqualTo(new String[] {"dings", "bums \t"});
    }

    @Test
    void stripEnd() {
        assertThat(StringUtils.stripEnd(null, null)).isNull();

        assertThat(StringUtils.stripEnd("dings", null)).isEqualTo("dings");

        assertThat(StringUtils.stripEnd("  dings \t ", null)).isEqualTo("  dings");

        assertThat(StringUtils.stripEnd(null, " ")).isNull();

        assertThat(StringUtils.stripEnd("dings", " ")).isEqualTo("dings");

        assertThat(StringUtils.stripEnd("  dings \t ", " ")).isEqualTo("  dings \t");
    }

    @Test
    void stripStart() {
        assertThat(StringUtils.stripStart(null, null)).isNull();

        assertThat(StringUtils.stripStart("dings", null)).isEqualTo("dings");

        assertThat(StringUtils.stripStart("  dings \t ", null)).isEqualTo("dings \t ");

        assertThat(StringUtils.stripStart(null, " ")).isNull();

        assertThat(StringUtils.stripStart("dings", " ")).isEqualTo("dings");

        assertThat(StringUtils.stripStart("  \t dings \t ", " ")).isEqualTo("\t dings \t ");
    }

    @Test
    void substring1() {
        assertThat(StringUtils.substring(null, 0)).isNull();
        assertThat(StringUtils.substring(null, -3)).isNull();

        assertThat(StringUtils.substring("dings", 2)).isEqualTo("ngs");

        assertThat(StringUtils.substring("dings", -2)).isEqualTo("gs");

        assertThat(StringUtils.substring("dings", 20)).isEqualTo("");
    }

    @Test
    void substring2() {
        assertThat(StringUtils.substring(null, 0, 2)).isNull();

        assertThat(StringUtils.substring(null, -3, 0)).isNull();

        assertThat(StringUtils.substring("dings", 2, 4)).isEqualTo("ng");

        assertThat(StringUtils.substring("dings", -2, 4)).isEqualTo("g");

        assertThat(StringUtils.substring("dings", 20, 23)).isEqualTo("");

        assertThat(StringUtils.substring("dings", 4, 2)).isEqualTo("");
    }

    @Test
    void swapCase() {
        assertThat(StringUtils.swapCase(null)).isNull();

        assertThat(StringUtils.swapCase("dings")).isEqualTo("DINGS");

        assertThat(StringUtils.swapCase("DinGs")).isEqualTo("dINgS");
    }

    @Test
    void trim() {
        assertThat(StringUtils.trim(null)).isNull();

        assertThat(StringUtils.trim("   ")).isEqualTo("");

        assertThat(StringUtils.trim("  c ")).isEqualTo("c");

        assertThat(StringUtils.trim("  dings \n  ")).isEqualTo("dings");
    }

    @Test
    void uncapitalise() {
        assertThat(StringUtils.uncapitalise(null)).isNull();

        assertThat(StringUtils.uncapitalise("   ")).isEqualTo("   ");

        assertThat(StringUtils.uncapitalise("dings")).isEqualTo("dings");

        assertThat(StringUtils.uncapitalise("Dings")).isEqualTo("dings");

        assertThat(StringUtils.uncapitalise("DINGS")).isEqualTo("dINGS");
    }

    @Test
    void uncapitaliseAllWords() {
        assertThat(StringUtils.uncapitaliseAllWords(null)).isNull();

        assertThat(StringUtils.uncapitaliseAllWords("   ")).isEqualTo("   ");

        assertThat(StringUtils.uncapitaliseAllWords("dings bums")).isEqualTo("dings bums");

        assertThat(StringUtils.uncapitaliseAllWords("Dings Bums")).isEqualTo("dings bums");

        assertThat(StringUtils.uncapitaliseAllWords("DINGS Bums")).isEqualTo("dINGS bums");
    }

    @Test
    void unifyLineSeparators1() {
        String sls = System.lineSeparator();

        assertThat(StringUtils.unifyLineSeparators(null)).isNull();

        assertThat(StringUtils.unifyLineSeparators("   ")).isEqualTo("   ");

        assertThat(StringUtils.unifyLineSeparators("dings\nbums\r\ndongs"))
                .isEqualTo("dings" + sls + "bums" + sls + "dongs");
    }

    @Test
    void unifyLineSeparators2() {
        assertThat(StringUtils.unifyLineSeparators(null, "\n")).isNull();

        assertThat(StringUtils.unifyLineSeparators("   ", "\n")).isEqualTo("   ");

        assertThat(StringUtils.unifyLineSeparators("   ", null)).isEqualTo("   ");

        assertThat(StringUtils.unifyLineSeparators("dings\nbums\r\ndongs", "\n"))
                .isEqualTo("dings\nbums\ndongs");
    }

    @Test
    void uppercase() {
        assertThat(StringUtils.upperCase(null)).isNull();

        assertThat(StringUtils.upperCase("   ")).isEqualTo("   ");

        assertThat(StringUtils.upperCase("")).isEqualTo("");

        assertThat(StringUtils.upperCase("dings")).isEqualTo("DINGS");
    }
}
