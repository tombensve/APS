/* 
 * 
 * PROJECT
 *     Name
 *         APSLogServiceProvider OSGi Bundle
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Application Platform Services root maven parent.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2011 by Biltmore Group AB All rights reserved.
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
 *         2011-08-04: Created!
 *         
 */
package se.natusoft.osgi.aps.log.sevice;

import org.apache.log4j.xml.DOMConfigurator;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * Provides an implementation of the standard OSGi LogService using log4j for logging.
 */
public class APSLogServiceProvider implements LogService {
    //
    // Private Members
    //
    
    
    //
    // Constructors
    //
    
    public APSLogServiceProvider() {
        DOMConfigurator dcf;
    }
    
    //
    // Methods
    //
    

    @Override
    public void log(int level, String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void log(int level, String message, Throwable exception) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void log(ServiceReference sr, int level, String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void log(ServiceReference sr, int level, String message, Throwable exception) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
