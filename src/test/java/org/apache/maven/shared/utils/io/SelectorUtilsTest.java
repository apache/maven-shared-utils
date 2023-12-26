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
package org.apache.maven.shared.utils.io;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the {@link SelectorUtils} class.
 */
@SuppressWarnings("deprecation")
class SelectorUtilsTest {

    @Test
    void matchPatternStart() {
        assertThrows(NullPointerException.class, () -> SelectorUtils.matchPatternStart(null, null));
    }

    @Test
    void emptyStrings() {
        assertTrue(SelectorUtils.matchPatternStart("", ""));
    }

    @Test
    void regexPrefix() throws Exception {
        assertTrue(SelectorUtils.matchPatternStart(
                SelectorUtils.REGEX_HANDLER_PREFIX + File.separator + "aaa" + SelectorUtils.PATTERN_HANDLER_SUFFIX,
                ""));
    }

    @Test
    void antPatternStrings() {
        assertAntDoesNotMatch("/aaa", "");
        assertAntDoesNotMatch("\\aaa", "");
        assertAntMatch("aaa", "");
        assertAntMatch("/aaa/bbb", "/aaa/bbb");
        assertAntMatch("/aaa/**", "/aaa/bbb");
        assertAntDoesNotMatch("/aaa/**", "/ccc/bbb");
        assertAntMatch("/aaa/**", "\\aaa\\bbb");
        assertAntDoesNotMatch("/aaa/**", "\\ccc\\bbb");
        assertAntDoesNotMatch("/aaa/", "\\aaa\\bbb");
    }

    private void assertAntDoesNotMatch(String pattern, String target) {
        assertFalse(SelectorUtils.matchPatternStart(wrapWithAntHandler(pattern), target));
    }

    private void assertAntMatch(String pattern, String target) {
        assertTrue(SelectorUtils.matchPatternStart(wrapWithAntHandler(pattern), target));
    }

    private String wrapWithAntHandler(String val) {
        return SelectorUtils.ANT_HANDLER_PREFIX + val + SelectorUtils.PATTERN_HANDLER_SUFFIX;
    }
}
