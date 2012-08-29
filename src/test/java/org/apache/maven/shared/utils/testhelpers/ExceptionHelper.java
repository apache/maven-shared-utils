package org.apache.maven.shared.utils.testhelpers;

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


import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;


public class ExceptionHelper
{

    /**
     * A matcher that verifies that the a root cause of an exception is of the specified type.
     *
     * @param cause the type of exception that caused this.
     * @return A matcher that verifies that the a root cause of an exception is of the specified type.
     */
    public static Matcher<Throwable> hasCause( Class<? extends Throwable> cause )
    {
        return new HasCause( cause );
    }

    private static class HasCause
            extends BaseMatcher<Throwable>
    {
        private final Class<? extends Throwable> cause;

        public HasCause( Class<? extends Throwable> cause )
        {
            this.cause = cause;
        }

        public boolean matches( Object item )
        {
            Throwable throwable = (Throwable) item;
            while ( throwable != null && !cause.isInstance( throwable ) )
            {
                throwable = throwable.getCause();
            }
            return cause.isInstance( throwable );
        }

        public void describeTo( Description description )
        {
            description.appendText( "was caused by a " ).appendValue( cause ).appendText( " being thrown" );
        }
    }
}
