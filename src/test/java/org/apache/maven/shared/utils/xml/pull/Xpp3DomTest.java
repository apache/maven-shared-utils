package org.apache.maven.shared.utils.xml.pull;

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

import java.io.IOException;
import java.io.StringReader;
import org.apache.maven.shared.utils.xml.Xpp3Dom;
import org.apache.maven.shared.utils.xml.Xpp3DomBuilder;

import org.junit.Test;

import static org.apache.maven.shared.utils.xml.Xpp3Dom.mergeXpp3Dom;
import static org.junit.Assert.*;

/**
 * @author Kristian Rosenvold
 */
public class Xpp3DomTest
{

    private Xpp3Dom createElement( String element, String value )
    {
        Xpp3Dom t1s1 = new Xpp3Dom( element );
        t1s1.setValue( value );
        return t1s1;
    }


    @Test
    public void mergePrecedenceSelfClosed()
        throws XmlPullParserException, IOException
    {
        Xpp3Dom parentConfig = build( "<configuration><items><item/></items></configuration>" );
        Xpp3Dom childConfig = build( "<configuration><items><item>ooopise</item></items></configuration>" );

        Xpp3Dom result = Xpp3Dom.mergeXpp3Dom( childConfig, parentConfig );
        Xpp3Dom items = result.getChild( "items" );

        assertEquals( 1, items.getChildCount() );
        Xpp3Dom item = items.getChild( 0 );
        assertEquals( "ooopise", item.getValue() );
    }

    @Test
    public void mergePrecedenceOpenClose()
        throws XmlPullParserException, IOException
    {
        Xpp3Dom parentConfig = build( "<configuration><items><item></item></items></configuration>" );
        Xpp3Dom childConfig = build( "<configuration><items><item>ooopise</item></items></configuration>" );

        Xpp3Dom result = Xpp3Dom.mergeXpp3Dom( childConfig, parentConfig );
        Xpp3Dom items = result.getChild( "items" );

        assertEquals( 1, items.getChildCount() );
        Xpp3Dom item = items.getChild( 0 );
        assertEquals( "ooopise", item.getValue() );
    }

    @Test
    public void selfOverrideOnRootNode()
    {
        // Todo: This does not work when loaded. Probably a bug related to null vs "" handling
        //      Xpp3Dom t1 = build( "<top combine.self='override' attr='value'></top>" );

        Xpp3Dom t1 = new Xpp3Dom( "top" );
        t1.setAttribute( "attr", "value" );

        t1.setAttribute( Xpp3Dom.SELF_COMBINATION_MODE_ATTRIBUTE, Xpp3Dom.SELF_COMBINATION_OVERRIDE );

        Xpp3Dom t2 = build( "<top attr2='value2'>val2</top>" );
        Xpp3Dom result = mergeXpp3Dom( t1, t2 );

        assertEquals( 2, result.getAttributeNames().length );
        assertNull( result.getValue() );
    }

    @Test
    public void mergeValuesOnRootNode()
    {
        Xpp3Dom t1 = build( "<root attr='value'/>" );
        Xpp3Dom t2 = build( "<root attr2='value2'>t2Val</root>" );
        Xpp3Dom result = mergeXpp3Dom( t1, t2 );
        assertEquals( 2, result.getAttributeNames().length );
        assertEquals( result.getValue(), t2.getValue() );
    }

    @Test
    public void mergeAttributesOnRootNode()
    {
        Xpp3Dom t1 = build( "<root combine.self='merge' attr='value'/>" );
        Xpp3Dom t2 = build( "<root attr2='value2'/>" );

        Xpp3Dom dom = mergeXpp3Dom( t1, t2 );
        assertEquals( 3, dom.getAttributeNames().length );
    }

    @Test
    public void combineAppend()
    {
        Xpp3Dom t1 = new Xpp3Dom( "root" );
        t1.setAttribute( Xpp3Dom.CHILDREN_COMBINATION_MODE_ATTRIBUTE, Xpp3Dom.CHILDREN_COMBINATION_APPEND );
        t1.addChild( createElement( "sub", "s1Value" ) );

        Xpp3Dom t2 = new Xpp3Dom( "root" );
        t2.addChild( createElement( "sub", "s1Value" ) );

        Xpp3Dom result = mergeXpp3Dom( t1, t2 );

        assertEquals( 2, result.getChildren( "sub" ).length );
    }

    @Test
    public void mergeOverride()
    {
        Xpp3Dom t1 = new Xpp3Dom( "root" );
        t1.setAttribute( Xpp3Dom.CHILDREN_COMBINATION_MODE_ATTRIBUTE, Xpp3Dom.CHILDREN_COMBINATION_APPEND );
        t1.addChild( createElement( "sub", "s1Value" ) );

        Xpp3Dom t2 = new Xpp3Dom( "root" );
        t2.addChild( createElement( "sub", "s1Value" ) );

        Xpp3Dom result = mergeXpp3Dom( t1, t2, Boolean.TRUE );

        assertEquals( 1, result.getChildren( "sub" ).length );
    }


    @Test( expected = NullPointerException.class )
    public void nullValue()
    {
        //noinspection ConstantConditions
        new Xpp3Dom( "top" ).setAttribute( null, "value" );
    }

    @Test( expected = NullPointerException.class )
    public void nullAttribute()
    {
        //noinspection ConstantConditions
        new Xpp3Dom( "root" ).setAttribute( "attr", null );
    }


    @Test
    public void testEquals()
    {
        Xpp3Dom dom = new Xpp3Dom( "single" );
        dom.addChild( new Xpp3Dom( "kid" ) );

        Xpp3Dom other = new Xpp3Dom( "single" );
        other.addChild( new Xpp3Dom( "kid2" ) );

        assertEquals( dom, dom );
        //noinspection ObjectEqualsNull
        assertFalse( dom.equals( null ) );
        assertFalse( dom.equals( new Xpp3Dom( (String) null ) ) );
        assertFalse( dom.equals( other ) );
    }

    @Test
    public void dominantWinsCollections()
        throws XmlPullParserException
    {
        Xpp3Dom parent = build( "<root><entries><entry>uno</entry><entry>dos</entry></entries></root>" );
        Xpp3Dom dominant = build( "<root><entries><entry>tres</entry></entries></root>" );

        Xpp3Dom result = mergeXpp3Dom( dominant, parent );

        Xpp3Dom items = result.getChild( "entries" );
        assertEquals( 1, items.getChildCount() );

        assertElemEquals( "tres", items.getChild( 0 ) );
    }

    @Test
    public void combineChildrenAppendTest()
        throws XmlPullParserException
    {
        Xpp3Dom parent =
            build( "<root><entries><entry>uno</entry><entry>dos</entry><entry>tres</entry></entries></root>" );
        Xpp3Dom child = build( "<root><entries combine.children=\"append\"><entry>quatro</entry></entries></root>" );

        Xpp3Dom result = mergeXpp3Dom( child, parent );

        Xpp3Dom items = result.getChild( "entries" );
        assertEquals( 4, items.getChildCount() );

        Xpp3Dom[] item = items.getChildren();

        assertElemEquals( "uno", item[0] );
        assertElemEquals( "dos", item[1] );
        assertElemEquals( "tres", item[2] );
        assertElemEquals( "quatro", item[3] );
    }

    @Test
    public void unchangedWithFirstOrLastEmpty()
        throws Exception
    {
        String configStr = "<root><entries><entry/><entry>test</entry><entry/></entries></root>";
        Xpp3Dom dominant = build( configStr );
        Xpp3Dom duplicatedDominant = build( configStr );
        validateEntries( dominant );
        Xpp3Dom result = mergeXpp3Dom( dominant, duplicatedDominant );
        validateEntries( result );
    }

    private void validateEntries( Xpp3Dom result )
    {
        Xpp3Dom entries = result.getChild( "entries" );
        assertEquals( 3, entries.getChildCount() );
        assertXpp3Null( entries.getChild( 0 ) );
        assertEquals( "test", entries.getChild( 1 ).getValue() );
        assertXpp3Null( entries.getChild( 2 ) );
    }


    static void assertElemEquals( String value, Xpp3Dom element )
    {
        assertEquals( value, element.getValue() );
    }


    void assertXpp3Null( Xpp3Dom entry )
    {
        // Todo: When we used xpp3dom, all methods using this assert used to return null
        assertEquals( "", entry.getValue() );
    }

    @Test
    public void recessiveChildrenIncludedWhenDominantEmpty()
        throws Exception
    {
        String dominant = "<root><baz>bazzy</baz></root>";
        String recessive = "<root><bar>barry</bar></root>";

        Xpp3Dom merged = mergeXpp3Dom( build( dominant ), build( recessive ) );

        assertEquals( 2, merged.getChildCount() );
        assertEquals( "bazzy", merged.getChild( "baz" ).getValue() );
        assertEquals( "barry", merged.getChild( "bar" ).getValue() );
    }

    static Xpp3Dom build( String stringContent )
    {
        return Xpp3DomBuilder.build( new StringReader( stringContent ) );
    }

    @Test
    public void duplicatedChildren()
        throws IOException, XmlPullParserException
    {
        String dupes = "<root><baz>x</baz><baz>y</baz></root>";
        assertEquals( "y", build( dupes ).getChild( "baz" ).getValue() );
    }

}
