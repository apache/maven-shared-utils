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
 * Message buffer implementation that just ignores styling, for Maven version earlier than 3.4.0.
 */
class PlainMessageBuffer
    implements MessageBuffer
{
    private StringBuilder buffer;

    PlainMessageBuffer()
    {
        buffer = new StringBuilder();
    }

    PlainMessageBuffer( StringBuilder builder )
    {
        buffer = builder;
    }

    PlainMessageBuffer( int size )
    {
        buffer = new StringBuilder( size );
    }

    public PlainMessageBuffer debug( Object message )
    {
        return a( message );
    }
    
    public PlainMessageBuffer info( Object message )
    {
        return a( message );
    }
    
    public PlainMessageBuffer warning( Object message )
    {
        return a( message );
    }
    
    public PlainMessageBuffer error( Object message )
    {
        return a( message );
    }

    public PlainMessageBuffer success( Object message )
    {
        return a( message );
    }

    public PlainMessageBuffer failure( Object message )
    {
        return a( message );
    }

    public PlainMessageBuffer strong( Object message )
    {
        return a( message );
    }

    public PlainMessageBuffer mojo( Object message )
    {
        return a( message );
    }

    public PlainMessageBuffer project( Object message )
    {
        return a( message );
    }

    public PlainMessageBuffer a( char[] value, int offset, int len )
    {
        buffer.append( value, offset, len );
        return this;
    }

    public PlainMessageBuffer a( char[] value )
    {
        buffer.append( value );
        return this;
    }

    public PlainMessageBuffer a( CharSequence value, int start, int end )
    {
        buffer.append( value, start, end );
        return this;
    }

    public PlainMessageBuffer a( CharSequence value )
    {
        buffer.append( value );
        return this;
    }

    public PlainMessageBuffer a( Object value )
    {
        buffer.append( value );
        return this;
    }

    public PlainMessageBuffer newline()
    {
        buffer.append( System.getProperty( "line.separator" ) );
        return this;
    }

    public PlainMessageBuffer format( String pattern, Object... args )
    {
        buffer.append( String.format( pattern, args ) );
        return this;
    }

    @Override
    public String toString()
    {
        return buffer.toString();
    }
}
