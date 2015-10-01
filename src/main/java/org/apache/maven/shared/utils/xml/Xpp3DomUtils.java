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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class Xpp3DomUtils
{
    /**
     * @param dominant {@link Xpp3Dom}
     * @param recessive {@link Xpp3Dom}
     * @param childMergeOverride true/false.
     * @return Merged dom.
     */
    public static Xpp3Dom mergeXpp3Dom( Xpp3Dom dominant, Xpp3Dom recessive, Boolean childMergeOverride )
    {
        return dominant != null ? merge( dominant, recessive, childMergeOverride ) : recessive;
    }

    /**
     * @param dominant {@link Xpp3Dom}
     * @param recessive {@link Xpp3Dom}
     * @return Merged dom.
     */
    public static Xpp3Dom mergeXpp3Dom( Xpp3Dom dominant, Xpp3Dom recessive )
    {
        return dominant != null ? merge( dominant, recessive, null ) : recessive;
    }

    /**
     * @param dominant {@link Xpp3Dom}
     * @param recessive {@link Xpp3Dom}
     * @param childMergeOverride true/false.
     * @return Merged dom.
     */
    public static Xpp3Dom merge( Xpp3Dom dominant, Xpp3Dom recessive, Boolean childMergeOverride )
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

    private static boolean isCombineSelfOverride( Xpp3Dom xpp3Dom )
    {
        String selfMergeMode = xpp3Dom.getAttribute( Xpp3Dom.SELF_COMBINATION_MODE_ATTRIBUTE );
        return Xpp3Dom.SELF_COMBINATION_OVERRIDE.equals( selfMergeMode );
    }

    private static boolean isMergeChildren( Xpp3Dom dominant, Boolean override )
    {
        return override != null ? override : !isMergeChildren( dominant );
    }

    private static boolean isMergeChildren( Xpp3Dom dominant )
    {
        return Xpp3Dom.CHILDREN_COMBINATION_APPEND.equals(
            dominant.getAttribute( Xpp3Dom.CHILDREN_COMBINATION_MODE_ATTRIBUTE ) );
    }

    /**
     * @param str The string to be checked.
     * @return <code>true</code> in case string is empty <code>false</code> otherwise.
     */
    public static boolean isEmpty( String str )
    {
        return str == null || str.trim().length() == 0;
    }




}
