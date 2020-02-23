package org.apache.maven.shared.utils.reflection;

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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class used to instantiate an object using reflection. This utility hides many of the gory details needed to
 * do this.
 * 
 * @author John Casey
 */
final class Reflector
{
    private static final String CONSTRUCTOR_METHOD_NAME = "$$CONSTRUCTOR$$";

    private static final String GET_INSTANCE_METHOD_NAME = "getInstance";

    private final Map<String, Map<String, Map<String, Member>>> classMaps =
        new HashMap<String, Map<String, Map<String, Member>>>();

    /**
     * Ensure no instances of Reflector are created...this is a utility.
     */
    Reflector()
    {
    }

    /**
     * Create a new instance of a class, given the array of parameters... Uses constructor caching to find a constructor
     * that matches the parameter types, either specifically (first choice) or abstractly...
     * 
     * @param theClass The class to instantiate
     * @param params The parameters to pass to the constructor
     * @return The instantiated object
     * @throws ReflectorException In case anything goes wrong here...
     */
    public Object newInstance( Class<?> theClass, Object... params )
        throws ReflectorException
    {
        if ( params == null )
        {
            params = new Object[0];
        }

        Class<?>[] paramTypes = new Class[params.length];

        for ( int i = 0, len = params.length; i < len; i++ )
        {
            paramTypes[i] = params[i].getClass();
        }

        try
        {
            Constructor<?> con = getConstructor( theClass, paramTypes );

            return con.newInstance( params );
        }
        catch ( InstantiationException ex )
        {
            throw new ReflectorException( ex );
        }
        catch ( InvocationTargetException ex )
        {
            throw new ReflectorException( ex );
        }
        catch ( IllegalAccessException ex )
        {
            throw new ReflectorException( ex );
        }
    }

    /**
     * Retrieve the singleton instance of a class, given the array of parameters... Uses constructor caching to find a
     * constructor that matches the parameter types, either specifically (first choice) or abstractly...
     * 
     * @param theClass The class to retrieve the singleton of
     * @param initParams The parameters to pass to the constructor
     * @return The singleton object
     * @throws ReflectorException In case anything goes wrong here...
     */
    public Object getSingleton( Class<?> theClass, Object... initParams )
        throws ReflectorException
    {
        Class<?>[] paramTypes = new Class[initParams.length];

        for ( int i = 0, len = initParams.length; i < len; i++ )
        {
            paramTypes[i] = initParams[i].getClass();
        }

        try
        {
            Method method = getMethod( theClass, GET_INSTANCE_METHOD_NAME, paramTypes );

            return method.invoke( null, initParams );
        }
        catch ( InvocationTargetException ex )
        {
            throw new ReflectorException( ex );
        }
        catch ( IllegalAccessException ex )
        {
            throw new ReflectorException( ex );
        }
    }

    /**
     * Invoke the specified method on the specified target with the specified params...
     * 
     * @param target The target of the invocation
     * @param methodName The method name to invoke
     * @param params The parameters to pass to the method invocation
     * @return The result of the method call
     * @throws ReflectorException In case of an error looking up or invoking the method.
     */
    public Object invoke( Object target, String methodName, Object... params )
        throws ReflectorException
    {
        if ( params == null )
        {
            params = new Object[0];
        }

        Class<?>[] paramTypes = new Class[params.length];

        for ( int i = 0, len = params.length; i < len; i++ )
        {
            paramTypes[i] = params[i].getClass();
        }

        try
        {
            Method method = getMethod( target.getClass(), methodName, paramTypes );

            return method.invoke( target, params );
        }
        catch ( InvocationTargetException ex )
        {
            throw new ReflectorException( ex );
        }
        catch ( IllegalAccessException ex )
        {
            throw new ReflectorException( ex );
        }
    }

    public Object getStaticField( Class<?> targetClass, String fieldName )
        throws ReflectorException
    {
        try
        {
            Field field = targetClass.getField( fieldName );

            return field.get( null );
        }
        catch ( SecurityException e )
        {
            throw new ReflectorException( e );
        }
        catch ( NoSuchFieldException e )
        {
            throw new ReflectorException( e );
        }
        catch ( IllegalArgumentException e )
        {
            throw new ReflectorException( e );
        }
        catch ( IllegalAccessException e )
        {
            throw new ReflectorException( e );
        }
    }

    public Object getField( Object target, String fieldName )
        throws ReflectorException
    {
        return getField( target, fieldName, false );
    }

    public Object getField( Object target, String fieldName, boolean breakAccessibility )
        throws ReflectorException
    {
        Class<?> targetClass = target.getClass();
        while ( targetClass != null )
        {
            try
            {
                Field field = targetClass.getDeclaredField( fieldName );

                boolean accessibilityBroken = false;
                if ( !field.isAccessible() && breakAccessibility )
                {
                    field.setAccessible( true );
                    accessibilityBroken = true;
                }

                Object result = field.get( target );

                if ( accessibilityBroken )
                {
                    field.setAccessible( false );
                }

                return result;
            }
            catch ( SecurityException e )
            {
                throw new ReflectorException( e );
            }
            catch ( NoSuchFieldException e )
            {
                if ( targetClass == Object.class )
                {
                    throw new ReflectorException( e );
                }
                targetClass = targetClass.getSuperclass();
            }
            catch ( IllegalAccessException e )
            {
                throw new ReflectorException( e );
            }
        }
        // Never reached, but needed to satisfy compiler
        return null;
    }

    /**
     * Invoke the specified static method with the specified params...
     * 
     * @param targetClass The target class of the invocation
     * @param methodName The method name to invoke
     * @param params The parameters to pass to the method invocation
     * @return The result of the method call
     * @throws ReflectorException In case of an error looking up or invoking the method.
     */
    public Object invokeStatic( Class<?> targetClass, String methodName, Object... params )
        throws ReflectorException
    {
        if ( params == null )
        {
            params = new Object[0];
        }

        Class<?>[] paramTypes = new Class[params.length];

        for ( int i = 0, len = params.length; i < len; i++ )
        {
            paramTypes[i] = params[i].getClass();
        }

        try
        {
            Method method = getMethod( targetClass, methodName, paramTypes );

            return method.invoke( null, params );
        }
        catch ( InvocationTargetException ex )
        {
            throw new ReflectorException( ex );
        }
        catch ( IllegalAccessException ex )
        {
            throw new ReflectorException( ex );
        }
    }

    /**
     * Return the constructor, checking the cache first and storing in cache if not already there..
     * 
     * @param targetClass The class to get the constructor from
     * @param params The classes of the parameters which the constructor should match.
     * @return the Constructor object that matches, never {@code null}
     * @throws ReflectorException In case we can't retrieve the proper constructor.
     */
    public Constructor<?> getConstructor( Class<?> targetClass, Class<?>... params )
        throws ReflectorException
    {
        Map<String, Member> constructorMap = getConstructorMap( targetClass );

        @SuppressWarnings( "checkstyle:magicnumber" )
        StringBuilder key = new StringBuilder( 200 );

        key.append( "(" );

        for ( Class<?> param : params )
        {
            key.append( param.getName() );
            key.append( "," );
        }

        if ( params.length > 0 )
        {
            key.setLength( key.length() - 1 );
        }

        key.append( ")" );

        Constructor<?> constructor;

        String paramKey = key.toString();

        synchronized ( paramKey.intern() )
        {
            constructor = (Constructor<?>) constructorMap.get( paramKey );

            if ( constructor == null )
            {
                Constructor<?>[] cands = targetClass.getConstructors();

                for ( Constructor<?> cand : cands )
                {
                    Class<?>[] types = cand.getParameterTypes();

                    if ( params.length != types.length )
                    {
                        continue;
                    }

                    for ( int j = 0, len2 = params.length; j < len2; j++ )
                    {
                        if ( !types[j].isAssignableFrom( params[j] ) )
                        {
                            continue;
                        }
                    }

                    // we got it, so store it!
                    constructor = cand;
                    constructorMap.put( paramKey, constructor );
                }
            }
        }

        if ( constructor == null )
        {
            throw new ReflectorException( "Error retrieving constructor object for: " + targetClass.getName()
                + paramKey );
        }

        return constructor;
    }

    public Object getObjectProperty( Object target, String propertyName )
        throws ReflectorException
    {
        Object returnValue;

        if ( propertyName == null || propertyName.trim().length() < 1 )
        {
            throw new ReflectorException( "Cannot retrieve value for empty property." );
        }

        String beanAccessor = "get" + Character.toUpperCase( propertyName.charAt( 0 ) );
        if ( propertyName.trim().length() > 1 )
        {
            beanAccessor += propertyName.substring( 1 ).trim();
        }

        Class<?> targetClass = target.getClass();
        Class<?>[] emptyParams = {};

        Method method = _getMethod( targetClass, beanAccessor, emptyParams );
        if ( method == null )
        {
            method = _getMethod( targetClass, propertyName, emptyParams );
        }

        if ( method != null )
        {
            try
            {
                returnValue = method.invoke( target, new Object[] {} );
            }
            catch ( IllegalAccessException e )
            {
                throw new ReflectorException( "Error retrieving property \'" + propertyName + "\' from \'"
                    + targetClass + "\'", e );
            }
            catch ( InvocationTargetException e )
            {
                throw new ReflectorException( "Error retrieving property \'" + propertyName + "\' from \'"
                    + targetClass + "\'", e );
            }
        }
        else
        {
            returnValue = getField( target, propertyName, true );
            if ( returnValue == null )
            {
                // TODO: Check if exception is the right action! Field exists, but contains null
                throw new ReflectorException( "Neither method: \'" + propertyName + "\' nor bean accessor: \'"
                    + beanAccessor + "\' can be found for class: \'" + targetClass + "\', and retrieval of field: \'"
                    + propertyName + "\' returned null as value." );
            }
        }

        return returnValue;
    }

    /**
     * Return the method, checking the cache first and storing in cache if not already there.
     * 
     * @param targetClass the class to get the method from
     * @param params the classes of the parameters which the method should match.
     * @return the Method object that matches, never {@code null}
     * @throws ReflectorException if we can't retrieve the proper method
     */
    public Method getMethod( Class<?> targetClass, String methodName, Class<?>... params )
        throws ReflectorException
    {
        Method method = _getMethod( targetClass, methodName, params );

        if ( method == null )
        {
            throw new ReflectorException( "Method: \'" + methodName + "\' not found in class: \'" + targetClass
                                          + "\'" );
        }

        return method;
    }

    @SuppressWarnings( "checkstyle:methodname" )
    private Method _getMethod( Class<?> targetClass, String methodName, Class<?>... params )
        throws ReflectorException
    {
        Map<String, Member> methodMap = getMethodMap( targetClass, methodName );

        @SuppressWarnings( "checkstyle:magicnumber" )
        StringBuilder key = new StringBuilder( 200 );

        key.append( "(" );

        for ( Class<?> param : params )
        {
            key.append( param.getName() );
            key.append( "," );
        }

        key.append( ")" );

        Method method;

        String paramKey = key.toString();

        synchronized ( paramKey.intern() )
        {
            method = (Method) methodMap.get( paramKey );

            if ( method == null )
            {
                Method[] cands = targetClass.getMethods();

                for ( Method cand : cands )
                {
                    String name = cand.getName();

                    if ( !methodName.equals( name ) )
                    {
                        continue;
                    }

                    Class<?>[] types = cand.getParameterTypes();

                    if ( params.length != types.length )
                    {
                        continue;
                    }

                    for ( int j = 0, len2 = params.length; j < len2; j++ )
                    {
                        if ( !types[j].isAssignableFrom( params[j] ) )
                        {
                            continue;
                        }
                    }

                    // we got it, so store it!
                    method = cand;
                    methodMap.put( paramKey, method );
                }
            }
        }

        return method;
    }

    /**
     * Retrieve the cache of constructors for the specified class.
     * 
     * @param theClass the class to lookup.
     * @return The cache of constructors.
     * @throws ReflectorException in case of a lookup error.
     */
    private Map<String, Member> getConstructorMap( Class<?> theClass )
        throws ReflectorException
    {
        return getMethodMap( theClass, CONSTRUCTOR_METHOD_NAME );
    }

    /**
     * Retrieve the cache of methods for the specified class and method name.
     * 
     * @param theClass the class to lookup.
     * @param methodName The name of the method to lookup.
     * @return The cache of constructors.
     */
    private Map<String, Member> getMethodMap( Class<?> theClass, String methodName )
    {
        Map<String, Member> methodMap;

        if ( theClass == null )
        {
            return null;
        }

        String className = theClass.getName();

        synchronized ( className.intern() )
        {
            Map<String, Map<String, Member>> classMethods = classMaps.get( className );

            if ( classMethods == null )
            {
                classMethods = new HashMap<String, Map<String, Member>>();
                methodMap = new HashMap<String, Member>();
                classMethods.put( methodName, methodMap );

                classMaps.put( className, classMethods );
            }
            else
            {
                String key = className + "::" + methodName;

                synchronized ( key.intern() )
                {
                    methodMap = classMethods.get( methodName );

                    if ( methodMap == null )
                    {
                        methodMap = new HashMap<String, Member>();
                        classMethods.put( methodName, methodMap );
                    }
                }
            }
        }

        return methodMap;
    }
}
