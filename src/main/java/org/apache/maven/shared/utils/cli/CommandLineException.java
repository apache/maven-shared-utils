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
package org.apache.maven.shared.utils.cli;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class CommandLineException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1344773066470228441L;

    /**
     * @param message The message of the exception.
     */
    public CommandLineException(String message) {
        super(message);
    }

    /**
     * @param message The message of the exception.
     * @param cause The problem which caused the exception.
     */
    public CommandLineException(String message, Throwable cause) {
        super(message, cause);
    }
}
