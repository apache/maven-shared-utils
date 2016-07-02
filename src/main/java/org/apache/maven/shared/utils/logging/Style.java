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
import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.Ansi.Color;

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;

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

    private final Attribute attribute;

    private final Color color;

    Style( String defaultValue )
    {
        Attribute currentAttribute = null;
        Color currentColor = null;

        String value = System.getProperty( "style." + name().toLowerCase( Locale.ENGLISH ), defaultValue );

        for ( String token : value.split( "," ) )
        {
            for ( Color color : Color.values() )
            {
                if ( color.toString().equalsIgnoreCase( token ) )
                {
                    currentColor = color;
                    break;
                }
            }

            if ( "bold".equalsIgnoreCase( token ) )
            {
                currentAttribute = INTENSITY_BOLD;
            }
        }

        this.attribute = currentAttribute;
        this.color = currentColor;
    }

    void apply( Ansi ansi )
    {
        if ( attribute != null )
        {
            ansi.a( attribute );
        }
        if ( color != null )
        {
            ansi.fg( color );
        }
    }

    @Override
    public String toString()
    {
        if ( attribute == null && color == null )
        {
            return name();
        }
        if ( attribute == null )
        {
            return name() + "=" + color.toString();
        }
        if ( color == null )
        {
            return name() + "=" + attribute.toString();
        }
        return name() + "=" + attribute + "," + color;
    }

}
