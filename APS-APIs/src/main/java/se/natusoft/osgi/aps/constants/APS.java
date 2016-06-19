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
 *         2016-06-12: Created!
 *         
 */
package se.natusoft.osgi.aps.constants;

/**
 * Hierarchy of constants.
 */
public interface APS {


    String SERVICE_PROVIDER = "service-provider";

    String SERVICE_CATEGORY = "service-category";

    String SERVICE_FUNCTION = "service-function";

    String SERVICE_PRODUCTION_READY = "service-production-ready";

    String TRUE = "true";
    String FALSE = "false";

    interface Service {
        String Provider = SERVICE_PROVIDER;
        String Category = SERVICE_CATEGORY;
        String Function = SERVICE_FUNCTION;

        interface Production {
            String Ready = SERVICE_PRODUCTION_READY;
        }
    }

    interface Uses {
        String Network = "uses.network";
        String Discovery = "uses.discovery";
    }

    interface Provides {
        String Discovery = "provides.discovery";
    }

    interface Messaging {
        String SERVICE_CATEGORY = "network";
        String SERVICE_FUNCTION = "messaging";

        String PROVIDER = "messaging-provider";
        String PERSISTENT = "messaging-persistent";
        String MULTIPLE_RECEIVERS = "messaging-multiple-receivers";

    }

    interface Value {
        interface Service {
            interface Category {
                String Network = "network";
                String Web = "web";
                String Storage = "storage";
                String Security = "security";
                String Authentication = "authentication";
                String Transform = "transform";
                String DataFormats = "data.formats";
                String Misc = "misc";
            }

            interface Function {
                String Discovery = "discovery";
                String JSON = "json";
                String RemoteService = "remote-service";
                String Configuration = "configuration";
                String Database = "database";
                String Filesystem = "filesystem";
                String Time = "time";
            }
        }
    }
}
