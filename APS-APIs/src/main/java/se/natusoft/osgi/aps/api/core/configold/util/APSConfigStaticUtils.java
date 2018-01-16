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
 *         2014-08-13: Created!
 *
 */
package se.natusoft.osgi.aps.api.core.configold.util;

import se.natusoft.osgi.aps.api.core.configold.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.configold.model.admin.APSConfigReference;
import se.natusoft.osgi.aps.api.core.configold.model.admin.APSConfigValueEditModel;

/**
 * This class contains static utility methods and is not instantiable.
 */
public class APSConfigStaticUtils {

    private APSConfigStaticUtils() {}

    /**
     * Extracts an APSConfigEditModel from an APSConfigReference. If the reference tip model is an APSConfigEditModel
     * it is returned, if not its parent is returned.
     *
     * @param ref The ref to get the APSConfigEditModel from.
     */
    public static APSConfigEditModel refToEditModel(APSConfigReference ref) {
        APSConfigValueEditModel cvem = ref.getConfigValueEditModel();
        return (cvem instanceof APSConfigEditModel) ? (APSConfigEditModel)cvem : cvem.getParent();
    }

}
