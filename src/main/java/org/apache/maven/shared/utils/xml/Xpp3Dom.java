package org.apache.maven.shared.utils.xml;

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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * A reimplementation of Plexus Xpp3Dom based on the public interface of Plexus Xpp3Dom.
 *
 * @author Kristian Rosenvold
 */
public class Xpp3Dom
    implements Iterable<Xpp3Dom>
{
    @SuppressWarnings( "UnusedDeclaration" )
    private static final long serialVersionUID = 2567894443061173996L;

    private String name; // plexus: protected

    private String value; // plexus: protected

    private Map<String, String> attributes; // plexus: protected

    private final List<Xpp3Dom> childList; // plexus: protected

    private final Map<String, Xpp3Dom> childMap; // plexus: protected

    private Xpp3Dom parent; // plexus: protected

    public static final String CHILDREN_COMBINATION_MODE_ATTRIBUTE = "combine.children";

    private static final String CHILDREN_COMBINATION_MERGE = "merge";

    public static final String CHILDREN_COMBINATION_APPEND = "append";

    @SuppressWarnings("UnusedDeclaration")
    private static final String DEFAULT_CHILDREN_COMBINATION_MODE = CHILDREN_COMBINATION_MERGE; // plexus: public

    public static final String SELF_COMBINATION_MODE_ATTRIBUTE = "combine.self";

    public static final String SELF_COMBINATION_OVERRIDE = "override";  // plexus: public

    public static final String SELF_COMBINATION_MERGE = "merge";

    @SuppressWarnings("UnusedDeclaration")
    private static final String DEFAULT_SELF_COMBINATION_MODE = SELF_COMBINATION_MERGE;  // plexus: public

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Xpp3Dom[] EMPTY_DOM_ARRAY = new Xpp3Dom[0];

    public Xpp3Dom( String name )
    {
        this.name = name;
        childList = new ArrayList<Xpp3Dom>();
        childMap = new HashMap<String, Xpp3Dom>();
    }

    public Xpp3Dom( Xpp3Dom source)
    {
        this( source, source.getName() );
    }

    public Xpp3Dom( @Nonnull Xpp3Dom src, String name )
    {
        this.name = name;

        int size = src.getChildCount();
        childList = new ArrayList<Xpp3Dom>( size );
        childMap = new HashMap<String, Xpp3Dom>();

        setValue( src.getValue() );

        for ( String attributeName : src.getAttributeNames() )
        {
            setAttribute( attributeName, src.getAttribute( attributeName ) );
        }

        for ( Xpp3Dom xpp3Dom : src.getChildren() )
        {
            addChild( new Xpp3Dom( xpp3Dom ) );
        }
    }

    public String getName()
    {
        return name;
    }

    public @Nonnull String getValue()
    {
        return value;
    }

    public void setValue( @Nonnull String value )
    {
        this.value = value;
    }


    public String[] getAttributeNames()
    {
        boolean isNothing = attributes == null || attributes.isEmpty();
        return isNothing ? EMPTY_STRING_ARRAY :  attributes.keySet().toArray( new String[attributes.size()] );
    }


    public String getAttribute( String name )
    {
        return attributes != null ? attributes.get( name ) : null;
    }

    @SuppressWarnings( "ConstantConditions" )
    public void setAttribute( @Nonnull String name, @Nonnull String value )
    {
        if ( value == null )
        {
            throw new NullPointerException( "value can not be null" );
        }
        if ( name == null )
        {
            throw new NullPointerException( "name can not be null" );
        }
        if ( attributes == null )
        {
            attributes = new HashMap<String, String>();
        }

        attributes.put( name, value );
    }

    public Xpp3Dom getChild( int i )
    {
        return childList.get( i );
    }

    public Xpp3Dom getChild( String name )
    {
        return childMap.get( name );
    }

    public void addChild( Xpp3Dom child )
    {
        child.setParent( this );
        childList.add( child );
        childMap.put( child.getName(), child );
    }

    public Xpp3Dom[] getChildren()
    {
        boolean isNothing = childList == null || childList.isEmpty();
        return isNothing ? EMPTY_DOM_ARRAY : childList.toArray( new Xpp3Dom[childList.size()] );
    }

    private List<Xpp3Dom> getChildrenList()
    {
        boolean isNothing = childList == null || childList.isEmpty();
        return isNothing ? Collections.<Xpp3Dom>emptyList() : childList;
    }

    public Xpp3Dom[] getChildren( String name )
    {
        List<Xpp3Dom> children = getChildrenList( name );
        return children.toArray( new Xpp3Dom[children.size()] );
    }

    private List<Xpp3Dom> getChildrenList( String name )
    {
        if ( childList == null )
        {
            return Collections.emptyList();
        }
        else
        {
            ArrayList<Xpp3Dom> children = new ArrayList<Xpp3Dom>();
            for ( Xpp3Dom aChildList : childList )
            {
                if ( name.equals( aChildList.getName() ) )
                {
                    children.add( aChildList );
                }
            }
            return children;
        }
    }

    public int getChildCount()
    {
        if ( childList == null )
        {
            return 0;
        }

        return childList.size();
    }

    public void removeChild( int i )
    {
        Xpp3Dom child = childList.remove( i );
        childMap.values().remove( child );
        child.setParent( null );
    }

    public Xpp3Dom getParent()
    {
        return parent;
    }

    public void setParent( Xpp3Dom parent )
    {
       this.parent = parent;
    }

    // Todo: Support writing to serializer (>1.0)
  //  public void writeToSerializer( String namespace, XmlSerializer serializer )
    //        throws IOException

    private static Xpp3Dom merge( Xpp3Dom dominant, Xpp3Dom recessive, Boolean childMergeOverride )
    {
        if ( recessive == null || isCombineSelfOverride( dominant ) )
        {
            return dominant;
        }

        if ( isEmpty( dominant.getValue() ) )
        {
            dominant.setValue( recessive.getValue() );
        }

        for ( String attr : recessive.getAttributeNames() )
        {
            if ( isEmpty( dominant.getAttribute( attr ) ) )
            {
                dominant.setAttribute( attr, recessive.getAttribute( attr ) );
            }
        }

        if ( recessive.getChildCount() > 0 )
        {
            boolean mergeChildren = isMergeChildren( dominant, childMergeOverride );

            if ( mergeChildren )
            {
                Map<String, Iterator<Xpp3Dom>> commonChildren = getCommonChildren( dominant, recessive );
                for ( Xpp3Dom recessiveChild : recessive )
                {
                    Iterator<Xpp3Dom> it = commonChildren.get( recessiveChild.getName() );
                    if ( it == null )
                    {
                        dominant.addChild( new Xpp3Dom( recessiveChild ) );
                    }
                    else if ( it.hasNext() )
                    {
                        Xpp3Dom dominantChild = it.next();
                        merge( dominantChild, recessiveChild, childMergeOverride );
                    }
                }
            }
            else
            {
                Xpp3Dom[] dominantChildren = dominant.getChildren();
                dominant.childList.clear();
                for ( Xpp3Dom child : recessive )
                {
                    dominant.addChild( new Xpp3Dom( child ) );
                }

                for ( Xpp3Dom aDominantChildren : dominantChildren )
                {
                    dominant.addChild( aDominantChildren );
                }
            }
        }
        return dominant;
    }

    private static Map<String, Iterator<Xpp3Dom>> getCommonChildren( Xpp3Dom dominant, Xpp3Dom recessive )
    {
        Map<String, Iterator<Xpp3Dom>> commonChildren = new HashMap<String, Iterator<Xpp3Dom>>();

        for ( String childName : recessive.childMap.keySet() )
        {
            List<Xpp3Dom> dominantChildren = dominant.getChildrenList( childName );
            if ( dominantChildren.size() > 0 )
            {
                commonChildren.put( childName, dominantChildren.iterator() );
            }
        }
        return commonChildren;
    }

    private static boolean isMergeChildren( Xpp3Dom dominant, Boolean override )
    {
        return override != null ? override : !isMergeChildren( dominant );
    }

    private static boolean isMergeChildren( Xpp3Dom dominant )
    {
        return CHILDREN_COMBINATION_APPEND.equals( dominant.getAttribute( CHILDREN_COMBINATION_MODE_ATTRIBUTE ) );
    }

    private static boolean isCombineSelfOverride( Xpp3Dom xpp3Dom )
    {
        String selfMergeMode = xpp3Dom.getAttribute( SELF_COMBINATION_MODE_ATTRIBUTE );
        return SELF_COMBINATION_OVERRIDE.equals( selfMergeMode );
    }

    public static Xpp3Dom mergeXpp3Dom( Xpp3Dom dominant, Xpp3Dom recessive, Boolean childMergeOverride )
    {
        return dominant != null ? merge( dominant, recessive, childMergeOverride ) : recessive;
    }

    public static Xpp3Dom mergeXpp3Dom( Xpp3Dom dominant, Xpp3Dom recessive )
    {
        return dominant != null ? merge( dominant, recessive, null ) : recessive;
    }

    public boolean equals( Object obj )
    {
        if ( obj == this )
        {
            return true;
        }

        if ( !( obj instanceof Xpp3Dom ) )
        {
            return false;
        }

        Xpp3Dom dom = (Xpp3Dom) obj;

        return !( name == null ? dom.name != null : !name.equals( dom.name ) )
            && !( value == null ? dom.value != null : !value.equals( dom.value ) )
            && !( attributes == null ? dom.attributes != null : !attributes.equals( dom.attributes ) )
            && !( childList == null ? dom.childList != null : !childList.equals( dom.childList ) );
    }

    public int hashCode()
    {
        int result = 17;
        result = 37 * result + ( name != null ? name.hashCode() : 0 );
        result = 37 * result + ( value != null ? value.hashCode() : 0 );
        result = 37 * result + ( attributes != null ? attributes.hashCode() : 0 );
        result = 37 * result + ( childList != null ? childList.hashCode() : 0 );
        return result;
    }

    public String toString()
    {
        StringWriter writer = new StringWriter();
        Xpp3DomWriter.write( getPrettyPrintXMLWriter( writer ), this );
        return writer.toString();

    }

    public String toUnescapedString()
    {
        StringWriter writer = new StringWriter();
        Xpp3DomWriter.write( getPrettyPrintXMLWriter( writer ), this, false );
        return writer.toString();
    }

    private PrettyPrintXMLWriter getPrettyPrintXMLWriter( StringWriter writer )
    {
        return new PrettyPrintXMLWriter( writer, "UTF-8", null );
    }

    public static boolean isNotEmpty( String str )
    {
        return str != null && str.length() > 0;
    }

    public static boolean isEmpty( String str )
    {
        return str == null || str.trim().length() == 0;
    }

    public Iterator<Xpp3Dom> iterator()
    {
        return getChildrenList().iterator();
    }
}
