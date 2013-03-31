# APSJPAService

This provides JPA to services and applications. It has a slightly more OSGi friendly API than the org.osgi.service.jpa.EntityManagerFactoryBuilder. The APSOpenJPAProvider however returns an APSJPAService instance that also implements EntityManagerFactoryBuilder. For some reason I haven’t figured out yet, it cannot be registered as a service with the EntityManagerFactoryBuilder interface! The bundle fails to deploy if that is done.

The provided service is using OpenJPA. The service works partly as an extender inspecting deployed bundles for a META-INF/persistence.xml file. When found this is read and some setup is done already there. The persistenceUnitName from the persistence.xml file is used to connect the client later with its configuration. When a JPA using bundle is shut down its JPA setup is automatically cleaned.

Here is an example of usage:

            private APSJPAEntityManagerProvider emp = null;
            ...
            private APSJPAEntityManagerProvider getEMP() {
                if (this.emp == null || !this.emp.isValid()) {
                    DataSourceDef dsDef = this.dataSourceDefService.lookupByName(”MyDS");
                    if (dsDef == null) {
                        throw new SomeException("Could not find an ’MyDs’ in 'persistence/datasources' configuration!");
                    }
                    Map<String, String> props = new HashMap<String, String>();
                    props.put("javax.persistence.jdbc.user", dsDef.getConnectionUserName());
                    props.put("javax.persistence.jdbc.password", dsDef.getConnectionPassword());
                    props.put("javax.persistence.jdbc.url", dsDef.getConnectionURL());
                    props.put("javax.persistence.jdbc.driver", dsDef.getConnectionDriveName());
                    this.emp = this.jpaService.initialize(this.bundleContext, ”myPersistenceUnitName”, props);
                }
                return this.emp;
            }
            ...
            EntityManager em = getEMP().createEntityManager();
            em.getTransaction().begin();
        
            try {
                RoleEntity role = new RoleEntity(id);
                role.setDescription(description);
                em.persist(role);
                em.getTransaction().commit();
            }
            catch (RuntimeException re) {
                em.getTransaction().rollback();
                throw re;
            }
            finally {
                em.close();
            }
                

This code example handles the APSJPAService having been restared or redeployed. When `emp.isValid()` returns false then all you need to do is to call `jpaService.initialize(...)` again. The rest is just POJPA (Plain Old JPA :-)).

## APIs

public _interface_ __APSJPAService__   [se.natusoft.osgi.aps.api.data.jpa.service] {

This service allows an JPA _EntityManager_ to be gotten for a persistent unit name.

So why is this done this way ? Why is not an _EntityManagerFactory_ returned?

The answer to that is that the _EntityManagerFactory_ is internal to the service who is responsible for creating it and for closing it at sometime (stopping of bundle). The client only needs an _EntityManager_ for which the client is responsible after its creation.

The creation of the _EntityManagerFactory_ is delayed until the call to _initialize(...)_. Creating the EMF along with the persistence provider at persistence bundle discovery would limit database connection properties to the persistence.xml file which is less than optimal to put it mildly. This way a client can make use of the _APSDataSourceDefService_ to get the JDBC properties which it can pass along to this service.

The default provider implementation of this service uses OpenJPA which provides its own connection pooling.

__APSJPAEntityManagerProvider initialize(BundleContext bundleContext, String persistenceUnitName, Map<String, String> props) throws APSResourceNotFoundException__

Initializes and returns a provider from the specified properties.

_Returns_

> A configured EntityManager.

_Parameters_

> _bundleContext_ - The context of the client bundle. It is used to locate its persistence provider. 

> _persistenceUnitName_ - The name of the persistent unit defined in persistence.xml. 

> _props_ - Custom properties to configure database, etc. 

public _static_ _interface_ __APSJPAEntityManagerProvider__   [se.natusoft.osgi.aps.api.data.jpa.service] {

Once you get this it is valid until the _APSJPAService_ is stopped (which will happen if the service is redeployed!).

__public boolean isValid()__

Returns true if this instance is valid. If not call APSJPAService.initialize(...) again to get a new instance. It will be invalid if the APSJPAService provider have been restarted.

__EntityManager createEntityManager()__

Creates a new _EntityManager_. You are responsible for closing it!

Please note that the _EntityManager_ caches all referenced entities. If you keep and reuse it for a longer time it can use more memory. For example at [http://docs.jboss.org/ejb3/app-server/tutorial/extended_pc/extended.html](http://docs.jboss.org/ejb3/app-server/tutorial/extended_pc/extended.html) it says that "Usually, an _EntityManager_ in JBoss EJB 3.0 lives and dies within a JTA transaction". This indicates how long-lived the _EntityManager_ should preferably be.

_Returns_

> A configured EntityManager.

__EntityManagerFactory getEntityManagerFactory()__

Returns the underlying _EntityManagerFactory_. This will return null if isValid() return false!

Be very careful what you do with this! It is managed by this service!

}

----

    

