/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *
 *     Code Version
 *         1.0.0
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
 *         2016-02-27: Created!
 *
 */
package se.natusoft.osgi.aps.api.core.meta;

/**
 * This is a very trivial little service that stores meta data about bundles and services.
 */
public interface APSMetaDataService {

    /**
     * Returns the meta data for the specified owner. This will return an empty MetaData object if no setMetaData(...) have been
     * called and this is the first call to getMetaData(...). Using only this method makes the service provide the MetaData instance.
     *
     * @param owner The owner to get meta data for. Make sure this is very unique!
     */
    MetaData getMetaData(String owner);

    /**
     * Sets a meta data object. In this case it is upp to the caller to make sure the set MetaData object is concurrently callable.
     * This is for when you don't want to use the default implementation of MetaData, but wan't to provide your own. One reason for
     * this could be to provide live realtime data.
     *
     * After this method have been called, a call to getMetaData(...) will return the set object for the owner.
     *
     * @param owner The owner to set new MetaData object for.
     * @param metaData The meta data object to set.
     */
    void setMetaData(String owner, MetaData metaData);

}
