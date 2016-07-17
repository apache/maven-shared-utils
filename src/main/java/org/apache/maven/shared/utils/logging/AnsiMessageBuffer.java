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
 * Message buffer implementation that supports ANSI colors through <a href="http://fusesource.github.io/jansi/">Jansi</a>
 * with configurable styles through {@link Style}.
 */
class AnsiMessageBuffer
    implements MessageBuffer
{
    private Ansi ansi;

    AnsiMessageBuffer()
    {
        this( Ansi.ansi() );
    }

    AnsiMessageBuffer( StringBuilder builder )
    {
        this( Ansi.ansi( builder ) );
    }

    AnsiMessageBuffer( int size )
    {
        this( Ansi.ansi( size ) );
    }

    AnsiMessageBuffer( Ansi ansi )
    {
        this.ansi = ansi;
    }

    public AnsiMessageBuffer debug()
    {
        Style.DEBUG.apply( ansi );
        return this;
    }

    public AnsiMessageBuffer info()
    {
        Style.INFO.apply( ansi );
        return this;
    }

    public AnsiMessageBuffer warning()
    {
        Style.WARNING.apply( ansi );
        return this;
    }

    public AnsiMessageBuffer warning( Object message )
    {
        return warning().a( message ).reset();
    }

    public AnsiMessageBuffer error()
    {
        Style.ERROR.apply( ansi );
        return this;
    }

    public AnsiMessageBuffer success()
    {
        Style.SUCCESS.apply( ansi );
        return this;
    }

    public AnsiMessageBuffer success( Object message )
    {
        return success().a( message ).reset();
    }

    public AnsiMessageBuffer failure()
    {
        Style.FAILURE.apply( ansi );
        return this;
    }

    public AnsiMessageBuffer failure( Object message )
    {
        return failure().a( message ).reset();
    }

    public AnsiMessageBuffer strong()
    {
        Style.STRONG.apply( ansi );
        return this;
    }

    public AnsiMessageBuffer strong( Object message )
    {
        return strong().a( message ).reset();
    }

    public AnsiMessageBuffer mojo()
    {
        Style.MOJO.apply( ansi );
        return this;
    }

    public AnsiMessageBuffer mojo( Object message )
    {
        return mojo().a( message ).reset();
    }


    public AnsiMessageBuffer project()
    {
        Style.PROJECT.apply( ansi );
        return this;
    }

    public AnsiMessageBuffer project( Object message )
    {
        return project().a( message ).reset();
    }
    public AnsiMessageBuffer reset()
    {
        ansi.reset();
        return this;
    }

    public AnsiMessageBuffer a( char[] value, int offset, int len )
    {
        ansi.a( value, offset, len );
        return this;
    }

    public AnsiMessageBuffer a( char[] value )
    {
        ansi.a( value );
        return this;
    }

    public AnsiMessageBuffer a( CharSequence value, int start, int end )
    {
        ansi.a( value, start, end );
        return this;
    }

    public AnsiMessageBuffer a( CharSequence value )
    {
        ansi.a( value );
        return this;
    }

    public AnsiMessageBuffer a( Object value )
    {
        ansi.a( value );
        return this;
    }

    public AnsiMessageBuffer newline()
    {
        ansi.newline();
        return this;
    }

    public AnsiMessageBuffer format( String pattern, Object... args )
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
