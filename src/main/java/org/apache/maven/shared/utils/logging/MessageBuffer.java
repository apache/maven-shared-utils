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
     * Append message content for DEBUG level.
     * By default, bold cyan
     */
    MessageBuffer debug( Object message );
    
    /**
     * Append message content for INFO level.
     * By default, bold blue
     */
    MessageBuffer info( Object message );
    
    /**
     * Append message content in warning style or WARNING level.
     * By default, bold yellow
     */
    MessageBuffer warning( Object message );
    
    /**
     * Append message content for ERROR level.
     * By default, bold red
     */
    MessageBuffer error( Object message );
    
    /**
     * Append message content in success style.
     * By default, bold green
     */
    MessageBuffer success( Object message );
    
    /**
     * Append message content in failure style.
     * By default, bold red
     */
    MessageBuffer failure( Object message );

    /**
     * Append message content in strong style.
     * By default, bold
     */
    MessageBuffer strong( Object message );
    
    /**
     * Append message content in mojo style.
     * By default, green
     */
    MessageBuffer mojo( Object message );
    
    /**
     * Append message content in project style.
     * By default, cyan
     */
    MessageBuffer project( Object message );
    
    //
    // message building methods modelled after Ansi methods
    //
    /**
     * Append content to the message buffer.
     */
    MessageBuffer a( char[] value, int offset, int len );

    /**
     * Append content to the message buffer.
     */
    MessageBuffer a( char[] value );

    /**
     * Append content to the message buffer.
     */
    MessageBuffer a( CharSequence value, int start, int end );

    /**
     * Append content to the message buffer.
     */
    MessageBuffer a( CharSequence value );

    /**
     * Append content to the message buffer.
     */
    MessageBuffer a( Object value );

    /**
     * Append newline to the message buffer.
     */
    MessageBuffer newline();

    /**
     * Append formatted content to the buffer.
     * @see String#format(String, Object...)
     */
    MessageBuffer format( String pattern, Object... args );
}
