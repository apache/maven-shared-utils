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

import java.io.IOException;
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
    private static final long serialVersionUID = 2567894443061173996L;

    private String name; // plexus: protected

    private String value; // plexus: protected

    private Map<String, String> attributes; // plexus: protected

    final List<Xpp3Dom> childList; // plexus: protected

    final Map<String, Xpp3Dom> childMap; // plexus: protected

    private Xpp3Dom parent; // plexus: protected

    /**
     * The attribute which identifies merge/append.
     */
    public static final String CHILDREN_COMBINATION_MODE_ATTRIBUTE = "combine.children";

    private static final String CHILDREN_COMBINATION_MERGE = "merge";

    /**
     * The attribute append.
     */
    public static final String CHILDREN_COMBINATION_APPEND = "append";

    @SuppressWarnings( "UnusedDeclaration" )
    private static final String DEFAULT_CHILDREN_COMBINATION_MODE = CHILDREN_COMBINATION_MERGE; // plexus: public

    /**
     * The name of the attribute.
     */
    public static final String SELF_COMBINATION_MODE_ATTRIBUTE = "combine.self";

    /**
     * The attributes which identifies <code>override</code>.
     */
    public static final String SELF_COMBINATION_OVERRIDE = "override";  // plexus: public

    /**
     * The attribute which identifies <code>merge</code>
     */
    public static final String SELF_COMBINATION_MERGE = "merge";

    @SuppressWarnings( "UnusedDeclaration" )
    private static final String DEFAULT_SELF_COMBINATION_MODE = SELF_COMBINATION_MERGE;  // plexus: public

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Xpp3Dom[] EMPTY_DOM_ARRAY = new Xpp3Dom[0];

    /**
     * @param name The name of the instance.
     */
    public Xpp3Dom( String name )
    {
        this.name = name;
        childList = new ArrayList<Xpp3Dom>();
        childMap = new HashMap<String, Xpp3Dom>();
    }

    /**
     * Create instance.
     * @param source The source.
     */
    public Xpp3Dom( Xpp3Dom source )
    {
        this( source, source.getName() );
    }

    /**
     * Create instance.
     * @param src The source Dom.
     * @param name The name of the Dom.
     */
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

    /**
     * @return The current name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return The current value.
     */
    @Nonnull public String getValue()
    {
        return value;
    }

    /**
     * @param value The value to be set.
     */
    public void setValue( @Nonnull String value )
    {
        this.value = value;
    }


    /**
     * @return The array of attribute names.
     */
    public String[] getAttributeNames()
    {
        boolean isNothing = attributes == null || attributes.isEmpty();
        return isNothing ? EMPTY_STRING_ARRAY :  attributes.keySet().toArray( new String[attributes.size()] );
    }


    /**
     * @param nameParameter The name of the attribute.
     * @return The attribute value.
     */
    public String getAttribute( String nameParameter )
    {
        return this.attributes != null ? this.attributes.get( nameParameter ) : null;
    }

    /**
     * @param nameParameter The name of the attribute.
     * @param valueParameter The value of the attribute.
     */
    public void setAttribute( @Nonnull String nameParameter, @Nonnull String valueParameter )
    {
        if ( valueParameter == null )
        {
            throw new NullPointerException( "value can not be null" );
        }
        if ( nameParameter == null )
        {
            throw new NullPointerException( "name can not be null" );
        }
        if ( attributes == null )
        {
            attributes = new HashMap<String, String>();
        }

        attributes.put( nameParameter, valueParameter );
    }

    /**
     * @param i The index to be selected.
     * @return The child selected by index.
     */
    public Xpp3Dom getChild( int i )
    {
        return childList.get( i );
    }

    /**
     * @param nameParameter The name of the child.
     * @return The child selected by name.
     */
    public Xpp3Dom getChild( String nameParameter )
    {
        return childMap.get( nameParameter );
    }

    /**
     * @param child The child to be added.
     */
    public void addChild( Xpp3Dom child )
    {
        child.setParent( this );
        childList.add( child );
        childMap.put( child.getName(), child );
    }

    /**
     * @return The array of childs.
     */
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

    /**
     * @param nameParameter The name of the child.
     * @return The array of the Dom.
     */
    public Xpp3Dom[] getChildren( String nameParameter )
    {
        List<Xpp3Dom> children = getChildrenList( nameParameter );
        return children.toArray( new Xpp3Dom[children.size()] );
    }

    List<Xpp3Dom> getChildrenList( String nameParameter )
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
                if ( nameParameter.equals( aChildList.getName() ) )
                {
                    children.add( aChildList );
                }
            }
            return children;
        }
    }

    /**
     * @return The number of childs.
     */
    public int getChildCount()
    {
        if ( childList == null )
        {
            return 0;
        }

        return childList.size();
    }

    /**
     * @param i The child to be removed.
     */
    public void removeChild( int i )
    {
        Xpp3Dom child = childList.remove( i );
        childMap.values().remove( child );
        child.setParent( null );
    }

    /**
     * @return The current parent.
     */
    public Xpp3Dom getParent()
    {
        return parent;
    }

    /**
     * @param parent Set the parent.
     */
    public void setParent( Xpp3Dom parent )
    {
       this.parent = parent;
    }

    // Todo: Support writing to serializer (>1.0)
  //  public void writeToSerializer( String namespace, XmlSerializer serializer )
    //        throws IOException

    private static Xpp3Dom merge( Xpp3Dom dominant, Xpp3Dom recessive, Boolean childMergeOverride )
    {
        return Xpp3DomUtils.merge( dominant, recessive, childMergeOverride );
    }

    /**
     * @param dominant The dominant part.
     * @param recessive The recessive part.
     * @param childMergeOverride true if child merge will take precedence false otherwise.
     * @return The merged Xpp3Dom.
     */
    public static Xpp3Dom mergeXpp3Dom( Xpp3Dom dominant, Xpp3Dom recessive, Boolean childMergeOverride )
    {
        return Xpp3DomUtils.mergeXpp3Dom( dominant, recessive, childMergeOverride );
    }

    /**
     * @param dominant The dominant part.
     * @param recessive The recessive part.
     * @return The merged Xpp3Dom.
     */
    public static Xpp3Dom mergeXpp3Dom( Xpp3Dom dominant, Xpp3Dom recessive )
    {
        return Xpp3DomUtils.mergeXpp3Dom( dominant, recessive );
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public int hashCode()
    {
        int result = 17;
        result = 37 * result + ( name != null ? name.hashCode() : 0 );
        result = 37 * result + ( value != null ? value.hashCode() : 0 );
        result = 37 * result + ( attributes != null ? attributes.hashCode() : 0 );
        result = 37 * result + ( childList != null ? childList.hashCode() : 0 );
        return result;
    }

    /** {@inheritDoc} */
    public String toString()
    {
        try
        {
            StringWriter writer = new StringWriter();
            Xpp3DomWriter.write( getPrettyPrintXMLWriter( writer ), this );
            return writer.toString();
        }
        catch ( final IOException e )
        {
            // JDK error in StringWriter.
            throw (AssertionError) new AssertionError( "Unexpected IOException from StringWriter." ).initCause( e );
        }
    }

    /**
     * @return Unescaped string.
     */
    public String toUnescapedString()
    {
        try
        {
            StringWriter writer = new StringWriter();
            Xpp3DomWriter.write( getPrettyPrintXMLWriter( writer ), this, false );
            return writer.toString();
        }
        catch ( final IOException e )
        {
            // JDK error in StringWriter.
            throw (AssertionError) new AssertionError( "Unexpected IOException from StringWriter." ).initCause( e );
        }
    }

    private PrettyPrintXMLWriter getPrettyPrintXMLWriter( StringWriter writer )
    {
        return new PrettyPrintXMLWriter( writer, "UTF-8", null );
    }

    /**
     * @param str The string to be checked.
     * @return true if the string is not empty (length &gt; 0) and not <code>null</code>.
     */
    public static boolean isNotEmpty( String str )
    {
        return str != null && str.length() > 0;
    }

    /**
     * @param str The string to be checked.
     * @return true if the string is empty or <code>null</code>.
     */
    public static boolean isEmpty( String str )
    {
        return str == null || str.trim().length() == 0;
    }

    /** {@inheritDoc} */
    public Iterator<Xpp3Dom> iterator()
    {
        return getChildrenList().iterator();
    }
}
