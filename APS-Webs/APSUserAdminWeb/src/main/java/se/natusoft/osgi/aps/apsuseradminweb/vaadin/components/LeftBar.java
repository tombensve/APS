/* 
 * 
 * PROJECT
 *     Name
 *         APS User Admin Web
 *     
 *     Code Version
 *         0.11.0
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
 *         2011-09-18: Created!
 *         
 */
package se.natusoft.osgi.aps.apsuseradminweb.vaadin.components;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.css.CSS;

/**
 * The left bar containing the menu.
 */
public class LeftBar extends Panel {

    /**
     * Creates a new LeftBar setting up some style.
     */
    public LeftBar() {
        VerticalLayout vl = new VerticalLayout();
        setStyleName(CSS.APS_LEFTBAR);
        vl.setMargin(true);
        setContent(vl);
    }

    /**
     * Adds a component to the LeftBar.
     *
     * @param component The component to add.
     */
    public void addComponent(Component component) {
        ((VerticalLayout)getContent()).addComponent(component);
    }
}
