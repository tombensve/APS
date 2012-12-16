/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         A more advanced configuration service that uses annotated interfaces to
 *         describe and provide access to configuration. It supports structured
 *         configuration models.
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
 *         2012-02-19: Created!
 *         
 */
package se.natusoft.osgi.aps.core.config.store;

import java.util.List;
import java.util.Set;

/**
 * This is implemented by both persistent and memory stores.
 */
public interface ConfigStoreInfo {

    /**
     * Returns the available configuration ids.
     */
    public Set<String> getAvailableConfigurationIds();

    /**
     * Returns the available versions for a specified configuration id.
     *
     * @param configId The configuration id to get available versions for.
     */
    public List<String> getAvailableVersions(String configId);

}
