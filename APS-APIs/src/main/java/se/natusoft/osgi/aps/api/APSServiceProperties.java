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
package se.natusoft.osgi.aps.api;

import java.util.Properties;

/**
 * These are "properties" for use in service registrations.
 */
public interface APSServiceProperties {

    abstract class Security {

        public static final String Key = "aps.props.security";

        public static final String Secure = "secure";
        public static final String NonSecure = "nonsecure";

        public static void setSecure(Properties properties) { properties.setProperty(Key, Secure); }
        public static void setNonsecure(Properties properties) { properties.setProperty(Key, NonSecure); }
        public static String getSecureLookupCriteria() { return "(" + Key + "=" + Secure + ")"; }
        public static String getNonsecureLookupCriteria() { return "(" + Key + "=" + NonSecure + ")"; }
    }

    interface Instance {
        String Name = "aps.svc.props.instance.name";
    }
}
