package org.apache.maven.shared.utils.logging;

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
 * Logger level renderer, intended for Maven slf4j logging provider implementers to render
 * logger level.
 *
 * @since 3.2.0
 */
public interface LoggerLevelRenderer
{
    /**
     * Render a message at DEBUG level.
     * @param message the message to render.
     * @return the formatted message.
     */
    String debug( String message );
    
    /**
     * Render a message at INFO level.
     * @param message the message to render.
     * @return the formatted message.
     */
    String info( String message );
    
    /**
     * Render a message at WARNING level.
     * @param message the message to render.
     * @return the formatted message.
     */
    String warning( String message );
    
    /**
     * Render a message at ERROR level.
     * @param message the message to render.
     * @return the formatted message.
     */
    String error( String message );
}
