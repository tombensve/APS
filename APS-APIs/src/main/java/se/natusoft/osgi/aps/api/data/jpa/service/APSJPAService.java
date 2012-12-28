/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
 *         2012-07-18: Created!
 *         
 */
package se.natusoft.osgi.aps.api.data.jpa.service;

import org.osgi.framework.BundleContext;
import se.natusoft.osgi.aps.exceptions.APSResourceNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Map;

/**
 * This service allows an JPA EntityManager to be gotten for a persistent unit name.
 * <p/>
 * So why is this done this way ? Why is not an EntityManagerFactory returned?
 * <p/>
 * The answer to that is that the EntityManagerFactory is internal to the service who is
 * responsible for creating it and for closing it at sometime (stopping of bundle). The
 * client only needs an EntityManager for which the client is responsible after its creation.
 * <p/>
 * The creation of the EntityManagerFactory is delayed until the call to initialize(...).
 * Creating the EMF along with the persistence provider at persistence bundle discovery would
 * limit database connection properties to the persistence.xml file which is less than optimal
 * to put it mildly. The whole point with the APS project is to provide a configured platform
 * into which you can drop applications and they adapt to their surrounding. Not unlike what
 * JEE does, but does it milder and more flexibly being OSGi and also provides application
 * and service specific configuration with a web gui for editing configuration. Thereby
 * providing database connection properties from clients allows clients more flexibility
 * in how they want to handle that. The APSDataSourceDef service can for example be used
 * to lookup a JDBC connection definition. The default provider implementation of this
 * service uses OpenJPA which provides its own connection pooling.
 */
public interface APSJPAService {

    /**
     * Initializes and returns a provider from the specified properties.
     *
     * @param bundleContext The context of the client bundle. It is used to locate its persistence provider.
     * @param persistenceUnitName The name of the persistent unit defined in persistence.xml.
     * @param props Custom properties to configure database, etc.
     *
     * @return A configured EntityManager.
     */
    APSJPAEntityManagerProvider initialize(BundleContext bundleContext, String persistenceUnitName, Map<String, String> props) throws APSResourceNotFoundException;

    /**
     * Once you get this it is valid until the APSJPAService is stopped (which will happen if the service is redeployed!).
     */
    public static interface APSJPAEntityManagerProvider {

        /**
         * Returns true if this instance is valid. If not call APSJPAService.initialize(...) again to get a new instance.
         * It will be invalid if the APSJPAService provider have been restarted.
         */
        public boolean isValid();

        /**
         * Creates a new EntityManager. You are responsible for closing it!
         * <p/>
         * Please note that the EntityManager caches all referenced entities. If you keep and reuse it for a longer
         * time it can use more memory. For example at
         * <a href='http://docs.jboss.org/ejb3/app-server/tutorial/extended_pc/extended.html'>http://docs.jboss.org/ejb3/app-server/tutorial/extended_pc/extended.html</a>
         * it says that "Usually, an EntityManager in JBoss EJB 3.0 lives and dies within a JTA transaction". This
         * indicates how long-lived the EntityManager should preferably be.
         *
         * @return A configured EntityManager.
         */
        EntityManager createEntityManager();

        /**
         * Returns the underlaying entity manager factory. This will return null if isValid() return false!
         */
        EntityManagerFactory getEntityManagerFactory();
    }

}
