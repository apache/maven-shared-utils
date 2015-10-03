package org.apache.maven.shared.utils.reflection;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
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
 *
 * @author stephenc
 */
class ReflectorTestHelper
{
    static String PACKAGE_STATIC_STRING = "package static string";
    protected static String PROTECTED_STATIC_STRING = "protected static string";
    public static String PUBLIC_STATIC_STRING = "public static string";

    private ReflectorTestHelper()
    {
    }

    ReflectorTestHelper( Boolean throwSomething )
    {
        if ( Boolean.TRUE.equals( throwSomething ) )
        {
            throw new HelperException( "Something" );
        }
    }

    protected ReflectorTestHelper( Integer throwCount )
    {
        if ( throwCount != null && throwCount > 0 )
        {
            throw new HelperException( "Something" );
        }
    }

    public ReflectorTestHelper( String throwMessage )
    {
        if ( throwMessage != null && throwMessage.length() > 0  )
        {
            throw new HelperException( throwMessage );
        }
    }

    static ReflectorTestHelper getInstance( Boolean throwSomething )
    {
        if ( Boolean.TRUE.equals( throwSomething ) )
        {
            throw new HelperException( "Something" );
        }
        return new ReflectorTestHelper();
    }

    protected static ReflectorTestHelper getInstance( Integer throwCount )
    {
        if ( throwCount != null && throwCount > 0 )
        {
            throw new HelperException( "Something" );
        }
        return new ReflectorTestHelper();
    }

    public static ReflectorTestHelper getInstance( String throwMessage )
    {
        if ( throwMessage != null && throwMessage.length() > 0 )
        {
            throw new HelperException( throwMessage );
        }
        return new ReflectorTestHelper();
    }

    public ReflectorTestHelper getInstance( String aString, Boolean aBoolean )
    {
        return new ReflectorTestHelper();
    }

    public static class HelperException
        extends RuntimeException
    {
        /**
         * 
         */
        private static final long serialVersionUID = -3395757415194358525L;

        public HelperException()
        {
            super();    //To change body of overridden methods use File | Settings | File Templates.
        }

        public HelperException( String message )
        {
            super( message );    //To change body of overridden methods use File | Settings | File Templates.
        }

        public HelperException( String message, Throwable cause )
        {
            super( message, cause );    //To change body of overridden methods use File | Settings | File Templates.
        }

        public HelperException( Throwable cause )
        {
            super( cause );    //To change body of overridden methods use File | Settings | File Templates.
        }
    }
}
