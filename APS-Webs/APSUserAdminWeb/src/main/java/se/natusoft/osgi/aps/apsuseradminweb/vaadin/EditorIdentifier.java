/* 
 * 
 * PROJECT
 *     Name
 *         APS User Admin Web
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This is an administration web for aps-simple-user-service that allows editing of roles and users.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-09-02: Created!
 *         
 */
package se.natusoft.osgi.aps.apsuseradminweb.vaadin;

/**
 * This is used to identify if shown editor is the same as the new set editor to be able to toggle on and off.
 */
public interface EditorIdentifier {

    /**
     * Returns the id of the editor.
     */
    public String getEditorId();
}
