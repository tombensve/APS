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

import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;

/**
 * Wraps an EntityManagerFactory and provides a valid context class loader during calls to it.
 */
public class ContextEntityManagerFactory implements EntityManagerFactory {

    //
    // Private Members
    //

    private ClassLoader contextClassLoader = null;

    private EntityManagerFactory emf = null;

    private Cache cache = null;

    private PersistenceUnitUtil puu = null;

    //
    // Constructors
    //

    public ContextEntityManagerFactory(ClassLoader contextClassLoader, EntityManagerFactory emf) {
        this.contextClassLoader = contextClassLoader;
        this.emf = emf;
    }

    //
    // Methods
    //

    public EntityManagerFactory getEntityManagerFactory() {
        return this.emf;
    }

    @Override
    public EntityManager createEntityManager() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        EntityManager em = new ContextEntityManager(this.contextClassLoader, this.emf.createEntityManager(), this);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return em;
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        EntityManager em = new ContextEntityManager(this.contextClassLoader, this.emf.createEntityManager(map), this);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return em;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        CriteriaBuilder cb = this.emf.getCriteriaBuilder();

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return cb;
    }

    @Override
    public Metamodel getMetamodel() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        Metamodel mm = new ContextMetaModel(this.contextClassLoader, this.emf.getMetamodel());

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return mm;
    }

    @Override
    public boolean isOpen() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        boolean isopen = this.emf.isOpen();

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return isopen;
    }

    @Override
    public void close() {
        throw new APSRuntimeException("You are not authorized to close this EntityManagerFactory! It is managed by aps-openjpa-provider.");
    }

    @Override
    public Map<String, Object> getProperties() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        Map<String, Object> props = this.emf.getProperties();

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return props;
    }

    @Override
    public Cache getCache() {
        if (this.cache == null) {
            this.cache = new ContextCache(this.contextClassLoader, this.emf.getCache());
        }

        return this.cache;
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        if (this.puu == null) {
            this.puu =  new ContextPersistenceUnitUtil(this.contextClassLoader, this.emf.getPersistenceUnitUtil());
        }

        return this.puu;
    }
}
