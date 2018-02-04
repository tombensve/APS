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

    String TRUE = "true";
    String FALSE = "false";
    String UNKNOWN = "unknown";

    String DEFAULT = "default";

    interface Service {
        String Provider = "service-provider";
        String Category = "service-category";
        String Function = "service-function";
        String PersistenceScope = "service-persistence-scope";

        interface Production {
            String Ready = "service-production-ready";
        }
    }

    interface Uses {
        String Network = "uses.network";
        String Discovery = "uses.discovery";
    }

    interface Provides {
        String Discovery = "provides.discovery";
    }

    interface Network {
        /** This is for APSSender and APSReceiver to use in their properties. */
        String CONNECTION_POINT_NAME = "connection-point-name";
    }

    interface Messaging {
        String Provider = "messaging-provider";

        String Persistent = "messaging-persistent";
        String MultipleReceivers = "messaging-multiple-receivers";
        String Clustered = "messaging-clustered";

        interface Protocol {
            /** This is for router implementations of APSMessageService to be able to delegate to provider of correct protocol. */
            String Name = "aps-messaging-protocol";
        }
    }

    // Values

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
                String General = "general";
                String Discovery = "discovery";
                String JSON = "json";
                String RemoteService = "remote-service";
                String Configuration = "configuration";
                String Database = "database";
                String Filesystem = "filesystem";
                String Time = "time";
                String Messaging = "messaging";
                String Storage = "storage";
            }

            interface PersistenceScope {
                String None = "none";
                String Session = "session";
                String Clustered = "clustered";
                String ClusteredSession = "clustered-session";
                String Permanent = "permanent";
            }

        }

        interface Messaging {
            interface Service {
                String Category = "network";
                String Function = "messaging";
            }
            interface Protocol {
                String ROUTER = "ROUTER";
            }
        }
    }
}
