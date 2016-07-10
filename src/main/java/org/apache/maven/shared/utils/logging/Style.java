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
import org.fusesource.jansi.Ansi.Color;

import java.util.Locale;

/**
 * Configurable message styles.
 */
enum Style
{

    DEBUG(   "bold,cyan"   ),
    INFO(    "bold,blue"   ),
    WARNING( "bold,yellow" ),
    ERROR(   "bold,red"    ),
    SUCCESS( "bold,green"  ),
    FAILURE( "bold,red"    ),
    STRONG(  "bold"        ),
    MOJO(    "green"       ),
    PROJECT( "cyan"        );

    private final boolean bold;

    private final Color color;

    private final Color bgColor;

    Style( String defaultValue )
    {
        boolean currentBold = false;
        Color currentColor = null;
        Color currentBgColor = null;

        String value = System.getProperty( "style." + name().toLowerCase( Locale.ENGLISH ),
                                           defaultValue ).toLowerCase( Locale.ENGLISH );

        for ( String token : value.split( "," ) )
        {
            if ( "bold".equals( token ) )
            {
                currentBold = true;
            }
            else if ( token.startsWith( "bg" ) )
            {
                currentBgColor = toColor( token.substring( 2 ) );
            }
            else
            {
                currentColor = toColor( token );
            }
        }

        this.bold = currentBold;
        this.color = currentColor;
        this.bgColor = currentBgColor;
    }

    private static Color toColor( String token )
    {
        for ( Color color : Color.values() )
        {
            if ( color.toString().equalsIgnoreCase( token ) )
            {
                return color;
            }
        }
        return null;
    }

    void apply( Ansi ansi )
    {
        if ( bold )
        {
            ansi.bold();
        }
        if ( color != null )
        {
            ansi.fg( color );
        }
        if ( bgColor != null )
        {
            ansi.bg( bgColor );
        }
    }

    @Override
    public String toString()
    {
        if ( !bold && color == null && bgColor == null )
        {
            return name();
        }
        StringBuilder sb = new StringBuilder();
        if ( bold )
        {
            sb.append( "bold" );
        }
        if ( color != null )
        {
            if ( sb.length() > 0 )
            {
                sb.append(  ',' );
            }
            sb.append( color.name() );
        }
        if ( bgColor != null )
        {
            if ( sb.length() > 0 )
            {
                sb.append(  ',' );
            }
            sb.append( "bg" );
            sb.append( bgColor.name() );
        }
        return name() + '=' + sb;
    }

}
