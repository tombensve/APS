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

