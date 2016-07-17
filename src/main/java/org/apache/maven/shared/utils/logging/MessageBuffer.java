package org.apache.maven.shared.utils.logging;

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
 * Message buffer that supports configurable styling.
 * @see MessageUtils
 */
public interface MessageBuffer
{
    /**
     * Insert color for DEBUG level display.
     * By default, bold cyan
     */
    MessageBuffer debug();
    
    /**
     * Insert color for INFO level display.
     * By default, bold blue
     */
    MessageBuffer info();
    
    /**
     * Insert color for WARNING level or warning message display.
     * By default, bold yellow
     */
    MessageBuffer warning();
    
    /**
     * Append warning message: equivalent to appending warning color, then message, then reset.
     */
    MessageBuffer warning( Object message );
    
    /**
     * Insert color for ERROR level display.
     * By default, bold red
     */
    MessageBuffer error();
    
    /**
     * Insert color for success message display.
     * By default, bold green
     */
    MessageBuffer success();
    
    /**
     * Append success message: equivalent to appending success color, then message, then reset.
     */
    MessageBuffer success( Object message );
    
    /**
     * Insert color for failure message display.
     * By default, bold red
     */
    MessageBuffer failure();

    /**
     * Append failure message: equivalent to appending failure color, then message, then reset.
     */
    MessageBuffer failure( Object message );
    
    /**
     * Insert color for strong message display.
     * By default, bold
     */
    MessageBuffer strong();

    /**
     * Append strong message: equivalent to appending strong color, then message, then reset.
     */
    MessageBuffer strong( Object message );
    
    /**
     * Insert color for mojo message display.
     * By default, green
     */
    MessageBuffer mojo();

    /**
     * Append mojo message: equivalent to appending mojo color, then message, then reset.
     */
    MessageBuffer mojo( Object message );
    
    /**
     * Insert color for project message display.
     * By default, cyan
     */
    MessageBuffer project();

    /**
     * Append project message: equivalent to appending project color, then message, then reset.
     */
    MessageBuffer project( Object message );
    
    //
    // message building methods modelled after Ansi methods
    //
    /**
     * Reset to default style.
     */
    MessageBuffer reset();

    /**
     * Append content to the buffer.
     */
    MessageBuffer a( char[] value, int offset, int len );

    /**
     * Append content to the buffer.
     */
    MessageBuffer a( char[] value );

    /**
     * Append content to the buffer.
     */
    MessageBuffer a( CharSequence value, int start, int end );

    /**
     * Append content to the buffer.
     */
    MessageBuffer a( CharSequence value );

    /**
     * Append content to the buffer.
     */
    MessageBuffer a( Object value );

    /**
     * Append a newline to the buffer.
     */
    MessageBuffer newline();

    /**
     * Append formatted content to the buffer.
     * @see String#format(String, Object...)
     */
    MessageBuffer format( String pattern, Object... args );
}
