/* 
 * 
 * PROJECT
 *     Name
 *         APS OpenJPA Provider
 *     
 *     Code Version
 *         0.9.2
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

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import java.util.Set;

/**
 * Wraps a MetaModel and provides a valid context class loader during calls to it.
 */
public class ContextMetaModel implements Metamodel {
    //
    // Private Members
    //

    private ClassLoader contextClassLoader = null;

    private Metamodel mm = null;

    //
    // Constructors
    //

    public ContextMetaModel(ClassLoader contextClassLoader, Metamodel mm) {
        this.contextClassLoader = contextClassLoader;
        this.mm = mm;
    }

    //
    // Methods
    //

    @Override
    public <X> EntityType<X> entity(Class<X> xClass) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        EntityType<X> et = this.mm.entity(xClass);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return et;
    }

    @Override
    public <X> ManagedType<X> managedType(Class<X> xClass) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        ManagedType<X> mt = this.mm.managedType(xClass);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return mt;
    }

    @Override
    public <X> EmbeddableType<X> embeddable(Class<X> xClass) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        EmbeddableType<X> et = this.mm.embeddable(xClass);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return  et;
    }

    @Override
    public Set<ManagedType<?>> getManagedTypes() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        Set<ManagedType<?>> mt = this.mm.getManagedTypes();

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return mt;
    }

    @Override
    public Set<EntityType<?>> getEntities() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        Set<EntityType<?>> e = this.mm.getEntities();

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return e;
    }

    @Override
    public Set<EmbeddableType<?>> getEmbeddables() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        Set<EmbeddableType<?>> e = this.mm.getEmbeddables();

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return e;
    }
}
