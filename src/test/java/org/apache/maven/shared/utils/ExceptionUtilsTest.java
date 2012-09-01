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

import org.apache.maven.shared.utils.exceptionutils.TestException;
import org.apache.maven.shared.utils.exceptionutils.TestExceptionWithDetail;
import org.junit.Test;
import org.junit.Assert;
import org.junit.matchers.JUnitMatchers;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.*;


/**
 * Test all public methods of {@link ExceptionUtils}.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class ExceptionUtilsTest extends Assert
{
    private static Logger logger = Logger.getLogger(ExceptionUtilsTest.class.getName());

    protected static StackTraceElement[] STACKTRACE_WO_SPECIAL_METHODS =
        {
            new StackTraceElement("org.apache.maven.test.Class1", "method1", null, 101),
            new StackTraceElement("org.apache.maven.test.Class2", "method2", null, 101),
            new StackTraceElement("org.apache.maven.test.Class3", "method3", null, 101),
            new StackTraceElement("org.apache.maven.test.Class4", "method4", null, 101),
        };


    /**
     * Hack, who invokes a state-changing static method?
     * That's SICK!
     * We should deprecate this very method.
     */
    @Test
    public void testAddCauseMethodName()
    {
        ExceptionUtils.addCauseMethodName( "getNestedException" );

        ExceptionUtils.addCauseMethodName( null );
    }

    @Test
    public void testGetCause()
    {
        NullPointerException npe = new NullPointerException( "dooh just a random, nullpointer" );

        {
            Exception exception = new Exception( npe );
            exception.setStackTrace(STACKTRACE_WO_SPECIAL_METHODS);
            assertThat( "getCause for custom Exception is the same as before"
                      , ExceptionUtils.getCause( exception )
                      , equalTo( (Throwable) npe ) );
        }

        {
            RuntimeException sqlException2 = new RuntimeException();
            RuntimeException sqlException1 = new RuntimeException(sqlException2);

            assertThat( "getCause for RuntimeException"
                      , ExceptionUtils.getCause( sqlException1 )
                      , equalTo( (Throwable) sqlException2 ) );
        }

        {
            InvocationTargetException ivte = new InvocationTargetException( npe );

            assertThat( "getCause for InvocationTargetException"
                      , ExceptionUtils.getCause( ivte )
                      , equalTo( (Throwable) npe ) );
        }

        {
            TestException testException = new TestException();
            testException.setSourceException( npe );

            assertThat( "getCause for InvocationTargetException"
                      , ExceptionUtils.getCause( testException )
                      , equalTo( (Throwable) npe ) );
        }

    }

    @Test( expected = NullPointerException.class )
    public void testGetCause_Null()
    {
        ExceptionUtils.getCause( null );
    }

    @Test
    public void testGetCause_MethodNames()
    {
        NullPointerException npe = new NullPointerException( "dooh just a random, nullpointer" );

        {
            TestException testException = new TestException();
            testException.setSpecialCause( npe );
            String[] methodNames = new String[]{ "getSpecialCause" };

            assertThat( "getCause for InvocationTargetException"
                      , ExceptionUtils.getCause( testException, methodNames )
                      , equalTo( (Throwable) npe ) );
        }

        {
            TestException testException = new TestException();
            testException.setSpecialCause( npe );
            String[] methodNames = new String[]{ "getNonExistingMethod" };

            assertThat("getCause for InvocationTargetException"
                    , ExceptionUtils.getCause( testException, methodNames )
                    , nullValue());
        }
    }

    @Test
    public void testGetFullStackTrace()
    {
        NullPointerException npe = new NullPointerException( "dooh just a random, nullpointer" );

        String fullStackTraceStart = "java.lang.NullPointerException: dooh just a random, nullpointer\n"
                     + "\tat org.apache.maven.shared.utils.ExceptionUtilsTest.testGetFullStackTrace(ExceptionUtilsTest.java";

        String fullStackTrace = ExceptionUtils.getFullStackTrace( npe );
        assertThat( "getFullStackTrace start with"
                  , fullStackTrace
                  , JUnitMatchers.containsString( fullStackTraceStart ) );

        RuntimeException rtException = new RuntimeException( npe );
        fullStackTrace = ExceptionUtils.getFullStackTrace( rtException );
                assertThat( "getFullStackTrace start with"
                          , fullStackTrace
                          , JUnitMatchers.containsString( fullStackTraceStart ) );

    }

    @Test
    public void testGetThrowables()
    {
        NullPointerException npe = new NullPointerException( "dooh just a random, nullpointer" );
        RuntimeException rtException = new RuntimeException( npe );
        TestException testException =  new TestException();
        testException.setSourceException( rtException );

        Throwable[] expectedExceptions = new Throwable[] { testException, rtException, npe };

        assertThat( "getThrowables"
                  , ExceptionUtils.getThrowables( testException )
                  , equalTo( expectedExceptions ) );

        // NPE safe test
        // this method should NOT throw a NPE on a null argument!
        ExceptionUtils.getThrowables( null );
    }


    /**
     * @see ExceptionUtils#getRootCause(Throwable)
     */
    @Test
    public void testGetRootCause()
    {
        NullPointerException npe = new NullPointerException( "dooh just a random, nullpointer" );
        RuntimeException rtException = new RuntimeException( npe );
        TestException testException =  new TestException();
        testException.setSourceException( rtException );

        assertThat( "getRootCause"
                  , ExceptionUtils.getRootCause(testException)
                  , equalTo( (Throwable) npe ) );

        assertThat( "getRootCause"
                  , ExceptionUtils.getRootCause( rtException )
                  , equalTo( (Throwable) npe ) );

        assertThat("getRootCause"
                , ExceptionUtils.getRootCause(npe)
                , nullValue() );

        try
        {
            ExceptionUtils.getRootCause( null );
            fail( "getRootCause(null) NPE expected" );
        }
        catch ( NullPointerException e )
        {
            //nothing to do, Exception was expected
        }
    }

    /**
     * @see ExceptionUtils#getStackTrace(Throwable)
     */
    @Test
    public void testGetStackTrace()
    {
        NullPointerException npe = new NullPointerException( "dooh just a random, nullpointer" );

        String stackTrace = ExceptionUtils.getStackTrace( npe );
        assertNotNull( stackTrace );
        assertTrue( "wrong stacktrace: " + stackTrace,
                    stackTrace.startsWith( "java.lang.NullPointerException: dooh just a random, nullpointer\n" +
                        "\tat org.apache.maven.shared.utils.ExceptionUtilsTest.testGetStackTrace(ExceptionUtilsTest.java" ));

        RuntimeException rtException = new RuntimeException( npe );
        stackTrace = ExceptionUtils.getStackTrace( rtException );
        assertNotNull( stackTrace );
        assertTrue( "wrong stacktrace: " + stackTrace,
                    stackTrace.startsWith( "java.lang.RuntimeException: java.lang.NullPointerException: "
                      + "dooh just a random, nullpointer\n"
                      + "\tat org.apache.maven.shared.utils.ExceptionUtilsTest.testGetStackTrace(ExceptionUtilsTest.java" ));

        // NPE safe test
        try
        {
            ExceptionUtils.getStackTrace((Throwable) null);
            fail( "getStackTrace(null) NPE expected" );
        }
        catch ( NullPointerException e )
        {
            //nothing to do, Exception was expected
        }
    }

    /**
     * @see ExceptionUtils#getStackFrames(Throwable)
     */
    @Test
    public void testGetStackFrames()
    {
        NullPointerException npe = new NullPointerException( "dooh just a random, nullpointer" );

        String[] stackFrames = ExceptionUtils.getStackFrames( npe );
        assertNotNull( stackFrames );
        assertTrue( stackFrames.length > 3 );

        assertEquals( "java.lang.NullPointerException: " + npe.getMessage(), stackFrames[0] );
        assertThat( "stackFrames", stackFrames[1]
                  , JUnitMatchers.containsString( "at org.apache.maven.shared.utils.ExceptionUtilsTest."
                                                  + "testGetStackFrames(ExceptionUtilsTest.java" ) );

        // NPE safe test
        try
        {
            ExceptionUtils.getStackFrames((Throwable) null);
            fail( "getStackFrames(null) NPE expected" );
        }
        catch ( NullPointerException e )
        {
            //nothing to do, Exception was expected
        }
    }

    /**
     * @see ExceptionUtils#getThrowableCount(Throwable)
     */
    @Test
    public void testGetThrowableCount()
    {
        NullPointerException npe = new NullPointerException( "dooh just a random, nullpointer" );
        RuntimeException rtException = new RuntimeException( npe );
        TestException testException =  new TestException();
        testException.setSourceException( rtException );

        assertThat( "getThrowableCount"
                  , ExceptionUtils.getThrowableCount( npe )
                  , is( 1 ));

        assertThat( "getThrowableCount"
                  , ExceptionUtils.getThrowableCount( rtException )
                  , is( 2 ));

        assertThat( "getThrowableCount"
                  , ExceptionUtils.getThrowableCount( testException )
                  , is( 3 ));

        // NPE safe test
        // this method should NOT throw a NPE on a null argument!
        ExceptionUtils.getThrowableCount( null );
    }

    /**
     * @see ExceptionUtils#indexOfThrowable(Throwable, Class)
     * @see ExceptionUtils#indexOfThrowable(Throwable, Class, int)
     */
    @Test
    public void testIndexOfThrowable()
    {
        NullPointerException npe = new NullPointerException( "dooh just a random, nullpointer" );
        RuntimeException rtException = new RuntimeException( npe );
        TestException testException =  new TestException();
        testException.setSourceException( rtException );

        assertThat("indexOfThrowable"
                , ExceptionUtils.indexOfThrowable(npe, NullPointerException.class)
                , is(0));

        assertThat( "indexOfThrowable for non contained Exception type"
                  , ExceptionUtils.indexOfThrowable( npe, RuntimeException.class )
                  , is( -1 ));


        assertThat( "indexOfThrowable"
                  , ExceptionUtils.indexOfThrowable( testException, NullPointerException.class )
                  , is( 2 ));

        assertThat( "indexOfThrowable for non contained Exception type"
                  , ExceptionUtils.indexOfThrowable( testException, RuntimeException.class )
                  , is( 1 ));

        assertThat( "indexOfThrowable"
                  , ExceptionUtils.indexOfThrowable( testException, TestException.class )
                  , is( 0 ));


        // tests for indexOfThrowable with start index param
        assertThat( "indexOfThrowable"
                  , ExceptionUtils.indexOfThrowable( testException, NullPointerException.class, 2 )
                  , is( 2 ));

        assertThat( "indexOfThrowable"
                  , ExceptionUtils.indexOfThrowable( testException, RuntimeException.class, 2 )
                  , is( -1 ));

        try
        {
            ExceptionUtils.indexOfThrowable( testException, TestException.class, 3 );
            fail( "indexOfThrowable with too large fromIndex" );
        }
        catch ( IndexOutOfBoundsException e )
        {
            //nothing to do, Exception was expected
        }

        try
        {
            ExceptionUtils.indexOfThrowable( testException, TestException.class, -1 );
            fail( "indexOfThrowable with too large fromIndex" );
        }
        catch ( IndexOutOfBoundsException e )
        {
            //nothing to do, Exception was expected
        }

        // NPE safe tests
        try
        {
            ExceptionUtils.indexOfThrowable( null, TestException.class );
            fail( "indexOfThrowable(null, Exception.class) NPE expected" );
        }
        catch ( IndexOutOfBoundsException e )
        {
            //nothing to do, Exception was expected
        }
        assertThat( "indexOfThrowable for null Exception type"
                  , ExceptionUtils.indexOfThrowable(npe, null)
                  , is(-1));
    }

    /**
     * Most probably this only ever returns false on null in JDK > 1.4
     * Because Throwable itself nowadays has a getCause() method which
     * is in the method list...
     *
     * @see ExceptionUtils#isNestedThrowable(Throwable)
     */
    @Test
    public void testIsNestedThrowable()
    {
        NullPointerException npe = new NullPointerException( "dooh just a random, nullpointer" );
        RuntimeException rtException = new RuntimeException( npe );
        TestException testException =  new TestException();
        testException.setSourceException( rtException );

        assertThat( "isNestedThrowable"
                  , ExceptionUtils.isNestedThrowable( null )
                  , is( false ) );

        assertThat("isNestedThrowable"
                , ExceptionUtils.isNestedThrowable(npe)
                , is(true));

        assertThat( "isNestedThrowable"
                  , ExceptionUtils.isNestedThrowable( rtException )
                  , is( true ) );

        assertThat( "isNestedThrowable"
                  , ExceptionUtils.isNestedThrowable( new InvocationTargetException( npe ) )
                  , is( true ) );

        assertThat( "isNestedThrowable"
                  , ExceptionUtils.isNestedThrowable( new TestExceptionWithDetail() )
                  , is( true ) );

        assertThat( "isNestedThrowable"
                  , ExceptionUtils.isNestedThrowable( new Exception() )
                  , is( true ) );

        assertThat( "isNestedThrowable"
                  , ExceptionUtils.isNestedThrowable( new Throwable() )
                  , is( true ) );
    }

    /**
     * @see ExceptionUtils#printRootCauseStackTrace(Throwable)
     * @see ExceptionUtils#printRootCauseStackTrace(Throwable, java.io.PrintWriter)
     * @see ExceptionUtils#printRootCauseStackTrace(Throwable, java.io.PrintStream)
     */
    @Test
    public void testPrintRootCauseStackTrace()
    {
        NullPointerException npe = new NullPointerException( "dooh just a random, nullpointer" );
        RuntimeException rtException = new RuntimeException( npe );
        TestException testException =  new TestException();
        testException.setSourceException( rtException );

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        PrintStream outStream = new PrintStream( bao );
        PrintStream originalErr = System.err;

        try
        {
            System.setErr(outStream);

            ExceptionUtils.printRootCauseStackTrace( npe );
            assertThat( "stackFrames"
                      , bao.toString()
                      , JUnitMatchers.containsString( "java.lang.NullPointerException: dooh just a random, nullpointer"
                                                      + "\n\tat org.apache.maven.shared.utils.ExceptionUtilsTest."
                                                      + "testPrintRootCauseStackTrace(ExceptionUtilsTest.java:" ) );
            bao.reset();
            ExceptionUtils.printRootCauseStackTrace( rtException );
            assertThat( "stackFrames"
                      , bao.toString()
                      , JUnitMatchers.containsString( "java.lang.NullPointerException: dooh just a random, nullpointer"
                                                      + "\n\tat org.apache.maven.shared.utils.ExceptionUtilsTest."
                                                      + "testPrintRootCauseStackTrace(ExceptionUtilsTest.java:" ) );

            // moving back to the original stdout and using the PrintStream directly
            System.setErr( originalErr );

            bao.reset();
            ExceptionUtils.printRootCauseStackTrace( rtException, outStream );
            assertThat("stackFrames"
                    , bao.toString()
                    , JUnitMatchers.containsString("java.lang.NullPointerException: dooh just a random, nullpointer"
                    + "\n\tat org.apache.maven.shared.utils.ExceptionUtilsTest."
                    + "testPrintRootCauseStackTrace(ExceptionUtilsTest.java:"));
            outStream.close();

            bao.reset();
            outStream = new PrintStream( bao );
            PrintWriter printWriter = new PrintWriter( outStream );
            ExceptionUtils.printRootCauseStackTrace( rtException, printWriter );
            assertThat( "stackFrames"
                      , bao.toString()
                      , JUnitMatchers.containsString( "java.lang.NullPointerException: dooh just a random, nullpointer"
                                                      + "\n\tat org.apache.maven.shared.utils.ExceptionUtilsTest."
                                                      + "testPrintRootCauseStackTrace(ExceptionUtilsTest.java:" ) );
        }
        finally
        {
            System.setErr(originalErr);
        }

        try
        {
            ExceptionUtils.printRootCauseStackTrace( null );
            fail("printRootCauseStackTrace(null) IndexOutOfBoundsException expected");
        }
        catch ( IndexOutOfBoundsException e )
        {
            //nothing to do, Exception was expected
        }

        try
        {
            ExceptionUtils.printRootCauseStackTrace( npe, (PrintStream) null );
            fail("printRootCauseStackTrace( x, null) NPE expected");
        }
        catch ( NullPointerException e )
        {
            //nothing to do, Exception was expected
        }

        try
        {
            ExceptionUtils.printRootCauseStackTrace( npe, (PrintWriter) null );
            fail("printRootCauseStackTrace( x, null) NPE expected");
        }
        catch ( NullPointerException e )
        {
            //nothing to do, Exception was expected
        }
    }

}
