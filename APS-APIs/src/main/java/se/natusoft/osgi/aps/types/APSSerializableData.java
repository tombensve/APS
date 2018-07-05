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
 *         2018-05-26: Created!
 *
 */
package se.natusoft.osgi.aps.types;

import java.io.Serializable;

/**
 * Classes implementing this interface are not Serializable in themselves, but have certain data
 * which can be serialized and be provided from deserialized data. In some cases the whole class
 * might be serializable, but there is no point in serializing everything.
 */
public interface APSSerializableData {

    /**
     * @return A Serializable object of the type provided by getSerializedType().
     */
    Serializable toSerializable();

    /**
     * Receives a deserialized object of the type provided by getSerializedType().
     *
     * @param serializable The deserialized object received.
     */
    void fromDeserialized(Serializable serializable);

    /**
     * @return The serialized type.
     */
    Class getSerializedType();
}
