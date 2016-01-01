/* 
 * 
 * PROJECT
 *     Name
 *         APS TCPIP Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides an implementation of APSTCPIPService. This service does not provide any security of its own,
 *         but makes use of APSTCPSecurityService, and APSUDPSecurityService when available and configured for
 *         security.
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
 *         2016-01-01: Created!
 *         
 */
package se.natusoft.osgi.aps.tcpipsvc.meta

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.meta.APSMetaDataBean
import se.natusoft.osgi.aps.api.core.meta.APSMetaDataService
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

/**
 *
 */
@CompileStatic
@TypeChecked
@OSGiServiceProvider
class APSTCPIPMetaDataServiceProvider implements APSMetaDataService {

    @Managed(name = "META-DATA")
    private APSTCPIPServiceMetaData metaData

    /**
     * Returns meta data about the service as a JSON object..
     */
    @Override
    APSMetaDataBean getMetaDataBean() {
        return this.metaData
    }

}
