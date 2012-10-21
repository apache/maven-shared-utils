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


/**
 * Interface for tools writing XML files.
 * XMLWriters are not thread safe and must not be accessed concurrently.
 */
public interface XMLWriter
{

    /**
     * Sets the encoding of the document.
     * If not set, UTF-8 is being used
     *
     * @param encoding the encoding
     * @throws IllegalStateException if the generation of the document has already started
     */
    void setEncoding( String encoding );

    /**
     * Sets the docType of the document.
     *
     * @param docType the docType
     * @throws IllegalStateException if the generation of the document has already started
     */
    void setDocType( String docType );


    /**
     * Start an XML Element tag.
     * @param name
     */
    void startElement( String name );


    /**
     * Add a XML attribute to the current XML Element.
     * This method must get called immediately after {@link #startElement(String)}
     * @param key
     * @param value
     * @throws IllegalStateException if no element tag is currently in process
     */
    void addAttribute( String key, String value );

    /**
     * Add a value text to the current element tag
     * This will perform XML escaping to guarantee valid content
     * @param text
     * @throws IllegalStateException if no element tag got started yet
     */
    void writeText( String text );

    /**
     * Add a preformatted markup to the current element tag
     * @param text
     * @throws IllegalStateException if no element tag got started yet
     */
    void writeMarkup( String text );

    /**
     * End the previously opened element.
     * @see #startElement(String)
     */
    void endElement();
}
