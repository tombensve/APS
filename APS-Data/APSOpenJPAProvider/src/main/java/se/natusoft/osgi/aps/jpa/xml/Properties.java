/* 
 * 
 * PROJECT
 *     Name
 *         APS OpenJPA Provider
 *     
 *     Code Version
 *         0.9.1
 *     
 *     Description
 *         Provides an implementation of APSJPAService using OpenJPA.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-08-20: Created!
 *         
 */
package se.natusoft.osgi.aps.jpa.xml;

import se.natusoft.tools.xob.XMLObject;

import java.util.Iterator;

/**
 * This represents a properties element.
 */
public interface Properties extends XMLObject {

    /**
     * Returns an iterator of individual properties.
     */
    public Iterator getPropertys();

    // Note: I know "Propertys" are wrong. In plain English it should be "Properties". The name used
    // is however set by XOB which require the word after "get" to be the name of the interface returned
    // and in the many case the return type is an Iterator and the end of the name is suffixed with an "s".
}

