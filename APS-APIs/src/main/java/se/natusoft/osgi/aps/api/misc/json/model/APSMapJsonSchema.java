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
package se.natusoft.osgi.aps.api.misc.json.model;

import java.util.Map;

/**
 * This represents a JSONish structure with `List<Object>`, `Map<String,Object>`, `String`, `Boolean`, `Number`, and null values,
 * where Òbject` __always__ refers to the same list of types.
 *
 * This represents a JSON schema according to APSGroovyToolsLib/MapJsonDocValidator. Needs GroovyRuntime deployed in server,
 * but can be used from Java.
 *
 * ----------------------------------------------------------------------------------------------------
 *
 * The reason for APSConfig and APSMapJsonSchema is that both really use exactly the same structure,
 * but contains very different information. I thought it might be confusing to declare them both as
 * maps. I think that the code is also more readable with this.
 *
 * To make things easier there is a util called APSConfigDelegator that takes a pure _Map_ and delegates
 * all calls to it. The _Map_ is also wrapped to be unmodifiable. APSConfigDelegator implements both
 * APSConfig and APSMapJsonSchema.
 */
public interface APSMapJsonSchema extends Map<String, Object> {

    static APSMapJsonSchema delegateTo(Map<String, Object> mapJson) {
        return new APSMapJsonDelegator(mapJson);
    }
}
