/* 
 * 
 * PROJECT
 *     Name
 *         APS User Admin Web
 *     
 *     Code Version
 *         0.9.1
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
 *         2013-02-03: Created!
 *         
 */
package se.natusoft.osgi.aps.apsuseradminweb.vaadin.components;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.VerticalLayout;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.EditorIdentifier;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.css.CSS;
import se.natusoft.osgi.aps.tools.web.vaadin.APSTheme;
import se.natusoft.osgi.aps.tools.web.vaadin.components.HTMLFileLabel;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.ComponentHandler;

/**
 * The center view description.
 */
public class Description extends HTMLFileLabel {

    public static DescriptionHandler DESCRIPTION_VIEW = new DescriptionHandler();

    private Description() {
        super("html/description.html", APSTheme.THEME, Description.class.getClassLoader());
    }

    /**
     * This is a component that delivers a layout with margins containing the description, and implementing
     * all interfaces needed to use it as a menu component handler and a default main app view.
     */
    private static class DescriptionHandler extends VerticalLayout implements ComponentHandler, EditorIdentifier {

        /**
         * Creates a new DescriptionHandler.
         */
        public DescriptionHandler() {
            setMargin(true);
            this.setStyleName(CSS.APS_CONTENT_PANEL);

            addComponent(new Description());
        }

        /**
         * @return The component that should handle the item.
         */
        @Override
        public AbstractComponent getComponent() {
            return this;
        }

        @Override
        public String getEditorId() {
            return "description";
        }
    }

}
