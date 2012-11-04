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

import org.apache.maven.shared.utils.io.IOUtil;
import org.apache.maven.shared.utils.xml.pull.XmlPullParserException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kristian Rosenvold
 */
public class Xpp3DomBuilder
{
    private static final boolean DEFAULT_TRIM = true;

    public static Xpp3Dom build( @WillClose @Nonnull Reader reader )
        throws XmlPullParserException
    {
        return build( reader, DEFAULT_TRIM );
    }

    public static Xpp3Dom build( @WillClose InputStream is, @Nonnull String encoding )
        throws XmlPullParserException
    {
        return build( is, encoding, DEFAULT_TRIM );
    }

    public static Xpp3Dom build( @WillClose InputStream is, @Nonnull String encoding, boolean trim )
        throws XmlPullParserException
    {
        try
        {
            Reader reader = new InputStreamReader( is, encoding );
            return build( reader, trim );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static Xpp3Dom build( @WillClose Reader reader, boolean trim )
            throws XmlPullParserException
    {
        try
        {
            DocHandler docHandler = parseSax( new InputSource( reader ), trim );
            return docHandler.result;
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    private static DocHandler parseSax( @Nonnull InputSource inputSource, boolean trim )
        throws XmlPullParserException
    {

        try
        {
            DocHandler ch = new DocHandler( trim );
            XMLReader parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
            parser.setContentHandler( ch );
            parser.parse( inputSource );
            return ch;
        }
        catch ( IOException e )
        {
            throw new XmlPullParserException( e );
        }
        catch ( SAXException e )
        {
            throw new XmlPullParserException( e );
        }
    }

    private static class DocHandler
        extends DefaultHandler
    {
        private final List<Xpp3Dom> elemStack = new ArrayList<Xpp3Dom>();

        private final List<StringBuilder> values = new ArrayList<StringBuilder>();

        // Todo: Use these for something smart !
        private final List<SAXParseException> warnings = new ArrayList<SAXParseException>();
        private final List<SAXParseException> errors = new ArrayList<SAXParseException>();
        private final List<SAXParseException> fatals = new ArrayList<SAXParseException>();


        Xpp3Dom result = null;

        private final boolean trim;

        DocHandler( boolean trim )
        {
            this.trim = trim;
        }

        @Override
        public void startElement( String uri, String localName, String qName, Attributes attributes )
            throws SAXException
        {

            Xpp3Dom child = new Xpp3Dom( localName );

            attachToParent( child );
            pushOnStack( child );

            // Todo: Detecting tags that close immediately seem to be impossible in sax ?
            // http://stackoverflow.com/questions/12968390/detecting-self-closing-tags-in-sax
            values.add( new StringBuilder() );

            int size = attributes.getLength();
            for ( int i = 0; i < size; i++ )
            {
                child.setAttribute( attributes.getQName( i ), attributes.getValue( i ) );
            }
        }

        private boolean pushOnStack( Xpp3Dom child )
        {
            return elemStack.add( child );
        }

        private void attachToParent( Xpp3Dom child )
        {
            int depth = elemStack.size();
            if ( depth > 0 )
            {
                elemStack.get( depth - 1 ).addChild( child );
            }
        }

        @Override
        public void warning( SAXParseException e )
            throws SAXException
        {
            warnings.add( e );
        }

        @Override
        public void error( SAXParseException e )
            throws SAXException
        {
            errors.add( e );
        }

        @Override
        public void fatalError( SAXParseException e )
            throws SAXException
        {
            fatals.add( e );
        }

        private Xpp3Dom pop()
        {
            int depth = elemStack.size() - 1;
            return elemStack.remove( depth );
        }

        @Override
        public void endElement( String uri, String localName, String qName )
            throws SAXException
        {
            int depth = elemStack.size() - 1;

            Xpp3Dom element = pop();

            /* this Object could be null if it is a singleton tag */
            Object accumulatedValue = values.remove( depth );

            if ( element.getChildCount() == 0 )
            {
                if ( accumulatedValue == null )
                {
                    element.setValue( "" ); // null in xpp3dom, but we don't do that around here
                }
                else
                {
                    element.setValue( accumulatedValue.toString() );
                }
            }

            if ( depth == 0 )
            {
                result = element;
            }
        }

        @Override
        public void characters( char[] ch, int start, int length )
            throws SAXException
        {
            String text = new String( ch, start, length );
            appendToTopValue( trim ? text.trim() : text );
        }

        private void appendToTopValue( String toAppend )
        {
            // noinspection MismatchedQueryAndUpdateOfStringBuilder
            StringBuilder stringBuilder = values.get( values.size() - 1 );
            stringBuilder.append( toAppend );
        }
    }

}
