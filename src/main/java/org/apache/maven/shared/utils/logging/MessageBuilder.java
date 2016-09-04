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
 * Message builder that supports configurable styling.
 * @see MessageUtils
 * @since 3.1.0
 */
public interface MessageBuilder
{
    /**
     * Append message content in success style.
     * By default, bold green
     */
    MessageBuilder success( Object message );
    
    /**
     * Append message content in warning style.
     * By default, bold yellow
     */
    MessageBuilder warning( Object message );
    
    /**
     * Append message content in failure style.
     * By default, bold red
     */
    MessageBuilder failure( Object message );

    /**
     * Append message content in strong style.
     * By default, bold
     */
    MessageBuilder strong( Object message );
    
    /**
     * Append message content in mojo style.
     * By default, green
     */
    MessageBuilder mojo( Object message );
    
    /**
     * Append message content in project style.
     * By default, cyan
     */
    MessageBuilder project( Object message );
    
    //
    // message building methods modelled after Ansi methods
    //
    /**
     * Append content to the message buffer.
     */
    MessageBuilder a( char[] value, int offset, int len );

    /**
     * Append content to the message buffer.
     */
    MessageBuilder a( char[] value );

    /**
     * Append content to the message buffer.
     */
    MessageBuilder a( CharSequence value, int start, int end );

    /**
     * Append content to the message buffer.
     */
    MessageBuilder a( CharSequence value );

    /**
     * Append content to the message buffer.
     */
    MessageBuilder a( Object value );

    /**
     * Append newline to the message buffer.
     */
    MessageBuilder newline();

    /**
     * Append formatted content to the buffer.
     * @see String#format(String, Object...)
     */
    MessageBuilder format( String pattern, Object... args );
}
