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
 * Colored message utils, to manage colors consistently across plugins (only if Maven version is at least 3.4.0).
 * For Maven version before 3.4.0, message built with this util will never add color.
 * <p>
 * Internally, <a href="http://fusesource.github.io/jansi/">Jansi</a> is used to render
 * <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#Colors">ANSI colors</a> on any platform.
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
     * This method is called by Maven core, and calling it is not necessary in plugins. 
     */
    public static void systemInstall()
    {
        if ( JANSI )
        {
            AnsiConsole.systemInstall();
        }
    }

    /**
     * Undo a previous {@link #systemInstall()}.  If {@link #systemInstall()} was called
     * multiple times, {@link #systemUninstall()} must be called call the same number of times before
     * it is actually uninstalled.
     */
    public static void systemUninstall()
    {
        if ( JANSI )
        {
            AnsiConsole.systemUninstall();
        }
    }

    /**
     * Enables message color (if JAnsi is available).
     * @param flag
     */
    public static void setColorEnabled( boolean flag )
    {
        if ( JANSI )
        {
            Ansi.setEnabled( flag );
        }
    }

    /**
     * Is message color enabled: requires JAnsi available (through Maven) and the color has not been disabled.
     */
    public static boolean isColorEnabled()
    {
        return JANSI ? Ansi.isEnabled() : false;
    }

    /**
     * Create a default message buffer.
     * @return a new buffer
     */
    public static MessageBuilder buffer()
    {
        return JANSI ? new AnsiMessageBuilder() : new PlainMessageBuilder();
    }

    /**
     * Create a message buffer with defined String builder.
     * @return a new buffer
     */
    public static MessageBuilder buffer( StringBuilder builder )
    {
        return JANSI ? new AnsiMessageBuilder( builder ) : new PlainMessageBuilder( builder );
    }

    /**
     * Create a message buffer with an internal buffer of defined size.
     * @return a new buffer
     */
    public static MessageBuilder buffer( int size )
    {
        return JANSI ? new AnsiMessageBuilder( size ) : new PlainMessageBuilder( size );
    }

    /**
     * Remove any ANSI code from a message (colors or other escape sequences).
     * @param msg message eventually containing ANSI codes
     * @return the message with ANSI codes removed
     */
    public static String stripAnsiCodes( String msg )
    {
        return msg.replaceAll( "\u001B\\[[;\\d]*[ -/]*[@-~]", "" );
    }

}
