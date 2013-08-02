/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Tools
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         This provides some utility classes for web applications.
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
 *         2012-03-17: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi;

import se.natusoft.osgi.aps.tools.web.vaadin.models.HierarchicalModel;

/**
 * This must be implemented by a provider of menu entries.
 */
public interface MenuBuilder<MenuItemRepresentative> {

    /**
     * This should add menu entries to the received menu model.
     *
     * @param menuModel The model to add menu entries to.
     */
    public void buildMenuEntries(HierarchicalModel<MenuItemData<MenuItemRepresentative>> menuModel);
}
