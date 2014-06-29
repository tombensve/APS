/* 
 * 
 * PROJECT
 *     Name
 *         APS OpenJPA Provider
 *     
 *     Code Version
 *         0.11.0
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
 *         2012-10-04: Created!
 *         
 */
package se.natusoft.osgi.aps.jpa.service;

import javax.persistence.PersistenceUnitUtil;

/**
 * Wraps a PersistenceUnitUtil and provides a valid context class loader during calls to it.
 */
public class ContextPersistenceUnitUtil implements PersistenceUnitUtil {
    //
    // Private Members
    //

    private ClassLoader contextClassLoader = null;

    private PersistenceUnitUtil puu = null;

    //
    // Constructors
    //

    public ContextPersistenceUnitUtil(ClassLoader contextClassLoader, PersistenceUnitUtil puu) {
        this.contextClassLoader = contextClassLoader;
        this.puu = puu;
    }

    //
    // Methods
    //

    @Override
    public boolean isLoaded(Object o, String s) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        boolean loaded = this.puu.isLoaded(o, s);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return loaded;
    }

    @Override
    public boolean isLoaded(Object o) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        boolean loaded = this.puu.isLoaded(o);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return loaded;
    }

    @Override
    public Object getIdentifier(Object o) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        Object obj = this.puu.getIdentifier(o);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return obj;
    }
}
