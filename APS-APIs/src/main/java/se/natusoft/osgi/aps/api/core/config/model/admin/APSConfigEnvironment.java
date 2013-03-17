/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.1
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
 *     tommy ()
 *         Changes:
 *         2011-08-13: Created!
 *         2012-02-13: Cleaned up!
 *         
 */
package se.natusoft.osgi.aps.api.core.config.model.admin;

/**
 * This represents a configuration environment. For example: Development, Systemtest, Acceptancetest, Production.
 * <p>
 * It is however seldom that simple, so instead of making an enum defining the above, this model represents one
 * environment, and can be instantiated for as many environment alternatives as are needed.
 * <p>
 * As an example there might be more than one development environment due to different stages of development
 * uses different backend services. 
 * <p>
 * Any configuration value annotated with @APSConfigItemDescription(environmentSpecific=true) will have different values for
 * each config environment. Those values that are not ignores the config environment even when provided.
 */
public interface APSConfigEnvironment {

    /**
     * Returns the description of the environment.
     */
    String getDescription();

    /**
     * Returns the name of the environment.
     */
    String getName();

    /**
     * Compares for equality.
     *
     * @param obj The object to compare to.
     *
     * @return true if equal, false otherwise.
     */
    @Override
    boolean equals(Object obj);
}
