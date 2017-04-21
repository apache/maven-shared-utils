// CHECKSTYLE_OFF: RegexpHeader
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
/**
 * An API to write Maven messages to console with styled color content, consistently across whole
 * Maven ecosystem (Maven itself or any plugin or extension).
 * <p>
 * Messages are built with instances of {@link org.apache.maven.shared.utils.logging.MessageBuilder MessageBuilder}
 * which provides a fluent API, while error level are colored by slf4j provider with
 * {@link org.apache.maven.shared.utils.logging.LoggerLevelRenderer LoggerLevelRenderer}.
 * <p>
 * {@link org.apache.maven.shared.utils.logging.MessageUtils MessageUtils} gives access to these builders.
 * <p>
 * Plugins can use this API with any Maven version: color
 * just won't be activated when run with Maven version older than 3.5.0.
 * <p>
 * Styles are:<ul>
 * <li><code>debug</code>, <code>info</code>, <code>warning</code> and <code>error</code> for
 * {@link org.apache.maven.shared.utils.logging.LoggerLevelRenderer logger level rendering},</li>
 * <li><code>success</code>, <code>warning</code>, <code>failure</code>, <code>strong</code>, <code>mojo</code>
 * and <code>project</code> for {@link org.apache.maven.shared.utils.logging.MessageBuilder message content}</li>
 * </ul>
 * Default styles colors can be overridden through system properties, that can be set in <code>MAVEN_OPTS</code>
 * environment variable (eventually in <code>.mavenrc</code> script):<ul>
 * <li>system properties are named <code>style.&lt;style name&gt;</code>,</li>
 * <li>values are comma separated combination of <code>bold</code>, <code>&lt;color&gt;</code> and
 * <code>bg&lt;color&gt;</code> (for background), where <code>&lt;color&gt;</code> is
 * an <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#Colors">ANSI color</a>: <code>black</code>,
 * <code>red</code>, <code>green</code>, <code>yellow</code>, <code>blue</code>, <code>magenta</code>,
 * <code>cyan</code> or <code>white</code>, eventually with <code>bright</code> prefix</li>
 * </ul>
 * @since 3.1.0
 */
package org.apache.maven.shared.utils.logging;

