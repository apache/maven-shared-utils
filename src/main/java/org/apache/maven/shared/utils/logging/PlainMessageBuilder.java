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
 * Message builder implementation that just ignores styling, for Maven version earlier than 3.5.0.
 */
class PlainMessageBuilder
    implements MessageBuilder, LoggerLevelRenderer
{
    private StringBuilder buffer;

    PlainMessageBuilder()
    {
        buffer = new StringBuilder();
    }

    PlainMessageBuilder( StringBuilder builder )
    {
        buffer = builder;
    }

    PlainMessageBuilder( int size )
    {
        buffer = new StringBuilder( size );
    }

    public String debug( String level )
    {
        return a( level ).toString();
    }

    public String info( String level )
    {
        return a( level ).toString();
    }

    public String warning( String level )
    {
        return a( level ).toString();
    }

    public String error( String level )
    {
        return a( level ).toString();
    }

    public PlainMessageBuilder success( Object message )
    {
        return a( message );
    }

    public PlainMessageBuilder warning( Object message )
    {
        return a( message );
    }

    public PlainMessageBuilder failure( Object message )
    {
        return a( message );
    }

    public PlainMessageBuilder strong( Object message )
    {
        return a( message );
    }

    public PlainMessageBuilder mojo( Object message )
    {
        return a( message );
    }

    public PlainMessageBuilder project( Object message )
    {
        return a( message );
    }

    public PlainMessageBuilder a( char[] value, int offset, int len )
    {
        buffer.append( value, offset, len );
        return this;
    }

    public PlainMessageBuilder a( char[] value )
    {
        buffer.append( value );
        return this;
    }

    public PlainMessageBuilder a( CharSequence value, int start, int end )
    {
        buffer.append( value, start, end );
        return this;
    }

    public PlainMessageBuilder a( CharSequence value )
    {
        buffer.append( value );
        return this;
    }

    public PlainMessageBuilder a( Object value )
    {
        buffer.append( value );
        return this;
    }

    public PlainMessageBuilder newline()
    {
        buffer.append( System.getProperty( "line.separator" ) );
        return this;
    }

    public PlainMessageBuilder format( String pattern, Object... args )
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
