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

import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.apache.maven.shared.utils.testhelpers.ExceptionHelper.*;

/**
 * @author Stephen Connolly
 */
public class ReflectorTest
{
    private final Reflector reflector = new Reflector();

    //// newInstance( Class, Object[] )

    @Test( expected = NullPointerException.class )
    public void newInstanceNullNull()
        throws Exception
    {
        reflector.newInstance( (Class<?>)null, (Object)null );
    }

    @Test
    public void newInstanceClassNull()
        throws Exception
    {
        assertThat( reflector.newInstance( Object.class, null ), is( Object.class ) );
    }

    @Test( expected = NullPointerException.class )
    public void newInstanceNullEmptyArray()
        throws Exception
    {
        reflector.newInstance( null, new Object[0] );
    }

    @Test
    public void newInstanceClassEmptyArray()
        throws Exception
    {
        assertThat( reflector.newInstance( Object.class, new Object[0] ), is( Object.class ) );
    }

    @Test( expected = ReflectorException.class )
    public void newInstanceClassInvalidSignature()
        throws Exception
    {
        reflector.newInstance( Object.class, new Object[]{ this } );
    }

    @Test( expected = ReflectorException.class )
    public void newInstancePrivateConstructor()
        throws Exception
    {
        reflector.newInstance( ReflectorTestHelper.class, new Object[0] );
    }

    @Test( expected = IllegalArgumentException.class )
    // // @ReproducesPlexusBug( "Looking up constructors by signature has an unlabelled continue, so finds the wrong constructor" )
    public void newInstancePackageConstructor()
        throws Exception
    {
        reflector.newInstance( ReflectorTestHelper.class, new Object[]{ Boolean.FALSE } );
    }

    @Test( expected = IllegalArgumentException.class )
    // // @ReproducesPlexusBug( "Looking up constructors by signature has an unlabelled continue, so finds the wrong constructor" )
    public void newInstancePackageConstructorThrowsSomething()
        throws Exception
    {
        reflector.newInstance( ReflectorTestHelper.class, new Object[]{ Boolean.TRUE } );
    }

    @Test( expected = IllegalArgumentException.class )
    // // @ReproducesPlexusBug("Looking up constructors by signature has an unlabelled continue, so finds the wrong constructor" )
    public void newInstanceProtectedConstructor()
        throws Exception
    {
        reflector.newInstance( ReflectorTestHelper.class, new Object[]{ 0 } );
    }

    @Test( expected = IllegalArgumentException.class )
    // // @ReproducesPlexusBug( "Looking up constructors by signature has an unlabelled continue, so finds the wrong constructor" )
    public void newInstanceProtectedConstructorThrowsSomething()
        throws Exception
    {
        reflector.newInstance( ReflectorTestHelper.class, new Object[]{ 1 } );
    }

    @Test
    public void newInstancePublicConstructor()
        throws Exception
    {
        assertTrue( reflector.newInstance( ReflectorTestHelper.class, new Object[]{ "" } )
                    instanceof ReflectorTestHelper );
    }

    @Test( expected = NullPointerException.class )
    public void newInstancePublicConstructorNullValue()
        throws Exception
    {
        reflector.newInstance( ReflectorTestHelper.class, new Object[]{ null } );
    }

    @Test
    public void newInstancePublicConstructorThrowsSomething()
        throws Exception
    {
        try
        {
            reflector.newInstance( ReflectorTestHelper.class, new Object[]{ "Message" } );
            fail();
        }
        catch ( ReflectorException e )
        {
            assertThat( e, hasCause( ReflectorTestHelper.HelperException.class ) );
        }
    }

    //// getSingleton( Class, Object[] )

    @Test( expected = NullPointerException.class )
    public void getSingletonNullNull()
        throws Exception
    {
        reflector.getSingleton( (Class<?>)null, (Object)null );
    }

    @Test( expected = NullPointerException.class )
    public void getSingletonClassNull()
        throws Exception
    {
        assertThat( reflector.getSingleton( (Class<?>)Object.class, (Object)null ), is( Object.class ) );
    }

    @Test( expected = NullPointerException.class )
    public void getSingletonNullEmptyArray()
        throws Exception
    {
        reflector.getSingleton( null, new Object[0] );
    }

    @Test( expected = ReflectorException.class )
    public void getSingletonClassEmptyArray()
        throws Exception
    {
        assertThat( reflector.getSingleton( Object.class, new Object[0] ), is( Object.class ) );
    }

    @Test( expected = ReflectorException.class )
    public void getSingletonClassInvalidSignature()
        throws Exception
    {
        reflector.getSingleton( Object.class, new Object[]{ this } );
    }

    @Test( expected = ReflectorException.class )
    public void getSingletonPrivateMethod()
        throws Exception
    {
        reflector.getSingleton( ReflectorTestHelper.class, new Object[0] );
    }

    @Test( expected = IllegalArgumentException.class )
    // @ReproducesPlexusBug( "Looking up methods by signature has an unlabeled continue, so finds the wrong method" )
    public void getSingletonPackageMethod()
        throws Exception
    {
        reflector.getSingleton( ReflectorTestHelper.class, new Object[]{ Boolean.FALSE } );
    }

    @Test( expected = IllegalArgumentException.class )
    // @ReproducesPlexusBug( "Looking up methods by signature has an unlabeled continue, so finds the wrong method" )
    public void getSingletonPackageMethodThrowsSomething()
        throws Exception
    {
        reflector.getSingleton( ReflectorTestHelper.class, new Object[]{ Boolean.TRUE } );
    }

    @Test( expected = IllegalArgumentException.class )
    // @ReproducesPlexusBug( "Looking up methods by signature has an unlabeled continue, so finds the wrong method" )
    public void getSingletonProtectedMethod()
        throws Exception
    {
        reflector.getSingleton( ReflectorTestHelper.class, new Object[]{ 0 } );
    }

    @Test( expected = IllegalArgumentException.class )
    // @ReproducesPlexusBug( "Looking up methods by signature has an unlabeled continue, so finds the wrong method" )
    public void getSingletonProtectedMethodThrowsSomething()
        throws Exception
    {
        reflector.getSingleton( ReflectorTestHelper.class, new Object[]{ 1 } );
    }

    @Test
    public void getSingletonPublicMethod()
        throws Exception
    {
        assertTrue( reflector.getSingleton( ReflectorTestHelper.class, new Object[]{ "" } )
                    instanceof ReflectorTestHelper );
    }

    @Test( expected = NullPointerException.class )
    public void getSingletonPublicMethodNullValue()
        throws Exception
    {
        reflector.getSingleton( ReflectorTestHelper.class, new Object[]{ null } );
    }

    @Test
    public void getSingletonPublicMethodThrowsSomething()
        throws Exception
    {
        try
        {
            reflector.getSingleton( ReflectorTestHelper.class, new Object[]{ "Message" } );
            fail();
        }
        catch ( ReflectorException e )
        {
            assertThat( e, hasCause( ReflectorTestHelper.HelperException.class ) );
        }
    }

    @Test( expected = NullPointerException.class )
    public void getSingletonNonStaticMethod()
        throws Exception
    {
        assertTrue( reflector.getSingleton( ReflectorTestHelper.class, new Object[]{ "", Boolean.FALSE } )
                    instanceof ReflectorTestHelper );
    }

    //// invoke( Object, String, Object[] )

    @Test( expected = NullPointerException.class )
    public void invokeNullNullNull()
        throws Exception
    {
        reflector.invoke( (Object)null, (String)null, (Object)null );
    }

    @Test( expected = NullPointerException.class )
    public void invokeNullNullEmpty()
        throws Exception
    {
        reflector.invoke( null, null, new Object[0] );
    }

    @Test( expected = NullPointerException.class )
    public void invokeNullEmptyNull()
        throws Exception
    {
        reflector.invoke( (Object)null, "", (Object)null );
    }

    @Test( expected = NullPointerException.class )
    public void invokeNullEmptyEmpty()
        throws Exception
    {
        reflector.invoke( null, "", new Object[0] );
    }

    @Test( expected = NullPointerException.class )
    public void invokeObjectNullNull()
        throws Exception
    {
        reflector.invoke( new Object(), (String)null, (Object)null );
    }

    @Test( expected = NullPointerException.class )
    public void invokeObjectNullEmpty()
        throws Exception
    {
        reflector.invoke( new Object(), null, new Object[0] );
    }

    @Test( expected = ReflectorException.class )
    public void invokeObjectEmptyNull()
        throws Exception
    {
        reflector.invoke( new Object(), "", null );
    }

    @Test( expected = ReflectorException.class )
    public void invokeObjectEmptyEmpty()
        throws Exception
    {
        reflector.invoke( new Object(), "", new Object[0] );
    }

    @Test
    public void invokeObjectValidNull()
        throws Exception
    {
        Object object = new Object();
        assertThat( reflector.invoke( object, "hashCode", null ), is( (Object) object.hashCode() ) );
    }

    @Test
    public void invokeObjectValidEmpty()
        throws Exception
    {
        Object object = new Object();
        assertThat( reflector.invoke( object, "hashCode", new Object[0] ),
                    is( (Object) object.hashCode() ) );
    }

    @Test( expected = ReflectorException.class )
    public void invokeObjectValidWrongSignature()
        throws Exception
    {
        reflector.invoke( new Object(), "hashCode", new Object[]{ this } );
    }

    @Test( expected = ReflectorException.class )
    public void invokePrivateMethod()
        throws Exception
    {
        class CoT
        {
            @SuppressWarnings( "unused" )
            private Object doSomething()
            {
                return "Done";
            }
        }
        assertThat( reflector.invoke( new CoT(), "doSomething", new Object[0] ), is( (Object) "Done" ) );
    }

    @Test( expected = ReflectorException.class )
    public void invokePackageMethod()
        throws Exception
    {
        class CoT
        {
            @SuppressWarnings( "unused" )
            Object doSomething()
            {
                return "Done";
            }
        }
        assertThat( reflector.invoke( new CoT(), "doSomething", new Object[0] ), is( (Object) "Done" ) );
    }

    @Test( expected = ReflectorException.class )
    public void invokeProtectedMethod()
        throws Exception
    {
        class CoT
        {
            @SuppressWarnings( "unused" )
            protected Object doSomething()
            {
                return "Done";
            }
        }
        assertThat( reflector.invoke( new CoT(), "doSomething", new Object[0] ), is( (Object) "Done" ) );
    }

    @Test
    public void invokePublicMethod()
        throws Exception
    {
        class CoT
        {
            @SuppressWarnings( "unused" )
            public Object doSomething()
            {
                return "Done";
            }
        }
        assertThat( reflector.invoke( new CoT(), "doSomething", new Object[0] ), is( (Object) "Done" ) );
    }

    //// getStaticField( Class, String )

    @Test( expected = NullPointerException.class )
    public void getStaticFieldNullNull()
        throws Exception
    {
        reflector.getStaticField( null, null );
    }

    @Test( expected = NullPointerException.class )
    public void getStaticFieldNullEmpty()
        throws Exception
    {
        reflector.getStaticField( null, "" );
    }

    @Test( expected = NullPointerException.class )
    public void getStaticFieldObjectNull()
        throws Exception
    {
        reflector.getStaticField( Object.class, null );
    }

    @Test
    public void getStaticFieldObjectEmpty()
        throws Exception
    {
        try
        {
            reflector.getStaticField( Object.class, "" );
            fail();
        }
        catch ( ReflectorException e )
        {
            assertThat( e, hasCause( NoSuchFieldException.class ) );
        }
    }

    @Test
    public void getStaticFieldPrivateField()
        throws Exception
    {
        try
        {
            reflector.getStaticField( ReflectorTestHelper.class, "PRIVATE_STATIC_STRING" );
            fail();
        }
        catch ( ReflectorException e )
        {
            assertThat( e, hasCause( NoSuchFieldException.class ) );
        }
    }

    @Test
    public void getStaticFieldPackageField()
        throws Exception
    {
        try
        {
            reflector.getStaticField( ReflectorTestHelper.class, "PACKAGE_STATIC_STRING" );
            fail();
        }
        catch ( ReflectorException e )
        {
            assertThat( e, hasCause( NoSuchFieldException.class ) );
        }
    }

    @Test
    public void getStaticFieldProtectedField()
        throws Exception
    {
        try
        {
            reflector.getStaticField( ReflectorTestHelper.class, "PROTECTED_STATIC_STRING" );
            fail();
        }
        catch ( ReflectorException e )
        {
            assertThat( e, hasCause( NoSuchFieldException.class ) );
        }
    }

    @Test
    public void getStaticFieldPublicField()
        throws Exception
    {
        assertThat( reflector.getStaticField( ReflectorTestHelper.class, "PUBLIC_STATIC_STRING" ),
                    is( (Object) "public static string" ) );
    }

    //// getField( Object, String )

    @Test( expected = NullPointerException.class )
    public void getFieldNullNull()
        throws Exception
    {
        reflector.getField( null, null );
    }

    @Test( expected = NullPointerException.class )
    public void getFieldNullEmpty()
        throws Exception
    {
        reflector.getField( null, "" );
    }

    @Test( expected = NullPointerException.class )
    public void getFieldObjectNull()
        throws Exception
    {
        reflector.getField( new Object(), null );
    }

    @Test
    public void getFieldObjectEmpty()
        throws Exception
    {
        try
        {
            reflector.getField( new Object(), "" );
            fail();
        }
        catch ( ReflectorException e )
        {
            assertThat( e, hasCause( NoSuchFieldException.class ) );
        }
    }

    @Test
    public void getFieldCoTValuePrivateField()
        throws Exception
    {
        final String expected = "gotIt";
        class CoT
        {
            @SuppressWarnings( "unused" )
            private String value = expected;
        }
        try
        {
            assertThat( reflector.getField( new CoT(), "value" ), is( (Object) expected ) );
            fail();
        }
        catch ( ReflectorException e )
        {
            assertThat( e, hasCause( IllegalAccessException.class ) );
        }
    }

    @Test
    public void getFieldCoTValuePackageField()
        throws Exception
    {
        final String expected = "gotIt";
        class CoT
        {
            @SuppressWarnings( "unused" )
            String value = expected;
        }
        assertThat( reflector.getField( new CoT(), "value" ), is( (Object) expected ) );
    }

    @Test
    public void getFieldCoTValueProtectedField()
        throws Exception
    {
        final String expected = "gotIt";
        class CoT
        {
            @SuppressWarnings( "unused" )
            protected String value = expected;
        }
        assertThat( reflector.getField( new CoT(), "value" ), is( (Object) expected ) );
    }

    @Test
    public void getFieldCoTValuePublicField()
        throws Exception
    {
        final String expected = "gotIt";
        class CoT
        {
            @SuppressWarnings( "unused" )
            public String value = expected;
        }
        assertThat( reflector.getField( new CoT(), "value" ), is( (Object) expected ) );
    }

    //// getField( Object, String, boolean )

    @Test( expected = NullPointerException.class )
    public void getFieldNullNullFalse()
        throws Exception
    {
        reflector.getField( null, null, false );
    }

    @Test( expected = NullPointerException.class )
    public void getFieldNullEmptyFalse()
        throws Exception
    {
        reflector.getField( null, "", false );
    }

    @Test( expected = NullPointerException.class )
    public void getFieldObjectNullFalse()
        throws Exception
    {
        reflector.getField( new Object(), null, false );
    }

    @Test
    public void getFieldObjectEmptyFalse()
        throws Exception
    {
        try
        {
            reflector.getField( new Object(), "", false );
            fail();
        }
        catch ( ReflectorException e )
        {
            assertThat( e, hasCause( NoSuchFieldException.class ) );
        }
    }

    @Test
    public void getFieldCoTValueFalsePrivateField()
        throws Exception
    {
        final String expected = "gotIt";
        class CoT
        {
            @SuppressWarnings( "unused" )
            private String value = expected;
        }
        try
        {
            assertThat( reflector.getField( new CoT(), "value", false ), is( (Object) expected ) );
            fail();
        }
        catch ( ReflectorException e )
        {
            assertThat( e, hasCause( IllegalAccessException.class ) );
        }
    }

    @Test
    public void getFieldCoTValueFalsePackageField()
        throws Exception
    {
        final String expected = "gotIt";
        class CoT
        {
            @SuppressWarnings( "unused" )
            String value = expected;
        }
        assertThat( reflector.getField( new CoT(), "value", false ), is( (Object) expected ) );
    }

    @Test
    public void getFieldCoTValueFalseProtectedField()
        throws Exception
    {
        final String expected = "gotIt";
        class CoT
        {
            @SuppressWarnings( "unused" )
            protected String value = expected;
        }
        assertThat( reflector.getField( new CoT(), "value", false ), is( (Object) expected ) );
    }

    @Test
    public void getFieldCoTValueFalsePublicField()
        throws Exception
    {
        final String expected = "gotIt";
        class CoT
        {
            @SuppressWarnings( "unused" )
            public String value = expected;
        }
        assertThat( reflector.getField( new CoT(), "value", false ), is( (Object) expected ) );
    }

    @Test( expected = NullPointerException.class )
    public void getFieldNullNullTrue()
        throws Exception
    {
        reflector.getField( null, null, true );
    }

    @Test( expected = NullPointerException.class )
    public void getFieldNullEmptyTrue()
        throws Exception
    {
        reflector.getField( null, "", true );
    }

    @Test( expected = NullPointerException.class )
    public void getFieldObjectNullTrue()
        throws Exception
    {
        reflector.getField( new Object(), null, true );
    }

    @Test
    public void getFieldObjectEmptyTrue()
        throws Exception
    {
        try
        {
            reflector.getField( new Object(), "", true );
            fail();
        }
        catch ( ReflectorException e )
        {
            assertThat( e, hasCause( NoSuchFieldException.class ) );
        }
    }

    @Test
    public void getFieldCoTValueTruePrivateField()
        throws Exception
    {
        final String expected = "gotIt";
        class CoT
        {
            @SuppressWarnings( "unused" )
            private String value = expected;
        }
        assertThat( reflector.getField( new CoT(), "value", true ), is( (Object) expected ) );
    }

    @Test
    public void getFieldCoTValueTruePackageField()
        throws Exception
    {
        final String expected = "gotIt";
        class CoT
        {
            @SuppressWarnings( "unused" )
            String value = expected;
        }
        assertThat( reflector.getField( new CoT(), "value", true ), is( (Object) expected ) );
    }

    @Test
    public void getFieldCoTValueTrueProtectedField()
        throws Exception
    {
        final String expected = "gotIt";
        class CoT
        {
            @SuppressWarnings( "unused" )
            protected String value = expected;
        }
        assertThat( reflector.getField( new CoT(), "value", true ), is( (Object) expected ) );
    }

    @Test
    public void getFieldCoTValueTruePublicField()
        throws Exception
    {
        final String expected = "gotIt";
        class CoT
        {
            @SuppressWarnings( "unused" )
            public String value = expected;
        }
        assertThat( reflector.getField( new CoT(), "value", true ), is( (Object) expected ) );
    }

    //// invokeStatic( Class, String, Object[] )

    @Test( expected = NullPointerException.class )
    public void invokeStaticNullNullNull()
        throws Exception
    {
        reflector.invokeStatic( (Class<?>)null, (String)null, (Object)null );
    }

    @Test( expected = NullPointerException.class )
    public void invokeStaticClassNullNull()
        throws Exception
    {
        assertThat( reflector.invokeStatic( Object.class, (String)null, (Object)null ), is( Object.class ) );
    }

    @Test( expected = NullPointerException.class )
    public void invokeStaticNullNullEmptyArray()
        throws Exception
    {
        reflector.invokeStatic( null, null, new Object[0] );
    }

    @Test( expected = NullPointerException.class )
    public void invokeStaticClassNullEmptyArray()
        throws Exception
    {
        assertThat( reflector.invokeStatic( Object.class, null, new Object[0] ), is( Object.class ) );
    }

    @Test( expected = NullPointerException.class )
    public void invokeStaticNullEmptyNull()
        throws Exception
    {
        reflector.invokeStatic( (Class<?>)null, "", (Object)null );
    }

    @Test( expected = ReflectorException.class )
    public void invokeStaticClassEmptyNull()
        throws Exception
    {
        reflector.invokeStatic( Object.class, "", null );
    }

    @Test( expected = NullPointerException.class )
    public void invokeStaticNullEmptyEmptyArray()
        throws Exception
    {
        reflector.invokeStatic( null, "", new Object[0] );
    }

    @Test( expected = ReflectorException.class )
    public void invokeStaticClassEmptyEmptyArray()
        throws Exception
    {
        assertThat( reflector.invokeStatic( Object.class, "", new Object[0] ), is( Object.class ) );
    }

    @Test( expected = IllegalArgumentException.class )
    // @ReproducesPlexusBug( "Looking up methods by signature has an unlabelled continue, so finds the wrong method" )
    public void invokeStaticClassInvalidSignature()
        throws Exception
    {
        reflector.invokeStatic( ReflectorTestHelper.class, "getInstance", new Object[]{ this } );
    }

    @Test( expected = ReflectorException.class )
    public void invokeStaticPrivateMethod()
        throws Exception
    {
        reflector.invokeStatic( ReflectorTestHelper.class, "getInstance", new Object[0] );
    }

    @Test( expected = IllegalArgumentException.class )
    // @ReproducesPlexusBug( "Looking up methods by signature has an unlabelled continue, so finds the wrong method" )
    public void invokeStaticPackageMethod()
        throws Exception
    {
        reflector.invokeStatic( ReflectorTestHelper.class, "getInstance", new Object[]{ Boolean.FALSE } );
    }

    @Test( expected = IllegalArgumentException.class )
    // @ReproducesPlexusBug( "Looking up methods by signature has an unlabelled continue, so finds the wrong method" )
    public void invokeStaticPackageMethodThrowsSomething()
        throws Exception
    {
        reflector.invokeStatic( ReflectorTestHelper.class, "getInstance", new Object[]{ Boolean.TRUE } );
    }

    @Test( expected = IllegalArgumentException.class )
    // @ReproducesPlexusBug( "Looking up methods by signature has an unlabelled continue, so finds the wrong method" )
    public void invokeStaticProtectedMethod()
        throws Exception
    {
        reflector.invokeStatic( ReflectorTestHelper.class, "getInstance", new Object[]{ 0 } );
    }

    @Test( expected = IllegalArgumentException.class )
    // @ReproducesPlexusBug( "Looking up methods by signature has an unlabelled continue, so finds the wrong method" )
    public void invokeStaticProtectedMethodThrowsSomething()
        throws Exception
    {
        reflector.invokeStatic( ReflectorTestHelper.class, "getInstance", new Object[]{ 1 } );
    }

    @Test
    public void invokeStaticPublicMethod()
        throws Exception
    {
        assertTrue( reflector.invokeStatic( ReflectorTestHelper.class, "getInstance", new Object[]{ "" } )
                    instanceof ReflectorTestHelper );
    }

    @Test( expected = NullPointerException.class )
    public void invokeStaticPublicMethodNullValue()
        throws Exception
    {
        reflector.invokeStatic( ReflectorTestHelper.class, "getInstance", new Object[]{ null } );
    }

    @Test
    public void invokeStaticPublicMethodThrowsSomething()
        throws Exception
    {
        try
        {
            reflector.invokeStatic( ReflectorTestHelper.class, "getInstance", new Object[]{ "Message" } );
            fail();
        }
        catch ( ReflectorException e )
        {
            assertThat( e, hasCause( ReflectorTestHelper.HelperException.class ) );
        }
    }

    @Test( expected = NullPointerException.class )
    public void invokeStaticNonStaticMethod()
        throws Exception
    {
        assertTrue(
            reflector.invokeStatic( ReflectorTestHelper.class, "getInstance", new Object[]{ "", Boolean.FALSE } )
            instanceof ReflectorTestHelper );
    }

    //// getConstructor( Class, Class[] )

    @Test( expected = NullPointerException.class )
    public void getConstructorNullNull()
        throws Exception
    {
        reflector.getConstructor( (Class<?>)null, (Class<?>)null );
    }

    @Test( expected = NullPointerException.class )
    public void getConstructorNullEmpty()
        throws Exception
    {
        reflector.getConstructor( null, new Class[0] );
    }

    @SuppressWarnings( "rawtypes" )
    @Test( expected = NullPointerException.class )
    public void getConstructorObjectNull()
        throws Exception
    {
        assertThat( reflector.getConstructor( Object.class, (Class<?>)null ),
                    is( (Constructor) Object.class.getDeclaredConstructor() ) );
    }

    @SuppressWarnings( "rawtypes" )
    @Test
    public void getConstructorObjectEmpty()
        throws Exception
    {
        assertThat( reflector.getConstructor( Object.class),
                    is( (Constructor) Object.class.getDeclaredConstructor() ) );
    }

    @SuppressWarnings( "rawtypes" )
    @Test( expected = ReflectorException.class )
    // @ReproducesPlexusBug( "Looking up methods by signature has an unlabelled continue, so finds the wrong method" )
    public void getConstructorPrivate()
        throws Exception
    {
        assertThat( reflector.getConstructor( ReflectorTestHelper.class),
                    is( (Constructor) ReflectorTestHelper.class.getDeclaredConstructor() ) );
    }

    @SuppressWarnings( "rawtypes" )
    @Test
    public void getConstructorPackage()
        throws Exception
    {
        assertThat( reflector.getConstructor( ReflectorTestHelper.class, Boolean.class),
                    not( is( (Constructor) ReflectorTestHelper.class.getDeclaredConstructor( Boolean.class ) ) ) );
    }

    @SuppressWarnings( "rawtypes" )
    @Test
    public void getConstructorProtected()
        throws Exception
    {
        assertThat( reflector.getConstructor( ReflectorTestHelper.class, Integer.class),
                    not( is( (Constructor) ReflectorTestHelper.class.getDeclaredConstructor( Integer.class ) ) ) );
    }

    @SuppressWarnings( "rawtypes" )
    @Test
    public void getConstructorPublic()
        throws Exception
    {
        assertThat( reflector.getConstructor( ReflectorTestHelper.class, String.class),
                    is( (Constructor) ReflectorTestHelper.class.getDeclaredConstructor( String.class ) ) );
    }

    //// getObjectProperty( Object, String )

    @Test( expected = ReflectorException.class )
    public void getObjectPropertyNullNull()
        throws Exception
    {
        reflector.getObjectProperty( null, null );
    }

    @Test( expected = ReflectorException.class )
    public void getObjectPropertyNullEmpty()
        throws Exception
    {
        reflector.getObjectProperty( null, "" );
    }

    @Test( expected = ReflectorException.class )
    public void getObjectPropertyObjectNull()
        throws Exception
    {
        reflector.getObjectProperty( new Object(), null );
    }

    @Test( expected = ReflectorException.class )
    public void getObjectPropertyObjectEmpty()
        throws Exception
    {
        reflector.getObjectProperty( new Object(), "" );
    }

    @Test
    // @ReproducesPlexusBug( "Should only access public properties" )
    public void getObjectPropertyViaPrivateField()
        throws Exception
    {
        class CoT
        {
            @SuppressWarnings( "unused" )
            private int value = 42;
        }
        assertThat( reflector.getObjectProperty( new CoT(), "value" ), is( (Object) 42 ) );
    }

    @Test
    // @ReproducesPlexusBug( "Should only access public properties" )
    public void getObjectPropertyViaPackageField()
        throws Exception
    {
        class CoT
        {
            @SuppressWarnings( "unused" )
            int value = 42;
        }
        assertThat( reflector.getObjectProperty( new CoT(), "value" ), is( (Object) 42 ) );
    }

    @Test
    // @ReproducesPlexusBug( "Should only access public properties" )
    public void getObjectPropertyViaProtectedField()
        throws Exception
    {
        class CoT
        {
            @SuppressWarnings( "unused" )
            protected int value = 42;
        }
        assertThat( reflector.getObjectProperty( new CoT(), "value" ), is( (Object) 42 ) );
    }

    @Test
    public void getObjectPropertyViaPublicField()
        throws Exception
    {
        class CoT
        {
            @SuppressWarnings( "unused" )
            public int value = 42;
        }
        assertThat( reflector.getObjectProperty( new CoT(), "value" ), is( (Object) 42 ) );
    }

    @Test( expected = ReflectorException.class )
    public void getObjectPropertyViaPrivateGetter()
        throws Exception
    {
        class CoT
        {
            private final int _value = 42;

            @SuppressWarnings( "unused" )
            private int getValue()
            {
                return _value;
            }
        }
        reflector.getObjectProperty( new CoT(), "value" );
    }

    @Test( expected = ReflectorException.class )
    public void getObjectPropertyViaPackageGetter()
        throws Exception
    {
        class CoT
        {
            private final int _value = 42;

            @SuppressWarnings( "unused" )
            int getValue()
            {
                return _value;
            }
        }
        reflector.getObjectProperty( new CoT(), "value" );
    }

    @Test( expected = ReflectorException.class )
    public void getObjectPropertyViaProtectedGetter()
        throws Exception
    {
        class CoT
        {
            private final int _value = 42;

            @SuppressWarnings( "unused" )
            protected int getValue()
            {
                return _value;
            }
        }
        reflector.getObjectProperty( new CoT(), "value" );
    }

    @Test
    public void getObjectPropertyViaPublicGetter()
        throws Exception
    {
        class CoT
        {
            private final int _value = 42;

            @SuppressWarnings( "unused" )
            public int getValue()
            {
                return _value;
            }
        }
        assertThat( reflector.getObjectProperty( new CoT(), "value" ), is( (Object) 42 ) );
    }

    //// getMethod( Class, String, Class[] )

    @Test( expected = NullPointerException.class )
    public void getMethodNullNullNull()
        throws Exception
    {
        reflector.getMethod( (Class<?>)null, (String)null, (Class<?>)null );
    }

    @Test( expected = NullPointerException.class )
    public void getMethodNullNullEmpty()
        throws Exception
    {
        reflector.getMethod( null, null, new Class[0] );
    }

    @Test( expected = NullPointerException.class )
    public void getMethodObjectNullNull()
        throws Exception
    {
        reflector.getMethod( Object.class, (String)null, (Class<?>)null );
    }

    @Test( expected = NullPointerException.class )
    public void getMethodObjectNullEmpty()
        throws Exception
    {
        reflector.getMethod( Object.class, null, new Class[0] );
    }

    @Test( expected = NullPointerException.class )
    public void getMethodNullEmptyNull()
        throws Exception
    {
        reflector.getMethod( (Class<?>)null, "", (Class<?>)null );
    }

    @Test( expected = NullPointerException.class )
    public void getMethodNullEmptyEmpty()
        throws Exception
    {
        reflector.getMethod( null, "", new Class[0] );
    }

    @Test( expected = NullPointerException.class )
    public void getMethodObjectEmptyNull()
        throws Exception
    {
        reflector.getMethod( Object.class, "", (Class<?>)null );
    }

    @Test( expected = ReflectorException.class )
    public void getMethodObjectEmptyEmpty()
        throws Exception
    {
        reflector.getMethod( Object.class, "", new Class[0] );
    }

    @Test( expected = ReflectorException.class )
    // @ReproducesPlexusBug( "Looking up methods by signature has an unlabelled continue, so finds the wrong method" )
    public void getMethodPrivate()
        throws Exception
    {
        assertThat( reflector.getMethod( ReflectorTestHelper.class, "getInstance", new Class[0] ),
                    is( ReflectorTestHelper.class.getDeclaredMethod( "getInstance" ) ) );
    }

    @Test
    public void getMethodPackage()
        throws Exception
    {
        assertThat( reflector.getMethod( ReflectorTestHelper.class, "getInstance", new Class[]{ Boolean.class } ),
                    not( is( ReflectorTestHelper.class.getDeclaredMethod( "getInstance", Boolean.class ) ) ) );
    }

    @Test
    public void getMethodProtected()
        throws Exception
    {
        assertThat( reflector.getMethod( ReflectorTestHelper.class, "getInstance", new Class[]{ Integer.class } ),
                    not( is( ReflectorTestHelper.class.getDeclaredMethod( "getInstance", Integer.class ) ) ) );
    }

    @Test
    public void getMethodPublic()
        throws Exception
    {
        assertThat( reflector.getMethod( ReflectorTestHelper.class, "getInstance", new Class[]{ String.class } ),
                    is( ReflectorTestHelper.class.getDeclaredMethod( "getInstance", String.class ) ) );
    }

}
