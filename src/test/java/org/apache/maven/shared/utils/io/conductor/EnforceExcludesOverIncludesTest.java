package org.apache.maven.shared.utils.io.conductor;

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

import org.apache.maven.shared.utils.io.DirectoryScanner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class EnforceExcludesOverIncludesTest
{
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void dontVisitChildren() throws IOException
    {
        createFakeFiles();

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setScanConductor(new EnforceExcludesOverIncludes());
        scanner.setExcludes( "**/target/**" );
        scanner.setIncludes( "**" ); // files in target will match include but our conductor will bypass them anyway
        scanner.setBasedir( folder.getRoot() );
        scanner.scan();
        assertResultEquals( asList( "bar", "sub/other" ), scanner.getIncludedFiles() );
        assertResultEquals( singletonList( "target" ), scanner.getExcludedDirectories() );
    }

    @Test // we don't set the conductor to ensure we have a "control" test to compare to the other
    public void controlTest() throws IOException
    {
        createFakeFiles();

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setExcludes( "**/target/**" );
        scanner.setIncludes( "**" ); // files in target will match include but our conductor will bypass them anyway
        scanner.setBasedir( folder.getRoot() );
        scanner.scan();
        assertResultEquals( asList( "bar", "sub/other" ), scanner.getIncludedFiles() );
        assertResultEquals( asList( "target", "target/nested" ), scanner.getExcludedDirectories() );
    }

    private void createFakeFiles() throws IOException
    {
        touch(new File(folder.getRoot(), "bar"));
        touch(new File(folder.getRoot(), "sub/other"));
        touch(new File(folder.getRoot(), "target/foo"));
        touch(new File(folder.getRoot(), "target/nested/dummy"));
    }

    private void assertResultEquals( final List<String> expected, final String[] actual )
    {
        final List<String> actualList = new ArrayList<>( asList( actual ) );
        Collections.sort( actualList );
        assertEquals( expected, actualList );
    }

    private void touch( final File file ) throws IOException
    {
        file.getParentFile().mkdirs();
        new FileWriter( file ).close();
    }
}
