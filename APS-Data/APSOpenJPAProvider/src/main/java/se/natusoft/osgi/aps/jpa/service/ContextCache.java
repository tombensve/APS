/* 
 * 
 * PROJECT
 *     Name
 *         APS OpenJPA Provider
 *     
 *     Code Version
 *         1.0.0
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

import javax.persistence.Cache;

/**
 * Wraps a Cache and provides a valid context class loader during calls to it.
 */
public class ContextCache implements Cache {
    //
    // Private Members
    //

    private ClassLoader contextClassLoader = null;

    private Cache cache = null;

    //
    // Constructors
    //

    public ContextCache(ClassLoader contextClassLoader, Cache cache) {
        this.contextClassLoader = contextClassLoader;
        this.cache = cache;
    }

    //
    // Methods
    //

    @Override
    public boolean contains(Class aClass, Object o) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        boolean cont = this.cache.contains(aClass, o);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return cont;
    }

    @Override
    public void evict(Class aClass, Object o) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.cache.evict(aClass, o);

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public void evict(Class aClass) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.cache.evict(aClass);

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public void evictAll() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.cache.evictAll();

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }
}
