/* 
 * 
 * PROJECT
 *     Name
 *         APS OpenJPA Provider
 *     
 *     Code Version
 *         0.9.0
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

import xob.XMLObject;

/**
 * This represents a property element.
 */
public interface Property extends XMLObject {

    /**
     * Returns the name of the property.
     */
    public String getName();

    /**
     * Returns the value of the property.
     */
    public String getValue();
}
