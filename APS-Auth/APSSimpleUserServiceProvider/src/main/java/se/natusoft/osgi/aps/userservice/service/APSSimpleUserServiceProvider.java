/*
 *
 * PROJECT
 *     Name
 *         APS Simple User Service Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides an implementation of APSSimpleUserService backed by a database.
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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-07-15: Created!
 *
 */
package se.natusoft.osgi.aps.userservice.service;

import org.osgi.framework.BundleContext;
import se.natusoft.osgi.aps.api.auth.user.APSSimpleUserService;
import se.natusoft.osgi.aps.api.auth.user.APSSimpleUserServiceAdmin;
import se.natusoft.osgi.aps.api.auth.user.model.Role;
import se.natusoft.osgi.aps.api.auth.user.model.RoleAdmin;
import se.natusoft.osgi.aps.api.auth.user.model.User;
import se.natusoft.osgi.aps.api.auth.user.model.UserAdmin;
import se.natusoft.osgi.aps.api.core.configold.service.APSConfigService;
import se.natusoft.osgi.aps.api.data.jdbc.model.DataSourceDef;
import se.natusoft.osgi.aps.api.data.jdbc.service.APSDataSourceDefService;
import se.natusoft.osgi.aps.api.data.jpa.service.APSJPAService;
import se.natusoft.osgi.aps.exceptions.APSPersistenceException;
import se.natusoft.osgi.aps.tools.APSActivator;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.annotation.activator.Managed;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider;
import se.natusoft.osgi.aps.userservice.config.UserServiceInstConfig;
import se.natusoft.osgi.aps.userservice.entities.RoleEntity;
import se.natusoft.osgi.aps.userservice.entities.UserEntity;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

/**
 * Provides an implementation of the APSSimpleUserService.
 */
@OSGiServiceProvider(instanceFactoryClass = APSSimpleUserServiceProvider.class, threadStart = true)
public class APSSimpleUserServiceProvider implements APSSimpleUserServiceAdmin, APSActivator.InstanceFactory {
    //
    // Private Members
    //

    @Managed
    private BundleContext bundleContext = null;

    @Managed(name = "userServiceLogger", loggingFor = "aps-simple-user-service")
    private APSLogger logger = null;

    @OSGiService
    private APSDataSourceDefService dataSourceDefService = null;

    @OSGiService
    private APSJPAService jpaService = null;

    // This is required for the instance factory method.
    @OSGiService(required = true)
    private APSConfigService configService;

    private APSJPAService.APSJPAEntityManagerProvider entityManagerProvider = null;

    // First field of properties type will automatically be injected with the service instance properties
    // registered with the OSGi server.
    private Properties instProps = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSSimpleUserServiceProvider instance.
     */
    public APSSimpleUserServiceProvider() {}

    /**
     * Creates a new APSSimpleUserServiceProvider instance.
     *
     * @param bundleContext The bundles context.
     * @param logger The logger to log to.
     * @param dataSourceDefService Needed to get data source data.
     * @param jpaService For using JPA.
     */
//    public APSSimpleUserServiceProvider(
//            BundleContext bundleContext,
//            APSLogger logger,
//            APSDataSourceDefService dataSourceDefService,
//            APSJPAService jpaService
//    ) {
//        this.bundleContext = bundleContext;
//        this.logger = logger;
//        this.dataSourceDefService = dataSourceDefService;
//        this.jpaService = jpaService;
//    }

    /**
     * Returns a set of Properties for each instance.
     */
    @Override
    public List<Properties> getPropertiesPerInstance() {
        List<Properties> props = new LinkedList<>();
        if (UserServiceInstConfig.get().instances.isEmpty()) {
            Properties instProps = new Properties();
            instProps.setProperty("instance", "aps-admin-web");
            instProps.setProperty("dsRef", "APSSimpleUserServiceDS");
            instProps.setProperty(APSActivator.InstanceFactory.SERVICE_API_CLASSES_PROPERTY,
                    APSSimpleUserService.class.getName()  +
                    ":" +
                    APSSimpleUserServiceAdmin.class.getName()
            );
            props.add(instProps);
        }
        else {
            for (UserServiceInstConfig.UserServiceInstance inst : UserServiceInstConfig.get().instances) {
                Properties instProps = new Properties();
                instProps.setProperty("instance", inst.name.toString());
                instProps.setProperty("dsRef", inst.dsRef.toString());
                instProps.setProperty(APSActivator.InstanceFactory.SERVICE_API_CLASSES_PROPERTY,
                        APSSimpleUserService.class.getName()  +
                                ":" +
                                APSSimpleUserServiceAdmin.class.getName()
                );
                props.add(instProps);
            }
        }

        return props;
    }

    //
    // Methods
    //

    /**
     * Sets up the EntityManager on the first call there after the created EntityManager is just delivered.
     */
    private APSJPAService.APSJPAEntityManagerProvider getEMP() {
        if (this.entityManagerProvider == null || !this.entityManagerProvider.isValid()) {
            String dsRef = this.instProps.getProperty("dsRef");
            DataSourceDef dsDef = this.dataSourceDefService.lookupByName(dsRef);
            if (dsDef == null) {
                throw new APSPersistenceException("Could not find an 'APSSimpleUserServiceDS' in 'persistence/datasources' configuration!");
            }
            Map<String, String> props = new HashMap<>();
            props.put("javax.persistence.jdbc.user", dsDef.getConnectionUserName());
            props.put("javax.persistence.jdbc.password", dsDef.getConnectionPassword());
            props.put("javax.persistence.jdbc.url", dsDef.getConnectionURL());
            props.put("javax.persistence.jdbc.driver", dsDef.getConnectionDriveName());
            this.entityManagerProvider = this.jpaService.initialize(this.bundleContext, "APSSimpleUserServiceEntities", props);

            setupEmptyDatabase();
        }

        return this.entityManagerProvider;
    }

    /**
     * This will setup a default apsadmin user with and apsadmin role and 'admin' as password if and only if
     * apsadmin doesn't already exist.
     */
    private void setupEmptyDatabase() {
        if (getUser("apsadmin") == null) {
            Role adminrole = getRole("apsadmin");
            if (adminrole == null) {
                adminrole = createRole("apsadmin", "Default APS admin role.");
                ((RoleAdmin)adminrole).setMasterRole(true);
                updateRole(adminrole);
            }

            User admin = createUser("apsadmin");
            admin.getUserProperties().setProperty("Description", "Default APS admin user.");
            ((UserAdmin)admin).addRole(adminrole);
            ((UserEntity)admin).setAuthentication("admin");
            updateUser(admin);
        }
    }

    /**
     * Creates a new role.
     *
     * @param id        The id of the role. This is also the key and cannot be changed.
     * @param description A description of the role. This can be updated afterwards.
     * @return a new Role object representing the role.
     */
    @Override
    public RoleAdmin createRole(String id, String description) {
        EntityManager em = getEMP().createEntityManager();
        em.getTransaction().begin();

        try {
            this.logger.info("Creating role: " + id);
            RoleEntity role = new RoleEntity(id);
            role.setDescription(description);
            em.persist(role);
            em.getTransaction().commit();

            return role;
        }
        catch (RuntimeException re) {
            em.getTransaction().rollback();
            throw re;
        }
        finally {
            em.close();
        }
    }

    /**
     * Updates a role.
     *
     * @param role The role to update.
     */
    @Override
    public void updateRole(Role role) {
        EntityManager em = getEMP().createEntityManager();
        em.getTransaction().begin();

        try {
            RoleEntity mergedRole = em.merge((RoleEntity)role);
            em.persist(mergedRole);
            em.getTransaction().commit();
        }
        catch (RuntimeException re) {
            em.getTransaction().rollback();
            throw re;
        }
        finally {
            em.close();
        }
    }

    /**
     * Deletes a role.
     *
     * @param role The role to delete. This will likely fail if there are users still having this role!
     */
    @Override
    public void deleteRole(Role role) {
        EntityManager em = getEMP().createEntityManager();
        em.getTransaction().begin();

        try {
            this.logger.info("Deleting role: " + role.getId());
            RoleEntity mergedRole = em.merge((RoleEntity)role);
            em.remove(mergedRole);
            em.getTransaction().commit();
        }
        catch (RuntimeException re) {
            em.getTransaction().rollback();
            throw re;
        }
        finally {
            em.close();
        }
    }

    /**
     * Returns all available roles.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<RoleAdmin> getRoles() {
        EntityManager em = getEMP().createEntityManager();

        try {
            Query q = em.createQuery("SELECT role FROM Role role");
            return (List<RoleAdmin>)q.getResultList();
        }
        finally {
            em.close();
        }
    }

    /**
     * Creates a new user. Please note that you get an empty user back. You probably want to
     * add roles and also possibly properties to the user. After you have done that call updateUser(user).
     *
     * @param id The id of the user. This is key so it must be unique.
     *
     * @return A User object representing the new user.
     */
    @Override
    public UserAdmin createUser(String id) {
        EntityManager em = getEMP().createEntityManager();
        em.getTransaction().begin();

        try {
            this.logger.info("Creating user: " + id);
            UserEntity user = new UserEntity(id);
            em.persist(user);
            em.getTransaction().commit();
            return user;
        }
        catch (RuntimeException re) {
            em.getTransaction().rollback();
            throw re;
        }
        finally {
            em.close();
        }
    }

    /**
     * Updates a user.
     *
     * @param user The user to update.
     */
    @Override
    public void updateUser(User user) {
        EntityManager em = getEMP().createEntityManager();
        em.getTransaction().begin();

        try {
            UserEntity mergedUser = em.merge((UserEntity)user);
            em.persist(mergedUser);
            em.getTransaction().commit();
        }
        catch (RuntimeException re) {
            em.getTransaction().rollback();
            throw re;
        }
        finally {
            em.close();
        }
    }

    /**
     * Deletes a user.
     *
     * @param user The user to delete.
     */
    @Override
    public void deleteUser(User user) {
        EntityManager em = getEMP().createEntityManager();
        em.getTransaction().begin();

        try {
            this.logger.info("Deleting user: " + user.getId());
            UserEntity mergedUser = em.merge((UserEntity)user);
            em.remove(mergedUser);
            em.getTransaction().commit();
        }
        catch (RuntimeException re) {
            em.getTransaction().rollback();
            throw re;
        }
        finally {
            em.close();
        }
    }

    /**
     * Returns all users.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<UserAdmin> getUsers() {
        EntityManager em = getEMP().createEntityManager();

        try {
            Query q = em.createQuery("SELECT user FROM SvcUser user");
            return (List<UserAdmin>)q.getResultList();
        }
        finally {
            em.close();
        }
    }

    /**
     * Gets a role by its id.
     *
     * @param roleId The id of the role to get.
     * @return A Role object representing the role or null if role was not found.
     */
    @Override
    public Role getRole(String roleId) {
        EntityManager em = getEMP().createEntityManager();

        try {
            return em.find(RoleEntity.class, roleId);
        }
        finally {
            em.close();
        }
    }

    /**
     * Gets a user by its id.
     *
     * @param userId The id of the user to get.
     * @return A User object representing the user or null if userId was not found.
     */
    @Override
    public User getUser(String userId) {
        EntityManager em = getEMP().createEntityManager();

        try {
            return em.find(UserEntity.class, userId);
        }
        finally {
            em.close();
        }
    }

    /**
     * Sets authentication for the user.
     *
     * @param user The user to set authentication for.
     * @param authentication The authentication to set.
     */
    @Override
    public void setUserAuthentication(User user, String authentication) {
        ((UserEntity)user).setAuthentication(authentication);
        updateUser(user);
    }

    /**
     * Authenticates a user using its user id and user provided authentication.
     *
     * @param user           The User object representing the user to authenticate.
     * @param authentication The user provided authentication data.
     * @param authMethod     Specifies what authentication method is wanted.
     *
     * @return true if authenticated, false otherwise. If true user.isAuthenticated() will also return true.
     */
    @Override
    public boolean authenticateUser(User user, Object authentication, String authMethod) {
        if (authMethod.equals(APSSimpleUserServiceAdmin.AUTH_METHOD_PASSWORD)) {
            // In this case we assume that the 'authentication' value is a string containing a password.
            boolean authenticated = ((UserEntity)user).getAuthentication().equals(authentication);
            ((UserEntity)user).setAuthenticated(authenticated);

            this.logger.info("Authentication: user=" + user.getId() + " result=" + authenticated);

            return authenticated;
        }

        return false;
    }

}
