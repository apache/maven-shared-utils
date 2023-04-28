/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.shared.utils.xml;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.shared.utils.xml.pull.XmlPullParserException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Kristian Rosenvold
 */
public class Xpp3DomBuilder {

    /**
     * @param reader {@link Reader}
     * @return the built DOM
     * @throws XmlPullParserException in case of an error
     */
    public static Xpp3Dom build(@WillClose @Nonnull Reader reader) throws XmlPullParserException {
        return build(reader, false);
    }

    /**
     * @param is {@link InputStream}
     * @param encoding the encoding
     * @return the built DOM
     * @throws XmlPullParserException in case of an error
     */
    public static Xpp3Dom build(@WillClose InputStream is, @Nonnull String encoding) throws XmlPullParserException {
        return build(is, encoding, false);
    }

    /**
     * @param is {@link InputStream}
     * @param encoding the encoding
     * @param noop vestigial argument with no effect
     * @return the built DOM
     * @throws XmlPullParserException in case of an error
     * @deprecated use the two-arg variant
     */
    @Deprecated
    public static Xpp3Dom build(@WillClose InputStream is, @Nonnull String encoding, boolean noop)
            throws XmlPullParserException {
        try {
            Reader reader = new InputStreamReader(is, encoding);
            return build(reader);
        } catch (UnsupportedEncodingException e) {
            throw new XmlPullParserException(e);
        }
    }

    /**
     * @param in {@link Reader}
     * @param noop vestigial argument with no effect
     * @return the built DOM
     * @throws XmlPullParserException in case of an error
     * @deprecated use {#build(java.io.Reader)}
     */
    @Deprecated
    public static Xpp3Dom build(@WillClose Reader in, boolean noop) throws XmlPullParserException {
        try (Reader reader = in) {
            DocHandler docHandler = parseSax(new InputSource(reader));
            reader.close();
            return docHandler.result;
        } catch (final IOException e) {
            throw new XmlPullParserException(e);
        }
    }

    private static DocHandler parseSax(@Nonnull InputSource inputSource) throws XmlPullParserException {
        try {
            DocHandler ch = new DocHandler();
            XMLReader parser = createXmlReader();
            parser.setContentHandler(ch);
            parser.parse(inputSource);
            return ch;
        } catch (IOException e) {
            throw new XmlPullParserException(e);
        } catch (SAXException e) {
            throw new XmlPullParserException(e);
        }
    }

    private static XMLReader createXmlReader() throws SAXException {
        XMLReader comSunXmlReader = instantiate("com.sun.org.apache.xerces.internal.parsers.SAXParser");
        if (comSunXmlReader != null) {
            return comSunXmlReader;
        }

        String key = "org.xml.sax.driver";
        String oldParser = System.getProperty(key);
        System.clearProperty(key); // There's a "slight" problem with this an parallel maven: It does not work ;)

        try {
            return org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
        } finally {
            if (oldParser != null) {
                System.setProperty(key, oldParser);
            }
        }
    }

    private static XMLReader instantiate(String s) {
        try {
            Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(s);
            return (XMLReader) aClass.newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static class DocHandler extends DefaultHandler {
        private final List<Xpp3Dom> elemStack = new ArrayList<Xpp3Dom>();

        private final List<StringBuilder> values = new ArrayList<StringBuilder>();

        Xpp3Dom result = null;

        private boolean spacePreserve = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            spacePreserve = false;
            Xpp3Dom child = new Xpp3Dom(localName);

            attachToParent(child);
            pushOnStack(child);

            values.add(new StringBuilder());

            int size = attributes.getLength();
            for (int i = 0; i < size; i++) {
                String name = attributes.getQName(i);
                String value = attributes.getValue(i);
                child.setAttribute(name, value);
                spacePreserve = spacePreserve || ("xml:space".equals(name) && "preserve".equals(value));
            }
        }

        private boolean pushOnStack(Xpp3Dom child) {
            return elemStack.add(child);
        }

        private void attachToParent(Xpp3Dom child) {
            int depth = elemStack.size();
            if (depth > 0) {
                elemStack.get(depth - 1).addChild(child);
            }
        }

        private Xpp3Dom pop() {
            int depth = elemStack.size() - 1;
            return elemStack.remove(depth);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            int depth = elemStack.size() - 1;

            Xpp3Dom element = pop();

            /* this Object could be null if it is a singleton tag */
            Object accumulatedValue = values.remove(depth);

            if (element.getChildCount() == 0) {
                if (accumulatedValue == null) {
                    element.setValue(""); // null in xpp3dom, but we don't do that around here
                } else {
                    element.setValue(accumulatedValue.toString());
                }
            }

            if (depth == 0) {
                result = element;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String text = new String(ch, start, length);
            appendToTopValue(text);
        }

        private void appendToTopValue(String toAppend) {
            // noinspection MismatchedQueryAndUpdateOfStringBuilder
            StringBuilder stringBuilder = values.get(values.size() - 1);
            stringBuilder.append(toAppend);
        }
    }
}
