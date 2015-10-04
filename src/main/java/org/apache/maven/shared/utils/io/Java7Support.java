package org.apache.maven.shared.utils.io;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Java7 feature detection
 *
 * @author Kristian Rosenvold
 */
public class Java7Support
{

    private static final boolean IS_JAVA7;

    private static Method isSymbolicLink;

    private static Method delete;

    private static Method toPath;

    private static Method exists;

    private static Method toFile;

    private static Method readSymlink;

    private static Method createSymlink;

    private static Object emptyLinkOpts;

    private static Object emptyFileAttributes;

    static
    {
        boolean isJava7x = true;
        try
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> files = cl.loadClass( "java.nio.file.Files" );
            Class<?> path = cl.loadClass( "java.nio.file.Path" );
            Class<?> fa = cl.loadClass( "java.nio.file.attribute.FileAttribute" );
            Class<?> linkOption = cl.loadClass( "java.nio.file.LinkOption" );
            isSymbolicLink = files.getMethod( "isSymbolicLink", path );
            delete = files.getMethod( "delete", path );
            readSymlink = files.getMethod( "readSymbolicLink", path );

            emptyFileAttributes = Array.newInstance( fa, 0 );
            final Object o = emptyFileAttributes;
            createSymlink = files.getMethod( "createSymbolicLink", path, path, o.getClass() );
            emptyLinkOpts = Array.newInstance( linkOption, 0 );
            exists = files.getMethod( "exists", path, emptyLinkOpts.getClass() );
            toPath = File.class.getMethod( "toPath" );
            toFile = path.getMethod( "toFile" );
        }
        catch ( ClassNotFoundException e )
        {
            isJava7x = false;
        }
        catch ( NoSuchMethodException e )
        {
            isJava7x = false;
        }
        IS_JAVA7 = isJava7x;
    }

    /**
     * @param file The file to check for being a symbolic link.
     * @return true if the file is a symlink false otherwise.
     */
    public static boolean isSymLink( @Nonnull File file )
    {
        try
        {
            Object path = toPath.invoke( file );
            return (Boolean) isSymbolicLink.invoke( null, path );
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( e );
        }
        catch ( InvocationTargetException e )
        {
            throw new RuntimeException( e );
        }
    }


    /**
     * @param symlink The sym link.
     * @return The file.
     * @throws IOException in case of error.
     */
    @Nonnull public static File readSymbolicLink( @Nonnull File symlink )
        throws IOException
    {
        try
        {
            Object path = toPath.invoke( symlink );
            Object resultPath =  readSymlink.invoke( null, path );
            return (File) toFile.invoke( resultPath );
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( e );
        }
        catch ( InvocationTargetException e )
        {
            throw new RuntimeException( e );
        }
    }


    /**
     * @param file The file to check.
     * @return true if exist false otherwise.
     * @throws IOException in case of failure.
     */
    public static boolean exists( @Nonnull File file )
        throws IOException
    {
        try
        {
            Object path = toPath.invoke( file );
            final Object invoke = exists.invoke( null, path, emptyLinkOpts );
            return (Boolean) invoke;
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( e );
        }
        catch ( InvocationTargetException e )
        {
            throw (RuntimeException) e.getTargetException();
        }

    }

    /**
     * @param symlink The link name.
     * @param target The target.
     * @return The linked file.
     * @throws IOException in case of an error.
     */
    @Nonnull public static File createSymbolicLink( @Nonnull File symlink,  @Nonnull File target )
        throws IOException
    {
        try
        {
            if ( !exists( symlink ) )
            {
                Object link = toPath.invoke( symlink );
                Object path = createSymlink.invoke( null, link, toPath.invoke( target ), emptyFileAttributes );
                return (File) toFile.invoke( path );
            }
            return symlink;
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( e );
        }
        catch ( InvocationTargetException e )
        {
            final Throwable targetException = e.getTargetException();
            if ( targetException instanceof IOException )
            {
                throw (IOException) targetException;
            }
            else if ( targetException instanceof RuntimeException )
            {
                // java.lang.UnsupportedOperationException: Symbolic links not supported on this operating system
                // java.lang.SecurityException: denies certain permissions see Javadoc
                throw ( RuntimeException ) targetException;
            }
            else
            {
                throw new IOException( targetException.getClass() + ": " + targetException.getLocalizedMessage() );
            }
        }

    }
    /**
     * Performs a nio delete
     * @param file the file to delete
     * @throws IOException in case of error.
     */
    public static void delete( @Nonnull File file )
        throws IOException
    {
        try
        {
            Object path = toPath.invoke( file );
            delete.invoke( null, path );
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( e );
        }
        catch ( InvocationTargetException e )
        {
            throw (IOException) e.getTargetException();
        }
    }

    /**
     * @return true in case of Java 7.
     */
    public static boolean isJava7()
    {
        return IS_JAVA7;
    }

    /**
     * @return true in case of Java7 or greater.
     */
    public static boolean isAtLeastJava7()
    {
        return IS_JAVA7;
    }

}
