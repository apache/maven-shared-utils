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
package org.apache.maven.shared.utils.logging;

import java.util.ArrayList;
import java.util.List;

/**
 * Message builder implementation that supports ANSI colors with configurable styles through {@link Style}.
 * Colors are written as SGR escape sequences, and only when {@link MessageUtils#isColorEnabled()} was true
 * when the builder was created.
 *
 * @deprecated migrate to {@code org.apache.maven.message.MessageBuilderFactory} (Maven 3.10.0 and later)
 *             or {@code org.apache.maven.api.services.MessageBuilderFactory} (Maven 4) once your
 *             plugin or extension requires one of those Maven versions. Until then, keep using
 *             this API; it remains supported.
 */
@Deprecated
class AnsiMessageBuilder implements MessageBuilder, LoggerLevelRenderer {
    private static final String ESC = "\u001B[";

    private final StringBuilder builder;

    private final boolean enabled;

    /** SGR parameters waiting to be written before the next text, so consecutive ones share one sequence. */
    private final List<Integer> attributes = new ArrayList<>(5);

    @SuppressWarnings("checkstyle:magicnumber")
    AnsiMessageBuilder() {
        this(new StringBuilder(80));
    }

    AnsiMessageBuilder(StringBuilder builder) {
        this(builder, MessageUtils.isColorEnabled());
    }

    AnsiMessageBuilder(int size) {
        this(new StringBuilder(size));
    }

    AnsiMessageBuilder(StringBuilder builder, boolean enabled) {
        this.builder = builder;
        this.enabled = enabled;
    }

    public String debug(String message) {
        return Style.DEBUG.apply(this).text(message).reset().toString();
    }

    public String info(String message) {
        return Style.INFO.apply(this).text(message).reset().toString();
    }

    public String warning(String message) {
        return Style.WARNING.apply(this).text(message).reset().toString();
    }

    public String error(String message) {
        return Style.ERROR.apply(this).text(message).reset().toString();
    }

    public AnsiMessageBuilder success(Object message) {
        return Style.SUCCESS.apply(this).text(message).reset();
    }

    public AnsiMessageBuilder warning(Object message) {
        return Style.WARNING.apply(this).text(message).reset();
    }

    public AnsiMessageBuilder failure(Object message) {
        return Style.FAILURE.apply(this).text(message).reset();
    }

    public AnsiMessageBuilder strong(Object message) {
        return Style.STRONG.apply(this).text(message).reset();
    }

    public AnsiMessageBuilder mojo(Object message) {
        return Style.MOJO.apply(this).text(message).reset();
    }

    public AnsiMessageBuilder project(Object message) {
        return Style.PROJECT.apply(this).text(message).reset();
    }

    public AnsiMessageBuilder a(char[] value, int offset, int len) {
        flushAttributes();
        builder.append(value, offset, len);
        return this;
    }

    public AnsiMessageBuilder a(char[] value) {
        flushAttributes();
        builder.append(value);
        return this;
    }

    public AnsiMessageBuilder a(CharSequence value, int start, int end) {
        flushAttributes();
        builder.append(value, start, end);
        return this;
    }

    public AnsiMessageBuilder a(CharSequence value) {
        flushAttributes();
        builder.append(value);
        return this;
    }

    public AnsiMessageBuilder a(Object value) {
        return text(value);
    }

    public AnsiMessageBuilder newline() {
        flushAttributes();
        builder.append(System.lineSeparator());
        return this;
    }

    public AnsiMessageBuilder format(String pattern, Object... args) {
        flushAttributes();
        builder.append(String.format(pattern, args));
        return this;
    }

    @Override
    public String toString() {
        return build();
    }

    @Override
    public String build() {
        flushAttributes();
        return builder.toString();
    }

    void sgr(int attribute) {
        if (enabled) {
            attributes.add(attribute);
        }
    }

    private AnsiMessageBuilder text(Object value) {
        flushAttributes();
        builder.append(value);
        return this;
    }

    private AnsiMessageBuilder reset() {
        sgr(0);
        return this;
    }

    private void flushAttributes() {
        if (attributes.isEmpty()) {
            return;
        }
        builder.append(ESC);
        // a lone reset is written as ESC[m, as Jansi does
        if (attributes.size() > 1 || attributes.get(0) != 0) {
            for (int i = 0; i < attributes.size(); i++) {
                if (i > 0) {
                    builder.append(';');
                }
                builder.append(attributes.get(i));
            }
        }
        builder.append('m');
        attributes.clear();
    }
}
