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
import org.fusesource.jansi.AnsiConsole;

/**
 * Colored message utils, to manage colors colors consistently across plugins (only if Maven version is at least 3.4).
 * For Maven version before 3.4, message built with this util will never add color.
 */
public class MessageUtils
{
    private static final boolean JANSI;

    static
    {
        boolean jansi = true;
        try
        {
            // JAnsi is provided by Maven core since 3.4.0
            Class.forName( "org.fusesource.jansi.Ansi" );
        }
        catch ( ClassNotFoundException cnfe )
        {
            jansi = false;
        }
        JANSI = jansi;
    }

    /**
     * Install color support.
     */
    public static void systemInstall()
    {
        if ( JANSI )
        {
            AnsiConsole.systemInstall();
        }
    }

    public static void systemUninstall()
    {
        if ( JANSI )
        {
            AnsiConsole.systemUninstall();
        }
    }

    /**
     * Activates message color (if JAnsi is available).
     * @param flag
     */
    public static void setColor( boolean flag )
    {
        if ( JANSI )
        {
            Ansi.setEnabled( flag );
        }
    }

    /**
     * Is message color active: requires JAnsi available (through Maven) and the color has not been disabled.
     * @return
     */
    public static boolean isColor()
    {
        return JANSI ? Ansi.isEnabled() : false;
    }

    /**
     * Create a default message buffer.
     * @return a new buffer
     */
    public static MessageBuffer buffer()
    {
        return JANSI ? new AnsiMessageBuffer() : new PlainMessageBuffer();
    }

    /**
     * Create a message buffer with defined String builder.
     * @return a new buffer
     */
    public static MessageBuffer buffer( StringBuilder builder )
    {
        return JANSI ? new AnsiMessageBuffer( builder ) : new PlainMessageBuffer( builder );
    }

    /**
     * Create a message buffer with an internal buffer of defined size.
     * @return a new buffer
     */
    public static MessageBuffer buffer( int size )
    {
        return JANSI ? new AnsiMessageBuffer( size ) : new PlainMessageBuffer( size );
    }

    /**
     * Remove any ANSI code from a message
     * @param msg message eventually containing ANSI codes
     * @return the message with ANSI codes removed
     */
    public static String stripAnsi( String msg )
    {
        return msg.replaceAll( "\u001B\\[[;\\d]*[ -/]*[@-~]", "" );
    }

}
