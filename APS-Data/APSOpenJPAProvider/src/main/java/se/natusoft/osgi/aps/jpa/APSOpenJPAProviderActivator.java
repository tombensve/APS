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
 *         2012-08-19: Created!
 *         
 */
package se.natusoft.osgi.aps.jpa;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import se.natusoft.osgi.aps.api.data.jpa.service.APSJPAService;
import se.natusoft.osgi.aps.jpa.service.APSOpenJPAServiceProvider;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.util.Dictionary;
import java.util.Properties;

public class APSOpenJPAProviderActivator implements BundleActivator {
    //
    // Private Members
    //

    private APSOpenJPAServiceProvider apsJPAServiceProvider = null;

    private ServiceRegistration apsOpenJPAServiceReg = null;

    private ServiceRegistration osgiEntityManagerFactoryBuilderServiceReg = null;

    private APSLogger logger = null;
    
    //
    // Bundle Start.
    //
    
    @Override
    public void start(BundleContext context) throws Exception {
        this.logger = new APSLogger(System.out);
        this.logger.start(context);

        Dictionary apsJPAServiceProps = new Properties();
        apsJPAServiceProps.put(Constants.SERVICE_PID, APSOpenJPAServiceProvider.class.getName());
        this.apsJPAServiceProvider = new APSOpenJPAServiceProvider(this.logger, context);
        this.apsOpenJPAServiceReg = context.registerService(APSJPAService.class.getName(), apsJPAServiceProvider, apsJPAServiceProps);

        // This fails due to that we build with org.osgi:org.osgi.enterprise:4.2.0 and that is not what the Virgo server apparently
        // has even though I dropped this jar into its pickup catalog. This results in the following error:
        //        Caused by: java.lang.LinkageError: loader constraint violation in interface itable initialization: when resolving method
        // "se.natusoft.osgi.aps.jpa.service.APSOpenJPAServiceProvider.createEntityManagerFactory(Ljava/util/Map;)
        // Ljavax/persistence/EntityManagerFactory;" the class loader (instance of
        // org/eclipse/virgo/kernel/userregion/internal/equinox/KernelBundleClassLoader) of the current class,
        // se/natusoft/osgi/aps/jpa/service/APSOpenJPAServiceProvider, and the class loader (instance of
        // org/eclipse/virgo/kernel/userregion/internal/equinox/KernelBundleClassLoader) for interface
        // org/osgi/service/jpa/EntityManagerFactoryBuilder have different Class objects for the type ceProvider.createEntityManagerFactory
        // (Ljava/util/Map;)Ljavax/persistence/EntityManagerFactory; used in the signature

        // I leave this commented out for now.

//        Dictionary entityManagerFactoryBuilderServiceProps = new Properties();
//        entityManagerFactoryBuilderServiceProps.put(Constants.SERVICE_PID, EntityManagerFactoryBuilder.class.getName());
//        this.osgiEntityManagerFactoryBuilderServiceReg =
//                context.registerService(
//                        EntityManagerFactoryBuilder.class.getName(),
//                        apsJPAServiceProvider,
//                        entityManagerFactoryBuilderServiceProps
//                );

        context.addBundleListener(this.apsJPAServiceProvider);
    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.apsOpenJPAServiceReg != null) {
            try {
                this.apsOpenJPAServiceReg.unregister();
                this.apsOpenJPAServiceReg = null;
            }
            catch (IllegalStateException ise) { /* This is OK! */ }
        }

//        if (this.osgiEntityManagerFactoryBuilderServiceReg != null) {
//            try {
//                this.osgiEntityManagerFactoryBuilderServiceReg.unregister();
//                this.osgiEntityManagerFactoryBuilderServiceReg = null;
//            }
//            catch (IllegalStateException ise) { /* This is OK! */ }
//        }

        if (this.apsJPAServiceProvider != null) {
            context.removeBundleListener(this.apsJPAServiceProvider);
            this.apsJPAServiceProvider.closeAll();
            this.apsJPAServiceProvider = null;
        }

        if (this.logger != null) {
            this.logger.stop(context);
            this.logger = null;
        }
    }

}
