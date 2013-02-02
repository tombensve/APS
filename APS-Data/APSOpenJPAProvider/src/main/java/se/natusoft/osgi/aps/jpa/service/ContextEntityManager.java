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
 *         2012-10-04: Created!
 *         
 */
package se.natusoft.osgi.aps.jpa.service;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;

/**
 * Wraps an EntityManager and provides a valid context class loader during calls to it.
 */
public class ContextEntityManager implements EntityManager {
    //
    // Private Members
    //

    private ClassLoader contextClassLoader = null;

    private EntityManager em = null;

    private EntityManagerFactory emf = null;

    private Metamodel mm = null;

    //
    // Constructors
    //

    public ContextEntityManager(ClassLoader contextClassLoader, EntityManager em, EntityManagerFactory emf) {
        this.contextClassLoader = contextClassLoader;
        this.em = em;
        this.emf = emf;
    }

    //
    // Methods
    //

    @Override
    public void persist(Object o) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.persist(o);

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public <T> T merge(T t) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        T obj = this.em.merge(t);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return obj;
    }

    @Override
    public void remove(Object o) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.remove(o);

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public <T> T find(Class<T> tClass, Object o) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        T found = this.em.find(tClass, o);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return found;
    }

    @Override
    public <T> T find(Class<T> tClass, Object o, Map<String, Object> stringObjectMap) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        T found = this.em.find(tClass, o, stringObjectMap);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return found;
    }

    @Override
    public <T> T find(Class<T> tClass, Object o, LockModeType lockModeType) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        T found = this.em.find(tClass, o, lockModeType);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return found;
    }

    @Override
    public <T> T find(Class<T> tClass, Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        T found = this.em.find(tClass, o, lockModeType, stringObjectMap);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return found;
    }

    @Override
    public <T> T getReference(Class<T> tClass, Object o) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        T ref = this.em.getReference(tClass, o);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return ref;
    }

    @Override
    public void flush() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.flush();

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public void setFlushMode(FlushModeType flushModeType) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.setFlushMode(flushModeType);

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public FlushModeType getFlushMode() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        FlushModeType fmt = this.em.getFlushMode();

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return fmt;
    }

    @Override
    public void lock(Object o, LockModeType lockModeType) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.lock(o, lockModeType);

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public void lock(Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.lock(o, lockModeType, stringObjectMap);

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public void refresh(Object o) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.refresh(o);

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public void refresh(Object o, Map<String, Object> stringObjectMap) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.refresh(o, stringObjectMap);

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public void refresh(Object o, LockModeType lockModeType) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.refresh(o, lockModeType);

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public void refresh(Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.refresh(o, lockModeType, stringObjectMap);

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public void clear() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.clear();

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public void detach(Object o) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.detach(o);

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public boolean contains(Object o) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        boolean c = this.em.contains(o);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return c;
    }

    @Override
    public LockModeType getLockMode(Object o) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        LockModeType lmt = this.em.getLockMode(o);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return lmt;
    }

    @Override
    public void setProperty(String s, Object o) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.setProperty(s, o);

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public Map<String, Object> getProperties() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        Map<String, Object> props = this.em.getProperties();

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return props;
    }

    @Override
    public Query createQuery(String s) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        Query q = this.em.createQuery(s);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return q;
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> tCriteriaQuery) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        TypedQuery<T> tq = this.em.createQuery(tCriteriaQuery);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return tq;
    }

    @Override
    public <T> TypedQuery<T> createQuery(String s, Class<T> tClass) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        TypedQuery<T> tq = this.em.createQuery(s, tClass);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return tq;
    }

    @Override
    public Query createNamedQuery(String s) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        Query q = this.em.createNamedQuery(s);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return q;
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String s, Class<T> tClass) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        TypedQuery<T> tq = this.em.createNamedQuery(s, tClass);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return tq;
    }

    @Override
    public Query createNativeQuery(String s) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        Query q = this.em.createNativeQuery(s);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return q;
    }

    @Override
    public Query createNativeQuery(String s, Class aClass) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        Query q = this.em.createNativeQuery(s, aClass);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return q;
    }

    @Override
    public Query createNativeQuery(String s, String s1) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        Query q = this.em.createNativeQuery(s, s1);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return q;
    }

    @Override
    public void joinTransaction() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.joinTransaction();

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public <T> T unwrap(Class<T> tClass) {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        T t = this.em.unwrap(tClass);

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return t;
    }

    @Override
    public Object getDelegate() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        Object obj = this.em.getDelegate();

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return obj;
    }

    @Override
    public void close() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        this.em.close();

        Thread.currentThread().setContextClassLoader(savedClassLoader);
    }

    @Override
    public boolean isOpen() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        boolean open = this.em.isOpen();

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return open;
    }

    @Override
    public EntityTransaction getTransaction() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        EntityTransaction et = this.em.getTransaction();

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return  et;
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return this.emf;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.contextClassLoader);

        CriteriaBuilder cb = this.em.getCriteriaBuilder();

        Thread.currentThread().setContextClassLoader(savedClassLoader);

        return cb;
    }

    @Override
    public Metamodel getMetamodel() {
        if (this.mm == null) {
            this.mm = new ContextMetaModel(this.contextClassLoader, this.em.getMetamodel());
        }

        return this.mm;
    }
}
