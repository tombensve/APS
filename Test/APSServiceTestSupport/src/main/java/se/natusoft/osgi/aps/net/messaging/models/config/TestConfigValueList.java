/*
 *
 * PROJECT
 *     Name
 *         APS Service Test Support
 *
 *     Code Version
 *         1.0.0
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
 *         2015-01-18: Created!
 *
 */
package se.natusoft.osgi.aps.net.messaging.models.config;

import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;

public class TestConfigValueList extends TestConfigList<APSConfigValue>  implements APSConfigValueList {}
