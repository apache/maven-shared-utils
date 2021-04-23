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

import org.fusesource.jansi.Ansi;

/**
 * Message builder implementation that supports ANSI colors through
 * <a href="http://fusesource.github.io/jansi/">Jansi</a> with configurable styles through {@link Style}.
 */
class AnsiMessageBuilder
    implements MessageBuilder, LoggerLevelRenderer
{
    private Ansi ansi;

    AnsiMessageBuilder()
    {
        this( Ansi.ansi() );
    }

    AnsiMessageBuilder( StringBuilder builder )
    {
        this( Ansi.ansi( builder ) );
    }

    AnsiMessageBuilder( int size )
    {
        this( Ansi.ansi( size ) );
    }

    AnsiMessageBuilder( Ansi ansi )
    {
        this.ansi = ansi;
    }

    public String debug( String message )
    {
        return Style.DEBUG.apply( ansi ).a( message ).reset().toString();
    }

    public String info( String message )
    {
        return Style.INFO.apply( ansi ).a( message ).reset().toString();
    }

    public String warning( String message )
    {
        return Style.WARNING.apply( ansi ).a( message ).reset().toString();
    }

    public String error( String message )
    {
        return Style.ERROR.apply( ansi ).a( message ).reset().toString();
    }

    public AnsiMessageBuilder success( Object message )
    {
        Style.SUCCESS.apply( ansi ).a( message ).reset();
        return this;
    }

    public AnsiMessageBuilder warning( Object message )
    {
        Style.WARNING.apply( ansi ).a( message ).reset();
        return this;
    }

    public AnsiMessageBuilder failure( Object message )
    {
        Style.FAILURE.apply( ansi ).a( message ).reset();
        return this;
    }

    public AnsiMessageBuilder strong( Object message )
    {
        Style.STRONG.apply( ansi ).a( message ).reset();
        return this;
    }

    public AnsiMessageBuilder mojo( Object message )
    {
        Style.MOJO.apply( ansi ).a( message ).reset();
        return this;
    }

    public AnsiMessageBuilder project( Object message )
    {
        Style.PROJECT.apply( ansi ).a( message ).reset();
        return this;
    }

    public AnsiMessageBuilder a( char[] value, int offset, int len )
    {
        ansi.a( value, offset, len );
        return this;
    }

    public AnsiMessageBuilder a( char[] value )
    {
        ansi.a( value );
        return this;
    }

    public AnsiMessageBuilder a( CharSequence value, int start, int end )
    {
        ansi.a( value, start, end );
        return this;
    }

    public AnsiMessageBuilder a( CharSequence value )
    {
        ansi.a( value );
        return this;
    }

    public AnsiMessageBuilder a( Object value )
    {
        ansi.a( value );
        return this;
    }

    public AnsiMessageBuilder newline()
    {
        ansi.newline();
        return this;
    }

    public AnsiMessageBuilder format( String pattern, Object... args )
    {
        ansi.format( pattern, args );
        return this;
    }

    @Override
    public String toString()
    {
        return ansi.toString();
    }
}
