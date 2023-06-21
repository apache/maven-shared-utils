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
package org.apache.maven.shared.utils.xml.pull;

import java.io.IOException;

import org.xml.sax.SAXException;

/**
* @deprecated use org.w3c.dom, JDOM, XOM, or SAX 
*/
@Deprecated
public class XmlPullParserException extends RuntimeException {

    private static final long serialVersionUID = 117075811816936575L;

    /**
     * @param e the root cause
     */
    public XmlPullParserException(IOException e) {
        super(e);
    }

    /**
     * @param e the root cause
     */
    public XmlPullParserException(SAXException e) {
        super(e);
    }

    /**
     * @param message the message
     */
    public XmlPullParserException(String message) {
        super(message);
    }
}
