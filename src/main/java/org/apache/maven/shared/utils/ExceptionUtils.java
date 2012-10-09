package org.apache.maven.shared.utils;

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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ExceptionUtils contains helper methods for treating Throwable objects.
 * Please note that lots of the given methods are nowadays not needed anymore
 * with Java &gt; 1.4. With Java-1.4 the Exception class itself got all
 * necessary methods to treat chained Exceptions. The original ExceptionUtils
 * got created when this was not yet the case!
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
class ExceptionUtils
{
    /**
     * The maximum level of nestings evaluated when searching for a root cause.
     * We do this to prevent stack overflows!
     *
     * @see #getRootCause(Throwable)
     */
    private static final int MAX_ROOT_CAUSE_DEPTH = 20;

    private static final CopyOnWriteArrayList<String> specialCauseMethodNames = new CopyOnWriteArrayList<String>();

    static
    {
        specialCauseMethodNames.add( "getException" );
        specialCauseMethodNames.add( "getSourceException" );
        specialCauseMethodNames.add( "getRootCause" );
        specialCauseMethodNames.add( "getCausedByException" );
        specialCauseMethodNames.add( "getNested" );
    }

    /**
     * This method is only here for backward compat reasons.
     * It's original means was to add additional method names
     * which got checked to determine if the Throwable in question
     * is a chained exception.
     * It's not needed anymore in case of java &gt; 1.4 since
     * Throwable itself supports chains nowadays.
     *
     * @param methodName
     */
    @Deprecated
    public static void addCauseMethodName( String methodName )
    {
        specialCauseMethodNames.add( methodName );
    }

    /**
     * The {@link Throwable#getCause()} of the given Throwable.
     *
     * @param throwable
     * @return the cause of the given Throwable, <code>null</code> if no cause exists.
     */
    public static Throwable getCause( Throwable throwable )
    {
        Throwable retVal = throwable.getCause();

        if ( retVal == null )
        {

            retVal =
                getCause( throwable, specialCauseMethodNames.toArray( new String[specialCauseMethodNames.size()] ) );
        }

        return retVal;
    }

    /**
     * Get the cause of the given throwable if any by using the given methodNames
     *
     * @param throwable
     * @param methodNames
     * @return
     */
    public static Throwable getCause( Throwable throwable, String[] methodNames )
    {
        Throwable retVal = null;

        // first try a few standard Exception types we already know
        if ( retVal == null && throwable instanceof SQLException )
        {
            retVal = ( (SQLException) throwable ).getNextException();
        }

        if ( retVal == null && throwable instanceof InvocationTargetException )
        {
            retVal = ( (InvocationTargetException) throwable ).getTargetException();
        }

        if ( retVal == null )
        {
            for ( String methodName : methodNames )
            {
                if ( methodName == null )
                {
                    continue;
                }

                retVal = getCauseByMethodName( throwable, methodName );
                if ( retVal != null )
                {
                    return retVal;
                }
            }
        }

        return retVal;
    }

    /**
     * Internal method to get the cause of a given Throwable by a specified methods name.
     *
     * @param throwable
     * @param methodName
     * @return the cause or <code>null</code> if either the method doesn't exist or there is no cause.
     */
    private static Throwable getCauseByMethodName( Throwable throwable, String methodName )
    {
        Method method;
        try
        {
            method = throwable.getClass().getMethod( methodName, null );
        }
        catch ( NoSuchMethodException e )
        {
            return null;
        }
        catch ( SecurityException e )
        {
            return null;
        }

        if ( method.getReturnType() == null || !method.getReturnType().isAssignableFrom( Throwable.class ) )
        {
            return null;
        }

        try
        {
            return (Throwable) method.invoke( throwable );
        }
        catch ( IllegalAccessException e )
        {
            return null;
        }
        catch ( InvocationTargetException e )
        {
            return null;
        }
    }


    /**
     * Go down the {@link Throwable#getCause()} chain to find the
     * source of the problem. For nested Throwables, this will return
     * the recursively deepest Throwable in the chain.
     *
     * @param throwable
     * @return the original cause of the Throwable
     */
    public static Throwable getRootCause( Throwable throwable )
    {
        if ( throwable == null )
        {
            throw new NullPointerException( "Throwable in ExceptionUtils#getRootCause must not be null!" );
        }

        Throwable rootCause = throwable;
        int depth = 0;

        while ( rootCause != null )
        {
            if ( depth >= MAX_ROOT_CAUSE_DEPTH )
            {
                // maximum depth level reached!
                return rootCause;
            }

            Throwable nextRootCause = getCause( rootCause );

            if ( nextRootCause == null )
            {
                if ( depth == 0 )
                {
                    return null;
                }
                else
                {
                    return rootCause;
                }
            }

            rootCause = nextRootCause;
            depth++;
        }

        return rootCause;
    }

    /**
     * Determine the number of causale Throwables. That is the amount of
     * all Throwables in the exception chain.
     *
     * @param throwable
     * @return the amount of Throwables in the exception chain.
     */
    public static int getThrowableCount( Throwable throwable )
    {
        Throwable[] throwables = getThrowables( throwable );

        if ( throwables != null )
        {
            return throwables.length;
        }
        return 0;
    }

    /**
     * Get an array of all Throwables in the 'cause' chain.
     * This will also evaluate special cause rools for SQLExceptions and
     * TargetInvocationExceptions.
     *
     * @param throwable
     * @return array with all causal Throwables
     */
    public static Throwable[] getThrowables( Throwable throwable )
    {
        ArrayList<Throwable> throwables = new ArrayList<Throwable>();

        Throwable rootCause = throwable;
        int depth = 0;

        while ( rootCause != null )
        {
            if ( depth >= MAX_ROOT_CAUSE_DEPTH )
            {
                // maximum depth level reached!
                break;
            }

            throwables.add( rootCause );

            Throwable nextRootCause = getCause( rootCause );

            if ( nextRootCause == null )
            {
                break;
            }

            rootCause = nextRootCause;
            depth++;
        }

        return throwables.toArray( new Throwable[throwables.size()] );

    }

    /**
     * Determines all the nested Throwables and calculate the index of the
     * Throwable with the given type
     *
     * @param throwable
     * @param type
     * @return the index of the type in the Throwable chain, or <code>-1</code> if it isn't contained.
     * @see #indexOfThrowable(Throwable, Class, int)
     */
    public static int indexOfThrowable( Throwable throwable, Class<? extends Throwable> type )
    {
        return indexOfThrowable( throwable, type, 0 );
    }

    /**
     * Determines all the nested Throwables and calculate the index of the
     * Throwable with the given type starting with the given fromIndex
     *
     * @param throwable
     * @param type
     * @param fromIndex the index to start with
     * @return the index of the type in the Throwable chain, or <code>-1</code> if it isn't contained.
     * @see #indexOfThrowable(Throwable, Class)
     */
    public static int indexOfThrowable( Throwable throwable, Class<? extends Throwable> type, int fromIndex )
    {
        if ( throwable == null )
        {
            // this is how the old plexus method failed ...
            throw new IndexOutOfBoundsException( "Throwable to check must not be null" );
        }
        if ( fromIndex < 0 )
        {
            // this is how the old plexus method failed ...
            throw new IndexOutOfBoundsException( "fromIndex must be > 0 but was: " + fromIndex );
        }

        if ( type != null )
        {
            Throwable[] throwables = getThrowables( throwable );

            if ( fromIndex >= throwables.length )
            {
                throw new IndexOutOfBoundsException( "fromIndex is too large" );
            }

            for ( int i = fromIndex; i < throwables.length; i++ )
            {
                Throwable t = throwables[i];

                if ( t.getClass().equals( type ) )
                {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Print the stacktrace of root cause of the given Throwable to System.err
     *
     * @param throwable
     * @see #getCause(Throwable)
     */
    public static void printRootCauseStackTrace( Throwable throwable )
    {
        if ( throwable == null )
        {
            // weird, but needed for backward compat...
            throw new IndexOutOfBoundsException( "Throwable param must not be null" );
        }

        Throwable rootCause = getRootCause( throwable );
        if ( rootCause == null )
        {
            rootCause = throwable;
        }
        rootCause.printStackTrace();
    }

    /**
     * Print the stacktrace of root cause of the given Throwable to the PrintStream
     *
     * @param throwable
     * @see #getCause(Throwable)
     */
    public static void printRootCauseStackTrace( Throwable throwable, PrintStream stream )
    {
        Throwable rootCause = getRootCause( throwable );
        if ( rootCause == null )
        {
            rootCause = throwable;
        }
        rootCause.printStackTrace( stream );
    }

    /**
     * Print the stacktrace of root cause of the given Throwable to the PrintWriter
     *
     * @param throwable
     * @see #getCause(Throwable)
     */
    public static void printRootCauseStackTrace( Throwable throwable, PrintWriter writer )
    {
        Throwable rootCause = getRootCause( throwable );
        if ( rootCause == null )
        {
            rootCause = throwable;
        }
        rootCause.printStackTrace( writer );
        writer.flush();
    }

    /**
     * The stacktrace for the given Throwable
     *
     * @param throwable
     * @return String with the Stacktrace of the Throwable
     */
    public static String getStackTrace( Throwable throwable )
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter( stringWriter, true );

        throwable.printStackTrace( printWriter );

        return stringWriter.getBuffer().toString();
    }

    /**
     * The stacktrace for the given Throwable
     *
     * @param throwable
     * @return String with the Stacktrace of the Throwable
     */
    public static String getFullStackTrace( Throwable throwable )
    {
        // nowadays this is the same...

        return getStackTrace( throwable );
    }

    /**
     * Since Throwable itself has a getCause() method since Java-1.4
     * we can safely assume that all Throwables are nested.
     *
     * @param throwable
     * @return
     */
    public static boolean isNestedThrowable( Throwable throwable )
    {
        return throwable != null;

    }

    /**
     * Get all the separate single lines of the stacktrace for the Throwable
     *
     * @param throwable
     * @return the lines of the stack trace for the throwable
     */
    public static String[] getStackFrames( Throwable throwable )
    {
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();

        String[] retVal = new String[stackTraceElements.length + 1];

        retVal[0] = throwable.getClass().getName() + ": " + throwable.getMessage();

        int i = 1;

        for ( StackTraceElement stackTraceElement : stackTraceElements )
        {
            StringBuilder sb = new StringBuilder( "\tat " );
            sb.append( stackTraceElement.getClassName() );
            sb.append( "." );
            sb.append( stackTraceElement.getMethodName() );
            sb.append( "(" ).append( stackTraceElement.getFileName() ).append( "):" );
            sb.append( stackTraceElement.getLineNumber() );

            retVal[i++] = sb.toString();
        }

        return retVal;
    }

}
