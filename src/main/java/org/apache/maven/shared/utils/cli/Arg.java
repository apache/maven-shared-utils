package org.apache.maven.shared.utils.cli;

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

import java.io.File;

/**
 * 
 */
public interface Arg
{
    /**
     * @param value the value to be set
     */
    void setValue( String value );

    /**
     * @param line the line of arguments
     * @throws CommandLineException in case of unbalanced quotes.
     */
    void setLine( String line ) throws CommandLineException;

    /**
     * @param file the file to be set
     */
    void setFile( File file );

    /**
     * Whether to hide the argument value when a command line prints the arguments.
     *
     * @param mask new state of the {@code mask} property
     * @since 0.6
     */
    void setMask( boolean mask );

}
