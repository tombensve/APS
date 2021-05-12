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
 *         2011-08-16: Created!
 *
 */
package se.natusoft.aps.api.core.platform.service;

import se.natusoft.aps.api.core.platform.model.NodeInfo;
import se.natusoft.aps.types.APSValue;
import se.natusoft.aps.types.APSHandler;

import java.util.List;

/**
 * Provides information about the platform instance.
 */
public interface APSNodeInfoService {

    /**
     * Delivers the descriptions of currently knows nodes.
     *
     * @param handler The handler to receive the node descriptions.
     */
    void nodeDescriptions(APSHandler<APSValue<List<NodeInfo>>> handler);

    /**
     * Delivers info about the local node.
     *
     * @param handler The handler to receive the local node info.
     */
    void localNode(APSHandler<APSValue<NodeInfo>> handler);
}
